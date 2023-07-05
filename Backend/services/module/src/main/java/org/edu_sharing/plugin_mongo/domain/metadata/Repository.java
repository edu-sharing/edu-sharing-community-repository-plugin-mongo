package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Repository extends RangedValue {
    String type;
    String origin;
}
