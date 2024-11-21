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
public class Permission {
    @NotBlank
    String action;
    List<String> history;
    List<String> invited;
    @NotNull
    Date modified;
    List<String> users;
}
