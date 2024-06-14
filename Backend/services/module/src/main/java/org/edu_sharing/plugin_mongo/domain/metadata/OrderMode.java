package org.edu_sharing.plugin_mongo.domain.metadata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


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
