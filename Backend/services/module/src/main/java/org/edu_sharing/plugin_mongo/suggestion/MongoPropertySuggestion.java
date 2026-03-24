package org.edu_sharing.plugin_mongo.suggestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.service.suggestion.PropertySuggestion;
import org.edu_sharing.service.suggestion.SuggestionStatus;
import org.edu_sharing.service.suggestion.SuggestionType;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "suggestion")
@CompoundIndexes({
        @CompoundIndex(name = "nodeId_1_createdBy_1_version_1_propertyId_1_value_1", def = "{'nodeId': 1, 'createdBy': 1, 'version': 1, 'propertyId': 1, 'value': 1}", unique = true),
        @CompoundIndex(name = "nodeId_1_status_1_propertyId_1_value_1", def = "{'nodeId': 1, 'status': 1, 'propertyId': 1, 'value': 1}"),
        @CompoundIndex(name = "nodeId_1_status_1__id_1", def = "{'nodeId': 1, 'status': 1, '_id': 1}")
})
public class MongoPropertySuggestion implements PropertySuggestion {
    private String id;
    private String nodeId;
    private String version;

    private String propertyId;
    private Object value;

    private SuggestionType type;
    private SuggestionStatus status;
    private String description;
    private double confidence = 0;

    private Date created;
    private String createdBy;
    private Date modified;
    private String modifiedBy;
}
