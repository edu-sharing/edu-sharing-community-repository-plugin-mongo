package org.edu_sharing.plugin_mongo.suggestion;

import lombok.Setter;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mongodb.MongoDatabaseFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
public class ObjectCodec implements Codec<Object>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private CodecRegistry getCodecRegistry(){
        return applicationContext.getBean(MongoDatabaseFactory.class).getCodecRegistry();
    }

    @Override
    public void encode(BsonWriter writer, Object value, EncoderContext encoderContext) {
        if (value == null) {
            writer.writeNull();
            return;
        }

        Class<?> clazz = value.getClass();
        if (clazz == String.class) {
            writer.writeString((String) value);
        } else if (clazz == Integer.class) {
            writer.writeInt32((Integer) value);
        } else if (clazz == Double.class) {
            writer.writeDouble((Double) value);
        } else if (clazz == Long.class) {
            writer.writeInt64((Long) value);
        } else if (clazz == Boolean.class) {
            writer.writeBoolean((Boolean) value);
        } else if (clazz == ObjectId.class) {
            writer.writeObjectId((ObjectId) value);
        } else if (clazz == Date.class) {
            writer.writeDateTime(((Date) value).getTime());
        }else if(clazz == BigInteger.class){
            writer.writeDecimal128(new Decimal128(new BigDecimal((BigInteger)value)));
        }else  if (List.class.isAssignableFrom(clazz)) {
            writer.writeStartArray();
            for (Object element : (List<?>) value) {
                encode(writer, element, encoderContext);
            }
            writer.writeEndArray();
        } else if (clazz.isArray()) {
            writer.writeStartArray();
            for (Object element : (Object[]) value) {
                encode(writer, element, encoderContext);
            }
            writer.writeEndArray();
        } else {
            encode(writer, value, encoderContext, value.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void encode(BsonWriter writer, Object value, EncoderContext encoderContext, Class<T> clazz){
        writer.writeStartDocument();
        writer.writeString("_type", clazz.getName());
        writer.writeName("value");
        getCodecRegistry().get(clazz).encode(writer, (T)value, encoderContext);
        writer.writeEndDocument();
    }

    @Override
    public Object decode(BsonReader reader, DecoderContext decoderContext) {
        BsonType bsonType = reader.getCurrentBsonType();
        switch (bsonType) {
            case NULL:
                reader.readNull();
                return null;
            case STRING:
                return reader.readString();
            case INT32:
                return reader.readInt32();
            case INT64:
                return reader.readInt64();
            case DOUBLE:
                return reader.readDouble();
            case BOOLEAN:
                return reader.readBoolean();
            case OBJECT_ID:
                return reader.readObjectId();
            case DATE_TIME:
                return new Date(reader.readDateTime());
            case DECIMAL128:
                return reader.readDecimal128();
            case DOCUMENT:
                reader.readStartDocument();
                String type = reader.readString("_type");
                try {
                    reader.readName();
                    Class<?> clazz = Class.forName(type);
                    return getCodecRegistry().get(clazz).decode(reader, decoderContext);
                } catch (ClassNotFoundException e) {
                    throw new CodecConfigurationException("Failed to decode Document or type: " + type, e);
                }
            case ARRAY:
                List<Object> list = new ArrayList<>();
                reader.readStartArray();
                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    list.add(decode(reader, decoderContext));
                }
                reader.readEndArray();
                return list;
            default:
                throw new CodecConfigurationException("Unsupported BSON type: " + bsonType);
        }
    }

    @Override
    public Class<Object> getEncoderClass() {
        return Object.class;
    }


}
