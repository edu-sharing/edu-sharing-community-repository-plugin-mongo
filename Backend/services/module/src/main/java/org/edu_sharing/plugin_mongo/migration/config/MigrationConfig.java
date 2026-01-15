package org.edu_sharing.plugin_mongo.migration.config;

import com.mongodb.ReadConcernLevel;
import io.mongock.api.config.MongockConfiguration;
import io.mongock.driver.api.driver.ConnectionDriver;
import org.edu_sharing.plugin_mongo.mongock.MongockSpringFramework;
import org.edu_sharing.plugin_mongo.mongock.RunnerSpringFrameworkBuilder;
import org.edu_sharing.plugin_mongo.mongock.base.config.MongockContextBase;
import org.edu_sharing.plugin_mongo.mongock.base.config.MongockSpringConfiguration;
import org.edu_sharing.plugin_mongo.mongock.base.config.SpringRunnerType;
import org.edu_sharing.plugin_mongo.mongock.driver.springdata.v4.config.MongoDBConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MigrationConfig extends MongockContextBase<MongockConfiguration> {

    @Bean
    public MongockSpringConfiguration mongockSpringConfiguration() {
        MongockSpringConfiguration mongockSpringConfiguration = new MongockSpringConfiguration();
        mongockSpringConfiguration.setRunnerType(SpringRunnerType.InitializingBean);
        mongockSpringConfiguration.setMigrationScanPackage(List.of("org.edu_sharing.plugin_mongo.migration"));
        return mongockSpringConfiguration;
    }

    @Bean
    public MongoDBConfiguration mongoDBConfiguration() {
        MongoDBConfiguration mongoDBConfiguration = new MongoDBConfiguration();
        mongoDBConfiguration.setWriteConcern(MongoDBConfiguration.WriteConcernLevel.MAJORITY_WITH_JOURNAL);
        mongoDBConfiguration.setReadConcern(ReadConcernLevel.MAJORITY);
        mongoDBConfiguration.setReadPreference(MongoDBConfiguration.ReadPreferenceLevel.PRIMARY);
        return mongoDBConfiguration;
    }

    public RunnerSpringFrameworkBuilder getBuilder(
            ConnectionDriver connectionDriver,
            MongockConfiguration mongockConfiguration,
            ApplicationContext applicationContext,
            ApplicationEventPublisher applicationEventPublisher) {
        return MongockSpringFramework.builder()
                .setDriver(connectionDriver)
                .setConfig(mongockConfiguration)
                .setSpringContext(applicationContext)
                .setEventPublisher(applicationEventPublisher);
    }
}
