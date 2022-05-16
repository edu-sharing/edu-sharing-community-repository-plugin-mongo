package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query;

import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.edu_sharing.plugin_mongo.domain.metadata.*;
import org.edu_sharing.plugin_mongo.graphql.domain.PreviewType;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.tools.ActionObserver;
import org.edu_sharing.repository.server.tools.URLTool;
import org.edu_sharing.service.nodeservice.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class InfoResolverResolver implements GraphQLResolver<Info> {

    final NodeService nodeService;

    public List<String> aspects(Info info, DataFetchingEnvironment environment){
        Metadata metadata = environment.getLocalContext();
        return metadata.getAspects().stream().map(CCConstants::getValidLocalName).collect(Collectors.toList());
    }

    public  String getNodeType(Metadata metadata){
        return CCConstants.getNameSpaceMap().get(metadata.getNodeType());
    }


    public org.edu_sharing.plugin_mongo.graphql.domain.Preview preview(Info info, DataFetchingEnvironment environment) {
        log.info("preview");
        return getPreview(info, environment);
    }

    private org.edu_sharing.plugin_mongo.graphql.domain.Preview getPreview(Info info, DataFetchingEnvironment environment) {
        Metadata metadata = environment.getLocalContext();
        if (!Objects.equals(metadata.getNodeType(), CCConstants.CCM_TYPE_IO)) {
            return null;
        }

        org.edu_sharing.plugin_mongo.graphql.domain.Preview.PreviewBuilder previewBuilder = org.edu_sharing.plugin_mongo.graphql.domain.Preview.builder();
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, metadata.getId());

        Optional<String> externalUrl = Optional.of(info)
                .map(Info::getPreview)
                .map(Preview::getUrl)
                .map(String::trim);

        if (externalUrl.isPresent() && !externalUrl.get().isEmpty()) {
            previewBuilder.url(externalUrl.get());
            previewBuilder.type(PreviewType.TYPE_EXTERNAL);
            return previewBuilder.build();
        }

        String version = metadata.getStorage() == Storage.VERSION_STORE
                ? Optional.of(metadata).map(Metadata::getVersion).map(Version::getVersion).orElse(null)
                : null;
        try {
            //Can this be optimized?
            ContentReader contentReader = nodeService.getContentReader(nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId(), version, CCConstants.CCM_PROP_IO_USERDEFINED_PREVIEW);
            if (contentReader != null && contentReader.exists()) {
                previewBuilder.url(nodeService.getPreviewUrl(nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId(), version));
                previewBuilder.type(PreviewType.TYPE_USERDEFINED);
                return previewBuilder.build();
            }
        } catch (Throwable t) {
            // may fails if the user does not has access for content
        }

        String defaultImageUrl = URLTool.getBaseUrl() + "/" + CCConstants.DEFAULT_PREVIEW_IMG;
        Action action = ActionObserver.getInstance().getAction(nodeRef, CCConstants.ACTION_NAME_CREATE_THUMBNAIL);
        if (action == null || action.getExecutionStatus().equals(ActionStatus.Completed)) {
            NodeRef previewProps = nodeService.getChild(nodeRef.getStoreRef(), nodeRef.getId(),
                    CCConstants.CM_TYPE_THUMBNAIL, CCConstants.CM_NAME, CCConstants.CM_VALUE_THUMBNAIL_NAME_imgpreview_png);

            if (previewProps != null) {
                previewBuilder.url(nodeService.getPreviewUrl(nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId(), version));
                previewBuilder.type(PreviewType.TYPE_GENERATED);
                return previewBuilder.build();
            }
        }

        previewBuilder.url(defaultImageUrl);
        previewBuilder.type(PreviewType.TYPE_DEFAULT);
        return previewBuilder.build();
    }
}
