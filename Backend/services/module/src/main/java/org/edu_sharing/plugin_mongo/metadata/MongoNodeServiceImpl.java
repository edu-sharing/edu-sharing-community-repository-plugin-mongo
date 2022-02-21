package org.edu_sharing.plugin_mongo.metadata;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.edu_sharing.alfrescocontext.gate.AlfAppContextGate;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.nodeservice.NodeServiceImpl;
import org.edu_sharing.service.nodeservice.RecurseMode;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class MongoNodeServiceImpl extends NodeServiceImpl {

    private final static String  ID_KEY = "_id";

    private final Logger logger = Logger.getLogger(NodeServiceImpl.class);

    private final String WORKSPACE_KEY = "workspace";
    private final String VERSION_KEY = "version";
    private final MongoCollection<Document> workspaceCollection;
    private final MongoCollection<Document> versionCollection;

    private final AlfrescoMappingService mappingService;

    private final ServiceRegistry serviceRegistry;

    private static final List<String> supportedNodeTypes = Arrays.asList(
            CCConstants.CCM_TYPE_IO,
            CCConstants.CCM_TYPE_MAP,
            //CCConstants.CCM_TYPE_COLLECTION_PROPOSAL,
            CCConstants.CCM_TYPE_REMOTEOBJECT,
            CCConstants.CCM_TYPE_SHARE
    );

    public MongoNodeServiceImpl(MongoDatabase database, AlfrescoMappingService mappingService) {
        logger.info("Hello World");
        this.mappingService = mappingService;
        this.workspaceCollection = database.getCollection(WORKSPACE_KEY);
        this.versionCollection = database.getCollection(VERSION_KEY);

        ApplicationContext applicationContext = AlfAppContextGate.getApplicationContext();
        this.serviceRegistry = (ServiceRegistry) applicationContext.getBean("ServiceRegistry");

    }

    public MongoNodeServiceImpl(String appId, MongoDatabase database, AlfrescoMappingService mappingService) {
        super(appId);
        logger.info("Hello World");
        this.mappingService = mappingService;
        this.workspaceCollection = database.getCollection(WORKSPACE_KEY);
        this.versionCollection = database.getCollection(VERSION_KEY);

        ApplicationContext applicationContext = AlfAppContextGate.getApplicationContext();
        this.serviceRegistry = (ServiceRegistry) applicationContext.getBean("ServiceRegistry");

    }


    private boolean isSupported(String nodeType) {
        return supportedNodeTypes.contains(nodeType);
    }

    @Override
    public void addAspect(String nodeId, String aspect) {
        if (!isSupported(getType(nodeId))) {
            super.addAspect(nodeId, aspect);
            return;
        }

        workspaceCollection.updateOne(Filters.eq(ID_KEY, nodeId), Updates.addToSet("aspects", aspect));
    }

    @Override
    public void removeAspect(String nodeId, String aspect) {
        if (!isSupported(getType(nodeId))) {
            super.addAspect(nodeId, aspect);
            return;
        }

        workspaceCollection.updateOne(Filters.eq(ID_KEY, nodeId), Updates.pull("aspects", aspect));
    }

    @Override
    public boolean hasAspect(String storeProtocol, String storeId, String nodeId, String aspect) {
        if (!isSupported(getType(nodeId))) {
            return super.hasAspect(storeProtocol, storeId, nodeId, aspect);
        }

        return workspaceCollection.find(Filters.and(
                        Filters.eq(ID_KEY, nodeId),
                        Filters.eq("aspects", aspect)))
                .cursor()
                .hasNext();
    }

    @Override
    public String[] getAspects(String storeProtocol, String storeId, String nodeId) {
        if (!isSupported(getType(nodeId))) {
            return super.getAspects(storeProtocol, storeId, nodeId);
        }

        Document document = workspaceCollection
                .find(Filters.eq(ID_KEY, nodeId))
                .projection(Projections.include("aspects"))
                .first();

        return Optional.ofNullable(document).
                map(x->x.getList("aspects", String.class))
                .map(x -> x.toArray(new String[0]))
                .orElse(null);
    }


    @Override
    public HashMap<String, Object> getProperties(String storeProtocol, String storeId, String nodeId) throws Throwable {
        HashMap<String, Object> result = super.getProperties(storeProtocol, storeId, nodeId);

        if (!isSupported(getType(nodeId))) {
            return result;
        }

        Document document = workspaceCollection
                .find(Filters.eq(ID_KEY, nodeId))
                .first();

        if(document == null){
            return result;
        }

        Map<String, Object> props = mappingService.getProperties(document);
        result.putAll(props);

        return result;
    }

    @Override
    public void setProperty(String protocol, String storeId, String nodeId, String property, Serializable value) {
        super.setProperty(protocol, storeId, nodeId, property, value);

        if (!isSupported(getType(nodeId))) {
            return;
        }

        Document document = Optional.ofNullable(workspaceCollection
                        .find(Filters.eq(ID_KEY, nodeId))
                        .first())
                .orElse(new Document());

        HashMap<String, Object> props = new HashMap<>();
        props.put(property, value);
        mappingService.setProperties(document, props);

        ReplaceOptions options = new ReplaceOptions().upsert(true);
        workspaceCollection.replaceOne(Filters.eq(ID_KEY, nodeId), document, options);
    }

    @Override
    public void removeProperty(String storeProtocol, String storeId, String nodeId, String property) {
        super.removeProperty(storeProtocol, storeId, nodeId, property);

        if (!isSupported(getType(nodeId))) {
            return;
        }

        Document document = Optional.ofNullable(workspaceCollection
                        .find(Filters.eq(ID_KEY, nodeId))
                        .first())
                .orElse(new Document());

        List<String> props = Collections.singletonList(property);
        mappingService.removeProperties(document, props);

        ReplaceOptions options = new ReplaceOptions().upsert(true);
        workspaceCollection.replaceOne(Filters.eq(ID_KEY, nodeId), document, options);
    }


    @Override
    public String createNodeBasic(StoreRef store, String parentID, String nodeTypeString, String childAssociation, HashMap<String, ?> _props) {
        String nodeId = super.createNodeBasic(store, parentID, nodeTypeString, childAssociation, _props);

        if(!isSupported(getType(nodeId))){
            return nodeId;
        }


        //TODO copyChildren?
        Document document = new Document(ID_KEY, nodeId);
        mappingService.setProperties(document, (HashMap<String, Object>) _props);

        ReplaceOptions options = new ReplaceOptions().upsert(true);
        workspaceCollection.replaceOne(Filters.eq(ID_KEY, nodeId), document, options);

        return nodeId;
    }

    @Override
    public void updateNodeNative(StoreRef store, String nodeId, HashMap<String, ?> _props) {
        super.updateNodeNative(store, nodeId, _props);

        if(!isSupported(getType(nodeId))){
            return;
        }

        Document document = workspaceCollection.find(Filters.eq(ID_KEY, nodeId)).first();
        if(document == null) {
            document = new Document(ID_KEY, nodeId);
        }

        mappingService.setProperties(document, (HashMap<String, Object>) _props);

        ReplaceOptions options = new ReplaceOptions().upsert(true);
        workspaceCollection.replaceOne(Filters.eq(ID_KEY, nodeId), document, options);
    }

    @Override
    public NodeRef copyNode(String nodeId, String toNodeId, boolean copyChildren) throws Throwable {
        NodeRef nodeRef = super.copyNode(nodeId, toNodeId, copyChildren);
        if (!isSupported(getType(nodeId))) {
            return nodeRef;
        }

        //TODO copyChildren
        Document document = workspaceCollection.find(Filters.eq(ID_KEY, nodeId)).first();
        if(document == null){
            return nodeRef;
        }

        document.replace(ID_KEY, toNodeId);
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        workspaceCollection.replaceOne(Filters.eq(ID_KEY, nodeId), document, options);

        return nodeRef;
    }

    @Override
    public void createVersion(String nodeId) throws Exception {
        super.createVersion(nodeId);

        if (!isSupported(getType(nodeId))) {
            return;
        }

        StoreRef store = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        String version = super.getProperty(store.getProtocol(), store.getIdentifier(), nodeId, "{http://www.alfresco.org/model/content/1.0}versionLabel");

        Document document = workspaceCollection.findOneAndUpdate(Filters.eq(ID_KEY, nodeId), Updates.set("version", version));
        if(document == null) {
            return;
        }

        String key = document.getString(ID_KEY) + "_" + document.getString("version");
        document.replace(ID_KEY, key);

        ReplaceOptions options = new ReplaceOptions().upsert(true);
        versionCollection.replaceOne(Filters.eq(ID_KEY, key), document, options);
    }

    @Override
    public void revertVersion(String nodeId, String verLbl) throws Exception {
        super.revertVersion(nodeId, verLbl);

        if (!isSupported(getType(nodeId))) {
            return;
        }

        String key = nodeId + "_" + verLbl;

        Document document = versionCollection.find(Filters.eq(ID_KEY, key)).first();
        if(document == null) {
            return;
        }

        document.replace(ID_KEY, nodeId);

        ReplaceOptions options = new ReplaceOptions().upsert(true);
        workspaceCollection.replaceOne(Filters.eq(ID_KEY, nodeId), document, options);
    }

    @Override
    public void deleteVersionHistory(String nodeId) throws Exception {
        super.deleteVersionHistory(nodeId);

        if (!isSupported(getType(nodeId))) {
            return;
        }

        versionCollection.deleteMany(Filters.regex(ID_KEY, "^" + nodeId + "_"));
    }

    @Override
    public HashMap<String, HashMap<String, Object>> getVersionHistory(String nodeId) throws Throwable {
        HashMap<String, HashMap<String, Object>> result = super.getVersionHistory(nodeId);

        if (!isSupported(getType(nodeId))) {
            return result;
        }

        serviceRegistry.getVersionService();
        VersionService versionService = serviceRegistry.getVersionService();
        VersionHistory versionHistory = versionService.getVersionHistory(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId));

        Map<String, String> versionLabelMapping = new HashMap<>();
        if (versionHistory != null && versionHistory.getAllVersions() != null && versionHistory.getAllVersions().size() > 0) {
            Collection<Version> versions = versionHistory.getAllVersions();
            for (Version version : versions) {
                versionLabelMapping.put(version.getVersionLabel(), version.getFrozenStateNodeRef().getId());
            }
        }

        List<Document> documents = new ArrayList<>();
        versionCollection.find(Filters.regex(ID_KEY, "^" + nodeId + "_"))
                .sort(Sorts.descending(ID_KEY))
                .into(documents);

        for (Document document : documents) {
            String version = document.getString("version");

            if (!versionLabelMapping.containsKey(version)) {
                continue;
            }

            Map<String, Object> properties = mappingService.getProperties(document);
            result.get(versionLabelMapping.get(version)).putAll(properties);
        }

        return result;
    }

    @Override
    public void removeNode(String nodeId, String parentId, boolean recycle) {
        org.alfresco.service.cmr.repository.NodeRef nodeRef = new org.alfresco.service.cmr.repository.NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, nodeId);
        List<org.alfresco.service.cmr.repository.NodeRef> nodes = this.getChildrenRecursive(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId, null, RecurseMode.All);
        nodes.add(0, nodeRef);

        super.removeNode(nodeId, parentId, recycle);

        if (!isSupported(getType(nodeId))) {
            return;
        }

        // we do nothing when recycling
        if (recycle) {
            return;
        }

        workspaceCollection.deleteMany(Filters.in(ID_KEY, nodes.stream().map(NodeRef::getId).collect(Collectors.toList())));
        versionCollection.deleteMany(Filters.in(ID_KEY, nodes.stream().map(x -> "/^" + x.getId() + "_").collect(Collectors.toList())));
    }

    @Override
    public void removeNodeForce(String storeProtocol, String storeId, String nodeId, boolean recycle) {
        org.alfresco.service.cmr.repository.NodeRef nodeRef = new org.alfresco.service.cmr.repository.NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, nodeId);
        List<org.alfresco.service.cmr.repository.NodeRef> nodes = this.getChildrenRecursive(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId, null, RecurseMode.All);
        nodes.add(0, nodeRef);

        super.removeNodeForce(storeProtocol, storeId, nodeId, recycle);

        if (!isSupported(getType(nodeId))) {
            return;
        }


        // we do nothing when recycling
        if (recycle) {
            return;
        }

        workspaceCollection.deleteMany(Filters.in(ID_KEY, nodes.stream().map(NodeRef::getId).collect(Collectors.toList())));
        versionCollection.deleteMany(Filters.in(ID_KEY, nodes.stream().map(x -> "/^" + x.getId() + "_").collect(Collectors.toList())));
    }
}
