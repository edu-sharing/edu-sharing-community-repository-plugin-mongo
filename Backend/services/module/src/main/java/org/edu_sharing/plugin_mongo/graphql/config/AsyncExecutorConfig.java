package org.edu_sharing.plugin_mongo.graphql.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.edu_sharing.graphql.util.GraphQLExecutorFactory;


import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AsyncExecutorConfig {

    @Bean
    public Executor metadataExecutor(GraphQLExecutorFactory executorFactory) {
        return executorFactory.newExecutor();
    }

}
