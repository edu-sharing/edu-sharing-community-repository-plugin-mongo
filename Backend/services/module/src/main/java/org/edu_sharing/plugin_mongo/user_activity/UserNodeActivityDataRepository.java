package org.edu_sharing.plugin_mongo.user_activity;

import org.edu_sharing.service.tracking.user_tracking.UserNodeActivity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface UserNodeActivityDataRepository extends MongoRepository<UserNodeActivityData, String> {
    void deleteAllByUserId(String userId);

    List<UserNodeActivity> findAllByUserIdAndTimestampAfter(String username, Date after);

    Page<UserNodeActivity> findAllByTimestampAfter(Date after, Pageable pageable);
}
