package org.edu_sharing.plugin_mongo.domain.suggestion;

import lombok.*;
import lombok.Builder.Default;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SuggestionInfo {
  @Default SuggestionStatus status = SuggestionStatus.PENDING;
  @Default Date date = new Date();
  String editor;
}
