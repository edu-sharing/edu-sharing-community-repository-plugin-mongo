package org.edu_sharing.plugin_mongo.user_activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.edu_sharing.alfresco.service.guest.GuestService;
import org.edu_sharing.service.tracking.ActivityOnNodeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNodeActivityTracker {

    private final GuestService guessService;
    private final UserNodeActivityDataRepository userNodeActivityDataRepository;
    private final PersonService personService;

    @Async
    @EventListener
    public void handleActivityOnNodeEvent(ActivityOnNodeEvent event) {
        if (event.getAuthorityName() == null
                || guessService.getAllGuestAuthorities().contains(event.getAuthorityName())
                || event.getAuthorityName().equals(AuthenticationUtil.getSystemUserName())
        ) {
            return;
        }

        NodeRef person = personService.getPerson(event.getAuthorityName());
        if(person == null){
            return;
        }

        userNodeActivityDataRepository.save(new UserNodeActivityData(
                event.getNodeRef().getId(),
                person.getId(),
                event.getType().name(),
                new Date()
        ));
    }
}
