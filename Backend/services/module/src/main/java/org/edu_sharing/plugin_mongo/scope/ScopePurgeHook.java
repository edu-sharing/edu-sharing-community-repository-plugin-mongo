package org.edu_sharing.plugin_mongo.scope;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.node.NodeArchiveServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Removes scope entries and history once a node is finally deleted (ADR-3).
 * <p>
 * Moving a node to the trashcan keeps its entries: the existing BeforeDeleteNodePolicy of the plugin already fires
 * when archiving, so it can't be used here. Instead:
 * <ul>
 *     <li>BeforePurgeNodePolicy (purge from the trashcan). It does not fire for the children of a purged folder,
 *     so the primary hierarchy is walked (same as ShareInfoServiceImpl)</li>
 *     <li>OnDeleteNodePolicy without archiving (e.g. sys:temporary)</li>
 * </ul>
 * The cleanup runs after the commit and is retried via the oplog infrastructure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScopePurgeHook implements MongoAlfOpLogRetryHandler<PurgeScopeNodeOpLogData>,
        NodeArchiveServicePolicies.BeforePurgeNodePolicy, NodeServicePolicies.OnDeleteNodePolicy {

    private static final QName TYPE_IO = QName.createQName(CCConstants.CCM_TYPE_IO);
    private static final QName TYPE_MAP = QName.createQName(CCConstants.CCM_TYPE_MAP);

    private final PolicyComponent policyComponent;
    private final NodeService nodeService;
    private final MongoAlfOpLogService opLogService;
    private final ObjectProvider<ScopeService> scopeService;

    @PostConstruct
    public void init() {
        policyComponent.bindClassBehaviour(NodeArchiveServicePolicies.BeforePurgeNodePolicy.QNAME, TYPE_IO,
                new JavaBehaviour(this, "beforePurgeNode"));
        policyComponent.bindClassBehaviour(NodeArchiveServicePolicies.BeforePurgeNodePolicy.QNAME, TYPE_MAP,
                new JavaBehaviour(this, "beforePurgeNode"));
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, TYPE_IO,
                new JavaBehaviour(this, "onDeleteNode"));
    }

    @Override
    public void beforePurgeNode(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        if (TYPE_IO.equals(type)) {
            register(nodeRef.getId());
        } else if (TYPE_MAP.equals(type)) {
            nodeService.getChildAssocs(nodeRef).stream()
                    .filter(ChildAssociationRef::isPrimary)
                    .map(ChildAssociationRef::getChildRef)
                    .filter(child -> TYPE_IO.equals(nodeService.getType(child)) || TYPE_MAP.equals(nodeService.getType(child)))
                    .forEach(this::beforePurgeNode);
        }
    }

    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
        // archived nodes stay in the trashcan, their entries are kept until the purge
        if (!isNodeArchived) {
            register(childAssocRef.getChildRef().getId());
        }
    }

    private void register(String nodeId) {
        opLogService.registerOpLogAction(new PurgeScopeNodeOpLogData(nodeId), this::onHandleCommit);
    }

    public void onHandleCommit(PurgeScopeNodeOpLogData data) {
        scopeService.getObject().onIoPurged(data.getNodeId());
    }

    @Override
    public Class<PurgeScopeNodeOpLogData> getRetryableType() {
        return PurgeScopeNodeOpLogData.class;
    }

    @Override
    public void retry(MongoAlfOpLogData opLogData) {
        if (!(opLogData instanceof PurgeScopeNodeOpLogData data)) {
            throw new IllegalArgumentException("Oplog data must be of type " + PurgeScopeNodeOpLogData.class.getSimpleName() + "!");
        }
        if (data.getNodeId() == null) {
            log.error("Node id must not be null!");
            return;
        }
        // the node may have been restored in the meantime, its entries must be kept then
        if (nodeService.exists(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, data.getNodeId()))
                || nodeService.exists(new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, data.getNodeId()))) {
            log.warn("Node {} still exists, skipping scope cleanup!", data.getNodeId());
            return;
        }
        onHandleCommit(data);
    }
}
