package org.edu_sharing.metadata.xml;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.FIELD)
public class MappingDefinition {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "converter")
    private String nativeConverter;
}
