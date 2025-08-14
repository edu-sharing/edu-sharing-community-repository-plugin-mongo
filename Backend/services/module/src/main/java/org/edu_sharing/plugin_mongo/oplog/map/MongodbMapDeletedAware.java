package org.edu_sharing.plugin_mongo.oplog.map;

public interface MongodbMapDeletedAware {
    void onMapDeleted(DeleteMapMongoAlfOpLogData actionData);
}
