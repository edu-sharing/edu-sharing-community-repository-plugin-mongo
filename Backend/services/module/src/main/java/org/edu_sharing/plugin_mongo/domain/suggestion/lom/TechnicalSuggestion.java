package org.edu_sharing.plugin_mongo.domain.suggestion.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TechnicalSuggestion {
    List<FormatSuggestionData> format;
    StringSuggestionData size;
    List<StringSuggestionData> location;
    List<StringSuggestionData> installationRemarks;
    List<StringSuggestionData> otherPlatformRequirements;
    DurationSuggestionData duration;
    DimensionSuggestionData dimension;
}
