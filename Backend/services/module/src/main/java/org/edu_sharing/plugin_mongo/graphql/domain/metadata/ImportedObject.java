package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ImportedObject {
    String nodeId;
    String appId;
    String appName;
}
