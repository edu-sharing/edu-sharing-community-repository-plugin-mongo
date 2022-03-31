package org.edu_sharing.plugin_mongo.metadata;

import javax.validation.constraints.NotBlank;


public class FloatRangedValue extends RangedValue<Double> {
    public FloatRangedValue(String id, @NotBlank Double value) {
        super(id, value);
    }
}
