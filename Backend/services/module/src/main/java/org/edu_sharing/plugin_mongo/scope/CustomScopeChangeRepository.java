package org.edu_sharing.plugin_mongo.scope;

import org.edu_sharing.service.scope.ScopeChangeType;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface CustomScopeChangeRepository {

    /**
     * appends a change with the next sequence number. at is set here, not by the caller (ADR-1)
     */
    MongoScopeChange append(String scopeId, Collection<String> nodeIds, ScopeChangeType type);

    /**
     * cursor (at, seq) like the share info oplog, ordered by (at, seq) ascending
     *
     * @param after    exclusive, null to start from the beginning
     * @param afterSeq without after: a standalone seq cursor. With after: tiebreaker for entries at exactly after
     * @param until    inclusive, may be null
     */
    List<MongoScopeChange> findChanges(Date after, Long afterSeq, Date until, int limit);
}
