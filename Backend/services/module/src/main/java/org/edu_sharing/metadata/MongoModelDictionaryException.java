package org.edu_sharing.metadata;

public class MongoModelDictionaryException extends RuntimeException {
    public MongoModelDictionaryException(MongoModelParseException ex, String bootstrapModel) {
        super(bootstrapModel, ex);
    }
}
