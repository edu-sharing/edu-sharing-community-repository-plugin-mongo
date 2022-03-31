package org.edu_sharing.plugin_mongo.repository;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import graphql.relay.Connection;
import graphql.relay.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.edu_sharing.plugin_mongo.metadata.Metadata;
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

    public Metadata getMetadata(String nodeId, String version) {
        Bson filter = Filters.eq(nodeId);
        String collection = WORKSPACE_KEY;
        if (Objects.nonNull(version) && !version.trim().equals("")) {
            filter = Filters.and(filter, Filters.eq("version.version", version));
            collection = VERSIONSPACE_KEY;
        }

        return mongoDatabase.getCollection(collection, Metadata.class).find(filter).first();
    }

    public List<Metadata> getMetadatas(Collection<String> set) {
        Bson filter = Filters.in("_id", set);
        return mongoDatabase.getCollection(WORKSPACE_KEY, Metadata.class).find(filter).into(new ArrayList<>());
    }

    public List<Metadata> getMetadatas(int first, String cursor) {
        FindIterable<Metadata> result;
        if (Objects.nonNull(cursor)) {
            result = mongoDatabase.getCollection(WORKSPACE_KEY, Metadata.class).find(Filters.gt("_id", cursor));
        } else {
            result = mongoDatabase.getCollection(WORKSPACE_KEY, Metadata.class).find();
        }
        return result.limit(first).into(new ArrayList<>());
    }
}
