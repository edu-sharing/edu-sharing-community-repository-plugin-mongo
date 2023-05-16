package org.edu_sharing.plugin_mongo.mongo.codec;

import org.alfresco.repo.domain.node.ContentDataWithId;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.BsonType;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.edu_sharing.plugin_mongo.mongo.MongoInvalidTypeException;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ContentDataWithIdCodec implements Codec<ContentDataWithId> {
    @Override
    public ContentDataWithId decode(BsonReader reader, DecoderContext decoderContext) {
        switch (reader.getCurrentBsonType()) {
            case STRING:
                return null;
            default:
                throw new MongoInvalidTypeException(String.format("%s can not create ContentDataWithId from type %s", reader.getCurrentName(), reader.getCurrentBsonType()));

        }
    }
    @Override
    public void encode(BsonWriter bsonWriter, ContentDataWithId contentDataWithId, EncoderContext encoderContext) {
        bsonWriter.writeString(contentDataWithId.toString());
    }

    @Override
    public Class<ContentDataWithId> getEncoderClass() {
        return ContentDataWithId.class;
    }
}

