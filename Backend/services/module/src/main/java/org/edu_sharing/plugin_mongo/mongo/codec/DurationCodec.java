package org.edu_sharing.plugin_mongo.mongo.codec;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DurationCodec implements Codec<Duration> {
    @Override
    public void encode(BsonWriter bsonWriter, Duration duration, EncoderContext encoderContext) {
        if(duration != null) {
            bsonWriter.writeString(duration.toString());
        }
    }

    @Override
    public Duration decode(BsonReader bsonReader, DecoderContext decoderContext) {
        String durationString = bsonReader.readString();
        if(durationString != null) {
            try {
                return Duration.parse(durationString);
            } catch(DateTimeParseException e) {
                return Duration.ofMillis(Long.parseLong(durationString));
            }
        }
        return null;
    }

    @Override
    public Class<Duration> getEncoderClass() {
        return Duration.class;
    }
}