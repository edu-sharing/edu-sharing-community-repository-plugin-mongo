package org.edu_sharing.plugin_mongo.oplog;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * Service responsible for managing and registering operational log (oplog) actions
 * within a transaction context. This includes logging actions tied to MongoDB and
 * associating callbacks that execute upon transaction commit.
 *
 * The primary use case of this service is to facilitate the tracking, logging, and
 * synchronized handling of operations within the lifecycle of transactions.
 *
 * This service relies on the {@link MongoAlfOpLogRepository} to persist and manage
 * operational log entries and integrates these logs into Alfresco transaction
 * workflows.
 *
 * Features:
 * - Registers operational logs with associated callbacks.
 * - Automatically binds transaction lifecycle listeners to ensure log handling is
 *   tied to transaction commit or rollback events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MongoAlfOpLogService {
    private final MongoAlfOpLogRepository mongoAlfOpLogRepository;

    /**
     * Registers an operational log (oplog) action within the context of a transaction,
     * associating it with a callback to handle the action upon transaction commit.
     * This method is primarily used to log specific actions that occur during a
     * transaction, enabling tracking and processing of these actions in a synchronized
     * manner with transaction lifecycle events.
     *
     * @param loggingAction       The operational log data to be registered. This should
     *                            implement the {@link MongoAlfOpLogData} interface and
     *                            contains the details of the action to be logged.
     * @param handleCommitCallback A callback function that is executed after the transaction
     *                            is successfully committed. This callback receives the
     *                            operational log data as input and facilitates custom
     *                            processing for the committed log.
     */
    public <T extends MongoAlfOpLogData> void registerOpLogAction(@NonNull T loggingAction, @NonNull Consumer<T> handleCommitCallback) {
        MongoAlfOpLogTransactionListener<T> tMongoAlfOpLogTransactionListener = new MongoAlfOpLogTransactionListener<>(mongoAlfOpLogRepository, loggingAction, handleCommitCallback);
        AlfrescoTransactionSupport.bindListener(tMongoAlfOpLogTransactionListener);
        log.debug("Registered oplog action {} for transaction listener {}", loggingAction, tMongoAlfOpLogTransactionListener);
    }
}
