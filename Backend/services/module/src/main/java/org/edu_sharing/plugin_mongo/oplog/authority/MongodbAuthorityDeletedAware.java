package org.edu_sharing.plugin_mongo.oplog.authority;

public interface MongodbAuthorityDeletedAware {
    void onAuthorityDeleted(DeleteAuthorityMongoAlfOpLogData actionData);
}
