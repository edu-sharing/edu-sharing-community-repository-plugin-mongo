package org.edu_sharing.metadata;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.edu_sharing.metadata.xml.ModelDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MongoModelBootstrap {

    //The list of models to bootsrap with
    @Getter
    private List<String> models = new ArrayList<>();

    @Setter
    private MongoModelDictionary modelDictionary;

    private final static Logger logger = Logger.getLogger(MongoModelBootstrap.class);

    public void bootstrap() {
        long startTime = System.currentTimeMillis();

        Collection<String> modelsBefore = modelDictionary.getModels(); // note: on first bootstrap will init empty dictionary
        int modelsBeforeCnt = modelsBefore != null ? modelsBefore.size() : 0;

        if (logger.isTraceEnabled()) {
            logger.trace("bootstrap: [" + Thread.currentThread() + "]");
        }


        for (String bootstrapModel : models) {
            InputStream modelStream = getClass().getClassLoader().getResourceAsStream(bootstrapModel);
            if (modelStream == null) {
                throw new MongoModelNotFoundException(bootstrapModel);
            }

            try {
                ModelDefinition model = ModelDefinition.createModel(modelStream);
                if (logger.isDebugEnabled()) {
                    logger.debug("Loading model: " + model.getName() + " (from " + bootstrapModel + ")");
                }
                modelDictionary.putModel(model);

            } catch (MongoModelParseException ex) {
                throw new MongoModelDictionaryException(ex, bootstrapModel);
            } finally {
                try {
                    modelStream.close();
                } catch (IOException ioe) {
                    logger.warn("Failed to close model input stream for '" + bootstrapModel + "': " + ioe);
                }
            }

            Collection<String> modelsAfter = modelDictionary.getModels();
            int modelsAfterCnt = modelsAfter != null ? modelsAfter.size() : 0;
            if (logger.isDebugEnabled()) {
                logger.debug("Model count: before=" + modelsBeforeCnt + ", load=" + models.size() + ", after=" + modelsAfterCnt + " in " + (System.currentTimeMillis() - startTime) + " msecs [" + Thread.currentThread() + "]");
            }
        }

    }
}
