package org.edu_sharing.plugin_mongo.tracking;


import org.springframework.beans.factory.annotation.Autowired;

public interface MongoTrackingServiceAware {
    @Autowired
    void setMongoTrackingService(MongoTrackingService mongoTrackingService);
}
