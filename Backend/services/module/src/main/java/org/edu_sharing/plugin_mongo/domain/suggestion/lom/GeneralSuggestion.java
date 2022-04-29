package org.edu_sharing.plugin_mongo.domain.suggestion.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.RangedValueSuggestionData;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.StringSuggestionData;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneralSuggestion {
    StringSuggestionData title;
    StringSuggestionData description;
    List<StringSuggestionData> language;
    List<StringSuggestionData> keyword;
    List<StringSuggestionData> coverage;
    RangedValueSuggestionData structure;
    RangedValueSuggestionData aggregationLevel;
}
