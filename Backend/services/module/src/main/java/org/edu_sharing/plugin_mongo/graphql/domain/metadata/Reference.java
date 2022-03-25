package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Reference {
    String title;
    String description;
    String version;
    String videoVtt;
    String proposalStatus;
    NodeRef collection;
}
