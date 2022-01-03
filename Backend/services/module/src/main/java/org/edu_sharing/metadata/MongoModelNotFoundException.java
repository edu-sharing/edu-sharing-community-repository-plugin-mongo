package org.edu_sharing.metadata;

public class MongoModelNotFoundException extends RuntimeException {
    public MongoModelNotFoundException(String bootstrapModel) {
        super(bootstrapModel);
    }
}
