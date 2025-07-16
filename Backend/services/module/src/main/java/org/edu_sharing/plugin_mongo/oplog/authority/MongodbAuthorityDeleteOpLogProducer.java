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
import org.edu_sharing.plugin_mongo.oplog.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * This class is responsible for handling operational log entries regarding the deletion
 * of authority nodes in a MongoDB-backed system. It integrates with the MongoAlfOpLog
 * infrastructure for logging deletion events and executing retry logic if required.
 *
 * It also implements Alfresco's {@link NodeServicePolicies.BeforeDeleteNodePolicy} to
 * intercept and process events before a person-type node is deleted.
 *
 * Responsibilities:
 * - Logs authority deletion actions as {@link DeleteAuthorityMongoAlfOpLogData} in the
 *   MongoDB operational log system.
 * - Manages retry logic for handling authority deletion actions if the initial operation
 *   fails or needs to be re-executed.
 * - Invokes any registered beans implementing {@link MongodbAuthorityDeletedAware} to
 *   trigger custom processing once an authority deletion action is committed.
 *
 * Key Components:
 * - {@link PolicyComponent}: Used to bind the behavior to Alfresco's `BeforeDeleteNodePolicy`.
 * - {@link NodeService}: Leverages Alfresco's NodeService to check for node existence.
 * - {@link MongoAlfOpLogService}: Facilitates the registration of delete operations
 *   into the operational log system, synchronized with transaction lifecycle events.
 * - {@link MongodbAuthorityDeletedAware}: Allows external beans to react to authority
 *   deletion actions.
 *
 * Methods:
 * - {@code init}: Binds the class to handle node deletion behavior for `ContentModel.TYPE_PERSON`.
 * - {@code beforeDeleteNode}: Intercepts node deletion events, logs the action, and sets up
 *   a commit handler callback.
 * - {@code onHandleCommit}: Executes the callback associated with an operational log entry
 *   upon transaction commit, notifying all beans implementing {@code MongodbAuthorityDeletedAware}.
 * - {@code getRetryableType}: Identifies the type of operational log data, {@code DeleteAuthorityMongoAlfOpLogData},
 *   that this handler supports for retry functionality.
 * - {@code retry}: Implements retry logic for processing a logged deletion action, ensuring
 *   appropriate validation and preventing duplicate operations on nodes that exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MongodbAuthorityDeleteOpLogProducer implements MongoAlfOpLogRetryHandler<DeleteAuthorityMongoAlfOpLogData>, NodeServicePolicies.BeforeDeleteNodePolicy {

    private final ObjectProvider<MongodbAuthorityDeletedAware> deletedAwareProvider;
    private final PolicyComponent policyComponent;
    private final NodeService nodeService;
    private final MongoAlfOpLogService opLogService;

    @PostConstruct
    public void init() {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "beforeDeleteNode"));
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        opLogService.registerOpLogAction(new DeleteAuthorityMongoAlfOpLogData(null, nodeRef.getId(), new Date()), this::onHandleCommit);
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
