package org.edu_sharing.plugin_mongo.oplog.authority;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLog;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogRetryHandler;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogTransactionListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongodbAuthorityDeleteOpLogProducer implements MongoAlfOpLogRetryHandler<DeleteAuthorityMongoAlfOpLogData>, NodeServicePolicies.BeforeDeleteNodePolicy {

    private final ObjectProvider<MongodbAuthorityDeletedAware> deletedAwareProvider;
    private final PolicyComponent policyComponent;
    private final NodeService nodeService;
    private final ObjectProvider<MongoAlfOpLogTransactionListener<DeleteAuthorityMongoAlfOpLogData>> transactionListenerProvider;

    @PostConstruct
    public void init() {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "beforeDeleteNode"));
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        MongoAlfOpLogTransactionListener<DeleteAuthorityMongoAlfOpLogData> instance = transactionListenerProvider.getObject();
        instance.setLoggingActionData(new DeleteAuthorityMongoAlfOpLogData(null, nodeRef.getId(), new Date()), this::onHandleCommit);
        AlfrescoTransactionSupport.bindListener(instance);
    }

    public void onHandleCommit(DeleteAuthorityMongoAlfOpLogData actionData) {
        deletedAwareProvider.orderedStream()
                .forEach(bean -> bean.onAuthorityDeleted(actionData));
    }


    @Override
    public Class<DeleteAuthorityMongoAlfOpLogData> getRetryableType() {
        return DeleteAuthorityMongoAlfOpLogData.class;
    }

    @Override
    public void retry(MongoAlfOpLogData opLogData) {
        if(!(opLogData instanceof DeleteAuthorityMongoAlfOpLogData)){
            throw new IllegalArgumentException("Oplog data must be of type DeleteAuthorityMongoAlfOpLogData!");
        }
        DeleteAuthorityMongoAlfOpLogData data = (DeleteAuthorityMongoAlfOpLogData)opLogData;
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
