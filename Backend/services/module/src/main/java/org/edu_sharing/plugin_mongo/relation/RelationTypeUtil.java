package org.edu_sharing.plugin_mongo.relation;

import java.util.HashMap;
import java.util.Map;

public final class RelationTypeUtil {
    private final static Map<RelationType, RelationType> invertRelationTypeSet = new HashMap<RelationType, RelationType>(){{
        put(RelationType.isBaseFor, RelationType.isBasedOn);
        put(RelationType.hasPart, RelationType.isPartOf);
        put(RelationType.references, RelationType.references);
        put(RelationType.isBasedOn, RelationType.isBaseFor);
        put(RelationType.isPartOf, RelationType.hasPart);
    }};

    public  static RelationType invert(RelationType type){
        return  invertRelationTypeSet.get(type);
    }
}
