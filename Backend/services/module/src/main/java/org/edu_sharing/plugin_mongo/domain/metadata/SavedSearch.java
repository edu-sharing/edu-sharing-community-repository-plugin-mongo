package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SavedSearch {
    String mds;
    List<String> parameters;
    String query;
    String repository;
}
