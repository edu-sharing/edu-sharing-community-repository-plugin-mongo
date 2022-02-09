package org.edu_sharing.plugin_mongo.rating;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.*;
import org.bson.conversions.Bson;
import org.edu_sharing.alfresco.policy.GuestCagePolicy;
import org.edu_sharing.plugin_mongo.codec.NodeRefCodec;
import org.edu_sharing.repository.client.rpc.User;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.InsufficientPermissionException;
import org.edu_sharing.service.authority.AuthorityService;
import org.edu_sharing.service.authority.AuthorityServiceFactory;
import org.edu_sharing.service.nodeservice.NodeServiceHelper;
import org.edu_sharing.service.permission.PermissionService;
import org.edu_sharing.service.permission.PermissionServiceFactory;
import org.edu_sharing.service.rating.Rating;
import org.edu_sharing.service.rating.RatingBase;
import org.edu_sharing.service.rating.RatingDetails;
import org.edu_sharing.service.rating.RatingService;
import org.edu_sharing.service.toolpermission.ToolPermissionHelper;

import java.util.*;

public class RatingServiceImpl implements RatingService {


    private final AuthorityService authorityService;
    private final PermissionService permissionService;
    private final MongoDatabase database;

    public RatingServiceImpl(MongoDatabase database) {

        ClassModelBuilder<Rating> ratingClassModelBuilder = ClassModel.builder(Rating.class);
        ((PropertyModelBuilder<String>)ratingClassModelBuilder.getProperty("text")).readName(RatingConstants.REASON_KEY);
        ((PropertyModelBuilder<String>)ratingClassModelBuilder.getProperty("ref")).readName(RatingConstants.NODEID_KEY);

        ClassModelBuilder<RatingDetails> ratingDetailsClassModelBuilder = ClassModel.builder(RatingDetails.class);
        ClassModelBuilder<RatingBase.RatingData> ratingDataClassModelBuilder = ClassModel.builder(RatingDetails.RatingData.class);


        CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                .register(ratingClassModelBuilder.build(),
                        ratingDetailsClassModelBuilder.build(),
                        ratingDataClassModelBuilder.build())
                .build();

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(new NodeRefCodec()),
                CodecRegistries.fromProviders(pojoCodecProvider));

