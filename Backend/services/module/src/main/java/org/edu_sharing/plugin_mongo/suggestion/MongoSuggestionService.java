package org.edu_sharing.plugin_mongo.suggestion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.restservices.suggestions.v1.dto.CreateSuggestionRequestDTO;
import org.edu_sharing.service.permission.annotation.NodePermission;
import org.edu_sharing.service.permission.annotation.Permission;
import org.edu_sharing.service.suggestion.Suggestion;
import org.edu_sharing.service.suggestion.SuggestionService;
import org.edu_sharing.service.suggestion.SuggestionStatus;
import org.edu_sharing.service.suggestion.SuggestionType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class MongoSuggestionService implements SuggestionService {

    private final SuggestionRepository repository;
    private final DictionaryComponent dictionaryComponent;

    Map<QName, Class<?>> typeMapping = new HashMap<>() {{
        put(DataTypeDefinition.TEXT, String.class);
        put(DataTypeDefinition.MLTEXT, String.class);
        put(DataTypeDefinition.NODE_REF, String.class);
        put(DataTypeDefinition.DATE, Long.class);
        put(DataTypeDefinition.DATETIME, Long.class);
        put(DataTypeDefinition.INT, Long.class);
        put(DataTypeDefinition.LONG, Long.class);
        put(DataTypeDefinition.FLOAT, Double.class);
        put(DataTypeDefinition.DOUBLE, Double.class);
        put(DataTypeDefinition.BOOLEAN, Boolean.class);
    }};

    Map<Class<?>, Function<String, Object>> typeConverterMapping = new HashMap<>() {{
        put(Long.class, Long::parseLong);
        put(Double.class, Double::parseDouble);
        put(Boolean.class, Boolean::parseBoolean);
    }};

    @Override
    @Permission(value = CCConstants.CCM_VALUE_TOOLPERMISSION_SUGGESTION_WRITE, requiresUser = true)
    public List<Suggestion> createSuggestion(@NodePermission(CCConstants.PERMISSION_READ) String nodeId, SuggestionType type, String version, List<CreateSuggestionRequestDTO> suggestionDtos) {

        suggestionDtos.forEach(x -> {

            if (x.getValue() == null) {
                throw new IllegalArgumentException(x.getPropertyId() + " can't be null");
            }

            PropertyDefinition property = dictionaryComponent.getProperty(QName.createQName(CCConstants.getValidGlobalName(x.getPropertyId())));
            if (property == null) {
                throw new IllegalArgumentException(x.getPropertyId() + " unknown property");
            }

            Class<?> valueClass = x.getValue().getClass();
            Class<?> targetType = typeMapping.get(property.getDataType().getName());
            if (targetType == null) {
                throw new IllegalArgumentException(x.getPropertyId() + " unsupported type " + property.getDataType().getName());
            }

            if (Collection.class.isAssignableFrom(valueClass) || valueClass.isArray()) {
                throw new IllegalArgumentException(x.getPropertyId() + " no support on List objects");
            }

            if (!targetType.isAssignableFrom(valueClass)) {
                if (valueClass == String.class) {
                    Function<String, Object> stringObjectFunction = typeConverterMapping.get(targetType);
                    if (stringObjectFunction != null) {
                        try {
                            x.setValue(stringObjectFunction.apply((String) x.getValue()));
                            return;
                        } catch (Exception e) {
                            log.error("Can't convert {} of type {} to {} because: {}", x.getValue(), valueClass, targetType, e.getMessage(), e);
                        }
                    }
                }

                if(Number.class.isAssignableFrom(valueClass) && Number.class.isAssignableFrom(targetType)){
                    return;
                }

                throw new IllegalArgumentException(x.getPropertyId() + " is not assignable to " + targetType.getName());
            }
        });


        List<Suggestion> suggestions = suggestionDtos.stream()
                .map(x -> new Suggestion(
                        null,
                        nodeId,
                        version,
                        x.getPropertyId(),
                        x.getValue(),
                        type,
                        repository.findByNodeIdAndPropertyIdAndNotStatusAndValue(nodeId, x.getPropertyId(), SuggestionStatus.PENDING, x.getValue()) == null
                                ? SuggestionStatus.PENDING
                                : SuggestionStatus.DECLINED,
                        x.getDescription(),
                        x.getConfidence(),
                        new Date(),
                        AuthenticationUtil.getFullyAuthenticatedUser(),
                        null,
                        null))
                .collect(Collectors.toList());
        return repository.saveAny(suggestions);
    }

    @Override
    @Permission(value = CCConstants.CCM_VALUE_TOOLPERMISSION_SUGGESTION_WRITE, requiresUser = true)
    public void deleteSuggestions(@NodePermission(CCConstants.PERMISSION_READ) String nodeId, List<String> versions) {
        if (versions == null || versions.isEmpty()) {
            repository.deleteByNodeIdAndCreatedBy(nodeId, AuthenticationUtil.getFullyAuthenticatedUser());
        } else {
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
        if (status == null || status.isEmpty()) {
            suggestions = repository.findAllByNodeId(nodeId);
        } else {
            suggestions = repository.findAllByNodeIdAndInStatus(nodeId, status);
        }

        return suggestions.stream().collect(Collectors.groupingBy(Suggestion::getPropertyId));
    }
}
