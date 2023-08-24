package org.edu_sharing.plugin_mongo.mongo.codec;

import lombok.RequiredArgsConstructor;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.service.cmr.repository.ContentData;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.edu_sharing.plugin_mongo.mongo.MongoInvalidTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

@Component
public class ContentDataWithIdCodec implements Codec<ContentDataWithId> {
    private final Codec<Locale> localeCodec;

    @Autowired
    public ContentDataWithIdCodec(LocaleCodec localeCodec) {
        this.localeCodec = localeCodec;
    }

    @Override
    public ContentDataWithId decode(BsonReader reader, DecoderContext decoderContext) {
        if (Objects.requireNonNull(reader.getCurrentBsonType()) == BsonType.DOCUMENT) {
            reader.readStartDocument();
            String contentUrl = "", mimetype = "", encoding = "";
            long size = 0;
            while (true) {
                BsonType bsonType = reader.readBsonType();
                if (bsonType == BsonType.END_OF_DOCUMENT) {
                    reader.readEndDocument();
                    break;
                }
                String name = reader.readName();
                if (bsonType == BsonType.STRING) {
                    switch (name) {
                        case "contentUrl":
                            contentUrl = reader.readString();
                            break;
                        case "mimetype":
                            mimetype = reader.readString();
                            break;
                        case "encoding":
                            encoding = reader.readString();
                            break;
                    }
                } else if(bsonType == BsonType.INT64) {
                     if (name.equals("size")) {
                        size = reader.readInt64();
                    }
                }
                else if(bsonType == BsonType.DOCUMENT) {
                    if (name.equals("locale")) {
                        this.localeCodec.decode(reader, decoderContext);
                    }
                }
            }
            return new ContentDataWithId(new ContentData(contentUrl, mimetype, size, encoding), null);
        }
        throw new MongoInvalidTypeException(String.format("%s can not create %s from type %s", reader.getCurrentName(), getClass().getName(), reader.getCurrentBsonType()));
    }
    @Override
    public void encode(BsonWriter bsonWriter, ContentDataWithId contentDataWithId, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("contentUrl", contentDataWithId.getContentUrl());
        bsonWriter.writeString("mimetype", contentDataWithId.getMimetype());
        bsonWriter.writeString("encoding", contentDataWithId.getEncoding());
        bsonWriter.writeString("contentUrl", contentDataWithId.getContentUrl());
        bsonWriter.writeInt64("size", contentDataWithId.getSize());
        bsonWriter.writeName("locale");
        this.localeCodec.encode(bsonWriter, contentDataWithId.getLocale(), encoderContext);
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<ContentDataWithId> getEncoderClass() {
        return ContentDataWithId.class;
    }
}

