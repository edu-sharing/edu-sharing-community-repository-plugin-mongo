package org.edu_sharing.plugin_mongo.graphql.dataloader;

import lombok.RequiredArgsConstructor;
import org.dataloader.MappedBatchLoader;
import org.edu_sharing.plugin_mongo.domain.suggestion.Suggestion;
import org.edu_sharing.plugin_mongo.repository.SuggestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SuggestionBatchedLoader implements MappedBatchLoader<String, List<Suggestion>> {
    final SuggestionRepository suggestionRepository;

    @Override
    public CompletionStage<Map<String, List<Suggestion>>> load(Set<String> nodeIds) {
        return CompletableFuture.completedFuture(suggestionRepository.getSuggestions(nodeIds));
    }
}
