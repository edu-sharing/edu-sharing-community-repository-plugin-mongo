package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import graphql.relay.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.Metadata;
import org.edu_sharing.plugin_mongo.graphql.connection.CursorUtils;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.RemoteShadow;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.Replication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetadataQueryResolver implements GraphQLQueryResolver {

    private final CursorUtils cursorUtils;


    public Metadata metadata(String id) {
        return Metadata.builder()._id(id).build();
    }

    public Connection<Metadata> metadatas(int first, String cursor){
        List<Edge<Metadata>> edges = new ArrayList<Metadata>()
                .stream()
                .map(metadata -> new DefaultEdge<>(metadata, cursorUtils.createCursorWith(metadata.get_id())))
                .limit(first)
                .collect(Collectors.toList());

        PageInfo pageInfo = new DefaultPageInfo(
                cursorUtils.getFirstCursorFrom(edges),
                cursorUtils.getLastCursorFrom(edges),
                cursor != null,
                edges.size() >= first);

        return  new DefaultConnection<>(edges, pageInfo);
    }

    public List<Replication> replication(){
        return null;
    }

    public List<RemoteShadow> remoteShadow(){
        return null;
    }
}
