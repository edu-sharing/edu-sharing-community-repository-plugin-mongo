package org.edu_sharing.plugin_mongo.oplog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Represents an operational log entry for persisting and tracking actions or events
 * associated with MongoDB and Alfresco transaction workflows. This entity is stored
 * in the MongoDB collection "mongo_alf_oplog".
 *
 * Each log entry includes:
 * - A unique identifier.
 * - A log data payload implementing {@link MongoAlfOpLogData}.
 * - A timestamp indicating when the log entry was created, indexed in descending order
 *   to facilitate operations like querying recent log entries.
 *
 * The purpose of this class is to enable structured and timestamped logging of
 * operational events, providing support for transaction-bound workflows, log tracking,
 * and system integrations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("mongo_alf_oplog")
public class MongoAlfOpLog {
    @Id
    private String id;
    private MongoAlfOpLogData data;
    @Indexed(direction = IndexDirection.DESCENDING)
    private Date timestamp;
}


