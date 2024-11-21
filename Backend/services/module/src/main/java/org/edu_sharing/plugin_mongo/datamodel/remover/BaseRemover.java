package org.edu_sharing.plugin_mongo.datamodel.remover;

import com.bazaarvoice.jolt.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class BaseRemover implements Remover {

    protected Map<String, BiFunction<Object, Object, Boolean>> functionHooks = new HashMap<>();

    @Override
    public boolean remove(Object from, Object to) {
        return removeHelper(from, to);
    }

    @SuppressWarnings("unchecked")
    protected boolean removeHelper(Object from, Object to) {
        if (from instanceof Map) {
            if (!(to instanceof Map)) {
                return false;
            }
            return removeMap((Map<String, Object>) from, (Map<String, Object>) to);
        } else if (from instanceof List) {
            if (!(to instanceof List)) {
                return false;
            }
            return removeList((List<Object>) from, (List<Object>) to);
        }
        return this.diffScalar(from, to);
    }

    private boolean removeMap(Map<String, Object> from, Map<String, Object> to) {
        String[] fromKeys = from.keySet().toArray(new String[0]);
        for (String key : fromKeys) {
            boolean removed  = functionHooks.getOrDefault(key, this::removeHelper)
                    .apply(from.get(key), to.get(key));

            if (removed) {
                to.remove(key);
            }
        }
        return to.isEmpty();
    }

    private boolean removeList(List<Object> from, List<Object> to) {
        int shortlen = Math.min(from.size(), to.size());

        for (int i = shortlen - 1; i >= 0; i--) {
            if (removeHelper(from.get(i), to.get(i))) {
                to.remove(i);
            }
        }

        return to.isEmpty();
    }

    protected boolean diffScalar(Object from, Object to) {
        return scalarHasNullString(from) && to != null;
    }

    /**
     * Allow subclasses to handle things like Long 0 versus Int 0.  They should be the same,
     * but the .equals doesn't handle it.
     */
    protected boolean scalarHasNullString(Object expected) {
        return Objects.equals(expected, Remover.RemoveToken);
    }
}
