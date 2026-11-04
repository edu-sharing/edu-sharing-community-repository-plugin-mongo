package org.edu_sharing.plugin_mongo.scope;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogRetryHandler;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogService;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.scope.ScopeService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * A deleted scope group removes all entries and the history of its scope immediately (groups have no trashcan).
 * The index is cleaned up by the tracker (SCOPE_DELETED).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScopeGroupDeleteHook implements MongoAlfOpLogRetryHandler<DeleteScopeGroupOpLogData>, NodeServicePolicies.BeforeDeleteNodePolicy {

    private final PolicyComponent policyComponent;
    private final NodeService nodeService;
    private final AuthorityService authorityService;
    private final MongoAlfOpLogService opLogService;
    private final ObjectProvider<ScopeService> scopeService;

    @PostConstruct
    public void init() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                ContentModel.TYPE_AUTHORITY_CONTAINER,
                new JavaBehaviour(this, "beforeDeleteNode"));
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        if (!CCConstants.GROUP_TYPE_SCOPE.equals(nodeService.getProperty(nodeRef, QName.createQName(CCConstants.CCM_PROP_GROUPEXTENSION_GROUPTYPE)))) {
            return;
        }
        String scopeId = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_NAME);
        opLogService.registerOpLogAction(new DeleteScopeGroupOpLogData(scopeId), this::onHandleCommit);
    }

    public void onHandleCommit(DeleteScopeGroupOpLogData data) {
        scopeService.getObject().onScopeGroupDeleted(data.getScopeId());
    }

    @Override
    public Class<DeleteScopeGroupOpLogData> getRetryableType() {
        return DeleteScopeGroupOpLogData.class;
    }

    @Override
    public void retry(MongoAlfOpLogData opLogData) {
        if (!(opLogData instanceof DeleteScopeGroupOpLogData data)) {
            throw new IllegalArgumentException("Oplog data must be of type " + DeleteScopeGroupOpLogData.class.getSimpleName() + "!");
        }
        if (data.getScopeId() == null) {
            log.error("Scope id must not be null!");
            return;
        }
        if (authorityService.authorityExists(data.getScopeId())) {
            log.warn("Scope group {} still exists, skipping scope cleanup!", data.getScopeId());
            return;
        }
        onHandleCommit(data);
    }
}
