package edu_sharing;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.edu_sharing.plugin_mongo.mongo.MongoSettings;
import org.edu_sharing.plugin_mongo.service.legacy.AlfrescoMetadataService;
import org.edu_sharing.service.nodeservice.NodeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;


// We need to run this with junit4 because of the spring version 3.2.17
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        loader = AnnotationConfigContextLoader.class,
        locations = "/org/edu_sharing/spring/plugin-mongo-services.xml")
public class NewSuggestionGraphQLTest {

    @Configuration
    public static class TestConfig {

        @Bean
        @Primary
        public MongoSettings mongoSettings() {
            MongoSettings mongoSettings = new MongoSettings();
            mongoSettings.setConnectionString("mongodb://repository:repository@repository.127.0.0.1.nip.io:8500/edu-sharing");
            mongoSettings.setDatabase("edu-sharing");
            return mongoSettings;
        }

        @Bean
        @Primary
        public NodeService nodeService(){
            return Mockito.mock(NodeService.class);
        }

        @Bean
        @Primary
        public AlfrescoMetadataService alfrescoMetadataService(){
            return Mockito.mock(AlfrescoMetadataService.class);
        }
    }

    private static final String GRAPHQL_QUERY_REQUEST_PATH = "graphql/resolver/query/request/%s.graphql";

    @Test
    public void Test() throws IOException {
    }
}
