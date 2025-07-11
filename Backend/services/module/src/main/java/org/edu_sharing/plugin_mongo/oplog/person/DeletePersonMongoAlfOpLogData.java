package org.edu_sharing.plugin_mongo.oplog.person;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.springframework.data.annotation.TypeAlias;


@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("DeletePersonLoggingAction")
public class DeletePersonMongoAlfOpLogData implements MongoAlfOpLogData {
    private String nodeId;
    private String username;

}


