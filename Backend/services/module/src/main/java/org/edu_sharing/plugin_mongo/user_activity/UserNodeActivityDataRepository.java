package org.edu_sharing.plugin_mongo.user_activity;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserNodeActivityDataRepository extends MongoRepository<UserNodeActivityData, String> {
    void deleteAllByUserId(String userId);

    List<UserNodeActivityData> findAllByUserIdAndTimestampAfter(String username, Date after);

    List<UserNodeActivityData> findAllByTimestampAfter(Date after, Limit limit);

    @Query("{'timestamp': {'$gt': ?0, '$lte': ?1}}")
    List<UserNodeActivityData> findAllByTimestampBetween(Date after, Date before, Limit of);
}
