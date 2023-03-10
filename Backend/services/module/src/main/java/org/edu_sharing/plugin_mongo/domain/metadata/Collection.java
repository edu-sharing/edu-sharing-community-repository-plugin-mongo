package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;

import java.awt.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Collection {
    int position;
    Pinned pinned;
    Color color;
    Boolean level;
    Order order;
    String scope;
    String type;
    String viewType;
    String shortTitle;
    NodeRef remote;
    String remoteSource;
    String authorFreetext;
}
