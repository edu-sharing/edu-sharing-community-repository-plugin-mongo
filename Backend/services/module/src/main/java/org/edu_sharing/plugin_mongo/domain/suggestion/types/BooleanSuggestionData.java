package org.edu_sharing.plugin_mongo.domain.suggestion.types;

import lombok.*;

@Builder
@AllArgsConstructor
public class BooleanSuggestionData extends SuggestionData<Boolean> {
    public BooleanSuggestionData(Boolean value) {
        super(value);
    }
}
