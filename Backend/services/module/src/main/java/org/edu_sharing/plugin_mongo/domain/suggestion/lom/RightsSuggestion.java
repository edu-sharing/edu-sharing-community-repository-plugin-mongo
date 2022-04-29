package org.edu_sharing.plugin_mongo.domain.suggestion.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RightsSuggestion {
    RangedValueSuggestionData cost;
    RangedValueSuggestionData copyrightAndOtherRestrictions;
    StringSuggestionData description;
    List<StringSuggestionData> author;
    StringSuggestionData version;
    List<StringSuggestionData> internal;
    LocaleSuggestionData locale;
    DateSuggestionData expirationDate;
    BooleanSuggestionData publicAccess;
    BooleanSuggestionData negotiationPermitted;
    BooleanSuggestionData restrictedAccess;
}
