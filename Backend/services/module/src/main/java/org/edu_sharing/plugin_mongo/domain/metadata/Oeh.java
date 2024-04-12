package org.edu_sharing.plugin_mongo.domain.metadata;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Oeh {
    List<RangedValue> unmetLegalCriteria;
    RangedValue sourceContentType;
    RangedValue toolCategory;
    RangedValue conditionsOfAccess;
    RangedValue containsAdvertisement;
    RangedValue price;
    List<RangedValue> accessibilitySummary;
    List<RangedValue> dataProtectionConformity;
    RangedValue fskRating;
    RangedValue licenseOer;
    String generalIdentifier;
}
