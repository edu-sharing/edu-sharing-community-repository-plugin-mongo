package org.edu_sharing.plugin_mongo.oplog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface MongoAlfOpLogRepository extends MongoRepository<MongoAlfOpLog, String> {

    Page<MongoAlfOpLog> findAllByTimestampBefore(Date timestampBefore, Pageable pageable);

}
