package org.edu_sharing.plugin_mongo.domain.metadata.lom;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntRange {
  int from;
  int to;
}
