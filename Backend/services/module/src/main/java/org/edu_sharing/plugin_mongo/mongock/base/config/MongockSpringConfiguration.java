package org.edu_sharing.plugin_mongo.mongock.base.config;

import io.mongock.api.config.MongockConfiguration;

public class MongockSpringConfiguration extends MongockConfiguration implements MongockSpringConfigurationBase {
    private boolean testEnabled = false;
    /**
     * Type of Spring bean Mongock should be: ApplicationRunner(default) or InitializingBean
     */
    private SpringRunnerType runnerType = SpringRunnerType.InitializingBean;

    public SpringRunnerType getRunnerType() {
        return runnerType;
    }

    public void setRunnerType(SpringRunnerType runnerType) {
        this.runnerType = runnerType;
    }

    public boolean isTestEnabled() {
        return testEnabled;
    }

    public void setTestEnabled(boolean testEnabled) {
        this.testEnabled = testEnabled;
    }

    public <T extends MongockSpringConfiguration> void updateFrom(T from) {
        super.updateFrom(from);
        testEnabled = from.isTestEnabled();
        runnerType = from.getRunnerType();
    }
}
