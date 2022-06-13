package org.edu_sharing.plugin_mongo.jobs.quarz;

import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.solr.*;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.edu_sharing.alfresco.service.search.cmis.*;
import org.edu_sharing.plugin_mongo.domain.system.TransactionalSyncState;
import org.edu_sharing.plugin_mongo.repository.AwareAlfrescoDeletion;
import org.edu_sharing.plugin_mongo.repository.MongoAlfrescoSyncStateRepository;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.jobs.quartz.AbstractJob;
import org.edu_sharing.repository.server.jobs.quartz.AbstractJobMapAnnotationParams;
import org.edu_sharing.repository.server.jobs.quartz.annotation.JobDescription;
import org.edu_sharing.repository.server.jobs.quartz.annotation.JobFieldDescription;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@JobDescription(
    description =
        "This will delete all MongoDB documents where the corresponding node and all its references were deleted from alfresco")
public class ObsoleteMongoEntiesDeletionJob extends AbstractJobMapAnnotationParams {

  @JobFieldDescription(
          description = "resets sync state. This will delete all data of the sync state and related collections",
          sampleValue = "false")
  protected boolean resetSyncState = false;

  @JobFieldDescription(
      description = "maximal transactions witch will be processed by this Job",
      sampleValue = "500")
  protected int maxTransactionResults = 500;

  @JobFieldDescription(
          description = "maximal number of checks against deleted nodes witch will be processed by this Job",
          sampleValue = "500")
  protected int maxDeletedNodeChecks = 500;

  @Autowired private SOLRTrackingComponent trackingComponent;
  @Autowired private MongoAlfrescoSyncStateRepository repository;
  @Autowired private TenantService tenantService;
  @Autowired private QNameDAO qnameDAO;

  @Autowired(required = false)
  private List<AwareAlfrescoDeletion> awareAlfrescoDeletions = new ArrayList<>();

  @Autowired private QueryBuilder queryBuilder;
  @Autowired private SearchService searchService;

  @Autowired private RetryingTransactionHelper retryingTransactionHelper;

  @Override
  public void executeInternal(JobExecutionContext jobExecutionContext) {
    retryingTransactionHelper.doInTransaction(this::trackRepository);
  }

  private Void trackRepository() {
    return AuthenticationUtil.runAsSystem(this::trackTransactions);
  }

  private Void trackTransactions() {

    if(resetSyncState){
      repository.reset();
    }

    List<Transaction> transactions;
    do {
      TransactionalSyncState syncState = repository.getTransactionalSyncState();
      Long minTxnId =
          Optional.of(syncState)
              .map(TransactionalSyncState::getLastTransactionId)
              .map(id -> id + 1)
              .orElse(0L);

      Long maxTxnId = minTxnId + maxTransactionResults;

      transactions = trackingComponent.getTransactions(minTxnId, null, maxTxnId, null, maxTransactionResults);

      Transaction latestTransaction =
          transactions.size() > 0 ? transactions.get(transactions.size() - 1) : null;

      if (latestTransaction == null
          || Objects.equals(syncState.getLastTransactionId(), latestTransaction.getId())) {
        logger.info("nothing to do");
        break;
      }

      NodeParameters nodeParameters = new NodeParameters();
      nodeParameters.setTransactionIds(
          transactions.stream().map(Transaction::getId).collect(Collectors.toList()));

      Set<String> nodeIdsToKeepTracking = new HashSet<>();
      Set<String> nodeIdsToDelete = new HashSet<>();
      trackingComponent.getNodes(
          nodeParameters,
          node -> {
            if (!node.getNodeStatus(qnameDAO).isDeleted()) {
              return true;
            }

            if (isReferenced(node.getNodeRef())) {
              nodeIdsToKeepTracking.add(node.getNodeRef().getId());
            } else {
              nodeIdsToDelete.add(node.getNodeRef().getId());
            }

            return true;
          });

      if (nodeIdsToDelete.size() > 0) {
        awareAlfrescoDeletions.forEach(
            x -> {
              try {
                x.OnDeletedInAlfresco(nodeIdsToDelete);
              } catch (Exception ex) {
                logger.error(ex.getMessage());
              }
            });
      }

      syncState.setLastTransactionId(latestTransaction.getId());
      repository.setTransactionalSyncState(syncState);
      repository.setDeletedNodeIdsToTrack(nodeIdsToKeepTracking);

    } while (transactions.size() > 0);

    List<String> deletedNodeIdsToTrack = repository.getDeletedNodeIdsToTrack(maxDeletedNodeChecks);
    Set<String> nodeIdsToDelete = new HashSet<>();
    Set<String> nodeIdsToKeepTracking = new HashSet<>(deletedNodeIdsToTrack);
    deletedNodeIdsToTrack.forEach(
        x -> {
          if (!isReferenced(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, x))) {
            nodeIdsToDelete.add(x);
            nodeIdsToKeepTracking.remove(x);
          }
        });

    if (nodeIdsToDelete.size() > 0) {
      awareAlfrescoDeletions.forEach(
          x -> {
            try {
              x.OnDeletedInAlfresco(nodeIdsToDelete);
            } catch (Exception ex) {
              logger.error(ex.getMessage());
            }
          });
    }
    repository.removeDeletedNodeIdsToTrack(nodeIdsToDelete);
    repository.setDeletedNodeIdsToTrack(nodeIdsToKeepTracking);

    return null;
  }

  private boolean isReferenced(NodeRef nodeRef) {
    // TODO alf 5 does not support left outer joins!
    return isReferencedByOriginal(nodeRef) || isReferencedByPublishedOriginal(nodeRef);
  }

  private boolean isReferencedByOriginal(NodeRef nodeRef) {
    QueryStatement query =
        Query.select(CCConstants.SYS_PROP_NODE_UID)
            .from(CCConstants.CCM_TYPE_IO)
            .where(
                Filters.and(
                    Filters.neq(CCConstants.SYS_PROP_NODE_UID, nodeRef.toString()),
                    Filters.eq(CCConstants.CCM_PROP_IO_ORIGINAL, nodeRef.getId())));

    return findReferences(query);
  }


  private boolean isReferencedByPublishedOriginal(NodeRef nodeRef) {
    QueryStatement query =
        Query.select(CCConstants.SYS_PROP_NODE_UID)
            .from(CCConstants.CCM_TYPE_IO)
            .where(
                Filters.and(
                    Filters.neq(CCConstants.SYS_PROP_NODE_UID, nodeRef.toString()),
                    Filters.eq(CCConstants.CCM_PROP_IO_PUBLISHED_ORIGINAL, nodeRef.toString())));

    return findReferences(query);
  }

  private boolean findReferences(QueryStatement query) {
    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
    searchParameters.setMaxPermissionChecks(0);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.addStore(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE);
    searchParameters.setMaxItems(1); // We only need at least one
    searchParameters.setQuery(queryBuilder.build(query));

    ResultSet result = searchService.query(searchParameters);
    return result.getNumberFound() > 0;
  }
}
