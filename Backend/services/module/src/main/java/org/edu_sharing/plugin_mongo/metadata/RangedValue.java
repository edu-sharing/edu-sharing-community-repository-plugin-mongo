package org.edu_sharing.plugin_mongo.metadata;

import lombok.*;
import org.apache.commons.lang.NullArgumentException;

import javax.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class RangedValue<T> {
    String id;

    @NotBlank
    T value;
}

