package org.edu_sharing.plugin_mongo.relation;

import com.mongodb.client.result.UpdateResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.bson.Document;
import org.edu_sharing.plugin_mongo.oplog.io.DeleteIoMongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.io.MongodbIoDeletedAware;
import org.edu_sharing.plugin_mongo.tracking.MongoTrackingDataUtils;
import org.edu_sharing.plugin_mongo.tracking.MongoTrackingService;
import org.edu_sharing.plugin_mongo.tracking.TrackingServiceCallback;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.restservices.relation.v1.model.CreateRelationRequest;
import org.edu_sharing.restservices.relation.v1.model.UpdateRelationRequest;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.relations.RelationData;
import org.edu_sharing.service.nodeservice.annotation.NodeOriginal;
import org.edu_sharing.service.relations.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Primary
@Service("relationService")
@RequiredArgsConstructor
public class MongoRelationService implements RelationService, TrackingServiceCallback<RelationData>, MongodbIoDeletedAware {


    private final RelationRepository relationRepository;
    private final MongoTemplate mongoTemplate;
    private final NodeService nodeService;
    private final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    private final MongoTrackingService mongoTrackingService;


    @Override
    @PreAuthorize("T(org.edu_sharing.service.authority.AuthorityServiceHelper).isAdmin()")
    public List<RelationData> getTrackedData(@NotNull @NonNull Date from, Date to, Limit limit) {
        return (to !=null
                ? relationRepository.findAllByTimestampBetween(from, to, limit)
                : relationRepository.findAllByTimestampAfter(from, limit))
                .stream()
                .map(RelationData.class::cast)
                .toList();
    }

    @Override
    @PreAuthorize("T(org.edu_sharing.service.authority.AuthorityServiceHelper).isAdmin()")
    public List<RelationData> getDeletedTrackedData(@NotNull @NonNull Date from, Date to, Limit limit) {
        return mongoTrackingService.getDeletedTrackedData(from, to, limit, MongoNodeRelation.class, RelationData.class);
    }


    @NotNull
    @Override
    @PreAuthorize("hasPermission(#nodeId, T(org.edu_sharing.repository.client.tools.CCConstants).PERMISSION_READ)")
    public List<RelationData> getRelations(@NodeOriginal @NotNull String nodeId) {

        final String COLLECTION_RELATIONS_V2 = "relationsV2";

        final String FIELD_FROM_NODE = "fromNode";
        final String FIELD_TO_NODE = "toNode";
        final String FIELD_TYPE = "type";
        final String FIELD_METADATA = "metadata";

        final String FIELD_DIRECTION = "direction";

        final String DIRECTION_OUTGOING = "OUTGOING";
        final String DIRECTION_INCOMING = "INCOMING";


        final Criteria nodeIsEitherEnd = new Criteria().orOperator(
                Criteria.where(FIELD_FROM_NODE).is(nodeId),
                Criteria.where(FIELD_TO_NODE).is(nodeId)
        );

        MatchOperation match = Aggregation.match(nodeIsEitherEnd);


        final ConditionalOperators.Cond isOutgoing = ConditionalOperators
                .when(ComparisonOperators.Eq.valueOf(FIELD_FROM_NODE).equalToValue(nodeId))
                .then(DIRECTION_OUTGOING)
                .otherwise(DIRECTION_INCOMING);

        final ConditionalOperators.Cond fromNode = ConditionalOperators
                .when(ComparisonOperators.Eq.valueOf(FIELD_FROM_NODE).equalToValue(nodeId))
                .thenValueOf(FIELD_FROM_NODE)
                .otherwiseValueOf(FIELD_TO_NODE);

        final ConditionalOperators.Cond toNode = ConditionalOperators
                .when(ComparisonOperators.Eq.valueOf(FIELD_FROM_NODE).equalToValue(nodeId))
                .thenValueOf(FIELD_TO_NODE)
                .otherwiseValueOf(FIELD_FROM_NODE);

        final AddFieldsOperation normalizeFields = Aggregation.addFields()
                .addFieldWithValue(FIELD_METADATA,
                        ConditionalOperators.ifNull(FIELD_METADATA).then(Collections.emptyMap()))
                .addFieldWithValue(FIELD_FROM_NODE, fromNode)
                .addFieldWithValue(FIELD_TO_NODE, toNode)
                .addFieldWithValue(FIELD_DIRECTION, isOutgoing)
                .build();

        List<ConditionalOperators.Switch.CaseOperator> outputTypeCases = Stream.concat(
                        Stream.of(
                                // If the relation is outgoing, keep the original type
                                ConditionalOperators.Switch.CaseOperator
                                        .when(ComparisonOperators.Eq.valueOf(FIELD_DIRECTION).equalToValue(DIRECTION_OUTGOING))
                                        .then("$" + FIELD_TYPE)
                        ),
                        // If incoming, map input relation type to its reversed output type
                        RelationTypeUtil.reverseInputRelationTypeSet.entrySet().stream()
                                .map(entry -> ConditionalOperators.Switch.CaseOperator
                                        .when(ComparisonOperators.Eq.valueOf(FIELD_TYPE).equalToValue(entry.getKey().name()))
                                        .then(entry.getValue().name()))

                )
                .toList();

        AddFieldsOperation mapOutputType = Aggregation.addFields()
                .addFieldWithValue(FIELD_TYPE, ConditionalOperators.switchCases(outputTypeCases).defaultTo("$" + FIELD_TYPE))
                .build();

        Aggregation aggregation = Aggregation.newAggregation(match, normalizeFields, mapOutputType);
        AggregationResults<Document> relationsV2 = mongoTemplate.aggregate(aggregation, COLLECTION_RELATIONS_V2, Document.class);

        return relationsV2.getMappedResults()
                .stream()
                .map(doc -> projectionFactory.createProjection(RelationData.class, doc))
                .toList();
    }

