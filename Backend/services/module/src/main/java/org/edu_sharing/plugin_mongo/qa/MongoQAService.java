package org.edu_sharing.plugin_mongo.qa;

import lombok.RequiredArgsConstructor;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.restservices.qa.v1.domain.CreateQANodeRequestDTO;
import org.edu_sharing.restservices.qa.v1.domain.UpdateQAEntriesRequestDTO;
import org.edu_sharing.service.permission.annotation.NodePermission;
import org.edu_sharing.service.permission.annotation.Permission;
import org.edu_sharing.service.qa.QAService;
import org.edu_sharing.service.qa.domain.QAEntry;
import org.edu_sharing.service.qa.domain.QANode;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MongoQAService implements QAService {

    private final QANodeRepository nodeRepository;

    @Override
    @Permission(value = {CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_QA}, requiresUser = true)
    public void createQANode(String sourceId, @NodePermission(CCConstants.PERMISSION_WRITE) String nodeId, CreateQANodeRequestDTO requestData) {
        QANode qaNode = new QANode(
                sourceId,
                nodeId,
                new Date(),
                requestData.getUsedText(),
                requestData.getEntries().stream()
                        .map(x -> new QAEntry(x.getQuestion(), x.getAnswer(), false, false, null, null))
                        .collect(Collectors.toList()));
        nodeRepository.save(qaNode);
    }

    @Override
    @Permission(value = {CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_QA}, requiresUser = true)
    public void updateQANode(String sourceId, @NodePermission(CCConstants.PERMISSION_WRITE) String nodeId, UpdateQAEntriesRequestDTO requestData) {
        QANode qaNode = nodeRepository.findQANodeBySourceIdAndNodeId(sourceId, nodeId).orElseThrow(IllegalArgumentException::new);
        qaNode.setEntries(requestData.getQaEntries());
        nodeRepository.save(qaNode);
    }

    @Override
    @Permission(value = {CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_QA}, requiresUser = true)
    public QANode getQANode(String sourceId, @NodePermission(CCConstants.PERMISSION_READ) String nodeId) {
        return nodeRepository.findQANodeBySourceIdAndNodeId(sourceId, nodeId).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    @Permission(value = {CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_QA}, requiresUser = true)
    public List<QANode> getAllQANode(@NodePermission(CCConstants.PERMISSION_READ) String nodeId) {
        return nodeRepository.findAllQANodeByNodeIdIs(nodeId);
    }

    @Override
    @Permission(value = {CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_QA}, requiresUser = true)
    public List<QAEntry> getAllQAEntriesOf(@NodePermission(CCConstants.PERMISSION_READ) String nodeId) {
        return nodeRepository.findAllByNodeIdIs(nodeId).stream().flatMap(x -> x.getEntries().stream()).collect(Collectors.toList());
    }

    @Override
    @Permission(value = {CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_QA}, requiresUser = true)
    public List<QAEntry> getAllQAEntriesOf(String sourceId, @NodePermission(CCConstants.PERMISSION_READ) String nodeId) {
        return nodeRepository.findBySourceIdAndNodeId(sourceId, nodeId).orElseThrow(IllegalArgumentException::new).getEntries();
    }

    @Override
    @Permission(value = {CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_QA}, requiresUser = true)
    public void delete(@NodePermission(CCConstants.PERMISSION_WRITE) String nodeId) {
        nodeRepository.deleteAllByNodeId(nodeId);
    }

    @Override
    @Permission(value = {CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_QA}, requiresUser = true)
    public void delete(String sourceId, @NodePermission(CCConstants.PERMISSION_WRITE) String nodeId) {
        nodeRepository.deleteBySourceIdAndNodeId(sourceId, nodeId);
    }
}
