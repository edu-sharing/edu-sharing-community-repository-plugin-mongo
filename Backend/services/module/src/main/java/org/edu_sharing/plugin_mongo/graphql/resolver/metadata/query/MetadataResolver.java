package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.execution.DataFetcherResult;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.edu_sharing.plugin_mongo.domain.metadata.Info;
import org.edu_sharing.plugin_mongo.domain.metadata.Metadata;
import org.edu_sharing.plugin_mongo.domain.suggestion.Suggestion;
import org.edu_sharing.plugin_mongo.graphql.dataloader.SuggestionBatchedLoader;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class MetadataResolver implements GraphQLResolver<Metadata> {

    public  String getNodeType(Metadata metadata){
        return CCConstants.getValidLocalName(metadata.getNodeType());
    }

    public DataFetcherResult<Info> info(Metadata metadata, DataFetchingEnvironment environment){
        return DataFetcherResult.<Info>newResult()
                .data(metadata.getInfo())
                .localContext(metadata)
                .build();
    }

    public CompletableFuture<List<Suggestion>> generated(Metadata metadata, DataFetchingEnvironment environment){
        DataLoader<String, List<Suggestion>> dataLoader =  environment.getDataLoader(SuggestionBatchedLoader.class.getSimpleName());
        return dataLoader.load(metadata.getId());
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


