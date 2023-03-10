package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {
    List<OrderMode> reference;
    List<OrderMode> collection;
}
