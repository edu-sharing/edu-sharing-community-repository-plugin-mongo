package org.edu_sharing.plugin_mongo.scope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.springframework.data.annotation.TypeAlias;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("DeleteScopeGroupOpLogData")
public class DeleteScopeGroupOpLogData implements MongoAlfOpLogData {
    private String scopeId;
}
