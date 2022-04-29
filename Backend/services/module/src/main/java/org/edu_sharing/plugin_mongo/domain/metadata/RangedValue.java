package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;
import org.apache.commons.lang.NullArgumentException;

import javax.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RangedValue {
    String id;

    @NotBlank
    String value;
}

