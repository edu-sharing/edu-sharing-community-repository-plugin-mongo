package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class Order {
    List<OrderMode> reference;
    List<OrderMode> collection;
}
