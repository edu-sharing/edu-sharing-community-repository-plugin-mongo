package org.edu_sharing.plugin_mongo.service.legacy;

import org.edu_sharing.plugin_mongo.domain.metadata.Metadata;

public interface MetadataClassProvider {
    Class<? extends Metadata> metadataClass();
}
