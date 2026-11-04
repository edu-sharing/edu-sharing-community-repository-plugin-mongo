package org.edu_sharing.plugin_mongo.scope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.springframework.data.annotation.TypeAlias;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("UploadScopeNodeOpLogData")
public class UploadScopeNodeOpLogData implements MongoAlfOpLogData {
    private String scopeId;
    private String nodeId;
    private String uploader;
}
