package org.edu_sharing.plugin_mongo.mongo.util;

import com.mongodb.client.model.Updates;
import lombok.NonNull;
import org.apache.http.util.Asserts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateUtil {


    /**
     * Create a partial update operation on the given document and its nested objects.
     * Fields with null values will be removed from the document.
     * Containing lists will be updated partially too, see {@link UpdateUtil#update(String, List)}
     *
     * @param data the partial data to be updated.
     * @return return the operations in a Bson format
     */
    public static Bson update(@NotNull @NonNull Map<String, Object> data) {
        return update(null, data);
    }

    /**
     * Create a partial update operation on the given document and its nested objects.
     * Fields with null values will be removed from the document.
     * Containing lists will be updated partially too, see {@link UpdateUtil#update(String, List)}
     *
     * @param prefix the path to the array field in dot notation
     * @param data   the partial data to be updated.
     * @return return the operations in a Bson format
     */
    public static Bson update(@Nullable String prefix, @NotNull @NonNull Map<String, Object> data) {
        List<Bson> updateSet = new ArrayList<>();
        data.forEach((key, value) -> {
            String newPrefix = key;
            if (prefix != null) {
                newPrefix = String.join(".", prefix, newPrefix);
            }

            if (value instanceof Document) {
                updateSet.add(update(newPrefix, (Document) value));
            } else if (value instanceof List) {
                updateSet.add(update(newPrefix, (List<Object>) value));
            } else {
                if (value != null) {
                    updateSet.add(Updates.set(newPrefix, value));
                } else {
                    updateSet.add(Updates.unset(newPrefix));
                }
            }
        });
        return Updates.combine(updateSet);
    }


    /**
     * Creates a partial update operation on the given array element and its nested objects. Not that this operation is indexed based.
     * Null elements in an array will be skipped by this operation. Wherefore setting an array element to null is not yet supported.
     *
     * @param prefix the path to the array field in dot notation
     * @param data   the list elements to update at the specific index. Null objects will be skipped.
     * @return return the operations in a Bson format
     */
    public static Bson update(@NotNull @NonNull String prefix, @NotNull @NonNull List<Object> data) {

        List<Bson> updateSet = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            Object value = data.get(i);
            if (value == null) {
                continue;
            }

            String newPrefix = String.join(".", prefix, String.valueOf(i));
            if (value instanceof Document) {
                updateSet.add(update(newPrefix, (Document) value));
            } else if (value instanceof List) {
                updateSet.add(update(newPrefix, (List<Object>) value));
            } else {
                updateSet.add(Updates.set(newPrefix, value));
            }
        }

        return Updates.combine(updateSet);
    }

}
