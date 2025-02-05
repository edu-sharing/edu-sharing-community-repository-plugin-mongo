package org.edu_sharing.plugin_mongo.mongo.config;

import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.bson.BsonType;
import org.bson.codecs.pojo.*;
import org.edu_sharing.plugin_mongo.mongo.util.AutomatedInstanceCreator;
import org.edu_sharing.plugin_mongo.mongo.util.BufferedInstanceCreator;
import org.edu_sharing.service.qa.domain.QANode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class ClassModelConfig {

    @Bean
    public ClassModel<ContentDataWithId> contentDataWithIdClassModel() {
        return ClassModel.builder(ContentDataWithId.class).instanceCreatorFactory(() -> new BufferedInstanceCreator<ContentDataWithId>() {
            @Override
            public ContentDataWithId getInstance() {
                return new ContentDataWithId(
                        new ContentData(
                                getValue("contentUrl"),
                                getValue("mimetype"),
                                getValue("size"),
                                getValue("encoding"),
                                getValue("locale")),
                        getValue("id"));
            }
        }).build();
    }

    @Bean
    public ClassModel<Locale> localeClassModel() {
        return ClassModel.builder(Locale.class).instanceCreatorFactory(() -> new BufferedInstanceCreator<Locale>() {

            @Override
            public Locale getInstance() {
                return new Locale(
                        getValue("language"),
                        getValue("country"),
                        getValue("variant"));
            }
        }).build();
    }

    @Bean
    public ClassModel<QANode> qaNodeClassModel() {
        ClassModelBuilder<QANode> builder = ClassModel.builder(QANode.class)
                .idPropertyName("id");

        builder.getProperty("id").bsonRepresentation(BsonType.OBJECT_ID);
        return builder.build();
    }
}
