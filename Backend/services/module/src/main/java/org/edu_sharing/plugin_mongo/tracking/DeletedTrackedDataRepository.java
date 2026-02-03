package org.edu_sharing.plugin_mongo.tracking;

import org.springframework.data.domain.Limit;

import java.util.Date;
import java.util.List;

public interface DeletedTrackedDataRepository extends MongoTrackingRepository<DeletedTrackedData<?>, String> {
    <T> List<DeletedTrackedData<T>> findAllByTimestampAfterAndCollectionName(Date timestampAfter, String collectionName, Limit limit);
}
