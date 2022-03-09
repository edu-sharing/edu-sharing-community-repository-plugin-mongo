package org.edu_sharing.plugin_mongo.relation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RelationTypeUtilTest {

    @ParameterizedTest
    @EnumSource(RelationType.class)
    void invertRelationType_KnownInversionsTest(RelationType type) {
        // This test will fail if we've added a new RelationType without assigning an inverse relation
        Assertions.assertNotNull(RelationTypeUtil.invert(type));
    }
}