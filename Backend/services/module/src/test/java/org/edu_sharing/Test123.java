//package org.edu_sharing;
//
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.client.model.Filters;
//import org.bson.BsonDocument;
//import org.bson.BsonDocumentWrapper;
//import org.bson.BsonDocumentWriter;
//import org.bson.codecs.configuration.CodecRegistries;
//import org.bson.codecs.configuration.CodecRegistry;
//import org.bson.codecs.pojo.PojoCodecProvider;
//import org.bson.internal.ProvidersCodecRegistry;
//import org.edu_sharing.plugin_mongo.metadata.Metadata;
//import org.edu_sharing.plugin_mongo.metadata.lom.General;
//import org.edu_sharing.plugin_mongo.metadata.lom.Lom;
//import org.edu_sharing.plugin_mongo.mongo.MongoDbConfiguration;
//import org.edu_sharing.plugin_mongo.mongo.MongoSettings;
//
//import org.edu_sharing.plugin_mongo.mongo.codec.DateCodec;
//import org.edu_sharing.plugin_mongo.mongo.codec.DurationCodec;
////import org.edu_sharing.plugin_mongo.mongo.codec.RangedValueCodec;
//import org.edu_sharing.plugin_mongo.mongo.config.ClassModelConfig;
//import org.edu_sharing.plugin_mongo.mongo.convention.ClassConvention;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.support.AnnotationConfigContextLoader;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(
//        loader = AnnotationConfigContextLoader.class,
//        classes = {
//                Test123.TestConfig.class,
//                MongoDbConfiguration.class,
//                DateCodec.class, DurationCodec.class, //RangedValueCodec.class,
//                ClassConvention.class,
//                ClassModelConfig.class
//        })
//public class Test123 {
//
//    @Configuration
//    public static class TestConfig {
//        @Bean
//        @Primary
//        public MongoSettings mongoSettings() {
//            MongoSettings mongoSettings = new MongoSettings();
//            mongoSettings.setConnectionString("mongodb://repository:repository@repository.127.0.0.1.nip.io:8500/edu-sharing");
//            mongoSettings.setDatabase("edu-sharing");
//            return mongoSettings;
//        }
//    }
//
//    @Autowired
//    private MongoDatabase mongoDatabase;
//    @Autowired
//    private CodecRegistry codecRegistry;
//
//    //@Test
//    public void mongoTest() {
//        //BsonDocument update = new BsonDocument().toBsonDocument(Lom.class, codecRegistry);
//        //new BsonDocumentWriter(new BsonDocument());
//        //codecRegistry.get(ProvidersCodecRegistry.class);
//        BsonDocument update = BsonDocumentWrapper.asBsonDocument(Lom.builder().general(General.builder().title("165156").build()).build(), codecRegistry);
//
//        Metadata metadata = mongoDatabase.getCollection("workspace", Metadata.class)
//                .find(Filters.eq("57cfb7fb-5d45-4ca4-944a-f5092dc6b5d0"))
//                .first();
//
//    }
//}
