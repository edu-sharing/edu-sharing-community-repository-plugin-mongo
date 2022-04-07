package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.edu_sharing.plugin_mongo.graphql.dataloader.MetadataBatchedLoader;
import org.edu_sharing.plugin_mongo.metadata.Collection;
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
public class CollectionResolver implements GraphQLResolver<Collection> {

    private final MetadataRepository metadataRepository;

    public CompletableFuture<Metadata> remote(Collection collection, DataFetchingEnvironment environment) {
        NodeRef nodeRef = collection.getRemote();
        if(Objects.nonNull(nodeRef)) {
            log.info("Requesting forked origin for reference id {}", nodeRef.getId());
            DataLoader<String, Metadata> dataLoader =  environment.getDataLoader(MetadataBatchedLoader.class.getSimpleName());
            return dataLoader.load(nodeRef.getId());
        }
        return null;
    }
}
