package org.edu_sharing.plugin_mongo.domain.suggestion;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SuggestionInfo {
    SuggestionStatus status;
    Date date;
    String editor;
}
