package org.edu_sharing.plugin_mongo.joltextension;


import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.modifier.function.Function;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ObjectOutputStream;


public class CustomFunction {

    public static final class stringfy extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String stringValue = objectMapper.writeValueAsString(o);
                return Optional.of(stringValue);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    public static final class jsonfy extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                //Object value = objectMapper.readTree((String) o);
                Object value = JsonUtils.jsonToObject((String) o);
                return Optional.of(value);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    public static class idFromNodeRef extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }

            try {
                return Optional.of(((NodeRef) o).getId());
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    public static class nodeRefFromId extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, (String) o));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }
}
