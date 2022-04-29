package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Date;

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
