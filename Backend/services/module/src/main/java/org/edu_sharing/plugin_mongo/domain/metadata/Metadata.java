package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.edu_sharing.plugin_mongo.domain.metadata.lom.Lom;

import java.util.List;
import java.util.Map;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Metadata {
    @BsonId
    String id;
    @BsonIgnore
    Storage storage;
    String nodeType;
    List<String> aspects;
    ContentDataWithId content;
    Version version;
    Store store;
    Affiliation affiliation;
    Remote remote;
    Info info;
    Directory directory;
    Collection collection;
    Reference reference;
    Permission permission;
    Published published;
    SavedSearch savedSearch;
    Share share;
    Workflow workflow;
    ImportedObject importedObject;
    Association association;
    Lom lom;
    Oeh oeh;
    Map<String, Object> alfMap;
}

