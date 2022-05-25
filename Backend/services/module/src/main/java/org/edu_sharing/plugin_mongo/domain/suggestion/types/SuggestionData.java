package org.edu_sharing.plugin_mongo.domain.suggestion.types;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.edu_sharing.plugin_mongo.domain.suggestion.SuggestionInfo;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SuggestionData<T> {
    T value;
    String description;
    Double confidence;
    List<String> version;
    SuggestionInfo info;
}
