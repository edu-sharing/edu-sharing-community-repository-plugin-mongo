package org.edu_sharing.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.typesafe.config.Config;
import org.edu_sharing.alfresco.lightbend.LightbendConfigLoader;
import org.springframework.beans.factory.FactoryBean;

public class MongoClientFactoryBean implements FactoryBean<MongoClient> {
    @Override
    public MongoClient getObject() throws Exception {
        Config rootConfig = LightbendConfigLoader.get();
        String connectionString = rootConfig.getString("mongo.connectionString");
        return MongoClients.create(connectionString);
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
