package org.edu_sharing.plugin_mongo.qa;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.InsertOneResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.edu_sharing.plugin_mongo.mongo.automation.annotation.Initialize;
import org.edu_sharing.plugin_mongo.repository.AwareAlfrescoDeletion;
import org.edu_sharing.service.qa.domain.QAEntry;
import org.edu_sharing.service.qa.domain.QANode;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class QANodeRepositoryImpl implements QANodeRepository, AwareAlfrescoDeletion {

    public static final String COLLECTION = "question_answers";
    public static final String SOURCE_ID = "sourceId";
    public static final String NODE_ID = "nodeId";
    @NonNull
    MongoDatabaseFactory mongoDbFactory; // can't use final because of proxying by CGLib

    @Initialize
    public void createIndices() {
        MongoCollection<Document> collection = mongoDbFactory.getMongoDatabase().getCollection(COLLECTION);
        collection.createIndex(Indexes.compoundIndex(Indexes.ascending(NODE_ID), Indexes.ascending(SOURCE_ID)), new IndexOptions().unique(true));
    }

    @NotNull
    private MongoCollection<QANode> getCollection() {
        return mongoDbFactory.getMongoDatabase().getCollection(COLLECTION, QANode.class);
    }

    @Override
    public Optional<QANode> findQANodeBySourceIdAndNodeId(String sourceId, String nodeId) {
        return Optional.ofNullable(getCollection()
                .find(Filters.and(Filters.eq(SOURCE_ID, sourceId), Filters.eq(NODE_ID, nodeId)))
                .first());
    }

    @Override
    public List<QANode> findAllQANodeByNodeIdIs(String nodeId) {
        return getCollection()
                .find(Filters.eq(NODE_ID, nodeId))
                .into(new ArrayList<>());
    }

    @Override
    public Optional<QAEntriesOnly> findBySourceIdAndNodeId(String sourceId, String nodeId) {
        return Optional.ofNullable(getCollection()
                .find(Filters.and(Filters.eq(SOURCE_ID, sourceId), Filters.eq(NODE_ID, nodeId)))
                        .map(x-> (QAEntriesOnly) x::getEntries)
                .first());
    }

    @Override
    public List<QAEntriesOnly> findAllByNodeIdIs(String nodeId) {
        return getCollection()
                .find(Filters.eq(NODE_ID, nodeId))
                .map(x-> (QAEntriesOnly) x::getEntries)
                .into(new ArrayList<>());
    }

    @Override
    public void deleteAllByNodeId(String nodeId) {
        getCollection().deleteMany(Filters.eq(NODE_ID, nodeId));
    }

    @Override
    public void deleteBySourceIdAndNodeId(String sourceId, String nodeId) {
        getCollection().deleteMany(Filters.and(Filters.eq(SOURCE_ID, sourceId), Filters.eq(NODE_ID, nodeId)));
    }


    @Override
    public void OnDeletedInAlfresco(Set<String> nodeIds) {
        getCollection().deleteMany(Filters.in(NODE_ID, nodeIds));
    }

    @Override
    public void save(QANode qaNode) {
        if(qaNode.getId() == null) {
            InsertOneResult insertOneResult = getCollection().insertOne(qaNode);
            BsonValue insertedId = insertOneResult.getInsertedId();
            if(insertedId != null) {
                qaNode.setId(insertedId.isObjectId() ? insertedId.asObjectId().getValue().toString(): insertedId.asString().getValue());
            }
        }else {
            getCollection().findOneAndReplace(Filters.eq(new ObjectId(qaNode.getId())), qaNode);
        }
    }
}
