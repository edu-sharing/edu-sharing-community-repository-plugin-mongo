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

    @Singular("context")
    List<RangedValueSuggestionData> context;

    @Singular("intendedEndUserRole")
    List<RangedValueSuggestionData> intendedEndUserRole;

    @Singular("learningResourceType")
    List<RangedValueSuggestionData> learningResourceType;

    @Singular("curriculum")
    List<RangedValueSuggestionData> curriculum;

    @Singular("typicalAgeRange")
    List<RangedValueSuggestionData> typicalAgeRange;

    @Singular("interactivityType")
    List<RangedValueSuggestionData> interactivityType;

    @Singular("language")
    List<StringSuggestionData> language;

    DurationSuggestionData typicalLerningTime;
    IntRangeSuggestionData typicalAgeRangeNominal;

}
