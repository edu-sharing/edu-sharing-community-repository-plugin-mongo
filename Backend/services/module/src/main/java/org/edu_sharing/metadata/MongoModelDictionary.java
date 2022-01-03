package org.edu_sharing.metadata;

import org.edu_sharing.metadata.xml.ModelDefinition;

import java.util.Collection;

public interface MongoModelDictionary {
    void putModel(ModelDefinition model);

    Collection<String> getModels();

    ModelDefinition getModelDefinition(String modelName);
}
