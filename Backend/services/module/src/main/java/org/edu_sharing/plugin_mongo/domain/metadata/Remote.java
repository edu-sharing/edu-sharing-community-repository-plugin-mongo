package org.edu_sharing.plugin_mongo.domain.metadata;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotBlank;

@BsonDiscriminator(key="location")
public interface Remote {
    @NotBlank String getId();
    @NotBlank Repository getRepository();
}
