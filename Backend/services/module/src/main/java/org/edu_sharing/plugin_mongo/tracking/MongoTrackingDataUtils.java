package org.edu_sharing.plugin_mongo.tracking;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

import java.util.Date;

public final class MongoTrackingDataUtils {


    public static UpdateDefinition updateTimeStamp(@NotNull @NonNull UpdateDefinition update) {
        if (update instanceof Update u) {
//            u.currentDate("timestamp"); // may differ if we set it either repo or on db
            return u.set("timestamp", new Date());
        }

        if (update instanceof AggregationUpdate au) {
//            return au.set("timestamp").toValue("$$NOW"); // may differ if we set it either repo or on db
            return au.set("timestamp").toValue(new Date());
        }

        return update;
    }
}
