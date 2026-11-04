package org.edu_sharing.plugin_mongo.scope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.service.scope.ScopeHistoryEntry;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * append only
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = ScopeConstants.COLLECTION_HISTORY)
@CompoundIndexes({
        @CompoundIndex(name = "scopeId_1_nodeId_1_at_-1", def = "{'scopeId': 1, 'nodeId': 1, 'at': -1}"),
        @CompoundIndex(name = "actor_1_at_-1", def = "{'actor': 1, 'at': -1}"),
        // purge of a node removes the history of all its scopes
        @CompoundIndex(name = "nodeId_1", def = "{'nodeId': 1}")
})
public class MongoScopeHistoryEntry {
    @Id
    private String id;
    private String scopeId;
    private String nodeId;
    /** null for the deletion of the whole entry */
    private String property;
    private List<String> from;
    private List<String> to;
    private String actor;
    private Date at;
    private String reason;
    private Long baseQueryVersion;
    private String contentVersion;

    public ScopeHistoryEntry toHistoryEntry() {
        return ScopeHistoryEntry.builder()
                .scopeId(scopeId)
                .nodeId(nodeId)
                .property(property)
                .from(from)
                .to(to)
                .actor(actor)
                .at(at)
                .reason(reason)
                .baseQueryVersion(baseQueryVersion)
                .contentVersion(contentVersion)
                .build();
    }
}
