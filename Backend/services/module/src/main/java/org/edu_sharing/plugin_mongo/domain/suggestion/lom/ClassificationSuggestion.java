package org.edu_sharing.plugin_mongo.domain.suggestion.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.RangedValueSuggestionData;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.StringSuggestionData;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassificationSuggestion {
    StringSuggestionData purpose;
    List<RangedValueSuggestionData> taxon;
    StringSuggestionData description;
}
