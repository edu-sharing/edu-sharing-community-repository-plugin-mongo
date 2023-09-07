package org.edu_sharing.plugin_mongo.domain.suggestion;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.metadata.RangedValue;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.RangedValueSuggestionData;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.StringSuggestionData;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OehSuggestion {
    List<RangedValueSuggestionData> unmetLegalCriteria;
    RangedValueSuggestionData sourceContentType;
    RangedValueSuggestionData toolCategory;
    RangedValueSuggestionData conditionsOfAccess;
    RangedValueSuggestionData containsAdvertisement;
    RangedValueSuggestionData price;
    List<RangedValueSuggestionData> accessibilitySummary;
    List<RangedValueSuggestionData> dataProtectionConformity;
    RangedValueSuggestionData fskRating;
    RangedValueSuggestionData licenseOer;
    StringSuggestionData generalIdentifier;
}
