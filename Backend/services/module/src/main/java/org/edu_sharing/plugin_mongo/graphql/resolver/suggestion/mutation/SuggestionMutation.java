package org.edu_sharing.plugin_mongo.graphql.resolver.suggestion.mutation;

import graphql.kickstart.tools.GraphQLMutationResolver;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.plugin_mongo.domain.suggestion.Suggestion;
import org.edu_sharing.plugin_mongo.repository.SuggestionRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


@Slf4j
@Validated
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SuggestionMutation implements GraphQLMutationResolver {

    final SuggestionRepository suggestionRepository;

    public boolean addOrUpdateSuggestion(@NotNull Suggestion suggestion) {
        return suggestionRepository.addOrUpdate(suggestion.getNodeId(), suggestion.getId(), suggestion);
    }

    public boolean removeSuggestion(@NotBlank String nodeId, @NotBlank String suggestionId) {
        return suggestionRepository.remove(nodeId, suggestionId);
    }
}
