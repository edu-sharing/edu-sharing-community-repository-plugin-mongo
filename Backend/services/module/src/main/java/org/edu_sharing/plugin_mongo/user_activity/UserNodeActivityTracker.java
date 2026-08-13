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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsible for tracking user activity on nodes. These actions are backed by the operational log.
 * This service listens for {@link ActivityOnNodeEvent} events and processes the events asynchronously to
 * log user interactions with nodes.
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
 * - Registers the activity via {@link MongoAlfOpLogService}. Note that because this method is
 *   {@code @Async}, it runs on a thread with no Alfresco transaction bound, so
 *   {@code registerOpLogAction} takes its "no transaction found" path and writes immediately -
 *   it does NOT wait for any transaction commit here, despite what that path's log message may
 *   suggest. The write timestamp is assigned by MongoDB itself at write time (see
 *   {@link UserNodeActivityDataRepositoryCustom#saveWithServerTimestamp}), not captured here,
 *   since only the server knows exactly when the write is actually applied. Must use that method,
 *   not the inherited {@code save(...)}, for any write to this repository.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserNodeActivityTracker {

    private final GuestService guessService;
    private final UserNodeActivityDataRepository userNodeActivityDataRepository;
    private final PersonService personService;
    private final MongoAlfOpLogService opLogService;


    @Async
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

        opLogService.registerOpLogAction(new UserNodeActivityData(
                null,
                event.getNodeRef().getId(),
                person.getId(),
                event.getAuthorityName(),
                event.getType().name(),
                null
        ), userNodeActivityDataRepository::saveWithServerTimestamp);
    }
}
