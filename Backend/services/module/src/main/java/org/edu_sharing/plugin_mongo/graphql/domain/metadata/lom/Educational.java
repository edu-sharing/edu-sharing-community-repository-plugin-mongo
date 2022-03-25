package org.edu_sharing.plugin_mongo.graphql.domain.metadata.lom;

import org.edu_sharing.plugin_mongo.graphql.domain.metadata.RangedValue;

import java.time.Duration;
import java.util.List;

public class Educational {
    List<RangedValue> context;
    List<RangedValue> intendedEndUserRoles;
    List<RangedValue> learningResourceTypes;
    List<RangedValue> curriculum;
    Duration typicalLerningTime;
    List<RangedValue> typicalAgeRange;
    List<RangedValue> interactivityType;
    IntRange typicalAgeRangeNominal;
    List<String> language;

}
