package org.edu_sharing.plugin_mongo.domain.suggestion;

import lombok.*;
import org.apache.commons.lang.StringUtils;
import org.edu_sharing.plugin_mongo.domain.suggestion.lom.LomSuggestion;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor(force = true)
public class Suggestion {
  @NonNull String nodeId;
  @NonNull String id;
  @NonNull @Builder.Default Date date = new Date();

  @NonNull SuggestionType type;
  LomSuggestion lom;
  OehSuggestion oeh;

  public Suggestion(
      @NonNull String nodeId,
      @NonNull String id,
      @NonNull Date date,
      @NonNull SuggestionType type,
      LomSuggestion lom,
      OehSuggestion oeh) {

    if (StringUtils.isBlank(nodeId)) {
      throw new IllegalArgumentException("nodeId");
    }

    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id");
    }

    this.nodeId = nodeId;
    this.id = id;
    this.date = date;
    this.type = type;
    this.lom = lom;
    this.oeh = oeh;
  }
}
