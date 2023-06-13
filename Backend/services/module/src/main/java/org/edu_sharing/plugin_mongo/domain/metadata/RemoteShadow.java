package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.edu_sharing.plugin_mongo.graphql.annotation.GraphQLSchema;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@GraphQLSchema
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@BsonDiscriminator(key="location", value = "REMOTE_SHADOW")
public class RemoteShadow implements Remote {
    @NotBlank
    String id;

    @NotBlank
    Repository repository;
}
