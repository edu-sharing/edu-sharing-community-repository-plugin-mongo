package org.edu_sharing.metadata.xml;

import lombok.Data;
import org.apache.commons.lang.NotImplementedException;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;

@Data
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class PropertyDefinition {

    @XmlElement(name="struct")
    private List<StructDefinition> structs;

    @XmlElementWrapper(name = "mappings")
    @XmlElement(name="map")
    private List<MappingDefinition> mappings = new ArrayList<>();

    @XmlAttribute(name = "type")
    private NativeTypeDefinition nativeType;

    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute
    private boolean multiple;

    @XmlTransient
    private MappingDefinition nativeMapping;

    @XmlAttribute(name = "mapping")
    @XmlJavaTypeAdapter(NativeMappingDefinitionAdapter.class)
    public void setNativeMapping(MappingDefinition mappingDefinition){
        if(nativeMapping != null){
            mappings.remove(nativeMapping);
        }

        nativeMapping = mappingDefinition;
        mappings.add(0, mappingDefinition);
    }
}

