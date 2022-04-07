package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.execution.DataFetcherResult;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.plugin_mongo.metadata.Info;
import org.edu_sharing.plugin_mongo.metadata.Metadata;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class MetadataResolver implements GraphQLResolver<Metadata> {

    public DataFetcherResult<Info> info(Metadata metadata, DataFetchingEnvironment environment){
        return DataFetcherResult.<Info>newResult()
                .data(metadata.getInfo())
                .localContext(metadata)
                .build();
    }

//    public Preview preview(Metadata metadata, DataFetchingEnvironment environment) {
//        log.info("mimetype");
//        return AuthenticationUtil.runAsSystem(() -> {
//            NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, metadata.getId());
//            ContentReader contentReader = contentService.getReader(nodeRef, QName.createQName(CCConstants.CM_PROP_CONTENT));
//
//            Preview preview = new Preview();
//
//            if (Objects.nonNull(contentReader)) {
//                preview.setMimetype(contentReader.getMimetype());
//            }
//
//            String renderServiceUrlPreview = URLTool.getRenderServiceURL(metadata.getId(), true);
//            if (Objects.nonNull(renderServiceUrlPreview)) {
//                preview.setUrl(renderServiceUrlPreview);
//            } else {
//                preview.setUrl(nodeService.getPreviewUrl(nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId(), null));
//            }
//
//            return preview;
//        });
//    }
}


