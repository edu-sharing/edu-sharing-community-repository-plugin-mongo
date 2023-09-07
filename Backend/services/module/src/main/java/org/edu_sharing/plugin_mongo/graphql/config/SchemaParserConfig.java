package org.edu_sharing.plugin_mongo.graphql.config;

import graphql.kickstart.tools.SchemaParserDictionary;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.plugin_mongo.graphql.annotation.GraphQLSchema;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;
import java.util.Set;

@Configuration
public class SchemaParserConfig implements EnvironmentAware {

    @Setter
    private Environment environment;
    @Bean
    public SchemaParserDictionary schemaParserDictionary() throws ClassNotFoundException {

        SchemaParserDictionary schemaParserDictionary = new SchemaParserDictionary();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false, environment);
        scanner.addIncludeFilter(new AnnotationTypeFilter(GraphQLSchema.class));

        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents("org.edu_sharing");
        for (BeanDefinition bd : candidateComponents) {
            if (bd instanceof AnnotatedBeanDefinition) {
                Map<String, Object> annotAttributeMap =  ((AnnotatedBeanDefinition) bd)
                        .getMetadata()
                        .getAnnotationAttributes(GraphQLSchema.class.getCanonicalName());
                String name = (String) annotAttributeMap.get("value");
                if(StringUtils.isBlank(name)){
                    schemaParserDictionary.add(Class.forName(bd.getBeanClassName()));
                }else {
                    schemaParserDictionary.add(name, Class.forName(bd.getBeanClassName()));
                }
            }
        }
        return schemaParserDictionary;
    }
}
