package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reference {
    String title;
    String description;
    String version;
    String videoVtt;
    String proposalStatus;
    NodeRef collection;
}
