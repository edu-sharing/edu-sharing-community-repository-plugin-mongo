package org.edu_sharing.plugin_mongo.mongo.convention;

import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PropertyModelBuilder;
import org.springframework.stereotype.Component;

@Component
public class ClassConvention implements Convention {
    @Override
    public void apply(final ClassModelBuilder<?> classModelBuilder) {
        if (classModelBuilder.getDiscriminatorKey() == null) {
            classModelBuilder.discriminatorKey("_t");
        }
        if (classModelBuilder.getDiscriminator() == null && classModelBuilder.getType() != null) {
            classModelBuilder.discriminator(classModelBuilder.getType().getName());
        }

        for (final PropertyModelBuilder<?> propertyModel : classModelBuilder.getPropertyModelBuilders()) {
            if (classModelBuilder.getIdPropertyName() == null) {
                String propertyName = propertyModel.getName();
                if (propertyName.equals("_id")) {
                    classModelBuilder.idPropertyName(propertyName);
                }
            }
        }
    }
}