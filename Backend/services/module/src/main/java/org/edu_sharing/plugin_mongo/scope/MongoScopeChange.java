package org.edu_sharing.plugin_mongo.scope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.service.scope.ScopeChange;
import org.edu_sharing.service.scope.ScopeChangeType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * change feed for the scope tracker (ADR-1), read in (at, seq) order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = ScopeConstants.COLLECTION_CHANGE)
@CompoundIndex(name = "at_1_seq_1", def = "{'at': 1, 'seq': 1}")
public class MongoScopeChange {
    @Id
    private String id;
    @Indexed(name = "seq_1", unique = true)
    private long seq;
    private String scopeId;
    private List<String> nodeIds;
    private ScopeChangeType type;
    /** set when inserted. entries expire, the tracker only needs the recent ones */
    @Indexed(name = "at_ttl", expireAfter = ScopeConstants.CHANGE_TTL)
    private Date at;

    public ScopeChange toChange() {
        return ScopeChange.builder()
                .seq(seq)
                .scopeId(scopeId)
                .nodeIds(nodeIds)
                .type(type)
                .at(at)
                .build();
    }
}
