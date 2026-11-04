package org.edu_sharing.plugin_mongo.scope;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogRetryHandler;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogService;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.scope.ScopeService;
import org.edu_sharing.service.scope.ScopesSettings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Content created (directly or deeper) inside the shared folder of a scope group gets an entry in that scope (C4).
 * The trigger is the location, not the active scope: uploads elsewhere never create an entry.
 * The shared folder is the organisation folder of the scope group (aspect ccm:edugroup_folder).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScopeUploadPolicy implements MongoAlfOpLogRetryHandler<UploadScopeNodeOpLogData>, NodeServicePolicies.OnCreateNodePolicy {

    private static final QName TYPE_IO = QName.createQName(CCConstants.CCM_TYPE_IO);
    private static final QName TYPE_MAP = QName.createQName(CCConstants.CCM_TYPE_MAP);
    private static final QName ASPECT_EDUGROUP_FOLDER = QName.createQName(CCConstants.CCM_ASPECT_EDUGROUP_FOLDER);
    private static final QName PROP_EDUGROUP_FOLDER_ORGANISATION = QName.createQName(CCConstants.CCM_PROP_EDUGROUP_FOLDER_ORGANISATION);
    private static final QName PROP_GROUP_TYPE = QName.createQName(CCConstants.CCM_PROP_GROUPEXTENSION_GROUPTYPE);
    /** guard against cyclic or unexpectedly deep hierarchies */
    private static final int MAX_DEPTH = 64;

    private final PolicyComponent policyComponent;
    private final NodeService nodeService;
    private final MongoAlfOpLogService opLogService;
    private final ObjectProvider<ScopeService> scopeService;
    private final ScopesSettings scopesSettings;

    @PostConstruct
    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, TYPE_IO,
                new JavaBehaviour(this, "onCreateNode"));
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        if (!scopesSettings.isEnabled()) {
            return;
        }
        NodeRef parent = childAssocRef.getParentRef();
        // child objects (parent is an io) inherit the entry of their parent
        if (!TYPE_MAP.equals(nodeService.getType(parent))) {
            return;
        }
        String scopeId = AuthenticationUtil.runAsSystem(() -> findScopeOfFolder(parent));
        if (scopeId == null) {
            return;
        }
        String uploader = AuthenticationUtil.getFullyAuthenticatedUser();
        opLogService.registerOpLogAction(new UploadScopeNodeOpLogData(scopeId, childAssocRef.getChildRef().getId(), uploader), this::onHandleCommit);
    }

    /**
     * @return the scope group whose shared folder contains the folder, null if none
     */
    String findScopeOfFolder(NodeRef folder) {
        NodeRef current = folder;
        for (int depth = 0; current != null && depth < MAX_DEPTH; depth++) {
            if (nodeService.hasAspect(current, ASPECT_EDUGROUP_FOLDER)) {
                NodeRef group = (NodeRef) nodeService.getProperty(current, PROP_EDUGROUP_FOLDER_ORGANISATION);
                if (group != null && nodeService.exists(group) && CCConstants.GROUP_TYPE_SCOPE.equals(nodeService.getProperty(group, PROP_GROUP_TYPE))) {
                    return (String) nodeService.getProperty(group, ContentModel.PROP_AUTHORITY_NAME);
                }
                // an organisation folder that is no scope folder: organisation folders are not nested
                return null;
            }
            ChildAssociationRef parent = nodeService.getPrimaryParent(current);
            current = parent == null ? null : parent.getParentRef();
        }
        return null;
    }

    public void onHandleCommit(UploadScopeNodeOpLogData data) {
        AuthenticationUtil.runAsSystem(() -> {
            scopeService.getObject().onUpload(data.getScopeId(), data.getNodeId(), data.getUploader());
            return null;
        });
    }

    @Override
    public Class<UploadScopeNodeOpLogData> getRetryableType() {
        return UploadScopeNodeOpLogData.class;
    }

    @Override
    public void retry(MongoAlfOpLogData opLogData) {
        if (!(opLogData instanceof UploadScopeNodeOpLogData data)) {
            throw new IllegalArgumentException("Oplog data must be of type " + UploadScopeNodeOpLogData.class.getSimpleName() + "!");
        }
        if (!nodeService.exists(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, data.getNodeId()))) {
            log.warn("Node {} does not exist anymore, skipping scope upload entry!", data.getNodeId());
            return;
        }
        onHandleCommit(data);
    }
}
