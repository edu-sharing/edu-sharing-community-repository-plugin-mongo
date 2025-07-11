package org.edu_sharing.plugin_mongo.user_activity;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface UserNodeActivityDataRepository extends MongoRepository<UserNodeActivityData, String> {
    void deleteAllByUserId(String userId);

    void deleteAllByUserIdIn(Collection<String> userIds);
}
