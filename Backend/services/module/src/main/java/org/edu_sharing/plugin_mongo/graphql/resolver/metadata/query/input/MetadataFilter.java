package org.edu_sharing.plugin_mongo.graphql.resolver.metadata.query.input;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;

@Data
public class MetadataFilter {
    List<String> ids;
}
