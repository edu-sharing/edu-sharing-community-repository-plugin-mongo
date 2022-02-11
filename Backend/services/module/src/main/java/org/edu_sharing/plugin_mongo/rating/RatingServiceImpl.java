package org.edu_sharing.plugin_mongo.rating;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.*;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;
import org.edu_sharing.plugin_mongo.codec.NodeRefCodec;
import org.edu_sharing.service.rating.Rating;
import org.edu_sharing.service.rating.RatingBase;
import org.edu_sharing.service.rating.RatingDetails;
import org.edu_sharing.service.rating.RatingService;

import java.util.*;

public class RatingServiceImpl implements RatingService {


    private final RatingIntegrityService ratingIntegrityService;
    private final MongoDatabase database;

    public RatingServiceImpl(MongoDatabase database, RatingIntegrityService ratingIntegrityService) {

        ClassModelBuilder<Rating> ratingClassModelBuilder = ClassModel.builder(Rating.class);
        ((PropertyModelBuilder<String>)ratingClassModelBuilder.getProperty("text")).writeName(RatingConstants.REASON_KEY);
        ((PropertyModelBuilder<String>)ratingClassModelBuilder.getProperty("ref")).writeName(RatingConstants.NODEID_KEY);

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
        this.ratingIntegrityService = ratingIntegrityService;
    }

    /**
     * @param nodeId --- the uuid of the node of the related rating to be added or updated
     * @param rating --- the rating value
     * @param text   --- the rating text
     */
    @Override
    public void addOrUpdateRating(String nodeId, Double rating, String text) throws Exception {
        ratingIntegrityService.checkPermissions(nodeId);

        String authority = ratingIntegrityService.getAuthority();
        String affiliation = ratingIntegrityService.getAffiliation();

        Document ratingObj = new Document();
        ratingObj.put(RatingConstants.NODEID_KEY, nodeId);
        ratingObj.put(RatingConstants.RATING_KEY, rating);
        ratingObj.put(RatingConstants.REASON_KEY, text);
        ratingObj.put(RatingConstants.TIMESTAMP_KEY, new Date());
        ratingObj.put(RatingConstants.AUTHORITY_KEY, authority);
        ratingObj.put(RatingConstants.AFFILIATION_KEY, affiliation);

        createIndexes();

        ReplaceOptions options = new ReplaceOptions();
        options.upsert(true);
        MongoCollection<Document> ratingCollection = database.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        ratingCollection.replaceOne(Filters.and(Filters.eq(RatingConstants.NODEID_KEY, nodeId), Filters.eq(RatingConstants.AUTHORITY_KEY, authority)), ratingObj, options);
    }

    /**
     * Deletes the rating of the user for the specified node
     * @param nodeId --- the uuid of the node of the related rating to be deleted
     */
    @Override
    public void deleteRating(String nodeId) throws Exception {
        ratingIntegrityService.checkPermissions(nodeId);
        String authority = ratingIntegrityService.getAuthority();

        MongoCollection<Document> ratingCollection = database.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        ratingCollection.deleteOne(Filters.and(Filters.eq(RatingConstants.NODEID_KEY, nodeId), Filters.eq(RatingConstants.AUTHORITY_KEY, authority)));
    }

    /**
     * Returns all ratings of the specified node that are newer or equals to the specified date
     * @param nodeId --- the uuid of the node of the related ratings
     * @param after  --- the date which the ratings should have at least. Use null (default) to use ratings of all times and also use the cache
     * @return all ratings of the desired node
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
     * @param nodeId --- the uuid of the node of the related ratings
     * @param after  --- the date which the ratings should have at least. Use null (default) to use ratings of all times and also use the cache
     * @return An accumulated RatingDetails of the desired node
     */
    @Override
    public RatingDetails getAccumulatedRatings(String nodeId, Date after){
        //after is optional
        Bson filter = Filters.eq(RatingConstants.NODEID_KEY, nodeId);
        if(after != null){
            filter = Filters.and(filter, Filters.gte(RatingConstants.TIMESTAMP_KEY, after));
        }

        MongoCollection<RatingDetails> ratingCollection = database.getCollection(RatingConstants.RATINGS_COLLECTION_KEY, RatingDetails.class);
        List<Bson> aggregation = Arrays.asList(
                Aggregates.match(filter),

                Aggregates.group("$affiliation",
                        Accumulators.sum("count", 1),
                        Accumulators.sum("sum", "$rating")),

                Aggregates.project(Projections.computed("doc", Projections.fields(
                        Projections.computed("k",  new Document("$ifNull",  Arrays.asList("$_id", "null"))),
                        Projections.computed("v", Projections.fields(
                                Projections.computed("count", "$count"),
                                Projections.computed("sum", "$sum")))))),

                Aggregates.group(null,
                        Accumulators.sum("count", "$doc.v.count"),
                        Accumulators.sum("sum", "$doc.v.sum"),
                        Accumulators.push("affiliation", "$doc")),

                Aggregates.project(Projections.fields(
                        Projections.computed("overall", Projections.fields(
                                Projections.computed("count", "$count"),
                                Projections.computed("sum", "$sum"))),
                        Projections.computed("affiliation", new Document("$arrayToObject", "$affiliation")))));

        return ratingCollection.aggregate(aggregation).first();
    }

    /**
     * This method replaces the authority with the new one on all nodes with the specified authority
     * @param oldAuthority --- The actual authority name
     * @param newAuthority --- The new authority name
     */
    public void changeUserData(String oldAuthority, String newAuthority) {
        // TODO do we need to update the timestamp as well? - No
        // TODO permission check? - No
        MongoCollection<Document> ratingCollection = database.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        ratingCollection.updateMany(Filters.eq(RatingConstants.AUTHORITY_KEY, oldAuthority),Updates.set(RatingConstants.AUTHORITY_KEY, newAuthority));
    }

    private void createIndexes() {
        MongoCollection<Document> ratingCollection = database.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        ratingCollection.createIndex(Indexes.descending(RatingConstants.TIMESTAMP_KEY));
        ratingCollection.createIndex(Indexes.ascending(RatingConstants.NODEID_KEY));
        ratingCollection.createIndex(Indexes.ascending(RatingConstants.AUTHORITY_KEY));
        ratingCollection.createIndex(Indexes.ascending(RatingConstants.NODEID_KEY, RatingConstants.AUTHORITY_KEY));
    }
}
