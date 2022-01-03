package org.edu_sharing.metadata.xml;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@XmlRootElement(name="type")
@XmlAccessorType(XmlAccessType.FIELD)
public class TypeDefinition extends BaseTypeDefinition {

    @XmlAttribute(required = true)
    private String name;

    @XmlElement(name = "property")
    private List<PropertyDefinition> properties = new ArrayList<>();


    @XmlTransient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, PropertyDefinition> mappedProperties;
    public Map<String, PropertyDefinition> getProperties(){
        if(mappedProperties == null){
            mappedProperties = properties.stream().collect(Collectors.toMap(PropertyDefinition::getName, x->x));
        }

        return mappedProperties;
    }


    public Map<String, PropertyDefinition> getMappedProperties() {
        if (mappedProperties == null) {
            mappedProperties = properties.stream().collect(Collectors.toMap(PropertyDefinition::getName, x -> x));
        }

        return mappedProperties;
    }

    public PropertyDefinition getPropertyDefinition(String name) {
        PropertyDefinition propertyDefinition = getMappedProperties().get(name);
        if (propertyDefinition == null) {
            throw new MongoUnknownMappingException("PropertyDefinition: '" + name + "' not found");
        }
        return propertyDefinition;
    }
}
