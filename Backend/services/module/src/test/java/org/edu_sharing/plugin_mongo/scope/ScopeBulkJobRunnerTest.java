package org.edu_sharing.plugin_mongo.scope;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.edu_sharing.plugin_mongo.util.AbstractMongoDbContainerTest;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.permission.PermissionService;
import org.edu_sharing.service.scope.*;
import org.edu_sharing.service.search.model.SearchToken;
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
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Testcontainers
class ScopeBulkJobRunnerTest extends AbstractMongoDbContainerTest {

    static final String SCOPE = "GROUP_ORG_wlo";
    static final String WORKSPACE = StoreRef.PROTOCOL_WORKSPACE;
    static final String STORE = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier();

    private MongoTemplate template;
    private ScopeAssignmentRepository assignments;
    private ScopeChangeRepository changes;
    private ScopeBulkJobRepository jobs;
    private ScopeBulkJobRunner underTest;
    private ScopeContextResolver scopeContextResolver;
    private final AtomicBoolean inScope = new AtomicBoolean();
    private final List<Map<String, String[]>> searchCriteria = new ArrayList<>();
    private MockedStatic<AuthenticationUtil> authenticationUtil;

    @BeforeEach
    void setUp() {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setAutoIndexCreation(true);
        mappingContext.afterPropertiesSet();
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(dbFactory), mappingContext);
        converter.afterPropertiesSet();
        template = new MongoTemplate(dbFactory, converter);
        MongoRepositoryFactory factory = new MongoRepositoryFactory(template);
        assignments = factory.getRepository(ScopeAssignmentRepository.class);
        changes = factory.getRepository(ScopeChangeRepository.class,
                RepositoryComposition.RepositoryFragments.of(RepositoryFragment.implemented(CustomScopeChangeRepository.class, new CustomScopeChangeRepositoryImpl(template))));
        jobs = factory.getRepository(ScopeBulkJobRepository.class);

