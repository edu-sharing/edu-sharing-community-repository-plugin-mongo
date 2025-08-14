package org.edu_sharing.plugin_mongo.oplog.person;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogRetryHandler;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * A component responsible for producing operational logs related to the deletion
 * of "Person" nodes in a MongoDB-backed Alfresco repository. This class listens to
 * the Alfresco BeforeDeleteNodePolicy for "Person" content types, logs delete operations,
 * and provides retry handling for the logged actions.
 * <p>
 * Implements:
 * - {@link MongoAlfOpLogRetryHandler} to provide retry capabilities for failed
 * MongoDB operation log handling.
 * - {@link NodeServicePolicies.BeforeDeleteNodePolicy} to listen for "Person"
 * node deletion events.
 * <p>
 * Responsibilities:
 * - Registers itself as a BeforeDeleteNodePolicy for "Person" nodes during initialization.
 * - Logs a delete operation by using {@link MongoAlfOpLogService#registerOpLogAction}
 * whenever a "Person" node is about to be deleted.
 * - Invokes external handlers implementing {@link MongodbPersonDeletedAware}
 * after a delete operation is committed.
 * - Retries handling of failed delete operation logs with appropriate validation
 * of operation log data.
 * <p>
 * Dependencies:
 * - {@link ObjectProvider<MongodbPersonDeletedAware>} to access handlers
 * that process person deletion-related actions.
 * - {@link PolicyComponent} to register custom class behavior policies in Alfresco.
 * - {@link NodeService} to interact with the node repository including
 * retrieving node properties and checking existence.
 * - {@link MongoAlfOpLogService} to register and process delete-related operational logs.
 * <p>
 * Behavior:
 * - During initialization, binds to Alfresco's BeforeDeleteNodePolicy for "Person" nodes.
 * - Before a "Person" node is deleted, logs the delete operation using
 * {@link MongoAlfOpLogService}, passing the node ID and username.
 * - Sends the delete person operation to all registered {@link MongodbPersonDeletedAware}
 * beans when the operation log is processed.
 * - Validates operation log data during retries, ensuring node existence
 * and appropriate data structure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MongodbPersonDeleteOpLogProducer implements MongoAlfOpLogRetryHandler<DeletePersonMongoAlfOpLogData>, NodeServicePolicies.BeforeDeleteNodePolicy {

    private final ObjectProvider<MongodbPersonDeletedAware> deletedAwareProvider;
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
        String userName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
        opLogService.registerOpLogAction(new DeletePersonMongoAlfOpLogData(nodeRef.getId(), userName), this::onHandleCommit);
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
        if (!(opLogData instanceof DeletePersonMongoAlfOpLogData data)) {
            throw new IllegalArgumentException("Oplog data must be of type " + DeletePersonMongoAlfOpLogData.class.getSimpleName() + "!");
        }
        if (data.getNodeId() == null) {
            log.error("Node id must not be null!");
            return;
        }

        if (nodeService.exists(new NodeRef(data.getNodeId()))) {
            log.warn("Node {} does exist, skipping delete action!", data.getNodeId());
            return;
        }

        onHandleCommit(data);
    }
}
