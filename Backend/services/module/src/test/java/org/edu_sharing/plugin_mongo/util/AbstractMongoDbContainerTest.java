package org.edu_sharing.plugin_mongo.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public abstract class AbstractMongoDbContainerTest {
    private static MongoDBContainer mongoDbContainer;
    private static final Logger logger = LoggerFactory.getLogger(AbstractMongoDbContainerTest.class);

    protected static MongoClient client;
    protected static MongoDatabaseFactory dbFactory;
    protected static MongoDatabase db;

    protected static String getReplicaSetUrl() {
        //return "mongodb://root:example@localhost:27017";
        return mongoDbContainer.getReplicaSetUrl();
    }

    @BeforeAll
    public static void startContainerAndPublicPortIsAvailable() {
        mongoDbContainer = new MongoDBContainer(DockerImageName.parse("mongo:5.0.3"));
        mongoDbContainer.start();
        initMongoDb();
        logger.info("mongoDB started.");
    }

    @AfterAll
    public static void stopContainerAndPublicPortIsAvailable() {
        client.close();
        mongoDbContainer.stop();
        logger.info("mongoDB stopped.");
    }

    private static void initMongoDb() {
        client = MongoClients.create(getReplicaSetUrl());
        dbFactory = new SimpleMongoClientDatabaseFactory(client, "edu-sharing");
        db=dbFactory.getMongoDatabase();
    }

    @AfterEach
    private void cleanUp() {
        db.drop();
    }
}
