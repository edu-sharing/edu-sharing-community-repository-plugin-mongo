package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.kickstart.tools.GraphQLResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.plugin_mongo.metadata.Metadata;
import org.edu_sharing.plugin_mongo.metadata.NodeRef;
import org.edu_sharing.plugin_mongo.metadata.Reference;
import org.edu_sharing.plugin_mongo.metadata.Version;
import org.edu_sharing.plugin_mongo.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReferenceResolver implements GraphQLResolver<Reference> {

    private final MetadataRepository metadataRepository;
    //private final Executor executor;

    public Metadata collection(Reference reference){
        NodeRef nodeRef = reference.getCollection();
        if(Objects.nonNull(nodeRef)) {
            log.info("Requesting collection data for reference id {} with version {}", nodeRef.getId(), nodeRef.getVersion());
            return metadataRepository.getMetadata(nodeRef.getId(), nodeRef.getVersion());
        }
        return null;
    }
}
