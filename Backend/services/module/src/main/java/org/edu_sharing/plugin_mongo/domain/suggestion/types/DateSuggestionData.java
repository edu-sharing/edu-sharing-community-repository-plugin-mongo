package org.edu_sharing.plugin_mongo.domain.suggestion.types;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DateSuggestionData extends SuggestionData<Date> {

    public DateSuggestionData(Date value) {
        super(value);
    }

    // not pretty but GraphQL doesn't resolve a generic value for input types
    // https://github.com/graphql-java-kickstart/graphql-java-tools/issues/468#issue-774610305
    // a short workaround:
    @Override
    public Date getValue() {
        return super.getValue();
    }
}
