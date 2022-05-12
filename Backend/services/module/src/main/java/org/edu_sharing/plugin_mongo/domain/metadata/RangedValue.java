package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RangedValue {
    String id;

    @NotBlank
    String value;
}

