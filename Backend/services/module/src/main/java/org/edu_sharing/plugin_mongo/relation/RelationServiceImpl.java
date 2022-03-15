package org.edu_sharing.plugin_mongo.relation;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.log4j.Log4j;
import org.alfresco.service.cmr.repository.NodeRef;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.edu_sharing.plugin_mongo.integrity.IntegrityService;
import org.edu_sharing.plugin_mongo.util.MongoDbUtil;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.InsufficientPermissionException;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.permission.annotation.NodePermission;
import org.edu_sharing.service.permission.annotation.Permission;
import org.edu_sharing.service.relations.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Log4j
public class RelationServiceImpl implements RelationService {

    private final MongoDatabase database;
    private final NodeService nodeService;
    private final IntegrityService integrityService;

    public RelationServiceImpl(MongoDatabase database, NodeService nodeService, IntegrityService integrityService) {
        this.integrityService = integrityService;
        this.nodeService = nodeService;

        ClassModelBuilder<RelationData> relationDataClassModelBuilder = ClassModel.builder(RelationData.class);
        ClassModelBuilder<NodeRelation> nodeRelationClassModelBuilder = ClassModel.builder(NodeRelation.class)
                .idPropertyName("node");

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                .register(relationDataClassModelBuilder.build(),
                        nodeRelationClassModelBuilder.build())
                .build();

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoCodecProvider));

        this.database = database.withCodecRegistry(pojoCodecRegistry);
    }

    private void createIndexes() {
        MongoCollection<Document> relationDataCollection = database.getCollection(RelationConstants.COLLECTION_KEY);

        // Important: unique can't prevent a relation from being added twice to the same node
        relationDataCollection.createIndex(
                Indexes.ascending(RelationConstants.ID_KEY,
                        MongoDbUtil.ConcatFields(RelationConstants.RELATION_KEY, RelationConstants.RELATION_NODE_KEY),
                        MongoDbUtil.ConcatFields(RelationConstants.RELATION_KEY, RelationConstants.RELATION_TYPE_KEY)),
                new IndexOptions().unique(true));

        relationDataCollection.createIndex(
                Indexes.ascending(MongoDbUtil.ConcatFields(RelationConstants.RELATION_KEY, RelationConstants.RELATION_CREATOR_KEY)));
    }

    /**
     * Returns the NodeRelation object of the given node
     *
     * @param node --- uuid of the requested node
     * @return NodeRelation object containing all relation data
     */
    @Override
    @NotNull
    public NodeRelation getRelations(@NotNull String node) {
        Objects.requireNonNull(node, "node must be set");
        log.debug(String.format("get relations of node %s", node));


        node = nodeService.getOriginalNode(node).getId();

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
                Aggregates.unwind("$" + RelationConstants.RELATION_KEY),
                Aggregates.set(new Field<>(
                        MongoDbUtil.ConcatFields(RelationConstants.RELATION_KEY, RelationConstants.RELATION_NODE_KEY),
                        "$" + RelationConstants.ID_KEY)),
                Aggregates.replaceRoot("$" + RelationConstants.RELATION_KEY)
        )).into(new ArrayList<>());

        relationData.forEach(x -> x.setType(RelationTypeUtil.invert(x.getType())));

        NodeRelation nodeRelation = nodeRelationCollection.find(Filters.eq(node)).first();
        if (nodeRelation == null) {
            nodeRelation = new NodeRelation(node);
        }
        nodeRelation.getRelations().addAll(relationData);

        return nodeRelation;
    }

    /**
     * Creates a relation between two nodes of the given type
     *
     * @param fromNode     --- the node that contains the relation
     * @param toNode       --- the node we are pointing to
     * @param relationType --- the type of relation
     * @throws NodeRelationException then a relation already exists to that node of the same type
     */
    @Override
    @Permission({CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_RELATIONS})
    public void createRelation(@NotNull @NodePermission({CCConstants.PERMISSION_RELATION})  String fromNode,
                               @NotNull @NodePermission({CCConstants.PERMISSION_RELATION})  String toNode,
                               @NotNull InputRelationType relationType) throws NodeRelationException, InsufficientPermissionException {
        Objects.requireNonNull(fromNode, "fromNode must be set");
        Objects.requireNonNull(toNode, "toNode must be set");
        Objects.requireNonNull(relationType, "relationType must be set");

        log.debug(String.format("create relation from node %s to node %s of type %s", fromNode, toNode, relationType));


        fromNode = nodeService.getOriginalNode(fromNode).getId();
        toNode = nodeService.getOriginalNode(toNode).getId();

        if(Objects.equals(fromNode, toNode)){
            throw new NodeRelationException("Relation cannot point to itself");
        }

        if(!Objects.equals(nodeService.getType(fromNode), CCConstants.CCM_TYPE_IO)) {
            throw new NodeRelationException("Relation can only set from nodes of type "+CCConstants.CCM_TYPE_IO);
        }

        if(!Objects.equals(nodeService.getType(toNode), CCConstants.CCM_TYPE_IO)) {
            throw new NodeRelationException("Relation can only set to nodes of type "+CCConstants.CCM_TYPE_IO);
        }

        MongoCollection<Document> collection = database.getCollection(RelationConstants.COLLECTION_KEY);

        Document relation = new Document(RelationConstants.RELATION_NODE_KEY, toNode)
                .append(RelationConstants.RELATION_CREATOR_KEY, integrityService.getAuthority())
                .append(RelationConstants.RELATION_TIMESTAMP_KEY, new Date())
                .append(RelationConstants.RELATION_TYPE_KEY, relationType.toString());


        createIndexes();

        // TODO can this be bypassed by concurrent writes?
        //check if there is already a relation of the same type
        boolean relationExists = collection.find(
                Filters.and(
                        Filters.eq(fromNode),
                        Filters.elemMatch(RelationConstants.RELATION_KEY,
                                Filters.and(
                                        Filters.eq(RelationConstants.RELATION_NODE_KEY, toNode),
                                        Filters.eq(RelationConstants.RELATION_TYPE_KEY, relationType.toString())
                                )))).first() != null;

        if (relationExists) {
            throw new NodeRelationException(String.format("Relation between %s and %s of type %s already exists", fromNode, toNode, relationType));
        }

        collection.updateOne(Filters.eq(fromNode),
                Updates.combine(
                        Updates.setOnInsert(new Document(RelationConstants.ID_KEY, fromNode)),
                        Updates.push(RelationConstants.RELATION_KEY, relation)
                ),
                new UpdateOptions().upsert(true)
        );
    }

    /**
     * Deletes a relation between two nodes of the given type
     *
     * @param fromNode     --- the node that contains the relation
     * @param toNode       --- the node we are pointing to
     * @param relationType --- the type of relation
     * @throws NodeRelationException then a relation doesn't exist to that node of the given type
     */
    @Override
    @Permission({CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_RELATIONS})
    public void deleteRelation(
            @NotNull @NodePermission({CCConstants.PERMISSION_RELATION}) String fromNode,
            @NotNull @NodePermission({CCConstants.PERMISSION_RELATION}) String toNode,
            @NotNull InputRelationType relationType) throws NodeRelationException, InsufficientPermissionException {
        Objects.requireNonNull(fromNode, "fromNode must be set");
        Objects.requireNonNull(toNode, "toNode must be set");
        Objects.requireNonNull(relationType, "relationType must be set");

        log.debug(String.format("delete relation from node %s to node %s of type %s", fromNode, toNode, relationType));

        fromNode = nodeService.getOriginalNode(fromNode).getId();
        toNode = nodeService.getOriginalNode(toNode).getId();

        createIndexes();
        MongoCollection<Document> collection = database.getCollection(RelationConstants.COLLECTION_KEY);
        UpdateResult result = collection.updateOne(Filters.and(
                        Filters.eq(fromNode),
                        Filters.elemMatch(RelationConstants.RELATION_KEY,
                                Filters.and(
                                        Filters.eq(RelationConstants.RELATION_NODE_KEY, toNode),
                                        Filters.eq(RelationConstants.RELATION_TYPE_KEY, relationType.toString())))),

                Updates.pull(RelationConstants.RELATION_KEY,
                        new Document(RelationConstants.RELATION_NODE_KEY, toNode)
                                .append(RelationConstants.RELATION_TYPE_KEY, relationType.toString())));

        if (result.getModifiedCount() == 0) {
            throw new NodeRelationException(String.format("Relation between %s and %s of type %s does not exists", fromNode, toNode, relationType));
        }
    }

    /**
     * This method replaces the authority with the new one on all nodes with the specified authority
     *
     * @param actualAuthority --- The actual authority name
     * @param newAuthority    --- The new authority name
     */
    @Override
    public void changeAuthority(@NotNull String actualAuthority, @NotNull String newAuthority) {
        Objects.requireNonNull(actualAuthority, "actualAuthority must be set");
        Objects.requireNonNull(newAuthority, "newAuthority must be set");

        log.debug(String.format("change authority from %s to %s", actualAuthority, newAuthority));

        createIndexes();
        MongoCollection<Document> collection = database.getCollection(RelationConstants.COLLECTION_KEY);
        collection.updateMany(
                Filters.elemMatch(RelationConstants.RELATION_KEY, Filters.eq(RelationConstants.RELATION_CREATOR_KEY, actualAuthority)),
                Updates.set(RelationConstants.RELATION_KEY + ".$[item].creator", newAuthority),
                new UpdateOptions().arrayFilters(
                        Arrays.asList(Filters.eq("item." + RelationConstants.RELATION_CREATOR_KEY, actualAuthority))
                )
        );
    }
}
