package org.edu_sharing.plugin_mongo.oplog.person;

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
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogRetryHandler;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogTransactionListener;
import org.edu_sharing.plugin_mongo.oplog.authority.DeleteAuthorityMongoAlfOpLogData;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongodbPersonDeleteOpLogProducer implements MongoAlfOpLogRetryHandler<DeletePersonMongoAlfOpLogData>, NodeServicePolicies.BeforeDeleteNodePolicy {

    private final ObjectProvider<MongodbPersonDeletedAware> deletedAwareProvider;
    private final PolicyComponent policyComponent;
    private final NodeService nodeService;
    private final ObjectProvider<MongoAlfOpLogTransactionListener<DeletePersonMongoAlfOpLogData>> transactionListenerProvider;

    @PostConstruct
    public void init() {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "beforeDeleteNode"));
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        MongoAlfOpLogTransactionListener<DeletePersonMongoAlfOpLogData> instance = transactionListenerProvider.getObject();

        String userName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
        instance.setLoggingActionData(new DeletePersonMongoAlfOpLogData(nodeRef.getId(), userName), this::onHandleCommit);

        AlfrescoTransactionSupport.bindListener(instance);
    }

    public void onHandleCommit(DeletePersonMongoAlfOpLogData entity) {
        deletedAwareProvider.orderedStream()
                .forEach(bean -> bean.onPersonDeleted(entity));
    }


    @Override
    public Class<DeletePersonMongoAlfOpLogData> getRetryableType() {
        return DeletePersonMongoAlfOpLogData.class;
    }

    @Override
    public void retry(MongoAlfOpLogData opLogData) {
        if(!(opLogData instanceof DeletePersonMongoAlfOpLogData)){
            throw new IllegalArgumentException("Oplog data must be of type DeletePersonMongoAlfOpLogData!");
        }
        DeletePersonMongoAlfOpLogData data = (DeletePersonMongoAlfOpLogData)opLogData;
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
