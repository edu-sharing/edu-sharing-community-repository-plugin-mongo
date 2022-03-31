package org.edu_sharing.plugin_mongo.metadata;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Workflow {
    String instructions;
    List<String> protocol;
    List<String> receiver;
    String status;
}
