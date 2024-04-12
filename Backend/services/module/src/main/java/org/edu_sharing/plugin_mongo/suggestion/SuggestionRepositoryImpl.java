package org.edu_sharing.plugin_mongo.suggestion;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteInsert;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.edu_sharing.plugin_mongo.mongo.automation.annotation.Initialize;
import org.edu_sharing.plugin_mongo.repository.AwareAlfrescoDeletion;
import org.edu_sharing.restservices.DAODuplicateNodeException;
import org.edu_sharing.service.suggestion.Suggestion;
import org.edu_sharing.service.suggestion.SuggestionStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@NoArgsConstructor  // Required for proxying by CGLib (CGLib is used because we don't use an interface here...)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SuggestionRepositoryImpl implements SuggestionRepository, AwareAlfrescoDeletion {

    public static final String STATUS = "status";
    public static final String CREATED_BY = "createdBy";
    public static final String PROPERTY_ID = "propertyId";
    public static final String VALUE = "value";
    private final String COLLECTION = "suggestion";
    private final String NODE_ID = "nodeId";
    private final String ID = "_id";

    @NonNull
    MongoDatabaseFactory mongoDatabaseFactory; // can't use final because of proxying by CGLib


    // will be called by IndexCreationAutomation aspect
    @Initialize
    public void createIndices() {
        MongoCollection<Document> collection = mongoDatabaseFactory.getMongoDatabase().getCollection(COLLECTION);

        collection.createIndex(Indexes.compoundIndex(Indexes.ascending(NODE_ID), Indexes.ascending(CREATED_BY), Indexes.ascending(PROPERTY_ID)), new IndexOptions().unique(true));
        collection.createIndex(Indexes.compoundIndex(Indexes.ascending(NODE_ID), Indexes.ascending(STATUS), Indexes.ascending(PROPERTY_ID), Indexes.ascending(VALUE)));
        collection.createIndex(Indexes.compoundIndex(Indexes.ascending(NODE_ID), Indexes.ascending(STATUS), Indexes.ascending(ID)));


        // Redundant
        // collection.createIndex(Indexes.ascending(NODE_ID));
        // collection.createIndex(Indexes.compoundIndex(Indexes.ascending(NODE_ID), Indexes.ascending(STATUS)));
        // collection.createIndex(Indexes.compoundIndex(Indexes.ascending(NODE_ID), Indexes.ascending(CREATED_BY)));
    }


    @Override
    public void OnDeletedInAlfresco(Set<String> nodeIds) {
        getCollection().deleteMany(Filters.in(NODE_ID, nodeIds));
    }


    @Override
    public List<Suggestion> saveAll(List<Suggestion> suggestions) {
        MongoCollection<Suggestion> collection = getCollection();
        BulkWriteResult bulkWriteResult = collection
                .bulkWrite(suggestions.stream()
                        .map(suggestion -> {
                                    if (StringUtils.isBlank(suggestion.getId())) {
                                        return new InsertOneModel<>(suggestion);
                                    } else {
                                        return new ReplaceOneModel<>(
                                                Filters.eq(new ObjectId(suggestion.getId())),
                                                suggestion);
                                    }
                                }
                        ).collect(Collectors.toList()));

        List<ObjectId> ids = Stream.concat(
                bulkWriteResult.getInserts().stream().map(BulkWriteInsert::getId).map(BsonValue::asObjectId).map(BsonObjectId::getValue),
                suggestions.stream().map(Suggestion::getId).filter(x -> !StringUtils.isBlank(x)).map(ObjectId::new)
        ).collect(Collectors.toList());

        return collection
                .find(Filters.in(ID, ids), Suggestion.class)
                .into(new ArrayList<>());
    }

    @Override
    public void deleteByNodeIdAndCreatedBy(String nodeId, String createBy) {
        MongoCollection<Suggestion> collection = getCollection();
        collection.deleteMany(Filters.and(Filters.eq(NODE_ID, nodeId), Filters.eq(CREATED_BY, createBy)));
    }

    @Override
    public List<Suggestion> updateStatus(String nodeId, List<String> ids, SuggestionStatus status) {
        MongoCollection<Suggestion> collection = getCollection();
        Bson filter = Filters.and(Filters.eq(NODE_ID, nodeId), Filters.in(ID, ids.stream().map(ObjectId::new).collect(Collectors.toList())));
        collection.updateMany(filter, Updates.set(STATUS, status));
        return collection.find(filter, Suggestion.class).into(new ArrayList<>());
    }

    @Override
    public List<Suggestion> findAllByNodeId(String nodeId) {
        MongoCollection<Suggestion> collection = getCollection();
        return collection.find(Filters.eq(NODE_ID, nodeId), Suggestion.class).into(new ArrayList<>());
    }

    @Override
    public Suggestion findByNodeIdAndPropertyIdAndNotStatusAndValue(String nodeId, String propertyId, SuggestionStatus status, Object value) {
        MongoCollection<Suggestion> collection = getCollection();
        return collection.find(Filters.and(
                        Filters.eq(NODE_ID, nodeId),
                        Filters.eq(PROPERTY_ID, propertyId),
                        Filters.ne(STATUS, status),
                        Filters.eq(VALUE, value)
                ), Suggestion.class)
                .limit(1)
                .first();
    }

    @Override
    public List<Suggestion> findAllByNodeIdAndInStatus(String nodeId, List<SuggestionStatus> status) {
        MongoCollection<Suggestion> collection = getCollection();
        return collection.find(Filters.and(Filters.eq(NODE_ID, nodeId), Filters.in(STATUS, status)), Suggestion.class).into(new ArrayList<>());
    }

    @NotNull
    private MongoCollection<Suggestion> getCollection() {
        return mongoDatabaseFactory.getMongoDatabase().getCollection(COLLECTION, Suggestion.class);
    }

}
