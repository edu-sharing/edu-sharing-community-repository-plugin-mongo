package org.edu_sharing.plugin_mongo.metadata;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@BsonDiscriminator(key="location", value = "REMOTE_SHADOW")
public class RemoteShadow implements Remote {
    @NotBlank
    String id;

    @NotBlank
    Repository repository;
}
