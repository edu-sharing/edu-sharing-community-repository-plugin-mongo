package org.edu_sharing.plugin_mongo.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.edu_sharing.plugin_mongo.domain.suggestion.Suggestion;
import org.edu_sharing.plugin_mongo.mongo.automation.annotation.Initialize;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.permission.annotation.NodePermission;
import org.edu_sharing.service.permission.annotation.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
@NoArgsConstructor  // Required for proxying by CGLib (CGLib is used because we don't use an interface here...)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SuggestionRepositoryImpl implements SuggestionRepository, AwareAlfrescoDeletion {

    private final String SUGGESTION_KEY = "suggestion";
    private final String NODE_ID = "nodeId";
    private final String SUGGESTION_ID = "id";

    @NonNull MongoDatabaseFactory mongoDatabaseFactory; // can't use final because of proxying by CGLib

    // will be called by IndexCreationAutomation aspect
    @Initialize
    public void createIndices() {
        MongoCollection<Document> collection = mongoDatabaseFactory.getMongoDatabase().getCollection(SUGGESTION_KEY);

        collection.createIndex(Indexes.ascending(NODE_ID));
        collection.createIndex(Indexes.compoundIndex(Indexes.ascending(NODE_ID), Indexes.ascending(SUGGESTION_KEY)), new IndexOptions().unique(true));
    }

    @Override
    @Permission(requiresUser = true)
    public List<Suggestion> getSuggestions(@NodePermission(CCConstants.PERMISSION_WRITE) String nodeId) {
        return mongoDatabaseFactory.getMongoDatabase().getCollection(SUGGESTION_KEY, Suggestion.class)
                .find(Filters.eq(NODE_ID, nodeId))
                .into(new ArrayList<>());
    }

    @Override
    @Permission(requiresUser = true)
    public Map<String, List<Suggestion>> getSuggestions(@NodePermission(CCConstants.PERMISSION_WRITE) Collection<String> nodeIds) {
        return mongoDatabaseFactory.getMongoDatabase().getCollection(SUGGESTION_KEY, Suggestion.class)
                .find(Filters.in(NODE_ID, nodeIds))
                .into(new ArrayList<>())
                .stream()
                .collect(Collectors.groupingBy(Suggestion::getNodeId));
    }

    @Override
    @Permission(requiresUser = true)
    public boolean addOrUpdate(@NodePermission(CCConstants.PERMISSION_WRITE) String nodeId, String suggestionId, Suggestion suggestion) {
        UpdateResult updateResult = mongoDatabaseFactory.getMongoDatabase().getCollection(SUGGESTION_KEY, Suggestion.class)
                .replaceOne(Filters.and(Filters.eq(NODE_ID, suggestion.getNodeId()), Filters.eq(SUGGESTION_ID, suggestion.getId())),
                        suggestion,
                        new ReplaceOptions().upsert(true));
//                .updateOne(Filters.and(Filters.eq(NODE_ID, suggestion.getNodeId()), Filters.eq(SUGGESTION_ID, suggestion.getId())),
//                        UpdateUtil.update(MongoSerializationUtil.toDocument(suggestion, codecRegistry)),
//                        new UpdateOptions().upsert(true));

        return updateResult.wasAcknowledged() && (updateResult.getModifiedCount() > 0 || updateResult.getUpsertedId() != null);
    }

    @Override
    @Permission(requiresUser = true)
    public boolean remove(@NodePermission(CCConstants.PERMISSION_WRITE) String nodeId, String suggestionId) {
        DeleteResult deleteResult = mongoDatabaseFactory.getMongoDatabase().getCollection(SUGGESTION_KEY, Suggestion.class)
                .deleteMany(Filters.and(Filters.eq(NODE_ID, nodeId), Filters.eq(SUGGESTION_ID, suggestionId)));

        return deleteResult.wasAcknowledged() && deleteResult.getDeletedCount() > 0;
    }

    @Override
    public void OnDeletedInAlfresco(Set<String> nodeIds) {
        mongoDatabaseFactory.getMongoDatabase().getCollection(SUGGESTION_KEY).deleteMany(Filters.in(NODE_ID, nodeIds));
    }
}
