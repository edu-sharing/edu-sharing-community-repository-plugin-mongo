package org.edu_sharing.plugin_mongo.oplog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.util.transaction.TransactionListener;
import org.edu_sharing.spring.scope.PrototypeScope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Consumer;


@Slf4j
@Component
@PrototypeScope
@RequiredArgsConstructor
public class MongoAlfOpLogTransactionListener<I extends MongoAlfOpLogData> implements TransactionListener {
    private final MongoAlfOpLogRepository mongoAlfOpLogRepository;

    private I loggingActionData;
    private MongoAlfOpLog entry;
    private Consumer<I> handleCommitCallback;

    public void setLoggingActionData(I loggingAction, Consumer<I> handleCommitCallback) {
        if(loggingAction == null){
            throw new IllegalArgumentException("Logging action must not be null!");
        }

        if(handleCommitCallback == null){
            throw new IllegalArgumentException("Handle commit callback must not be null!");
        }

        this.loggingActionData = loggingAction;
        this.handleCommitCallback = handleCommitCallback;
    }


    @Override
    public void beforeCommit(boolean readOnly) {
        if(loggingActionData == null){
            throw new IllegalStateException("No logging action set!");
        }

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