    @NotNull
    @Override
    @PreAuthorize("@toolPermissionServiceImpl.hasToolPermission(T(org.edu_sharing.repository.client.tools.CCConstants).CCM_VALUE_TOOLPERMISSION_MANAGE_RELATIONS) " +
            "and hasPermission(#request.fromNode, T(org.edu_sharing.repository.client.tools.CCConstants).PERMISSION_RELATION)" +
            "and hasPermission(#request.toNode, T(org.edu_sharing.repository.client.tools.CCConstants).PERMISSION_RELATION)")
    public RelationData createRelation(@NotNull CreateRelationRequest request) {

        if (request.fromNode().equals(request.toNode())) {
            throw new IllegalArgumentException("Cannot create relation between the same node");
        }

        if (!Objects.equals(nodeService.getType(request.fromNode()), CCConstants.CCM_TYPE_IO)) {
            throw new IllegalArgumentException("Cannot create relation from non-IO node");
        }

        if (!Objects.equals(nodeService.getType(request.toNode()), CCConstants.CCM_TYPE_IO)) {
            throw new IllegalArgumentException("Cannot create relation to non-IO node");
        }

        MongoNodeRelation.MongoNodeRelationBuilder relationBuilder = MongoNodeRelation.builder()
                .fromNode(request.fromNode())
                .toNode(request.toNode())
                .type(request.type())
                .aiGenerated(request.isAiGenerated())
                .createdBy(AuthenticationUtil.getFullyAuthenticatedUser())
                .createdAt(new Date());

        EvaluationData.EvaluationDataBuilder evaluationDataBuilder = EvaluationData.builder();
        if (request.isEvaluated()) {
            evaluationDataBuilder
                    .approved(true)
                    .approvedAt(new Date())
                    .approvedBy(AuthenticationUtil.getFullyAuthenticatedUser());
        }
        relationBuilder.evaluation(evaluationDataBuilder.build());

        return relationRepository.save(relationBuilder.build());
    }

    @NotNull
    @Override
    @PreAuthorize("@toolPermissionServiceImpl.hasToolPermission(T(org.edu_sharing.repository.client.tools.CCConstants).CCM_VALUE_TOOLPERMISSION_MANAGE_RELATIONS) " +
            "and hasPermission(#request.fromNode, T(org.edu_sharing.repository.client.tools.CCConstants).PERMISSION_RELATION)" +
            "and hasPermission(#request.toNode, T(org.edu_sharing.repository.client.tools.CCConstants).PERMISSION_RELATION)")
    public RelationData updateRelation(@NotNull UpdateRelationRequest request) {
        MongoNodeRelation relation = relationRepository.findByFromNodeAndToNodeAndType(request.fromNode(), request.toNode(), request.type())
                .orElseThrow(() -> new IllegalArgumentException("Relation not found"));

        relation.setMetadata(request.metadata());
        relation.setModifiedBy(AuthenticationUtil.getFullyAuthenticatedUser());
        relation.setModifiedAt(new Date());
        return relationRepository.save(relation);
    }

