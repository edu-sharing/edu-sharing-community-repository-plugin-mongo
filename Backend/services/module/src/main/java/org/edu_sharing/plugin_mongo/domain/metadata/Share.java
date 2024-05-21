package org.edu_sharing.plugin_mongo.domain.metadata;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Share {
    int downloadCount;
    long date;
    String mail;
    @NotBlank
    String token;
}
