package org.edu_sharing.plugin_mongo.relation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edu_sharing.service.relations.Evaluation;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationData implements Evaluation {
    private boolean approved;
    private Date approvedAt;
    private String approvedBy;
}
