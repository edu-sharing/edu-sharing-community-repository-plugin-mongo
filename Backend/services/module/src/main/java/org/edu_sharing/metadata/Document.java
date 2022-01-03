package org.edu_sharing.metadata;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Document {


    Document set(String key, Object value);

    Object get(String key);

    <T> T get(String key, Class<T> clazz);

    <T> T get(String key, T defaultValue);

    Integer getInteger(String key);

    Integer getInteger(String key, int defaultValue);

    Long getLong(String key);

    Long getLong(String key, long defaultValue);

    Double getDouble(String key);

    Double getDouble(String key, double defaultValue);

    Boolean getBoolean(String key);

    Boolean getBoolean(String key, boolean defaultValue);

    String getString(String key);

    String getString(String key, String defaultValue);

    Date getDate(String key);

    Date getDate(String key, Date defaultValue);

    String toJson();

    boolean isEmpty();

    boolean containsKey(String key);

    Object remove(String key);

    void clear();
}
