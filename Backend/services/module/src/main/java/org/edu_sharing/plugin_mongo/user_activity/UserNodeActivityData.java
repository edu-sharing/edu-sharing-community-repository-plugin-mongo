package org.edu_sharing.plugin_mongo.user_activity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Document("user_node_events")
@CompoundIndex(def = "{'nodeId': 1, 'userId': 1, 'type': 1}", unique = true)
public class UserNodeActivityData {
    String nodeId;
    String userId;
    String type;
    @Indexed
    Date timestamp;
}
