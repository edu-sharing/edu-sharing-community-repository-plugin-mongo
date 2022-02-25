package org.edu_sharing.plugin_mongo.spring_config;

import org.edu_sharing.plugin_mongo.Initialization;
import org.edu_sharing.plugin_mongo.datamodel.MongoModelDictionary;
import org.edu_sharing.plugin_mongo.datamodel.MongoModelDictionaryImpl;
import org.edu_sharing.plugin_mongo.metadata.AlfrescoMappingService;
import org.edu_sharing.plugin_mongo.metadata.AlfrescoRuntimePropertyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetadataConfiguration {

    @Bean
    Initialization initialization(){
        return new Initialization();
    }

    @Bean
    public MongoModelDictionary mongoModelDictionary() {
        return new MongoModelDictionaryImpl();
    }

    @Bean
    public AlfrescoMappingService alfMapping(MongoModelDictionary modelDictionary){
        return new AlfrescoMappingService(modelDictionary);
    }
    @Bean
    public AlfrescoRuntimePropertyService alfrescoRuntimePropertyService(){
        return new AlfrescoRuntimePropertyService();
    }
}
