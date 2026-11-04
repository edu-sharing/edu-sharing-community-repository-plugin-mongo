package org.edu_sharing.plugin_mongo.scope;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ScopeAssignmentRepository extends MongoRepository<MongoScopeAssignment, String> {

    Optional<MongoScopeAssignment> findByScopeIdAndNodeId(String scopeId, String nodeId);

    List<MongoScopeAssignment> findAllByNodeId(String nodeId);

    List<MongoScopeAssignment> findAllByNodeIdIn(Collection<String> nodeIds);

    List<MongoScopeAssignment> findAllByScopeIdAndNodeIdIn(String scopeId, Collection<String> nodeIds);

    boolean existsByScopeIdAndNodeIdAndPublishedTrue(String scopeId, String nodeId);

    Stream<MongoScopeAssignment> streamAllByScopeId(String scopeId);

    long deleteAllByNodeId(String nodeId);

    long deleteAllByScopeId(String scopeId);
}
