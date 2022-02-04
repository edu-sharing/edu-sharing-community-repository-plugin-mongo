package org.edu_sharing.plugin_mongo.datamodel.remover;

public interface Remover {
    String RemoveToken = "null";
    boolean remove(Object from, Object to);
}
