package org.edu_sharing.plugin_mongo.rating;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.edu_sharing.plugin_mongo.util.AbstractMongoDbContainerTest;
import org.edu_sharing.service.rating.Rating;
import org.edu_sharing.service.rating.RatingBase;
import org.edu_sharing.service.rating.RatingDetails;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;
import java.util.stream.Collectors;


@ExtendWith(MockitoExtension.class)
@Testcontainers
class RatingServiceImplTest extends AbstractMongoDbContainerTest {
    private RatingServiceImpl underTest;

    @Mock
    private RatingIntegrityService ratingIntegrityService;


    @BeforeEach
    void initTestSet() {
        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        collection.insertMany(Arrays.asList(
                createRatingObject("1", "Müller", "teacher", "good content", 4d, new Date()),
                createRatingObject("1", "Meier", "teacher", "i'm pretty good ;)", 5d, new Date()),
                createRatingObject("1", "Schmidt", "student", "well described", 5d, new Date()),
                createRatingObject("2", "Schiller", "student", "bad poetry", 3d, new Date()),
                createRatingObject("1", "Schulz", "student", "the content isn't visible to me", 1d, new Date()),
                createRatingObject("1", "Schmidt", "", "missing some content", 2d, new Date()),
                createRatingObject("2", "Bach", "", "good bead", 5d, new Date())
        ));

        underTest = new RatingServiceImpl(db, ratingIntegrityService);
    }


    private Document createRatingObject(String node, String authority, String affiliation, String reason, Double rating, Date timeStamp) {
        Document ratingDoc = new Document();
        ratingDoc.put(RatingConstants.NODEID_KEY, node);
        ratingDoc.put(RatingConstants.AUTHORITY_KEY, authority);
        ratingDoc.put(RatingConstants.AFFILIATION_KEY, affiliation);
        ratingDoc.put(RatingConstants.REASON_KEY, reason);
        ratingDoc.put(RatingConstants.RATING_KEY, rating);
        ratingDoc.put(RatingConstants.TIMESTAMP_KEY, timeStamp);
        return ratingDoc;
    }

    @Test
    void addRating() throws Exception {

        // given
        String nodeId = "1";
        Double rating = 5d;
        String authority = "Muster";
        String affiliation = "teacher";
        String reason = "nice video about...";

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        long beforeCount = collection.countDocuments(Filters.eq(RatingConstants.NODEID_KEY, nodeId));

        Mockito.when(ratingIntegrityService.getAffiliation()).thenReturn(affiliation);
        Mockito.when(ratingIntegrityService.getAuthority()).thenReturn(authority);

        // when
        underTest.addOrUpdateRating(nodeId, rating, reason);

        // then
        long afterCount = collection.countDocuments(Filters.eq(RatingConstants.NODEID_KEY, nodeId));
        Document result = collection.find(Filters.and(
                Filters.eq(RatingConstants.NODEID_KEY, nodeId),
                Filters.eq(RatingConstants.AUTHORITY_KEY, authority))).first();

        Mockito.verify(ratingIntegrityService).checkPermissions(nodeId);

        Assertions.assertEquals(beforeCount + 1, afterCount);
        Assertions.assertEquals(nodeId, result.get(RatingConstants.NODEID_KEY));
        Assertions.assertEquals(rating, result.get(RatingConstants.RATING_KEY));
        Assertions.assertEquals(reason, result.get(RatingConstants.REASON_KEY));
        Assertions.assertEquals(affiliation, result.get(RatingConstants.AFFILIATION_KEY));
        Assertions.assertEquals(authority, result.get(RatingConstants.AUTHORITY_KEY));
        Assertions.assertNotNull(result.get(RatingConstants.TIMESTAMP_KEY)); // TODO can we do this better?
    }

    @Test
    void updateRating() throws Exception {

        // given
        String nodeId = "1";
        Double rating = 5d;
        String authority = "Müller";
        String affiliation = "teacher";
        String reason = "nice video about...";

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        long beforeCount = collection.countDocuments(Filters.eq(RatingConstants.NODEID_KEY, nodeId));

        Mockito.when(ratingIntegrityService.getAffiliation()).thenReturn(affiliation);
        Mockito.when(ratingIntegrityService.getAuthority()).thenReturn(authority);

        // when
        underTest.addOrUpdateRating(nodeId, rating, reason);

        // then
        long afterCount = collection.countDocuments(Filters.eq(RatingConstants.NODEID_KEY, nodeId));
        Document result = collection.find(Filters.and(
                Filters.eq(RatingConstants.NODEID_KEY, nodeId),
                Filters.eq(RatingConstants.AUTHORITY_KEY, authority))).first();

        Mockito.verify(ratingIntegrityService).checkPermissions(nodeId);

        Assertions.assertEquals(beforeCount, afterCount);
        Assertions.assertEquals(nodeId, result.get(RatingConstants.NODEID_KEY));
        Assertions.assertEquals(rating, result.get(RatingConstants.RATING_KEY));
        Assertions.assertEquals(reason, result.get(RatingConstants.REASON_KEY));
        Assertions.assertEquals(affiliation, result.get(RatingConstants.AFFILIATION_KEY));
        Assertions.assertEquals(authority, result.get(RatingConstants.AUTHORITY_KEY));
        Assertions.assertNotNull(result.get(RatingConstants.TIMESTAMP_KEY)); // TODO can we do this better?
    }

