package org.edu_sharing.plugin_mongo.scope;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogService;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.scope.ScopeService;
import org.edu_sharing.service.scope.ScopesSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScopeUploadPolicyTest {

    static final QName IO = QName.createQName(CCConstants.CCM_TYPE_IO);
    static final QName MAP = QName.createQName(CCConstants.CCM_TYPE_MAP);

    private NodeService nodeService;
    private ScopeService scopeService;
    private ScopesSettings settings;
    private ScopeUploadPolicy underTest;
    private MockedStatic<AuthenticationUtil> authenticationUtil;

    private NodeRef ref(String id) {
        return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    }

    private void parent(NodeRef child, NodeRef parent) {
        when(nodeService.getPrimaryParent(child)).thenReturn(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parent, QName.createQName("a"), child));
    }

    private void groupFolder(NodeRef folder, NodeRef group, String groupType, String name) {
        when(nodeService.hasAspect(folder, QName.createQName(CCConstants.CCM_ASPECT_EDUGROUP_FOLDER))).thenReturn(true);
        when(nodeService.getProperty(folder, QName.createQName(CCConstants.CCM_PROP_EDUGROUP_FOLDER_ORGANISATION))).thenReturn(group);
        when(nodeService.exists(group)).thenReturn(true);
        when(nodeService.getProperty(group, QName.createQName(CCConstants.CCM_PROP_GROUPEXTENSION_GROUPTYPE))).thenReturn(groupType);
        when(nodeService.getProperty(group, ContentModel.PROP_AUTHORITY_NAME)).thenReturn(name);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        nodeService = mock(NodeService.class);
        // scope folder  <- sub <- (upload), org folder <- (upload), home <- (upload)
        NodeRef scopeFolder = ref("scopeFolder");
        NodeRef sub = ref("sub");
        NodeRef orgFolder = ref("orgFolder");
        NodeRef home = ref("home");
        NodeRef root = ref("root");
        for (NodeRef folder : new NodeRef[]{scopeFolder, sub, orgFolder, home}) {
            when(nodeService.getType(folder)).thenReturn(MAP);
        }
        when(nodeService.getType(ref("parentIo"))).thenReturn(IO);
        parent(sub, scopeFolder);
        parent(scopeFolder, root);
        parent(orgFolder, root);
        parent(home, root);
        groupFolder(scopeFolder, ref("scopeGroup"), CCConstants.GROUP_TYPE_SCOPE, "GROUP_ORG_wlo");
        groupFolder(orgFolder, ref("orgGroup"), null, "GROUP_ORG_school");

        scopeService = mock(ScopeService.class);
        ObjectProvider<ScopeService> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(scopeService);
        MongoAlfOpLogService opLogService = mock(MongoAlfOpLogService.class);
        doAnswer(i -> {
            ((Consumer<Object>) i.getArgument(1)).accept(i.getArgument(0));
            return null;
        }).when(opLogService).registerOpLogAction(any(), any());
        settings = new ScopesSettings();
        settings.setEnabled(true);

        authenticationUtil = Mockito.mockStatic(AuthenticationUtil.class);
        authenticationUtil.when(AuthenticationUtil::getFullyAuthenticatedUser).thenReturn("uploader");
        authenticationUtil.when(() -> AuthenticationUtil.runAsSystem(any()))
                .thenAnswer(i -> ((AuthenticationUtil.RunAsWork<?>) i.getArgument(0)).doWork());

        underTest = new ScopeUploadPolicy(mock(PolicyComponent.class), nodeService, opLogService, provider, settings);
    }

    @AfterEach
    void tearDown() {
        authenticationUtil.close();
    }

    private void upload(String parentId) {
        underTest.onCreateNode(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, ref(parentId), QName.createQName("a"), ref("new")));
    }

    @Test
    void uploadIntoTheScopeFolderOrDeeper() {
        upload("scopeFolder");
        upload("sub");
        verify(scopeService, times(2)).onUpload("GROUP_ORG_wlo", "new", "uploader");
    }

    @Test
    void uploadsElsewhereCreateNoEntry() {
        upload("orgFolder");
        upload("home");
        verifyNoInteractions(scopeService);
    }

    @Test
    void childObjectsAreSkipped() {
        upload("parentIo");
        verifyNoInteractions(scopeService);
    }

    @Test
    void nothingWhileDisabled() {
        settings.setEnabled(false);
        upload("scopeFolder");
        verifyNoInteractions(scopeService);
    }
}
