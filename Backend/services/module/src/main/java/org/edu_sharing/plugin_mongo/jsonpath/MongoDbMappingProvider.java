package org.edu_sharing.plugin_mongo.jsonpath;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.mapper.MappingException;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.Callable;

public class MongoDbMappingProvider implements MappingProvider {

    private static final Logger logger = LoggerFactory.getLogger(MongoDbMappingProvider.class);
    private final Callable<Gson> factory;

    public MongoDbMappingProvider(Callable<Gson> factory) {
        this.factory = factory;
    }

    public MongoDbMappingProvider() {
        super();
        try {
            Class.forName("com.google.gson.Gson");
            this.factory = Gson::new;
        } catch (ClassNotFoundException e) {
            logger.error("Gson not found on class path. No converters configured.");
            throw new JsonPathException("Gson not found on path", e);
        }
    }

    @Override
    public <T> T map(Object source, Class<T> targetType, Configuration configuration) {
        if(source == null){
            return null;
        }

        if(source instanceof Document){
            try {
                factory.call().getAdapter(targetType).fromJson(((Document)source).toJson());
            } catch (Exception e){
                throw new MappingException(e);
            }
        }
        return targetType != null ? targetType.cast(source) : (T) source;
    }

    @Override
    public <T> T map(Object source, TypeRef<T> targetType, Configuration configuration) {
        if(source == null){
            return null;
        }

        if(source instanceof Document){
            try {
                factory.call().getAdapter(TypeToken.get(targetType.getType())).fromJson(((Document)source).toJson());
            } catch (Exception e){
                throw new MappingException(e);
            }
        }
        return targetType != null ? ((Class<T>)((ParameterizedType)targetType.getType()).getRawType()).cast(source) : (T) source;
    }
}
