package org.edu_sharing.plugin_mongo.suggestion;

import org.edu_sharing.service.suggestion.PropertySuggestion;
import org.edu_sharing.service.suggestion.SuggestionStatus;

import java.util.Date;
import java.util.List;

public interface CustomSuggestionRepository {
    List<PropertySuggestion> saveAny(List<MongoPropertySuggestion> suggestions);

    List<PropertySuggestion> updateStatus(String nodeId, List<String> ids, SuggestionStatus status, String fullyAuthenticatedUser, Date modifiedDate);
}