    @Test
    void deleteRating() throws Exception {
        // given
        String authority = "Müller";
        String nodeId = "1";

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        long beforeCount = collection.countDocuments(Filters.eq(RatingConstants.NODEID_KEY, nodeId));

        Mockito.when(ratingIntegrityService.getAuthority()).thenReturn(authority);

        // when
        underTest.deleteRating(nodeId);

        // then
        long afterCount = collection.countDocuments(Filters.eq(RatingConstants.NODEID_KEY, nodeId));
        Document result = collection.find(Filters.and(
                Filters.eq(RatingConstants.NODEID_KEY, nodeId),
                Filters.eq(RatingConstants.AUTHORITY_KEY, authority))).first();

        Mockito.verify(ratingIntegrityService).checkPermissions(nodeId);

        Assertions.assertEquals(beforeCount - 1, afterCount);
        Assertions.assertNull(result);
    }

    @Test
    void getRatingsWithoutDate() {
        // given
        String nodeId = "1";

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);

        // when
        List<Rating> ratings = underTest.getRatings(nodeId, null);

        // then
        List<Document> expected = new ArrayList<>();
        collection.find(Filters.eq(RatingConstants.NODEID_KEY, nodeId)).into(expected);

        Assertions.assertEquals(expected.size(), ratings.size());
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.NODEID_KEY)).toArray(), ratings.stream().map(x -> x.getRef().getId()).toArray());
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.RATING_KEY)).toArray(), ratings.stream().map(Rating::getRating).toArray());
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.REASON_KEY)).toArray(), ratings.stream().map(Rating::getText).toArray());
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.AUTHORITY_KEY)).toArray(), ratings.stream().map(Rating::getAuthority).toArray());
    }

    @Test
    void getAccumulatedRatingsWithoutDate() {
        // given
        String nodeId = "1";
        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);

        // when
        RatingDetails ratingDetails = underTest.getAccumulatedRatings(nodeId, null);

        // then
        List<Document> expected = new ArrayList<>();
        collection.find(Filters.eq(RatingConstants.NODEID_KEY, nodeId)).into(expected);

        Assertions.assertNotNull(ratingDetails);
        Assertions.assertNotNull(ratingDetails.getOverall());
        Assertions.assertNotNull(ratingDetails.getAffiliation());

        Assertions.assertEquals( expected.stream().map(x->x.getDouble(RatingConstants.RATING_KEY)).reduce(0d, Double::sum),ratingDetails.getOverall().getSum());
        Assertions.assertEquals( expected.size(), ratingDetails.getOverall().getCount());

        Map<String, List<Document>> expectedAffiliations = expected.stream().collect(Collectors.groupingBy(x -> x.getString(RatingConstants.AFFILIATION_KEY)));
        Map<String, RatingBase.RatingData>  actualAffiliations = ratingDetails.getAffiliation();

        Assertions.assertEquals(expectedAffiliations.size(),actualAffiliations.size());
        for (Map.Entry<String, List<Document>> expectedAffiliation : expectedAffiliations.entrySet()) {
            RatingBase.RatingData actualRatingData = actualAffiliations.get(expectedAffiliation.getKey());

            Assertions.assertNotNull(actualRatingData);
            Assertions.assertEquals( expectedAffiliation.getValue().stream().map(x->x.getDouble(RatingConstants.RATING_KEY)).reduce(0d, Double::sum), actualRatingData.getSum());
            Assertions.assertEquals( expectedAffiliation.getValue().size(), actualRatingData.getCount());
        }
    }

    @Test
    void changeUserData() {
        // given
        String oldAuthority = "Schmidt";
        String newAuthority = "Hummels";
        List<Document> expected = new ArrayList<>();

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        collection.find(Filters.eq(RatingConstants.AUTHORITY_KEY, oldAuthority))
                .into(expected);

        // when
        underTest.changeUserData(oldAuthority, newAuthority);

        // then
        List<Document> actual = new ArrayList<>();
        collection.find(Filters.eq(RatingConstants.AUTHORITY_KEY, newAuthority))
                .into(actual);


        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.NODEID_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.NODEID_KEY)).toArray());
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.RATING_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.RATING_KEY)).toArray());
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.REASON_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.REASON_KEY)).toArray());
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.TIMESTAMP_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.TIMESTAMP_KEY)).toArray());
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.ID_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.ID_KEY)).toArray());
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.AFFILIATION_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.AFFILIATION_KEY)).toArray());

    }
}