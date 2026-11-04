package org.edu_sharing.plugin_mongo.scope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.service.scope.ScopeBulkRequest;
import org.edu_sharing.service.scope.ScopeBulkStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = ScopeConstants.COLLECTION_BULK_JOB)
public class MongoScopeBulkJob {
    @Id
    private String jobId;
    @Indexed(name = "scopeId_1")
    private String scopeId;
    private ScopeBulkStatus.State state;
    private long total;
    private long done;
    private List<ScopeBulkStatus.Failure> failed;
    private String startedBy;
    private Date startedAt;
    private Date finishedAt;
    private ScopeBulkRequest request;

    public ScopeBulkStatus toStatus() {
        return ScopeBulkStatus.builder()
                .jobId(jobId)
                .scopeId(scopeId)
                .state(state)
                .total(total)
                .done(done)
                .failed(failed)
                .startedBy(startedBy)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .build();
    }
}
