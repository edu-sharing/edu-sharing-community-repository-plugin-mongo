package org.edu_sharing.plugin_mongo.mongo.util;

import lombok.NonNull;
import org.bson.*;
import org.bson.codecs.Decoder;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

public final class MongoSerializationUtil {

    /**
     * A helper to convert an object to a Document
     * @param obj the object to covert
     * @param codecRegistry the codecRegistry that can be used in the conversion of the Object
     * @return a Document
     */
    public static <T> Document toDocument(@NotNull @NonNull T obj, @NotNull @NonNull CodecRegistry codecRegistry) {
        BsonDocument bsonDocument = BsonDocumentWrapper.asBsonDocument(obj, codecRegistry);
        BsonReader reader = new BsonDocumentReader(bsonDocument);
        Decoder<Document> encoder = codecRegistry.get(Document.class);
        return encoder.decode(reader, DecoderContext.builder().build());
    }
    /**
     * A helper to convert an object to a BsonDocument
     * @param obj the object to covert
     * @param codecRegistry the codecRegistry that can be used in the conversion of the Object
     * @return a BsonDocument
     */
    public static <T> BsonDocument toBsonDocument(@NotNull @NonNull T obj, @NotNull @NonNull CodecRegistry codecRegistry) {
        return BsonDocumentWrapper.asBsonDocument(obj, codecRegistry);
    }

    /**
     * A helper to convert a Bson to an Object of type T
     * @param document the document to covert
     * @param codecRegistry the codecRegistry that can be used in the conversion of the Bson
     * @param clazz the class to convert to
     * @return an Object of type T
     */
    public static <T> T toObject(@NotNull @NonNull Bson document, @NotNull @NonNull CodecRegistry codecRegistry, @NotNull @NonNull Class<T> clazz) {
        return toObject(document.toBsonDocument(), codecRegistry, clazz);
    }

    /**
     * A helper to convert an BsonDocument to an Object of type T
     * @param document the document to covert
     * @param codecRegistry the codecRegistry that can be used in the conversion of the BsonDocument
     * @param clazz the class to convert to
     * @return an Object of type T
     */
    public static <T> T toObject(@NotNull @NonNull BsonDocument document, @NotNull @NonNull CodecRegistry codecRegistry, @NotNull @NonNull Class<T> clazz) {
        BsonReader reader = new BsonDocumentReader(document);
        Decoder<T> encoder = codecRegistry.get(clazz);
        return encoder.decode(reader, DecoderContext.builder().build());
    }
}
