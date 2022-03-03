package org.edu_sharing.plugin_mongo.rating;

import com.mongodb.client.MongoDatabase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.edu_sharing.alfresco.policy.GuestCagePolicy;
import org.edu_sharing.repository.client.rpc.User;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.InsufficientPermissionException;
import org.edu_sharing.service.authority.AuthorityService;
import org.edu_sharing.service.nodeservice.NodeServiceHelper;
import org.edu_sharing.service.permission.PermissionService;
import org.edu_sharing.service.toolpermission.ToolPermissionHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatingIntegrityServiceImpl implements RatingIntegrityService {

    private final AuthorityService authorityService;
    private final PermissionService permissionService;

    public RatingIntegrityServiceImpl(AuthorityService authorityService, PermissionService permissionService) {
        this.authorityService = authorityService;
        this.permissionService = permissionService;
    }

    @Override
    public String getAffiliation(){
        String user = getAuthority();
        User userInfo = authorityService.getUser(user);
        HashMap<String, Serializable> properties = userInfo.getProperties();
        if(properties == null){
            return null;
        }
        return (String) properties.get(CCConstants.CM_PROP_PERSON_EDU_SCHOOL_PRIMARY_AFFILIATION);
    }

    @Override
    public  String getAuthority(){
        return AuthenticationUtil.getFullyAuthenticatedUser(); // TODO can this be used on unit tests?

    }

    @Override
    public void checkPermissions(String nodeId) throws Exception {
        if(authorityService.isGuest()){
            throw new GuestCagePolicy.GuestPermissionDeniedException("guests can not use ratings");
        }

        ToolPermissionHelper.throwIfToolpermissionMissing(CCConstants.CCM_VALUE_TOOLPERMISSION_RATE); // TODO can this be used on unit tests?
        if(!NodeServiceHelper.getType(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId)).equals(CCConstants.CCM_TYPE_IO)){ // TODO can this be used on unit tests?
            throw new IllegalArgumentException("Ratings only supported for nodes of type "+CCConstants.CCM_TYPE_IO);
        }

        List<String> permissions = permissionService.getPermissionsForAuthority(nodeId, AuthenticationUtil.getFullyAuthenticatedUser());
        if (!permissions.contains(CCConstants.PERMISSION_RATE)) {
            throw new InsufficientPermissionException("No permission '" + CCConstants.PERMISSION_RATE + "' to add ratings to node " + nodeId);
        }
    }
}
