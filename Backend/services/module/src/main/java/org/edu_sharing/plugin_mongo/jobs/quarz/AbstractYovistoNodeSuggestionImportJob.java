package org.edu_sharing.plugin_mongo.jobs.quarz;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.edu_sharing.plugin_mongo.domain.suggestion.Suggestion;
import org.edu_sharing.plugin_mongo.domain.suggestion.SuggestionType;
import org.edu_sharing.plugin_mongo.repository.SuggestionRepository;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.jobs.helper.NodeRunner;
import org.edu_sharing.repository.server.jobs.quartz.AbstractJob;
import org.edu_sharing.repository.server.jobs.quartz.annotation.JobFieldDescription;
import org.edu_sharing.service.nodeservice.NodeService;
import org.edu_sharing.service.nodeservice.RecurseMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractYovistoNodeSuggestionImportJob extends AbstractJob {

  public static final String HTTPS_WLO_YOVISTO_COM = "https://wlo.yovisto.com";

  @JobFieldDescription(description = "folder id to start from")
  protected String startFolder;

  @JobFieldDescription(
      description =
          "Lucene query to fetch the nodes that shall be processed. When used, the 'startFolder' parameter is ignored")
  protected String lucene;

  @JobFieldDescription(
      description = "Element types to modify, e.g. ccm:map,ccm:io",
      sampleValue = "ccm:io")
  protected List<String> types;

  @JobFieldDescription(description = "RecurseMode to use")
  protected RecurseMode recurseMode;

  @Autowired protected NodeService nodeService;
  @Autowired protected SuggestionRepository suggenstionRepository;

  protected ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {

    super.execute(context);

    lucene = prepareParam(context, "lucene", false);
    startFolder = prepareParam(context, "startFolder", true);

    try {
      types =
          ((ArrayList<String>) context.getJobDetail().getJobDataMap().get("types"))
              .stream()
                  .map(String::trim)
                  .map(CCConstants::getValidGlobalName)
                  .collect(Collectors.toList());
    } catch (Throwable ignored) {
    }

    if (types == null || types.isEmpty()) {
      throwMissingParam("types");
    }

    recurseMode = RecurseMode.Folders;
    try {
      if (context.getJobDetail().getJobDataMap().get("recurseMode") != null) {
        recurseMode =
            RecurseMode.valueOf((String) context.getJobDetail().getJobDataMap().get("recurseMode"));
      }
    } catch (Throwable t) {
      throw new IllegalArgumentException("Missing or invalid value for parameter 'recurseMode'", t);
    }

    NodeRunner runner = new NodeRunner();

    runner.setTask(
        (ref) -> {
          if (isInterrupted()) {
            return;
          }
          try {
            logger.info("Import suggestions for node " + ref.getId());

            HashMap<String, Object> properties =
                nodeService.getProperties(
                    ref.getStoreRef().getProtocol(),
                    ref.getStoreRef().getIdentifier(),
                    ref.getId());

            String message = buildRequestBody(properties);
            if (message == null) {
              return;
            }

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
              HttpPost httpPost = new HttpPost(HTTPS_WLO_YOVISTO_COM + getApiEndpointUri());
              httpPost.setEntity(new StringEntity(String.format("{\"text\":\"%s\"}", message), "UTF-8"));
              httpPost.setHeader("Accept", "application/json");
              httpPost.setHeader("Content-Type", "application/json");

              HttpResponse response = httpClient.execute(httpPost);
              HttpEntity entity = response.getEntity();
              if (entity == null) {
                return;
              }

              String jsonResponse = EntityUtils.toString(entity, "UTF-8");
              Suggestion.SuggestionBuilder suggestionBuilder = Suggestion.builder()
                      .id(getId())
                      .nodeId(ref.getId())
                      .type(SuggestionType.AI);

              fillSuggestionData(suggestionBuilder, jsonResponse);
              Suggestion suggestion = suggestionBuilder.build();

              suggenstionRepository.addOrUpdate(
                  suggestion.getNodeId(), suggestion.getId(), suggestion);
            }

          } catch (Throwable e) {
            logger.error(e.getMessage());
          }
        });

    runner.setTypes(types);
    runner.setRunAsSystem(true);
    runner.setThreaded(false);
    runner.setRecurseMode(recurseMode);
    runner.setStartFolder(startFolder);
    runner.setLucene(lucene);
    runner.setKeepModifiedDate(true);
    runner.setTransaction(NodeRunner.TransactionMode.Local);
    int count = runner.run();
    logger.info("Processed " + count + " nodes");
  }

  @NotNull
  protected abstract String getId();

  protected abstract void fillSuggestionData(Suggestion.SuggestionBuilder suggestionBuilder, String jsonResponse) throws IOException;

  @NotNull
  protected abstract String getApiEndpointUri();

  @Nullable
  protected abstract String buildRequestBody(HashMap<String, Object> properties);

  private String prepareParam(JobExecutionContext context, String param, boolean required) {
    String value = (String) context.getJobDetail().getJobDataMap().get(param);
    if (value == null && required) {
      throwMissingParam(param);
    }
    return value;
  }

  private void throwMissingParam(String param) {
    throw new IllegalArgumentException("Missing required parameter(s) '" + param + "'");
  }
}
