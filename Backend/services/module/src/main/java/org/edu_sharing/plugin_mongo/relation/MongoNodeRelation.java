package org.edu_sharing.plugin_mongo.relation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.plugin_mongo.tracking.TrackedData;
import org.edu_sharing.service.relations.InputRelationType;
import org.edu_sharing.service.relations.OutputRelationType;
import org.edu_sharing.service.relations.RelationData;
import org.edu_sharing.service.relations.RelationTypeUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

// TODO endpunkt: such endpunkt  parameter nach typen, aigeneriert, metadaten, nodeId, validiert -> relations -> Einschränkungen in DB, Rechte (community suche) elastic als nested abbilden
// TODO endpunkt: pfadsuche mit Tiefenbegrenzung (cycles, hierarchie?) NICE TO HAVE -> node + relations?
@Data
@Builder
@Document("relationsV2")
@CompoundIndexes({
        @CompoundIndex(name = "fromNode_1_toNode_1_type_1", def = "{'fromNode': 1, 'toNode': 1, 'type': 1,}", unique = true),
})
@NoArgsConstructor
@AllArgsConstructor
public class MongoNodeRelation implements RelationData, TrackedData {
    @Id
    private String id;
    private String fromNode;
    private String toNode;
    @Indexed
    private String createdBy;
    private Date created;
    private String modifiedBy;
    private Date modified;
    private InputRelationType type;
    @Indexed
    private Date timestamp; //for tracking

    private boolean aiGenerated;
    private EvaluationData evaluation;
    private Map<String, Object> metadata;

    public OutputRelationType getType() {
        return OutputRelationType.valueOf(type.name());
    }

    public OutputRelationType getReverseType() {
        return RelationTypeUtil.reverse(getType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MongoNodeRelation that = (MongoNodeRelation) o;
        return Objects.equals(fromNode, that.fromNode)
                && Objects.equals(toNode, that.toNode)
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromNode, toNode, type);
    }

    /**
     * Creates an essential representation of the current {@code MongoNodeRelation} object.
     * The essential representation includes only the core attributes: {@code id}, {@code fromNode},
     * {@code toNode}, and {@code type}.
     *
     * @return A new {@code MongoNodeRelation} instance containing the essential fields of the current object.
     */
    public MongoNodeRelation toEssential(){
        return MongoNodeRelation.builder()
                .id(id)
                .fromNode(fromNode)
                .toNode(toNode)
                .type(type)
                .build();
    }
}

