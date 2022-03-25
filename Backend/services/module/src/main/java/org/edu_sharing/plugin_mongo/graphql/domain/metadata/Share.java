package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import java.time.OffsetDateTime;


@Builder
@Value
public class Share {
    int downloadCount;
    OffsetDateTime date;
    String mail;
    @NotBlank
    String token;
}
