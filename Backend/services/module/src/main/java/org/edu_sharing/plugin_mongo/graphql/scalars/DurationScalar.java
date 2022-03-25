package org.edu_sharing.plugin_mongo.graphql.scalars;

import graphql.language.StringValue;
import graphql.scalars.util.Kit;
import graphql.schema.*;

import java.time.Duration;

public class DurationScalar {
    public static final GraphQLScalarType INSTANCE;

    static {
        Coercing<Duration, String> coercing = new Coercing<Duration, String>() {

            @Override
            public String serialize(Object input) throws CoercingSerializeException {
                Duration duration;
                if(input instanceof Duration) {
                    duration = (Duration)input;
                }else {
                    if (!(input instanceof String)) {
                        throw new CoercingSerializeException("Expected something we can convert to 'java.time.Duration' but was '" + Kit.typeName(input) + "'.");
                    }
                    duration = Duration.parse(input.toString());
                }
                return duration.toString();
            }

            @Override
            public Duration parseValue(Object input) throws CoercingParseValueException {
                Duration duration;
                if(input instanceof Duration){
                    duration = (Duration) input;
                }else{
                    if (!(input instanceof String)) {
                        throw new CoercingParseValueException("Expected a 'String' but was '" + Kit.typeName(input) + "'.");
                    }
                    duration = Duration.parse(input.toString());
                }
                return duration;
            }

            @Override
            public Duration parseLiteral(Object input) throws CoercingParseLiteralException {
                if(!(input instanceof StringValue)){
                    throw new CoercingParseLiteralException("Expected AST type 'StringValue' but was '" + Kit.typeName(input) + "'.");
                }
                return Duration.parse(input.toString());
            }
        };

        INSTANCE = GraphQLScalarType.newScalar().name("Duration").description("An String compliant Locale Scalar").coercing(coercing).build();
    }
}
