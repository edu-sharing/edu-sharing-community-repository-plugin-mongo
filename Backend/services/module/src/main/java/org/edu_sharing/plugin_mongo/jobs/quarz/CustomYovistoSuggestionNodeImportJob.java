package org.edu_sharing.plugin_mongo.jobs.quarz;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.edu_sharing.plugin_mongo.domain.suggestion.Suggestion;
import org.edu_sharing.plugin_mongo.domain.suggestion.lom.ClassificationSuggestion;
import org.edu_sharing.plugin_mongo.domain.suggestion.lom.LomSuggestion;
import org.edu_sharing.plugin_mongo.domain.suggestion.types.RangedValueSuggestionData;
import org.edu_sharing.repository.server.jobs.quartz.annotation.JobDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@JobDescription(description = "Creates suggestions for nodes from Yovisto")
public class CustomYovistoSuggestionNodeImportJob extends AbstractYovistoNodeSuggestionImportJob {

  @NotNull
  @Override
  protected String getId() {
    return "1";
  }

  @Override
  protected void fillSuggestionData(
      Suggestion.SuggestionBuilder suggestionBuilder, String jsonResponse) throws IOException {

    YovistoResponse[] yovistoResponse = getResponse(jsonResponse);

    suggestionBuilder.lom(
        LomSuggestion.builder()
            .classification(
                ClassificationSuggestion.builder()
                    .keyword(
                        Arrays.stream(yovistoResponse)
                            .map(YovistoResponse::getUri)
                            .map(URI::toString)
                            .map(RangedValueSuggestionData::new)
                            .collect(Collectors.toList()))
                    .build())
            .build());
  }

  @NotNull
  @Override
  protected String getApiEndpointUri() {
    return "/topics";
  }

  @Nullable
  @Override
  protected String buildRequestBody(HashMap<String, Object> properties) {
    String message =
        (String) properties.get("{http://www.campuscontent.de/model/lom/1.0}general_description");

    if (StringUtils.isBlank(message)) {
      message = (String) properties.get("{http://www.alfresco.org/model/content/1.0}description");
    }

    if (StringUtils.isBlank(message)) {
      return null;
    }

    message = message.replace("\n", "").replace("\"", "\\\"");

    if (StringUtils.isBlank(message)) {
      return null;
    }

    return message;
  }

  private YovistoResponse[] getResponse(String jsonResponse) throws IOException {
    objectMapper.readTree(jsonResponse);
    Map<String, Object> map =
        objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});

    List<String> uris = new ArrayList<>();
    parseURIs(map, uris);
    return uris.stream()
        .map(
            x -> {
              try {
                return YovistoResponse.builder().uri(new URI(x)).build();
              } catch (URISyntaxException e) {
                throw new RuntimeException(e);
              }
            })
        .toArray(YovistoResponse[]::new);
  }

  private void parseURIs(Map<String, Object> jsonMap, List<String> uris) {
    jsonMap
        .entrySet()
        .forEach(
            entry -> {
              if (Objects.equals(entry.getKey(), "uri")) {
                if (entry.getValue() instanceof List) {
                  List<?> list = (List<?>) entry.getValue();
                  list.forEach(
                      listEntry -> {
                        if (listEntry instanceof String) {
                          uris.add((String) listEntry);
                        }
                      });
                } else if (entry.getValue() instanceof String) {
                  uris.add((String) entry.getValue());
                }

              } else if (entry.getValue() instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) entry.getValue();
                parseURIs(map, uris);
              } else if (entry.getValue() instanceof List) {
                List<?> list = (List<?>) entry.getValue();
                list.forEach(
                    listEntry -> {
                      if (listEntry instanceof Map) {
                        parseURIs((Map<String, Object>) listEntry, uris);
                      }
                    });
              }
            });
  }
}
