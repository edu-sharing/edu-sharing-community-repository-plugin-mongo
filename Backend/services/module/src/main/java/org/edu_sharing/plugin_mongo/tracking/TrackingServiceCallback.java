package org.edu_sharing.plugin_mongo.tracking;

import org.springframework.data.domain.Limit;

import java.util.Date;
import java.util.List;

public interface TrackingServiceCallback<T> {
    List<T> getTrackedData(Date from, Limit limit);
    List<T> getDeletedTrackedData(Date from, Limit limit);
}
