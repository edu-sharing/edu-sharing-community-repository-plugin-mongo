package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.kickstart.tools.GraphQLResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.Metadata;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.NodeRef;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.Reference;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReferenceResolver implements GraphQLResolver<Reference> {

    public Metadata collection(Reference reference){
        NodeRef nodeRef = reference.getCollection();
        log.info("Requesting collection data for reference id {} with version {}", nodeRef.getId(), nodeRef.getVersion());

        //TODO
        return Metadata.builder()
                ._id(nodeRef.getId())
                .version(Version.builder().version(nodeRef.getVersion()).build())
                .build();
    }
}
