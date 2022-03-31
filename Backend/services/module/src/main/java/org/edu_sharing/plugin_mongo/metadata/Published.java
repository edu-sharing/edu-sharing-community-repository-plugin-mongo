package org.edu_sharing.plugin_mongo.metadata;

import lombok.*;
import org.joda.time.DateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Published {
    DateTime date;
    String handleId;
    String mode;
}
