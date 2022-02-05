package org.edu_sharing.mapping;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.Diffy;
import com.bazaarvoice.jolt.JsonUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class MappingTest {

    public static List<String> getTestCaseUnits() {
        return Arrays.asList("lom2AlfMappingTest");
    }

    @ParameterizedTest
    @MethodSource("getTestCaseUnits")
    void runtTestUnits(String testCaseName) {

        String basePath = "org/edu_sharing/mapping/";
        String testPath = basePath + testCaseName + ".json";

        ClassLoader classLoader = getClass().getClassLoader();

        Map<String, Object> testUnit = JsonUtils.jsonToMap(classLoader.getResourceAsStream(testPath));

        Object input = testUnit.get("input");
        String model = (String)testUnit.get("model");
        Object expected = testUnit.get("expected");


        Chainr chainr = Chainr.fromSpec(JsonUtils.jsonToList(classLoader.getResourceAsStream(model)));
        Object actual = chainr.transform(input);

        runDiffy("failed case " + testPath, expected, actual);

        InputStream stream2 = classLoader.getResourceAsStream("org/edu_sharing/mapping/alf2lom.json");
    }

    private static final Diffy diffy = new Diffy();

    void runDiffy(String failureMessage, Object expected, Object actual) {
        Diffy.Result result = diffy.diff(expected, actual);
        if(!result.isEmpty()) {
            Assert.fail(failureMessage + ".\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\nactual: " + JsonUtils.toJsonString(result.actual));
        }
    }
}
