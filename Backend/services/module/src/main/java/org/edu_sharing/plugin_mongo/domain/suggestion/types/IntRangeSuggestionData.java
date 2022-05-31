package org.edu_sharing.plugin_mongo.domain.suggestion.types;


import lombok.*;
import org.edu_sharing.plugin_mongo.domain.metadata.lom.IntRange;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntRangeSuggestionData extends SuggestionData<IntRange> {

    public IntRangeSuggestionData(IntRange value) {
        super(value);
    }

    // not pretty but GraphQL doesn't resolve a generic value for input types
    // https://github.com/graphql-java-kickstart/graphql-java-tools/issues/468#issue-774610305
    // a short workaround:
    @Override
    public IntRange getValue() {
        return super.getValue();
    }
}
