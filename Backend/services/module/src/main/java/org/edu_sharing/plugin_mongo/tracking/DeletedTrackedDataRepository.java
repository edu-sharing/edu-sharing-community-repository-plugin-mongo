package org.edu_sharing.plugin_mongo.tracking;

import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface DeletedTrackedDataRepository extends MongoTrackingRepository<DeletedTrackedData<?>, String> {
    <T> List<DeletedTrackedData<T>> findAllByTimestampAfterAndCollectionName(Date timestampAfter, String collectionName, Limit limit);

    @Query("{'timestamp' : { $gt: ?0, $lte: ?1 }, 'collectionName': ?2 }")
    <T> List<DeletedTrackedData<T>> findAllByTimestampBetweenAndCollectionName(Date from, Date to, String collectionName, Limit limit);
}
