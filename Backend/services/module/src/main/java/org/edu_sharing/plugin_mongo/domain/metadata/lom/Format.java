package org.edu_sharing.plugin_mongo.domain.metadata.lom;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Format {
    String mimetype;
    String type;
    List<String> subtype;
    String version;
    String content;
}
