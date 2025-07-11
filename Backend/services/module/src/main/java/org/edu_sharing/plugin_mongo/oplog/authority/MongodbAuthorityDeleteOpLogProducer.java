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
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogTransactionListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongodbAuthorityDeleteOpLogProducer implements NodeServicePolicies.BeforeDeleteNodePolicy {

    private final ObjectProvider<MongodbAuthorityDeletedAware> deletedAwareProvider;
    private final PolicyComponent policyComponent;
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
}
