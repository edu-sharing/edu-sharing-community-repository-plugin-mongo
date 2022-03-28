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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDbConfiguration {

    @Autowired MongoConfig mongoConfig;

    @Bean
    public MongoClientSettings mongoClientSettings(){
        Config rootConfig = LightbendConfigLoader.get();
        ConnectionString connectionString = new ConnectionString(mongoConfig.getConnectionString());
        return MongoClientSettings.builder().applyConnectionString(connectionString).build();
    }

    @Bean
    public MongoClient mongoClient(MongoClientSettings settings) {
        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabase mongoDatabase(MongoClient mongoClient){
        return mongoClient.getDatabase(mongoConfig.getDatabase());
    }

}
