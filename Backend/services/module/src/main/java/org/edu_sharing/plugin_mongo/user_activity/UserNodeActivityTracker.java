package org.edu_sharing.plugin_mongo.user_activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.edu_sharing.alfresco.service.guest.GuestService;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogService;
import org.edu_sharing.repository.server.tools.security.RunAsSystem;
import org.edu_sharing.service.tracking.ActivityOnNodeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.Executor;

/**
 * Service responsible for tracking user activity on nodes. These actions are backed by the operational log.
 * This service listens for {@link ActivityOnNodeEvent} events and logs user interactions with nodes.
 *
 * Responsibilities:
 * - Ignoring events from guest users, system users, or null authority names.
 * - Resolving the user associated with the authority name from the {@link PersonService}.
 * - Registering actions such as node interactions in the operational log using the {@link MongoAlfOpLogService}.
 *
 * Dependencies:
 * - {@link GuestService}: Used to determine whether a user is a guest.
 * - {@link UserNodeActivityDataRepository}: Saves activity data into the MongoDB repository.
 * - {@link PersonService}: Resolves user information from the authority name.
 * - {@link MongoAlfOpLogService}: Handles registering operational log entries.
 *
 * Event Handling:
 * - The {@code handleActivityOnNodeEvent} method listens for {@link ActivityOnNodeEvent} instances.
 * - Filters out events based on guest users, system users, and invalid authority names.
 * - Extracts activity details, including node reference, user ID and event type.
 * - Registers the activity via {@link MongoAlfOpLogService}, which binds a listener to the CURRENT
 *   Alfresco transaction: {@code afterCommit()} fires only if that transaction actually commits, and
 *   is skipped on rollback; if {@link org.alfresco.repo.transaction.RetryingTransactionHelper} retries
 *   the surrounding unit of work, only the attempt that actually commits gets an {@code afterCommit()}
 *   call, so a retried action is not double-counted.
 *
 * <p>This method deliberately does NOT use {@code @Async}: doing so would run it on a thread with no
 * Alfresco transaction bound, so {@code registerOpLogAction} would take its "no transaction found"
 * path and write immediately - with no rollback/retry protection at all (this was the case here
 * before, and both problems above - phantom activity records for actions that never actually
 * committed, and duplicate records across retries - applied). Instead, only the actual MongoDB write
 * (the slow part, in a network round trip) is offloaded to {@code taskExecutor} from inside
 * {@code afterCommit()}, keeping request latency low without losing that protection. The write
 * timestamp is assigned by MongoDB itself at write time (see
 * {@link UserNodeActivityDataRepositoryCustom#saveWithServerTimestamp}), not captured here, since
 * only the server knows exactly when the write is actually applied. Must use that method, not the
 * inherited {@code save(...)}, for any write to this repository. That write-time value is only a
 * polling cursor, though - it can lag well behind (e.g. the retry job's default 6h offset) when
 * the activity actually happened, so {@code occurredAt} is captured here, at event-handling time,
 * for anything that needs to show users when the activity actually occurred.
 *
 * <p>This class deliberately implements NO interfaces: {@code @RunAsSystem} forces Spring AOP to
 * proxy this bean, and with zero interfaces implemented Spring can only use a CGLIB (subclass)
 * proxy, which inherits every public method - so {@code @EventListener} always finds
 * {@code handleActivityOnNodeEvent}. Adding an interface here (this used to also implement
 * {@code MongoAlfOpLogRetryHandler} directly) makes Spring prefer a JDK dynamic proxy instead,
 * which exposes ONLY the declared interface(s) - not this class's own methods - so
 * {@code @EventListener} registration fails at context startup with "Need to invoke method
 * ... but not found in any interface(s) of the exposed proxy type". The retry handler for
 * {@link UserNodeActivityData} therefore lives in the separate {@link UserNodeActivityRetryHandler}
 * bean instead of here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserNodeActivityTracker {

    private final GuestService guessService;
    private final UserNodeActivityDataRepository userNodeActivityDataRepository;
    private final PersonService personService;
    private final MongoAlfOpLogService opLogService;
    // SpringConfigRoot#taskExecutor is currently the only Executor bean in the application, so
    // this resolves unambiguously by type; a @Qualifier would need repository-mongo's own
    // lombok.config (unlike the core repo's) to actually reach the generated constructor parameter.
    private final Executor taskExecutor;

    @RunAsSystem
    @EventListener
    public void handleActivityOnNodeEvent(ActivityOnNodeEvent event) {
        if (event.getAuthorityName() == null
                || guessService.getAllGuestAuthorities().contains(event.getAuthorityName())
                || event.getAuthorityName().equals(AuthenticationUtil.getSystemUserName())
        ) {
            return;
        }

        NodeRef person = personService.getPerson(event.getAuthorityName());
        if (person == null) {
            return;
        }

        UserNodeActivityData data = new UserNodeActivityData(
                null,
                event.getNodeRef().getId(),
                person.getId(),
                event.getAuthorityName(),
                event.getType().name(),
                null,
                new Date()
        );

        opLogService.registerOpLogAction(data, saved -> taskExecutor.execute(() -> persist(saved)));
    }

    private void persist(UserNodeActivityData data) {
        try {
            userNodeActivityDataRepository.saveWithServerTimestamp(data);
        } catch (Exception e) {
            log.error("Failed to persist user node activity for node {} user {}: {}",
                    data.getNodeId(), data.getUsername(), e.getMessage(), e);
        }
    }
}
