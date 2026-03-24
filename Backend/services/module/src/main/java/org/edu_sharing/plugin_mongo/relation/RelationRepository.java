package org.edu_sharing.plugin_mongo.relation;

import org.edu_sharing.service.relations.InputRelationType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RelationRepository extends MongoRepository<MongoNodeRelation, String> {

    Optional<MongoNodeRelation> findByFromNodeAndToNodeAndType(String fromNode, String toNode, InputRelationType type);

    List<MongoNodeRelation> findAllByFromNodeOrToNode(String fromNode, String toNode);

}