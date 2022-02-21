package org.edu_sharing.plugin_mongo.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;

public class MongoDatabaseFactoryBean implements FactoryBean<MongoDatabase> {
    private final MongoClient client;
    @Setter
    private String databaseName = "edu-sharing";

    private MongoDatabase mongoDatabase;

    public MongoDatabaseFactoryBean(MongoClient client) {
        this.client = client;
    }

    @Override
    public MongoDatabase getObject() {
        if (mongoDatabase == null) {
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
