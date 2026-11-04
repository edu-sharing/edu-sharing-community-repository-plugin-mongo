package org.edu_sharing.plugin_mongo.scope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.service.scope.ScopeAssignment;
import org.edu_sharing.service.scope.ScopeSource;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = ScopeConstants.COLLECTION_ASSIGNMENT)
@CompoundIndexes({
        @CompoundIndex(name = "scopeId_1_nodeId_1", def = "{'scopeId': 1, 'nodeId': 1}", unique = true),
        @CompoundIndex(name = "scopeId_1_published_1", def = "{'scopeId': 1, 'published': 1}")
})
public class MongoScopeAssignment {
    /** "&lt;scopeId&gt;:&lt;nodeId&gt;" */
    @Id
    private String id;
    private String scopeId;
    @Indexed(name = "nodeId_1")
    private String nodeId;
    private Map<String, List<String>> properties;
    private boolean published;
    private boolean excluded;
    private ScopeSource source;
    private Date created;
    private String createdBy;
    private Date modified;
    private String modifiedBy;

    public static String createId(String scopeId, String nodeId) {
        return scopeId + ":" + nodeId;
    }

    public ScopeAssignment toAssignment() {
        return ScopeAssignment.builder()
                .scopeId(scopeId)
                .nodeId(nodeId)
                .properties(properties)
                .published(published)
                .excluded(excluded)
                .source(source)
                .created(created)
                .createdBy(createdBy)
                .modified(modified)
                .modifiedBy(modifiedBy)
                .build();
    }
}
