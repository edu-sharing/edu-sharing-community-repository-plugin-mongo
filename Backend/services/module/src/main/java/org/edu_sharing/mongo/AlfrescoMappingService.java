package org.edu_sharing.mongo;

import org.bson.Document;
import org.edu_sharing.metadata.MongoModelDictionary;
import org.edu_sharing.metadata.xml.*;

import java.util.*;

public class AlfrescoMappingService {


    private final ModelDefinition modelDefinition;
    private final static HashMap<NativeTypeDefinition, Class> TYPE_MAPPING = new HashMap<NativeTypeDefinition, Class>() {{
        put(NativeTypeDefinition.Integer, Integer.class);
        put(NativeTypeDefinition.String, String.class);
        put(NativeTypeDefinition.Boolean, Boolean.class);
        put(NativeTypeDefinition.Date, Date.class);
        put(NativeTypeDefinition.Double, Double.class);
        put(NativeTypeDefinition.Long, Long.class);
    }};


    public AlfrescoMappingService(String model, MongoModelDictionary modelDictionary) {
        this.modelDefinition = modelDictionary.getModelDefinition(model);
    }

    public Map<String, Object> getProperties(Document rootDocument) {
        final Map<String, Object> properties = new HashMap<>();

        ModelRunner.builder()
                .propertyVisitor((propertyDefinition, document) -> {
                    String mapping = propertyDefinition.getNativeMapping();
                    if (mapping == null || mapping.trim().isEmpty()) {
                        return;
                    }

                    Object value = document.get(propertyDefinition.getName());
                    if (value != null) {
                        properties.put(mapping.trim(), value);
                    }
                })
                .build()
                .run(modelDefinition, rootDocument);

        return properties;
    }

    public void setProperties(Document rootDocument, HashMap<String, Object> properties) {
        ModelRunner.builder()
                .createIfNotExist(true)
                .removeIfEmpty(true)
                .propertyVisitor((propertyDefinition, document) -> {
                    String mapping = propertyDefinition.getNativeMapping();
                    if (mapping == null || mapping.trim().isEmpty()) {
                        return;
                    }

                    Class propertyTypeDefinition = TYPE_MAPPING.get(propertyDefinition.getNativeType());
                    Object value = properties.get(mapping);
                    if (propertyDefinition.isMultiple()) {
                        if(!(value instanceof Collection<?>) && !value.getClass().isArray()) {
                            throw new MongoInvalidTypeException(mapping + " is not a list but a list value was expected");
                        }


                        if(value.getClass().isArray()){
                            Class clazz = value.getClass().getComponentType();
                            if(!propertyTypeDefinition.isAssignableFrom(clazz)){
                                throw new MongoInvalidTypeException(mapping + " is not of type " + propertyDefinition.getNativeType() + "[]");
                            }
                        }else {
                            for (Object item: (Collection)value) {
                                Class clazz = item.getClass();
                                if(!propertyTypeDefinition.isAssignableFrom(clazz)){
                                    throw new MongoInvalidTypeException(mapping + " not all elements are of type " + propertyDefinition.getNativeType());
                                }
                            }
                        }

                    } else {
                        if(value instanceof List<?> || value.getClass().isArray()) {
                            throw new MongoInvalidTypeException(mapping + " is a list but a single value was expected");
                        }

                        Class clazz = value.getClass();
                        if(!propertyTypeDefinition.isAssignableFrom(clazz)){
                            throw new MongoInvalidTypeException(mapping + " is not of type " + propertyDefinition.getNativeType());
                        }
                        properties.put(mapping.trim(), value);
                    }
                })
                .build()
                .run(modelDefinition, rootDocument);
    }

    public void removeProperties(Document rootDocument, List<String> props) {
        ModelRunner.builder()
                .removeIfEmpty(true)
                .propertyVisitor((propertyDefinition, document) -> {
                    String mapping = propertyDefinition.getNativeMapping();
                    if (mapping == null || mapping.trim().isEmpty()) {
                        return;
                    }

                    if(props.contains(mapping)){
                        document.clear();
                    }
                })
                .build()
                .run(modelDefinition, rootDocument);
    }
}

