package org.edu_sharing.plugin_mongo.qa;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteInsert;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.edu_sharing.plugin_mongo.mongo.automation.annotation.Initialize;
import org.edu_sharing.plugin_mongo.repository.AwareAlfrescoDeletion;
import org.edu_sharing.service.qa.domain.QAEntry;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class QARepositoryImpl implements QARepository, AwareAlfrescoDeletion {

    public static final String COLLECTION = "question_answers";
    public static final String ID = "_id";
    public static final String CREATED_BY = "createdBy";
    public static final String NODE_ID = "nodeId";
    public static final String QUESTION = "question";
    public static final String ANSWER = "answer";
    public static final String EDUCATIONAL_LEVEL = "educationalLevel";
    @NonNull
    MongoDatabaseFactory mongoDbFactory; // can't use final because of proxying by CGLib

    @Initialize
    public void createIndices() {
        MongoCollection<Document> collection = mongoDbFactory.getMongoDatabase().getCollection(COLLECTION);
        collection.createIndex(Indexes.compoundIndex(Indexes.ascending(NODE_ID), Indexes.ascending(QUESTION), Indexes.ascending(ANSWER), Indexes.ascending(EDUCATIONAL_LEVEL)),
                new IndexOptions()
                        .collation(Collation.builder()
                                .locale("simple") // "simple" for language independent"
                                .collationStrength(CollationStrength.SECONDARY) // ignores case sensitivity
                                .build())
                        .unique(true));

        collection.createIndex(Indexes.compoundIndex(Indexes.ascending(NODE_ID), Indexes.ascending(CREATED_BY)));
    }

    @Override
    public List<QAEntry> saveAll(List<QAEntry> entries) {
        MongoCollection<QAEntry> collection = getCollection();

        List<WriteModel<QAEntry>> bulkOperations = entries.stream()
                .map(entry -> {
                    if (StringUtils.isBlank(entry.getId())) {
                        return new InsertOneModel<>(entry);
                    } else {
                        return new ReplaceOneModel<>(
                                Filters.eq(new ObjectId(entry.getId())),
                                entry);
                    }
                })
                .collect(Collectors.toList());

        BulkWriteResult bulkWriteResult = collection.bulkWrite(bulkOperations, new BulkWriteOptions().ordered(true));

        List<ObjectId> ids = Stream.concat(
                bulkWriteResult.getInserts().stream().map(BulkWriteInsert::getId).map(BsonValue::asObjectId).map(BsonObjectId::getValue),
                entries.stream().map(QAEntry::getId).filter(x -> !StringUtils.isBlank(x)).map(ObjectId::new)
        ).collect(Collectors.toList());

        return collection
                .find(Filters.in(ID, ids), QAEntry.class)
                .into(new ArrayList<>());
    }


    @Override
    public List<QAEntry> saveAny(List<QAEntry> entries) {
        MongoCollection<QAEntry> collection = getCollection();

        List<WriteModel<QAEntry>> bulkOperations = entries.stream()
                .map(entry -> {
                    if (StringUtils.isBlank(entry.getId())) {
                        return new InsertOneModel<>(entry);
                    } else {
                        return new ReplaceOneModel<>(
                                Filters.eq(new ObjectId(entry.getId())),
                                entry);
                    }
                })
                .collect(Collectors.toList());

        BulkWriteResult bulkWriteResult;
        try {
            bulkWriteResult = collection.bulkWrite(bulkOperations, new BulkWriteOptions().ordered(false));
        } catch (MongoBulkWriteException e) {
            log.warn("Error on writing items to {}: {}", collection.getNamespace().getCollectionName(), e.getMessage(), e);
            for (BulkWriteError writeError : e.getWriteErrors()) {
                log.warn("Item {} has failed with {}: {}", entries.get(writeError.getIndex()), writeError.getCategory().name(), writeError.getMessage());
            }
            bulkWriteResult = e.getWriteResult();
        }

        List<ObjectId> ids = Stream.concat(
                bulkWriteResult.getInserts().stream().map(BulkWriteInsert::getId).map(BsonValue::asObjectId).map(BsonObjectId::getValue),
                entries.stream().map(QAEntry::getId).filter(x -> !StringUtils.isBlank(x)).map(ObjectId::new)
        ).collect(Collectors.toList());

        return collection
                .find(Filters.in(ID, ids), QAEntry.class)
                .into(new ArrayList<>());
    }

    @Override
    public void deleteAllById(List<String> ids) {
        getCollection().deleteMany(Filters.in(ID, ids.stream().map(ObjectId::new).collect(Collectors.toList())));
    }

    @NotNull
    private MongoCollection<QAEntry> getCollection() {
        return mongoDbFactory.getMongoDatabase().getCollection(COLLECTION, QAEntry.class);
    }

    @Override
    public List<QAEntry> findAllByNodeId(String nodeId) {
        return getCollection()
                .find(Filters.and(Filters.eq(NODE_ID, nodeId)), QAEntry.class)
                .into(new ArrayList<>());
    }


    @Override
    public List<QAEntry> findAllByNodeIdAndCreator(String nodeId, String creator) {
        return getCollection()
                .find(Filters.and(Filters.eq(NODE_ID, nodeId), Filters.eq(CREATED_BY, creator)), QAEntry.class)
                .into(new ArrayList<>());
    }


    @Override
    public void deleteAllByNodeId(String nodeId) {
        getCollection().deleteMany(Filters.eq(NODE_ID, nodeId));
    }

    @Override
    public void deleteAllByNodeIdAndCreator(String nodeId, String creator) {
        getCollection().deleteMany(Filters.and(Filters.eq(CREATED_BY, creator), Filters.eq(NODE_ID, nodeId)));
    }


    @Override
    public void OnDeletedInAlfresco(Set<String> nodeIds) {
        getCollection().deleteMany(Filters.in(NODE_ID, nodeIds));
    }
}
