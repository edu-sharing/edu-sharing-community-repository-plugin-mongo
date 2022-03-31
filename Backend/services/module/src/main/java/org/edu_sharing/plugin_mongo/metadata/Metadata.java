package org.edu_sharing.plugin_mongo.metadata;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;
import org.edu_sharing.plugin_mongo.metadata.lom.Lom;

import java.util.List;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Metadata {
    @BsonId
    String id;
    String nodeType;
    List<String> aspects;
    String content;
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
    Map<String, Object> alfMap;

//    public static MetadataBuilder builder(String id){
//        return new MetadataBuilder()._id(id);
//    }
}
