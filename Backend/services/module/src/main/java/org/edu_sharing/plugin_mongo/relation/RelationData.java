package org.edu_sharing.plugin_mongo.relation;

import lombok.Data;

import java.util.Date;

@Data
public class RelationData {
    private String node;
    private String creator;
    private Date timestamp;
    private RelationType type;
}
