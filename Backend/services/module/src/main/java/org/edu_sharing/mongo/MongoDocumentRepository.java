package org.edu_sharing.mongo;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.edu_sharing.metadata.MongoDocumentAdapter;
import org.edu_sharing.mongo.DataValidationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MongoDocumentRepository {

    private static final String ID_KEY = "_id";


    private String MetadataModel = "lom";


    private final MongoCollection<Document> collection;
    private final DataValidationService dataValidationService;

    public MongoDocumentRepository(String collection, MongoDatabase database, DataValidationService dataValidationService) {
        this.collection = database.getCollection(collection);
        this.dataValidationService = dataValidationService;
    }

    public MongoDocumentAdapter findById(String id) {
        Document document = collection.find(Filters.eq(ID_KEY, id)).first();
        return new MongoDocumentAdapter(document);
    }

    public List<MongoDocumentAdapter> findAll() {
        ArrayList<MongoDocumentAdapter> result = new ArrayList<>();
        collection.find().map(MongoDocumentAdapter::new).into(result);
        return result;
    }

    public MongoDocumentAdapter save(MongoDocumentAdapter docAdapter) {
        dataValidationService.validate(MetadataModel, docAdapter);
        Document document = docAdapter.getRootDocument();

        Object docId = document.get(ID_KEY);
        if(docId == null){
            InsertOneResult result = collection.insertOne(document);
            docAdapter.set(ID_KEY, result.getInsertedId());
        } else {
            ReplaceOptions options = new ReplaceOptions().upsert(true);
            UpdateResult result = collection.replaceOne(Filters.eq(ID_KEY, document.get(ID_KEY)), document, options);
            docAdapter.set(ID_KEY, result.getUpsertedId());
        }

        return docAdapter;
    }
}