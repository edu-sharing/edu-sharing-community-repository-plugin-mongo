package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import graphql.relay.*;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.dataloader.DataLoader;
import org.edu_sharing.plugin_mongo.graphql.dataloader.MetadataBatchedLoader;
import org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query.input.MetadataFilter;
import org.edu_sharing.plugin_mongo.metadata.Metadata;
import org.edu_sharing.plugin_mongo.graphql.connection.CursorUtils;
import org.edu_sharing.plugin_mongo.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MetadataQueryResolver implements GraphQLQueryResolver {

    private final CursorUtils cursorUtils;
    private final MetadataRepository metadataRepository;


    public CompletableFuture<Metadata> metadata(String id, DataFetchingEnvironment environment) {
        DataLoader<String, Metadata> dataLoader =  environment.getDataLoader(MetadataBatchedLoader.class.getSimpleName());
        return dataLoader.load(id);
    }

    public CompletableFuture<List<Metadata>> metadatas(MetadataFilter filter, DataFetchingEnvironment environment) {
        DataLoader<String, Metadata> dataLoader =  environment.getDataLoader(MetadataBatchedLoader.class.getSimpleName());
        return dataLoader.loadMany(filter.getIds());
    }

    public Connection<Metadata> pagedMetadatas(int first, String cursor) {
        List<Metadata> metadataList = metadataRepository.getMetadatas(first+1, cursor);

        List<Edge<Metadata>> edges = metadataList.stream()
                .map(metadata -> new DefaultEdge<>(metadata, cursorUtils.createCursorWith(metadata.getId())))
                .limit(first)
                .collect(Collectors.toList());

        PageInfo pageInfo = new DefaultPageInfo(
                cursorUtils.getFirstCursorFrom(edges),
                cursorUtils.getLastCursorFrom(edges),
                cursor != null,
                metadataList.size() > first);

        return new DefaultConnection<>(edges, pageInfo);
    }
}
