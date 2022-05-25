package org.edu_sharing.plugin_mongo.service.legacy;

import com.bazaarvoice.jolt.Diffy;
import com.bazaarvoice.jolt.JsonUtils;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.nodeservice.NodeServiceImpl;
import org.edu_sharing.service.nodeservice.RecurseMode;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class MongoNodeServiceImpl extends NodeServiceImpl {

    private final static String ID_KEY = "_id";
    private final static List<String> VERSION_KEYS = Arrays.asList("version", "version");
    public static final String ASPECTS_KEY = "aspects";
    public static final String NODE_TYPE_KEY = "nodeType";

    private final Logger logger = Logger.getLogger(NodeServiceImpl.class);

    private final String WORKSPACE_KEY = "workspace";
    private final String VERSIONSPACE_KEY = "version";
    private final MongoCollection<Document> workspaceCollection;
    private final MongoCollection<Document> versionCollection;

    private final AlfrescoMappingService mappingService;
    private final AlfrescoRuntimePropertyService runtimePropertyService;

    private final Configuration jsonPathConfig = Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS).build();


    private static final List<String> supportedNodeTypes = Arrays.asList(
            CCConstants.CCM_TYPE_IO,
            CCConstants.CCM_TYPE_MAP,
            //CCConstants.CCM_TYPE_COLLECTION_PROPOSAL,
            CCConstants.CCM_TYPE_REMOTEOBJECT,
            CCConstants.CCM_TYPE_SHARE
    );

    public MongoNodeServiceImpl(MongoDatabase database, AlfrescoMappingService mappingService, AlfrescoRuntimePropertyService runtimePropertyService) {
        super();
        //logger.setLevel(Level.DEBUG);
        logger.info("Hello World");
        this.mappingService = mappingService;
        this.runtimePropertyService = runtimePropertyService;

        this.workspaceCollection = database.getCollection(WORKSPACE_KEY);
        this.versionCollection = database.getCollection(VERSIONSPACE_KEY);
    }

    public MongoNodeServiceImpl(String appId, MongoDatabase database, AlfrescoMappingService mappingService, AlfrescoRuntimePropertyService runtimePropertyService) {
        super(appId);
        logger.info("Hello World");
        this.mappingService = mappingService;
        this.runtimePropertyService = runtimePropertyService;

        this.workspaceCollection = database.getCollection(WORKSPACE_KEY);
        this.versionCollection = database.getCollection(VERSIONSPACE_KEY);
    }

    private Document importNodeFromAlf(String storeProtocol, String storeId, String nodeId) throws Throwable {
        String[] aspects = super.getAspects(storeProtocol, storeId, nodeId);
        NodeRef nodeRef = new NodeRef(new StoreRef(storeProtocol, storeId), nodeId);
        Map<QName, Serializable> nativeProperties = super.nodeService.getProperties(nodeRef);

        HashMap<String, Object> properties = new HashMap<>(nativeProperties.size());
        for (Map.Entry<QName, Serializable> entry : nativeProperties.entrySet()) {
            if(entry.getValue() != null) {
                properties.put(entry.getKey().toString(), entry.getValue());
            }
        }

        Document document = new Document(ID_KEY, nodeId);
        document.put(NODE_TYPE_KEY, getType(nodeId));
        document.put(ASPECTS_KEY, Arrays.asList(aspects));

        mappingService.setProperties(document, properties);
        workspaceCollection.replaceOne(Filters.eq(ID_KEY, nodeId), document, new ReplaceOptions().upsert(true));
        logger.debug(String.format("Unknown node %s imported into mongodb", nodeId));
        return document;
    }

    private boolean isSupported(String nodeType) {
        return supportedNodeTypes.contains(nodeType);
    }

    @Override
    public void addAspect(String nodeId, String aspect) {
        super.addAspect(nodeId, aspect);

        if (!isSupported(getType(nodeId))) {
            return;
        }

        workspaceCollection.updateOne(Filters.eq(ID_KEY, nodeId), Updates.addToSet(ASPECTS_KEY, aspect));
    }

    @Override
    public void removeAspect(String nodeId, String aspect) {
        super.addAspect(nodeId, aspect);

        if (!isSupported(getType(nodeId))) {
            return;
        }
        workspaceCollection.updateOne(Filters.eq(ID_KEY, nodeId), Updates.pull(ASPECTS_KEY, aspect));
    }

    @Override
    public boolean hasAspect(String storeProtocol, String storeId, String nodeId, String aspect) {
        if (!isSupported(getType(nodeId))) {
            return super.hasAspect(storeProtocol, storeId, nodeId, aspect);
        }


        return workspaceCollection.find(Filters.and(
                        Filters.eq(ID_KEY, nodeId),
                        Filters.eq(ASPECTS_KEY, aspect)))
                .cursor()
                .hasNext();
    }

    @Override
    public String[] getAspects(String storeProtocol, String storeId, String nodeId) {

        Document document = workspaceCollection
                .find(Filters.eq(ID_KEY, nodeId))
                .projection(Projections.include(ASPECTS_KEY))
                .first();

        if (document == null) {
            try {
                if (isSupported(getType(nodeId))) {
                    document = importNodeFromAlf(storeProtocol, storeId, nodeId);
                }
                return super.getAspects(storeProtocol, storeId, nodeId);
            } catch (Throwable e) {
                logger.error(e);
                return super.getAspects(storeProtocol, storeId, nodeId);
            }
        }

        return Optional.of(document).
                map(x -> x.getList(ASPECTS_KEY, String.class))
                .map(x -> x.toArray(new String[0]))
                .orElse(null);
    }


    @Override
    public HashMap<String, Object> getProperties(String storeProtocol, String storeId, String nodeId) throws Throwable {

        Document document = workspaceCollection
                .find(Filters.eq(ID_KEY, nodeId))
                .first();

        if (document == null) {
            try {
                String nodeType = getType(nodeId);
                if (isSupported(nodeType)) {
                    document = importNodeFromAlf(storeProtocol, storeId, nodeId);
                }else{
                    return super.getProperties(storeProtocol, storeId, nodeId);
                }

            } catch (Throwable e) {
                logger.error(e);
                return super.getProperties(storeProtocol, storeId, nodeId);
            }
        }

        HashMap<String, Object> props = (HashMap<String, Object>)mappingService.getProperties(document);
        runtimePropertyService.addRuntimeProperties(storeProtocol, storeId, nodeId, document.getString(NODE_TYPE_KEY), props);

        //NodeRef nodeRef = new NodeRef(new StoreRef(storeProtocol,storeId), nodeId);
        //props = apiClient.addDynamicProperties(nodeRef, document.getString(NODE_TYPE_KEY), props);

        if(logger.isDebugEnabled()) {
            Map expected = super.getProperties(storeProtocol, storeId, nodeId);
            Diffy diffy = new Diffy();
            Diffy.Result result = diffy.diff(expected, props);
            if (!result.isEmpty()) {
                logger.debug("There is a diff: " + JsonUtils.toJsonString(result) + " for document: " + JsonUtils.toJsonString(document));
            }
        }

        return props;
    }

    @Override
    public String getProperty(String storeProtocol, String storeId, String nodeId, String property) {
        Document document = workspaceCollection
                .find(Filters.eq(ID_KEY, nodeId))
                .first();

        if (document == null) {
            try {
                String nodeType = getType(nodeId);
                if (isSupported(nodeType)) {
                    document = importNodeFromAlf(storeProtocol, storeId, nodeId);
                }else{
                    return super.getProperty(storeProtocol, storeId, nodeId, property);
                }

            } catch (Throwable e) {
                logger.error(e);
                return super.getProperty(storeProtocol, storeId, nodeId, property);
            }
        }

        Map<String, Object> props = mappingService.getProperties(document);
        runtimePropertyService.addRuntimeProperties(storeProtocol, storeId, nodeId, document.getString(NODE_TYPE_KEY), props);
        return Optional.ofNullable(props)
                .map(x -> x.get(property))
                .map(Object::toString)
                .orElse(null);
    }

    @Override
    public Serializable getPropertyNative(String storeProtocol, String storeId, String nodeId, String property) {
        Document document = workspaceCollection
                .find(Filters.eq(ID_KEY, nodeId))
                .first();

        if (document == null) {
            try {
                String nodeType = getType(nodeId);
                if (isSupported(nodeType)) {
                    document = importNodeFromAlf(storeProtocol, storeId, nodeId);
                }else{
                    return super.getPropertyNative(storeProtocol, storeId, nodeId, property);
                }

            } catch (Throwable e) {
                logger.error(e);
                return super.getPropertyNative(storeProtocol, storeId, nodeId, property);
            }
        }

        Map<String, Object> props = mappingService.getProperties(document);
        return Optional.ofNullable(props)
                .map(x -> x.get(property))
                .map(x -> (Serializable) x)
                .orElse(null);
    }

    @Override
    public void setProperty(String protocol, String storeId, String nodeId, String property, Serializable value) {

        super.setProperty(protocol, storeId, nodeId, property, value);

        if (!isSupported(getType(nodeId))) {
            return;
        }

//        Document document = Optional.ofNullable(workspaceCollection
//                        .find(Filters.eq(ID_KEY, nodeId))
//                        .first())
//                .orElse(new Document());

        try {
            importNodeFromAlf(protocol, storeId, nodeId);
        } catch (Throwable e) {
            logger.error(e.getMessage());
        }

//        HashMap<String, Object> props = new HashMap<>();
//        props.put(property, value);
//        mappingService.setProperties(document, props);
//
//        ReplaceOptions options = new ReplaceOptions().upsert(true);
//        workspaceCollection.replaceOne(Filters.eq(ID_KEY, nodeId), document, options);
    }

    @Override
    public void removeProperty(String storeProtocol, String storeId, String nodeId, String property) {
        super.removeProperty(storeProtocol, storeId, nodeId, property);

        if (!isSupported(getType(nodeId))) {
            return;
        }

        try {
            importNodeFromAlf(storeProtocol, storeId, nodeId);
        } catch (Throwable e) {
            logger.error(e.getMessage());
        }

//        Document document = Optional.ofNullable(workspaceCollection
//                        .find(Filters.eq(ID_KEY, nodeId))
//                        .first())
//                .orElse(new Document());
//
//        List<String> props = Collections.singletonList(property);
//        mappingService.removeProperties(document, props);
//
//        ReplaceOptions options = new ReplaceOptions().upsert(true);
//        workspaceCollection.replaceOne(Filters.eq(ID_KEY, nodeId), document, options);
    }


    @Override
    public void updateNodeNative(StoreRef store, String nodeId, Map<String, ?> _props) {
        super.updateNodeNative(store, nodeId, _props);

        if (!isSupported(getType(nodeId))) {
            return;
        }

        try {
            importNodeFromAlf(store.getProtocol(), store.getIdentifier(), nodeId);
        } catch (Throwable e) {
            logger.error(e.getMessage());
        }

//        Document document = workspaceCollection.find(Filters.eq(ID_KEY, nodeId)).first();
//        if (document == null) {
//            document = new Document(ID_KEY, nodeId);
//        }
//
//
//        Map<QName, Serializable> props = transformPropMap(_props);
//        HashMap<String, Object> propsNotNull = new HashMap<>(props.size());
//        for (Map.Entry<QName, Serializable> prop : props.entrySet()) {
//            if (prop.getValue() == null) {
//                continue;
//            }
//            propsNotNull.put(prop.getKey().toString(), prop.getValue());
//        }
//
//        mappingService.setProperties(document, propsNotNull);
//
//        ReplaceOptions options = new ReplaceOptions().upsert(true);
//        workspaceCollection.replaceOne(Filters.eq(ID_KEY, nodeId), document, options);
    }

    @Override
    public String createNodeBasic(StoreRef store, String parentID, String nodeTypeString, String childAssociation, HashMap<String, ?> _props) {
        String nodeId = super.createNodeBasic(store, parentID, nodeTypeString, childAssociation, _props);

        if (!isSupported(getType(nodeId))) {
            return nodeId;
        }

        List<NodeRef> nodes = new ArrayList<>();
        nodes.add(new NodeRef(store, nodeId));
        nodes.addAll(this.getChildrenRecursive(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId, null, RecurseMode.All));

        for (NodeRef node : nodes) {
            try {
               importNodeFromAlf(node.getStoreRef().getProtocol(), node.getStoreRef().getIdentifier(), node.getId());
            } catch (Throwable e) {
                logger.error(e);
            }
        }

        return nodeId;
    }

    @Override
    public NodeRef copyNode(String nodeId, String toNodeId, boolean copyChildren) throws Throwable {
        NodeRef nodeRef = super.copyNode(nodeId, toNodeId, copyChildren);
        if (!isSupported(getType(nodeId))) {
            return nodeRef;
        }

        List<NodeRef> nodes = new ArrayList<>();
        nodes.add(nodeRef);
        nodes.addAll(this.getChildrenRecursive(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId, null, RecurseMode.All));

        for (NodeRef node : nodes) {
            try {
                importNodeFromAlf(node.getStoreRef().getProtocol(), node.getStoreRef().getIdentifier(), node.getId());
            } catch (Throwable e) {
                logger.error(e);
            }
        }

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

        Document document = workspaceCollection.findOneAndUpdate(Filters.eq(ID_KEY, nodeId), Updates.set(String.join(".",VERSION_KEYS), version));
        if (document == null) {
            try {
                document = importNodeFromAlf(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol(), StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier(), nodeId);
            } catch (Throwable e) {
                logger.error(e);
                return;
            }
        }


        String key = document.getString(ID_KEY) + "_" + JsonPath.parse(document, jsonPathConfig).read(String.join(".",VERSION_KEYS));
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
        if (document == null) {
            //TODO null handling
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
        // TODO handle only by mongo if supported
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
            String version = JsonPath.parse(document, jsonPathConfig).read(String.join(".",VERSION_KEYS));

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
