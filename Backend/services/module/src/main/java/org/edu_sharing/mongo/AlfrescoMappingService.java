package org.edu_sharing.mongo;

import lombok.Builder;
import org.apache.commons.lang.NotImplementedException;
import org.apache.xpath.operations.Bool;
import org.bson.Document;
import org.edu_sharing.metadata.MongoDocumentAdapter;
import org.edu_sharing.metadata.MongoModelDictionary;
import org.edu_sharing.metadata.xml.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class AlfrescoMappingService {

    private final MongoModelDictionary modelDictionary;

    public AlfrescoMappingService(MongoModelDictionary modelDictionary) {
        this.modelDictionary = modelDictionary;
    }

    public Map<String, Object> GetProperties(String model, org.edu_sharing.metadata.Document document) {
        if (!(document instanceof MongoDocumentAdapter)) {
            throw new NoMongoDocumentAdapterException(document.getClass());
        }
        MongoDocumentAdapter mongoAdapter = (MongoDocumentAdapter) document;
        ModelDefinition modelDefinition = modelDictionary.getModelDefinition(model);
        final Map<String, Object> properties = new HashMap<>();

        ModelRunner.builder()
                .propertyDefinitionConsumer(((propertyDefinition, value) -> {
                    String mapping = propertyDefinition.getNativeMapping();
                    if (mapping != null && !mapping.trim().isEmpty()) {
                        properties.put(mapping, value);
                    }
                }))
                .build()
                .run(modelDefinition, mongoAdapter.getRootDocument());

        return properties;
    }

    public void SetProperties(String model, Map<String, Object> properties) {
        ModelDefinition modelDefinition = modelDictionary.getModelDefinition(model);
        MongoDocumentAdapter mongoAdapter = new MongoDocumentAdapter();
        ModelRunner.builder()
                .propertyDefinitionFunction(((propertyDefinition) -> {
                    String mapping = propertyDefinition.getNativeMapping();
                    if (mapping != null && !mapping.trim().isEmpty()) {
                        if (properties.containsKey(mapping)) {
                            return properties.get(mapping);
                        }
                    }
                    return null;
                }))
                .build()
                .run(modelDefinition, mongoAdapter.getRootDocument());
    }
}

@Builder
class ModelRunner {
    private boolean createIfNotExist;
    private Function<PropertyDefinition, Object> propertyDefinitionFunction;
    private BiConsumer<PropertyDefinition, Object> propertyDefinitionConsumer;
    private BiConsumer<TypeDefinition, Object> typeDefinitionConsumer;

    public void run(ModelDefinition modelDefinition, Document mongoDocument) {
        for (PropertyDefinition property : modelDefinition.getProperties()) {
            InvestigateProperty(mongoDocument, property);
        }
    }

    private void InvestigateBaseType(Document mongoDocument, Map<String, BaseTypeDefinition> baseTypeMap) {
        String documentName = mongoDocument.getString("name");
        if (documentName == null) {
            throw new MongoTypeNameNotFoundException();
        }

        if (!baseTypeMap.containsKey(mongoDocument.getString("name"))) {
            throw new MongoUnknownTypeException(documentName);
        }

        BaseTypeDefinition baseType = baseTypeMap.get(documentName);
        if (baseType instanceof TypeDefinition) {
            InvestigateType(mongoDocument, (TypeDefinition) baseType);
        } else {
            throw new NotImplementedException(baseType.getClass());
        }
    }

    void InvestigateType(Document mongoDocument, TypeDefinition typeDefinition) {
        if (typeDefinitionConsumer != null) {
            typeDefinitionConsumer.accept(typeDefinition, mongoDocument);
        }

        for (PropertyDefinition property : typeDefinition.getProperties()) {
            InvestigateProperty(mongoDocument, property);
        }
    }

    private void InvestigateProperty(Document mongoDocument, PropertyDefinition propertyDefinition) {
        mongoDocument.toBsonDocument()
        String name = propertyDefinition.getName();
        Map<String, BaseTypeDefinition> types = propertyDefinition.getTypes();

        if (types != null) {
            if (propertyDefinition.isMultiple()) {
                for (Document document : mongoDocument.getList(name, Document.class)) {
                    InvestigateBaseType(document, types);
                }

            } else {
                Document document = mongoDocument.get(name, Document.class);
                InvestigateBaseType(document, types);
            }
            return;
        }

        if (propertyDefinitionConsumer != null) {
            Object value;
            switch (propertyDefinition.getNativeType()) {
                case Date:
                    value = propertyDefinition.isMultiple() ? mongoDocument.getList(name, Date.class) : mongoDocument.getDate(name);
                    break;
                case Double:
                    value = propertyDefinition.isMultiple() ? mongoDocument.getList(name, Double.class) : mongoDocument.getDouble(name);
                    break;
                case String:
                    value = propertyDefinition.isMultiple() ? mongoDocument.getList(name, String.class) : mongoDocument.getString(name);
                    break;
                case Boolean:
                    value = propertyDefinition.isMultiple() ? mongoDocument.getList(name, Boolean.class) : mongoDocument.getBoolean(name);
                    break;
                case Integer:
                    value = propertyDefinition.isMultiple() ? mongoDocument.getList(name, Integer.class) : mongoDocument.getInteger(name);
                    break;
                case Long:
                    value = propertyDefinition.isMultiple() ? mongoDocument.getList(name, Long.class) : mongoDocument.getLong(name);
                    break;
                default:
                    throw new NotImplementedException(propertyDefinition.getNativeType().toString());
            }

            if (value != null) {
                propertyDefinitionConsumer.accept(propertyDefinition, value);
            }
        }

        if (propertyDefinitionFunction != null) {
            Object value = propertyDefinitionFunction.apply(propertyDefinition);
            if (value instanceof List<?> != propertyDefinition.isMultiple()) {
                throw new MongoTypeMappingException(value.getClass().getName() + " does not match with property type!");
            }

            NativeTypeDefinition nativeType = propertyDefinition.getNativeType();
            if (!(nativeType == NativeTypeDefinition.Boolean && value instanceof Boolean
                    || nativeType == NativeTypeDefinition.Date && value instanceof Date
                    || nativeType == NativeTypeDefinition.Double && value instanceof Double
                    || nativeType == NativeTypeDefinition.Integer && value instanceof Integer
                    || nativeType == NativeTypeDefinition.Long && value instanceof Long)) {
                throw new MongoTypeMappingException(value.getClass().getName() + " does not match with property type!");
            }

            mongoDocument.put(name, value);
        }
    }

}
