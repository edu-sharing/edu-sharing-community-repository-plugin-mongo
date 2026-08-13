package org.edu_sharing.plugin_mongo.user_activity;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserNodeActivityDataRepository extends MongoRepository<UserNodeActivityData, String>, UserNodeActivityDataRepositoryCustom {
    void deleteAllByUserId(String userId);

    List<UserNodeActivityData> findAllByUserIdAndTimestampAfter(String username, Date after);

    /**
     * A poller relies on the returned batch's last entry to know where to resume, so it must be
     * sorted oldest-first. Without an explicit Sort, results have no guaranteed order - and since
     * this collection's only index is {timestamp: -1, username: 1}, the query planner uses it in
     * its natural (descending) direction, silently returning the NEWEST matching entries instead
     * and permanently skipping the oldest ones whenever more rows match than fit in one batch.
     */
    List<UserNodeActivityData> findAllByTimestampAfter(Date after, Sort sort, Limit limit);

    @Query("{'timestamp': {'$gt': ?0, '$lte': ?1}}")
    List<UserNodeActivityData> findAllByTimestampBetween(Date after, Date before, Sort sort, Limit limit);
}
