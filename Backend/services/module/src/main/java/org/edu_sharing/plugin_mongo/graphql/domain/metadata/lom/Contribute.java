package org.edu_sharing.plugin_mongo.graphql.domain.metadata.lom;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class Contribute {
    @NotBlank
    String role;
    @NotNull
    List<String> content;
}
