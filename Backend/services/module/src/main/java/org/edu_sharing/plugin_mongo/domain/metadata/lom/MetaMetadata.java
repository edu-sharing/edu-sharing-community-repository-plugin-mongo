package org.edu_sharing.plugin_mongo.domain.metadata.lom;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MetaMetadata {
    String schema;
    List<Contribute> contribute;
}
