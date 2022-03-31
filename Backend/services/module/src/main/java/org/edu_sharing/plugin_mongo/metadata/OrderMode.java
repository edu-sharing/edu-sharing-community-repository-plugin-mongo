package org.edu_sharing.plugin_mongo.metadata;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderMode {
    @NotBlank
    String active;
    @NotNull
    OrderDirection direction;
}
