package org.edu_sharing.plugin_mongo.metadata.lom;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Dimension {
    double height;
    double width;
}
