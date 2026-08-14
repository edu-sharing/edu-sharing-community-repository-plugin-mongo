package org.edu_sharing.plugin_mongo.user_activity;

import lombok.RequiredArgsConstructor;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogRetryHandler;
import org.springframework.stereotype.Component;

/**
 * Registered as a separate bean (rather than on {@link UserNodeActivityTracker} itself) purely to
 * avoid it implementing any interface - see that class's javadoc for why that would break its
 * {@code @EventListener} registration. {@code RetryFailedOrMissingMongoAlfOpLogJob} picks up any
 * leftover {@link UserNodeActivityData} oplog record (e.g. from a crash between commit and the
 * async write in {@code UserNodeActivityTracker} finishing) and dispatches to this handler purely
 * by the persisted data's runtime type - it has no way to resurrect the original callback, since
 * that was a lambda closure, never itself persisted.
 */
@Component
@RequiredArgsConstructor
public class UserNodeActivityRetryHandler implements MongoAlfOpLogRetryHandler<UserNodeActivityData> {

    private final UserNodeActivityDataRepository userNodeActivityDataRepository;

    @Override
    public Class<UserNodeActivityData> getRetryableType() {
        return UserNodeActivityData.class;
    }

    @Override
    public void retry(MongoAlfOpLogData opLogData) {
        if (!(opLogData instanceof UserNodeActivityData data)) {
            throw new IllegalArgumentException("Oplog data must be of type " + UserNodeActivityData.class.getSimpleName() + "!");
        }
        userNodeActivityDataRepository.saveWithServerTimestamp(data);
    }
}
