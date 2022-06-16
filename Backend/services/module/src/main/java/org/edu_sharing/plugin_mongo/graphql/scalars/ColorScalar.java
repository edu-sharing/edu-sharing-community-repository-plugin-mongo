package org.edu_sharing.plugin_mongo.graphql.scalars;

import graphql.language.StringValue;
import graphql.scalars.datetime.DateTimeScalar;
import graphql.scalars.util.Kit;
import graphql.schema.*;

import javax.management.StringValueExp;
import java.awt.*;

public class ColorScalar {
    public static final GraphQLScalarType INSTANCE;

    static {
        Coercing<Color, String> coercing = new Coercing<Color, String>() {
            @Override
            public String serialize(Object input) throws CoercingSerializeException {
                Color color;
                if(input instanceof Color) {
                    color = (Color)input;
                }else {
                    if (!(input instanceof String)) {
                        throw new CoercingSerializeException("Expected something we can convert to 'java.awt.Color' but was '" + Kit.typeName(input) + "'.");
                    }

                    color = Color.decode(input.toString());
                }
                return String.format("#%s",Integer.toHexString(color.getRGB()).substring(2).toUpperCase());
            }

            @Override
            public Color parseValue(Object input) throws CoercingParseValueException {
                Color color;
                if(input instanceof Color){
                    color = (Color) input;
                }else{
                    if (!(input instanceof String)) {
                        throw new CoercingParseValueException("Expected a 'String' but was '" + Kit.typeName(input) + "'.");
                    }
                    color = Color.decode(input.toString());
                }
                return color;
            }

            @Override
            public Color parseLiteral(Object input) throws CoercingParseLiteralException {
                if(!(input instanceof StringValue)){
                    throw new CoercingParseLiteralException("Expected AST type 'StringValue' but was '" + Kit.typeName(input) + "'.");
                }
                return Color.decode(((StringValue) input).getValue());
            }
        };
        INSTANCE = GraphQLScalarType.newScalar().name("Color").description("An Hex String compliant Color Scalar").coercing(coercing).build();
    }
}
