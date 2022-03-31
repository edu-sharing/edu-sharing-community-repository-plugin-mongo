package org.edu_sharing.plugin_mongo.graphql.scalars;

import graphql.scalars.util.Kit;
import graphql.schema.*;

import java.util.Date;

public class DateScalar {
    public static final GraphQLScalarType INSTANCE;

    static {
        Coercing<Date, Long> coercing = new Coercing<Date, Long>() {

            @Override
            public Long serialize(Object input) throws CoercingSerializeException {
                Date date;
                if (input instanceof Date) {
                    date = (Date) input;
                } else if (input instanceof Long) {
                    date = new Date((Long) input);
                } else {
                    throw new CoercingSerializeException("Expected something we can convert to 'java.util.Date' but was '" + Kit.typeName(input) + "'.");
                }
                return date.getTime();
            }

            @Override
            public Date parseValue(Object input) throws CoercingParseValueException {
                Date date;
                if (input instanceof Date) {
                    date = (Date) input;
                } else if (input instanceof Long) {
                    date = new Date((Long) input);
                } else {
                    throw new CoercingSerializeException("Expected something we can convert to 'java.util.Date' but was '" + Kit.typeName(input) + "'.");
                }
                return date;
            }

            @Override
            public Date parseLiteral(Object input) throws CoercingParseLiteralException {
                Date date;
                if (input instanceof Date) {
                    date = (Date) input;
                } else if (input instanceof Long) {
                    date = new Date((Long) input);
                } else {
                    throw new CoercingSerializeException("Expected something we can convert to 'java.util.Date' but was '" + Kit.typeName(input) + "'.");
                }
                return date;
            }
        };
        INSTANCE = GraphQLScalarType.newScalar().name("Date").description("An Long compliant Locale Scalar").coercing(coercing).build();
    }
}