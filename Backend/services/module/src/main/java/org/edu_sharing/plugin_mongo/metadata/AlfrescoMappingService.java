package org.edu_sharing.plugin_mongo.metadata;

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

    public static final String MONGO_CONTENT_KEY = "content";
    public static final String ALF_CONTENT_KEY = "{http://www.alfresco.org/model/content/1.0}content";

    private final MongoModelDictionary modelDictionary;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getProperties(Document rootDocument) {
        final Map<String, Object> properties = new HashMap<>();

        for (Map.Entry<String, Object> entry : rootDocument.entrySet()){
            String model = entry.getKey();
            if(Objects.equals(model, "_id")){
                continue;
            }

            if(Objects.equals(model, MONGO_CONTENT_KEY)){
                properties.put(ALF_CONTENT_KEY, entry.getValue().toString());
                continue;
            }

            MongoModelInfo modelInfo = modelDictionary.getModelInfo(model);
            if(modelInfo == null){
                continue;
            }

            Chainr chainr = modelInfo.getMongo2alfChainr();
            Map props = (Map)chainr.transform(rootDocument);
            if(props != null && !props.isEmpty()) {
                properties.putAll(props);
            }
        }


        return properties;
    }

    public void setProperties(Document rootDocument, HashMap<String, Object> properties) {

        if(properties.containsKey(ALF_CONTENT_KEY)){
            rootDocument.put(MONGO_CONTENT_KEY, properties.get(ALF_CONTENT_KEY));
        }

        for (MongoModelInfo modelInfo : modelDictionary.getModelInfos()){

            Chainr chainr = modelInfo.getAlf2mongoChainr();
            Object updateValues = chainr.transform(properties);
            if(updateValues == null){
                continue;
            }

            Object modelDocument = rootDocument.get(modelInfo.getName());
            if(modelDocument != null) {
                modelInfo.getMerger().merge(updateValues, modelDocument);
            }else {
                rootDocument.put(modelInfo.getName(), updateValues);
            }
        }
    }

    public void removeProperties(Document rootDocument, List<String> props) {
        final Map<String, Object> properties = props.stream().collect(Collectors.toMap(x->x, x-> Remover.RemoveToken));

        for (Map.Entry<String, Object> entry : rootDocument.entrySet()) {
            String modelName = entry.getKey();
            if (Objects.equals(modelName, "_id")) {
                continue;
            }

            if (Objects.equals(modelName, MONGO_CONTENT_KEY) && props.contains(ALF_CONTENT_KEY)) {
                properties.remove(MONGO_CONTENT_KEY);
                continue;
            }

            Object model = entry.getValue();
            if(!(model instanceof Map)) {
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

