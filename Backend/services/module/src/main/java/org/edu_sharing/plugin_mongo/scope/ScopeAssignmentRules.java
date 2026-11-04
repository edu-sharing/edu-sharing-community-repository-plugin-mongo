package org.edu_sharing.plugin_mongo.scope;

import org.edu_sharing.service.scope.ScopeDefinition;
import org.edu_sharing.service.scope.ScopeField;
import org.edu_sharing.service.scope.ScopeFieldVisibility;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Pure rules for scope entries: validation, merging, history diff and derived flags (concept §4.2, §4.3).
 * There are no implications between properties: setting one property never changes another one.
 */
public final class ScopeAssignmentRules {

    /**
     * one changed property, written as one history entry
     */
    public record PropertyChange(String property, List<String> from, List<String> to) {
    }

    private ScopeAssignmentRules() {
    }

    /**
     * @param isMultiValued    dictionary lookup: true if the property allows multiple values
     * @param allowedValues    valuespace lookup: the allowed values of the property, null if it has no fixed valuespace
     * @throws IllegalArgumentException if a property is not in the field list, a single value property gets more than
     *                                  one value or a value is not in the valuespace
     */
    public static void validate(ScopeDefinition definition, Map<String, List<String>> properties,
                                Predicate<String> isMultiValued, Function<String, Collection<String>> allowedValues) {
        Set<String> allowed = fieldIds(definition);
        for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
            String property = entry.getKey();
            List<String> values = entry.getValue() == null ? List.of() : entry.getValue();
            if (!allowed.contains(property)) {
                throw new IllegalArgumentException(property + " is not in the field list of scope " + definition.getScopeId());
            }
            if (values.size() > 1 && !isMultiValued.test(property)) {
                throw new IllegalArgumentException(property + " allows only one value");
            }
            Collection<String> valuespace = allowedValues.apply(property);
            if (valuespace != null) {
                for (String value : values) {
                    if (!valuespace.contains(value)) {
                        throw new IllegalArgumentException(value + " is not a valid value for " + property);
                    }
                }
            }
        }
    }

    /**
     * @param append properties whose requested values are added to the current ones (duplicates are skipped), null
     *               for none
     * @param remove values removed from the current ones per property before appending, null for none
     * @return the properties after applying the request. An empty (or null) value list removes the property, an
     * appended empty list keeps it
     */
    public static Map<String, List<String>> merge(Map<String, List<String>> current, Map<String, List<String>> request,
                                                  Set<String> append, Map<String, List<String>> remove) {
        Map<String, List<String>> replaced = new LinkedHashMap<>(request == null ? Map.of() : request);
        Map<String, List<String>> result = new LinkedHashMap<>(current == null ? Map.of() : current);
        if (remove != null) {
            remove.forEach((property, values) -> {
                List<String> kept = result.getOrDefault(property, List.of()).stream()
                        .filter(value -> values == null || !values.contains(value))
                        .toList();
                if (kept.isEmpty()) {
                    result.remove(property);
                } else {
                    result.put(property, kept);
                }
            });
        }
        if (append != null) {
            for (String property : append) {
                List<String> values = replaced.remove(property);
                if (values == null || values.isEmpty()) {
                    continue;
                }
                LinkedHashSet<String> joined = new LinkedHashSet<>(result.getOrDefault(property, List.of()));
                joined.addAll(values);
                result.put(property, List.copyOf(joined));
            }
        }
        return merge(result, replaced);
    }

    /**
     * @return the properties after applying the request. An empty (or null) value list removes the property
     */
    public static Map<String, List<String>> merge(Map<String, List<String>> current, Map<String, List<String>> request) {
        Map<String, List<String>> result = new LinkedHashMap<>(current == null ? Map.of() : current);
        request.forEach((property, values) -> {
            if (values == null || values.isEmpty()) {
                result.remove(property);
            } else {
                result.put(property, List.copyOf(values));
            }
        });
        return result;
    }

    /**
     * @return one change per property whose values differ, ordered by property name
     */
    public static List<PropertyChange> diff(Map<String, List<String>> before, Map<String, List<String>> after) {
        Map<String, List<String>> from = before == null ? Map.of() : before;
        Map<String, List<String>> to = after == null ? Map.of() : after;
        SortedSet<String> properties = new TreeSet<>(from.keySet());
        properties.addAll(to.keySet());
        List<PropertyChange> changes = new ArrayList<>();
        for (String property : properties) {
            List<String> oldValues = from.getOrDefault(property, List.of());
            List<String> newValues = to.getOrDefault(property, List.of());
            if (!oldValues.equals(newValues)) {
                changes.add(new PropertyChange(property, oldValues, newValues));
            }
        }
        return changes;
    }

    /**
     * published: any property has one of the publishedValues of its field
     */
    public static boolean isPublished(ScopeDefinition definition, Map<String, List<String>> properties) {
        return matches(definition, properties, ScopeField::getPublishedValues);
    }

    /**
     * excluded: any property has one of the excludedValues of its field
     */
    public static boolean isExcluded(ScopeDefinition definition, Map<String, List<String>> properties) {
        return matches(definition, properties, ScopeField::getExcludedValues);
    }

    private static boolean matches(ScopeDefinition definition, Map<String, List<String>> properties, Function<ScopeField, List<String>> values) {
        for (ScopeField field : fields(definition)) {
            List<String> markers = values.apply(field);
            List<String> set = properties.get(field.getId());
            if (markers != null && set != null && set.stream().anyMatch(markers::contains)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return "&lt;scopeId&gt;|&lt;property&gt;" for every set property with replaces = true
     */
    public static List<String> overrides(ScopeDefinition definition, Map<String, List<String>> properties) {
        List<String> result = new ArrayList<>();
        for (ScopeField field : fields(definition)) {
            List<String> set = properties.get(field.getId());
            if (field.isReplaces() && set != null && !set.isEmpty()) {
                result.add(definition.getScopeId() + "|" + field.getId());
            }
        }
        return result;
    }

    /**
     * @return the properties of an entry created by an upload into the shared folder (uploadValues of the field list)
     */
    public static Map<String, List<String>> uploadProperties(ScopeDefinition definition) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (ScopeField field : fields(definition)) {
            if (field.getUploadValues() != null && !field.getUploadValues().isEmpty()) {
                result.put(field.getId(), List.copyOf(field.getUploadValues()));
            }
        }
        return result;
    }

    /**
     * @return the properties that may be searched and shown outside of the own scope: public and not replacing
     */
    public static List<String> publicFields(ScopeDefinition definition) {
        List<String> result = new ArrayList<>();
        for (ScopeField field : fields(definition)) {
            if (field.getVisibility() == ScopeFieldVisibility.PUBLIC && !field.isReplaces()) {
                result.add(field.getId());
            }
        }
        return result;
    }

    private static List<ScopeField> fields(ScopeDefinition definition) {
        return definition.getFields() == null ? List.of() : definition.getFields();
    }

    private static Set<String> fieldIds(ScopeDefinition definition) {
        Set<String> ids = new HashSet<>();
        fields(definition).forEach(f -> ids.add(f.getId()));
        return ids;
    }
}
