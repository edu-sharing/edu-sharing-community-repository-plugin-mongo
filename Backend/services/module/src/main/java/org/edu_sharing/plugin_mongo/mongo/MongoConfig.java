package org.edu_sharing.plugin_mongo.mongo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MongoConfig implements Serializable {
    private String connectionString;
    private String database;
}