        this.database = database.withCodecRegistry(pojoCodecRegistry);
        this.authorityService = AuthorityServiceFactory.getLocalService();  // TODO can this be used on unit tests?
        this.permissionService = PermissionServiceFactory.getLocalService(); // TODO can this be used on unit tests?
    }

    /**
     * @param nodeId     ---  the uuid of the node of the related rating to be added or updated
     * @param rating     ---  the rating value
     * @param text       ---  the rating text
     */
    @Override
    public void addOrUpdateRating(String nodeId, Double rating, String text) throws Exception {
        checkPreconditions(nodeId);
        String user = AuthenticationUtil.getFullyAuthenticatedUser(); // TODO can this be used on unit tests?
        User userInfo = authorityService.getUser(user);
        String role = (String) userInfo.getProfileSettings().get(CCConstants.CM_PROP_PERSON_EDU_SCHOOL_PRIMARY_AFFILIATION);

        Document ratingObj = new Document();
        ratingObj.put(RatingConstants.NODEID_KEY, nodeId);
        ratingObj.put(RatingConstants.RATINGS_COLLECTION_KEY, rating);
        ratingObj.put(RatingConstants.REASON_KEY, text);
        ratingObj.put(RatingConstants.TIMESTAMP_KEY, new Date());
        ratingObj.put(RatingConstants.AUTHORITY_KEY, user);
        ratingObj.put(RatingConstants.AFFILIATION_KEY, role);

        createIndexes();

        ReplaceOptions options = new ReplaceOptions();
        options.upsert(true);
        MongoCollection<Document> ratingCollection = database.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        ratingCollection.replaceOne(Filters.and(Filters.eq(RatingConstants.NODEID_KEY, nodeId), Filters.eq(RatingConstants.AUTHORITY_KEY, user)), ratingObj, options);
    }

    /**
     * Deletes the rating of the user for the specified node
     * @param nodeId  ---  the uuid of the node of the related rating to be deleted
     */
    @Override
    public void deleteRating(String nodeId) throws Exception {
        checkPreconditions(nodeId);
        String user = AuthenticationUtil.getFullyAuthenticatedUser(); // TODO can this be used on unit tests?

        MongoCollection<Document> ratingCollection = database.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        ratingCollection.deleteOne(Filters.and(Filters.eq(RatingConstants.NODEID_KEY, nodeId), Filters.eq(RatingConstants.AUTHORITY_KEY, user)));
    }

    /**
     * Returns all ratings of the specified node that are newer or equals to the specified date
     * @param nodeId  ---  the uuid of the node of the related ratings
     * @param after   ---  the date which the ratings should have at least. Use null (default) to use ratings of all times and also use the cache
     * @return
     */
    @Override
    public List<Rating> getRatings(String nodeId, Date after) {
        List<Rating> ratings = new ArrayList<>();

        Bson filter = Filters.eq(RatingConstants.NODEID_KEY, nodeId);
        if(after != null){
            filter = Filters.and(filter, Filters.gte(RatingConstants.TIMESTAMP_KEY, after));
        }

        MongoCollection<Rating> ratingCollection = database.getCollection(RatingConstants.RATINGS_COLLECTION_KEY, Rating.class);
        ratingCollection.find(filter).into(ratings);
        return ratings;
    }

    /**
     * Get the accumulated ratings data
     * @param nodeId   ---  the uuid of the node of the related ratings
     * @param after    ---  the date which the ratings should have at least. Use null (default) to use ratings of all times and also use the cache
     * @return
     */
    @Override
    public RatingDetails getAccumulatedRatings(String nodeId, Date after){
        //after is optional
        Bson filter = Filters.eq(RatingConstants.NODEID_KEY, nodeId);
        if(after != null){
            filter = Filters.and(filter, Filters.gte(RatingConstants.TIMESTAMP_KEY, after));
        }

        MongoCollection<RatingDetails> ratingCollection = database.getCollection(RatingConstants.RATINGS_COLLECTION_KEY, RatingDetails.class);
        RatingDetails ratingDetails = ratingCollection.aggregate(Arrays.asList(
                Aggregates.match(filter),

                Aggregates.group("affiliation",
                        Accumulators.sum("count", 1),
                        Accumulators.sum("sum", "$rating")),

                Projections.computed("doc", Projections.fields(
                        Projections.computed("k",  new Document("$ifNull",  new Document("$_id", "null"))),
                        Projections.computed("v", Projections.fields(
                                Projections.computed("count", "$count"),
                                Projections.computed("sum", "$sum"))))),

                Aggregates.group(null,
                        Accumulators.sum("count", "$doc.v.count"),
                        Accumulators.sum("sum", "$doc.v.sum"),
                        Accumulators.push("affiliation", "$doc")),

                Projections.fields(
                        Projections.computed("global", Projections.fields(
                                Projections.computed("count", "$count"),
                                Projections.computed("sum", "$sum"))),
                        Projections.computed("affiliation", "$affiliation"))))
                .first();

        return ratingDetails;
    }

    /*
    [
        {
            $match: {
                node : "1"
            }
        }, {
            $group: {
                _id: "$affiliation",
                count : {
                    $sum: 1
                },
                sum : {
                    $sum: "$rating"
                }
            }
        }, {
            $project: {
                doc: {
                    k: {
                        $ifNull : [ "$_id", "null" ]
                    },
                    v: {
                        count : "$count",
                        sum : "$sum"
                    }
                }
            }
        }, {
            $group: {
                _id: null,
                count: {
                    $sum: "$doc.v.count"
                },
                sum:{
                    $sum: "$doc.v.sum"
                },
                affiliation: {
                    $push: "$doc"
                }
            }
        }, {
            $project: {
                global: {
                    count: "$count",
                    sum: "$sum"
                },
                affiliation : {
                    $arrayToObject: "$affiliation"
                }
            }
        }
    ]
    */


    public void changeUserData(String oldAuthority, String newAuthority) {
        // TODO do we need to update the timestamp as well? - No
        // TODO permission check? - No
        MongoCollection<Document> ratingCollection = database.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        ratingCollection.updateMany(Filters.eq(RatingConstants.AUTHORITY_KEY, oldAuthority),Updates.addToSet(RatingConstants.AUTHORITY_KEY, newAuthority));
    }

    private void createIndexes() {
        MongoCollection<Document> ratingCollection = database.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        ratingCollection.createIndex(Indexes.descending(RatingConstants.TIMESTAMP_KEY));
        ratingCollection.createIndex(Indexes.ascending(RatingConstants.NODEID_KEY));
        ratingCollection.createIndex(Indexes.ascending(RatingConstants.AUTHORITY_KEY));
        ratingCollection.createIndex(Indexes.ascending(RatingConstants.NODEID_KEY, RatingConstants.AUTHORITY_KEY));
    }


    private void checkPreconditions(String nodeId) throws Exception {
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
