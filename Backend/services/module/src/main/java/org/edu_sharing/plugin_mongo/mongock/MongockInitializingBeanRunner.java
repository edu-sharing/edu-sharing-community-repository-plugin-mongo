package org.edu_sharing.plugin_mongo.mongock;

import io.mongock.runner.core.executor.MongockRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;

@RequiredArgsConstructor
public class MongockInitializingBeanRunner implements InitializingBean {

    private final MongockRunner runner;

    @Override
    public void afterPropertiesSet() {
        runner.execute();
    }
}
