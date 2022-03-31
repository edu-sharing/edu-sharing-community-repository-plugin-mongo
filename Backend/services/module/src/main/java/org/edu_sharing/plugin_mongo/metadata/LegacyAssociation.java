package org.edu_sharing.plugin_mongo.metadata;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LegacyAssociation {
    @NotNull
    List<String> schemaRelation;
}
