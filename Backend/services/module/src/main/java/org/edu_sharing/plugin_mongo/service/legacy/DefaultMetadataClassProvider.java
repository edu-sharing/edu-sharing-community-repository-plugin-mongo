package org.edu_sharing.plugin_mongo.service.legacy;

import org.edu_sharing.plugin_mongo.domain.metadata.Metadata;

public class DefaultMetadataClassProvider implements MetadataClassProvider {

    @Override
    public Class<? extends Metadata> metadataClass(){
        return Metadata.class;
    }
}
