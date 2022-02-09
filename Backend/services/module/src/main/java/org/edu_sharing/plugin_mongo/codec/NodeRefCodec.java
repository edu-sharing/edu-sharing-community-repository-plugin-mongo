package org.edu_sharing.plugin_mongo.codec;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class NodeRefCodec implements Codec<NodeRef> {
    @Override
    public NodeRef decode(BsonReader bsonReader, DecoderContext decoderContext) {
        return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, bsonReader.readString());
    }

    @Override
    public void encode(BsonWriter bsonWriter, NodeRef nodeRef, EncoderContext encoderContext) {
        bsonWriter.writeString(nodeRef.getId());
    }

    @Override
    public Class<NodeRef> getEncoderClass() {
        return NodeRef.class;
    }
}

