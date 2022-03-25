package org.edu_sharing.plugin_mongo.graphql.domain.metadata.lom;

import lombok.Builder;
import lombok.Value;
import org.edu_sharing.plugin_mongo.graphql.domain.metadata.RangedValue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Builder
@Value
public class Rights {
    RangedValue cost;
    RangedValue copyrightAndOtherRestrictions;
    List<String> description;
    List<String> author;
    String version;
    List<String> internal;
    Locale locale;
    OffsetDateTime expirationDate;
    Boolean publicAccess;
    Boolean negotiationPermitted;
    Boolean restrictedAccess;
}
