package org.edu_sharing.plugin_mongo.graphql.domain.metadata.lom;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class Lom {
    General general;
    Lifecycle lifecycle;
    MetaMetadata metaMetadata;
    Technical technical;
    List<Educational> educational;
    Rights rights;
    List<Classification> classification;
    List<Editorial> editorial;
}
