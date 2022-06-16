package org.edu_sharing.plugin_mongo.jsonpath;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class JsonPathStartupListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(JsonPathStartupListener.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Setup JsonPath default configuration for Mongodb Documents");
        Configuration.setDefaults(new Configuration.Defaults() {
            @Override
            public JsonProvider jsonProvider() {
                return new MongoDbDocumentProvider();
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }

            @Override
            public MappingProvider mappingProvider() {
                return new MongoDbMappingProvider();
            }
        });
    }
}
