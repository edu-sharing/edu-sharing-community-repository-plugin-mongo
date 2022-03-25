package org.edu_sharing.plugin_mongo.graphql.domain.metadata;

import lombok.Builder;
import lombok.Value;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.lom.Lom;


@Builder
@Value
public class Metadata {
    String _id;
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
}
