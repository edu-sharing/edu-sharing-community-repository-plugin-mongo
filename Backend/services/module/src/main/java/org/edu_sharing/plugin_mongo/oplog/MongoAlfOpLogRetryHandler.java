package org.edu_sharing.plugin_mongo.oplog;

public interface MongoAlfOpLogRetryHandler <T extends MongoAlfOpLogData> {
    Class<T> getRetryableType();
    void retry(MongoAlfOpLogData opLogData);
}
