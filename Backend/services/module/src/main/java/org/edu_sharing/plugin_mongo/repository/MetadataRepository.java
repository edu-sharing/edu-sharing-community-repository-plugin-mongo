package org.edu_sharing.plugin_mongo.repository;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.edu_sharing.plugin_mongo.metadata.Metadata;
import org.edu_sharing.plugin_mongo.metadata.Storage;
import org.edu_sharing.plugin_mongo.metadata.lom.Lom;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.permission.annotation.NodePermission;
import org.edu_sharing.service.permission.annotation.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@NoArgsConstructor // Required for proxying by CGLib (CGLib is used because we don't use an interface here...)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MetadataRepository {
    private final String WORKSPACE_KEY = "workspace";
    private final String VERSIONSPACE_KEY = "version";

    @NonNull MongoDatabase mongoDatabase; // can't use final because of proxying by CGLib
    @NonNull CodecRegistry codecRegistry; // can't use final because of proxying by CGLib

    @Permission(requiresUser = true)
    public Metadata getMetadata(@NodePermission({CCConstants.PERMISSION_READ}) String nodeId, String version) {
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

    @Permission(requiresUser = true)
    public List<Metadata> getMetadatas(@NodePermission({CCConstants.PERMISSION_READ}) Collection<String> ids) {
        Bson filter = Filters.in("_id", ids);
        return mongoDatabase.getCollection(WORKSPACE_KEY, Metadata.class)
                .find(filter)
                .map(metadata -> {
                    metadata.setStorage(Storage.WORKSPACE);
                    return metadata;
                })
                .into(new ArrayList<>());
    }

//    public List<Metadata> getMetadatas(int first, String cursor) {
//        FindIterable<Metadata> result;
//        if (Objects.nonNull(cursor)) {
//            result = mongoDatabase.getCollection(WORKSPACE_KEY, Metadata.class).find(Filters.gt("_id", cursor));
//        } else {
//            result = mongoDatabase.getCollection(WORKSPACE_KEY, Metadata.class).find();
//        }
//
//        return result.limit(first)
//                .map(metadata -> {
//                    metadata.setStorage(Storage.WORKSPACE);
//                    return metadata;
//                })
//                .into(new ArrayList<>());
//    }

    @Permission(requiresUser = true)
    public Metadata updateLom(@NodePermission({CCConstants.PERMISSION_WRITE}) String id, Lom lom) {
        return mongoDatabase.getCollection(WORKSPACE_KEY, Metadata.class)
                .findOneAndUpdate(Filters.eq(id), Updates.set("lom", BsonDocumentWrapper.asBsonDocument(lom, codecRegistry)));
    }
}
