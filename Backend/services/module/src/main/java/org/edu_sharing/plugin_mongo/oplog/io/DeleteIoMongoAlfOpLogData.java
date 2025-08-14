package org.edu_sharing.plugin_mongo.oplog.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.springframework.data.annotation.TypeAlias;


@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("DeleteIoMongoAlfOpLogData")
public class DeleteIoMongoAlfOpLogData implements MongoAlfOpLogData {
    private String nodeId;
}


