package org.edu_sharing.plugin_mongo.metadata;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
