package org.edu_sharing.plugin_mongo.relation;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.edu_sharing.plugin_mongo.rating.RatingConstants;
import org.edu_sharing.plugin_mongo.rating.RatingIntegrityService;
import org.edu_sharing.plugin_mongo.util.AbstractMongoDbContainerTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
@Testcontainers
class RelationServiceImplTest extends AbstractMongoDbContainerTest {
    private RelationServiceImpl underTest;
    private Date now = new Date();
    @Mock
    private RatingIntegrityService integrityService;

    @BeforeEach
    void setUp() {

        /*
         * 0
         *     ---> 3
         *   /    /
         * 1 ---> 2
         *   \    \
         *     ---> 4 <---> 5
         */

        MongoCollection<Document> collection = db.getCollection(RelationConstants.COLLECTION_KEY);
        collection.insertMany(Arrays.asList(
                createNodeRelation("2",
                        createRelation("1", "", now, RelationType.isBasedOn)
                ),
                createNodeRelation("3",
                        createRelation("1", "", now, RelationType.isPartOf),
                        createRelation("2", "", now, RelationType.isBasedOn)
                ),
                createNodeRelation("4",
                        createRelation("1", "", now, RelationType.isPartOf),
                        createRelation("2", "", now, RelationType.isBasedOn),
                        createRelation("3", "", now, RelationType.isPartOf)
                ),
                createNodeRelation("5",
                        createRelation("4", "", now, RelationType.references)
                )
        ));

        underTest = new RelationServiceImpl(db, integrityService);
    }

    private Document createNodeRelation(String node, final Document... relations) {
        return new Document(RelationConstants.ID_KEY, node)
                .append(RelationConstants.RELATION_KEY, Arrays.asList(relations));
    }

    private Document createRelation(String node, String creator, Date date, RelationType type) {
        return new Document(RelationConstants.RELATION_NODE_KEY, node)
                .append(RelationConstants.RELATION_CREATOR_KEY, creator)
                .append(RelationConstants.RELATION_TIMESTAMP_KEY, date)
                .append(RelationConstants.RELATION_TYPE_KEY, type.toString());
    }

    @NotNull
    private CodecRegistry getCodecRegistry() {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                .automatic(true)
                .build();

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoCodecProvider));
        return pojoCodecRegistry;
    }


    @Test
    void getRelations_UnknownNodeWithoutRelationsTest() {
        // given
        String nodeId = "0";

        // when
        NodeRelation nodeRelation = underTest.getRelations(nodeId);

        // then
        Assertions.assertNotNull(nodeRelation, "nodeRelation");
        Assertions.assertEquals(nodeRelation.getNode(), nodeId, "node");
        Assertions.assertNotNull(nodeRelation.getRelations(), "relations");
        Assertions.assertEquals(nodeRelation.getRelations().size(), 0, "relations");
    }

    @Test
    void getRelations_UnknownNodeWithRelationsTest() {
        // given
        String nodeId = "1";
        MongoCollection<NodeRelation> collection = db.withCodecRegistry(getCodecRegistry()).getCollection(RelationConstants.COLLECTION_KEY, NodeRelation.class);


        // when
        NodeRelation nodeRelation = underTest.getRelations(nodeId);

        // then
        List<RelationData> expected = collection.find(Filters.eq(
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
                .collect(Collectors.toList());

        Assertions.assertNotNull(nodeRelation, "nodeRelation");
        Assertions.assertEquals(nodeRelation.getNode(), nodeId, "node");
        Assertions.assertNotNull(nodeRelation.getRelations(), "relations");
        Assertions.assertArrayEquals(nodeRelation.getRelations().toArray(), expected.toArray(), "relations");
    }

    @Test
    void getRelations_KnownNodeWithoutForeignRelationsTest() {
        // given
        String nodeId = "4";
        MongoCollection<NodeRelation> collection = db.withCodecRegistry(getCodecRegistry()).getCollection(RelationConstants.COLLECTION_KEY, NodeRelation.class);

        // when
        NodeRelation nodeRelation = underTest.getRelations(nodeId);

        // then
        NodeRelation expected = collection.find(Filters.eq(nodeId)).first();
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

    @ParameterizedTest
    @CsvSource(value = {
            "0:unknown node without relations",
            "1:unknown node with relations",
            "2:node no foreign relations",
            "3:node with foreign relations",
            "4:node with foreign relations",
            "5:node with relations (reference)" },
            delimiter = ':')
    void getRelations_KnownNodeWithForeignRelationsTest(String nodeId, String info) {
        // given
        //String nodeId = "4";
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
    void createRelationTest() {
        // given

        // when

        // then
    }

    @Test
    void deleteRelationTest() {
        // given

        // when

        // then
    }

    @Test
    void changeAuthorityTest() {
        // given

        // when

        // then
    }
}