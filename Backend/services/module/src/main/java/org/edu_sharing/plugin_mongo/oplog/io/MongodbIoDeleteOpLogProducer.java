package org.edu_sharing.plugin_mongo.oplog.io;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogRetryHandler;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogService;
import org.edu_sharing.plugin_mongo.oplog.map.DeleteMapMongoAlfOpLogData;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class MongodbIoDeleteOpLogProducer implements MongoAlfOpLogRetryHandler<DeleteIoMongoAlfOpLogData>, NodeServicePolicies.BeforeDeleteNodePolicy {

    private final ObjectProvider<MongodbIoDeletedAware> deletedAwareProvider;
    private final PolicyComponent policyComponent;
    private final NodeService nodeService;
    private final MongoAlfOpLogService opLogService;

    @PostConstruct
    public void init() {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                QName.createQName(CCConstants.CCM_TYPE_IO),
                new JavaBehaviour(this, "beforeDeleteNode"));
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        opLogService.registerOpLogAction(new DeleteIoMongoAlfOpLogData(nodeRef.getId()), this::onHandleCommit);
    }

    public void onHandleCommit(DeleteIoMongoAlfOpLogData entity) {
        deletedAwareProvider.orderedStream()
                .forEach(bean -> bean.onIoDeleted(entity));
    }


    @Override
    public Class<DeleteIoMongoAlfOpLogData> getRetryableType() {
        return DeleteIoMongoAlfOpLogData.class;
    }

    @Override
    public void retry(MongoAlfOpLogData opLogData) {
        if(!(opLogData instanceof DeleteIoMongoAlfOpLogData data)){
            throw new IllegalArgumentException("Oplog data must be of type " + DeleteIoMongoAlfOpLogData.class.getSimpleName() + "!");
        }

        if(data.getNodeId() == null){
            log.error("Node id must not be null!");
            return;
        }

        if(nodeService.exists(new NodeRef(data.getNodeId()))){
            log.warn("Node {} does exist, skipping delete action!", data.getNodeId());
            return;
        }

        onHandleCommit(data);
    }
}
