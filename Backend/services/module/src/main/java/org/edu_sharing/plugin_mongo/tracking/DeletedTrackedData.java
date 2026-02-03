package org.edu_sharing.plugin_mongo.tracking;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TimeSeries;

import java.util.Date;
import java.util.function.Function;

@Data
@Builder
@TimeSeries(timeField = "timestamp", metaField = "collectionName", expireAfter = "${repository.mongoDataTracking.expireAfter}")
@NoArgsConstructor
@AllArgsConstructor
public class DeletedTrackedData<T> implements TrackedData {
    @Id
    private String id;

    private String collectionName;
    private T content;

    private Date timestamp;

    public <S> DeletedTrackedData<S> mapTo(Function<T,S> mapper){
        return DeletedTrackedData.<S>builder()
                .id(id)
                .collectionName(collectionName)
                .content(mapper.apply(content))
                .build();
    }
}
