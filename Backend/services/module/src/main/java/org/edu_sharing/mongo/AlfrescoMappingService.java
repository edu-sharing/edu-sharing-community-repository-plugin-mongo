package org.edu_sharing.mongo;

import org.bson.Document;
import org.edu_sharing.metadata.MongoModelDictionary;
import org.edu_sharing.metadata.xml.*;
import org.hibernate.engine.Mapping;

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
                    List<MappingDefinition> mappings = propertyDefinition.getMappings();
                    if (mappings == null || mappings.isEmpty()) {
                        return;
                    }

                    Object value = document.get(propertyDefinition.getName());
                    if (value != null) {
                        // we only give the first mapping back to alfresco
                        MappingDefinition mappingDefinition = mappings.get(0);
                        properties.put(mappingDefinition.getName(), mappingDefinition.convertToAlf(value));
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
                    List<MappingDefinition> mappings = propertyDefinition.getMappings();
                    if (mappings == null || mappings.isEmpty()) {
                        return;
                    }

                    Class propertyTypeDefinition = TYPE_MAPPING.get(propertyDefinition.getNativeType());

                    //Find the first mapping and that returns a value of non-null
                    for (MappingDefinition mappingDefinition : mappings) {
                        Object value = mappingDefinition.convertToMongo(properties.get(mappingDefinition.getName()));

                        if (value == null) {
                            continue;
                        }

                        if (propertyDefinition.isMultiple()) {
                            if (!(value instanceof Collection<?>) && !value.getClass().isArray()) {
                                throw new MongoInvalidTypeException(mappingDefinition.getName() + " is not a list but a list value was expected");
                            }

                            if (value.getClass().isArray()) {
                                Class clazz = value.getClass().getComponentType();
                                if (!propertyTypeDefinition.isAssignableFrom(clazz)) {
                                    throw new MongoInvalidTypeException(mappingDefinition.getName() + " is not of type " + propertyDefinition.getNativeType() + "[]");
                                }
                            } else {
                                for (Object item : (Collection) value) {
                                    Class clazz = item.getClass();
                                    if (!propertyTypeDefinition.isAssignableFrom(clazz)) {
                                        throw new MongoInvalidTypeException(mappingDefinition.getName() + " not all elements are of type " + propertyDefinition.getNativeType());
                                    }
                                }
                            }

                        } else {
                            if (value instanceof List<?> || value.getClass().isArray()) {
                                throw new MongoInvalidTypeException(mappingDefinition.getName() + " is a list but a single value was expected");
                            }

                            Class clazz = value.getClass();
                            if (!propertyTypeDefinition.isAssignableFrom(clazz)) {
                                throw new MongoInvalidTypeException(mappingDefinition.getName() + " is not of type " + propertyDefinition.getNativeType());
                            }
                        }

                        document.put(mappingDefinition.getName(), value);
                        return;
                    }
                })
                .build()
                .run(modelDefinition, rootDocument);
    }

    public void removeProperties(Document rootDocument, List<String> props) {
        ModelRunner.builder()
                .removeIfEmpty(true)
                .propertyVisitor((propertyDefinition, document) -> {
                    List<MappingDefinition> mappings = propertyDefinition.getMappings();
                    if (mappings == null || mappings.isEmpty()) {
                        return;
                    }
                    for (MappingDefinition mappingDefinition : mappings) {
                        if (props.contains(mappingDefinition.getName())) {
                            document.remove(propertyDefinition.getName());
                            break;
                        }
                    }
                })
                .build()
                .run(modelDefinition, rootDocument);
    }
}

