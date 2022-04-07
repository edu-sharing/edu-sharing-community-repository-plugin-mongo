package org.edu_sharing.plugin_mongo.repository;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import graphql.relay.Connection;
import graphql.relay.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.edu_sharing.plugin_mongo.metadata.Metadata;
import org.edu_sharing.plugin_mongo.metadata.Storage;
import org.edu_sharing.plugin_mongo.metadata.lom.General;
import org.edu_sharing.plugin_mongo.metadata.lom.Lom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MetadataRepository {

    private final String WORKSPACE_KEY = "workspace";
    private final String VERSIONSPACE_KEY = "version";

    final MongoDatabase mongoDatabase;
    final CodecRegistry codecRegistry;

    public Metadata getMetadata(String nodeId, String version) {
        Bson filter = Filters.eq(nodeId);
        String collection = WORKSPACE_KEY;
        final Storage storage;
        if (Objects.nonNull(version) && !version.trim().equals("")) {
            filter = Filters.and(filter, Filters.eq("version.version", version));
            collection = VERSIONSPACE_KEY;
            storage = Storage.VERSION_STORE;
        } else {
            storage = Storage.WORKSPACE;
        }

        return mongoDatabase.getCollection(collection, Metadata.class).find(filter)
                .map(metadata -> {
                    metadata.setStorage(storage);
                    return metadata;
                })
                .first();
    }

    public List<Metadata> getMetadatas(Collection<String> ids) {
        Bson filter = Filters.in("_id", ids);
        return mongoDatabase.getCollection(WORKSPACE_KEY, Metadata.class)
                .find(filter)
                .map(metadata -> {
                    metadata.setStorage(Storage.WORKSPACE);
                    return metadata;
                })
                .into(new ArrayList<>());
    }

    public List<Metadata> getMetadatas(int first, String cursor) {
        FindIterable<Metadata> result;
        if (Objects.nonNull(cursor)) {
            result = mongoDatabase.getCollection(WORKSPACE_KEY, Metadata.class).find(Filters.gt("_id", cursor));
        } else {
            result = mongoDatabase.getCollection(WORKSPACE_KEY, Metadata.class).find();
        }

        return result.limit(first)
                .map(metadata -> {
                    metadata.setStorage(Storage.WORKSPACE);
                    return metadata;
                })
                .into(new ArrayList<>());
    }

    public void updateLom(String id, Lom lom) {
        UpdateResult result = mongoDatabase.getCollection(WORKSPACE_KEY).updateOne(Filters.eq(id), Updates.set("lom", BsonDocumentWrapper.asBsonDocument(lom, codecRegistry)));
    }
}
