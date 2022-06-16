package org.edu_sharing.plugin_mongo.mongo.codec;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.edu_sharing.plugin_mongo.mongo.MongoInvalidTypeException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class DateCodec implements Codec<Date> {

    @Override
    public Date decode(BsonReader reader, DecoderContext decoderContext) {
        switch (reader.getCurrentBsonType()) {
            case STRING:
                return Date.from(LocalDateTime.parse(reader.readString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
            case INT64:
                return new Date(reader.readInt64());

            case INT32:
                return new Date(reader.readInt32());

            case DATE_TIME:
                return new Date(reader.readDateTime());

            default:
                throw new MongoInvalidTypeException(String.format("%s can not create date from type %s",reader.getCurrentName(), reader.getCurrentBsonType()));
        }
    }

    @Override
    public void encode(BsonWriter writer, Date value, EncoderContext encoderContext) {
        writer.writeDateTime(value.getTime());
    }

    @Override
    public Class<Date> getEncoderClass() {
        return Date.class;
    }
}
