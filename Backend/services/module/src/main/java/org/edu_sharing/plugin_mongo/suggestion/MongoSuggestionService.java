package org.edu_sharing.plugin_mongo.suggestion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.restservices.suggestions.v1.dto.CreateSuggestionRequestDTO;
import org.edu_sharing.service.permission.annotation.NodePermission;
import org.edu_sharing.service.permission.annotation.Permission;
import org.edu_sharing.service.suggestion.Suggestion;
import org.edu_sharing.service.suggestion.SuggestionService;
import org.edu_sharing.service.suggestion.SuggestionStatus;
import org.edu_sharing.service.suggestion.SuggestionType;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class MongoSuggestionService implements SuggestionService {

    private final SuggestionRepository repository;

    @Override
    @Permission(value = CCConstants.CCM_VALUE_TOOLPERMISSION_SUGGESTION_WRITE, requiresUser = true)
    public List<Suggestion> createSuggestion(@NodePermission(CCConstants.PERMISSION_READ) String nodeId, SuggestionType type, String version, List<CreateSuggestionRequestDTO> suggestionDtos) {
        List<Suggestion> suggestions = suggestionDtos.stream()
                .map(x -> new Suggestion(
                        null,
                        nodeId,
                        version,
                        x.getPropertyId(),
                        x.getValue(),
                        type,
                        repository.findByNodeIdAndPropertyIdAndNotStatusAndValue(nodeId, x.getPropertyId(), SuggestionStatus.PENDING, x.getValue()) == null
                                ?  SuggestionStatus.PENDING
                                : SuggestionStatus.DECLINED,
                        x.getDescription(),
                        x.getConfidence(),
                        new Date(),
                        AuthenticationUtil.getFullyAuthenticatedUser(),
                        null,
                        null))
                .collect(Collectors.toList());
        return repository.saveAll(suggestions);
    }

    @Override
    @Permission(value = CCConstants.CCM_VALUE_TOOLPERMISSION_SUGGESTION_WRITE, requiresUser = true)
    public void deleteSuggestions(@NodePermission(CCConstants.PERMISSION_READ) String nodeId, List<String> versions) {
        if(versions == null || versions.isEmpty()){
            repository.deleteByNodeIdAndCreatedBy(nodeId, AuthenticationUtil.getFullyAuthenticatedUser());
        }else {
            repository.deleteByNodeIdAndCreatedByAndInVersion(nodeId, AuthenticationUtil.getFullyAuthenticatedUser(), versions);
        }
    }

    @Override
    @Permission(value = CCConstants.CCM_VALUE_TOOLPERMISSION_SUGGESTION_WRITE, requiresUser = true)
    public List<Suggestion> updateStatus(@NodePermission({CCConstants.PERMISSION_WRITE}) String nodeId, List<String> ids, SuggestionStatus status) {
        return repository.updateStatus(nodeId, ids, status, AuthenticationUtil.getFullyAuthenticatedUser(), new Date());
    }

    @Override
    @Permission(value = CCConstants.CCM_VALUE_TOOLPERMISSION_SUGGESTION_READ, requiresUser = true)
    public Map<String, List<Suggestion>> getSuggestionsByNodeId(@NodePermission(CCConstants.PERMISSION_READ) String nodeId, List<SuggestionStatus> status) {
        List<Suggestion> suggestions;
        if(status == null || status.isEmpty()) {
           suggestions = repository.findAllByNodeId(nodeId);
        }else{
            suggestions = repository.findAllByNodeIdAndInStatus(nodeId, status);
        }

        return suggestions.stream().collect(Collectors.groupingBy(Suggestion::getPropertyId));
    }
}
