package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
@Value
public class OrderMode {
    @NotBlank
    String active;
    @NotNull
    OrderDirection direction;
}
