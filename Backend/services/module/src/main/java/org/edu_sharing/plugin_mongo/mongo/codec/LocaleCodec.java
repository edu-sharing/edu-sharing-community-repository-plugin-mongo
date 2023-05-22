package org.edu_sharing.plugin_mongo.mongo.codec;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.edu_sharing.plugin_mongo.mongo.MongoInvalidTypeException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

@Component
public class LocaleCodec implements Codec<Locale> {
    @Override
    public Locale decode(BsonReader reader, DecoderContext decoderContext) {
        if (Objects.requireNonNull(reader.getCurrentBsonType()) == BsonType.DOCUMENT) {
            reader.readStartDocument();
            String language = "", country = "", variant = "";
            while (true) {
                BsonType bsonType = reader.readBsonType();
                if (bsonType == BsonType.END_OF_DOCUMENT) {
                    reader.readEndDocument();
                    break;
                }
                String name = reader.readName();
                if (bsonType == BsonType.STRING) {
                    switch (name) {
                        case "language":
                            language = reader.readString();
                            break;
                        case "country":
                            country = reader.readString();
                            break;
                        case "variant":
                            variant = reader.readString();
                            break;
                    }
                }
            }
            return new Locale(language, country, variant);
        }
        throw new MongoInvalidTypeException(String.format("%s can not create %s from type %s", reader.getCurrentName(), getClass().getName(), reader.getCurrentBsonType()));
    }
    @Override
    public void encode(BsonWriter bsonWriter, Locale locale, EncoderContext encoderContext) {
        // @TODO
    }

    @Override
    public Class<Locale> getEncoderClass() {
        return Locale.class;
    }
}

