package org.edu_sharing.plugin_mongo.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import org.edu_sharing.plugin_mongo.tracking.DeletedTrackedData;
import org.springframework.data.mongodb.core.MongoTemplate;

@RequiredArgsConstructor
@ChangeUnit(id = "createDeletedTrackedDataCollection", order = "001",runAlways = true, transactional = false)
public class CreateDeletedTrackedDataCollection {

    private final MongoTemplate mongoTemplate;

    @Execution
    public void execution() {
        if(!mongoTemplate.collectionExists(DeletedTrackedData.class)) {
            mongoTemplate.createCollection(DeletedTrackedData.class);
        }
    }

    @RollbackExecution
    public void rollback() {

    }
}
