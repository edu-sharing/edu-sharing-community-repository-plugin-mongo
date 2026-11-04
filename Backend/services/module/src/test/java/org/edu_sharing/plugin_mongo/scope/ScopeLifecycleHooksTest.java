package org.edu_sharing.plugin_mongo.scope;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.edu_sharing.plugin_mongo.oplog.MongoAlfOpLogService;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.scope.ScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScopeLifecycleHooksTest {

    static final StoreRef ARCHIVE = StoreRef.STORE_REF_ARCHIVE_SPACESSTORE;
    static final QName IO = QName.createQName(CCConstants.CCM_TYPE_IO);
    static final QName MAP = QName.createQName(CCConstants.CCM_TYPE_MAP);

    private NodeService nodeService;
    private ScopeService scopeService;
    private ScopePurgeHook purgeHook;
    private ScopeGroupDeleteHook groupHook;
    private AuthorityService authorityService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        nodeService = mock(NodeService.class);
        scopeService = mock(ScopeService.class);
        ObjectProvider<ScopeService> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(scopeService);
        // no transaction in the test: run the commit callback immediately
        MongoAlfOpLogService opLogService = mock(MongoAlfOpLogService.class);
        doAnswer(i -> {
            ((Consumer<Object>) i.getArgument(1)).accept(i.getArgument(0));
            return null;
        }).when(opLogService).registerOpLogAction(any(), any());
        authorityService = mock(AuthorityService.class);

        purgeHook = new ScopePurgeHook(mock(PolicyComponent.class), nodeService, opLogService, provider);
        groupHook = new ScopeGroupDeleteHook(mock(PolicyComponent.class), nodeService, authorityService, opLogService, provider);
    }

    private NodeRef node(String id, QName type) {
        NodeRef ref = new NodeRef(ARCHIVE, id);
        when(nodeService.getType(ref)).thenReturn(type);
        return ref;
    }

    private ChildAssociationRef child(NodeRef parent, NodeRef child, boolean primary) {
        return new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parent, QName.createQName("a"), child, primary, 0);
    }

    @Test
    void purgeOfAFolderCleansUpTheWholeHierarchy() {
        NodeRef folder = node("folder", MAP);
        NodeRef sub = node("sub", MAP);
        NodeRef io1 = node("io1", IO);
        NodeRef io2 = node("io2", IO);
        NodeRef linked = node("linked", IO);
        NodeRef other = node("preview", ContentModel.TYPE_CONTENT);
        when(nodeService.getChildAssocs(folder)).thenReturn(List.of(child(folder, io1, true), child(folder, sub, true), child(folder, linked, false), child(folder, other, true)));
        when(nodeService.getChildAssocs(sub)).thenReturn(List.of(child(sub, io2, true)));

        purgeHook.beforePurgeNode(folder);

        verify(scopeService).onIoPurged("io1");
        verify(scopeService).onIoPurged("io2");
        verifyNoMoreInteractions(scopeService);
    }

    @Test
    void trashcanKeepsTheEntries() {
        NodeRef io = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "io");
        ChildAssociationRef assoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "p"), QName.createQName("a"), io);

        purgeHook.onDeleteNode(assoc, true);
        verifyNoInteractions(scopeService);

        purgeHook.onDeleteNode(assoc, false);
        verify(scopeService).onIoPurged("io");
    }

    @Test
    void retryKeepsEntriesOfRestoredNodes() {
        when(nodeService.exists(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "restored"))).thenReturn(true);

        purgeHook.retry(new PurgeScopeNodeOpLogData("restored"));
        verifyNoInteractions(scopeService);

        purgeHook.retry(new PurgeScopeNodeOpLogData("gone"));
        verify(scopeService).onIoPurged("gone");
    }

    @Test
    void onlyScopeGroupsAreCleanedUp() {
        NodeRef scopeGroup = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "scope");
        NodeRef otherGroup = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "other");
        QName groupType = QName.createQName(CCConstants.CCM_PROP_GROUPEXTENSION_GROUPTYPE);
        when(nodeService.getProperty(scopeGroup, groupType)).thenReturn(CCConstants.GROUP_TYPE_SCOPE);
        when(nodeService.getProperty(scopeGroup, ContentModel.PROP_AUTHORITY_NAME)).thenReturn("GROUP_ORG_wlo");
        when(nodeService.getProperty(otherGroup, groupType)).thenReturn(CCConstants.EDITORIAL_GROUP_TYPE);

        groupHook.beforeDeleteNode(otherGroup);
        verifyNoInteractions(scopeService);

        groupHook.beforeDeleteNode(scopeGroup);
        verify(scopeService).onScopeGroupDeleted("GROUP_ORG_wlo");
    }

    @Test
    void groupRetryOnlyForDeletedGroups() {
        when(authorityService.authorityExists("GROUP_ORG_wlo")).thenReturn(true);
        groupHook.retry(new DeleteScopeGroupOpLogData("GROUP_ORG_wlo"));
        verifyNoInteractions(scopeService);

        groupHook.retry(new DeleteScopeGroupOpLogData("GROUP_ORG_gone"));
        verify(scopeService).onScopeGroupDeleted("GROUP_ORG_gone");
    }
}
