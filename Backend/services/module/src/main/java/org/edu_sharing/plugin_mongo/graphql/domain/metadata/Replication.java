package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

@Builder
@Value
public class Replication implements Remote {
    @NotBlank
    String id;

    @NotBlank
    String uuid;
    String hash;
    OffsetDateTime modified;
    OffsetDateTime timestamp;
    boolean importBlocked;

    @NotBlank
    Repository repository;
}
