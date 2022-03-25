package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

import java.awt.*;

@Builder
@Value
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
    NodeRef remoteNodeRef;
    String remoteSource;
    String authorFreetext;
}
