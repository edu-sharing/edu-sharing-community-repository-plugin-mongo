package org.edu_sharing.plugin_mongo.domain.metadata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Info {
    @NotBlank
    String filename;
    @NotBlank
    String modifier;
    @NotNull
    Date modifiedDate;
    @NotBlank
    String creator;
    @NotNull
    Date createDate;
    String url;
    String owner;
    Boolean propagateMetadataSet;
    String metadataSet;
    String urlOrigin;
    RangedValue objectType;
    Date originallyCreated;
    List<String> searchContext;
    Preview preview;
    String organisation;
}
