package org.edu_sharing.plugin_mongo.mongo.codec;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.Decimal128;
import org.edu_sharing.plugin_mongo.metadata.*;
import org.edu_sharing.plugin_mongo.mongo.MongoTypeMappingException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

//@Component
//public class RangedValueCodec implements Codec<RangedValue> {
//
//    @Override
//    public RangedValue<?> decode(BsonReader reader, DecoderContext decoderContext) {
//
//        if (reader.getCurrentBsonType() == BsonType.NULL) {
//            return null;
//        }
//
//        RangedValue<?> result = null;
//        String id = null;
//        reader.readStartDocument();
//        while (true) {
//            BsonType bsonType = reader.readBsonType();
//            if (bsonType == BsonType.END_OF_DOCUMENT) {
//                reader.readEndDocument();
//                break;
//            }
//
//            String name = reader.readName();
//            if (Objects.equals(name, "id")) {
//                id = reader.readString();
//            } else if (Objects.equals(name, "value")) {
//                switch (bsonType) {
//                    case DOUBLE:
//                        result = new FloatRangedValue(id, reader.readDouble());
//                        break;
//                    case REGULAR_EXPRESSION:
//                    case STRING:
//                        result = new StringRangedValue(id, reader.readString());
//                        break;
//                    case BOOLEAN:
//                        result = new BooleanRangedValue(id, reader.readBoolean());
//                        break;
//                    case DATE_TIME:
//                        result = new DateRangedValue(id, new Date(reader.readDateTime()));
//                        break;
//                    case SYMBOL:
//                        result = new StringRangedValue(id, reader.readSymbol());
//                        break;
//                    case INT32:
//                        result = new IntRangedValue(id, (long) reader.readInt32());
//                        break;
//                    case TIMESTAMP:
//                        result = new DateRangedValue(id, new Date(reader.readTimestamp().getValue()));
//                        break;
//                    case INT64:
//                        result = new IntRangedValue(id, reader.readInt64());
//                        break;
//                    default:
//                        throw new MongoTypeMappingException(String.format("RangeValue.value can't be of type: %s", bsonType.name()));
//                }
//            }
//        }
//
//        if (Objects.nonNull(result)) {
//            result.setId(id);
//        }
//
//        return result;
//    }
//
//    @Override
//    public void encode(BsonWriter writer, RangedValue rangedValue, EncoderContext encoderContext) {
//
//        String id = rangedValue.getId();
//        Object value = rangedValue.getValue();
//        if (!Objects.nonNull(id) && !Objects.nonNull(value)) {
//            writer.writeNull();
//            return;
//        }
//
//        writer.writeStartDocument();
//        if (Objects.nonNull(id)) {
//            writer.writeName("id");
//            writer.writeString(id);
//        }
//
//        if (Objects.nonNull(value)) {
//            writer.writeName("value");
//
//            if (value instanceof String) {
//                writer.writeString((String) value);
//            } else if (value instanceof Integer) {
//                writer.writeInt32((Integer) value);
//            } else if (value instanceof Long) {
//                writer.writeInt64((Long) value);
//            } else if (value instanceof Boolean) {
//                writer.writeBoolean((Boolean) value);
//            } else if (value instanceof Decimal128) {
//                writer.writeDecimal128((Decimal128) value);
//            } else if (value instanceof Float) {
//                writer.writeDouble((Float) value);
//            } else if (value instanceof Double) {
//                writer.writeDouble((Double) value);
//            } else {
//                throw new MongoTypeMappingException(String.format("RangeValue.value can't be of type: %s", value.getClass()));
//            }
//        }
//        writer.writeEndDocument();
//    }
//
//    @Override
//    public Class<RangedValue> getEncoderClass() {
//        return RangedValue.class;
//    }
//}
