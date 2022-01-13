package org.edu_sharing.metadata;

public class MongoDocumentFactory implements DocumentFactory {
    @Override
    public Document createDocument() {
        return new MongoDocumentAdapter();
    }

    @Override
    public Document createDocument(String key, Object value) {
        return new MongoDocumentAdapter(key, value);
    }

    @Override
    public  Document createDocument(String json) {
        return new MongoDocumentAdapter(org.bson.Document.parse(json));
    }
}
