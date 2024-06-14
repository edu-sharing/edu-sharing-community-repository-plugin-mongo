package org.edu_sharing.plugin_mongo.domain.metadata.lom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
