package org.edu_sharing.plugin_mongo.graphql.config;

import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.edu_sharing.plugin_mongo.graphql.scalars.ColorScalar;
import org.edu_sharing.plugin_mongo.graphql.scalars.DateScalar;
import org.edu_sharing.plugin_mongo.graphql.scalars.DurationScalar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScalarConfig {
    @Bean
    public GraphQLScalarType dateTime() {
        return DateScalar.INSTANCE;
    }

    @Bean
    public GraphQLScalarType color() {
        return ColorScalar.INSTANCE;
    }

    @Bean
    public GraphQLScalarType locale() {
        return  ExtendedScalars.Locale;
    }

    @Bean
    public GraphQLScalarType duration() {
        return DurationScalar.INSTANCE;
    }
}
