package org.edu_sharing.plugin_mongo.oplog.io;

public interface MongodbIoDeletedAware {
    void onIoDeleted(DeleteIoMongoAlfOpLogData actionData);
}
