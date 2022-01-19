package org.edu_sharing.metadata.xml;

import lombok.Builder;
import org.bson.Document;
import org.edu_sharing.metadata.xml.ModelDefinition;
import org.edu_sharing.metadata.xml.PropertyDefinition;
import org.edu_sharing.metadata.xml.StructDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Builder
public class ModelRunner {
    private boolean createIfNotExist;
    private boolean removeIfEmpty;

    private BiConsumer<PropertyDefinition, Document> propertyVisitor;

    public void run(ModelDefinition modelDefinition, Document document) {
        for (PropertyDefinition property : modelDefinition.getProperties()) {
            InvestigateProperty(document, property);
        }
    }


    void InvestigateStruct(Document document, StructDefinition structDefinition) {
        for (PropertyDefinition property : structDefinition.getProperties()) {
            InvestigateProperty(document, property);
        }
    }

    private void InvestigateProperty(Document document, PropertyDefinition propertyDefinition) {

        List<StructDefinition> structs = propertyDefinition.getStructs();

        if (structs == null) {
            propertyVisitor.accept(propertyDefinition, document);
            return;
        }

        String name = propertyDefinition.getName();
        if (propertyDefinition.isMultiple()) {
            List<Document> childDocuments = document.getList(name, Document.class);
            if (childDocuments == null) {
                childDocuments = new ArrayList<>(structs.size());
                for (int i = 0; i < structs.size(); i++) {
                    childDocuments.add(new Document());
                }

                if (createIfNotExist) {
                    document.put(name, childDocuments);
                }
            }

            List<Document> deleteList = new ArrayList<>();
            for (Document childDocument : childDocuments) {
                for (StructDefinition struct : structs) {
                    InvestigateStruct(childDocument, struct);
                }

                if (removeIfEmpty && childDocument.isEmpty()) {
                    deleteList.add(childDocument);
                }
            }

            if (removeIfEmpty) {
                childDocuments.removeAll(deleteList);
                if (childDocuments.isEmpty()) {
                    document.remove(name);
                }
            }

        } else {
            Document childDocument = document.get(name, Document.class);
            if (childDocument == null) {
                childDocument = new Document();
                if(createIfNotExist) {
                    document.put(name, childDocument);
                }
            }

            for (StructDefinition struct : structs) {
                InvestigateStruct(childDocument, struct);
            }

            if (removeIfEmpty && childDocument.isEmpty()) {
                document.remove(name);
            }
        }
    }

}
