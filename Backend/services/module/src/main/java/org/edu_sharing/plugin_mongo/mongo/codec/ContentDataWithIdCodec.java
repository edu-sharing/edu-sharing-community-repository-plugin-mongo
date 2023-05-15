package org.edu_sharing.plugin_mongo.mongo.codec;

import org.alfresco.repo.domain.node.ContentDataWithId;
import org.apache.commons.lang3.NotImplementedException;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class ContentDataWithIdCodec implements Codec<ContentDataWithId> {
    @Override
    public ContentDataWithId decode(BsonReader bsonReader, DecoderContext decoderContext) {
        throw new NotImplementedException("ContentDataWithId can not be decoded");
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

