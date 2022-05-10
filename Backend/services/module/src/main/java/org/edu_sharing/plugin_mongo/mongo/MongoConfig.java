package org.edu_sharing.plugin_mongo.mongo;

import lombok.Data;
import org.edu_sharing.lightbend.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mongo")
public class MongoConfig {
    String connectionString;
    String database;
}
