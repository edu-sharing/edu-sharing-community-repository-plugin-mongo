package org.edu_sharing.plugin_mongo.mongo.util;

import org.bson.codecs.pojo.InstanceCreator;
import org.bson.codecs.pojo.PropertyModel;

import java.util.HashMap;
import java.util.Map;

public abstract class BufferedInstanceCreator<T> implements InstanceCreator<T> {
    protected final Map<String, Object> buffer = new HashMap<>();

    @Override
    public <S> void set(S s, PropertyModel<S> propertyModel) {
        buffer.put(propertyModel.getWriteName(), s);
    }

    protected <S> S getValue(String propName) {
        return (S) buffer.get(propName);
    }
}

