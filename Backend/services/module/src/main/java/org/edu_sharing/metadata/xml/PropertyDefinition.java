package org.edu_sharing.metadata.xml;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.NotImplementedException;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;
import java.util.stream.Collectors;

@Data
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class PropertyDefinition {

    @XmlElements({
        @XmlElement(name="type", type = TypeDefinition.class)
    })
    private List<BaseTypeDefinition> types;

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

    @XmlTransient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, BaseTypeDefinition> mappedTypes;
    public Map<String, BaseTypeDefinition> getTypes(){
        if(mappedTypes == null){
            mappedTypes = types.stream().collect(Collectors.toMap(BaseTypeDefinition::getName, x->x));
        }

        return mappedTypes;
    }

    public Map<String, BaseTypeDefinition> getMappedTypes() {
        if (mappedTypes == null) {
            mappedTypes = types.stream().collect(Collectors.toMap(BaseTypeDefinition::getName, x -> x));
        }

        return mappedTypes;
    }

    public BaseTypeDefinition getTypeDefinition(String name) {
        BaseTypeDefinition propertyDefinition = getMappedTypes().get(name);
        if (propertyDefinition == null) {
            throw new MongoUnknownMappingException("TypeDefinition: '" + name + "' not found");
        }
        return propertyDefinition;
    }
}