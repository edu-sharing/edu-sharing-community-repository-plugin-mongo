package org.edu_sharing.plugin_mongo.metadata;

import javax.validation.constraints.NotBlank;
import java.util.Date;

public class DateRangedValue extends RangedValue<Date> {

    public DateRangedValue(String id, @NotBlank Date value) {
        super(id, value);
    }
}
