package org.edu_sharing.mongo;

import org.apache.commons.lang.NotImplementedException;
import org.bson.Document;
import org.edu_sharing.metadata.MongoDocumentAdapter;
import org.edu_sharing.metadata.MongoModelDictionary;
import org.edu_sharing.metadata.xml.*;

import java.util.*;

public class DataValidationServiceImpl {

    private MongoModelDictionary modelDictionary;

    private final static String ID_KEY = "_id";

    private final static Map<NativeTypeDefinition, Class<?>> nativeTypeMap = new HashMap<NativeTypeDefinition, Class<?>>(){{
        put(NativeTypeDefinition.String, String.class);
        put(NativeTypeDefinition.Integer, Integer.class);
        put(NativeTypeDefinition.Date, Date.class);
        put(NativeTypeDefinition.Boolean, Boolean.class);
        put(NativeTypeDefinition.Double, Double.class);
        put(NativeTypeDefinition.Long, Long.class);
    }};


    public void validate(String modelName, MongoDocumentAdapter mongoDocumentAdapter) throws MongoUnknownMappingException {
        ModelDefinition model = modelDictionary.getModelDefinition(modelName);

        Document mongoDocument = mongoDocumentAdapter.getRootDocument();
        boolean hasIdKey = false;
        for (Map.Entry<String, Object> entry : mongoDocument.entrySet()) {
            String key = entry.getKey();
            if (Objects.equals(key, ID_KEY)) {
                hasIdKey = true;
                continue;
            }

            PropertyDefinition propertyDefinition = model.getPropertyDefinition(key);
            InvestigateProperty(propertyDefinition, entry.getValue());
        }
    }

    private void InvestigateProperty(PropertyDefinition propertyDefinition, Object value) {
        if (propertyDefinition.isMultiple()) {
            if (!(value instanceof List || value.getClass().isArray())) {
                throw new MongoInvalidTypeException("Property '" + propertyDefinition.getName() + "' is not of type list");
            }


        } else {
            if (value instanceof List || value.getClass().isArray()) {
                throw new MongoInvalidTypeException("Property '" + propertyDefinition.getName() + "' does not expect a list");
            }


        }

        Map<String, BaseTypeDefinition> types = propertyDefinition.getTypes();
        if(types == null){
            Class<?> nativeType = nativeTypeMap.get(propertyDefinition.getNativeType());
            if(nativeType == null){
                throw new NotImplementedException();
            }


        }

    }
}
