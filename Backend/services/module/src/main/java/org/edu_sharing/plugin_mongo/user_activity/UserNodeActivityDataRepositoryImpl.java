package org.edu_sharing.plugin_mongo.user_activity;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@RequiredArgsConstructor
public class UserNodeActivityDataRepositoryImpl implements UserNodeActivityDataRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public UserNodeActivityData saveWithServerTimestamp(UserNodeActivityData entity) {
        // a fresh id can never match an existing document, so this upserts an insert unless the
        // caller explicitly re-saves an entity that already has an id
        ObjectId id = entity.getId() != null ? new ObjectId(entity.getId()) : new ObjectId();
        Query query = Query.query(Criteria.where("_id").is(id));
        Update update = new Update()
                .set("nodeId", entity.getNodeId())
                .set("userId", entity.getUserId())
                .set("username", entity.getUsername())
                .set("type", entity.getType())
                .currentDate("timestamp");
        FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);
        UserNodeActivityData saved = mongoTemplate.findAndModify(query, update, options, UserNodeActivityData.class);
        entity.setId(saved.getId());
        entity.setTimestamp(saved.getTimestamp());
        return entity;
    }
}
