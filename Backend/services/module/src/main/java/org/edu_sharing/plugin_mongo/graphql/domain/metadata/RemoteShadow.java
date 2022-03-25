package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;

@Builder
@Value
public class RemoteShadow implements Remote {
    @NotBlank
    String id;

    @NotBlank
    Repository repository;
}