    @Override
    @PreAuthorize("@toolPermissionServiceImpl.hasToolPermission(T(org.edu_sharing.repository.client.tools.CCConstants).CCM_VALUE_TOOLPERMISSION_MANAGE_RELATIONS) " +
            "and hasPermission(#fromNode, T(org.edu_sharing.repository.client.tools.CCConstants).PERMISSION_RELATION)" +
            "and hasPermission(#toNode, T(org.edu_sharing.repository.client.tools.CCConstants).PERMISSION_RELATION)")
    public void deleteRelation(@NotNull String fromNode, @NotNull String toNode, @NotNull InputRelationType relationType) {
        log.debug("delete relation from node {} to node {} of type {}", fromNode, toNode, relationType);
        Optional<MongoNodeRelation> relation = relationRepository.findByFromNodeAndToNodeAndType(fromNode, toNode, relationType);
        relation.ifPresent(x -> {
            mongoTrackingService.trackDeletedData(x.toEssential());
            relationRepository.delete(x);
        });
    }

    @Override
    @PreAuthorize("T(org.edu_sharing.service.authority.AuthorityServiceHelper).isAdmin()")
    public void changeAuthority(@NotNull String actualAuthority, @NotNull String newAuthority) {
        log.debug("change authority from {} to {}", actualAuthority, newAuthority);

        // Match documents where either creator or evaluation.approvedBy equals actualAuthority
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("creator").is(actualAuthority),
                Criteria.where("evaluation.approvedBy").is(actualAuthority)
        );
        Query query = new Query(criteria);

        // Use aggregation-style update pipeline
        AggregationUpdate update = AggregationUpdate.update()
                .set("createdBy").toValue(
                        ConditionalOperators.when(ComparisonOperators.Eq.valueOf("createdBy").equalToValue(actualAuthority))
                                .then(newAuthority)
                                .otherwise("$createdBy")
                )
                .set("modifiedBy").toValue(
                        ConditionalOperators.when(ComparisonOperators.Eq.valueOf("modifiedBy").equalToValue(actualAuthority))
                                .then(newAuthority)
                                .otherwise("$modifiedBy")
                )
                .set("evaluation.approvedBy").toValue(
                        ConditionalOperators.when(ComparisonOperators.Eq.valueOf("evaluation.approvedBy").equalToValue(actualAuthority))
                                .then(newAuthority)
                                .otherwise("$evaluation.approvedBy")
                );
        MongoTrackingDataUtils.updateTimeStamp(update);
        UpdateResult result = mongoTemplate.updateMulti(query, update, MongoNodeRelation.class);
        log.debug("Updated {} documents changing authority from {} to {}",
                result.getModifiedCount(), actualAuthority, newAuthority);
    }

    @NotNull
    @Override
    @PreAuthorize("@toolPermissionServiceImpl.hasToolPermission(T(org.edu_sharing.repository.client.tools.CCConstants).CCM_VALUE_TOOLPERMISSION_MANAGE_RELATIONS) " +
            "and hasPermission(#fromNode, T(org.edu_sharing.repository.client.tools.CCConstants).PERMISSION_RELATION)" +
            "and hasPermission(#toNode, T(org.edu_sharing.repository.client.tools.CCConstants).PERMISSION_RELATION)")
    public RelationData approveRelation(@NotNull String fromNode, @NotNull String toNode, @NotNull InputRelationType relationType) {
        MongoNodeRelation relation = relationRepository.findByFromNodeAndToNodeAndType(fromNode, toNode, relationType)
                .orElseThrow(() -> new IllegalArgumentException("Relation not found"));

        if (relation.getEvaluation() != null && relation.getEvaluation().isApproved()) {
            throw new IllegalStateException("Relation is already evaluated");
        }

        EvaluationData evaluation = EvaluationData.builder()
                .approved(true)
                .approvedAt(new Date())
                .approvedBy(AuthenticationUtil.getFullyAuthenticatedUser())
                .build();

        relation.setEvaluation(evaluation);
        return relationRepository.save(relation);
    }

    @Override
    public void onIoDeleted(DeleteIoMongoAlfOpLogData actionData) {
        String originalNode = nodeService.getOriginalNode(actionData.getNodeId()).getId();
        List<String> publishedCopies = nodeService.getPublishedCopies(originalNode);
        if(publishedCopies.isEmpty()) {
            List<MongoNodeRelation> nodesToDelete = relationRepository.findAllByFromNodeOrToNode(originalNode, originalNode);
            mongoTrackingService.trackDeletedData(nodesToDelete.stream().map(MongoNodeRelation::toEssential).toList());
            relationRepository.deleteAll(nodesToDelete);
        }
    }
}
