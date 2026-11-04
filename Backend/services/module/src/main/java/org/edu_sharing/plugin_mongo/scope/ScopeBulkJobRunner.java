package org.edu_sharing.plugin_mongo.scope;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.metadataset.v2.tools.MetadataHelper;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.jobs.annotations.Queued;
import org.edu_sharing.repository.server.tools.ApplicationInfoList;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.permission.PermissionService;
import org.edu_sharing.service.scope.*;
import org.edu_sharing.service.search.SearchServiceElastic;
import org.edu_sharing.service.search.model.SearchToken;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Executes scope bulk jobs (A4). Queued: runs in the background as the user who started the job, one job after
 * another. Permissions (membership, toolpermission) are checked by {@link MongoScopeService#startBulk}; annotations
 * would not be enforced inside a queued job, Read is checked here per node.
 */
@Slf4j
@Component
public class ScopeBulkJobRunner {

    private final ScopeBulkJobRepository bulkJobRepository;
    private final ScopeAssignmentRepository assignmentRepository;
    private final ScopeChangeRepository changeRepository;
    private final MongoOperations mongoOperations;
    private final ScopeDefinitionService definitionService;
    private final NodeService nodeService;
    private final PermissionService permissionService;
    private final SearchServiceElastic searchService;
    private final ScopesSettings scopesSettings;
    private final ScopeContextResolver scopeContextResolver;

    public ScopeBulkJobRunner(ScopeBulkJobRepository bulkJobRepository,
                              ScopeAssignmentRepository assignmentRepository,
                              ScopeChangeRepository changeRepository,
                              MongoOperations mongoOperations,
                              ScopeDefinitionService definitionService,
                              @Qualifier("nodeService") NodeService nodeService,
                              @Qualifier("permissionService") PermissionService permissionService,
                              SearchServiceElastic searchService,
                              ScopesSettings scopesSettings,
                              ScopeContextResolver scopeContextResolver) {
        this.bulkJobRepository = bulkJobRepository;
        this.assignmentRepository = assignmentRepository;
        this.changeRepository = changeRepository;
        this.mongoOperations = mongoOperations;
        this.definitionService = definitionService;
        this.nodeService = nodeService;
        this.permissionService = permissionService;
        this.searchService = searchService;
        this.scopesSettings = scopesSettings;
        this.scopeContextResolver = scopeContextResolver;
    }

    @Queued(group = "scope-bulk")
    public void run(String jobId) {
        MongoScopeBulkJob job = bulkJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.warn("scope bulk job {} not found", jobId);
            return;
        }
        try {
            job.setState(ScopeBulkStatus.State.RUNNING);
            job.setFailed(new ArrayList<>());
            bulkJobRepository.save(job);

            ScopeDefinition definition = definitionService.getDefinition(job.getScopeId())
                    .orElseThrow(() -> new ScopeNotFoundException("unknown scope " + job.getScopeId()));
            List<String> nodeIds = resolveNodeIds(job.getScopeId(), job.getRequest());
            job.setTotal(nodeIds.size());
            bulkJobRepository.save(job);

            int partitionSize = Math.max(1, scopesSettings.getBulk().getPartitionSize());
            for (int i = 0; i < nodeIds.size(); i += partitionSize) {
                List<String> partition = nodeIds.subList(i, Math.min(i + partitionSize, nodeIds.size()));
                processPartition(job, definition, partition);
                job.setDone(job.getDone() + partition.size());
                bulkJobRepository.save(job);
            }
            job.setState(ScopeBulkStatus.State.FINISHED);
        } catch (Throwable t) {
            log.error("scope bulk job {} failed: {}", jobId, t.getMessage(), t);
            job.getFailed().add(new ScopeBulkStatus.Failure(null, t.getMessage()));
            job.setState(ScopeBulkStatus.State.FAILED);
        }
        job.setFinishedAt(new Date());
        bulkJobRepository.save(job);
        log.info("scope bulk job {} {}: {}/{} nodes, {} failed", jobId, job.getState(), job.getDone(), job.getTotal(), job.getFailed().size());
    }

    /**
     * the search is resolved here, with the permissions of the user who started the job and inside the scope of the
     * job: the queued thread has no request or session, without it the search would ignore the scope filter and the
     * work sets (virtual:scope_view) and hit the whole repository
     */
    private List<String> resolveNodeIds(String scopeId, ScopeBulkRequest request) throws Throwable {
        if (request.getNodeIds() != null) {
            return request.getNodeIds().stream().distinct().toList();
        }
        ScopeBaseQuery.Query search = request.getSearch();
        Map<String, String[]> criteria = new HashMap<>();
        if (search.getCriteria() != null) {
            search.getCriteria().forEach((key, values) -> criteria.put(key, values.toArray(new String[0])));
        }
        SearchToken token = new SearchToken();
        token.setFacets(new ArrayList<>());
        token.setExcludes(Arrays.asList("preview", "collections", "children"));
        return scopeContextResolver.runWithScope(scopeId, () -> searchAll(search, criteria, token));
    }

    /**
     * all hits of the search (node ids), overridable for tests
     */
    protected List<String> searchAll(ScopeBaseQuery.Query search, Map<String, String[]> criteria, SearchToken token) throws Exception {
        try {
            return searchService.searchAll(
                            MetadataHelper.getMetadataset(ApplicationInfoList.getHomeRepository(), StringUtils.defaultIfBlank(search.getMds(), CCConstants.metadatasetdefault_id)),
                            search.getQuery(), criteria, token).stream()
                    .map(org.edu_sharing.service.model.NodeRef::getNodeId)
                    .distinct()
                    .toList();
        } catch (Exception e) {
            throw e;
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    private void processPartition(MongoScopeBulkJob job, ScopeDefinition definition, List<String> nodeIds) {
        ScopeBulkRequest request = job.getRequest();
        String scopeId = job.getScopeId();
        String actor = "bulk:" + job.getJobId();
        Date now = new Date();

        // Read on the original, per node
        Map<String, String> originals = new LinkedHashMap<>();
        for (String nodeId : nodeIds) {
            try {
                String originalId = nodeService.getOriginalNode(nodeId).getId();
                if (!permissionService.hasPermission(StoreRef.PROTOCOL_WORKSPACE, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), originalId, CCConstants.PERMISSION_READ)) {
                    job.getFailed().add(new ScopeBulkStatus.Failure(nodeId, "no read permission"));
                    continue;
                }
                originals.putIfAbsent(originalId, nodeId);
            } catch (Throwable t) {
                job.getFailed().add(new ScopeBulkStatus.Failure(nodeId, t.getMessage()));
            }
        }
        if (originals.isEmpty()) {
            return;
        }
        Map<String, MongoScopeAssignment> existing = assignmentRepository.findAllByScopeIdAndNodeIdIn(scopeId, originals.keySet()).stream()
                .collect(Collectors.toMap(MongoScopeAssignment::getNodeId, Function.identity()));
        Long baseQueryVersion = definition.getBaseQuery() == null ? null : definition.getBaseQuery().getVersion();

        BulkOperations assignmentOps = mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED, MongoScopeAssignment.class);
        List<MongoScopeHistoryEntry> history = new ArrayList<>();
        List<String> changed = new ArrayList<>();
        for (String originalId : originals.keySet()) {
            MongoScopeAssignment assignment = existing.get(originalId);
            String contentVersion = getContentVersion(originalId);
            if (request.isDelete()) {
                if (assignment == null) {
                    continue;
                }
                assignmentOps.remove(Query.query(Criteria.where("_id").is(assignment.getId())));
                history.add(historyEntry(scopeId, originalId, null, null, null, actor, now, request.getReason(), baseQueryVersion, contentVersion));
                changed.add(originalId);
                continue;
            }
            Map<String, List<String>> before = assignment == null ? Map.of() : assignment.getProperties();
            Map<String, List<String>> after = ScopeAssignmentRules.merge(before, request.getProperties(), request.getAppendProperties(), request.getRemoveValues());
            List<ScopeAssignmentRules.PropertyChange> changes = ScopeAssignmentRules.diff(before, after);
            if (assignment != null && changes.isEmpty()) {
                continue;
            }
            if (assignment == null) {
                assignment = MongoScopeAssignment.builder()
                        .id(MongoScopeAssignment.createId(scopeId, originalId))
                        .scopeId(scopeId)
                        .nodeId(originalId)
                        .source(ScopeSource.BULK)
                        .created(now)
                        .createdBy(job.getStartedBy())
                        .build();
            }
            assignment.setProperties(after);
            assignment.setPublished(ScopeAssignmentRules.isPublished(definition, after));
            assignment.setExcluded(ScopeAssignmentRules.isExcluded(definition, after));
            assignment.setModified(now);
            assignment.setModifiedBy(job.getStartedBy());
            assignmentOps.replaceOne(Query.query(Criteria.where("_id").is(assignment.getId())), assignment,
                    org.springframework.data.mongodb.core.FindAndReplaceOptions.options().upsert());
            for (ScopeAssignmentRules.PropertyChange change : changes) {
                history.add(historyEntry(scopeId, originalId, change.property(), change.from(), change.to(), actor, now, request.getReason(), baseQueryVersion, contentVersion));
            }
            changed.add(originalId);
        }
        if (changed.isEmpty()) {
            return;
        }
        assignmentOps.execute();
        changed.forEach(nodeId -> ScopePublishedCache.invalidate(scopeId, nodeId));
        mongoOperations.insert(history, MongoScopeHistoryEntry.class);
        changeRepository.append(scopeId, changed, request.isDelete() ? ScopeChangeType.DELETE : ScopeChangeType.UPSERT);
    }

    private static MongoScopeHistoryEntry historyEntry(String scopeId, String nodeId, String property, List<String> from, List<String> to,
                                                       String actor, Date at, String reason, Long baseQueryVersion, String contentVersion) {
        return MongoScopeHistoryEntry.builder()
                .scopeId(scopeId)
                .nodeId(nodeId)
                .property(property)
                .from(from)
                .to(to)
                .actor(actor)
                .at(at)
                .reason(reason)
                .baseQueryVersion(baseQueryVersion)
                .contentVersion(contentVersion)
                .build();
    }

    private String getContentVersion(String originalId) {
        return AuthenticationUtil.runAsSystem(() -> nodeService.getProperty(StoreRef.PROTOCOL_WORKSPACE,
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), originalId, CCConstants.CM_PROP_VERSIONABLELABEL));
    }
}
