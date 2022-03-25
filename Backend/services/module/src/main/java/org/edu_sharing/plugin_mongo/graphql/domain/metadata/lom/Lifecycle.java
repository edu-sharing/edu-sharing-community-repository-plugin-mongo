package org.edu_sharing.plugin_mongo.graphql.domain.metadata.lom;

import lombok.Builder;
import lombok.Value;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.RangedValue;

import java.util.List;

@Builder
@Value
public class Lifecycle {
    String version;
    RangedValue status;
    List<Contribute> contribute;
}
