package org.edu_sharing.plugin_mongo.domain.suggestion.types;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.edu_sharing.plugin_mongo.domain.suggestion.SuggestionInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static lombok.Builder.*;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class SuggestionData<T> {

  public SuggestionData(T value) {
    setValue(value);
  }

  @Singular("version")
  @Default
  List<String> version = Collections.singletonList("1");

  T value;
  String description;
  @Default double confidence = 0;
  @Default SuggestionInfo info = new SuggestionInfo();
}
