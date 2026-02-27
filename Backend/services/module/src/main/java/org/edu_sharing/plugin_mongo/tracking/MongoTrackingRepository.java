package org.edu_sharing.plugin_mongo.tracking;

import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Date;
import java.util.List;

@NoRepositoryBean
public interface MongoTrackingRepository<T extends TrackedData, ID> extends MongoRepository<T, ID> {
    List<T> findAllByTimestampAfter(Date timestamp, Limit limit);

    @Query("{'timestamp' : { $gt: ?0, $lte: ?1 } }")
    List<T> findAllByTimestampBetween(Date from, Date to, Limit limit);
}
