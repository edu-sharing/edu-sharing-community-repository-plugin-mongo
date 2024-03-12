package org.edu_sharing.plugin_mongo.jsonpath;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.AbstractJsonProvider;
import org.apache.commons.io.IOUtils;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MongoDbDocumentProvider extends AbstractJsonProvider {

    @Override
    public Object parse(String s) throws InvalidJsonException {
        return Document.parse(s);
    }

    @Override
    public Object parse(InputStream inputStream, String s) throws InvalidJsonException {
        try{
            return Document.parse(IOUtils.toString(inputStream, s));
        } catch (IOException e) {
            throw new JsonPathException(e);
        }
    }

    @Override
    public String toJson(Object o) {
        return toDocument(o).toJson();
    }

    @Override
    public Object createArray() {
        return new ArrayList<>();
    }

    @Override
    public Object createMap() {
        return new Document();
    }

    private Document toDocument(final Object obj) {
        return (Document) obj;
    }
}
