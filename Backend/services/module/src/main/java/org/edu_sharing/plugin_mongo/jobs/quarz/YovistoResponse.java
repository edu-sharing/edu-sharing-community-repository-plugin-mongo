package org.edu_sharing.plugin_mongo.jobs.quarz;


import lombok.Builder;
import lombok.Data;

import java.net.URI;

@Data
@Builder
public class YovistoResponse {
    public String name;
    public double weight;
    public URI uri;
    public String match;
    public String label;
}
