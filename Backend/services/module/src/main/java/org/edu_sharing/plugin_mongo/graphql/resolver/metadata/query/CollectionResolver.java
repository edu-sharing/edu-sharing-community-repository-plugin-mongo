package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.kickstart.tools.GraphQLResolver;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CollectionResolver implements GraphQLResolver<Collection> {

    public Metadata remote(Collection collection){
        NodeRef nodeRef = collection.getRemoteNodeRef();
        log.info("Requesting forked origin for reference id {}", nodeRef.getId());

        //TODO
        return Metadata.builder()
                ._id(nodeRef.getId())
                .build();
    }
}
