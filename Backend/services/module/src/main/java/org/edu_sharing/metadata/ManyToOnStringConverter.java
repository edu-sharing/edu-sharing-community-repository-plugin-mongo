package org.edu_sharing.metadata;

import org.edu_sharing.metadata.xml.Converter;

import java.util.List;

public class ManyToOnStringConverter implements Converter {
    @Override
    public Object toMongo(Object alfValue) {
        if(alfValue == null){
            return null;
        }

        if(alfValue instanceof List<?>){
            return String.join("; ", (List)alfValue);
        }

        if(alfValue.getClass().isArray()){
            return String.join("; ", (String[])alfValue);
        }

        return alfValue;
    }

    @Override
    public Object toAlf(Object mongoValue) {

        if(mongoValue instanceof String){
            return ((String)mongoValue).split("\\s*;\\s*");
        }

        return mongoValue;
    }
}
