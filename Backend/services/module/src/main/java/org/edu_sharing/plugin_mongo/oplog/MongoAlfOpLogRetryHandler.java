package org.edu_sharing.plugin_mongo.oplog;

/**
 * Defines a handler interface for retrying MongoDB operational log (oplog) actions
 * within the MongoAlfOpLog infrastructure. This interface is designed to handle
 * specific types of {@link MongoAlfOpLogData} that need retryable operations.
 *
 * Implementations of this interface are responsible for:
 * - Specifying the type of {@link MongoAlfOpLogData} they can handle using the method {@code getRetryableType()}.
 * - Implementing the retry logic for the associated {@link MongoAlfOpLogData} type through the {@code retry()} method.
 *
 * @param <T> The type of {@link MongoAlfOpLogData} that this handler supports.
 */
public interface MongoAlfOpLogRetryHandler <T extends MongoAlfOpLogData> {
    /**
     * Retrieves the type of {@link MongoAlfOpLogData} that this handler is responsible for managing
     * during retryable operations.
     *
     * This method is used to define the specific type of operational log data that can
     * be processed by the implementing handler.
     *
     * @return the {@link Class} representing the type of {@link MongoAlfOpLogData} that this handler supports.
     */
    Class<T> getRetryableType();
    /**
     * Implements a retry mechanism for a provided instance of {@link MongoAlfOpLogData}.
     * This method is responsible for handling operations that failed or need to be retried
     * for specific types of operational log data within the MongoAlfOpLog infrastructure.
     *
     * The retry process may involve reprocessing, reattempting persistence, or any
     * custom-defined retry logic depending on the type of {@link MongoAlfOpLogData}.
     *
     * @param opLogData The operational log data instance that should be retried.
     *                  The concrete implementation of {@link MongoAlfOpLogData}
     *                  determines the specific retry logic to be applied.
     */
    void retry(MongoAlfOpLogData opLogData);
}
