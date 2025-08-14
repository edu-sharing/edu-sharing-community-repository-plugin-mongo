package org.edu_sharing.plugin_mongo.oplog.map;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogRetryHandler;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogService;
import org.edu_sharing.plugin_mongo.oplog.person.DeletePersonMongoAlfOpLogData;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class MongodbMapDeleteOpLogProducer implements MongoAlfOpLogRetryHandler<DeleteMapMongoAlfOpLogData>, NodeServicePolicies.BeforeDeleteNodePolicy {

    private final ObjectProvider<MongodbMapDeletedAware> deletedAwareProvider;
    private final PolicyComponent policyComponent;
    private final NodeService nodeService;
    private final MongoAlfOpLogService opLogService;

    @PostConstruct
    public void init() {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                QName.createQName(CCConstants.CCM_TYPE_MAP),
                new JavaBehaviour(this, "beforeDeleteNode"));
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        opLogService.registerOpLogAction(new DeleteMapMongoAlfOpLogData(nodeRef.getId()), this::onHandleCommit);
    }

    public void onHandleCommit(DeleteMapMongoAlfOpLogData entity) {
        deletedAwareProvider.orderedStream()
                .forEach(bean -> bean.onMapDeleted(entity));
    }


    @Override
    public Class<DeleteMapMongoAlfOpLogData> getRetryableType() {
        return DeleteMapMongoAlfOpLogData.class;
    }

    @Override
    public void retry(MongoAlfOpLogData opLogData) {
        if(!(opLogData instanceof DeleteMapMongoAlfOpLogData data)){
            throw new IllegalArgumentException("Oplog data must be of type " + DeleteMapMongoAlfOpLogData.class.getSimpleName() + "!");
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
