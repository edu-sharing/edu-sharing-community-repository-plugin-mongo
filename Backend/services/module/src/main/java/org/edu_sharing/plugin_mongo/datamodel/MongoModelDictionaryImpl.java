package org.edu_sharing.plugin_mongo.datamodel;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component("mongoModelDictionary")
public class MongoModelDictionaryImpl implements MongoModelDictionary {

    private final Map<String, MongoModelInfo> models = new HashMap<>();

    @Override
    public void putModel(MongoModelInfo model) {
        models.put(model.getName(), model);
    }

    @Override
    public Collection<MongoModelInfo> getModelInfos() {
        return models.values();
    }

    @Override
    public MongoModelInfo getModelInfo(String modelName){
        return models.get(modelName);
    }
}
