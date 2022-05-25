package org.edu_sharing.plugin_mongo.domain.metadata.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.metadata.RangedValue;

import java.time.Duration;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Educational {
    List<RangedValue> context;
    List<RangedValue> intendedEndUserRole;
    List<RangedValue> learningResourceType;
    List<RangedValue> curriculum;
    Duration typicalLearningTime;
    List<RangedValue> typicalAgeRange;
    List<RangedValue> interactivityType;
    IntRange typicalAgeRangeNominal;
    List<String> language;

}
