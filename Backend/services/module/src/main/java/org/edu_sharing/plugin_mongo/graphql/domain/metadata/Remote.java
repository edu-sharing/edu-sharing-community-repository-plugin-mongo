package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import javax.validation.constraints.NotBlank;

public interface Remote {
    @NotBlank String getId();
    @NotBlank Repository getRepository();
}
