package org.edu_sharing.plugin_mongo.mongock;

import io.mongock.api.config.MongockConfiguration;
import io.mongock.runner.core.executor.ExecutorBuilder;
import io.mongock.runner.core.executor.ExecutorBuilderDefault;
import io.mongock.runner.core.executor.changelog.ChangeLogService;
import io.mongock.runner.core.executor.changelog.ChangeLogServiceBase;
import org.edu_sharing.plugin_mongo.mongock.base.builder.SpringFrameworkBuilderBase;

import static io.mongock.runner.core.builder.BuilderType.COMMUNITY;

public class MongockSpringFramework {
    //TODO javadoc
    public static RunnerSpringFrameworkBuilder builder() {
        return new RunnerSpringbootBuilderImpl(new ExecutorBuilderDefault(), new ChangeLogService(), new MongockConfiguration());
    }

    public static class RunnerSpringbootBuilderImpl extends SpringFrameworkBuilderBase<RunnerSpringbootBuilderImpl, MongockConfiguration>
            implements RunnerSpringFrameworkBuilder {

        RunnerSpringbootBuilderImpl(ExecutorBuilder<MongockConfiguration> executorFactory, ChangeLogServiceBase changeLogService, MongockConfiguration config) {
            super(COMMUNITY, executorFactory, changeLogService, config);
        }

        @Override
        public RunnerSpringbootBuilderImpl getInstance() {
            return this;
        }
    }
}

