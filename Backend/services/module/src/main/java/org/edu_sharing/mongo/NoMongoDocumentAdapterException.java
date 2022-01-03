package org.edu_sharing.mongo;

import org.edu_sharing.metadata.Document;

public class NoMongoDocumentAdapterException extends RuntimeException {
    public NoMongoDocumentAdapterException(Class<? extends Document> clazz) {
        super(clazz.getName());
    }
}
