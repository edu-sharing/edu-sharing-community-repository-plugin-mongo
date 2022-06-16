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
    @Singular("language")
    List<StringSuggestionData> language;

    @Singular("keyword")
    List<StringSuggestionData> keyword;

    @Singular("coverage")
    List<StringSuggestionData> coverage;

    StringSuggestionData title;
    StringSuggestionData description;
    RangedValueSuggestionData structure;
    RangedValueSuggestionData aggregationLevel;
}
