package org.edu_sharing.plugin_mongo.repository;

import org.edu_sharing.plugin_mongo.domain.suggestion.Suggestion;

public class DefaultSuggestionClassProvider implements SuggestionClassProvider {

    @Override
    public Class<? extends Suggestion> suggestionClass(){
        return Suggestion.class;
    }
}
