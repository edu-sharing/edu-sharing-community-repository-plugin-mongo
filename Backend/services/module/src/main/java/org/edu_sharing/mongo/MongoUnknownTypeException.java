package org.edu_sharing.mongo;

public class MongoUnknownTypeException extends RuntimeException {
    public MongoUnknownTypeException(String name) {
        super(name);
    }
}
