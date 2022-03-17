package org.edu_sharing.plugin_mongo.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import lombok.Setter;
import org.edu_sharing.alfresco.lightbend.LightbendConfigLoader;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Configuration;

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

class MongoDatabaseFactoryBean implements FactoryBean<MongoDatabase> {
    private final MongoClient client;
    @Setter
    private String databaseName = "edu-sharing";

    private MongoDatabase mongoDatabase;

    public MongoDatabaseFactoryBean(MongoClient client) {
        this.client = client;
    }

    @Override
    public MongoDatabase getObject() {
        if(mongoDatabase == null){
            mongoDatabase = client.getDatabase(databaseName);
        }
        return mongoDatabase;
    }

    @Override
    public Class<?> getObjectType() {
        return MongoDatabase.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
