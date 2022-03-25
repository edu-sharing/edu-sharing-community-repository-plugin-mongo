package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.kickstart.tools.GraphQLResolver;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.Association;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.Metadata;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.NodeRef;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.Version;
import org.springframework.stereotype.Component;

@Slf4j
@Component
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AssociationResolver implements GraphQLResolver<Association> {

    public Metadata symlink(Association association){
        NodeRef nodeRef = association.getSymlinkNodeRef();
        log.info("Requesting forked origin for reference id {}", nodeRef.getId());

        //TODO
        return Metadata.builder()
                ._id(nodeRef.getId())
                .build();
    }

    public Metadata forkedOrigin(Association association){
        NodeRef nodeRef = association.getForkedOriginNodeRef();
        log.info("Requesting forked origin for reference id {} with version {}", nodeRef.getId(), nodeRef.getVersion());

        //TODO
        return Metadata.builder()
                ._id(nodeRef.getId())
                .version(Version.builder().version(nodeRef.getVersion()).build())
                .build();
    }

    public Metadata original(Association association){
        NodeRef nodeRef = association.getOriginalNodeRef();
        log.info("Requesting original for reference id {}", nodeRef.getId());

        //TODO
        return Metadata.builder()
                ._id(nodeRef.getId())
                .build();
    }

    public Metadata publishedOriginal(Association association){
        NodeRef nodeRef = association.getPublishedOriginalNodeRef();
        log.info("Requesting published original for reference id {}", nodeRef.getId());

        //TODO
        return Metadata.builder()
                ._id(nodeRef.getId())
                .build();
    }

}
