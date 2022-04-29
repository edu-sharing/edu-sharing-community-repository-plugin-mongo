package org.edu_sharing.plugin_mongo.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.edu_sharing.plugin_mongo.mongo.util.MongoSerializationUtil;
import org.edu_sharing.plugin_mongo.mongo.util.UpdateUtil;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.permission.annotation.NodePermission;
import org.edu_sharing.service.permission.annotation.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
@NoArgsConstructor  // Required for proxying by CGLib (CGLib is used because we don't use an interface here...)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SuggestionRepository {

    private final String SUGGESTION_KEY = "suggestion";
    private final String NODE_ID = "nodeId";
    private final String SUGGESTION_ID = "id";

    @NonNull MongoDatabase mongoDatabase; // can't use final because of proxying by CGLib
    @NonNull CodecRegistry codecRegistry; // can't use final because of proxying by CGLib

    // will be called by IndexCreationAutomation aspect
    @Initialize
    public void createIndices() throws JsonProcessingException {
        MongoCollection<Document> collection = mongoDatabase.getCollection(SUGGESTION_KEY);

        collection.createIndex(Indexes.ascending(NODE_ID));
        collection.createIndex(Indexes.compoundIndex(Indexes.ascending(NODE_ID), Indexes.ascending(SUGGESTION_KEY)), new IndexOptions().unique(true));
    }

    @Permission(requiresUser = true)
    public List<Suggestion> getSuggestions(@NodePermission(CCConstants.PERMISSION_WRITE) String nodeId) {
        return mongoDatabase.getCollection(SUGGESTION_KEY, Suggestion.class)
                .find(Filters.eq(NODE_ID, nodeId))
                .into(new ArrayList<>());
    }

    @Permission(requiresUser = true)
    public Map<String, List<Suggestion>> getSuggestions(@NodePermission(CCConstants.PERMISSION_WRITE) Collection<String> nodeIds) {
        return mongoDatabase.getCollection(SUGGESTION_KEY, Suggestion.class)
                .find(Filters.in(NODE_ID, nodeIds))
                .into(new ArrayList<>())
                .stream()
                .collect(Collectors.groupingBy(Suggestion::getNodeId));
    }

    @Permission(requiresUser = true)
    public boolean addOrUpdate(@NodePermission(CCConstants.PERMISSION_WRITE) String nodeId, String suggestionId, Suggestion suggestion) {
        UpdateResult updateResult = mongoDatabase.getCollection(SUGGESTION_KEY, Suggestion.class)
                .updateOne(Filters.and(Filters.eq(NODE_ID, suggestion.getNodeId()), Filters.eq(SUGGESTION_ID, suggestion.getId())),
                        UpdateUtil.update(MongoSerializationUtil.toDocument(suggestion, codecRegistry)),
                        new UpdateOptions().upsert(true));

        return updateResult.wasAcknowledged() && updateResult.getModifiedCount() > 0;
    }

    @Permission(requiresUser = true)
    public boolean remove(@NodePermission(CCConstants.PERMISSION_WRITE) String nodeId, String suggestionId) {
        DeleteResult deleteResult = mongoDatabase.getCollection(SUGGESTION_KEY, Suggestion.class)
                .deleteMany(Filters.and(Filters.eq(NODE_ID, nodeId), Filters.eq(SUGGESTION_ID, suggestionId)));

        return deleteResult.wasAcknowledged() && deleteResult.getDeletedCount() > 0;
    }
}
