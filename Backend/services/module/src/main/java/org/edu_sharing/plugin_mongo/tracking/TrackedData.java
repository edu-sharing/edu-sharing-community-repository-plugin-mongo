package org.edu_sharing.plugin_mongo.tracking;

import java.util.Date;

public interface TrackedData {
    String getId();
    Date getTimestamp();
    void setTimestamp(Date timestamp);
}
