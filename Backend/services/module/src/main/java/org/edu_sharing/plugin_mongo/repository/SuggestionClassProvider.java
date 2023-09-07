package org.edu_sharing.plugin_mongo.repository;

import org.edu_sharing.plugin_mongo.domain.suggestion.Suggestion;

public interface SuggestionClassProvider {
    Class<? extends Suggestion> suggestionClass();
}
