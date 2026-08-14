package org.edu_sharing.plugin_mongo.user_activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.edu_sharing.service.tracking.user_tracking.UserNodeActivity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("user_node_events")
@CompoundIndex(name = "timestamp_username_idx", def = "{'timestamp': -1, 'username': 1}")
public class UserNodeActivityData implements MongoAlfOpLogData, UserNodeActivity {
    @Id
    String id;
    String nodeId;
    @Indexed
    String userId;
    String username;
    String type;
    /**
     * MongoDB server-assigned write time (via $currentDate in
     * {@link UserNodeActivityDataRepositoryCustom#saveWithServerTimestamp}), used purely as the
     * polling cursor for {@code GenericTimebaseTracker} - NOT when the activity actually happened.
     * Async dispatch and the retry job can delay the write well past the real event time (the
     * retry job's default offset alone is 6h), so this must never be shown to users as "when" an
     * activity occurred. See {@link #occurredAt} for that.
     */
    Date timestamp;
    /**
     * When the activity actually happened, captured client-side at event-handling time (see
     * {@link UserNodeActivityTracker#handleActivityOnNodeEvent}) - this is what should be shown to
     * users, never {@link #timestamp}.
     */
    Date occurredAt;

    /**
     * Overrides the Lombok-generated getter: documents written before this field existed have it
     * as null in MongoDB, and callers (REST API consumers, the tracker's ES indexing) should not
     * each have to know to fall back to {@link #timestamp} themselves - normalizing it once here
     * means every reader, present and future, gets a populated value for free.
     */
    public Date getOccurredAt() {
        return occurredAt != null ? occurredAt : timestamp;
    }
}
