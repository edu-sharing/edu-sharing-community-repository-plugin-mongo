package org.edu_sharing.plugin_mongo.scope;

import org.edu_sharing.service.scope.ScopeDefinition;
import org.edu_sharing.service.scope.ScopeField;
import org.edu_sharing.service.scope.ScopeFieldVisibility;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ScopeAssignmentRulesTest {

    static final ScopeDefinition DEFINITION = ScopeDefinition.builder()
            .scopeId("GROUP_ORG_wlo")
            .fields(List.of(
                    ScopeField.builder().id("ccm:editorial_inventory").visibility(ScopeFieldVisibility.INTERNAL).excludedValues(List.of("rejected")).build(),
                    ScopeField.builder().id("ccm:editorial_release").visibility(ScopeFieldVisibility.INTERNAL).publishedValues(List.of("released")).build(),
                    ScopeField.builder().id("ccm:editorial_internal").visibility(ScopeFieldVisibility.INTERNAL).build(),
                    ScopeField.builder().id("cclom:title").visibility(ScopeFieldVisibility.PUBLIC).replaces(true).build()))
            .build();

    private static void validate(Map<String, List<String>> properties) {
        ScopeAssignmentRules.validate(DEFINITION, properties,
                "ccm:editorial_internal"::equals,
                property -> switch (property) {
                    case "ccm:editorial_inventory" -> Set.of("proposed", "accepted", "rejected");
                    case "ccm:editorial_release" -> Set.of("not_released", "released", "withdrawn");
                    default -> null;
                });
    }

    @Test
    void validation() {
        validate(Map.of("ccm:editorial_release", List.of("released"), "ccm:editorial_internal", List.of("a", "b"), "cclom:title", List.of("free text")));
        // removing a property is always allowed
        validate(Map.of("ccm:editorial_release", List.of()));

        assertThrows(IllegalArgumentException.class, () -> validate(Map.of("ccm:curriculum", List.of("x"))), "not in the field list");
        assertThrows(IllegalArgumentException.class, () -> validate(Map.of("ccm:editorial_release", List.of("released", "withdrawn"))), "single value");
        assertThrows(IllegalArgumentException.class, () -> validate(Map.of("ccm:editorial_release", List.of("approved"))), "not in the valuespace");
    }

    @Test
    void mergeKeepsOtherPropertiesAndRemovesEmptyOnes() {
        Map<String, List<String>> current = Map.of(
                "ccm:editorial_inventory", List.of("rejected"),
                "ccm:editorial_internal", List.of("a"));

        Map<String, List<String>> merged = ScopeAssignmentRules.merge(current, Map.of(
                "ccm:editorial_release", List.of("released"),
                "ccm:editorial_internal", List.of()));

        // no implications: rejected stays rejected although the entry is released now
        assertEquals(Map.of("ccm:editorial_inventory", List.of("rejected"), "ccm:editorial_release", List.of("released")), merged);
    }

    @Test
    void appendAddsValuesWithoutDuplicates() {
        Map<String, List<String>> current = Map.of(
                "ccm:editorial_internal", List.of("a", "b"),
                "ccm:editorial_inventory", List.of("rejected"));

        Map<String, List<String>> merged = ScopeAssignmentRules.merge(current, Map.of(
                "ccm:editorial_internal", List.of("b", "c"),
                "ccm:editorial_inventory", List.of("accepted")), Set.of("ccm:editorial_internal"), null);

        assertEquals(List.of("a", "b", "c"), merged.get("ccm:editorial_internal"));
        // not appended: replaced
        assertEquals(List.of("accepted"), merged.get("ccm:editorial_inventory"));
        // an empty appended list keeps the values
        assertEquals(List.of("a", "b"), ScopeAssignmentRules.merge(current, Map.of("ccm:editorial_internal", List.of()),
                Set.of("ccm:editorial_internal"), null).get("ccm:editorial_internal"));
    }

    /**
     * bulk editor over a selection: deselected values are removed, new ones added, other values of the nodes stay
     */
    @Test
    void removeBeforeAppend() {
        Map<String, List<String>> current = Map.of("ccm:editorial_internal", List.of("a", "b", "x"));

        Map<String, List<String>> merged = ScopeAssignmentRules.merge(current, Map.of("ccm:editorial_internal", List.of("c")),
                Set.of("ccm:editorial_internal"), Map.of("ccm:editorial_internal", List.of("b")));
        assertEquals(List.of("a", "x", "c"), merged.get("ccm:editorial_internal"));

        // removing the last value removes the property
        assertFalse(ScopeAssignmentRules.merge(Map.of("ccm:editorial_internal", List.of("a")), Map.of(), null,
                Map.of("ccm:editorial_internal", List.of("a"))).containsKey("ccm:editorial_internal"));
    }

    @Test
    void diffHasOneChangePerProperty() {
        List<ScopeAssignmentRules.PropertyChange> changes = ScopeAssignmentRules.diff(
                Map.of("ccm:editorial_inventory", List.of("proposed"), "ccm:editorial_internal", List.of("a")),
                Map.of("ccm:editorial_inventory", List.of("accepted"), "ccm:editorial_release", List.of("released"), "ccm:editorial_internal", List.of("a")));

        assertEquals(List.of(
                new ScopeAssignmentRules.PropertyChange("ccm:editorial_inventory", List.of("proposed"), List.of("accepted")),
                new ScopeAssignmentRules.PropertyChange("ccm:editorial_release", List.of(), List.of("released"))), changes);
        assertEquals(List.of(), ScopeAssignmentRules.diff(Map.of("a", List.of("1")), Map.of("a", List.of("1"))));
    }

    @Test
    void flagsAreDerivedFromTheFieldList() {
        Map<String, List<String>> rejectedButReleased = Map.of(
                "ccm:editorial_inventory", List.of("rejected"),
                "ccm:editorial_release", List.of("released"));
        assertTrue(ScopeAssignmentRules.isPublished(DEFINITION, rejectedButReleased));
        assertTrue(ScopeAssignmentRules.isExcluded(DEFINITION, rejectedButReleased));

        Map<String, List<String>> withdrawn = Map.of("ccm:editorial_release", List.of("withdrawn"));
        assertFalse(ScopeAssignmentRules.isPublished(DEFINITION, withdrawn));
        assertFalse(ScopeAssignmentRules.isExcluded(DEFINITION, withdrawn));
    }

    @Test
    void overridesOnlyForSetReplacingFields() {
        assertEquals(List.of("GROUP_ORG_wlo|cclom:title"), ScopeAssignmentRules.overrides(DEFINITION,
                Map.of("cclom:title", List.of("Eigener Titel"), "ccm:editorial_release", List.of("released"))));
        assertEquals(List.of(), ScopeAssignmentRules.overrides(DEFINITION, Map.of("ccm:editorial_release", List.of("released"))));
    }

    @Test
    void publicFieldsAreThePublicNonReplacingOnes() {
        ScopeDefinition definition = ScopeDefinition.builder().scopeId("GROUP_ORG_wlo").fields(List.of(
                ScopeField.builder().id("ccm:editorial_recommendation").visibility(ScopeFieldVisibility.PUBLIC).build(),
                ScopeField.builder().id("ccm:editorial_inventory").visibility(ScopeFieldVisibility.INTERNAL).build(),
                ScopeField.builder().id("cclom:title").visibility(ScopeFieldVisibility.PUBLIC).replaces(true).build())).build();

        assertEquals(List.of("ccm:editorial_recommendation"), ScopeAssignmentRules.publicFields(definition));
    }

    @Test
    void uploadPropertiesComeFromTheFieldList() {
        ScopeDefinition definition = ScopeDefinition.builder().scopeId("GROUP_ORG_wlo").fields(List.of(
                ScopeField.builder().id("ccm:editorial_inventory").uploadValues(List.of("accepted")).build(),
                ScopeField.builder().id("ccm:editorial_release").build())).build();

        assertEquals(Map.of("ccm:editorial_inventory", List.of("accepted")), ScopeAssignmentRules.uploadProperties(definition));
        assertEquals(Map.of(), ScopeAssignmentRules.uploadProperties(DEFINITION));
    }
}
