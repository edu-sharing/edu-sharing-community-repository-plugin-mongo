package org.edu_sharing.plugin_mongo.metadata;

import javax.validation.constraints.NotBlank;
import java.util.Date;

public class BooleanRangedValue extends RangedValue<Boolean> {

    public BooleanRangedValue(String id, @NotBlank Boolean value) {
        super(id, value);
    }
}

