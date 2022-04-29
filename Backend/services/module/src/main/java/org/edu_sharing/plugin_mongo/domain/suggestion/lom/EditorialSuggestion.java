package org.edu_sharing.plugin_mongo.domain.suggestion.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.RangedValueSuggestionData;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EditorialSuggestion {
    RangedValueSuggestionData state;
    List<RangedValueSuggestionData> checklist;
}
