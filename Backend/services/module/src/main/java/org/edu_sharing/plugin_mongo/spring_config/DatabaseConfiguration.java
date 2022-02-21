package org.edu_sharing.plugin_mongo.spring_config;

import org.edu_sharing.plugin_mongo.mongo.MongoClientFactoryBean;
import org.edu_sharing.plugin_mongo.mongo.MongoDatabaseFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfiguration {

    @Bean(name = "mongoClient")
    public MongoClientFactoryBean mongoClientFactoryBean() {
        return new MongoClientFactoryBean();
    }

    @Bean(name = "eduMongoDb")
    public MongoDatabaseFactoryBean mongoDatabaseFactoryBean() {
        return new MongoDatabaseFactoryBean(mongoClientFactoryBean().getObject());
    }

}
