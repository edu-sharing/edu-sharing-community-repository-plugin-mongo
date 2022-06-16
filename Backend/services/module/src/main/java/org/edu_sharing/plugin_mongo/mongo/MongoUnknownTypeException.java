package org.edu_sharing.plugin_mongo.mongo;

public class MongoUnknownTypeException extends RuntimeException {
    public MongoUnknownTypeException(String name) {
        super(name);
    }
}
