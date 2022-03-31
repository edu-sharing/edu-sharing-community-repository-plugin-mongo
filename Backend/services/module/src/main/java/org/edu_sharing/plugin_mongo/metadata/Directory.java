package org.edu_sharing.plugin_mongo.metadata;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Directory {
    String type;
    Boolean hasTemplate;
}
