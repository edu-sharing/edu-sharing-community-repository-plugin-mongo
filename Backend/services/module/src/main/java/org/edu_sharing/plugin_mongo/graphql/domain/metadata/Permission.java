package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
@Value
public class Permission {
    @NotBlank
    String action;
    List<String> history;
    List<String> invited;
    @NotNull
    OffsetDateTime modified;
    List<String> users;
}
