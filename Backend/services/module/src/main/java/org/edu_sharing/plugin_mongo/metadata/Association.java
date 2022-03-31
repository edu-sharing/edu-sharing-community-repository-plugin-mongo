package org.edu_sharing.plugin_mongo.metadata;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Association {
    NodeRef symlinkNodeRef;
    NodeRef forkedOriginNodeRef;
    NodeRef originalNodeRef;
    NodeRef publishedOriginalNodeRef;
    LegacyAssociation legacy;
}
