package org.edu_sharing.plugin_mongo.metadata.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.metadata.RangedValue;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Classification {
    String purpose;
    List<RangedValue> taxon;
    String description;
}
