package org.edu_sharing.plugin_mongo.oplog;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.util.transaction.TransactionListener;
import org.edu_sharing.spring.scope.PrototypeScope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;


@Slf4j
public class MongoAlfOpLogTransactionListener<I extends MongoAlfOpLogData> implements TransactionListener {
    private final MongoAlfOpLogRepository mongoAlfOpLogRepository;

    private final I loggingActionData;
    private final Consumer<I> handleCommitCallback;

    private MongoAlfOpLog entry;

    public MongoAlfOpLogTransactionListener(@NonNull MongoAlfOpLogRepository mongoAlfOpLogRepository, @NonNull I loggingAction, @NonNull Consumer<I> handleCommitCallback) {
        this.mongoAlfOpLogRepository = mongoAlfOpLogRepository;
        this.loggingActionData = loggingAction;
        this.handleCommitCallback = handleCommitCallback;
    }

    @Override
    public void beforeCommit(boolean readOnly) {
        entry = mongoAlfOpLogRepository.save(new MongoAlfOpLog(null, loggingActionData, new Date()));
    }

    @Override
    public void beforeCompletion() {

    }

    @Override
    public void afterCommit() {
        handleCommitCallback.accept(loggingActionData);
        handleLogDeletion();
    }

    @Override
    public void afterRollback() {
        handleLogDeletion();
    }

    private void handleLogDeletion() {
        try {
            mongoAlfOpLogRepository.delete(entry);
        } catch (Exception e) {
            MongoAlfOpLogTransactionListener.log.error("Error on deleting logging action {}: {}", entry.getId(), e.getMessage(), e);
        }
    }
}
