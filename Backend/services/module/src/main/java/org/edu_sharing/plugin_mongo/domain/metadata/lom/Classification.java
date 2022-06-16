package org.edu_sharing.plugin_mongo.domain.metadata.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.domain.metadata.RangedValue;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Classification {
    String learningGoal;
    List<RangedValue> taxon;
    List<RangedValue> keyword;
}
