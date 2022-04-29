package org.edu_sharing.plugin_mongo.domain.suggestion;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.suggestion.lom.LomSuggestion;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Suggestion {
    String nodeId;
    String id;
    Date date;
    SuggestionType type;
    LomSuggestion lom;
}
