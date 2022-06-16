package org.edu_sharing.plugin_mongo.domain.suggestion.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RightsSuggestion {
    @Singular("author")
    List<StringSuggestionData> author;

    @Singular("internal")
    List<StringSuggestionData> internal;

    RangedValueSuggestionData cost;
    RangedValueSuggestionData copyrightAndOtherRestrictions;
    StringSuggestionData description;
    StringSuggestionData version;
    LocaleSuggestionData locale;
    DateSuggestionData expirationDate;
    BooleanSuggestionData publicAccess;
    BooleanSuggestionData negotiationPermitted;
    BooleanSuggestionData restrictedAccess;
}
