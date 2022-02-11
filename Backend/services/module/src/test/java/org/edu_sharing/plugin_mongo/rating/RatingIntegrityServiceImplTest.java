package org.edu_sharing.plugin_mongo.rating;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.edu_sharing.alfresco.policy.GuestCagePolicy;
import org.edu_sharing.alfresco.service.toolpermission.ToolPermissionException;
import org.edu_sharing.repository.client.rpc.User;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.InsufficientPermissionException;
import org.edu_sharing.service.authority.AuthorityService;
import org.edu_sharing.service.nodeservice.NodeServiceHelper;
import org.edu_sharing.service.permission.PermissionService;
import org.edu_sharing.service.toolpermission.ToolPermissionHelper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;


@ExtendWith(MockitoExtension.class)
class RatingIntegrityServiceImplTest {

    private RatingIntegrityService underTest;

    @Mock
    private AuthorityService authorityService;

    @Mock
    private PermissionService permissionService;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        underTest = new RatingIntegrityServiceImpl(authorityService, permissionService);
    }

    @Test
    void getAuthority() {
        // given
        String authority = "Muster";

        try (MockedStatic<AuthenticationUtil> authenticationUtilMockedStatic = Mockito.mockStatic(AuthenticationUtil.class)) {
            authenticationUtilMockedStatic.when(AuthenticationUtil::getFullyAuthenticatedUser).thenReturn(authority);

            // when
            String actual = underTest.getAuthority();

            // then
            Assertions.assertEquals(authority, actual);
        }
    }

    @Test
    void getAffiliation() {
        // given
        String authority = "Muster";
        String affiliation = "teacher";

        Mockito.when(authorityService.getUser(authority)).thenReturn(user);
        Mockito.when(user.getProfileSettings()).thenReturn(Collections.singletonMap(CCConstants.CM_PROP_PERSON_EDU_SCHOOL_PRIMARY_AFFILIATION, affiliation));

        try (MockedStatic<AuthenticationUtil> authenticationUtilMockedStatic = Mockito.mockStatic(AuthenticationUtil.class)) {
            authenticationUtilMockedStatic.when(AuthenticationUtil::getFullyAuthenticatedUser).thenReturn(authority);

            // when
            String actual = underTest.getAffiliation();

            // then
            Assertions.assertEquals(affiliation, actual);
        }
    }

    @Test
    void checkPermissionsOk() {
        // given
        String authority = "Muster";
        String nodeId = "1";
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);

        Mockito.when(authorityService.isGuest()).thenReturn(false);

        try (MockedStatic<AuthenticationUtil> authenticationUtilMockedStatic = Mockito.mockStatic(AuthenticationUtil.class);
             MockedStatic<ToolPermissionHelper> authToolPermissionHelperMockedStatic = Mockito.mockStatic(ToolPermissionHelper.class);
             MockedStatic<NodeServiceHelper> nodeServiceHelperMockedStatic = Mockito.mockStatic(NodeServiceHelper.class)) {

            authenticationUtilMockedStatic.when(AuthenticationUtil::getFullyAuthenticatedUser).thenReturn(authority);

            authToolPermissionHelperMockedStatic
                    .when(() -> ToolPermissionHelper.throwIfToolpermissionMissing(CCConstants.CCM_VALUE_TOOLPERMISSION_RATE))
                    .then(Answers.RETURNS_DEFAULTS);

            nodeServiceHelperMockedStatic.when(() -> NodeServiceHelper.getType(nodeRef)).thenReturn(CCConstants.CCM_TYPE_IO);

            Mockito.when(permissionService.getPermissionsForAuthority(nodeId, authority)).thenReturn(Collections.singletonList(CCConstants.PERMISSION_RATE));

            // when
            // then
            underTest.checkPermissions(nodeId);

        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    void checkPermissionsThrowsGuestPermissionDeniedException() {
        // given
        String nodeId = "1";

        Mockito.when(authorityService.isGuest()).thenReturn(true);

        // when
        // then
        Assertions.assertThrows(GuestCagePolicy.GuestPermissionDeniedException.class, () -> underTest.checkPermissions(nodeId));
    }

    @Test
    void checkPermissionsThrowsToolPermissionMissing() {
        // given
        String nodeId = "1";

        Mockito.when(authorityService.isGuest()).thenReturn(false);

        try (MockedStatic<ToolPermissionHelper> authToolPermissionHelperMockedStatic = Mockito.mockStatic(ToolPermissionHelper.class)) {

            authToolPermissionHelperMockedStatic
                    .when(() -> ToolPermissionHelper.throwIfToolpermissionMissing(CCConstants.CCM_VALUE_TOOLPERMISSION_RATE))
                    .thenThrow(new ToolPermissionException(CCConstants.CCM_VALUE_TOOLPERMISSION_RATE));

            // when
            // then
            Assertions.assertThrows(ToolPermissionException.class, () -> underTest.checkPermissions(nodeId));
        }
    }

    @Test
    void checkPermissionsThrowsIllegalArgumentException() {
        // given
        String authority = "Muster";
        String nodeId = "1";
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);

        Mockito.when(authorityService.isGuest()).thenReturn(false);

        try (MockedStatic<AuthenticationUtil> authenticationUtilMockedStatic = Mockito.mockStatic(AuthenticationUtil.class);
             MockedStatic<ToolPermissionHelper> authToolPermissionHelperMockedStatic = Mockito.mockStatic(ToolPermissionHelper.class);
             MockedStatic<NodeServiceHelper> nodeServiceHelperMockedStatic = Mockito.mockStatic(NodeServiceHelper.class)) {

            authenticationUtilMockedStatic.when(AuthenticationUtil::getFullyAuthenticatedUser).thenReturn(authority);

            authToolPermissionHelperMockedStatic
                    .when(() -> ToolPermissionHelper.throwIfToolpermissionMissing(CCConstants.CCM_VALUE_TOOLPERMISSION_RATE))
                    .then(Answers.RETURNS_DEFAULTS);

            nodeServiceHelperMockedStatic.when(() -> NodeServiceHelper.getType(nodeRef)).thenReturn("Something different");

            // when
            // then
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.checkPermissions(nodeId));

        }
    }

    @Test
    void checkPermissionsThrowsInsufficientPermissionException() {
        // given
        String authority = "Muster";
        String nodeId = "1";
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);

        Mockito.when(authorityService.isGuest()).thenReturn(false);

        try (MockedStatic<AuthenticationUtil> authenticationUtilMockedStatic = Mockito.mockStatic(AuthenticationUtil.class);
             MockedStatic<ToolPermissionHelper> authToolPermissionHelperMockedStatic = Mockito.mockStatic(ToolPermissionHelper.class);
             MockedStatic<NodeServiceHelper> nodeServiceHelperMockedStatic = Mockito.mockStatic(NodeServiceHelper.class)) {

            authenticationUtilMockedStatic.when(AuthenticationUtil::getFullyAuthenticatedUser).thenReturn(authority);

            authToolPermissionHelperMockedStatic
                    .when(() -> ToolPermissionHelper.throwIfToolpermissionMissing(CCConstants.CCM_VALUE_TOOLPERMISSION_RATE))
                    .then(Answers.RETURNS_DEFAULTS);

            nodeServiceHelperMockedStatic.when(() -> NodeServiceHelper.getType(nodeRef)).thenReturn(CCConstants.CCM_TYPE_IO);

            Mockito.when(permissionService.getPermissionsForAuthority(nodeId, authority)).thenReturn(Collections.singletonList("something different"));

            // when
            // then
            Assertions.assertThrows(InsufficientPermissionException.class, () -> underTest.checkPermissions(nodeId));

        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}