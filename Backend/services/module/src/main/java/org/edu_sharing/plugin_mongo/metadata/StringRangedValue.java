package org.edu_sharing.plugin_mongo.metadata;

import javax.validation.constraints.NotBlank;

public class StringRangedValue extends RangedValue<String> {

    public StringRangedValue(String id, @NotBlank String value) {
        super(id, value);
    }
}


