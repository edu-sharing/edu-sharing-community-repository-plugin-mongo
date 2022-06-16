package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImportedObject {
    String nodeId;
    String appId;
    String appName;
}
