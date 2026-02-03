package org.edu_sharing.plugin_mongo.tracking;


import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TrackedDataTimestampBeforeConvertCallback implements BeforeConvertCallback<TrackedData> {

    @NotNull
    @Override
    public TrackedData onBeforeConvert(@NotNull TrackedData entity, @NotNull String collection) {
        entity.setTimestamp(new Date());
        return entity;
    }
}