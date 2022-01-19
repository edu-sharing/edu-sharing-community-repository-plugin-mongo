package org.edu_sharing.metadata.xml;

public interface Converter {
    Object toMongo(Object alfValue);

    Object toAlf(Object mongoValue);
}
