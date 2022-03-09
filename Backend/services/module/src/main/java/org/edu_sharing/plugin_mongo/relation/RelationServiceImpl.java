package org.edu_sharing.plugin_mongo.relation;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import lombok.extern.log4j.Log4j;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.pojo.PropertyModelBuilder;
import org.edu_sharing.plugin_mongo.rating.RatingConstants;
import org.edu_sharing.plugin_mongo.rating.RatingIntegrityService;
import org.edu_sharing.service.rating.Rating;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Log4j
public class RelationServiceImpl {

    private final MongoDatabase database;
    private final RatingIntegrityService integrityService;

    public RelationServiceImpl(MongoDatabase database, RatingIntegrityService integrityService) {
        this.integrityService = integrityService;

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                .automatic(true)
                .build();

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoCodecProvider));

        this.database = database.withCodecRegistry(pojoCodecRegistry);
    }

    @NotNull
    public NodeRelation getRelations(String node) {
        log.debug(String.format("get relations of node %s", node));
        MongoCollection<RelationData> relationDataCollection = database.getCollection(RelationConstants.COLLECTION_KEY, RelationData.class);
        MongoCollection<NodeRelation> nodeRelationCollection = database.getCollection(RelationConstants.COLLECTION_KEY, NodeRelation.class);

        List<RelationData> relationData = relationDataCollection.aggregate(Arrays.asList(
                Aggregates.match(Filters.elemMatch(RelationConstants.RELATION_KEY, Filters.eq(RelationConstants.RELATION_NODE_KEY, node))),
                Aggregates.project(
                        Projections.computed(
                                RelationConstants.RELATION_KEY,
                                Filters.eq("$filter",
                                        Projections.fields(
                                                Filters.eq("input", "$" + RelationConstants.RELATION_KEY),
                                                Filters.eq("as", "item"),
                                                Filters.eq("cond", Filters.eq("$eq", Arrays.asList("$$item." + RelationConstants.RELATION_NODE_KEY, node)))
                                        )
                                )
                        )
                ),
                Aggregates.unwind("$"+RelationConstants.RELATION_KEY),
                Aggregates.set(new Field<>(
                        String.join(".", RelationConstants.RELATION_KEY, RelationConstants.RELATION_NODE_KEY),
                        "$" + RelationConstants.ID_KEY)),
                Aggregates.replaceRoot("$"+RelationConstants.RELATION_KEY)
        )).into(new ArrayList<>());

        relationData.forEach(x->x.setType(RelationTypeUtil.invert(x.getType())));

        NodeRelation nodeRelation = nodeRelationCollection.find(Filters.eq(node)).first();
        if(nodeRelation == null) {
            nodeRelation = new NodeRelation(node);
        }
        nodeRelation.getRelations().addAll(relationData);

        return nodeRelation;
    }

    public void createRelation(String fromNode, String toNode, RelationType relationType) {
        log.debug(String.format("create relation from node %s to node %s of type %s", fromNode, toNode, relationType));

        MongoCollection<Document> collection = database.getCollection(RelationConstants.COLLECTION_KEY);

        Document relation = new Document(RelationConstants.RELATION_NODE_KEY, toNode)
                .append(RelationConstants.RELATION_CREATOR_KEY, integrityService.getAuthority())
                .append(RelationConstants.RELATION_TIMESTAMP_KEY, new Date())
                .append(RelationConstants.RELATION_TYPE_KEY, relationType.toString());

        collection.updateOne(Filters.eq(fromNode),
                Updates.combine(
                        Updates.addToSet(RelationConstants.RELATION_KEY, relation),
                        Updates.setOnInsert(new Document(RelationConstants.ID_KEY, fromNode)
                                .append(RelationConstants.RELATION_KEY, Collections.singletonList(relation)))
                ),
                new UpdateOptions().upsert(true)
        );
    }

    public void deleteRelation(String fromNode, String toNode, RelationType relationType) {
        log.debug(String.format("delete relation from node %s to node %s of type %s", fromNode, toNode, relationType));

        Document relation = new Document(RelationConstants.RELATION_NODE_KEY, toNode)
                .append(RelationConstants.RELATION_TYPE_KEY, relationType.toString());

        MongoCollection<Document> collection = database.getCollection(RelationConstants.COLLECTION_KEY);
        collection.updateOne(Filters.eq(fromNode), Updates.pull(RelationConstants.RELATION_KEY, relation));
    }

    public void changeAuthority(String oldName, String newName) {
        log.debug(String.format("change authority from %s to %s", oldName, newName));

        MongoCollection<Document> collection = database.getCollection(RelationConstants.COLLECTION_KEY);
        collection.updateMany(
                Filters.elemMatch(RelationConstants.RELATION_KEY, Filters.eq(RelationConstants.RELATION_CREATOR_KEY, oldName)),
                Updates.set(RelationConstants.RELATION_CREATOR_KEY + ".$[username]", newName),
                new UpdateOptions().arrayFilters(
                        Arrays.asList(Filters.eq("item." + RelationConstants.RELATION_CREATOR_KEY, oldName))
                )
        );
    }
}
