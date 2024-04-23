package org.edu_sharing.plugin_mongo.rating;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.edu_sharing.plugin_mongo.integrity.IntegrityService;
import org.edu_sharing.plugin_mongo.integrity.IntegrityServiceImpl;
import org.edu_sharing.repository.client.rpc.User;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.authority.AuthorityService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.util.HashMap;


@ExtendWith(MockitoExtension.class)
class IntegrityServiceImplTest {

    private IntegrityService underTest;

    @Mock
    private AuthorityService authorityService;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        underTest = new IntegrityServiceImpl(authorityService);
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
    void getAffiliationHasAffiliation() {
        // given
        String authority = "Muster";
        Serializable affiliation = "teacher";

        Mockito.when(authorityService.getUser(authority)).thenReturn(user);
        Mockito.when(user.getProperties()).thenReturn(new HashMap<>(){{
            put(CCConstants.CM_PROP_PERSON_EDU_SCHOOL_PRIMARY_AFFILIATION, affiliation);
        }});

        try (MockedStatic<AuthenticationUtil> authenticationUtilMockedStatic = Mockito.mockStatic(AuthenticationUtil.class)) {
            authenticationUtilMockedStatic.when(AuthenticationUtil::getFullyAuthenticatedUser).thenReturn(authority);

            // when
            String actual = underTest.getAffiliation();

            // then
            Assertions.assertEquals(affiliation, actual);
        }
    }

    @Test
    void getAffiliationHasNoAffiliation() {
        // given
        String authority = "Muster";

        Mockito.when(authorityService.getUser(authority)).thenReturn(user);
        Mockito.when(user.getProperties()).thenReturn(null);

        try (MockedStatic<AuthenticationUtil> authenticationUtilMockedStatic = Mockito.mockStatic(AuthenticationUtil.class)) {
            authenticationUtilMockedStatic.when(AuthenticationUtil::getFullyAuthenticatedUser).thenReturn(authority);

            // when
            String actual = underTest.getAffiliation();

            // then
            Assertions.assertNull(actual);
        }
    }
}
