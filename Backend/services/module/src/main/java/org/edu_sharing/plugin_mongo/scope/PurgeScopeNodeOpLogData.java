package org.edu_sharing.plugin_mongo.scope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.springframework.data.annotation.TypeAlias;

/**
 * a node was finally deleted (purged from the trashcan or deleted without archiving)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("PurgeScopeNodeOpLogData")
public class PurgeScopeNodeOpLogData implements MongoAlfOpLogData {
    private String nodeId;
}
