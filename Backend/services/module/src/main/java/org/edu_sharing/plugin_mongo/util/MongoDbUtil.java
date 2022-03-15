package org.edu_sharing.plugin_mongo.util;

public final class MongoDbUtil {
    public static String ConcatFields(String... fields){
        return String.join(".", fields);
    }
}
