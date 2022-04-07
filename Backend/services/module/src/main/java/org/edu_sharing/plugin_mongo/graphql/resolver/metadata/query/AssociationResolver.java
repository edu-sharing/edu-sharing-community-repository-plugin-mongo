package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.edu_sharing.plugin_mongo.graphql.dataloader.MetadataBatchedLoader;
import org.edu_sharing.plugin_mongo.metadata.Association;
import org.edu_sharing.plugin_mongo.metadata.Metadata;
import org.edu_sharing.plugin_mongo.metadata.NodeRef;
import org.edu_sharing.plugin_mongo.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AssociationResolver implements GraphQLResolver<Association> {

    private final MetadataRepository metadataRepository;
    //private final Executor executor;

    public CompletableFuture<Metadata> symlink(Association association, DataFetchingEnvironment environment){
        NodeRef nodeRef = association.getSymlink();
        if(Objects.nonNull(nodeRef)) {
            log.info("Requesting forked origin for reference id {}", nodeRef.getId());
            DataLoader<String, Metadata> dataLoader =  environment.getDataLoader(MetadataBatchedLoader.class.getSimpleName());
            return dataLoader.load(nodeRef.getId());
        }
        return null;
    }

    public Metadata forkedOrigin(Association association){
        NodeRef nodeRef = association.getForkedOrigin();
        if(Objects.nonNull(nodeRef)) {
            log.info("Requesting forked origin for reference id {} with version {}", nodeRef.getId(), nodeRef.getVersion());
            //return CompletableFuture.supplyAsync(()-> metadataRepository.getMetadata(nodeRef.getId(), nodeRef.getVersion()), executor);
            return metadataRepository.getMetadata(nodeRef.getId(), nodeRef.getVersion());
        }
        return null;
    }

    public CompletableFuture<Metadata>  original(Association association, DataFetchingEnvironment environment){
        NodeRef nodeRef = association.getOriginal();
        if(Objects.nonNull(nodeRef)) {
            log.info("Requesting original for reference id {}", nodeRef.getId());
            DataLoader<String, Metadata> dataLoader =  environment.getDataLoader(MetadataBatchedLoader.class.getSimpleName());
            return dataLoader.load(nodeRef.getId());
        }
        return null;
    }

    public CompletableFuture<Metadata>  publishedOriginal(Association association, DataFetchingEnvironment environment){
        NodeRef nodeRef = association.getPublishedOriginal();
        if(Objects.nonNull(nodeRef)) {
            log.info("Requesting published original for reference id {}", nodeRef.getId());
            DataLoader<String, Metadata> dataLoader =  environment.getDataLoader(MetadataBatchedLoader.class.getSimpleName());
            return dataLoader.load(nodeRef.getId());
        }
        return null;
    }

}
