package org.edu_sharing.plugin_mongo.metadata.lom;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Contribute {
    @NotBlank
    String role;
    @NotNull
    List<String> content;
}
