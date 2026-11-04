package org.edu_sharing.plugin_mongo.scope;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScopeHistoryRepository extends MongoRepository<MongoScopeHistoryEntry, String> {

    List<MongoScopeHistoryEntry> findAllByScopeIdAndNodeIdOrderByAtDesc(String scopeId, String nodeId);

    long deleteAllByNodeId(String nodeId);

    long deleteAllByScopeId(String scopeId);
}
