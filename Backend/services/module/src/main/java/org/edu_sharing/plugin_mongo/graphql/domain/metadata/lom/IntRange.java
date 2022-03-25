package org.edu_sharing.plugin_mongo.graphql.domain.metadata.lom;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class IntRange {
  int from;
  int to;
}
