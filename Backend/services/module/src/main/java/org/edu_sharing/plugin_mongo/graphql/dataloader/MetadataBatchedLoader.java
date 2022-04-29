package org.edu_sharing.plugin_mongo.graphql.dataloader;

import lombok.RequiredArgsConstructor;
import org.dataloader.MappedBatchLoader;
import org.edu_sharing.plugin_mongo.domain.metadata.Metadata;
import org.edu_sharing.plugin_mongo.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MetadataBatchedLoader implements MappedBatchLoader<String, Metadata> {

    final MetadataRepository metadataRepository;
//    final Executor metadataExecutor;
    @Override
    public CompletionStage<Map<String, Metadata>> load(Set<String> set) {
        //return CompletableFuture.supplyAsync(()->metadataRepository.getMetadatas(set).stream().collect(Collectors.toMap(Metadata::getId, x->x)),  metadataExecutor);
        return CompletableFuture.completedFuture(metadataRepository.getMetadatas(set).stream().collect(Collectors.toMap(Metadata::getId, x->x)));
    }
}
