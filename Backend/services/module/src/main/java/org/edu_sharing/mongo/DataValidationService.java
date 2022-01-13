package org.edu_sharing.mongo;

import org.edu_sharing.metadata.MongoDocumentAdapter;
import org.edu_sharing.metadata.xml.MongoUnknownMappingException;

public interface DataValidationService {
    void validate(String modelName, MongoDocumentAdapter mongoDocumentAdapter) throws MongoUnknownMappingException;
}
