package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.util.List;

@Builder
@Value
public class LegacyAssociation {
    @NotNull
    List<String> schemaRelation;
}
