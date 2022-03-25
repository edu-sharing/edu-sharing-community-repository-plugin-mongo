package org.edu_sharing.plugin_mongo.graphql.domain.metadata.lom;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class MetaMetadata {
    String schema;
    List<Contribute> contribute;
}
