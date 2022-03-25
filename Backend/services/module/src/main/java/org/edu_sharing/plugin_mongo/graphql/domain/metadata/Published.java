package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;
import org.joda.time.DateTime;

@Builder
@Value
public class Published {
    DateTime date;
    String handleId;
    String mode;
}
