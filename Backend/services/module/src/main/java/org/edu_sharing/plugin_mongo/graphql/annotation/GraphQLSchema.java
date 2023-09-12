package org.edu_sharing.plugin_mongo.graphql.annotation;


import java.lang.annotation.*;

/**
 * Registers a type for GraphQL schema definition.
 * Mainly used for polymorphic types like a GraphQL type that implements an interface
 * Or if classname and model name diverges
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GraphQLSchema {

    /**
     * The GraphQL type name use for
     * If the value is empty the name is resolved by class name
     */
    String value() default "";
}
