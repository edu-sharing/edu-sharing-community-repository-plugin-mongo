package org.edu_sharing.plugin_mongo.relation;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class NodeRelation {
    @BsonId
    public String node;
    public List<RelationData> relations = new ArrayList<>();

    public NodeRelation(String node) {
        this.node = node;
    }
}

