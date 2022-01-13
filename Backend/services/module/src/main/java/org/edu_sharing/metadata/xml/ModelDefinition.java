package org.edu_sharing.metadata.xml;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.edu_sharing.metadata.MongoModelParseException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@XmlRootElement(name = "model", namespace = "http://www.edu-sharing.com/model/dictionary/1.0")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModelDefinition extends StructDefinition {
    private String name;
    private String description;
    private String author;
    private String version;

    public static ModelDefinition createModel(InputStream xmlStream) {
        try {
            JAXBContext jc = JAXBContext.newInstance(ModelDefinition.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            ModelDefinition model = (ModelDefinition) unmarshaller.unmarshal(xmlStream);
            return model;
        } catch (JAXBException e) {
            throw new MongoModelParseException(e);
        }
    }
}
