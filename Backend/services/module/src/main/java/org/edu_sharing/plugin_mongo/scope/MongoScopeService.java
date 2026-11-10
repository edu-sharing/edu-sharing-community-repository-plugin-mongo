package org.edu_sharing.plugin_mongo.scope;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.metadataset.v2.MetadataKey;
import org.edu_sharing.metadataset.v2.MetadataSet;
import org.edu_sharing.metadataset.v2.MetadataWidget;
import org.edu_sharing.metadataset.v2.tools.MetadataHelper;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.tools.ApplicationInfoList;
import org.edu_sharing.service.InsufficientPermissionException;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.permission.PermissionService;
import org.edu_sharing.service.permission.annotation.Permission;
import org.edu_sharing.service.scope.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scope entries in mongo (A2/A3). The original node is never modified.
 * Registered as bean "scopeService" in plugin-mongo-services.xml (overrides the core adapter).
 */
@Slf4j
@ScopesFeature
@RequiredArgsConstructor
public class MongoScopeService implements ScopeService {

    /**
     * changes younger than this are not handed out yet: an entry is inserted with a timestamp taken shortly before
     * the insert, so a concurrent writer could otherwise become visible behind the tracker's cursor (ADR-1)
     */
    static final long CHANGE_FEED_SAFETY_LAG_MS = 2000;
    /** entries per change feed entry when the field list of a scope changed */
    static final int DEFINITION_CHANGE_BATCH = 1000;

    private final ScopeAssignmentRepository assignmentRepository;
    private final ScopeHistoryRepository historyRepository;
    private final ScopeChangeRepository changeRepository;
    private final ScopeBulkJobRepository bulkJobRepository;
    private final ScopeDefinitionService definitionService;
    private final NodeService nodeService;
    private final PermissionService permissionService;
    private final DictionaryService dictionaryService;
    private final ScopeBulkJobRunner bulkJobRunner;

    // --- read

    @Override
    public ScopeAssignment getAssignment(String scopeId, String nodeId) {
        String originalId = checkReadAccess(scopeId, nodeId);
        return assignmentRepository.findByScopeIdAndNodeId(scopeId, originalId)
                .map(MongoScopeAssignment::toAssignment)
                .orElse(null);
    }

    @Override
    public List<ScopeHistoryEntry> getHistory(String scopeId, String nodeId) {
        String originalId = checkReadAccess(scopeId, nodeId);
        return historyRepository.findAllByScopeIdAndNodeIdOrderByAtDesc(scopeId, originalId).stream()
                .map(MongoScopeHistoryEntry::toHistoryEntry)
                .toList();
    }

    @Override
    @Permission(requiresGlobalAdmin = true)
    public ScopeIndexData getIndexData(String nodeId) {
        // both variants are annotated: this self-invocation bypasses the PermissionChecking proxy
        return getIndexData(List.of(nodeId)).get(nodeId);
    }

    @Override
    @Permission(requiresGlobalAdmin = true)
    public Map<String, ScopeIndexData> getIndexData(Collection<String> nodeIds) {
        // child objects inherit everything from their parent, published copies/references belong to the original
        Map<String, String> sourceIds = AuthenticationUtil.runAsSystem(() -> {
            Map<String, String> result = new LinkedHashMap<>();
            for (String nodeId : nodeIds) {
                result.put(nodeId, resolveIndexSource(nodeId));
            }
            return result;
        });
        Map<String, List<MongoScopeAssignment>> assignmentsBySource = assignmentRepository.findAllByNodeIdIn(new HashSet<>(sourceIds.values())).stream()
                .collect(Collectors.groupingBy(MongoScopeAssignment::getNodeId));
        Map<String, Optional<ScopeDefinition>> definitions = new HashMap<>();

        Map<String, ScopeIndexData> result = new LinkedHashMap<>();
        sourceIds.forEach((nodeId, sourceId) -> {
            List<MongoScopeAssignment> assignments = assignmentsBySource.getOrDefault(sourceId, List.of());
            ScopeIndexData data = ScopeIndexData.builder()
                    .nodeId(nodeId)
                    .scopeIds(new ArrayList<>())
                    .scopeIdsPublished(new ArrayList<>())
                    .scopeIdsExcluded(new ArrayList<>())
                    .scopeOverrides(new ArrayList<>())
                    .scopes(new ArrayList<>())
                    .build();
            for (MongoScopeAssignment assignment : assignments) {
                Optional<ScopeDefinition> definition = definitions.computeIfAbsent(assignment.getScopeId(), definitionService::getDefinition);
                if (definition.isEmpty()) {
                    // scope group is gone, its cleanup (SCOPE_DELETED) is pending
                    continue;
                }
                data.getScopeIds().add(assignment.getScopeId());
                if (assignment.isPublished()) {
                    data.getScopeIdsPublished().add(assignment.getScopeId());
                }
                if (assignment.isExcluded()) {
                    data.getScopeIdsExcluded().add(assignment.getScopeId());
                }
                data.getScopeOverrides().addAll(ScopeAssignmentRules.overrides(definition.get(), assignment.getProperties()));
                data.getScopes().add(ScopeIndexData.Entry.builder()
                        .scopeId(assignment.getScopeId())
                        .published(assignment.isPublished())
                        .excluded(assignment.isExcluded())
                        .created(assignment.getCreated())
                        .createdBy(assignment.getCreatedBy())
                        .modified(assignment.getModified())
                        .modifiedBy(assignment.getModifiedBy())
                        .properties(assignment.getProperties())
                        .publicFields(ScopeAssignmentRules.publicFields(definition.get()))
                        .build());
            }
            result.put(nodeId, data);
        });
        return result;
    }

