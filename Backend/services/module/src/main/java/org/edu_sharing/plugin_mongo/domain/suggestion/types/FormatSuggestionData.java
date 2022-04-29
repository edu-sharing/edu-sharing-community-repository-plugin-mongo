package org.edu_sharing.plugin_mongo.domain.suggestion.types;


import org.edu_sharing.plugin_mongo.domain.metadata.lom.Format;

import java.util.List;

public class FormatSuggestionData extends SuggestionData<Format> {

    // not pretty but GraphQL doesn't resolve a generic value for input types
    // https://github.com/graphql-java-kickstart/graphql-java-tools/issues/468#issue-774610305
    // a short workaround:
    @Override
    public Format getValue() {
        return super.getValue();
    }
}
