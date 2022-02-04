package org.edu_sharing.plugin_mongo;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.edu_sharing.plugin_mongo.jsonpath.MongoDbDocumentProvider;
import org.edu_sharing.plugin_mongo.jsonpath.MongoDbMappingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.EnumSet;
import java.util.Set;

public class Initialization implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(Initialization.class);

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
