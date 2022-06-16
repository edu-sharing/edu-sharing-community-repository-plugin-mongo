package org.edu_sharing.plugin_mongo.domain.metadata.lom;

import lombok.*;

import java.time.Duration;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Technical {
    List<Format> format;
    String size;
    List<String> location;
    List<String> installationRemarks;
    List<String> otherPlatformRequirements;
    Duration duration;
    Dimension dimension;
}
