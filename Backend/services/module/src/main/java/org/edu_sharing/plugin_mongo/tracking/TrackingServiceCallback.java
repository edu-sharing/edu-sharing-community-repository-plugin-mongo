package org.edu_sharing.plugin_mongo.tracking;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Limit;

import java.util.Date;
import java.util.List;

public interface TrackingServiceCallback<T> {
    List<T> getTrackedData(@NotNull Date from, Date to, Limit limit);
    List<T> getDeletedTrackedData(@NotNull Date from, Date to, Limit limit);
}
