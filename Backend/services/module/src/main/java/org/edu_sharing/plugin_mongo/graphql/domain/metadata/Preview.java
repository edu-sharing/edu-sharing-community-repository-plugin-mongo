package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Preview {
    String url;
    String mimetype;
    String data;
    PreviewType type;
}
