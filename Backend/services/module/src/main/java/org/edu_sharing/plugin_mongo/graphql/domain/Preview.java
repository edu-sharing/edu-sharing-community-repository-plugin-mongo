package org.edu_sharing.plugin_mongo.graphql.domain;

import lombok.*;

@Value
@Builder
public class Preview {
    String url;
    PreviewType type;

    // via elastic
    String mimetype;
    String data;
}
