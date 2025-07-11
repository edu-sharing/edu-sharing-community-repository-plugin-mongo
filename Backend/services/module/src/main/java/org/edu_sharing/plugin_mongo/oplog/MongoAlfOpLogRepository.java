package org.edu_sharing.plugin_mongo.oplog;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoAlfOpLogRepository extends MongoRepository<MongoAlfOpLog, String> {

}
