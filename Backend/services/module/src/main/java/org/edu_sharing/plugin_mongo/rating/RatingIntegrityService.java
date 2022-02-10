package org.edu_sharing.plugin_mongo.rating;

public interface RatingIntegrityService {
    String getAffiliation();

    String getAuthority();

    void checkPermissions(String nodeId) throws Exception;
}
