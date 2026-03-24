package org.edu_sharing.plugin_mongo.suggestion;

import org.edu_sharing.service.suggestion.PropertySuggestion;
import org.edu_sharing.service.suggestion.SuggestionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

public interface SuggestionRepository extends MongoRepository<MongoPropertySuggestion, String>, CustomSuggestionRepository {

    @Query("{ 'nodeId': ?0, 'propertyId': ?1, 'status': { $ne: ?2 }, 'value': ?3 }")
    PropertySuggestion findByNodeIdAndPropertyIdAndNotStatusAndValue(String nodeId, String propertyId, SuggestionStatus suggestionStatus, Object value);

    void deleteByNodeIdAndCreatedBy(String nodeId, String createdBy);

    void deleteByNodeIdAndCreatedByAndVersionIn(String nodeId, String createdBy, List<String> version);

    List<MongoPropertySuggestion> findAllByNodeId(String nodeId);

    List<PropertySuggestion> findAllByNodeIdAndStatusIn(String nodeId, Collection<SuggestionStatus> statuses);
}
