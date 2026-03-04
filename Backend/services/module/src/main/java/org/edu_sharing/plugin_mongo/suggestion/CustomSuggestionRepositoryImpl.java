package org.edu_sharing.plugin_mongo.suggestion;

import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteInsert;
import com.mongodb.bulk.BulkWriteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.types.ObjectId;
import org.edu_sharing.service.suggestion.PropertySuggestion;
import org.edu_sharing.service.suggestion.SuggestionStatus;
import org.springframework.data.mongodb.BulkOperationException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuggestionRepositoryImpl implements CustomSuggestionRepository {

    public static final String ID = "_id";
    public static final String NODE_ID = "nodeId";
    public static final String STATUS = "status";
    public static final String MODIFIED_BY = "modifiedBy";
    public static final String MODIFIED = "modified";
    private final MongoOperations mongoOperations;


    public List<PropertySuggestion> saveAny(List<MongoPropertySuggestion> suggestions) {
        if (suggestions.isEmpty()) {
            return List.of();
        }

        BulkOperations bulkOps = mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED, MongoPropertySuggestion.class);
        for (MongoPropertySuggestion suggestion : suggestions) {
            if (StringUtils.isBlank(suggestion.getId())) {
                bulkOps.insert(suggestion);
            } else {
                Query query = new Query(Criteria.where(ID).is(new ObjectId(suggestion.getId())));
                bulkOps.replaceOne(query, suggestion);
            }
        }

        BulkWriteResult bulkWriteResult;
        try {
            bulkWriteResult = bulkOps.execute();
        } catch (BulkOperationException e) {
            log.warn("Error on writing items to suggestion collection: {}", e.getMessage(), e);
            for (BulkWriteError writeError : e.getErrors()) {
                log.warn("Item {} has failed with {}: {}", suggestions.get(writeError.getIndex()), writeError.getCategory().name(), writeError.getMessage());
            }
            bulkWriteResult = e.getResult();
        }


        List<ObjectId> ids = Stream.concat(
                bulkWriteResult.getInserts().stream().map(BulkWriteInsert::getId).map(BsonValue::asObjectId).map(BsonObjectId::getValue),
                suggestions.stream().map(PropertySuggestion::getId).filter(x -> !StringUtils.isBlank(x)).map(ObjectId::new)
        ).collect(Collectors.toList());

        return mongoOperations.find(new Query(Criteria.where(ID).in(ids)), PropertySuggestion.class, mongoOperations.getCollectionName(MongoPropertySuggestion.class));
    }

    @Override
    public List<PropertySuggestion> updateStatus(String nodeId, List<String> ids, SuggestionStatus status, String fullyAuthenticatedUser, Date modifiedDate) {
        Query query = new Query(Criteria.where(NODE_ID).is(nodeId).and(ID).in(ids.stream().map(ObjectId::new).collect(Collectors.toList())));
        Update update = new Update()
                .set(STATUS, status)
                .set(MODIFIED_BY, fullyAuthenticatedUser)
                .set(MODIFIED, modifiedDate);
        mongoOperations.updateMulti(query, update, MongoPropertySuggestion.class);
        return mongoOperations.find(query,  PropertySuggestion.class, mongoOperations.getCollectionName(MongoPropertySuggestion.class));
    }
}