        ScopeDefinitionService definitionService = mock(ScopeDefinitionService.class);
        when(definitionService.getDefinition(SCOPE)).thenReturn(Optional.of(ScopeDefinition.builder()
                .scopeId(SCOPE)
                .fields(ScopeAssignmentRulesTest.DEFINITION.getFields())
                .baseQuery(ScopeBaseQuery.builder().version(7).queries(List.of()).build())
                .build()));
        NodeService nodeService = mock(NodeService.class);
        when(nodeService.getOriginalNode(anyString())).thenAnswer(i -> new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, i.getArgument(0)));
        when(nodeService.getOriginalNode("copy1")).thenReturn(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "n1"));
        when(nodeService.getProperty(eq(WORKSPACE), eq(STORE), anyString(), eq(CCConstants.CM_PROP_VERSIONABLELABEL))).thenReturn("2.0");
        PermissionService permissionService = mock(PermissionService.class);
        when(permissionService.hasPermission(eq(WORKSPACE), eq(STORE), anyString(), eq(CCConstants.PERMISSION_READ))).thenReturn(true);
        when(permissionService.hasPermission(WORKSPACE, STORE, "secret", CCConstants.PERMISSION_READ)).thenReturn(false);
        ScopesSettings settings = new ScopesSettings();
        ScopesSettings.Bulk bulk = new ScopesSettings.Bulk();
        bulk.setPartitionSize(2);
        settings.setBulk(bulk);

        scopeContextResolver = mock(ScopeContextResolver.class);
        underTest = new ScopeBulkJobRunner(jobs, assignments, changes, template, definitionService, nodeService, permissionService,
                // the search needs elasticsearch, see searchAll
                null, settings, scopeContextResolver) {
            @Override
            protected List<String> searchAll(ScopeBaseQuery.Query search, Map<String, String[]> criteria, SearchToken token) {
                searchCriteria.add(criteria);
                assertTrue(inScope.get(), "search outside of the scope of the job");
                return List.of("n1");
            }
        };

        authenticationUtil = Mockito.mockStatic(AuthenticationUtil.class);
        authenticationUtil.when(() -> AuthenticationUtil.runAsSystem(any()))
                .thenAnswer(i -> ((AuthenticationUtil.RunAsWork<?>) i.getArgument(0)).doWork());
    }

    @AfterEach
    void tearDown() {
        authenticationUtil.close();
    }

    private String job(ScopeBulkRequest request) {
        return jobs.save(MongoScopeBulkJob.builder()
                .jobId(UUID.randomUUID().toString())
                .scopeId(SCOPE)
                .state(ScopeBulkStatus.State.QUEUED)
                .failed(new ArrayList<>())
                .startedBy("editor")
                .startedAt(new Date())
                .request(request)
                .build()).getJobId();
    }

    @Test
    void setPropertiesForAListOfNodes() {
        // existing entry: only the changed property goes into the history, other properties stay
        assignments.save(MongoScopeAssignment.builder().id(MongoScopeAssignment.createId(SCOPE, "n2")).scopeId(SCOPE).nodeId("n2")
                .properties(Map.of("ccm:editorial_inventory", List.of("rejected"))).excluded(true).source(ScopeSource.USER).createdBy("other").build());

        String jobId = job(ScopeBulkRequest.builder()
                .nodeIds(List.of("n1", "n2", "secret", "copy1", "n3"))
                .properties(Map.of("ccm:editorial_release", List.of("released")))
                .reason("Migration")
                .build());
        underTest.run(jobId);

        ScopeBulkStatus status = jobs.findById(jobId).orElseThrow().toStatus();
        assertEquals(ScopeBulkStatus.State.FINISHED, status.getState());
        assertEquals(5, status.getTotal());
        assertEquals(5, status.getDone());
        assertEquals(List.of("secret"), status.getFailed().stream().map(ScopeBulkStatus.Failure::getNodeId).toList());

        MongoScopeAssignment n1 = assignments.findByScopeIdAndNodeId(SCOPE, "n1").orElseThrow();
        assertTrue(n1.isPublished());
        assertEquals(ScopeSource.BULK, n1.getSource());
        assertEquals("editor", n1.getCreatedBy());
        MongoScopeAssignment n2 = assignments.findByScopeIdAndNodeId(SCOPE, "n2").orElseThrow();
        assertTrue(n2.isPublished());
        assertTrue(n2.isExcluded());
        assertEquals(ScopeSource.USER, n2.getSource());
        assertEquals(3, assignments.count());

        List<MongoScopeHistoryEntry> history = template.findAll(MongoScopeHistoryEntry.class);
        // n1 (copy1 is the same original), n2, n3
        assertEquals(3, history.size());
        assertTrue(history.stream().allMatch(h -> h.getActor().equals("bulk:" + jobId) && "Migration".equals(h.getReason())
                && h.getBaseQueryVersion() == 7 && "2.0".equals(h.getContentVersion())));

        // one change entry per partition (partition size 2: [n1,n2], [secret,copy1], [n3])
        List<MongoScopeChange> feed = changes.findChanges(null, null, null, 10);
        assertEquals(List.of(List.of("n1", "n2"), List.of("n3")), feed.stream().map(MongoScopeChange::getNodeIds).toList());
    }

    /**
     * the queued thread has no request or session: the search must run inside the scope of the job, otherwise the
     * scope filter and the work sets (virtual:scope_view) are ignored and the whole repository is hit
     */
    @Test
    void searchRunsInsideTheScopeOfTheJob() throws Exception {
        when(scopeContextResolver.runWithScope(eq(SCOPE), any())).thenAnswer(i -> {
            inScope.set(true);
            try {
                return ((Callable<?>) i.getArgument(1)).call();
            } finally {
                inScope.set(false);
            }
        });

        String jobId = job(ScopeBulkRequest.builder()
                .search(ScopeBaseQuery.Query.builder().mds("-default-").query("ngsearch")
                        .criteria(Map.of("virtual:scope_view", List.of("inbox"))).build())
                .properties(Map.of("ccm:editorial_release", List.of("released")))
                .build());
        underTest.run(jobId);

        assertEquals(ScopeBulkStatus.State.FINISHED, jobs.findById(jobId).orElseThrow().getState());
        assertArrayEquals(new String[]{"inbox"}, searchCriteria.get(0).get("virtual:scope_view"));
        assertTrue(assignments.findByScopeIdAndNodeId(SCOPE, "n1").isPresent());
    }

    @Test
    void appendAddsToTheExistingValues() {
        assignments.save(MongoScopeAssignment.builder().id(MongoScopeAssignment.createId(SCOPE, "n1")).scopeId(SCOPE).nodeId("n1")
                .properties(Map.of("ccm:editorial_internal", List.of("a"))).source(ScopeSource.USER).build());

        String jobId = job(ScopeBulkRequest.builder()
                .nodeIds(List.of("n1", "n2"))
                .properties(Map.of("ccm:editorial_internal", List.of("a", "b")))
                .appendProperties(Set.of("ccm:editorial_internal"))
                .build());
        underTest.run(jobId);

        assertEquals(List.of("a", "b"), assignments.findByScopeIdAndNodeId(SCOPE, "n1").orElseThrow().getProperties().get("ccm:editorial_internal"));
        assertEquals(List.of("a", "b"), assignments.findByScopeIdAndNodeId(SCOPE, "n2").orElseThrow().getProperties().get("ccm:editorial_internal"));
        // n1: only "b" is new
        MongoScopeHistoryEntry n1 = template.findAll(MongoScopeHistoryEntry.class).stream().filter(h -> h.getNodeId().equals("n1")).findFirst().orElseThrow();
        assertEquals(List.of("a"), n1.getFrom());
        assertEquals(List.of("a", "b"), n1.getTo());
    }

    @Test
    void deleteEntries() {
        assignments.save(MongoScopeAssignment.builder().id(MongoScopeAssignment.createId(SCOPE, "n1")).scopeId(SCOPE).nodeId("n1").properties(Map.of()).build());

        String jobId = job(ScopeBulkRequest.builder().nodeIds(List.of("n1", "n2")).delete(true).build());
        underTest.run(jobId);

        assertEquals(0, assignments.count());
        assertEquals(1, template.findAll(MongoScopeHistoryEntry.class).size());
        assertEquals(ScopeChangeType.DELETE, changes.findChanges(null, null, null, 10).get(0).getType());
        assertEquals(ScopeBulkStatus.State.FINISHED, jobs.findById(jobId).orElseThrow().getState());
    }

    @Test
    void unknownScopeFailsTheJob() {
        String jobId = jobs.save(MongoScopeBulkJob.builder().jobId("j").scopeId("GROUP_gone").failed(new ArrayList<>())
                .request(ScopeBulkRequest.builder().nodeIds(List.of("n1")).build()).build()).getJobId();
        underTest.run(jobId);

        MongoScopeBulkJob job = jobs.findById(jobId).orElseThrow();
        assertEquals(ScopeBulkStatus.State.FAILED, job.getState());
        assertNotNull(job.getFinishedAt());
    }
}
