package org.edu_sharing.plugin_mongo.mongo;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.util.List;

@Value
@Builder
public class MongoConfig implements Serializable {
    String connectionString;
    String database;
}
