package org.edu_sharing.plugin_mongo.oplog.authority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("logging")
@TypeAlias("DeleteAuthorityLoggingAction")
public class DeleteAuthorityMongoAlfOpLogData implements MongoAlfOpLogData {
    @Id
    private String id;
    private String nodeId;
    private Date timestamp;

}


