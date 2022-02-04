package org.edu_sharing.plugin_mongo.datamodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MongoModelDictionaryImpl implements MongoModelDictionary {

    private final Map<String, MongoModelInfo> models = new HashMap<>();

    @Override
    public void putModel(MongoModelInfo model) {
        models.put(model.getName(), model);
    }

    @Override
    public Collection<String> getModels() {
        return models.keySet();
    }

    @Override
    public MongoModelInfo getModelInfo(String modelName){
        return models.get(modelName);
    }
}
