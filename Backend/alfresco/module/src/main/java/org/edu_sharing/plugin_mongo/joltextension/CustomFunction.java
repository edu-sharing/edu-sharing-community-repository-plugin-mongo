package org.edu_sharing.plugin_mongo.joltextension;


import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.modifier.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.bson.Document;
import org.codehaus.jackson.map.ObjectMapper;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;


public class CustomFunction {

    @Slf4j
    public static final class stringfy extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String stringValue = objectMapper.writeValueAsString(o);
                return Optional.of(stringValue);
            } catch (Exception e) {
                log.error(e.getMessage());
                return Optional.empty();
            }
        }
    }

    @Slf4j
    public static final class jsonfy extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                //Object value = objectMapper.readTree((String) o);
                Object value = JsonUtils.jsonToObject((String) o);
                return Optional.of(value);
            } catch (Exception e) {
                log.error(e.getMessage());
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

    @Slf4j
    public static class nodeRefFromId extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, (String) o));
            } catch (Exception e) {
                log.error(e.getMessage());
                return Optional.empty();
            }
        }
    }

    @Slf4j
    public static class mapFromLocale extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }

            try {
                Locale locale = (Locale) o;
                Document document = new Document();
                document.put("language", locale.getLanguage());
                document.put("country", locale.getCountry());
                document.put("variant", locale.getVariant());

                return Optional.of(document);
            } catch (Exception e) {
                log.error(e.getMessage());
                return Optional.empty();
            }
        }
    }

    @Slf4j
    public static class localeFromMap extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }
            try {
                Document doc = new Document((Map) o);
                return Optional.of(new Locale(
                        doc.getString("language"),
                        doc.getString("country"),
                        doc.getString("variant")));

            } catch (Exception e) {
                log.error(e.getMessage());
                return Optional.empty();
            }
        }
    }

    @Slf4j
    public static class mapFromContentDataWithId extends Function.SingleFunction<Object> {
        private static mapFromLocale mapFromLocale = new mapFromLocale();

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }

            try {
                ContentDataWithId contentDataWithId = (ContentDataWithId) o;
                Document document = new Document();
                document.put("contentUrl", contentDataWithId.getContentUrl());
                document.put("mimetype", contentDataWithId.getMimetype());
                document.put("size", contentDataWithId.getSize());
                document.put("encoding", contentDataWithId.getEncoding());
                document.put("locale", mapFromLocale.applySingle(contentDataWithId.getLocale()).get());
                document.put("id", contentDataWithId.getId());

                return Optional.of(document);
            } catch (Exception e) {
                log.error(e.getMessage());
                return Optional.empty();
            }
        }
    }

    @Slf4j
    public static class contentDataWithIdFromMap extends Function.SingleFunction<Object> {
        private static localeFromMap localeFromMap = new localeFromMap();

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }
            try {
                Document doc = new Document((Map) o);

                return Optional.of(new ContentDataWithId(
                        new ContentData(
                                doc.getString("contentUrl"),
                                doc.getString("mimetype"),
                                doc.getLong("size"),
                                doc.getString("encoding"),
                                (Locale) localeFromMap.applySingle(doc.get("locale")).get()),
                        doc.getLong("id")));

            } catch (Exception e) {
                log.error(e.getMessage());
                return Optional.empty();
            }
        }
    }

    @Slf4j
    public static class unifyDurationFormat extends Function.SingleFunction<Object> {

        private final static DateTimeFormatter dateTimeFormatter;

        static {
            dateTimeFormatter = new DateTimeFormatterBuilder()
                    .appendOptional(DateTimeFormatter.ISO_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_DATE)
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                    .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE)
                    .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_TIME)
                    .appendOptional(DateTimeFormatter.ISO_OFFSET_TIME)
                    .appendOptional(DateTimeFormatter.ISO_ORDINAL_DATE)
                    .appendOptional(DateTimeFormatter.ISO_TIME)
                    .appendOptional(DateTimeFormatter.ISO_WEEK_DATE)
                    .appendOptional(DateTimeFormatter.ISO_INSTANT)
                    .appendOptional(DateTimeFormatter.BASIC_ISO_DATE)
                    .appendPattern("[HH:mm:ss][H:m:s]")
                    .toFormatter();
        }

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }
            try {
                try {
                    String durationText = (String) o;
                    durationText = durationText.replaceFirst("(\\d{2}):(\\d{2}):(\\d{2}(?:\\.\\d+)?)", "PT$1H$2M$3S");
                    Duration.parse(durationText);
                    return Optional.of(durationText);
                } catch (Exception ex) {
                    return Optional.of(Duration.between(LocalTime.MIN, LocalTime.parse((String) o, dateTimeFormatter)).toString());
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                return Optional.empty();
            }
        }
    }






    @Slf4j
    public static class longToDate extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }

            try {
                return Optional.of(new Date((Long) o));
            } catch (Exception e) {
                log.error(e.getMessage());
                return Optional.empty();
            }
        }
    }

    @Slf4j
    public static class dateToLong extends Function.SingleFunction<Object> {

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }

            try {
                return Optional.of(((Date) o).getTime());
            } catch (Exception e) {
                log.error(e.getMessage());
                return Optional.empty();
            }
        }
    }


    @Slf4j
    public static class unifyDurationFormat1 extends Function.SingleFunction<Object> {

        private final static DateTimeFormatter dateTimeFormatter;

        static {
            dateTimeFormatter = new DateTimeFormatterBuilder()
                    .appendOptional(DateTimeFormatter.ISO_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_DATE)
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                    .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE)
                    .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_TIME)
                    .appendOptional(DateTimeFormatter.ISO_OFFSET_TIME)
                    .appendOptional(DateTimeFormatter.ISO_ORDINAL_DATE)
                    .appendOptional(DateTimeFormatter.ISO_TIME)
                    .appendOptional(DateTimeFormatter.ISO_WEEK_DATE)
                    .appendOptional(DateTimeFormatter.ISO_INSTANT)
                    .appendOptional(DateTimeFormatter.BASIC_ISO_DATE)
                    .appendPattern("[HH:mm:ss][H:m:s]")
                    .toFormatter();
        }

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }
            try {
                try {
                    String durationText = (String) o;
                    durationText = durationText.replaceFirst("(\\d{2}):(\\d{2}):(\\d{2}(?:\\.\\d+)?)", "PT$1H$2M$3S");
                    Duration.parse(durationText);
                    return Optional.of(durationText);
                } catch (Exception ex) {
                    return Optional.of(Duration.between(LocalTime.MIN, LocalTime.parse((String) o, dateTimeFormatter)).toString());
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                return Optional.empty();
            }
        }
    }


    @Slf4j
    public static class unifyDurationFormat2 extends Function.SingleFunction<Object> {

        private final static DateTimeFormatter dateTimeFormatter;

        static {
            dateTimeFormatter = new DateTimeFormatterBuilder()
                    .appendOptional(DateTimeFormatter.ISO_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_DATE)
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                    .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE)
                    .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_TIME)
                    .appendOptional(DateTimeFormatter.ISO_OFFSET_TIME)
                    .appendOptional(DateTimeFormatter.ISO_ORDINAL_DATE)
                    .appendOptional(DateTimeFormatter.ISO_TIME)
                    .appendOptional(DateTimeFormatter.ISO_WEEK_DATE)
                    .appendOptional(DateTimeFormatter.ISO_INSTANT)
                    .appendOptional(DateTimeFormatter.BASIC_ISO_DATE)
                    .appendPattern("[HH:mm:ss][H:m:s]")
                    .toFormatter();
        }

        @Override
        protected Optional<Object> applySingle(Object o) {
            if (o == null) {
                return Optional.empty();
            }
            try {
                try {
                    String durationText = (String) o;
                    durationText = durationText.replaceFirst("(\\d{2}):(\\d{2}):(\\d{2}(?:\\.\\d+)?)", "PT$1H$2M$3S");
                    Duration.parse(durationText);
                    return Optional.of(durationText);
                } catch (Exception ex) {
                    return Optional.of(Duration.between(LocalTime.MIN, LocalTime.parse((String) o, dateTimeFormatter)).toString());
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                return Optional.empty();
            }
        }
    }



}
