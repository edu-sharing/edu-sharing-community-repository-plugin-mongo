package org.edu_sharing.plugin_mongo.relation;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.edu_sharing.plugin_mongo.integrity.IntegrityService;
import org.edu_sharing.plugin_mongo.util.AbstractMongoDbContainerTest;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.InsufficientPermissionException;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.relations.InputRelationType;
import org.edu_sharing.service.relations.NodeRelation;
import org.edu_sharing.service.relations.NodeRelationException;
import org.edu_sharing.service.relations.RelationTypeUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
@Testcontainers
class RelationServiceImplTest extends AbstractMongoDbContainerTest {
    private RelationServiceImpl underTest;
    private Date now = new Date();
    @Mock
    private IntegrityService integrityService;

    @Mock
    private NodeService nodeService;

    @BeforeEach
    void setUp() {

        MongoCollection<Document> collection = db.getCollection(RelationConstants.COLLECTION_KEY);
        collection.insertMany(Arrays.asList(
                createNodeRelation("Node A",
                        createRelation("Node C", "Müller", now, InputRelationType.isPartOf),
                        createRelation("Node B", "Müller", now, InputRelationType.isPartOf),
                        createRelation("Node B", "Schulz", now, InputRelationType.isBasedOn)
                ),
                createNodeRelation("Node B"),
                //createNodeRelation("Node C"),
                createNodeRelation("Node D",
                        createRelation("Node A", "Maier", now, InputRelationType.isPartOf)
                ),
                createNodeRelation("Node E",
                        createRelation("Node C", "Müller", now, InputRelationType.references)
                )
        ));

        underTest = new RelationServiceImpl(db, nodeService, integrityService);
        Mockito.lenient()
                .when(nodeService.getOriginalNode(anyString()))
                .thenAnswer((Answer<NodeRef>) invocationOnMock -> new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, invocationOnMock.getArgument(0)));
    }

    private Document createNodeRelation(String node, final Document... relations) {
        return new Document(RelationConstants.ID_KEY, node)
                .append(RelationConstants.RELATION_KEY, Arrays.asList(relations));
    }

    private Document createRelation(String node, String creator, Date date, InputRelationType type) {
        return new Document(RelationConstants.RELATION_NODE_KEY, node)
                .append(RelationConstants.RELATION_CREATOR_KEY, creator)
                .append(RelationConstants.RELATION_TIMESTAMP_KEY, date)
                .append(RelationConstants.RELATION_TYPE_KEY, type.toString());
    }

    @NotNull
    private CodecRegistry getCodecRegistry() {

        ClassModelBuilder<NodeRelation> nodeRelationClassModelBuilder = ClassModel.builder(NodeRelation.class)
                .idPropertyName("node");

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                .register(nodeRelationClassModelBuilder.build())
                .automatic(true)
                .build();

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoCodecProvider));
        return pojoCodecRegistry;
    }

    @ParameterizedTest
    @CsvSource(value = {
            "Node A;isPartOf: B,C isBasedOn: B hasPart: D",
            "Node B;hasPart: A isBaseFor: A",
            "Node C;hasPart: A references: E",
            "Node D;isPartOf: A",
            "Node E;references: C",
            "Node F;nothing"
    },
            delimiter = ';')
    void getRelationsTest(String nodeId, String info) {
        // given
        MongoCollection<NodeRelation> collection = db.withCodecRegistry(getCodecRegistry()).getCollection(RelationConstants.COLLECTION_KEY, NodeRelation.class);

        // when
        NodeRelation nodeRelation = underTest.getRelations(nodeId);

        // then
        NodeRelation expected = collection.find(Filters.eq(nodeId)).first();
        if (expected == null) {
            expected = new NodeRelation(nodeId);
        }

        expected.getRelations().addAll(
                collection.find(Filters.eq(
                                String.join(".",
                                        RelationConstants.RELATION_KEY,
                                        RelationConstants.RELATION_NODE_KEY),
                                nodeId))
                        .into(new ArrayList<>())
                        .stream().flatMap(x ->
                                x.getRelations()
                                        .stream()
                                        .filter(y -> Objects.equals(y.getNode(), nodeId))
                                        .peek(y -> {
                                            y.setType(RelationTypeUtil.invert(y.getType()));
                                            y.setNode(x.getNode());
                                        }))
                        .collect(Collectors.toList()));


        Assertions.assertEquals(nodeRelation, expected);
    }

    @Test
    void createRelationTest() throws NodeRelationException, InsufficientPermissionException {
        // given
        String from = "Node E";
        String to = "Node B";
        String authority = "Muster";
        InputRelationType type = InputRelationType.isPartOf;
        String nodeType = CCConstants.CCM_TYPE_IO;

        MongoCollection<NodeRelation> collection = db.withCodecRegistry(getCodecRegistry()).getCollection(RelationConstants.COLLECTION_KEY, NodeRelation.class);
        int beforeCount = collection.find(Filters.eq(from)).first().getRelations().size();

        Mockito.when(integrityService.getAuthority()).thenReturn(authority);
        Mockito.when(nodeService.getType(from)).thenReturn(nodeType);
        Mockito.when(nodeService.getType(to)).thenReturn(nodeType);

        // when
        underTest.createRelation(from, to, type);

        // then
        NodeRelation result = collection.find(Filters.eq(from)).first();

        Assertions.assertNotNull(result, "NodeRelation");
        Assertions.assertEquals(from, result.getNode(), "NodeRelation node");
        Assertions.assertNotNull(result.getRelations(), "relations");
        Assertions.assertEquals(beforeCount + 1, result.getRelations().size(), "size of relations");
        Assertions.assertEquals(to, result.getRelations().get(beforeCount).getNode(), "relation node");
        Assertions.assertEquals(authority, result.getRelations().get(beforeCount).getCreator(), "relation creator");
        Assertions.assertNotNull(result.getRelations().get(beforeCount).getTimestamp(), "relation timestamp");
        Assertions.assertEquals(RelationTypeUtil.toOutputType(type), result.getRelations().get(beforeCount).getType(), "relation type");
    }

    @Test
    void createRelation_ToExistingNodeRelationTest() throws NodeRelationException, InsufficientPermissionException {
        // given
        String from = "Node E";
        String to = "Node B";
        String authority = "Muster";
        InputRelationType type = InputRelationType.isPartOf;
        String nodeType = CCConstants.CCM_TYPE_IO;

        MongoCollection<NodeRelation> collection = db.withCodecRegistry(getCodecRegistry()).getCollection(RelationConstants.COLLECTION_KEY, NodeRelation.class);
        int beforeCount = collection.find(Filters.eq(from)).first().getRelations().size();

        Mockito.when(integrityService.getAuthority()).thenReturn(authority);
        Mockito.when(integrityService.getAuthority()).thenReturn(authority);
        Mockito.when(nodeService.getType(from)).thenReturn(nodeType);
        Mockito.when(nodeService.getType(to)).thenReturn(nodeType);

        // when
        underTest.createRelation(from, to, type);

        // then
        NodeRelation result = collection.find(Filters.eq(from)).first();

        Assertions.assertNotNull(result, "NodeRelation");
        Assertions.assertEquals(from, result.getNode(), "NodeRelation node");
        Assertions.assertNotNull(result.getRelations(), "relations");
        Assertions.assertEquals(beforeCount + 1, result.getRelations().size(), "size of relations");
        Assertions.assertEquals(to, result.getRelations().get(beforeCount).getNode(), "relation node");
        Assertions.assertEquals(authority, result.getRelations().get(beforeCount).getCreator(), "relation creator");
        Assertions.assertNotNull(result.getRelations().get(beforeCount).getTimestamp(), "relation timestamp");
        Assertions.assertEquals(RelationTypeUtil.toOutputType(type), result.getRelations().get(beforeCount).getType(), "relation type");
    }

    @Test
    void createRelation_ThrowNodeRelationException_ExistingRelationTest() {
        // given
        String from = "Node E";
        String to = "Node C";
        String authority = "Muster";
        InputRelationType type = InputRelationType.references;
        String nodeType = CCConstants.CCM_TYPE_IO;

        Mockito.when(integrityService.getAuthority()).thenReturn(authority);
        Mockito.when(nodeService.getType(from)).thenReturn(nodeType);
        Mockito.when(nodeService.getType(to)).thenReturn(nodeType);

        // when
        Assertions.assertThrows(NodeRelationException.class, () -> underTest.createRelation(from, to, type));
    }


    @Test
    void createRelation_ThrowNodeRelationException_FromNodeInvalidTypeTest() {
        // given
        String from = "Node E";
        String to = "Node C";
        InputRelationType type = InputRelationType.references;
        String fromNodeType = "some other type";

        Mockito.when(nodeService.getType(from)).thenReturn(fromNodeType);

        // when
        Assertions.assertThrows(NodeRelationException.class, () -> underTest.createRelation(from, to, type));
    }

    @Test
    void createRelation_ThrowNodeRelationException_ToNodeInvalidTypeTest() {
        // given
        String from = "Node E";
        String to = "Node C";
        InputRelationType type = InputRelationType.references;
        String fromNodeType = CCConstants.CCM_TYPE_IO;
        String toNodeType = "some other type";

        Mockito.when(nodeService.getType(from)).thenReturn(fromNodeType);
        Mockito.when(nodeService.getType(to)).thenReturn(toNodeType);

        // when
        Assertions.assertThrows(NodeRelationException.class, () -> underTest.createRelation(from, to, type));
    }

    @Test
    void createRelation_ThrowNodeRelationException_SelfReferencingRelationTest() {
        // given
        String from = "Node E";
        String to = from;
        InputRelationType type = InputRelationType.references;

        // when
        Assertions.assertThrows(NodeRelationException.class, () -> underTest.createRelation(from, to, type));
    }

    @Test
    void deleteRelationTest() throws NodeRelationException, InsufficientPermissionException {
        // given
        String from = "Node A";
        String to = "Node B";
        InputRelationType type = InputRelationType.isBasedOn;

        MongoCollection<NodeRelation> collection = db.withCodecRegistry(getCodecRegistry()).getCollection(RelationConstants.COLLECTION_KEY, NodeRelation.class);

        // when
        underTest.deleteRelation(from, to, type);

        // then
        NodeRelation result = collection.find(Filters.eq(from)).first();

        Assertions.assertNotNull(result, "NodeRelation");
        Assertions.assertEquals(from, result.getNode(), "NodeRelation node");
        Assertions.assertNotNull(result.getRelations(), "relations");
        Assertions.assertFalse(result.getRelations().stream().anyMatch(x -> Objects.equals(x.getNode(), to) && x.getType() == RelationTypeUtil.toOutputType(type)), "contains relation");

    }

    @Test
    void deleteRelation_ThrowNodeRelationExceptionTest() {
        // given
        String from = "Node E";
        String to = "Node Z";
        InputRelationType type = InputRelationType.references;

        // when
        Assertions.assertThrows(NodeRelationException.class, () -> underTest.deleteRelation(from, to, type));
    }

    @Test
    void changeAuthorityTest() {
        // given
        String from = "Müller";
        String to = "Muster";

        MongoCollection<NodeRelation> collection = db.withCodecRegistry(getCodecRegistry()).getCollection(RelationConstants.COLLECTION_KEY, NodeRelation.class);
        long expected = collection.countDocuments(Filters.elemMatch(RelationConstants.RELATION_KEY,
                Filters.eq(RelationConstants.RELATION_CREATOR_KEY, from)));

        // when
        underTest.changeAuthority(from, to);

        // then
        long actual = collection.countDocuments(Filters.elemMatch(RelationConstants.RELATION_KEY,
                Filters.eq(RelationConstants.RELATION_CREATOR_KEY, to)));

        Assertions.assertEquals(0, collection.countDocuments(Filters.elemMatch(RelationConstants.RELATION_KEY,
                Filters.eq(RelationConstants.RELATION_CREATOR_KEY, from))), from);
        Assertions.assertEquals(expected, actual, to);
    }
}