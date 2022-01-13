package org.edu_sharing.metadata;

import lombok.Getter;

import java.util.*;

public class MongoDocumentAdapter implements Document {

    public  static final String ID_KEY = "_id";

    @Getter
    private final org.bson.Document rootDocument;


    public MongoDocumentAdapter() {
        rootDocument = new org.bson.Document();
    }

    public MongoDocumentAdapter(String key, Object value) {
        rootDocument = new org.bson.Document(key, value);
    }

    public MongoDocumentAdapter(org.bson.Document bsonDoc) {
        rootDocument = bsonDoc;
    }


    @Override
    public Document set(String key, Object value) {
        if (!key.contains(".")) {
            rootDocument.append(key, value);
            return this;
        }

        Object document = rootDocument;
        List<String> keys = Arrays.asList(key.split("\\."));
        for (int i = 0; i < keys.size() - 1; i++) {
            String segment = keys.get(i);
            Object child = ((org.bson.Document) document).get(segment);
            if (!(child instanceof org.bson.Document)) {
                if (child == null) {
                    child = new org.bson.Document();
                    ((org.bson.Document) document).append(segment, child);
                } else {
                    throw new ClassCastException(String.format("At key %s, the value is not a Document (%s)", key, document.getClass().getName()));
                }
            }
            document = child;
        }

        if (value instanceof Document) {
            ((org.bson.Document) document).append(keys.get(keys.size() - 1), ((MongoDocumentAdapter) value).rootDocument);
        } else {
            ((org.bson.Document) document).append(keys.get(keys.size() - 1), value);
        }
        return this;
    }

    @Override
    public Object get(String key) {
        Object value;
        if (!key.contains(".")) {
            value = rootDocument.get(key);
        } else {
            List<String> keys = Arrays.asList(key.split("\\."));
            value = rootDocument.getEmbedded(keys, Object.class);
        }

        if (value instanceof Document) {
            return new MongoDocumentAdapter((org.bson.Document) value);
        }
        return value;
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        if (Document.class.isAssignableFrom(clazz)) {
            if (!key.contains(".")) {
                return clazz.cast(new MongoDocumentAdapter(rootDocument.get(key, org.bson.Document.class)));
            }

            List<String> keys = Arrays.asList(key.split("\\."));
            return clazz.cast(new MongoDocumentAdapter(rootDocument.getEmbedded(keys, org.bson.Document.class)));
        }

        if (!key.contains(".")) {
            return rootDocument.get(key, clazz);
        }

        List<String> keys = Arrays.asList(key.split("\\."));
        return rootDocument.getEmbedded(keys, clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, T defaultValue) {
        if (defaultValue instanceof Document) {
            org.bson.Document value = get(key, org.bson.Document.class);
            return value == null ? defaultValue : (T) new MongoDocumentAdapter(value);
        }

        if (!key.contains(".")) {
            return rootDocument.get(key, defaultValue);
        }
        List<String> keys = Arrays.asList(key.split("\\."));
        return rootDocument.getEmbedded(keys, defaultValue);
    }

    @Override
    public Integer getInteger(String key) {
        return this.get(key, Integer.class);
    }

    @Override
    public Integer getInteger(String key, int defaultValue) {
        return this.get(key, defaultValue);
    }

    @Override
    public Long getLong(String key) {
        return this.get(key, Long.class);
    }

    @Override
    public Long getLong(String key, long defaultValue) {
        return this.get(key, defaultValue);
    }

    @Override
    public Double getDouble(String key) {
        return this.get(key, Double.class);
    }

    @Override
    public Double getDouble(String key, double defaultValue) {
        return this.get(key, defaultValue);
    }

    @Override
    public Boolean getBoolean(String key) {
        return this.get(key, Boolean.class);
    }

    @Override
    public Boolean getBoolean(String key, boolean defaultValue) {
        return this.get(key, defaultValue);
    }

    @Override
    public String getString(String key) {
        return this.get(key, String.class);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return this.get(key, defaultValue);
    }

    @Override
    public Date getDate(String key) {
        return this.get(key, Date.class);
    }

    @Override
    public Date getDate(String key, Date defaultValue) {
        return this.get(key, defaultValue);
    }

    public String toJson() {
        return rootDocument.toJson();
    }

    @Override
    public boolean isEmpty() {
        return rootDocument.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        if (!key.contains(".")) {
            return rootDocument.containsKey(key);
        }

        Object document = rootDocument;
        List<String> keys = Arrays.asList(key.split("\\."));
        for (int i = 0; i < keys.size() - 1; i++) {
            String segment = keys.get(i);
            document = ((org.bson.Document) document).get(segment);
            if (!(document instanceof org.bson.Document)) {
                if (document == null) {
                    return false;
                } else {
                    throw new ClassCastException(String.format("At key %s, the value is not a Document (%s)", key, document.getClass().getName()));
                }
            }
        }

        return ((org.bson.Document) document).containsKey(keys.get(keys.size() - 1));
    }


    @Override
    public Object remove(String key) {
        if (!key.contains(".")) {
            return rootDocument.remove(key);
        }

        List<String> keys = Arrays.asList(key.split("\\."));
        return remove(rootDocument, keys.listIterator());
    }

    private Object remove(org.bson.Document document, Iterator<String> keyIterator) {
        Object result = null;
        if (keyIterator.hasNext()) {
            String key = keyIterator.next();
            Object value = document.get(key);
            if (!(value instanceof org.bson.Document)) {
                if (value == null) {
                    return null;
                }

                if (keyIterator.hasNext()) {
                    throw new ClassCastException(String.format("At key %s, the value is not a Document (%s)", key, value.getClass().getName()));
                } else {
                    return document.remove(key);
                }
            }

            if (keyIterator.hasNext()) {
                result = remove((org.bson.Document) value, keyIterator);
            }

            if (((org.bson.Document) value).isEmpty()) {
                document.remove(key);
            }

        }
        return result;
    }

    @Override
    public void clear() {
        rootDocument.clear();
    }


    @Override
    public int hashCode() {
        return rootDocument.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj != null && this.getClass() == obj.getClass()) {
            MongoDocumentAdapter document = (MongoDocumentAdapter)obj;
            return rootDocument.equals(document.rootDocument);
        }

        return false;
    }

    @Override
    public String toString() {
        return rootDocument.toString();
    }
}
