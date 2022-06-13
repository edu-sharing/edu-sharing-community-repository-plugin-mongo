package org.edu_sharing.plugin_mongo.repository;


import java.util.Set;

/**
 * This interface is be used by the {@link org.edu_sharing.plugin_mongo.jobs.quarz.ObsoleteMongoEntiesDeletionJob}
 * It is called when a node was finally deleted from alfresco and no reference exists anymore */
public interface AwareAlfrescoDeletion {

  /**
   * This interface is be used by the {@link org.edu_sharing.plugin_mongo.jobs.quarz.ObsoleteMongoEntiesDeletionJob} *
   * It is called when anode was finally deleted from alfresco and no reference exists anymore
   *
   * @param nodeIds The nodeRef of the original Node
   */
  void OnDeletedInAlfresco(Set<String> nodeIds);
}
