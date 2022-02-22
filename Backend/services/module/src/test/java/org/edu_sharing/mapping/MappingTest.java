package org.edu_sharing.mapping;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.Diffy;
import com.bazaarvoice.jolt.JsonUtils;
import org.junit.Assert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

class MappingTest {

    public static List<String> getTestCaseUnits() {
        return Arrays.asList(
                "alf2LomFallbackMappingTest",
                "alf2LomMappingTest",
                "lom2AlfMappingTest",
                "alf2VersionMappingTest",
                "version2AlfMappingTest",
                "alf2InfoMappingTest",
                "info2AlfMappingTest",
                "alf2DirectoryMappingTest",
                "directory2AlfMappingTest",
                "alf2CollectionMappingTest",
                "collection2AlfMappingTest",
                "alf2StoreMappingTest",
                "store2AlfMappingTest",
                "alf2AffiliationMappingTest",
                "affiliation2AlfMappingTest",
                "alf2Remote_ShadowMappingTest",
                "remote_Shadow2AlfMappingTest",
                "alf2Remote_ReplicationMappingTest",
                "remote_Replication2AlfMappingTest",
                "alfmapMappingTest"
        );
    }

    @ParameterizedTest
    @MethodSource("getTestCaseUnits")
    void runtTestUnits(String testCaseName) {

        String basePath = "org/edu_sharing/mapping/";
        String testPath = basePath + testCaseName + ".json";

        ClassLoader classLoader = getClass().getClassLoader();

        Map<String, Object> testUnit = JsonUtils.jsonToMap(classLoader.getResourceAsStream(testPath));

        String model = (String) testUnit.get("mapping");
        Object input = testUnit.get("input");
        Object expected = testUnit.get("expected");

        Chainr chainr = Chainr.fromSpec(JsonUtils.jsonToList(classLoader.getResourceAsStream(model)));
        Object actual = chainr.transform(input);

        runDiffy("failed case " + testPath, expected, actual);
    }

    private static final Diffy diffy = new Diffy();

    void runDiffy(String failureMessage, Object expected, Object actual) {
        Diffy.Result result = diffy.diff(expected, actual);
        if (!result.isEmpty()) {
            Assert.fail(failureMessage + ".\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
        }
    }
}
