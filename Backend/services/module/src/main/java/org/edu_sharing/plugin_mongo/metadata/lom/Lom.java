package org.edu_sharing.plugin_mongo.metadata.lom;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
