package org.edu_sharing.plugin_mongo.oplog.person;

public interface MongodbPersonDeletedAware {
    void onPersonDeleted(DeletePersonMongoAlfOpLogData actionData);
}
