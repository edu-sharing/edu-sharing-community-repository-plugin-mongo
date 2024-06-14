package org.edu_sharing.plugin_mongo.domain.metadata;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LegacyAssociation {
    @NotNull
    List<String> schemaRelation;
}
