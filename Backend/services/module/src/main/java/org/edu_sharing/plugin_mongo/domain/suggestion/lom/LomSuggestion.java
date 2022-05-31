package org.edu_sharing.plugin_mongo.domain.suggestion.lom;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LomSuggestion {

    @Singular("editorial")
    List<EditorialSuggestion> editorial;

    GeneralSuggestion general;
    TechnicalSuggestion technical;
    List<EducationalSuggestion> educational;
    RightsSuggestion rights;
    ClassificationSuggestion classification;
}
