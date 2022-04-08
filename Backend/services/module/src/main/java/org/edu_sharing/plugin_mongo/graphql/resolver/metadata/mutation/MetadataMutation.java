package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.mutation;

import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.plugin_mongo.metadata.Metadata;
import org.edu_sharing.plugin_mongo.metadata.lom.Lom;
import org.edu_sharing.plugin_mongo.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Slf4j
@Validated
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MetadataMutation implements GraphQLMutationResolver {

    final MetadataRepository metadataRepository;

    public Metadata updateLom(@NotBlank String id, Lom lom) {
        return metadataRepository.updateLom(id, lom);
    }
}
