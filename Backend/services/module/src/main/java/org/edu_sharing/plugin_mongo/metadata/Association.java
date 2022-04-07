package org.edu_sharing.plugin_mongo.metadata;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Association {
    NodeRef symlink;
    NodeRef forkedOrigin;
    NodeRef original;
    NodeRef publishedOriginal;
    LegacyAssociation legacy;
}
