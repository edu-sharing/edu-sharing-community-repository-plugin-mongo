package org.edu_sharing.plugin_mongo.domain.suggestion.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TechnicalSuggestion {
    @Singular("format")
    List<FormatSuggestionData> format;

    @Singular("location")
    List<StringSuggestionData> location;

    @Singular("installationRemarks")
    List<StringSuggestionData> installationRemarks;

    @Singular("otherPlatformRequirements")
    List<StringSuggestionData> otherPlatformRequirements;

    StringSuggestionData size;
    DurationSuggestionData duration;
    DimensionSuggestionData dimension;
}
