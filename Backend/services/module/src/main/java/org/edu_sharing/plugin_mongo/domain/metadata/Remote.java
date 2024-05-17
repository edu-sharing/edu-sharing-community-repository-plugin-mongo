package org.edu_sharing.plugin_mongo.domain.metadata;

import jakarta.validation.constraints.NotBlank;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;


@BsonDiscriminator(key="location")
public interface Remote {
    @NotBlank
    String getId();
    @NotBlank Repository getRepository();
}
