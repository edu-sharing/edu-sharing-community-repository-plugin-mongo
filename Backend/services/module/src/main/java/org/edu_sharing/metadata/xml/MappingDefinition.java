package org.edu_sharing.metadata.xml;

import lombok.Data;
import org.apache.commons.lang.NotImplementedException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@Data
@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.FIELD)
public class MappingDefinition {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "converter")
    @XmlJavaTypeAdapter(TypeConverterAdapter.class)
    private Converter converter;

    public Object convertToAlf(Object value) {
        if(value == null) {
            return null;
        }

         if(converter != null){
             return converter.toAlf(value);
         }

         return value;
    }

    public Object convertToMongo(Object value) {
        if(value == null) {
            return null;
        }

        if(converter != null){
            return converter.toMongo(value);
        }

        return value;
    }
}

