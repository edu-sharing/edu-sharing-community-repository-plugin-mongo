package org.edu_sharing.plugin_mongo.qa;

import lombok.RequiredArgsConstructor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.lang3.StringUtils;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.restservices.qa.v1.domain.CreateQAEntryDTO;
import org.edu_sharing.restservices.qa.v1.domain.UpdateQAEntryDTO;
import org.edu_sharing.service.permission.annotation.NodePermission;
import org.edu_sharing.service.permission.annotation.Permission;
import org.edu_sharing.service.qa.QAService;
import org.edu_sharing.service.qa.domain.QAEntry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MongoQAService implements QAService {

    private final QARepository qaRepository;

    @Override
    public List<QAEntry> createQAEntries(@NotNull String nodeId, List<CreateQAEntryDTO> qaEntries) {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

        List<QAEntry> entries = qaEntries.stream()
                .peek(x -> {
                    x.setQuestion(x.getQuestion().trim());
                    x.setAnswer(x.getAnswer().trim());
                })
                .map(x -> new QAEntry(
                        null,
                        nodeId,
                        x.getQuestion(),
                        x.getAnswer(),
                        x.getUsedText(),
                        x.getEducationalLevel(),
                        new Date(),
                        currentUser,
                        null,
                        null,
                        false)
                )
                .collect(Collectors.toList());

        return qaRepository.saveAll(entries);
    }

    @Override
    public List<QAEntry> updateQAEntries(@NotNull String nodeId, List<UpdateQAEntryDTO> qaEntries) {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        Map<String, QAEntry> knownEntities = qaRepository.findAllByNodeIdAndCreator(nodeId, currentUser)
                .stream()
                .collect(Collectors.toMap(QAEntry::getId, x -> x));

        List<QAEntry> entries = qaEntries.stream()
                .peek(x -> {
                    x.setQuestion(x.getQuestion().trim());
                    x.setAnswer(x.getAnswer().trim());
                })
                .map(x -> Optional.of(x.getId())
                        .map(knownEntities::get)
                        .map(entry -> {
                            UpdateQAEntryDTO knownEntity = new UpdateQAEntryDTO(entry.getId(), entry.getQuestion(), entry.getAnswer(), entry.getUsedText(), entry.getEducationalLevel());
                            if (!knownEntity.equals(x)) {
                                entry.setAnswer(x.getAnswer());
                                entry.setQuestion(x.getQuestion());
                                entry.setUsedText(x.getUsedText());
                                entry.setEducationalLevel(x.getEducationalLevel());

                                if (!currentUser.equals(entry.getCreatedBy())) {
                                    entry.setReviewedBy(currentUser);
                                    entry.setLastReviewed(new Date());
                                    entry.setEdited(true);
                                }
                            }
                            return entry;
                        }).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return qaRepository.saveAny(entries);
    }

    @NotNull
    @Override
    @Permission(value = {CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_QA}, requiresUser = true)
    public List<QAEntry> getAllQAEntriesOf(@NotNull @NodePermission(CCConstants.PERMISSION_READ) String nodeId, String creator) {
        if (StringUtils.isBlank(creator)) {
            return qaRepository.findAllByNodeId(nodeId);
        }
        return qaRepository.findAllByNodeIdAndCreator(nodeId, creator);
    }

    @Override
    @Permission(value = {CCConstants.CCM_VALUE_TOOLPERMISSION_MANAGE_QA}, requiresUser = true)
    public void delete(@NotNull @NodePermission(CCConstants.PERMISSION_WRITE) String nodeId, String creator) {
        if (StringUtils.isBlank(creator)) {
            qaRepository.deleteAllByNodeId(nodeId);
        } else {
            qaRepository.deleteAllByNodeIdAndCreator(nodeId, creator);
        }
    }

    @Override
    public void delete(@NotNull List<String> ids) {
        qaRepository.deleteAllById(ids);
    }
}
