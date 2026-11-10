package org.edu_sharing.plugin_mongo.scope;

import org.edu_sharing.service.permission.annotation.Permission;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.edu_sharing.plugin_mongo.util.AbstractMongoDbContainerTest;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.InsufficientPermissionException;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.permission.PermissionService;
import org.edu_sharing.service.scope.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Testcontainers
class MongoScopeServiceTest extends AbstractMongoDbContainerTest {

    static final String SCOPE = "GROUP_ORG_wlo";
    static final String WORKSPACE = StoreRef.PROTOCOL_WORKSPACE;
    static final String STORE = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier();

    private ScopeAssignmentRepository assignments;
    private ScopeHistoryRepository history;
    private ScopeChangeRepository changes;
    private ScopeDefinitionService definitionService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private MongoScopeService underTest;
    private MockedStatic<AuthenticationUtil> authenticationUtil;
    private ScopeBulkJobRunner bulkJobRunner;
    private ScopeBulkJobRepository bulkJobs;

    @BeforeEach
    void setUp() {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setAutoIndexCreation(true);
        mappingContext.afterPropertiesSet();
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(dbFactory), mappingContext);
        converter.afterPropertiesSet();
        MongoTemplate template = new MongoTemplate(dbFactory, converter);
        MongoRepositoryFactory factory = new MongoRepositoryFactory(template);
        assignments = factory.getRepository(ScopeAssignmentRepository.class);
        history = factory.getRepository(ScopeHistoryRepository.class);
        changes = factory.getRepository(ScopeChangeRepository.class,
                RepositoryComposition.RepositoryFragments.of(RepositoryFragment.implemented(CustomScopeChangeRepository.class, new CustomScopeChangeRepositoryImpl(template))));
        bulkJobs = factory.getRepository(ScopeBulkJobRepository.class);
        bulkJobRunner = mock(ScopeBulkJobRunner.class);

        ScopeDefinition definition = ScopeDefinition.builder()
                .scopeId(SCOPE)
                .fields(ScopeAssignmentRulesTest.DEFINITION.getFields())
                .baseQuery(ScopeBaseQuery.builder().version(3).queries(List.of()).build())
                .build();
        definitionService = mock(ScopeDefinitionService.class);
        when(definitionService.getDefinition(anyString())).thenReturn(Optional.empty());
        when(definitionService.getDefinition(SCOPE)).thenReturn(Optional.of(definition));
        when(definitionService.isMember(SCOPE)).thenReturn(true);

