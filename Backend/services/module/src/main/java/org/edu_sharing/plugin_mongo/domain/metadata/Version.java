package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Version {
    String version;
    String comment;
    String type;
    Boolean autoCreateVersion;
}
