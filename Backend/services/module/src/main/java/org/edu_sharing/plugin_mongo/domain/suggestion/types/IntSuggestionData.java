package org.edu_sharing.plugin_mongo.domain.suggestion.types;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.edu_sharing.plugin_mongo.graphql.annotation.GraphQLSchema;

@Builder
@GraphQLSchema // We need this until are actually using it
@AllArgsConstructor
public class IntSuggestionData extends SuggestionData<Integer> {
    public IntSuggestionData(Integer value) {
        super(value);
    }
}
