package org.edu_sharing.metadata;

import javax.xml.bind.JAXBException;

public class MongoModelParseException extends RuntimeException {
    public MongoModelParseException(JAXBException e) {
        super(e);
    }
}
