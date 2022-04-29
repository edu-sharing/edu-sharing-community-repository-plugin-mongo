package org.edu_sharing.plugin_mongo.domain.suggestion.types;

import java.util.Locale;

public class LocaleSuggestionData extends SuggestionData<Locale> {

    // not pretty but GraphQL doesn't resolve a generic value for input types
    // https://github.com/graphql-java-kickstart/graphql-java-tools/issues/468#issue-774610305
    // a short workaround:
    @Override
    public Locale getValue() {
        return super.getValue();
    }
}
