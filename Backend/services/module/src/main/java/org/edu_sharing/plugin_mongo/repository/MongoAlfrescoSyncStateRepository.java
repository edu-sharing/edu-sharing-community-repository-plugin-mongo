package org.edu_sharing.plugin_mongo.repository;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.edu_sharing.plugin_mongo.domain.system.TransactionalSyncState;
import org.edu_sharing.plugin_mongo.mongo.automation.annotation.Initialize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@NoArgsConstructor // Required for proxying by CGLib (CGLib is used because we don't use an
// interface here...)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MongoAlfrescoSyncStateRepository {
  private static final String ALFRESCO_SYNC_STATE_COLLECTION = "alfrescoSyncState";
  private static final String DELETED_REFERENCED_NODES_COLLECTION =
      "alfrescoSyncState.tracking.deletedReferenceNodes";
  private static final String TRANSACTIONAL_SYNC_STATE_ID = "transactionalSyncState";
  public static final String ID = "_id";
  public static final String MODIFIED = "modified";

  @NonNull private MongoDatabase mongoDatabase;

  @Initialize
  public void createIndices() {
    mongoDatabase
        .getCollection(DELETED_REFERENCED_NODES_COLLECTION)
        .createIndex(Indexes.ascending(MODIFIED));
  }

  @NonNull
  public TransactionalSyncState getTransactionalSyncState() {
    return Optional.ofNullable(
            mongoDatabase
                .getCollection(ALFRESCO_SYNC_STATE_COLLECTION, TransactionalSyncState.class)
                .find(Filters.eq(TRANSACTIONAL_SYNC_STATE_ID))
                .first())
        .orElse(new TransactionalSyncState());
  }

  public boolean setTransactionalSyncState(@NonNull TransactionalSyncState syncState) {
    UpdateResult updateResult =
        mongoDatabase
            .getCollection(ALFRESCO_SYNC_STATE_COLLECTION, TransactionalSyncState.class)
            .replaceOne(
                Filters.eq(TRANSACTIONAL_SYNC_STATE_ID),
                syncState,
                new ReplaceOptions().upsert(true));

    return updateResult.wasAcknowledged()
        && (updateResult.getModifiedCount() > 0 || updateResult.getUpsertedId() != null);
  }

  public boolean setDeletedNodeIdsToTrack(Set<String> nodeIdsToKeepChecking) {
    if (nodeIdsToKeepChecking.size() == 0) {
      return true;
    }

    MongoCollection<Document> collection =
        mongoDatabase.getCollection(DELETED_REFERENCED_NODES_COLLECTION);
    BulkWriteResult result =
        collection.bulkWrite(
            nodeIdsToKeepChecking.stream()
                .map(
                    x ->
                        new UpdateOneModel<Document>(
                            Filters.eq(x),
                            Updates.currentTimestamp(MODIFIED),
                            new UpdateOptions().upsert(true)))
                .collect(Collectors.toList()));

    return result.wasAcknowledged()
        && (result.getModifiedCount() > 0 || result.getUpserts().size() > 0);
  }

  @NonNull
  public List<String> getDeletedNodeIdsToTrack(int amount) {
    return mongoDatabase
        .getCollection(DELETED_REFERENCED_NODES_COLLECTION, Document.class)
        .find()
        .sort(new Document(MODIFIED, 1))
        .projection(Projections.include(ID))
        .limit(amount)
        .map(x -> (String) x.get(ID))
        .into(new ArrayList<>());
  }

  public boolean removeDeletedNodeIdsToTrack(Set<String> nodeIdsToDelete) {
    if (nodeIdsToDelete.size() == 0) {
      return true;
    }

    DeleteResult result =
        mongoDatabase
            .getCollection(DELETED_REFERENCED_NODES_COLLECTION)
            .deleteMany(Filters.in(ID, nodeIdsToDelete));
    return result.wasAcknowledged() && result.getDeletedCount() > 0;
  }

  public void reset() {
    mongoDatabase.getCollection(ALFRESCO_SYNC_STATE_COLLECTION).deleteMany(new Document());
    mongoDatabase.getCollection(DELETED_REFERENCED_NODES_COLLECTION).deleteMany(new Document());
  }
}
