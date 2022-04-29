package org.edu_sharing.plugin_mongo.domain.suggestion.types;

import org.edu_sharing.plugin_mongo.domain.metadata.RangedValue;

public class RangedValueSuggestionData extends SuggestionData<RangedValue> {

    // not pretty but GraphQL doesn't resolve a generic value for input types
    // https://github.com/graphql-java-kickstart/graphql-java-tools/issues/468#issue-774610305
    // a short workaround:
    @Override
    public RangedValue getValue() {
        return super.getValue();
    }
}