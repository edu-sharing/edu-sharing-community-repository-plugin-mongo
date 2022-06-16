package org.edu_sharing.plugin_mongo.domain.suggestion.types;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.metadata.RangedValue;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RangedValueSuggestionData extends SuggestionData<RangedValue> {

  public RangedValueSuggestionData(RangedValue value) {
    super(value);
  }

  public RangedValueSuggestionData(String value) {
    this(null, value);
  }

  public RangedValueSuggestionData(String id, String value) {
    this(new RangedValue(id, value));
  }

  // not pretty but GraphQL doesn't resolve a generic value for input types
  // https://github.com/graphql-java-kickstart/graphql-java-tools/issues/468#issue-774610305
  // a short workaround:
  @Override
  public RangedValue getValue() {
    return super.getValue();
  }
}
