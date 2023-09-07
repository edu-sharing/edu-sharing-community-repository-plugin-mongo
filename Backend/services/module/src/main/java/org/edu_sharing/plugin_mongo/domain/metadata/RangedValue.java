package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RangedValue {
    String id;

    @NotBlank
    String value;
}

