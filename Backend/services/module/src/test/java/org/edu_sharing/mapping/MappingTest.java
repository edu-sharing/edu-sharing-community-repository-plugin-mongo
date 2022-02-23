package org.edu_sharing.mapping;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.Diffy;
import com.bazaarvoice.jolt.JsonUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.http.util.Asserts;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

class MappingTest {

    public static Stream<Arguments> getTestCaseUnits() {
        return Stream.of(
                Arguments.of("alf2LomFallbackMappingTest", null, null),
                Arguments.of("alf2LomMappingTest", null, null),
                Arguments.of("lom2AlfMappingTest", null, null),
                Arguments.of("alf2VersionMappingTest", null, null),
                Arguments.of("version2AlfMappingTest", null, null),
                Arguments.of("alf2InfoMappingTest", null, null),
                Arguments.of("info2AlfMappingTest", null, null),
                Arguments.of("alf2DirectoryMappingTest", null, null),
                Arguments.of("directory2AlfMappingTest", null, null),
                Arguments.of("alf2CollectionMappingTest", null, null),
                Arguments.of("collection2AlfMappingTest", null, null),
                Arguments.of("alf2StoreMappingTest", null, null),
                Arguments.of("store2AlfMappingTest", null, null),
                Arguments.of("alf2AffiliationMappingTest", null, null),
                Arguments.of("affiliation2AlfMappingTest", null, null),
                Arguments.of("alf2Remote_ShadowMappingTest", null, null),
                Arguments.of("remote_Shadow2AlfMappingTest", null, null),
                Arguments.of("alf2Remote_ReplicationMappingTest", null, null),
                Arguments.of("remote_Replication2AlfMappingTest", null, null),
                Arguments.of("alf2PermissionMappingTest", null, null),
                Arguments.of("permission2AlfMappingTest", null, null),
                Arguments.of("alf2PublishedMappingTest", null, null),
                Arguments.of("published2AlfMappingTest", null, null),
                Arguments.of("alf2SavedSearchMappingTest", null, null),
                Arguments.of("savedSearch2AlfMappingTest", null, null),
                Arguments.of("alf2ShareMappingTest", null, null),
                Arguments.of("share2AlfMappingTest", null, null),
                Arguments.of("alf2WorkflowMappingTest", null, null),
                Arguments.of("workflow2AlfMappingTest", null, null),
                Arguments.of("alf2ImportedObjectMappingTest", null, null),
                Arguments.of("importedObject2AlfMappingTest", null, null),

                Arguments.of("alf2ReferenceMappingTest",
                        (Consumer<Map>) o -> o.put("{http://www.campuscontent.de/model/1.0}collection_proposal_target", new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "29a6417a-c2d5-42bd-846f-0c4274832464")),
                        null
                ),

                Arguments.of("reference2AlfMappingTest",
                        null,
                        (Consumer<Map>) o -> o.put("{http://www.campuscontent.de/model/1.0}collection_proposal_target", new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "29a6417a-c2d5-42bd-846f-0c4274832464"))
                ),

                Arguments.of("alf2AssociationMappingTest",
                        (Consumer<Map>) o -> {
                            o.put("{http://www.campuscontent.de/model/1.0}map_ref_target", new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "2a8a811d-45dc-4d48-af03-a4d40f669e97"));
                            o.put("{http://www.campuscontent.de/model/1.0}forked_origin", new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "100e16d2-3109-4767-b31f-2c4384d05e31"));
                            o.put("{http://www.campuscontent.de/model/1.0}published_original", new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "108bb0ff-9d75-470d-ad49-64e4108a6b4f"));
                        },
                        null
                ),

                Arguments.of("association2AlfMappingTest",
                        null,
                        (Consumer<Map>) o -> {
                            o.put("{http://www.campuscontent.de/model/1.0}map_ref_target", new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "2a8a811d-45dc-4d48-af03-a4d40f669e97"));
                            o.put("{http://www.campuscontent.de/model/1.0}forked_origin", new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "100e16d2-3109-4767-b31f-2c4384d05e31"));
                            o.put("{http://www.campuscontent.de/model/1.0}published_original", new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "108bb0ff-9d75-470d-ad49-64e4108a6b4f"));
                        }
                ),
                Arguments.of("alf2AlfmapMappingTest",
                        (Consumer<Map>) o -> o.put("{http://www.alfresco.org/model/content/1.0}template", new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "5f1aa124-6873-4fef-b776-fef0ef8b0178")),
                        null
                ),
                Arguments.of("alfmap2AlfMappingTest",
                        null,
                        (Consumer<Map>) o -> o.put("{http://www.alfresco.org/model/content/1.0}template", new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "5f1aa124-6873-4fef-b776-fef0ef8b0178"))
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getTestCaseUnits")
    void runtTestUnits(String testCaseName, Consumer<Map> inputHook, Consumer<Map> expectedHook) {

        String basePath = "org/edu_sharing/mapping/";
        String testPath = basePath + testCaseName + ".json";

        ClassLoader classLoader = getClass().getClassLoader();


        InputStream testStream = classLoader.getResourceAsStream(testPath);
        Asserts.notNull(testStream, "test file");

        Map<String, Object> testUnit = JsonUtils.jsonToMap(testStream);

        String model = (String) testUnit.get("mapping");
        Map input = (Map) testUnit.get("input");
        Map expected = (Map) testUnit.get("expected");


        Asserts.notNull(model, "model");
        Asserts.notNull(input, "input");
        Asserts.notNull(expected, "expected");

        if (inputHook != null) {
            inputHook.accept(input);
        }

        if (expectedHook != null) {
            expectedHook.accept(expected);
        }

        InputStream modelStream = classLoader.getResourceAsStream(model);
        Asserts.notNull(modelStream, "mapping model");

        Chainr chainr = Chainr.fromSpec(JsonUtils.jsonToList(modelStream));
        Object actual = chainr.transform(input);

        runDiffy("failed case " + testPath, expected, actual);
    }

    @Test
    void reference2AlfMappingTest() {

    }


    private static final Diffy diffy = new Diffy();

    void runDiffy(String failureMessage, Object expected, Object actual) {
        Diffy.Result result = diffy.diff(expected, actual);
        if (!result.isEmpty()) {
            Assert.fail(failureMessage + ".\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
        }
    }
}
