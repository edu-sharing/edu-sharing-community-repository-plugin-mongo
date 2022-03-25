package org.edu_sharing.plugin_mongo.graphql.domain.metadata.lom;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.util.List;

@Builder
@Value
public class Technical {
    List<Format> format;
    String size;
    List<String> location;
    List<String> installationRemarks;
    List<String> otherPlatformRequirements;
    Duration duration;
    Dimension dimension;
}
