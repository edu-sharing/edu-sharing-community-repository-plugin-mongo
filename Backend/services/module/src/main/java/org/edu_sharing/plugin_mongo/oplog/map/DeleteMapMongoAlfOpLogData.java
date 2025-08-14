package org.edu_sharing.plugin_mongo.oplog.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.springframework.data.annotation.TypeAlias;


@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("DeleteMapMongoAlfOpLogData")
public class DeleteMapMongoAlfOpLogData implements MongoAlfOpLogData {
    private String nodeId;
}


