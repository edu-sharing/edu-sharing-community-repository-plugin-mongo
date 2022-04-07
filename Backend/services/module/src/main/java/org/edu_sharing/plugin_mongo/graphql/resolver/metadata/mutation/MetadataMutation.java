package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.mutation;

import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public String updateLom(@NotBlank String id, Lom lom, DataFetchingEnvironment environment){
        metadataRepository.updateLom(id, lom);
        return "";
    }
}
