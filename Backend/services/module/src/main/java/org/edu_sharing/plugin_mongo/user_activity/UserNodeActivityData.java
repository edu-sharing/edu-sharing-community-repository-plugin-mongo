package org.edu_sharing.plugin_mongo.user_activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("user_node_events")
public class UserNodeActivityData implements MongoAlfOpLogData {
    String nodeId;
    String userId;
    String type;
    @Indexed
    Date timestamp;
}