    private String resolveIndexSource(String nodeId) {
        String sourceId = nodeId;
        if (nodeService.hasAspect(StoreRef.PROTOCOL_WORKSPACE, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), nodeId, CCConstants.CCM_ASPECT_IO_CHILDOBJECT)) {
            sourceId = nodeService.getPrimaryParent(nodeId);
        }
        return nodeService.getOriginalNode(sourceId).getId();
    }

    @Override
    @Permission(requiresGlobalAdmin = true)
    public List<ScopeChange> getChanges(Date after, Long afterSeq, Date until, int maxItems) {
        Date latest = new Date(System.currentTimeMillis() - CHANGE_FEED_SAFETY_LAG_MS);
        Date effectiveUntil = until == null || until.after(latest) ? latest : until;
        return changeRepository.findChanges(after, afterSeq, effectiveUntil, maxItems).stream()
                .map(MongoScopeChange::toChange)
                .toList();
    }

    @Override
    public boolean isPublishedInScope(String scopeId, String nodeId) {
        // permission check helper, the caller resolves the original node
        return assignmentRepository.existsByScopeIdAndNodeIdAndPublishedTrue(scopeId, nodeId);
    }

    // --- write

    @Override
    @Permission(value = CCConstants.CCM_VALUE_TOOLPERMISSION_SCOPE_WRITE, requiresUser = true)
    public ScopeAssignment setProperties(String scopeId, String nodeId, Map<String, List<String>> properties, String reason) {
        ScopeDefinition definition = getDefinition(scopeId);
        String originalId = checkWriteAccess(scopeId, nodeId);
        ScopeAssignmentRules.validate(definition, properties, this::isMultiValued, property -> getAllowedValues(definition, property));

        Optional<MongoScopeAssignment> existing = assignmentRepository.findByScopeIdAndNodeId(scopeId, originalId);
        Map<String, List<String>> before = existing.map(MongoScopeAssignment::getProperties).orElse(Map.of());
        Map<String, List<String>> after = ScopeAssignmentRules.merge(before, properties);
        List<ScopeAssignmentRules.PropertyChange> changes = ScopeAssignmentRules.diff(before, after);
        if (existing.isPresent() && changes.isEmpty()) {
            return existing.get().toAssignment();
        }

        String actor = AuthenticationUtil.getFullyAuthenticatedUser();
        Date now = new Date();
        MongoScopeAssignment assignment = existing.orElseGet(() -> MongoScopeAssignment.builder()
                .id(MongoScopeAssignment.createId(scopeId, originalId))
                .scopeId(scopeId)
                .nodeId(originalId)
                .source(ScopeSource.USER)
                .created(now)
                .createdBy(actor)
                .build());
        assignment.setProperties(after);
        assignment.setPublished(ScopeAssignmentRules.isPublished(definition, after));
        assignment.setExcluded(ScopeAssignmentRules.isExcluded(definition, after));
        assignment.setModified(now);
        assignment.setModifiedBy(actor);
        assignmentRepository.save(assignment);
        ScopePublishedCache.invalidate(scopeId, originalId);

        Long baseQueryVersion = definition.getBaseQuery() == null ? null : definition.getBaseQuery().getVersion();
        String contentVersion = getContentVersion(originalId);
        historyRepository.saveAll(changes.stream()
                .map(change -> MongoScopeHistoryEntry.builder()
                        .scopeId(scopeId)
                        .nodeId(originalId)
                        .property(change.property())
                        .from(change.from())
                        .to(change.to())
                        .actor(actor)
                        .at(now)
                        .reason(reason)
                        .baseQueryVersion(baseQueryVersion)
                        .contentVersion(contentVersion)
                        .build())
                .toList());
        // after the entry is stored: the tracker reads the current state when it handles the change
        changeRepository.append(scopeId, List.of(originalId), ScopeChangeType.UPSERT);
        return assignment.toAssignment();
    }

    @Override
    @Permission(value = CCConstants.CCM_VALUE_TOOLPERMISSION_SCOPE_WRITE, requiresUser = true)
    public void deleteAssignment(String scopeId, String nodeId, String reason) {
        ScopeDefinition definition = getDefinition(scopeId);
        String originalId = checkWriteAccess(scopeId, nodeId);
        MongoScopeAssignment existing = assignmentRepository.findByScopeIdAndNodeId(scopeId, originalId)
                .orElseThrow(() -> new ScopeNotFoundException("no entry for node " + nodeId + " in scope " + scopeId));
        assignmentRepository.delete(existing);
        ScopePublishedCache.invalidate(scopeId, originalId);
        historyRepository.save(MongoScopeHistoryEntry.builder()
                .scopeId(scopeId)
                .nodeId(originalId)
                .property(null)
                .from(null)
                .to(null)
                .actor(AuthenticationUtil.getFullyAuthenticatedUser())
                .at(new Date())
                .reason(reason)
                .baseQueryVersion(definition.getBaseQuery() == null ? null : definition.getBaseQuery().getVersion())
                .contentVersion(getContentVersion(originalId))
                .build());
        changeRepository.append(scopeId, List.of(originalId), ScopeChangeType.DELETE);
    }

    /**
     * the toolpermission is checked when the job is started (call through the proxy), the queued job itself writes
     * without annotation checks
     */
    @Override
    @Permission(value = CCConstants.CCM_VALUE_TOOLPERMISSION_SCOPE_WRITE, requiresUser = true)
    public String startBulk(String scopeId, ScopeBulkRequest request) {
        ScopeDefinition definition = getDefinition(scopeId);
        checkMember(scopeId);
        if (request == null || (request.getNodeIds() == null) == (request.getSearch() == null)) {
            throw new IllegalArgumentException("exactly one of nodeIds or search must be set");
        }
        if (request.getFolderId() != null || request.isCreateOnly()) {
            throw new IllegalArgumentException("folderId and createOnly are internal");
        }
        if (!request.isDelete()) {
            boolean removes = request.getRemoveValues() != null && !request.getRemoveValues().isEmpty();
            if ((request.getProperties() == null || request.getProperties().isEmpty()) && !removes) {
                throw new IllegalArgumentException("properties or removeValues must be set");
            }
            if (request.getProperties() != null) {
                ScopeAssignmentRules.validate(definition, request.getProperties(), this::isMultiValued, property -> getAllowedValues(definition, property));
            }
            if (removes) {
                ScopeAssignmentRules.validate(definition, request.getRemoveValues(), property -> true, property -> null);
            }
            if (request.getAppendProperties() != null) {
                for (String property : request.getAppendProperties()) {
                    if (request.getProperties() == null || !request.getProperties().containsKey(property) || !isMultiValued(property)) {
                        throw new IllegalArgumentException("only requested multi value properties can be appended: " + property);
                    }
                }
            }
        }
        MongoScopeBulkJob job = bulkJobRepository.save(MongoScopeBulkJob.builder()
                .jobId(UUID.randomUUID().toString())
                .scopeId(scopeId)
                .state(ScopeBulkStatus.State.QUEUED)
                .failed(new ArrayList<>())
                .startedBy(AuthenticationUtil.getFullyAuthenticatedUser())
                .startedAt(new Date())
                .request(request)
                .build());
        // queued: returns immediately, runs as the current user
        bulkJobRunner.run(job.getJobId());
        return job.getJobId();
    }

    @Override
    @Permission(requiresGlobalAdmin = true)
    public String startFolderImport(String scopeId) {
        ScopeDefinition definition = getDefinition(scopeId);
        if (definition.getFolderId() == null) {
            return null;
        }
        ScopeBulkRequest request = ScopeBulkRequest.builder()
                .folderId(definition.getFolderId())
                .properties(ScopeAssignmentRules.uploadProperties(definition))
                .createOnly(true)
                .reason("organization converted to scope")
                .build();
        MongoScopeBulkJob job = bulkJobRepository.save(MongoScopeBulkJob.builder()
                .jobId(UUID.randomUUID().toString())
                .scopeId(scopeId)
                .state(ScopeBulkStatus.State.QUEUED)
                .failed(new ArrayList<>())
                .startedBy(AuthenticationUtil.getFullyAuthenticatedUser())
                .startedAt(new Date())
                .request(request)
                .build());
        // queued: returns immediately, runs as the current user (admin, Read on every node)
        bulkJobRunner.run(job.getJobId());
        return job.getJobId();
    }

    @Override
    public ScopeBulkStatus getBulkStatus(String scopeId, String jobId) {
        checkMember(scopeId);
        return bulkJobRepository.findByJobIdAndScopeId(jobId, scopeId)
                .map(job -> {
                    ScopeBulkStatus status = job.toStatus();
                    if (job.getState() == ScopeBulkStatus.State.QUEUED) {
                        // not started yet: own counters stay 0, only the jobs ahead are reported
                        status.setTotal(0);
                        status.setDone(0);
                        if (job.getStartedAt() != null) {
                            status.setQueue(getQueueAhead(job.getStartedAt()));
                        }
                    }
                    return status;
                })
                .orElse(null);
    }

    /**
     * progress of the jobs that run before a queued one (all scopes: one queue for all bulk jobs)
     */
    private ScopeBulkStatus.Queue getQueueAhead(Date startedAt) {
        List<MongoScopeBulkJob> ahead = bulkJobRepository.findAllByStateInAndStartedAtBefore(
                List.of(ScopeBulkStatus.State.QUEUED, ScopeBulkStatus.State.RUNNING), startedAt);
        return ScopeBulkStatus.Queue.builder()
                .jobsAhead(ahead.size())
                .done(ahead.stream().mapToLong(MongoScopeBulkJob::getDone).sum())
                .total(ahead.stream().mapToLong(MongoScopeBulkJob::getTotal).sum())
                .build();
    }

    // --- lifecycle

    @Override
    public void onUpload(String scopeId, String nodeId, String uploader) {
        ScopeDefinition definition = getDefinition(scopeId);
        if (assignmentRepository.findByScopeIdAndNodeId(scopeId, nodeId).isPresent()) {
            return;
        }
        Map<String, List<String>> properties = ScopeAssignmentRules.uploadProperties(definition);
        Date now = new Date();
        MongoScopeAssignment assignment = MongoScopeAssignment.builder()
                .id(MongoScopeAssignment.createId(scopeId, nodeId))
                .scopeId(scopeId)
                .nodeId(nodeId)
                .properties(properties)
                .published(ScopeAssignmentRules.isPublished(definition, properties))
                .excluded(ScopeAssignmentRules.isExcluded(definition, properties))
                .source(ScopeSource.UPLOAD)
                .created(now)
                .createdBy(uploader)
                .modified(now)
                .modifiedBy(uploader)
                .build();
        assignmentRepository.save(assignment);
        ScopePublishedCache.invalidate(scopeId, nodeId);
        Long baseQueryVersion = definition.getBaseQuery() == null ? null : definition.getBaseQuery().getVersion();
        String contentVersion = getContentVersion(nodeId);
        historyRepository.saveAll(ScopeAssignmentRules.diff(Map.of(), properties).stream()
                .map(change -> MongoScopeHistoryEntry.builder()
                        .scopeId(scopeId)
                        .nodeId(nodeId)
                        .property(change.property())
                        .from(change.from())
                        .to(change.to())
                        .actor(uploader)
                        .at(now)
                        .reason("upload")
                        .baseQueryVersion(baseQueryVersion)
                        .contentVersion(contentVersion)
                        .build())
                .toList());
        changeRepository.append(scopeId, List.of(nodeId), ScopeChangeType.UPSERT);
    }

    @Override
    public void onIoPurged(String nodeId) {
        assignmentRepository.findAllByNodeId(nodeId).forEach(assignment -> ScopePublishedCache.invalidate(assignment.getScopeId(), nodeId));
        long assignments = assignmentRepository.deleteAllByNodeId(nodeId);
        long history = historyRepository.deleteAllByNodeId(nodeId);
        if (assignments > 0) {
            changeRepository.append(null, List.of(nodeId), ScopeChangeType.DELETE);
        }
        log.debug("node {} purged: removed {} scope entries, {} history entries", nodeId, assignments, history);
    }

    @Override
    public void onScopeGroupDeleted(String scopeId) {
        long assignments = assignmentRepository.deleteAllByScopeId(scopeId);
        long history = historyRepository.deleteAllByScopeId(scopeId);
        bulkJobRepository.deleteAllByScopeId(scopeId);
        ScopePublishedCache.clear();
        changeRepository.append(scopeId, List.of(), ScopeChangeType.SCOPE_DELETED);
        log.info("scope group {} deleted: removed {} scope entries, {} history entries", scopeId, assignments, history);
    }

    @Override
    public void onDefinitionChanged(String scopeId) {
        ScopeDefinition definition = getDefinition(scopeId);
        List<MongoScopeAssignment> changed = new ArrayList<>();
        List<String> nodeIds = new ArrayList<>();
        long total = 0;
        long updated = 0;
        try (Stream<MongoScopeAssignment> assignments = assignmentRepository.streamAllByScopeId(scopeId)) {
            for (MongoScopeAssignment assignment : (Iterable<MongoScopeAssignment>) assignments::iterator) {
                boolean published = ScopeAssignmentRules.isPublished(definition, assignment.getProperties());
                boolean excluded = ScopeAssignmentRules.isExcluded(definition, assignment.getProperties());
                if (published != assignment.isPublished() || excluded != assignment.isExcluded()) {
                    assignment.setPublished(published);
                    assignment.setExcluded(excluded);
                    changed.add(assignment);
                }
                nodeIds.add(assignment.getNodeId());
                total++;
                if (nodeIds.size() >= DEFINITION_CHANGE_BATCH) {
                    updated += flushDefinitionChange(scopeId, changed, nodeIds);
                }
            }
        }
        updated += flushDefinitionChange(scopeId, changed, nodeIds);
        ScopePublishedCache.clear();
        log.info("scope {} definition changed: {} of {} entries changed published/excluded", scopeId, updated, total);
    }

    /**
     * saves the changed entries and queues all entries of the batch for the index
     */
    private long flushDefinitionChange(String scopeId, List<MongoScopeAssignment> changed, List<String> nodeIds) {
        long count = changed.size();
        if (!changed.isEmpty()) {
            assignmentRepository.saveAll(changed);
        }
        if (!nodeIds.isEmpty()) {
            changeRepository.append(scopeId, new ArrayList<>(nodeIds), ScopeChangeType.UPSERT);
        }
        changed.clear();
        nodeIds.clear();
        return count;
    }

    // --- helpers

    private ScopeDefinition getDefinition(String scopeId) {
        return definitionService.getDefinition(scopeId)
                .orElseThrow(() -> new ScopeNotFoundException("unknown scope " + scopeId));
    }

    private void checkMember(String scopeId) {
        getDefinition(scopeId);
        if (!permissionService.isAdminOrSystem() && !definitionService.isMember(scopeId)) {
            throw new InsufficientPermissionException("not a member of scope " + scopeId);
        }
    }

    /**
     * membership and Read on the original (concept §5.5). CCPublish is not required
     *
     * @return the id of the original node
     */
    private String checkReadAccess(String scopeId, String nodeId) {
        checkMember(scopeId);
        String originalId = nodeService.getOriginalNode(nodeId).getId();
        if (!permissionService.hasPermission(StoreRef.PROTOCOL_WORKSPACE, StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), originalId, CCConstants.PERMISSION_READ)) {
            throw new InsufficientPermissionException("no read permission on node " + originalId);
        }
        return originalId;
    }

    private String checkWriteAccess(String scopeId, String nodeId) {
        // the toolpermission is checked via @Permission, writing an entry only needs Read on the original
        return checkReadAccess(scopeId, nodeId);
    }

    private boolean isMultiValued(String property) {
        PropertyDefinition definition = dictionaryService.getProperty(QName.createQName(CCConstants.getValidGlobalName(property)));
        if (definition == null) {
            throw new IllegalArgumentException(property + " is not known by the dictionary");
        }
        return definition.isMultiValued();
    }

    /**
     * @return the fixed values of the widget of the property in the scope mds, null if there is no fixed valuespace
     */
    protected Collection<String> getAllowedValues(ScopeDefinition definition, String property) {
        try {
            MetadataSet mds = MetadataHelper.getMetadataset(ApplicationInfoList.getHomeRepository(),
                    StringUtils.defaultIfBlank(definition.getMds(), CCConstants.metadatasetdefault_id));
            MetadataWidget widget = mds.findWidget(property);
            if (widget == null || widget.getValues() == null || widget.getValues().isEmpty()) {
                return null;
            }
            return widget.getValues().stream().map(MetadataKey::getKey).collect(Collectors.toSet());
        } catch (Exception e) {
            throw new IllegalStateException("could not load mds " + definition.getMds() + " of scope " + definition.getScopeId(), e);
        }
    }

    private String getContentVersion(String originalId) {
        return AuthenticationUtil.runAsSystem(() -> nodeService.getProperty(StoreRef.PROTOCOL_WORKSPACE,
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), originalId, CCConstants.CM_PROP_VERSIONABLELABEL));
    }
}
