package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
@Value
public class Info {
    @NotBlank
    String filename;
    @NotBlank
    String modifier;
    @NotNull
    OffsetDateTime modifiedDate;
    @NotBlank
    String creator;
    @NotNull
    OffsetDateTime creatorDate;
    String url;
    @NotBlank
    String owner;
    Boolean propagateMetaDataSet;
    String metadataSet;
    String urlOrigin;
    RangedValue objectType;
    OffsetDateTime originallyCreated;
    List<String> searchContext;
    Preview preview;
    String organisation;
}
