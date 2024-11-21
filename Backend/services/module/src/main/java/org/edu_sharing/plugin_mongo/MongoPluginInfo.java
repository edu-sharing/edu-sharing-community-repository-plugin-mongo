package org.edu_sharing.plugin_mongo;

import org.edu_sharing.restservices.about.v1.model.PluginInfo;
import org.springframework.stereotype.Component;

@Component
public class MongoPluginInfo implements PluginInfo {
    public String getId() {
        return "mongo-plugin";
    }
}
