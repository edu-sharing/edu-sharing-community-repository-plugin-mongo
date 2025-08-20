package org.edu_sharing.plugin_mongo.user_activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserNodeActivityDataRepository extends MongoRepository<UserNodeActivityData, String> {
    void deleteAllByUserId(String userId);

    List<UserNodeActivityData> findAllByUserIdAndTimestampAfter(String username, Date after);

    Page<UserNodeActivityData> findAllByTimestampAfter(Date after, Pageable pageable);
}
