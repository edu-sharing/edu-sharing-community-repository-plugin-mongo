package org.edu_sharing.metadata;

import org.edu_sharing.metadata.xml.ModelDefinition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MongoModelDictionaryImpl implements MongoModelDictionary {

    private Map<String, ModelDefinition> models = new HashMap<>();

    @Override
    public void putModel(ModelDefinition model) {
        models.put(model.getName(), model);
    }

    @Override
    public Collection<String> getModels() {
        return models.keySet();
    }

    @Override
    public ModelDefinition getModelDefinition(String modelName){
        return models.get(modelName);
    }
}
