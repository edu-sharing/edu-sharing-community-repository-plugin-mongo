package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;
import org.joda.time.DateTime;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Published {
    Date date;
    String handleId;
    String mode;
}
