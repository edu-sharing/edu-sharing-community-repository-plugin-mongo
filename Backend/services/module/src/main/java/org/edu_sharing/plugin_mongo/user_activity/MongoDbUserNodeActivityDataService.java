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
import org.edu_sharing.service.tracking.user_tracking.UserNodeActivity;
import org.edu_sharing.service.tracking.user_tracking.UserNodeActivityDataService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MongoDbUserNodeActivityDataService implements UserNodeActivityDataService, MongodbPersonDeletedAware {
    private final UserNodeActivityDataRepository userNodeActivityDataRepository;
    private final PersonService personService;

    @PostConstruct
    void init(){
      log.info("Initializing MongoDbUserNodeActivityDataService");
    }


    @NotNull
    @Override
    public Page<UserNodeActivity> getDataForAllUsers(@NotNull Date after, Pageable pageable) {
        String username = AuthenticationUtil.getFullyAuthenticatedUser();
        if (!AuthenticationUtil.getAdminUserName().equals(username)) {
            throw new InsufficientPermissionException("User " + username + " has no access to this data!");
        }

        return userNodeActivityDataRepository.findAllByTimestampAfter(after, pageable);
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
        if(nodeRef == null) {
            throw new IllegalArgumentException("Person with username " + username + " does not exist!");
        }

        return userNodeActivityDataRepository.findAllByUserIdAndTimestampAfter(nodeRef.getId(), after);
    }


    @Override
    public void onPersonDeleted(DeletePersonMongoAlfOpLogData actionData) {
        userNodeActivityDataRepository.deleteAllByUserId(actionData.getNodeId());
    }
}
