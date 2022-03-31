package org.edu_sharing.plugin_mongo.metadata.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.metadata.RangedValue;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class General {
    String title;
    List<String> description;
    List<String> language;
    List<String> keyword;
    List<String> coverage;
    RangedValue structure;
    RangedValue aggregationLevel;
}
