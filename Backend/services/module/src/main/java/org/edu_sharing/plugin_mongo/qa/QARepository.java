package org.edu_sharing.plugin_mongo.qa;

import org.edu_sharing.service.qa.domain.QAEntry;

import java.util.List;

public interface QARepository {
    List<QAEntry> findAllByNodeId(String nodeId);
    List<QAEntry> findAllByNodeIdAndCreator(String nodeId, String creator);

    void deleteAllByNodeIdAndCreator(String nodeId, String creator);

    List<QAEntry> saveAny(List<QAEntry> nodeEntries);

    void deleteAllById(List<String> ids);

    void deleteAllByNodeId(String nodeId);
}
