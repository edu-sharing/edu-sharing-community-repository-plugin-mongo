package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Store {
    int dbId;
    @NotBlank
    String id;
    String protocol;
}
