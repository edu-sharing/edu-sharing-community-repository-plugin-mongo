package org.edu_sharing.plugin_mongo.tracking;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MongoTrackingService {

    private final MongoTemplate mongoTemplate;
    private final DeletedTrackedDataRepository deletedTrackedDataRepository;

    public List<TrackedData> getTrackedData(@NonNull @NotNull String collectionName, @NonNull @NotNull Date timestamp, int limit) {
        return mongoTemplate.find(Query.query(Criteria.where("timestamp").gt(timestamp)).limit(limit), TrackedData.class, collectionName);
    }

    @SafeVarargs
    public final <T extends TrackedData> void trackDeletedData(T... nodesToDelete) {
        deletedTrackedDataRepository.saveAll(Arrays.stream(nodesToDelete)
                .map(x -> DeletedTrackedData.builder()
                        .content(x)
                        .collectionName(mongoTemplate.getCollectionName(x.getClass()))
                        .build())
                .toList());
    }

    public <T extends TrackedData> void trackDeletedData(List<T> nodesToDelete) {
        deletedTrackedDataRepository.saveAll(nodesToDelete.stream()
                .map(x -> DeletedTrackedData.builder()
                        .content(x)
                        .collectionName(mongoTemplate.getCollectionName(x.getClass()))
                        .build())
                .toList());
    }

    public <T> List<DeletedTrackedData<T>> getDeletedTrackedData(Date from, Limit limit, Class<? extends T> entityClass) {
        return deletedTrackedDataRepository.findAllByTimestampAfterAndCollectionName(from, mongoTemplate.getCollectionName(entityClass), limit);
    }
}
