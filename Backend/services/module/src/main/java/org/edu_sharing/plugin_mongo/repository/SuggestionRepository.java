package org.edu_sharing.plugin_mongo.repository;

import org.edu_sharing.plugin_mongo.domain.suggestion.Suggestion;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.permission.annotation.NodePermission;
import org.edu_sharing.service.permission.annotation.Permission;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SuggestionRepository {
    @Permission(requiresUser = true)
    List<Suggestion> getSuggestions(@NodePermission(CCConstants.PERMISSION_WRITE) String nodeId);

    @Permission(requiresUser = true)
    Map<String, List<Suggestion>> getSuggestions(@NodePermission(CCConstants.PERMISSION_WRITE) Collection<String> nodeIds);

    @Permission(requiresUser = true)
    boolean addOrUpdate(@NodePermission(CCConstants.PERMISSION_WRITE) String nodeId, String suggestionId, Suggestion suggestion);

    @Permission(requiresUser = true)
    boolean remove(@NodePermission(CCConstants.PERMISSION_WRITE) String nodeId, String suggestionId);
}
