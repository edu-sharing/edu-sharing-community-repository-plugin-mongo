package org.edu_sharing.plugin_mongo.datamodel;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MongoMetaDataBootstrap {

    //The list of models to bootsrap with
    @Getter
    @Setter
    private List<MongoModelInfo> modelInfos = new ArrayList<>();

    @Setter
    private MongoModelDictionary modelDictionary;

    private final static Logger logger = Logger.getLogger(MongoMetaDataBootstrap.class);

    public void bootstrap() {
        long startTime = System.currentTimeMillis();

        Collection<String> modelsBefore = modelDictionary.getModels(); // note: on first bootstrap will init empty dictionary
        int modelsBeforeCnt = modelsBefore != null ? modelsBefore.size() : 0;

        if (logger.isTraceEnabled()) {
            logger.trace("bootstrap: [" + Thread.currentThread() + "]");
        }

        for (MongoModelInfo modelInfo : modelInfos) {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading model: " + modelInfo.getName() + " (from " + modelInfo + ")");
            }
            modelDictionary.putModel(modelInfo);
        }

        Collection<String> modelsAfter = modelDictionary.getModels();
        int modelsAfterCnt = modelsAfter != null ? modelsAfter.size() : 0;
        if (logger.isDebugEnabled()) {
            logger.debug("Model count: before=" + modelsBeforeCnt + ", load=" + modelInfos.size() + ", after=" + modelsAfterCnt + " in " + (System.currentTimeMillis() - startTime) + " msecs [" + Thread.currentThread() + "]");
        }
    }
}