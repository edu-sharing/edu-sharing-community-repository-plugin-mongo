package org.edu_sharing.plugin_mongo.mongock.base.config;

public interface MongockSpringConfigurationBase {

    SpringRunnerType getRunnerType();

    void setRunnerType(SpringRunnerType runnerType);

    boolean isTestEnabled();

    void setTestEnabled(boolean testEnabled);
}

