package org.edu_sharing.plugin_mongo.metadata;


import javax.validation.constraints.NotBlank;

public class IntRangedValue extends RangedValue<Long> {

    public IntRangedValue(String id, @NotBlank Long value) {
        super(id, value);
    }
}

