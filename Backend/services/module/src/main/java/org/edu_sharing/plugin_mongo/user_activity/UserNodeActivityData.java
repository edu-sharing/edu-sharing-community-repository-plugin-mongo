package org.edu_sharing.plugin_mongo.user_activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonapi.io.github.classgraph.json.Id;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.edu_sharing.service.tracking.user_tracking.UserNodeActivity;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("user_node_events")
@CompoundIndex(name = "timestamp_userId_idx", def = "{'timestamp': -1, 'userId': 1}")
public class UserNodeActivityData implements MongoAlfOpLogData, UserNodeActivity {
    @Id
    String id;
    String nodeId;
    String userId;
    String username;
    String type;
    Date timestamp;
}
