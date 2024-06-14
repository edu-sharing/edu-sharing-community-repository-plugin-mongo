package org.edu_sharing.plugin_mongo.domain.metadata;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.edu_sharing.plugin_mongo.graphql.annotation.GraphQLSchema;

import java.util.Date;

@Data
@Builder
@GraphQLSchema
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@BsonDiscriminator(key="location", value = "REPLICATION")
public class Replication implements Remote {
    @NotBlank
    String id;

    String uuid;
    String hash;
    Date modified;
    String timestamp;
    boolean importBlocked;

    @NotBlank
    Repository repository;
}