        nodeService = mock(NodeService.class);
        when(nodeService.getOriginalNode(anyString())).thenAnswer(i -> new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, i.getArgument(0)));
        // published copy -> original
        when(nodeService.getOriginalNode("copy1")).thenReturn(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "n1"));
        when(nodeService.hasAspect(WORKSPACE, STORE, "child1", CCConstants.CCM_ASPECT_IO_CHILDOBJECT)).thenReturn(true);
        when(nodeService.getPrimaryParent("child1")).thenReturn("n1");
        when(nodeService.getProperty(eq(WORKSPACE), eq(STORE), anyString(), eq(CCConstants.CM_PROP_VERSIONABLELABEL))).thenReturn("1.4");

        permissionService = mock(PermissionService.class);
        when(permissionService.hasPermission(eq(WORKSPACE), eq(STORE), anyString(), eq(CCConstants.PERMISSION_READ))).thenReturn(true);
        when(permissionService.hasPermission(WORKSPACE, STORE, "secret", CCConstants.PERMISSION_READ)).thenReturn(false);

        DictionaryService dictionaryService = mock(DictionaryService.class);
        when(dictionaryService.getProperty(any(QName.class))).thenAnswer(i -> {
            PropertyDefinition property = mock(PropertyDefinition.class);
            when(property.isMultiValued()).thenReturn(i.getArgument(0).equals(QName.createQName(CCConstants.CCM_PROP_EDITORIAL_INTERNAL)));
            return property;
        });

        underTest = new MongoScopeService(assignments, history, changes, bulkJobs, definitionService, nodeService, permissionService, dictionaryService, bulkJobRunner) {
            @Override
            protected Collection<String> getAllowedValues(ScopeDefinition definition, String property) {
                return switch (property) {
                    case "ccm:editorial_inventory" -> Set.of("proposed", "accepted", "rejected");
                    case "ccm:editorial_release" -> Set.of("not_released", "released", "withdrawn");
                    default -> null;
                };
            }
        };
        authenticationUtil = Mockito.mockStatic(AuthenticationUtil.class);
        authenticationUtil.when(AuthenticationUtil::getFullyAuthenticatedUser).thenReturn("editor");
        authenticationUtil.when(() -> AuthenticationUtil.runAsSystem(any()))
                .thenAnswer(i -> ((AuthenticationUtil.RunAsWork<?>) i.getArgument(0)).doWork());
    }

    @AfterEach
    void tearDown() {
        authenticationUtil.close();
    }

    @Test
    void setPropertiesCreatesEntryHistoryAndChange() {
        ScopeAssignment result = underTest.setProperties(SCOPE, "n1", Map.of(
                "ccm:editorial_inventory", List.of("accepted"),
                "ccm:editorial_release", List.of("released")), "first review");

        assertTrue(result.isPublished());
        assertFalse(result.isExcluded());
        assertEquals(ScopeSource.USER, result.getSource());
        assertEquals("editor", result.getCreatedBy());

        List<ScopeHistoryEntry> entries = underTest.getHistory(SCOPE, "n1");
        assertEquals(2, entries.size());
        ScopeHistoryEntry release = entries.stream().filter(e -> e.getProperty().equals("ccm:editorial_release")).findFirst().orElseThrow();
        assertEquals(List.of(), release.getFrom());
        assertEquals(List.of("released"), release.getTo());
        assertEquals("editor", release.getActor());
        assertEquals("first review", release.getReason());
        assertEquals(3L, release.getBaseQueryVersion());
        assertEquals("1.4", release.getContentVersion());

        List<MongoScopeChange> feed = changes.findChanges(null, null, null, 10);
        assertEquals(1, feed.size());
        assertEquals(ScopeChangeType.UPSERT, feed.get(0).getType());
        assertEquals(List.of("n1"), feed.get(0).getNodeIds());
    }

    @Test
    void entriesBelongToTheOriginal() {
        underTest.setProperties(SCOPE, "copy1", Map.of("ccm:editorial_release", List.of("released")), null);

        assertTrue(assignments.findByScopeIdAndNodeId(SCOPE, "n1").isPresent());
        assertTrue(underTest.isPublishedInScope(SCOPE, "n1"));
    }

    @Test
    void unchangedRequestWritesNothing() {
        underTest.setProperties(SCOPE, "n1", Map.of("ccm:editorial_release", List.of("released")), null);
        underTest.setProperties(SCOPE, "n1", Map.of("ccm:editorial_release", List.of("released")), null);

        assertEquals(1, history.count());
        assertEquals(1, changes.count());
    }

    @Test
    void onlyChangedPropertiesGoIntoTheHistory() {
        underTest.setProperties(SCOPE, "n1", Map.of("ccm:editorial_inventory", List.of("proposed"), "ccm:editorial_release", List.of("not_released")), null);
        underTest.setProperties(SCOPE, "n1", Map.of("ccm:editorial_inventory", List.of("rejected"), "ccm:editorial_release", List.of("not_released")), null);

        List<ScopeHistoryEntry> entries = underTest.getHistory(SCOPE, "n1");
        assertEquals(3, entries.size());
        ScopeAssignment assignment = underTest.getAssignment(SCOPE, "n1");
        assertTrue(assignment.isExcluded());
        assertFalse(assignment.isPublished());
    }

    @Test
    void validationAndPermissions() {
        assertThrows(IllegalArgumentException.class, () -> underTest.setProperties(SCOPE, "n1", Map.of("ccm:editorial_release", List.of("approved")), null));
        assertThrows(IllegalArgumentException.class, () -> underTest.setProperties(SCOPE, "n1", Map.of("ccm:curriculum", List.of("x")), null));
        assertThrows(ScopeNotFoundException.class, () -> underTest.setProperties("GROUP_unknown", "n1", Map.of(), null));
        assertThrows(InsufficientPermissionException.class, () -> underTest.setProperties(SCOPE, "secret", Map.of("ccm:editorial_release", List.of("released")), null));

        when(definitionService.isMember(SCOPE)).thenReturn(false);
        assertThrows(InsufficientPermissionException.class, () -> underTest.setProperties(SCOPE, "n1", Map.of("ccm:editorial_release", List.of("released")), null));
        assertThrows(InsufficientPermissionException.class, () -> underTest.getHistory(SCOPE, "n1"));
        assertEquals(0, assignments.count());
    }

    @Test
    void deleteKeepsADeletionRecord() {
        underTest.setProperties(SCOPE, "n1", Map.of("ccm:editorial_release", List.of("released")), null);
        underTest.deleteAssignment(SCOPE, "n1", "wrong scope");

        assertNull(underTest.getAssignment(SCOPE, "n1"));
        List<ScopeHistoryEntry> entries = underTest.getHistory(SCOPE, "n1");
        assertEquals(2, entries.size());
        assertTrue(entries.stream().anyMatch(e -> e.getProperty() == null && "wrong scope".equals(e.getReason())));
        assertEquals(ScopeChangeType.DELETE, changes.findChanges(null, null, null, 10).get(1).getType());
        assertThrows(ScopeNotFoundException.class, () -> underTest.deleteAssignment(SCOPE, "n1", null));
    }

    @Test
    void indexDataIncludingChildObjectsAndOverrides() {
        underTest.setProperties(SCOPE, "n1", Map.of(
                "ccm:editorial_inventory", List.of("rejected"),
                "ccm:editorial_release", List.of("released"),
                "cclom:title", List.of("Eigener Titel")), null);

        when(permissionService.isAdminOrSystem()).thenReturn(true);
        Map<String, ScopeIndexData> data = underTest.getIndexData(List.of("n1", "child1", "n2"));

        ScopeIndexData original = data.get("n1");
        assertEquals(List.of(SCOPE), original.getScopeIds());
        assertEquals(List.of(SCOPE), original.getScopeIdsPublished());
        assertEquals(List.of(SCOPE), original.getScopeIdsExcluded());
        assertEquals(List.of(SCOPE + "|cclom:title"), original.getScopeOverrides());
        assertEquals("editor", original.getScopes().get(0).getModifiedBy());
        assertEquals(List.of("Eigener Titel"), original.getScopes().get(0).getProperties().get("cclom:title"));

        // child objects carry the fragment of their parent, under their own id
        ScopeIndexData child = data.get("child1");
        assertEquals("child1", child.getNodeId());
        assertEquals(original.getScopeIds(), child.getScopeIds());
        assertEquals(original.getScopes(), child.getScopes());

        assertEquals(List.of(), data.get("n2").getScopeIds());
    }

    @Test
    void indexDataAndFeedAreAdminOnly() throws NoSuchMethodException {
        // enforced by PermissionChecking (proxy), admins and the system user pass
        for (var method : List.of(
                MongoScopeService.class.getMethod("getIndexData", String.class),
                MongoScopeService.class.getMethod("getIndexData", Collection.class),
                MongoScopeService.class.getMethod("getChanges", Date.class, Long.class, Date.class, int.class))) {
            assertTrue(method.getAnnotation(Permission.class).requiresGlobalAdmin(), method.toString());
        }
    }

    @Test
    void changeFeedHoldsBackTheNewestEntries() throws InterruptedException {
        when(permissionService.isAdminOrSystem()).thenReturn(true);
        underTest.setProperties(SCOPE, "n1", Map.of("ccm:editorial_release", List.of("released")), null);

        assertEquals(0, underTest.getChanges(null, null, null, 10).size());
        Thread.sleep(MongoScopeService.CHANGE_FEED_SAFETY_LAG_MS + 100);
        assertEquals(1, underTest.getChanges(null, null, null, 10).size());
    }

    @Test
    void purgeAndScopeDeletion() {
        underTest.setProperties(SCOPE, "n1", Map.of("ccm:editorial_release", List.of("released")), null);
        underTest.setProperties(SCOPE, "n2", Map.of("ccm:editorial_release", List.of("released")), null);

        underTest.onIoPurged("n1");
        assertTrue(assignments.findAllByNodeId("n1").isEmpty());
        assertEquals(0, history.findAllByScopeIdAndNodeIdOrderByAtDesc(SCOPE, "n1").size());

        underTest.onScopeGroupDeleted(SCOPE);
        assertEquals(0, assignments.count());
        assertEquals(0, history.count());
        List<MongoScopeChange> feed = changes.findChanges(null, null, null, 10);
        assertEquals(List.of(ScopeChangeType.UPSERT, ScopeChangeType.UPSERT, ScopeChangeType.DELETE, ScopeChangeType.SCOPE_DELETED),
                feed.stream().map(MongoScopeChange::getType).toList());
    }

    @Test
    void definitionChangeRecomputesPublished() {
        underTest.setProperties(SCOPE, "n1", Map.of("ccm:editorial_inventory", List.of("accepted")), null);
        assertFalse(underTest.getAssignment(SCOPE, "n1").isPublished());
        long feed = changes.count();

        // the inventory decision now publishes
        when(definitionService.getDefinition(SCOPE)).thenReturn(Optional.of(ScopeDefinition.builder()
                .scopeId(SCOPE)
                .fields(List.of(ScopeField.builder().id("ccm:editorial_inventory").visibility(ScopeFieldVisibility.INTERNAL)
                        .publishedValues(List.of("accepted")).build()))
                .build()));
        underTest.onDefinitionChanged(SCOPE);

        assertTrue(underTest.getAssignment(SCOPE, "n1").isPublished());
        List<MongoScopeChange> added = changes.findChanges(null, null, null, 100).subList((int) feed, (int) feed + 1);
        assertEquals(ScopeChangeType.UPSERT, added.get(0).getType());
        assertEquals(List.of("n1"), added.get(0).getNodeIds());
    }

    @Test
    void queuedJobsShowTheProgressOfTheJobsAhead() {
        bulkJobs.save(MongoScopeBulkJob.builder().jobId("running").scopeId("GROUP_ORG_other").state(ScopeBulkStatus.State.RUNNING)
                .done(3).total(10).failed(new ArrayList<>()).startedAt(new Date(1000)).build());
        bulkJobs.save(MongoScopeBulkJob.builder().jobId("finished").scopeId(SCOPE).state(ScopeBulkStatus.State.FINISHED)
                .done(5).total(5).failed(new ArrayList<>()).startedAt(new Date(500)).build());
        bulkJobs.save(MongoScopeBulkJob.builder().jobId("mine").scopeId(SCOPE).state(ScopeBulkStatus.State.QUEUED)
                .done(2).total(7).failed(new ArrayList<>()).startedAt(new Date(2000)).build());

        // a waiting job does not report counters of its own
        assertEquals(0, underTest.getBulkStatus(SCOPE, "mine").getTotal());
        assertEquals(0, underTest.getBulkStatus(SCOPE, "mine").getDone());
        ScopeBulkStatus.Queue queue = underTest.getBulkStatus(SCOPE, "mine").getQueue();
        assertEquals(1, queue.getJobsAhead());
        assertEquals(3, queue.getDone());
        assertEquals(10, queue.getTotal());
        // only queued jobs carry the queue
        assertNull(underTest.getBulkStatus(SCOPE, "finished").getQueue());
    }

    @Test
    void startBulkChecksAndQueues() throws NoSuchMethodException {
        assertThrows(IllegalArgumentException.class, () -> underTest.startBulk(SCOPE, ScopeBulkRequest.builder().build()));
        assertThrows(IllegalArgumentException.class, () -> underTest.startBulk(SCOPE, ScopeBulkRequest.builder()
                .nodeIds(List.of("n1")).properties(Map.of("ccm:editorial_release", List.of("approved"))).build()));

        // only requested multi value properties can be appended (ccm:editorial_internal is the multi value one)
        assertThrows(IllegalArgumentException.class, () -> underTest.startBulk(SCOPE, ScopeBulkRequest.builder()
                .nodeIds(List.of("n1")).properties(Map.of("ccm:editorial_release", List.of("released")))
                .appendProperties(Set.of("ccm:editorial_release")).build()));
        assertThrows(IllegalArgumentException.class, () -> underTest.startBulk(SCOPE, ScopeBulkRequest.builder()
                .nodeIds(List.of("n1")).properties(Map.of("ccm:editorial_release", List.of("released")))
                .appendProperties(Set.of("ccm:editorial_internal")).build()));

        String jobId = underTest.startBulk(SCOPE, ScopeBulkRequest.builder()
                .nodeIds(List.of("n1")).properties(Map.of("ccm:editorial_release", List.of("released"))).build());

        verify(bulkJobRunner).run(jobId);
        ScopeBulkStatus status = underTest.getBulkStatus(SCOPE, jobId);
        assertEquals(ScopeBulkStatus.State.QUEUED, status.getState());
        assertEquals("editor", status.getStartedBy());

        // the toolpermission is enforced by PermissionChecking (proxy)
        Permission permission = MongoScopeService.class.getMethod("startBulk", String.class, ScopeBulkRequest.class).getAnnotation(Permission.class);
        assertArrayEquals(new String[]{CCConstants.CCM_VALUE_TOOLPERMISSION_SCOPE_WRITE}, permission.value());
    }

    @Test
    void startBulkRejectsInternalFields() {
        assertThrows(IllegalArgumentException.class, () -> underTest.startBulk(SCOPE, ScopeBulkRequest.builder()
                .nodeIds(List.of("n1")).properties(Map.of("ccm:editorial_release", List.of("released"))).folderId("folder").build()));
        assertThrows(IllegalArgumentException.class, () -> underTest.startBulk(SCOPE, ScopeBulkRequest.builder()
                .nodeIds(List.of("n1")).properties(Map.of("ccm:editorial_release", List.of("released"))).createOnly(true).build()));
        verifyNoInteractions(bulkJobRunner);
    }

    @Test
    void folderImportQueuesAJobWithTheUploadValues() throws NoSuchMethodException {
        when(definitionService.getDefinition(SCOPE)).thenReturn(Optional.of(ScopeDefinition.builder()
                .scopeId(SCOPE)
                .folderId("folder")
                .fields(List.of(ScopeField.builder().id("ccm:editorial_inventory").uploadValues(List.of("accepted")).build()))
                .build()));

        String jobId = underTest.startFolderImport(SCOPE);

        verify(bulkJobRunner).run(jobId);
        ScopeBulkRequest request = bulkJobs.findById(jobId).orElseThrow().getRequest();
        assertEquals("folder", request.getFolderId());
        assertTrue(request.isCreateOnly());
        assertEquals(Map.of("ccm:editorial_inventory", List.of("accepted")), request.getProperties());
        assertTrue(MongoScopeService.class.getMethod("startFolderImport", String.class).getAnnotation(Permission.class).requiresGlobalAdmin());
    }

    @Test
    void folderImportWithoutFolderStartsNoJob() {
        assertNull(underTest.startFolderImport(SCOPE));
        verifyNoInteractions(bulkJobRunner);
    }

    @Test
    void uploadCreatesEntryWithUploadValues() {
        ScopeDefinition definition = ScopeDefinition.builder()
                .scopeId(SCOPE)
                .fields(List.of(
                        ScopeField.builder().id("ccm:editorial_inventory").uploadValues(List.of("accepted")).excludedValues(List.of("rejected")).build(),
                        ScopeField.builder().id("ccm:editorial_release").publishedValues(List.of("released")).build()))
                .build();
        when(definitionService.getDefinition(SCOPE)).thenReturn(Optional.of(definition));

        underTest.onUpload(SCOPE, "upload1", "uploader");
        underTest.onUpload(SCOPE, "upload1", "uploader");

        MongoScopeAssignment assignment = assignments.findByScopeIdAndNodeId(SCOPE, "upload1").orElseThrow();
        assertEquals(Map.of("ccm:editorial_inventory", List.of("accepted")), assignment.getProperties());
        assertEquals(ScopeSource.UPLOAD, assignment.getSource());
        assertEquals("uploader", assignment.getCreatedBy());
        // not published until released by an editor
        assertFalse(assignment.isPublished());
        assertEquals(1, history.count());
        assertEquals(1, changes.count());
    }
}
