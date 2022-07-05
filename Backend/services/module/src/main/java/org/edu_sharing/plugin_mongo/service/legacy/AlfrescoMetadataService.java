package org.edu_sharing.plugin_mongo.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionDoesNotExistException;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.lang3.StringUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.edu_sharing.plugin_mongo.domain.metadata.Metadata;
import org.edu_sharing.plugin_mongo.mongo.util.MongoSerializationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AlfrescoMetadataService {

    private final static String ID_KEY = "_id";
    public static final String ASPECTS_KEY = "aspects";
    public static final String NODE_TYPE_KEY = "nodeType";

    private final NodeService nodeService;
    private final VersionService versionService;

    private final AlfrescoMappingService mappingService;
    private final MongoDatabaseFactory databaseFactory;

    public Metadata getMetadata(String nodeId, String version) {

        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        String type = nodeService.getType(nodeRef).toString();

        if(StringUtils.isNoneBlank(version)){
            VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
            if(versionHistory == null){
                return null;
            }

            try {
                Version nodeVersion = versionHistory.getVersion(version);
                nodeRef = nodeVersion.getVersionedNodeRef();
            } catch (VersionDoesNotExistException ex){
                return null;
            }
        }

        Map<QName, Serializable> nativeProperties = nodeService.getProperties(nodeRef);
        Set<QName> aspects = nodeService.getAspects(nodeRef);


        HashMap<String, Object> properties = new HashMap<>(nativeProperties.size());
        for (Map.Entry<QName, Serializable> entry : nativeProperties.entrySet()) {
            if(entry.getValue() != null) {
                properties.put(entry.getKey().toString(), entry.getValue());
            }
        }

        Document document = new Document(ID_KEY, nodeId);
        document.put(NODE_TYPE_KEY, type);
        document.put(ASPECTS_KEY, aspects.stream().map(QName::toString).collect(Collectors.toList()));
        mappingService.setProperties(document, properties);

        return MongoSerializationUtil.toObject(document, databaseFactory.getCodecRegistry(), Metadata.class);
    }


    public List<Metadata> getMetadatas(Collection<String> ids) {
        List<Metadata> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            result.add(getMetadata(id, null));
        }
        return result;
    }


}
