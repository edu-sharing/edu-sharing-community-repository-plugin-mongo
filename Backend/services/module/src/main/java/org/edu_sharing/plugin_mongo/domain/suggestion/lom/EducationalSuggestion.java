package org.edu_sharing.plugin_mongo.domain.suggestion.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.DurationSuggestionData;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.IntRangeSuggestionData;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.RangedValueSuggestionData;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.StringSuggestionData;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EducationalSuggestion {
    List<RangedValueSuggestionData> context;
    List<RangedValueSuggestionData> intendedEndUserRole;
    List<RangedValueSuggestionData> learningResourceType;
    List<RangedValueSuggestionData> curriculum;
    DurationSuggestionData typicalLerningTime;
    List<RangedValueSuggestionData> typicalAgeRange;
    List<RangedValueSuggestionData> interactivityType;
    IntRangeSuggestionData typicalAgeRangeNominal;
    List<StringSuggestionData> language;

}
