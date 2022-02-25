package org.edu_sharing.plugin_mongo.joltextension;


import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.modifier.function.Function;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.bson.Document;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Locale;
import java.util.Map;


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

    public static class mapFromLocale extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }

            try {
                Locale locale = (Locale)o;
                Document document = new Document();
                document.put("language", locale.getLanguage());
                document.put("country", locale.getCountry());
                document.put("variant", locale.getVariant());

                return Optional.of(document);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    public static class localeFromMap extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }
            try {
                Document doc = new Document((Map)o);
                return Optional.of(new Locale(
                        doc.getString("language"),
                        doc.getString("country"),
                        doc.getString("variant")));

            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    public static class mapFromContentDataWithId extends Function.SingleFunction<Object> {
        private static mapFromLocale mapFromLocale = new CustomFunction.mapFromLocale();

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }

            try {
                ContentDataWithId contentDataWithId = (ContentDataWithId)o;
                Document document = new Document();
                document.put("contentUrl", contentDataWithId.getContentUrl());
                document.put("mimetype", contentDataWithId.getMimetype());
                document.put("size", contentDataWithId.getSize());
                document.put("encoding", contentDataWithId.getEncoding());
                document.put("locale", mapFromLocale.applySingle(contentDataWithId.getLocale()).get());
                document.put("id", contentDataWithId.getId());

                return Optional.of(document);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    public static class contentDataWithIdFromMap extends Function.SingleFunction<Object> {
        private static localeFromMap localeFromMap = new CustomFunction.localeFromMap();

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }
            try {
                Document doc = new Document((Map)o);

                return Optional.of(new ContentDataWithId(
                    new ContentData(
                            doc.getString("contentUrl"),
                            doc.getString("mimetype"),
                            doc.getLong("size"),
                            doc.getString("encoding"),
                            (Locale)localeFromMap.applySingle(doc.get("locale")).get()),
                        doc.getLong("id")));

            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

}
