package org.edu_sharing.plugin_mongo.mongock.base.config;

import io.mongock.api.config.MongockConfiguration;
import io.mongock.driver.api.driver.ConnectionDriver;
import org.edu_sharing.plugin_mongo.mongock.MongockInitializingBeanRunner;
import org.edu_sharing.plugin_mongo.mongock.base.builder.SpringApplicationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

public abstract class MongockContextBase<CONFIG extends MongockConfiguration> {

    @Bean
    public MongockInitializingBeanRunner initializingBeanRunner(ConnectionDriver connectionDriver,
                                                                CONFIG springConfiguration,
                                                                ApplicationContext springContext,
                                                                ApplicationEventPublisher applicationEventPublisher) {
        return getBuilder(connectionDriver, springConfiguration, springContext, applicationEventPublisher)
                .buildInitializingBeanRunner();
    }

    @SuppressWarnings("all")
    public abstract SpringApplicationBean getBuilder(ConnectionDriver connectionDriver,
                                                     CONFIG springConfiguration,
                                                     ApplicationContext springContext,
                                                     ApplicationEventPublisher applicationEventPublisher);
}
