package org.edu_sharing.plugin_mongo.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import lombok.Setter;
import org.edu_sharing.alfresco.lightbend.LightbendConfigLoader;
import org.springframework.beans.factory.FactoryBean;

public class MongoClientFactoryBean implements FactoryBean<MongoClient> {

    private MongoClient mongoClient;

    @Override
    public MongoClient getObject() {
        if(mongoClient == null) {
            Config rootConfig = LightbendConfigLoader.get();
            String connectionString = rootConfig.getString("mongo.connectionString");
            mongoClient = MongoClients.create(connectionString);
        }

        return mongoClient;
    }

    @Override
    public Class<?> getObjectType() {
        return MongoClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

