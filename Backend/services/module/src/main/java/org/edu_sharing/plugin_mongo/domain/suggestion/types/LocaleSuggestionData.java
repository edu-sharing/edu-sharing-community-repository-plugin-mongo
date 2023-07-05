package org.edu_sharing.plugin_mongo.domain.suggestion.types;

import lombok.*;

import java.util.Locale;

@Builder
@AllArgsConstructor
public class LocaleSuggestionData extends SuggestionData<Locale> {

    public LocaleSuggestionData(Locale value) {
        super(value);
    }

    // not pretty but GraphQL doesn't resolve a generic value for input types
    // https://github.com/graphql-java-kickstart/graphql-java-tools/issues/468#issue-774610305
    // a short workaround:
    @Override
    public Locale getValue() {
        return super.getValue();
    }
}
