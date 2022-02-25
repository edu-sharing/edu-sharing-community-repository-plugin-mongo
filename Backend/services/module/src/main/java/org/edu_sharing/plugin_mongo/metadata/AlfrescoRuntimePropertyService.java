package org.edu_sharing.plugin_mongo.metadata;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.edu_sharing.alfresco.repository.server.authentication.Context;
import org.edu_sharing.alfresco.workspace_administration.NodeServiceInterceptor;
import org.edu_sharing.alfrescocontext.gate.AlfAppContextGate;
import org.edu_sharing.metadataset.v2.MetadataKey;
import org.edu_sharing.metadataset.v2.MetadataSet;
import org.edu_sharing.metadataset.v2.MetadataWidget;
import org.edu_sharing.metadataset.v2.tools.MetadataHelper;
import org.edu_sharing.repository.client.rpc.User;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.client.tools.UrlTool;
import org.edu_sharing.repository.client.tools.metadata.ValueTool;
import org.edu_sharing.repository.server.tools.*;
import org.edu_sharing.repository.server.tools.cache.UserCache;
import org.edu_sharing.service.license.LicenseService;
import org.edu_sharing.service.nodeservice.NodeServiceHelper;
import org.edu_sharing.service.nodeservice.NodeServiceImpl;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AlfrescoRuntimePropertyService {

    private final Logger logger = Logger.getLogger(NodeServiceImpl.class);
    private final DateTool dateTool = new DateTool();

    private final ServiceRegistry serviceRegistry;
    private final PersonService personService;
    private final NodeService nodeService;
    private final ContentService contentService;
    private final DictionaryService dictionaryService;

    private final ApplicationInfo appInfo;
    private final String repId;

    public AlfrescoRuntimePropertyService() {
        //TODO use spring configuration! Won't work actually because of side effects.

        appInfo = ApplicationInfoList.getHomeRepository();
        repId = appInfo.getAppId();

        ApplicationContext applicationContext = AlfAppContextGate.getApplicationContext();
        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        personService = serviceRegistry.getPersonService();
        nodeService = serviceRegistry.getNodeService();
        contentService = serviceRegistry.getContentService();
        dictionaryService = serviceRegistry.getDictionaryService();
    }

    public void addRuntimeProperties(String storeProtocol, String storeId, String nodeId, String nodeType, Map<String, Object> properties) {
        NodeRef nodeRef = new NodeRef(new StoreRef(storeProtocol, storeId), nodeId);

        //TODO BEGIN MCSAlfrescoAPIClient.getPropertiesCached

        for (Map.Entry<String, Object> entry : new ArrayList<>(properties.entrySet())) {
            String propName = entry.getKey();
            Object object = entry.getValue();

            if (!(object instanceof String
                    || object instanceof Date
                    || object instanceof Number
                    || object instanceof List
                    || object instanceof Boolean
                    || object instanceof NodeRef)) {
                properties.remove(propName);
                continue;
            }

            String value = formatData(propName, object);
            entry.setValue(value);

            if (object instanceof Date) {
                properties.put(propName + CCConstants.ISODATE_SUFFIX, ISO8601DateFormat.format((Date) object));
                properties.put(propName + CCConstants.LONG_DATE_SUFFIX, ((Date) object).getTime());
            }

            // VCard
            HashMap<String, Object> vcard = VCardConverter.getVCardHashMap(nodeType, propName, value);
            if (vcard != null && !vcard.isEmpty()) {
                properties.putAll(vcard);
            }
        }

        // add formated replicationsourcetimestamp
        if (properties.containsKey(CCConstants.CCM_PROP_IO_REPLICATIONSOURCETIMESTAMP)) {
            String value = (String) properties.get(CCConstants.CCM_PROP_IO_REPLICATIONSOURCETIMESTAMP);
            if (!value.equals("") && !value.trim().equals("0000-00-00T00:00:00Z")) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss");
                    Date date = sdf.parse((String) value);
                    DateFormat df = ServerConstants.DATEFORMAT_WITHOUT_TIME;
                    String formatedDate = df.format(date);
                    properties.put(CCConstants.CCM_PROP_IO_REPLICATIONSOURCETIMESTAMPFORMATED, formatedDate);

                } catch (ParseException e) {
                    logger.error(value + " was no valid date of format " + "yyyy-MM-dd'T'HH:mm:sss");
                }
            }
        }

        if (properties.containsKey(CCConstants.CM_PROP_VERSIONABLELABEL)) {
            properties.put(CCConstants.CM_ASPECT_VERSIONABLE, "true");
        }

        // MimeType
        // we run as system because the current user may not has enough permissions to access content
        properties.put(CCConstants.ALFRESCO_MIMETYPE, getAlfrescoMimetype(nodeRef));


        // Preview
        if (nodeType.equals(CCConstants.CCM_TYPE_IO)) {
            //TODO
            //List<NodeRef> usages = this.getChildrenByAssociationNodeIds(nodeRef.getStoreRef(),nodeRef.getId(), CCConstants.CCM_ASSOC_USAGEASPECT_USAGES);
            //if (usages != null) {
            //    properties.put(CCConstants.VIRT_PROP_USAGECOUNT, "" + usages.size());
            //}
            //List<NodeRef> childs = this.getChildrenByAssociationNodeIds(nodeRef.getStoreRef(),nodeRef.getId(), CCConstants.CCM_ASSOC_CHILDIO);
            //if (childs != null) {
            //    properties.put(CCConstants.VIRT_PROP_CHILDOBJECTCOUNT, "" + childs.size());
            //}
            //List<NodeRef> comments = this.getChildrenByAssociationNodeIds(nodeRef.getStoreRef(),nodeRef.getId(), CCConstants.CCM_ASSOC_COMMENT);
            //if (comments != null) {
            //    properties.put(CCConstants.VIRT_PROP_COMMENTCOUNT,comments.size());
            //}

            // add permalink
            String version = (String) properties.get(CCConstants.LOM_PROP_LIFECYCLE_VERSION);
            if (version == null) {
                version = (String) properties.get(CCConstants.CM_PROP_VERSIONABLELABEL);
            }

            String permaLink = URLTool.getNgComponentsUrl() + "render/" + nodeRef.getId();
            permaLink = (version != null) ? permaLink + "/" + version : permaLink;
            properties.put(CCConstants.VIRT_PROP_PERMALINK, permaLink);
        }

        if (nodeType.equals(CCConstants.CCM_TYPE_MAP)) {
            String iconUrl = URLTool.getBrowserURL(nodeRef, CCConstants.CCM_PROP_MAP_ICON);
            if (iconUrl != null) {
                properties.put(CCConstants.CCM_PROP_MAP_ICON, iconUrl);
            }
        }

        //TODO nodeService!
        if (nodeType.equals(CCConstants.CCM_TYPE_IO) || nodeType.equals(CCConstants.CCM_TYPE_MAP)) {
            ChildAssociationRef parentNodeRef = nodeService.getPrimaryParent(nodeRef);
            properties.put(CCConstants.VIRT_PROP_PRIMARYPARENT_NODEID, parentNodeRef.getParentRef().getId());
        }

        properties.put(CCConstants.NODETYPE, nodeType);
        properties.put(CCConstants.NODEID, nodeRef.getId());

        // Repository Id is in API Mode always the home repository
        properties.put(CCConstants.REPOSITORY_ID, repId);
        properties.put(CCConstants.REPOSITORY_CAPTION, appInfo.getAppCaption());

        addCreatorInfos(properties);
        addModiefierInfos(properties);

        //TODO END MCSAlfrescoAPIClient.getPropertiesCached

        // TODO NodeServiceInterceptor.throwIfWrongScope(nodeService, nodeRef);


        // checking if it is form type content
        boolean isSubOfContent = dictionaryService.isSubClass(QName.createQName(nodeType), QName.createQName(CCConstants.CM_TYPE_CONTENT));

        String contentUrl = URLTool.getNgRenderNodeUrl(nodeRef.getId(), null);
        contentUrl = URLTool.addOAuthAccessToken(contentUrl);
        properties.put(CCConstants.CONTENTURL, contentUrl);

        // external URL
        if (isSubOfContent) {

            //TODO Serializable commonLicenseKey = (String)properties.get(CCConstants.CCM_PROP_IO_COMMONLICENSE_KEY);
            //TODO boolean downloadAllowed = downloadAllowed(nodeRef.getId(),commonLicenseKey,(String)properties.get(CCConstants.CCM_PROP_EDITOR_TYPE));
            boolean downloadAllowed = true;

            if ((properties.get(CCConstants.ALFRESCO_MIMETYPE) != null || properties.get(CCConstants.LOM_PROP_TECHNICAL_LOCATION) != null) && downloadAllowed) {
                properties.put(CCConstants.DOWNLOADURL, URLTool.getDownloadServletUrl(nodeRef.getId(), null, true));
            }

            String commonLicenseKey = (String) properties.get(CCConstants.CCM_PROP_IO_COMMONLICENSE_KEY);
            if (commonLicenseKey != null) {
                if (Context.getCurrentInstance() != null) {
                    String ccversion = (String) properties.get(CCConstants.CCM_PROP_IO_COMMONLICENSE_CC_VERSION);
                    String licenseUrl = new LicenseService().getLicenseUrl(commonLicenseKey, Context.getCurrentInstance().getLocale(), ccversion);
                    if (licenseUrl != null) {
                        properties.put(CCConstants.VIRT_PROP_LICENSE_URL, licenseUrl);
                    }
                }
                String licenseIcon = new LicenseService().getIconUrl(commonLicenseKey);
                if (licenseIcon != null) properties.put(CCConstants.VIRT_PROP_LICENSE_ICON, licenseIcon);

            }
        }


        /** Add the image dimensions to the common CCM fields */
        if (nodeType.equals(CCConstants.CCM_TYPE_IO)) {
            if (properties.containsKey(CCConstants.EXIF_PROP_PIXELXDIMENSION)) {
                properties.put(CCConstants.CCM_PROP_IO_WIDTH, properties.get(CCConstants.EXIF_PROP_PIXELXDIMENSION));
            }
            if (properties.containsKey(CCConstants.EXIF_PROP_PIXELYDIMENSION)) {
                properties.put(CCConstants.CCM_PROP_IO_HEIGHT, properties.get(CCConstants.EXIF_PROP_PIXELYDIMENSION));
            }

            //Preview Url not longer in cache
            String renderServiceUrlPreview = URLTool.getRenderServiceURL(nodeRef.getId(), true);
            if (renderServiceUrlPreview != null) {
                properties.put(CCConstants.CM_ASSOC_THUMBNAILS, renderServiceUrlPreview);
            } else {
                properties.put(CCConstants.CM_ASSOC_THUMBNAILS, NodeServiceHelper.getPreview(nodeRef, (HashMap<String, Object>) properties).getUrl());
            }
        }

        /**
         * run over all properties and format the date props with with current
         * user locale
         */
        if (nodeType.equals(CCConstants.CCM_TYPE_IO) || nodeType.equals(CCConstants.CCM_TYPE_MAP)) {
            String mdsId = CCConstants.metadatasetdefault_id;
            if (properties.containsKey(CCConstants.CM_PROP_METADATASET_EDU_METADATASET)) {
                mdsId = (String) properties.get(CCConstants.CM_PROP_METADATASET_EDU_METADATASET);
            }
            try {
                MetadataSet mds = MetadataHelper.getMetadataset(ApplicationInfoList.getHomeRepository(), mdsId);
                for (Map.Entry<String, Object> entry : new ArrayList<>(properties.entrySet())) {
                    PropertyDefinition propDef = dictionaryService.getProperty(QName.createQName(entry.getKey()));

                    DataTypeDefinition dtd = null;
                    if (propDef != null)
                        dtd = propDef.getDataType();
                    if (Context.getCurrentInstance() != null && dtd != null
                            && (dtd.getName().equals(DataTypeDefinition.DATE) || dtd.getName().equals(DataTypeDefinition.DATETIME))) {
                        String[] values = ValueTool.getMultivalue((String) entry.getValue());
                        String[] formattedValues = new String[values.length];
                        int i = 0;
                        for (String value : values) {
                            formattedValues[i++] = new DateTool().formatDate(new Long(value));
                        }
                        // put time as long i.e. for sorting or formating in gui
                        // this is basically just a copy of the real value for backward compatibility
                        properties.put(entry.getKey() + CCConstants.LONG_DATE_SUFFIX, entry.getValue());
                        // put formated
                        properties.put(entry.getKey(), ValueTool.toMultivalue(formattedValues));
                    }
                    try {
                        MetadataWidget widget = mds.findWidget(CCConstants.getValidLocalName(entry.getKey()));
                        Map<String, MetadataKey> map = widget.getValuesAsMap();
                        if (!map.isEmpty()) {
                            String[] keys = ValueTool.getMultivalue((String) entry.getValue());
                            String[] values = new String[keys.length];
                            for (int i = 0; i < keys.length; i++) {
                                values[i] = map.containsKey(keys[i]) ? map.get(keys[i]).getCaption() : keys[i];
                            }
                            properties.put(entry.getKey() + CCConstants.DISPLAYNAME_SUFFIX, StringUtils.join(values, CCConstants.MULTIVALUE_SEPARATOR));
                        }
                    } catch (Throwable t) {
                        //logger.warn(t.getMessage());
                    }
                }
            }catch (Exception ex){
                logger.error(ex.getMessage());
            }
        }

        // Preview this was done already in getPropertiesCached (the heavy
        // performance must be done in getPropertiesCached)
        // but we need to set the ticket when it's an alfresco generated preview
        // logger.info("setting Preview");
        if (nodeType.equals(CCConstants.CCM_TYPE_IO)) {
            String renderServiceUrlPreview = URLTool.getRenderServiceURL(nodeRef.getId(), true);
            if (renderServiceUrlPreview == null) {
                // prefer alfresco thumbnail
                String thumbnailUrl = (String) properties.get(CCConstants.CM_ASSOC_THUMBNAILS);
                if (thumbnailUrl != null && !thumbnailUrl.trim().equals("")) {
                    // prevent Browser Caching:
                    thumbnailUrl = UrlTool.setParam(thumbnailUrl, "dontcache", Long.toString(System.currentTimeMillis()));
                    properties.put(CCConstants.CM_ASSOC_THUMBNAILS, thumbnailUrl);
                }
            }
            /**
             * for Collections Ref Objects return original nodeid
             * @TODO its a association so it could be multivalue
             */
            //if(Arrays.asList(getAspects(nodeRef)).contains(CCConstants.CCM_ASPECT_COLLECTION_IO_REFERENCE)){
            //    AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {

            //        @Override
            //        public Void doWork() throws Exception{
            //            try {
            //                List<NodeRef> assocNode = getAssociationNodeIds(nodeRef, CCConstants.CM_ASSOC_ORIGINAL);
            //                if(assocNode.size() > 0){
            //                    String originalNodeId = assocNode.get(0).getId();
            //                    propsCopy.put(CCConstants.CM_ASSOC_ORIGINAL, originalNodeId);
            //                }
            //            } catch (Throwable t) {
            //                throw new Exception(t);
            //            }
            //            return null;
            //        }
            //    });
            //}
        }

        //TODO setting ticket for map icon url
        //if (nodeType.equals(CCConstants.CCM_TYPE_MAP)) {
        //    String iconUrl = (String) properties.get(CCConstants.CCM_PROP_MAP_ICON);
        //    if (iconUrl != null) {
        //        String paramToken = (iconUrl.contains("?")) ? "&" : "?";
        //        iconUrl = iconUrl + paramToken + "ticket=" + authenticationInfo.get(CCConstants.AUTH_TICKET);
        //        // prevent Browser Caching:
        //        iconUrl = UrlTool.setParam(iconUrl, "dontcache", new Long(System.currentTimeMillis()).toString());

        //        properties.put(CCConstants.CCM_PROP_MAP_ICON, iconUrl);
        //    }
        //}

        if (nodeType.equals(CCConstants.CCM_TYPE_MAP)) {

            // TODO Information if write is allowed (important for DragDropComponent)
            // and drawRelations
            //HashMap<String, Boolean> permissions = hasAllPermissions(nodeRef.getId(), new String[] { PermissionService.WRITE, PermissionService.ADD_CHILDREN });
            //for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            //    properties.put(entry.getKey(), entry.getValue().toString());
            //}

            // for the system folder: these are created in german and english
            // language.
            // we can not cache it, cause cache mechanism is not able to handle
            // multiple lang props
            Object mlfolderTitleObject = nodeService.getProperty(nodeRef, QName.createQName(CCConstants.CM_PROP_C_TITLE));
            String mlFolderTitle = formatData(CCConstants.CM_PROP_C_TITLE, mlfolderTitleObject);
            properties.put(CCConstants.CM_PROP_C_TITLE, mlFolderTitle);
        }
    }

    public String formatData(String key, Object value) {
        String returnValue = null;
        if (key != null && value != null) {
            boolean processed = false;
            // value is date than put a String with a long value so that it can
            // be formated with userInfo later
            if (value instanceof List) {
                List<Object> list = (List<Object>) value;
                if (list.size() > 0) {
                    if (list.get(0) instanceof Date) {
                        returnValue = ValueTool.toMultivalue(
                                list.stream().
                                        map((date) -> new Long(((Date) date).getTime()).toString()).
                                        collect(Collectors.toList()).toArray(new String[0])
                        );
                        processed = true;
                    }
                }
            }
            if (value instanceof Date) {

                Date date = (Date) value;
                returnValue = new Long(date.getTime()).toString();
                processed = true;
            }
            if (!processed) {
                returnValue = getValue(value);
            }
            // !(value instanceof MLText || value instanceof List): prevent sth.
            // like de_DE=null in gui
            if (returnValue == null && value != null && !(value instanceof MLText || value instanceof List)) {
                returnValue = value.toString();
            }
        }
        return returnValue;
    }

    protected String getValue(Object value) {
        if (value instanceof List && ((List) value).size() > 0) {
            StringBuilder result = null;
            for (Object item : (List) value) {
                if (result != null) {
                    result.append(CCConstants.MULTIVALUE_SEPARATOR);
                }

                if (item != null) {
                    if (result != null) {
                        result.append(item);
                    } else {
                        result = new StringBuilder(item.toString());
                    }
                }
            }

            if (result == null) {
                return null;
            }

            return result.toString();
        } else if (value instanceof List && ((List) value).isEmpty()) {
            // cause empty list toString returns "[]"
            return "";
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Number) {
            return value.toString();
        } else {
            return value.toString();
        }
    }

    private String getAlfrescoMimetype(NodeRef nodeRef) {
        return AuthenticationUtil.runAsSystem(() -> {
            ContentReader contentReader = contentService.getReader(nodeRef, QName.createQName(CCConstants.CM_PROP_CONTENT));
            if (contentReader != null) {
                return contentReader.getMimetype();
            }
            return null;
        });
    }

    private void addCreatorInfos(Map<String, Object> properties) {
        User user = getUser((String) properties.get(CCConstants.CM_PROP_C_CREATOR));
        properties.put(CCConstants.NODECREATOR_FIRSTNAME, user.getGivenName());
        properties.put(CCConstants.NODECREATOR_LASTNAME, user.getSurname());
        properties.put(CCConstants.NODECREATOR_EMAIL, user.getEmail());
    }

    private void addModiefierInfos(Map<String, Object> properties) {
        User user = getUser((String) properties.get(CCConstants.CM_PROP_C_CREATOR));
        properties.put(CCConstants.NODEMODIFIER_FIRSTNAME, user.getGivenName());
        properties.put(CCConstants.NODEMODIFIER_LASTNAME, user.getSurname());
        properties.put(CCConstants.NODEMODIFIER_EMAIL, user.getEmail());
    }


    private User getUser(String username) {
        User user = UserCache.get(username);
        if (user != null) {
            return user;
        }

        user = new User();
        user.setUsername(username);

        NodeRef persNoderef = null;
        try {
            persNoderef = personService.getPerson(username, false);
        } catch (NoSuchPersonException e) {
            // ie the system user
        }

        if (persNoderef != null) {
            Map<QName, Serializable> props = nodeService.getProperties(persNoderef);
            user.setEmail((String) props.get(QName.createQName(CCConstants.CM_PROP_PERSON_EMAIL)));
            user.setGivenName((String) props.get(QName.createQName(CCConstants.CM_PROP_PERSON_FIRSTNAME)));
            user.setSurname(((String) props.get(QName.createQName(CCConstants.CM_PROP_PERSON_LASTNAME))));
            user.setNodeId(persNoderef.getId());
            HashMap<String, Serializable> userProperties = new HashMap<>();
            for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
                Serializable value = entry.getValue();
                userProperties.put(entry.getKey().toString(), value);
            }
            user.setProperties(userProperties);
        }

        UserCache.put(username, user);
        return user;
    }
}
