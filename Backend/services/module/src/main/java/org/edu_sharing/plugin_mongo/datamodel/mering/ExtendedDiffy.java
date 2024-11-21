package org.edu_sharing.plugin_mongo.datamodel.mering;

import com.bazaarvoice.jolt.Diffy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ExtendedDiffy extends Diffy {

    protected Map<String, BiFunction<Object, Object, Result>> functionHooks = new HashMap<>();

    @Override
    protected Result diffMap(Map<String, Object> expected, Map<String, Object> actual) {

        // Make a copy of the expected keySet so that we can remove things w/out concurrent mod exceptions
        String[] expectedKeys = expected.keySet().toArray(new String[0]);
        for (String key : expectedKeys) {
            BiFunction<Object, Object, Result> functionHook = functionHooks.get(key);
            Result subResult;
            if (functionHook != null) {
                subResult = functionHook.apply(expected.get(key), actual.get(key));
            } else {
                subResult = diffHelper(expected.get(key), actual.get(key));
            }

            if (subResult.isEmpty()) {
                expected.remove(key);
                actual.remove(key);
            }
        }
        if (expected.isEmpty() && actual.isEmpty()) {
            return new Result();
        }
        return new Result(expected, actual);
    }
}
