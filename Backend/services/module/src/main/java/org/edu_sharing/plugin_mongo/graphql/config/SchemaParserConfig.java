package org.edu_sharing.plugin_mongo.graphql.config;

import graphql.kickstart.tools.SchemaParserDictionary;
import org.edu_sharing.plugin_mongo.metadata.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchemaParserConfig {
    @Bean
    public SchemaParserDictionary schemaParserDictionary(){
        return new SchemaParserDictionary()
                .add(RemoteShadow.class)
                .add(Replication.class)
                ;
//                .add(BooleanRangedValue.class)
//                .add(StringRangedValue.class)
//                .add(FloatRangedValue.class)
//                .add(IntRangedValue.class)
//                .add(DateRangedValue.class);
    }
}
