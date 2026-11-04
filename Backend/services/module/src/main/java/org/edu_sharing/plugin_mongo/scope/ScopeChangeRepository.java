package org.edu_sharing.plugin_mongo.scope;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ScopeChangeRepository extends MongoRepository<MongoScopeChange, String>, CustomScopeChangeRepository {
}
