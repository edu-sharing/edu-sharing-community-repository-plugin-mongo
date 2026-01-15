package org.edu_sharing.plugin_mongo.mongock.base.builder.migration;

import io.mongock.api.config.MongockConfiguration;
import io.mongock.driver.api.driver.ChangeSetDependency;
import io.mongock.runner.core.builder.RunnerBuilder;
import org.edu_sharing.plugin_mongo.mongock.base.builder.SpringApplicationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

//TODO javadoc
public interface RunnerSpringFrameworkBuilderBase<
        SELF extends RunnerSpringFrameworkBuilderBase<SELF, CONFIG>,
        CONFIG extends MongockConfiguration>
        extends
        SpringApplicationBean,
        RunnerBuilder<SELF, CONFIG> {

    //TODO javadoc
    SELF setSpringContext(ApplicationContext springContext);

    //TODO javadoc
    SELF setEventPublisher(ApplicationEventPublisher applicationEventPublisher);

    @Override
    default SELF addDependency(String name, Class<?> type, Object instance) {
        getDependencyManager().addDriverDependency(new ChangeSetDependency(name, type, instance));
        return getInstance();
    }
}
