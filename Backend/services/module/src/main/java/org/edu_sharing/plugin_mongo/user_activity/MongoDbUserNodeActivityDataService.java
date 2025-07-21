package org.edu_sharing.plugin_mongo.user_activity;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.edu_sharing.plugin_mongo.oplog.person.DeletePersonMongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.person.MongodbPersonDeletedAware;
import org.edu_sharing.service.InsufficientPermissionException;
import org.edu_sharing.service.search.SearchServiceElastic;
import org.edu_sharing.service.search.SearchServiceFactory;
import org.edu_sharing.service.tracking.user_tracking.UserNodeActivity;
import org.edu_sharing.service.tracking.user_tracking.UserNodeActivityDataService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Service implementation for managing user activity data on nodes using MongoDB.
 * This class provides functionality to retrieve and manage user activity records,
 * including handling permission checks and purging user data upon deletion of a user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MongoDbUserNodeActivityDataService implements UserNodeActivityDataService, MongodbPersonDeletedAware {
    private final UserNodeActivityDataRepository userNodeActivityDataRepository;
    private final PersonService personService;

    @PostConstruct
    void init() {
        log.info("Initializing MongoDbUserNodeActivityDataService");
    }


    @NotNull
    @Override
    public Page<UserNodeActivity> getDataForAllUsers(@NotNull Date after, int skip, int limit) {
        String username = AuthenticationUtil.getFullyAuthenticatedUser();
        if (!AuthenticationUtil.getAdminUserName().equals(username)) {
            throw new InsufficientPermissionException("User " + username + " has no access to this data!");
        }

        Page<UserNodeActivityData> allByTimestampAfter = userNodeActivityDataRepository.findAllByTimestampAfter(after, Pageable.ofSize(limit).withPage(skip / limit));
        return allByTimestampAfter.map(UserNodeActivity.class::cast);
    }

    @NotNull
    @Override
    public List<UserNodeActivity> getDataForUser(@NotNull String username, @NotNull Date after) {
        if (!username.equals(AuthenticationUtil.getFullyAuthenticatedUser())
                && !AuthenticationUtil.getSystemUserName().equals(username)
                && !AuthenticationUtil.getAdminUserName().equals(username)) {
            throw new InsufficientPermissionException("User " + username + " has no access to this data!");
        }

        NodeRef nodeRef = personService.getPersonOrNull(username);
        if (nodeRef == null) {
            throw new IllegalArgumentException("Person with username " + username + " does not exist!");
        }

        List<UserNodeActivityData> allByUserIdAndTimestampAfter = userNodeActivityDataRepository.findAllByUserIdAndTimestampAfter(nodeRef.getId(), after);
        return allByUserIdAndTimestampAfter.stream().map(UserNodeActivity.class::cast).toList();
    }

    @Override
    public void onPersonDeleted(DeletePersonMongoAlfOpLogData actionData) {
        userNodeActivityDataRepository.deleteAllByUserId(actionData.getNodeId());
        SearchServiceElastic elasticSearchService = (SearchServiceElastic) SearchServiceFactory.getLocalService();
        elasticSearchService.deleteUserActivitiesByUsername(actionData.getUsername());
    }
}
