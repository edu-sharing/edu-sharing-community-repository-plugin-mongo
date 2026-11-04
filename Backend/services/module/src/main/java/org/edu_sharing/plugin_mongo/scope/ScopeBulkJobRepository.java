package org.edu_sharing.plugin_mongo.scope;

import org.edu_sharing.service.scope.ScopeBulkStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ScopeBulkJobRepository extends MongoRepository<MongoScopeBulkJob, String> {

    Optional<MongoScopeBulkJob> findByJobIdAndScopeId(String jobId, String scopeId);

    long deleteAllByScopeId(String scopeId);

    List<MongoScopeBulkJob> findAllByStateInAndStartedAtBefore(Collection<ScopeBulkStatus.State> states, Date startedAt);
}
