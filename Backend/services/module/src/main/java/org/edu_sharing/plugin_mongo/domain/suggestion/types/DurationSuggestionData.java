package org.edu_sharing.plugin_mongo.domain.suggestion.types;

import lombok.*;

import java.time.Duration;

@Builder
@AllArgsConstructor
public class DurationSuggestionData extends SuggestionData<Duration> {

  public DurationSuggestionData(Duration value) {
    super(value);
  }

  // not pretty but GraphQL doesn't resolve a generic value for input types
  // https://github.com/graphql-java-kickstart/graphql-java-tools/issues/468#issue-774610305
  // a short workaround:
  @Override
  public Duration getValue() {
    return super.getValue();
  }
}
