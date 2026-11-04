package org.edu_sharing.plugin_mongo.scope;

import org.bson.Document;
import org.edu_sharing.plugin_mongo.util.AbstractMongoDbContainerTest;
import org.edu_sharing.service.scope.ScopeChangeType;
import org.edu_sharing.service.scope.ScopeSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class ScopeRepositoriesTest extends AbstractMongoDbContainerTest {

    private MongoTemplate template;
    private ScopeAssignmentRepository assignments;
    private ScopeHistoryRepository history;
    private ScopeChangeRepository changes;

    @BeforeEach
    void setUp() {
        // same as the plugin configuration: indexes are created from the annotations
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setAutoIndexCreation(true);
        mappingContext.afterPropertiesSet();
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(dbFactory), mappingContext);
        converter.afterPropertiesSet();
        template = new MongoTemplate(dbFactory, converter);

        MongoRepositoryFactory factory = new MongoRepositoryFactory(template);
        assignments = factory.getRepository(ScopeAssignmentRepository.class);
        history = factory.getRepository(ScopeHistoryRepository.class);
        changes = factory.getRepository(ScopeChangeRepository.class,
                RepositoryComposition.RepositoryFragments.of(RepositoryFragment.implemented(CustomScopeChangeRepository.class, new CustomScopeChangeRepositoryImpl(template))));
    }

    private MongoScopeAssignment assignment(String scopeId, String nodeId, boolean published) {
        return MongoScopeAssignment.builder()
                .id(MongoScopeAssignment.createId(scopeId, nodeId))
                .scopeId(scopeId)
                .nodeId(nodeId)
                .properties(Map.of("ccm:editorial_release", List.of(published ? "released" : "not_released")))
                .published(published)
                .source(ScopeSource.USER)
                .created(new Date())
                .createdBy("editor")
                .build();
    }

    @Test
    void indexesAreCreated() {
        template.save(assignment("GROUP_ORG_a", "n1", true));
        template.save(MongoScopeHistoryEntry.builder().scopeId("GROUP_ORG_a").nodeId("n1").at(new Date()).build());
        changes.append("GROUP_ORG_a", List.of("n1"), ScopeChangeType.UPSERT);

        assertTrue(indexNames(ScopeConstants.COLLECTION_ASSIGNMENT).containsAll(Set.of("scopeId_1_nodeId_1", "scopeId_1_published_1", "nodeId_1")));
        assertTrue(indexNames(ScopeConstants.COLLECTION_HISTORY).containsAll(Set.of("scopeId_1_nodeId_1_at_-1", "actor_1_at_-1", "nodeId_1")));
        assertTrue(indexNames(ScopeConstants.COLLECTION_CHANGE).containsAll(Set.of("seq_1", "at_1_seq_1", "at_ttl")));
        Document ttl = template.getCollection(ScopeConstants.COLLECTION_CHANGE).listIndexes().into(new ArrayList<>()).stream()
                .filter(i -> "at_ttl".equals(i.getString("name"))).findFirst().orElseThrow();
        assertEquals(30L * 24 * 60 * 60, ((Number) ttl.get("expireAfterSeconds")).longValue());
    }

    private Set<String> indexNames(String collection) {
        return template.getCollection(collection).listIndexes().into(new ArrayList<>()).stream()
                .map(i -> i.getString("name")).collect(Collectors.toSet());
    }

    @Test
    void scopeAndNodeAreUnique() {
        assignments.insert(assignment("GROUP_ORG_a", "n1", false));
        MongoScopeAssignment duplicate = assignment("GROUP_ORG_a", "n1", true);
        // different _id, same (scopeId, nodeId): rejected by the unique index
        duplicate.setId("other");
        assertThrows(DuplicateKeyException.class, () -> assignments.insert(duplicate));
    }

    @Test
    void crudAndLookups() {
        assignments.saveAll(List.of(
                assignment("GROUP_ORG_a", "n1", true),
                assignment("GROUP_ORG_b", "n1", false),
                assignment("GROUP_ORG_a", "n2", false)));

        assertEquals("GROUP_ORG_b", assignments.findByScopeIdAndNodeId("GROUP_ORG_b", "n1").orElseThrow().getScopeId());
        assertEquals(2, assignments.findAllByNodeId("n1").size());
        assertEquals(3, assignments.findAllByNodeIdIn(List.of("n1", "n2")).size());
        assertTrue(assignments.existsByScopeIdAndNodeIdAndPublishedTrue("GROUP_ORG_a", "n1"));
        assertFalse(assignments.existsByScopeIdAndNodeIdAndPublishedTrue("GROUP_ORG_b", "n1"));

        assertEquals(2, assignments.deleteAllByNodeId("n1"));
        assertEquals(1, assignments.deleteAllByScopeId("GROUP_ORG_a"));
        assertEquals(0, assignments.count());
    }

    @Test
    void historyNewestFirst() {
        Date older = new Date(1000);
        Date newer = new Date(2000);
        history.saveAll(List.of(
                MongoScopeHistoryEntry.builder().scopeId("GROUP_ORG_a").nodeId("n1").property("ccm:editorial_release").at(older).build(),
                MongoScopeHistoryEntry.builder().scopeId("GROUP_ORG_a").nodeId("n1").property("ccm:editorial_inventory").at(newer).build(),
                MongoScopeHistoryEntry.builder().scopeId("GROUP_ORG_b").nodeId("n1").at(newer).build()));

        List<MongoScopeHistoryEntry> result = history.findAllByScopeIdAndNodeIdOrderByAtDesc("GROUP_ORG_a", "n1");
        assertEquals(List.of("ccm:editorial_inventory", "ccm:editorial_release"), result.stream().map(MongoScopeHistoryEntry::getProperty).toList());
        assertEquals(3, history.deleteAllByNodeId("n1"));
    }

    @Test
    void changeFeedSequenceAndCursor() {
        MongoScopeChange first = changes.append("GROUP_ORG_a", List.of("n1"), ScopeChangeType.UPSERT);
        MongoScopeChange second = changes.append("GROUP_ORG_a", List.of("n2"), ScopeChangeType.DELETE);
        MongoScopeChange third = changes.append("GROUP_ORG_b", List.of(), ScopeChangeType.SCOPE_DELETED);
        assertEquals(first.getSeq() + 1, second.getSeq());
        assertEquals(second.getSeq() + 1, third.getSeq());

        // no cursor: everything, ordered
        assertEquals(List.of(first.getSeq(), second.getSeq(), third.getSeq()), seqs(changes.findChanges(null, null, null, 10)));
        // standalone seq cursor
        assertEquals(List.of(third.getSeq()), seqs(changes.findChanges(null, second.getSeq(), null, 10)));
        // (at, seq) cursor: entries at exactly "after" are only returned when their seq is greater
        assertEquals(List.of(second.getSeq(), third.getSeq()),
                seqs(changes.findChanges(first.getAt(), first.getSeq(), null, 10)).stream().filter(s -> s >= second.getSeq()).toList());
        assertFalse(seqs(changes.findChanges(first.getAt(), first.getSeq(), null, 10)).contains(first.getSeq()));
        // limit
        assertEquals(1, changes.findChanges(null, null, null, 1).size());
        // until is inclusive
        assertTrue(seqs(changes.findChanges(null, null, first.getAt(), 10)).contains(first.getSeq()));
    }

    private List<Long> seqs(List<MongoScopeChange> list) {
        return list.stream().map(MongoScopeChange::getSeq).toList();
    }
}
