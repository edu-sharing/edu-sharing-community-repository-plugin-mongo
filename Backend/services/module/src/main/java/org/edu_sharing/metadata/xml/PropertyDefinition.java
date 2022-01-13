package org.edu_sharing.metadata.xml;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.*;

@Data
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class PropertyDefinition {

    @XmlElement(name="struct")
    private List<StructDefinition> structs;

    @XmlElementWrapper(name = "mappings")
    private List<MappingDefinition> mappings;

    @XmlAttribute(name = "type")
    private NativeTypeDefinition nativeType;

    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute
    private boolean multiple;

    @XmlAttribute(name = "mapping")
    private String nativeMapping;
}