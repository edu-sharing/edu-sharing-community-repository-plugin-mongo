package org.edu_sharing.plugin_mongo.datamodel;

import java.util.Collection;

public interface MongoModelDictionary {
    void putModel(MongoModelInfo model);

    Collection<MongoModelInfo> getModelInfos();

    MongoModelInfo getModelInfo(String modelName);

}
