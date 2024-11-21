package org.edu_sharing.plugin_mongo.suggestion;

import org.edu_sharing.service.suggestion.Suggestion;
import org.edu_sharing.service.suggestion.SuggestionStatus;

import java.util.Date;
import java.util.List;

public interface SuggestionRepository {
    List<Suggestion> saveAll(List<Suggestion> suggestions);

    void deleteByNodeIdAndCreatedBy(String nodeId, String providerId);

    List<Suggestion> updateStatus(String nodeId, List<String> ids, SuggestionStatus status, String modifiedBy, Date modified);

    List<Suggestion> findAllByNodeId(String nodeId);

    Suggestion findByNodeIdAndPropertyIdAndNotStatusAndValue(String nodeId, String propertyId, SuggestionStatus suggestionStatus, Object value);

    List<Suggestion> findAllByNodeIdAndInStatus(String nodeId, List<SuggestionStatus> status);

    void deleteByNodeIdAndCreatedByAndInVersion(String nodeId, String fullyAuthenticatedUser, List<String> versions);
}
