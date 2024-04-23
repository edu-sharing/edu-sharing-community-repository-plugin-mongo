package org.edu_sharing.plugin_mongo.datamodel.mering;

import com.bazaarvoice.jolt.JsonUtils;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class BaseMerger implements Merger {

    protected Map<String, BiFunction<Object, Object, Result>> functionHooks = new HashMap<>();

    @Override
    public Object merge(Object from, Object to) {
        Result diff = mergHelper(from, to);
        return diff.isEmpty() ? to : from;
    }

    @SuppressWarnings("unchecked")
    protected Result mergHelper(Object from, Object to) {
        if (from instanceof Map) {
            if (!(to instanceof Map)) {
                return new Result(from, to);
            }
            mergeMap((Map<String, Object>) from, (Map<String, Object>) to);
            return Result.empty();
        } else if (from instanceof List) {
            if (!(to instanceof List)) {
                return new Result(from, to);
            }
            mergeList((List<Object>) from, (List<Object>) to);
            return Result.empty();
        }
        return this.diffScalar(from, to);
    }

    private void mergeMap(Map<String, Object> from, Map<String, Object> to) {
        String[] fromKeys = from.keySet().toArray(new String[0]);
        for (String key : fromKeys) {
            Result diff = functionHooks.getOrDefault(key, this::mergHelper)
                    .apply(from.get(key), to.get(key));

            if (!diff.isEmpty() && diff.from != null) {
                to.put(key, diff.from);
            }
        }
    }

    private void mergeList(List<Object> from, List<Object> to) {
        int shortlen = Math.min(from.size(), to.size());
        for (int i = 0; i < shortlen; i++) {
            Result diff = mergHelper(from.get(i), to.get(i));
            if (!diff.isEmpty() && diff.from != null) {
                to.set(i, diff.from);
            }
        }

        if (from.size() > to.size()) {
            to.addAll(to.size(), from);
        }
    }

    protected Result diffScalar(Object from, Object to) {
        if (from == null) {
            if (to == null) {
                return Result.empty();            // both null, isEmpty diff
            }
            return new Result(null, to);        // one is null, full diff
        }
        if (to == null) {
            return new Result(from, null);      // one is null, full diff
        }
        if (scalarEquals(from, to)) {
            return Result.empty();                // equivalent, isEmpty diff
        }
        return new Result(from, to);            // non-equivalent, full diff
    }

    /**
     * Allow subclasses to handle things like Long 0 versus Int 0.  They should be the same,
     * but the .equals doesn't handle it.
     */
    protected boolean scalarEquals(Object expected, Object actual) {
        return expected.equals(actual);
    }

    /**
     * Contains the unmatched fields from the Diffy operation.
     * <p>
     * A sucessful/identical match returns isEmpty() == true.
     */
    public static class Result {
        public final Object from;
        public final Object to;

        private final static Result empty = new Result(null, null);

        public static Result empty() {
            return empty;
        }

        public Result(Object from, Object to) {
            this.from = from;
            this.to = to;
        }

        public boolean isEmpty() {
            return (from == null) && (to == null);
        }

        @Override
        public String toString() {
            if (isEmpty()) {
                return "There is no difference!";
            } else {
                return "\nExpected:\n" + JsonUtils.toPrettyJsonString(from) + "\n" +
                        "\nActual\n" + JsonUtils.toPrettyJsonString(to);
            }
        }
    }
}
