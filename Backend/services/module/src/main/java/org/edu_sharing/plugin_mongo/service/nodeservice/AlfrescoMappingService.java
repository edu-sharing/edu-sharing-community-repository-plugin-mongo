package org.edu_sharing.plugin_mongo.service.nodeservice;

import com.bazaarvoice.jolt.Chainr;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.edu_sharing.plugin_mongo.datamodel.MongoModelDictionary;
import org.edu_sharing.plugin_mongo.datamodel.MongoModelInfo;
import org.edu_sharing.plugin_mongo.datamodel.remover.Remover;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class AlfrescoMappingService {

    private final MongoModelDictionary modelDictionary;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getProperties(Document rootDocument) {
        final Map<String, Object> properties = new HashMap<>();

        for (Map.Entry<String, Object> entry : rootDocument.entrySet()){
            String model = entry.getKey();
            if(Objects.equals(model, "_id")){
                continue;
            }

            MongoModelInfo modelInfo = modelDictionary.getModelInfo(model);
            if(modelInfo == null){
                continue;
            }

            Chainr chainr = modelInfo.getMongo2alfChainr();
            Map props = (Map)chainr.transform(rootDocument);
            properties.putAll(props);
        }


        return properties;
    }

    public void setProperties(Document rootDocument, HashMap<String, Object> properties) {
        for (Map.Entry<String, Object> entry : rootDocument.entrySet()){
            String modelName = entry.getKey();
            Map model  = (Map)entry.getValue();
            if(Objects.equals(modelName, "_id")){
                continue;
            }

            MongoModelInfo modelInfo = modelDictionary.getModelInfo(modelName);
            if(modelInfo == null){
                continue;
            }

            Chainr chainr = modelInfo.getAlf2mongoChainr();
            Object updateValues = chainr.transform(properties);
            modelInfo.getMerger().merge(updateValues, model);
        }
    }

    public void removeProperties(Document rootDocument, List<String> props) {
        final Map<String, Object> properties = props.stream().collect(Collectors.toMap(x->x, x-> Remover.RemoveToken));
        for (Map.Entry<String, Object> entry : rootDocument.entrySet()) {
            String modelName = entry.getKey();
            Map model = (Map) entry.getValue();
            if (Objects.equals(modelName, "_id")) {
                continue;
            }

            MongoModelInfo modelInfo = modelDictionary.getModelInfo(modelName);
            if (modelInfo == null) {
                continue;
            }

            Chainr chainr = modelInfo.getAlf2mongoChainr();
            Object toRemove = chainr.transform(properties);
            modelInfo.getRemover().remove(toRemove, model);
        }
    }
}

