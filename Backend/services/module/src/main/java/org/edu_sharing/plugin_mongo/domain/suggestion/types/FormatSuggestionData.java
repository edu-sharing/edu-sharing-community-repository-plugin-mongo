package org.edu_sharing.plugin_mongo.domain.suggestion.types;


import lombok.*;
import org.edu_sharing.plugin_mongo.domain.metadata.lom.Format;

import java.util.List;

@Builder
@AllArgsConstructor
public class FormatSuggestionData extends SuggestionData<Format> {

    public FormatSuggestionData(Format value) {
        super(value);
    }

    // not pretty but GraphQL doesn't resolve a generic value for input types
    // https://github.com/graphql-java-kickstart/graphql-java-tools/issues/468#issue-774610305
    // a short workaround:
    @Override
    public Format getValue() {
        return super.getValue();
    }
}
