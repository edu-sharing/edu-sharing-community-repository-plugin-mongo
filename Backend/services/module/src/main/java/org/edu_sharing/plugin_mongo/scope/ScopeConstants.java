package org.edu_sharing.plugin_mongo.scope;

public final class ScopeConstants {
    public static final String COLLECTION_ASSIGNMENT = "scope_assignment";
    public static final String COLLECTION_HISTORY = "scope_assignment_history";
    public static final String COLLECTION_CHANGE = "scope_change";
    public static final String COLLECTION_COUNTER = "scope_counter";
    public static final String COLLECTION_BULK_JOB = "scope_bulk_job";

    /** counter id of the change feed sequence */
    public static final String COUNTER_CHANGE = "scope_change";
    /** ttl of change feed entries [assumption, see contract] */
    public static final String CHANGE_TTL = "30d";

    private ScopeConstants() {
    }
}
