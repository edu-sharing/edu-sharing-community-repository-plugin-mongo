package org.edu_sharing.plugin_mongo.jobs.quarz;


import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLog;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogRepository;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogRetryHandler;
import org.edu_sharing.repository.server.jobs.quartz.AbstractJobMapAnnotationParams;
import org.edu_sharing.repository.server.jobs.quartz.annotation.JobDescription;
import org.edu_sharing.repository.server.jobs.quartz.annotation.JobFieldDescription;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A job for retrying operations on failed or missing MongoDB Alfresco operation logs.
 * <p>
 * This job processes entries from the MongoDB Alfresco operation logs that have
 * timestamps before a specified offset relative to the current time. Each entry is
 * retried using the appropriate retry handler. Successfully retried entries are
 * removed from the database; failed retries are logged for further investigation.
 * <p>
 * The class extends {@link AbstractJobMapAnnotationParams}, providing logic for
 * parameterized execution using job data maps.
 * <p>
 * Required components, such as a repository to access logs and a list of retry
 * handlers, are injected via Spring's dependency injection.
 *
 * Responsibilities:
 * - Retrieve operation logs older than the specified time offset from the current time.
 * - Match the operation log to the appropriate retry handler based on the log data type.
 * - Process each operation log by attempting retry using the appropriate handler.
 * - Log success or failure of retry operations.
 * - Remove successfully processed logs from the database.
 */
@Slf4j
@JobDescription(
        description = "This job processes entries from the MongoDB Alfresco operation logs that have " +
                "timestamps before a specified offset relative to the current time. Each entry is " +
                "retried using the appropriate retry handler. Successfully retried entries are " +
                "removed from the database; failed retries are logged for further investigation."
)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RetryFailedOrMissingMongoAlfOpLogJob extends AbstractJobMapAnnotationParams {

    @Autowired
    private MongoAlfOpLogRepository mongoAlfOpLogRepository;

    @Autowired
    private List<MongoAlfOpLogRetryHandler<?>> retryHandlers;

    @JobFieldDescription(
            description = "We need to set an min time offset in milliseconds, so that no running transactions are effected. The default is 10 min",
            sampleValue = "36000000")
    protected int minTimeOffset = 24*60*60*1000;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        Pageable pageable = Pageable.ofSize(1000);
        int succeeded = 0;
        int failed = 0;
        int total = 0;

        Map<? extends Class<?>, ? extends MongoAlfOpLogRetryHandler<?>> mappedRetryHandlers = retryHandlers.stream()
                .collect(Collectors.toMap(MongoAlfOpLogRetryHandler::getRetryableType, x -> x));


        Page<MongoAlfOpLog> oplogs;
        do {
            oplogs = mongoAlfOpLogRepository.findAllByTimestampBefore(new Date(System.currentTimeMillis() - minTimeOffset), pageable);
            if(oplogs.isEmpty()){
                break;
            }

            for (MongoAlfOpLog oplog : oplogs) {
                try {
                    MongoAlfOpLogData data = oplog.getData();
                    log.info("Retrying oplog {} with data {}", oplog.getId(), data);
                    MongoAlfOpLogRetryHandler<?> mongoAlfOpLogRetryHandler = mappedRetryHandlers.get(data.getClass());
                    mongoAlfOpLogRetryHandler.retry(data);
                    mongoAlfOpLogRepository.delete(oplog);
                    succeeded++;
                } catch (Exception e) {
                    log.error("Error on retrying oplog {}: {}", oplog.getId(), e.getMessage(), e);
                    failed++;
                }
            }

            pageable = pageable.next();
        }
        while (true);
        log.info("Finished retrying {} oplogs, with {} succeeded and {} failed jobs", total, succeeded, failed);
    }
}
