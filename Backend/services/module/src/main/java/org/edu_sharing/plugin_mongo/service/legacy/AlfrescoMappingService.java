package org.edu_sharing.plugin_mongo.service.legacy;

import com.bazaarvoice.jolt.Chainr;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.service.cmr.repository.ContentData;
import org.bson.Document;
import org.edu_sharing.plugin_mongo.datamodel.MongoModelDictionary;
import org.edu_sharing.plugin_mongo.datamodel.MongoModelInfo;
import org.edu_sharing.plugin_mongo.datamodel.remover.Remover;
import org.edu_sharing.plugin_mongo.domain.metadata.Metadata;
import org.edu_sharing.plugin_mongo.joltextension.CustomFunction;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.tools.URLTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AlfrescoMappingService {

    public static final String MONGO_CONTENT_KEY = "content";
    public static final String ALF_CONTENT_KEY = "{http://www.alfresco.org/model/content/1.0}content";
    public static final String ALFMAP_KEY = "alfMap";
    public static final String ID_KEY = "_id";

    private final MongoModelDictionary modelDictionary;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getProperties(Document rootDocument) {
        final Map<String, Object> properties = new HashMap<>();

        for (Map.Entry<String, Object> entry : rootDocument.entrySet()) {
            String model = entry.getKey();
            if (Objects.equals(model, ID_KEY)) {
                continue;
            }

            if (Objects.equals(model, MONGO_CONTENT_KEY)) {
                CustomFunction.contentDataWithIdFromMap mapper = new CustomFunction.contentDataWithIdFromMap();
                properties.put(ALF_CONTENT_KEY, mapper.apply(entry.getValue()).get());
                continue;
            }

            MongoModelInfo modelInfo = modelDictionary.getModelInfo(model);
            if (modelInfo == null) {
                continue;
            }

            Chainr chainr = modelInfo.getMongo2alfChainr();
            Map props = (Map) chainr.transform(entry.getValue());
            if (props != null && !props.isEmpty()) {
                properties.putAll(props);
            }
        }

        return properties;
    }

    public void setProperties(Document rootDocument, Map<String, Object> properties) {

        HashMap<String, Object> copyProperties = new HashMap<>(properties);

        if (properties.containsKey(ALF_CONTENT_KEY)) {
            CustomFunction.mapFromContentDataWithId mapper = new CustomFunction.mapFromContentDataWithId();
            rootDocument.put(MONGO_CONTENT_KEY, mapper.apply(copyProperties.get(ALF_CONTENT_KEY)).get());
            copyProperties.remove(ALF_CONTENT_KEY);
        }

        for (MongoModelInfo modelInfo : modelDictionary.getModelInfos()) {
            if (modelInfo.getName().equals(ALFMAP_KEY)) {
                continue;
            }

            Chainr alf2Mongo = modelInfo.getAlf2mongoChainr();
            Chainr mongo2Alf = modelInfo.getMongo2alfChainr();
            Object mongoValues = alf2Mongo.transform(copyProperties);
            if (mongoValues == null) {
                continue;
            }
            Map<String, Object> alfValues = (Map<String, Object>) mongo2Alf.transform(mongoValues);
            for (Map.Entry<String, Object> entry : alfValues.entrySet()) {
                copyProperties.remove(entry.getKey());
            }

            Object modelDocument = rootDocument.get(modelInfo.getName());
            if (modelDocument != null) {
                modelInfo.getMerger().merge(mongoValues, modelDocument);
            } else {
                rootDocument.put(modelInfo.getName(), mongoValues);
            }
        }

        if (!copyProperties.isEmpty()) {
            MongoModelInfo modelInfo = modelDictionary.getModelInfo(ALFMAP_KEY);
            Chainr alf2Mongo = modelInfo.getAlf2mongoChainr();
            Object mongoValues = alf2Mongo.transform(copyProperties);

            Object modelDocument = rootDocument.get(modelInfo.getName());
            if (modelDocument != null) {
                modelInfo.getMerger().merge(mongoValues, modelDocument);
            } else {
                rootDocument.put(modelInfo.getName(), mongoValues);
            }
        }

    }

    public void removeProperties(Document rootDocument, List<String> props) {
        final Map<String, Object> properties = props.stream().collect(Collectors.toMap(x -> x, x -> Remover.RemoveToken));

        for (Map.Entry<String, Object> entry : rootDocument.entrySet()) {
            String modelName = entry.getKey();
            if (Objects.equals(modelName, ID_KEY)) {
                continue;
            }

            if (Objects.equals(modelName, MONGO_CONTENT_KEY) && props.contains(ALF_CONTENT_KEY)) {
                properties.remove(MONGO_CONTENT_KEY);
                continue;
            }

            Object model = entry.getValue();
            if (!(model instanceof Map)) {
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

