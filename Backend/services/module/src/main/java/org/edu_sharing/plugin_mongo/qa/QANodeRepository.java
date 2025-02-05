package org.edu_sharing.plugin_mongo.qa;

import org.edu_sharing.service.qa.domain.QANode;

import java.util.List;
import java.util.Optional;

public interface QANodeRepository {
    Optional<QANode> findQANodeBySourceIdAndNodeId(String sourceId, String nodeId);

    List<QANode> findAllQANodeByNodeIdIs(String nodeId);

    Optional<QAEntriesOnly> findBySourceIdAndNodeId(String sourceId, String nodeId);

    List<QAEntriesOnly> findAllByNodeIdIs(String nodeId);

    void deleteAllByNodeId(String nodeId);

    void deleteBySourceIdAndNodeId(String sourceId, String nodeId);

    void save(QANode qaNode);
}
