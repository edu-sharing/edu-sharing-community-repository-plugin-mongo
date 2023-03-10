package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query.input.MetadataFilter;
import org.edu_sharing.plugin_mongo.domain.metadata.Metadata;
import org.edu_sharing.plugin_mongo.service.legacy.AlfrescoMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MetadataQueryResolver implements GraphQLQueryResolver {

    //private final CursorUtils cursorUtils;
    private final AlfrescoMetadataService metadataNodeService;

    public Metadata metadata(String id, String version) {
        return metadataNodeService.getMetadata(id, version);
    }

    public List<Metadata> metadatas(MetadataFilter filter, DataFetchingEnvironment environment) {
        return metadataNodeService.getMetadatas(filter.getIds());
    }

//    public Connection<Metadata> pagedMetadatas(int first, String cursor) {
//        List<Metadata> metadataList = metadataRepository.getMetadatas(first+1, cursor);
//
//        List<Edge<Metadata>> edges = metadataList.stream()
//                .map(metadata -> new DefaultEdge<>(metadata, cursorUtils.createCursorWith(metadata.getId())))
//                .limit(first)
//                .collect(Collectors.toList());
//
//        PageInfo pageInfo = new DefaultPageInfo(
//                cursorUtils.getFirstCursorFrom(edges),
//                cursorUtils.getLastCursorFrom(edges),
//                cursor != null,
//                metadataList.size() > first);
//
//        return new DefaultConnection<>(edges, pageInfo);
//    }
}
