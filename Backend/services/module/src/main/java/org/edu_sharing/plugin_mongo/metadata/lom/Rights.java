package org.edu_sharing.plugin_mongo.metadata.lom;

import lombok.*;
import org.edu_sharing.plugin_mongo.metadata.RangedValue;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Rights {
    RangedValue cost;
    RangedValue copyrightAndOtherRestrictions;
    String description;
    List<String> author;
    String version;
    List<String> internal;
    Locale locale;
    Date expirationDate;
    Boolean publicAccess;
    Boolean negotiationPermitted;
    Boolean restrictedAccess;
}
