package org.edu_sharing.plugin_mongo.domain.suggestion.types;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class StringSuggestionData extends SuggestionData<String> {
    public StringSuggestionData(String value) {
        super(value);
    }
}
