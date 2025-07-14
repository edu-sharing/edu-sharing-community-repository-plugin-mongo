package org.edu_sharing.plugin_mongo.oplog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

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


