package org.edu_sharing.plugin_mongo.mongo;

import com.google.common.collect.Lists;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.edu_sharing.alfresco.lightbend.LightbendConfigLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.*;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;

@Configuration
public class MongoDbConfiguration {

    @Autowired MongoSettings mongoSettings;


    @Autowired(required = false) List<Convention> conventions;
    @Autowired( required = false) List<ClassModel<?>> classModels;
    @Bean
    public CodecProvider pojoCodecProvider() throws ClassNotFoundException {

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(BsonDiscriminator.class));
        List<Class<?>> discriminatorClasses = new ArrayList<>();
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("org.edu_sharing")) {
            Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
            discriminatorClasses.add(clazz);
        }


        List<Convention> allConventions = new ArrayList<>(Collections.singletonList(Conventions.ANNOTATION_CONVENTION));
        Optional.ofNullable(conventions).ifPresent(allConventions::addAll);

        PojoCodecProvider.Builder builder = PojoCodecProvider.builder()
                .register(discriminatorClasses.toArray(new Class[0]));

        Optional.ofNullable(classModels).ifPresent(classModels->builder.register(classModels.toArray(new ClassModel<?>[0])));

        // We need to add this because MongoDb will automatically convert id to _id
        builder.conventions(allConventions);
        builder.automatic(true);

        return builder.build();
    }

    @Autowired(required = false) List<Codec<?>> codecs;
    @Autowired(required = false) List<CodecProvider> codecProviders;

    @Bean
    public CodecRegistry codecRegistry(CodecProvider pojoCodecProvider) {
        ArrayList<CodecRegistry> codecRegistries = new ArrayList<>();

        Optional.ofNullable(codecs).ifPresent(codecs -> codecRegistries.add(CodecRegistries.fromCodecs(codecs)));
        codecRegistries.add(getDefaultCodecRegistry());

        Optional.ofNullable(codecProviders).ifPresent(providers -> codecRegistries.add(CodecRegistries.fromProviders(providers)));
        codecRegistries.add(CodecRegistries.fromProviders(pojoCodecProvider));

        return CodecRegistries.fromRegistries(codecRegistries);
    }

    @Bean
    public MongoClientSettings mongoClientSettings(CodecRegistry codecRegistry) {
        ConnectionString connectionString = new ConnectionString(mongoSettings.getConnectionString());
        return MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();
    }

    @Bean
    public MongoClient mongoClient(MongoClientSettings settings) {
        return MongoClients.create(settings);
    }


    @Bean
    public MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase(mongoSettings.getDatabase());
    }

}
