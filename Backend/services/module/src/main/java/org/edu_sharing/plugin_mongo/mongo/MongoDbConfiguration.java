package org.edu_sharing.plugin_mongo.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import org.bson.Document;
import org.edu_sharing.alfresco.lightbend.LightbendConfigLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDbConfiguration {

    @Bean
    public MongoConfig mongoConfig() {
        Config mongoConfig = LightbendConfigLoader.get().getConfig("mongo");
        return MongoConfig.builder()
                .connectionString(mongoConfig.getString("connectionString"))
                .database(mongoConfig.getString("database"))
                .build();
    }

    @Bean
    public MongoClientSettings mongoClientSettings(MongoConfig mongoConfig){
        Config rootConfig = LightbendConfigLoader.get();
        ConnectionString connectionString = new ConnectionString(mongoConfig.getConnectionString());
        return MongoClientSettings.builder().applyConnectionString(connectionString).build();
    }

    @Bean
    public MongoClient mongoClient(MongoClientSettings settings) {
        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabase mongoDatabase(MongoClient mongoClient, MongoConfig mongoConfig){
        return mongoClient.getDatabase(mongoConfig.getDatabase());
    }

}
