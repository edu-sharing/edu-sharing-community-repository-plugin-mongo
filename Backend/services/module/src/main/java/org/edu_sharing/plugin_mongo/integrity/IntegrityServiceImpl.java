package org.edu_sharing.plugin_mongo.integrity;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.edu_sharing.repository.client.rpc.User;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.authority.AuthorityService;

import java.io.Serializable;
import java.util.Map;

public class IntegrityServiceImpl implements IntegrityService {

    private final AuthorityService authorityService;

    public IntegrityServiceImpl(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @Override
    public String getAuthority(){
        return AuthenticationUtil.getFullyAuthenticatedUser(); // TODO can this be used on unit tests?
    }

    @Override
    public String getAffiliation(){
        String user = getAuthority();
        User userInfo = authorityService.getUser(user);
        Map<String, Serializable> properties = userInfo.getProperties();
        if(properties == null){
            return null;
        }
        return (String) properties.get(CCConstants.CM_PROP_PERSON_EDU_SCHOOL_PRIMARY_AFFILIATION);
    }
}
