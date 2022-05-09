package org.edu_sharing;

import graphql.kickstart.tools.GraphQLMutationResolver;
import org.edu_sharing.graphql.tools.GraphQLJavaToolsAutoConfiguation;
import org.edu_sharing.plugin_mongo.graphql.config.ScalarConfig;
import org.edu_sharing.plugin_mongo.graphql.config.SchemaParserConfig;
import org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query.*;
import org.edu_sharing.plugin_mongo.graphql.resolver.suggestion.mutation.SuggestionMutation;
import org.edu_sharing.plugin_mongo.mongo.MongoDbConfiguration;
import org.edu_sharing.plugin_mongo.mongo.MongoSettings;
import org.edu_sharing.plugin_mongo.mongo.codec.DateCodec;
import org.edu_sharing.plugin_mongo.mongo.codec.DurationCodec;
import org.edu_sharing.plugin_mongo.mongo.config.ClassModelConfig;
import org.edu_sharing.plugin_mongo.mongo.convention.ClassConvention;
import org.edu_sharing.plugin_mongo.repository.SuggestionRepository;
import org.edu_sharing.service.nodeservice.NodeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        loader = AnnotationConfigContextLoader.class,
        classes = {
                SuggestionGraphQLTest.TestConfig.class,
                MongoDbConfiguration.class,
                DateCodec.class, DurationCodec.class, //RangedValueCodec.class,
                ClassConvention.class,
                ClassModelConfig.class,

                GraphQLJavaToolsAutoConfiguation.class,
                ScalarConfig.class,
                SchemaParserConfig.class,

                SuggestionRepository.class,

                AssociationResolver.class,
                CollectionResolver.class,
                InfoResolverResolver.class,
                MetadataResolver.class,
                MetadataQueryResolver.class,
                ReferenceResolver.class,
                SuggestionMutation.class,
        }
)
public class SuggestionGraphQLTest {

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
    }


    @Test
    public void test(){

    }
}
