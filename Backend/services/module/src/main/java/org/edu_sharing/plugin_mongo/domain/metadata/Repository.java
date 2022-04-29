package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Repository {
    @NotBlank
    String id;
    String type;
    String origin;
}
