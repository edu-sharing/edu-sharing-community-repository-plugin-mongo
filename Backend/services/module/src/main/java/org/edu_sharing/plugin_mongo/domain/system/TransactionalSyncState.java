package org.edu_sharing.plugin_mongo.domain.system;

import lombok.Data;

@Data
public class TransactionalSyncState {
    private Long lastTransactionId;
}
