package org.edu_sharing.plugin_mongo.suggestion;

import org.bson.BsonType;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.PropertyModelBuilder;
import org.edu_sharing.service.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config implements ApplicationContextAware {

    private final ObjectCodec codec = new ObjectCodec();


    @Autowired
    public void setApplicationContext(@NotNull ApplicationContext applicationContext){
        codec.setApplicationContext(applicationContext);
    }

    @Bean
    @SuppressWarnings("unchecked")
    public ClassModel<Suggestion> suggestionClassModel(){
        ClassModelBuilder<Suggestion> builder = ClassModel.builder(Suggestion.class);
        builder.idPropertyName("id");
        builder.getProperty("id").bsonRepresentation(BsonType.OBJECT_ID);

        ((PropertyModelBuilder<Object>)builder.getProperty("value")).codec(codec);
        return builder.build();
    }
}
