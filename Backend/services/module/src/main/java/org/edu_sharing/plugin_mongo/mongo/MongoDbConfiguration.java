package org.edu_sharing.plugin_mongo.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.lang.Nullable;

import java.util.*;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;

@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired(required = false))
@EnableMongoRepositories(basePackages = "org.edu_sharing.plugin_mongo.repository")
public class MongoDbConfiguration extends AbstractMongoClientConfiguration {

    @NonNull final MongoSettings mongoSettings;
    @Nullable final List<Convention> conventions;
    @Nullable final List<ClassModel<?>> classModels;
    @Nullable final List<Codec<?>> codecs;
    @Nullable final List<CodecProvider> codecProviders;

    @Override
    protected String getDatabaseName() {
        return mongoSettings.getDatabase();
    }

    //@Bean
    //@DependsOn({"mongoDbConfiguration"})
    public PojoCodecProvider pojoCodecProvider() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(BsonDiscriminator.class));
        List<Class<?>> discriminatorClasses = new ArrayList<>();
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("org.edu_sharing")) {
            try {
                Class<?>  clazz = Class.forName(beanDefinition.getBeanClassName());
                discriminatorClasses.add(clazz);
            } catch (ClassNotFoundException e) {
                log.error(e.toString());
            }
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


    //@Bean
    //@DependsOn({"mongoDbConfiguration"})
    public CodecRegistry codecRegistry(PojoCodecProvider pojoCodecProvider) {
        ArrayList<CodecRegistry> codecRegistries = new ArrayList<>();

        Optional.ofNullable(codecs).ifPresent(c -> codecRegistries.add(CodecRegistries.fromCodecs(c)));
        codecRegistries.add(getDefaultCodecRegistry());

        Optional.ofNullable(codecProviders).ifPresent(providers -> codecRegistries.add(CodecRegistries.fromProviders(providers)));
        codecRegistries.add(CodecRegistries.fromProviders(pojoCodecProvider));

        return CodecRegistries.fromRegistries(codecRegistries);
    }

    @Override
    public void configureClientSettings(MongoClientSettings.Builder builder) {
        ConnectionString connectionString = new ConnectionString(mongoSettings.getConnectionString());
        builder.applyConnectionString(connectionString)
                .codecRegistry(codecRegistry(pojoCodecProvider()));
    }

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory){
        return new MongoTransactionManager(dbFactory);
    }
}
