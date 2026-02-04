package org.edu_sharing.plugin_mongo.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@RequiredArgsConstructor
@ChangeUnit(id = "unwrapRelationsMigration", order = "002", transactional = false)
public class UnwrapRelationsMigration {

    private final MongoTemplate mongoTemplate;
    private static final String SOURCE_COLLECTION = "relations";
    private static final String TARGET_COLLECTION = "relationsV2";

    @Execution
    public void execution() {
        Aggregation aggregation = newAggregation(
                unwind("relations"),
                project()
                        .and("_id").as("fromNode")
                        .and("relations.node").as("toNode")
                        .and("relations.creator").as("createdBy")
                        .and("relations.timestamp").as("createdAt")
                        .and("relations.type").as("type")
                        .and("relations.creator").as("evaluation.approvedBy")
                        .and("relations.timestamp").as("evaluation.approvedAt")
                        .andExclude("_id"),
                addFields()
                        .addFieldWithValue("isAiGenerated", false)
                        .addFieldWithValue("evaluation.isApproved", true)
                        .build(),
                merge().intoCollection(TARGET_COLLECTION).build()
        );

        mongoTemplate.aggregate(aggregation, SOURCE_COLLECTION, Object.class);
        //mongoTemplate.dropCollection(SOURCE_COLLECTION);
    }

    @RollbackExecution
    public void rollback() {
        mongoTemplate.dropCollection(TARGET_COLLECTION);
    }
}
