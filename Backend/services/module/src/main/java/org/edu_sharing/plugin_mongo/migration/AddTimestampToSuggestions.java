package org.edu_sharing.plugin_mongo.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@RequiredArgsConstructor
@ChangeUnit(id = "addTimestampToSuggestions", order = "003", transactional = false)
public class AddTimestampToSuggestions {

    private final MongoTemplate mongoTemplate;

    @Execution
    public void execute() {
        // Set timestamp to modified date where it exists
        Query modifiedExistsQuery = new Query(Criteria.where("modified").exists(true));
        Update updateWithModified = new Update();
        updateWithModified.set("timestamp", "$modified");
        mongoTemplate.updateMulti(modifiedExistsQuery, updateWithModified, "suggestions");

        // Set timestamp to created date where modified doesn't exist
        Query modifiedNotExistsQuery = new Query(Criteria.where("modified").exists(false).and("created").exists(true));
        Update updateWithCreated = new Update();
        updateWithCreated.set("timestamp", "$created");
        mongoTemplate.updateMulti(modifiedNotExistsQuery, updateWithCreated, "suggestions");
    }

    @RollbackExecution
    public void rollback() {

    }
}
