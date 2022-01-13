package org.edu_sharing.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.edu_sharing.alfresco.lightbend.LightbendConfigLoader;

public class MongoConnectionServiceImpl {


    private MongoClient mongoClient;
    private MongoDatabase database;

    private MongoConfig mongoConfig;

    private Log logger = LogFactory.getLog(MongoConnectionServiceImpl.class);


    public MongoConnectionServiceImpl() {
        try {
            Config rootConfig = LightbendConfigLoader.get();
            Config mongoConfiguaration = rootConfig.getConfig("mongo");
            mongoConfig = ConfigBeanFactory.create(mongoConfiguaration, MongoConfig.class);
        } catch (Exception e) {
            logger.info("No mongo to use found or invalid mongo config: " + e.getMessage());
        }
    }

    public void connect() {
        if (mongoClient != null) {
            logger.info("Connection already established");
            return;
        }

        mongoClient = MongoClients.create(mongoConfig.getConnectionString());
        database = mongoClient.getDatabase(mongoConfig.getDatabase());
    }

    public void disconnect() {
        if (mongoClient == null) {
            logger.info("No established connection found");
            return;
        }
        mongoClient.close();
        mongoClient = null;
    }

//    public Document FindDocument(Document query) {
//        if (!(query instanceof MongoDocumentAdapter)) {
//            throw new NoMongoDocumentAdapterException(query.getClass());
//        }
//
//        MongoDocumentAdapter mongoDocument = (MongoDocumentAdapter) query;
//        database.getCollection("").find(mongoDocument.getRootDocument());
//    }
}