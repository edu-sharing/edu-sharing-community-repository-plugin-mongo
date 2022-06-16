package org.edu_sharing.plugin_mongo.graphql.scalars;

import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.scalars.util.Kit;
import graphql.schema.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateScalar {
    public static final GraphQLScalarType INSTANCE;

    private final static String dateFormat = "dd-M-yyyy hh:mm:ss a";
    private final static SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

    static {
        Coercing<Date, Long> coercing = new Coercing<Date, Long>() {

            @Override
            public Long serialize(Object input) throws CoercingSerializeException {
                Date date;
                if (input instanceof Date) {
                    date = (Date) input;
                } else if (input instanceof Long) {
                    date = new Date((Long) input);
                } else if (input instanceof Integer) {
                    date = new Date((Integer) input);
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
                } else if (input instanceof Integer) {
                    date = new Date((Integer) input);
                } else {
                    throw new CoercingSerializeException("Expected something we can convert to 'java.util.Date' but was '" + Kit.typeName(input) + "'.");
                }
                return date;
            }

            @Override
            public Date parseLiteral(Object input) throws CoercingParseLiteralException {
                Date date;
                if (input instanceof StringValue) {
                    try {
                        date = formatter.parse(input.toString());
                    } catch (ParseException e) {
                        throw new CoercingSerializeException("Invalid Date format. Date format must be in form of: " + dateFormat);
                    }
                } else if (input instanceof IntValue) {
                    date = new Date(((IntValue) input).getValue().longValue());
                } else {
                    throw new CoercingSerializeException("Expected something we can convert to 'java.util.Date' but was '" + Kit.typeName(input) + "'.");
                }
                return date;
            }
        };
        INSTANCE = GraphQLScalarType.newScalar().name("Date").description("An Long compliant Locale Scalar").coercing(coercing).build();
    }
}