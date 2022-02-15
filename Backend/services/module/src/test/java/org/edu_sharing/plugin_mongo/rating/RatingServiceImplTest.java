package org.edu_sharing.plugin_mongo.rating;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.openxml4j.util.Nullable;
import org.bson.Document;
import org.edu_sharing.plugin_mongo.util.AbstractMongoDbContainerTest;
import org.edu_sharing.service.rating.Rating;
import org.edu_sharing.service.rating.RatingBase;
import org.edu_sharing.service.rating.RatingDetails;

import org.edu_sharing.service.rating.RatingHistory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@ExtendWith(MockitoExtension.class)
@Testcontainers
class RatingServiceImplTest extends AbstractMongoDbContainerTest {
    private RatingServiceImpl underTest;
    private Date now = new Date();

    @Mock
    private RatingIntegrityService ratingIntegrityService;


    @BeforeEach
    void initTestSet() {

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        collection.insertMany(Arrays.asList(
                createRatingObject("1", "Müller", "teacher", "good content", 4d, DateUtils.addDays(now, -2)),
                createRatingObject("1", "Meier", "teacher", "i'm pretty good ;)", 5d, DateUtils.addDays(now, -5)),
                createRatingObject("1", "Schmidt", "student", "well described", 5d, DateUtils.addDays(now, -1)),
                createRatingObject("1", "Schulz", "student", "the content isn't visible to me", 1d, DateUtils.addDays(now, -1)),
                createRatingObject("1", "Schmidt", null, "missing some content", 2d, DateUtils.addDays(now, -5)),

                createRatingObject("2", "Schiller", "student", "bad poetry", 3d, DateUtils.addDays(now, -5)),
                createRatingObject("2", "Bach", null, "good bead", 5d, DateUtils.addDays(now, -3))
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

        Assertions.assertEquals(beforeCount + 1, afterCount, "count");
        Assertions.assertEquals(nodeId, result.get(RatingConstants.NODEID_KEY), RatingConstants.NODEID_KEY);
        Assertions.assertEquals(rating, result.get(RatingConstants.RATING_KEY), RatingConstants.RATING_KEY);
        Assertions.assertEquals(reason, result.get(RatingConstants.REASON_KEY), RatingConstants.REASON_KEY);
        Assertions.assertEquals(affiliation, result.get(RatingConstants.AFFILIATION_KEY), RatingConstants.AFFILIATION_KEY);
        Assertions.assertEquals(authority, result.get(RatingConstants.AUTHORITY_KEY), RatingConstants.AUTHORITY_KEY);
        Assertions.assertNotNull(result.get(RatingConstants.TIMESTAMP_KEY), RatingConstants.TIMESTAMP_KEY); // TODO can we do this better?
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

        Assertions.assertEquals(beforeCount, afterCount, "count");
        Assertions.assertEquals(nodeId, result.get(RatingConstants.NODEID_KEY), RatingConstants.NODEID_KEY);
        Assertions.assertEquals(rating, result.get(RatingConstants.RATING_KEY), RatingConstants.RATING_KEY);
        Assertions.assertEquals(reason, result.get(RatingConstants.REASON_KEY), RatingConstants.REASON_KEY);
        Assertions.assertEquals(affiliation, result.get(RatingConstants.AFFILIATION_KEY), RatingConstants.AFFILIATION_KEY);
        Assertions.assertEquals(authority, result.get(RatingConstants.AUTHORITY_KEY), RatingConstants.AUTHORITY_KEY);
        Assertions.assertNotNull(result.get(RatingConstants.TIMESTAMP_KEY), RatingConstants.TIMESTAMP_KEY); // TODO can we do this better?
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

        Assertions.assertEquals(beforeCount - 1, afterCount, "count");
        Assertions.assertNull(result, "result");
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

        Assertions.assertEquals(expected.size(), ratings.size(), "size");
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.NODEID_KEY)).toArray(), ratings.stream().map(x -> x.getRef().getId()).toArray(), RatingConstants.NODEID_KEY);
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.RATING_KEY)).toArray(), ratings.stream().map(Rating::getRating).toArray(), RatingConstants.RATING_KEY);
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.REASON_KEY)).toArray(), ratings.stream().map(Rating::getText).toArray(), RatingConstants.REASON_KEY);
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.AUTHORITY_KEY)).toArray(), ratings.stream().map(Rating::getAuthority).toArray(), RatingConstants.AUTHORITY_KEY);
    }

    @Test
    void getRatingsWithDate() {
        // given
        String nodeId = "1";
        Date after = DateUtils.addDays(now, -2);

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);

        // when
        List<Rating> ratings = underTest.getRatings(nodeId, after);

        // then
        List<Document> expected = new ArrayList<>();
        collection.find(Filters.and(Filters.eq(RatingConstants.NODEID_KEY, nodeId), Filters.gte(RatingConstants.TIMESTAMP_KEY, after))).into(expected);

        Assertions.assertEquals(expected.size(), ratings.size(), "size");
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.NODEID_KEY)).toArray(), ratings.stream().map(x -> x.getRef().getId()).toArray(), RatingConstants.NODEID_KEY);
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.RATING_KEY)).toArray(), ratings.stream().map(Rating::getRating).toArray(), RatingConstants.RATING_KEY);
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.REASON_KEY)).toArray(), ratings.stream().map(Rating::getText).toArray(), RatingConstants.REASON_KEY);
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.AUTHORITY_KEY)).toArray(), ratings.stream().map(Rating::getAuthority).toArray(), RatingConstants.AUTHORITY_KEY);
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

        Assertions.assertNotNull(ratingDetails, String.format("no rating found for node %s", nodeId));
        Assertions.assertNotNull(ratingDetails.getOverall(), "overall");
        Assertions.assertNotNull(ratingDetails.getAffiliation(), "affiliation");

        Assertions.assertEquals(expected.stream().map(x -> x.getDouble(RatingConstants.RATING_KEY)).reduce(0d, Double::sum), ratingDetails.getOverall().getSum(), "overall sum");
        Assertions.assertEquals(expected.size(), ratingDetails.getOverall().getCount(), "overall count");

        Map<String, List<Document>> expectedAffiliations = expected.stream().collect(Collectors.groupingBy(x -> Optional.ofNullable(x.getString(RatingConstants.AFFILIATION_KEY)).orElse("null")));
        Map<String, RatingBase.RatingData> actualAffiliations = ratingDetails.getAffiliation();

        Assertions.assertEquals(expectedAffiliations.size(), actualAffiliations.size(), "number of affiliations");
        for (Map.Entry<String, List<Document>> expectedAffiliation : expectedAffiliations.entrySet()) {
            RatingBase.RatingData actualRatingData = actualAffiliations.get(expectedAffiliation.getKey());

            Assertions.assertNotNull(actualRatingData, String.format("affiliation for %s", expectedAffiliation.getKey()));
            Assertions.assertEquals(expectedAffiliation.getValue().stream().map(x -> x.getDouble(RatingConstants.RATING_KEY)).reduce(0d, Double::sum), actualRatingData.getSum(), String.format("affiliation count for %s", expectedAffiliation.getKey()));
            Assertions.assertEquals(expectedAffiliation.getValue().size(), actualRatingData.getCount(), String.format("affiliation sum for %s", expectedAffiliation.getKey()));
        }
    }

    @Test
    void getAccumulatedRatingsWithDate() {
        // given
        String nodeId = "1";
        Date after = DateUtils.addDays(now, -2);

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);

        // when
        RatingDetails ratingDetails = underTest.getAccumulatedRatings(nodeId, after);

        // then
        List<Document> expected = new ArrayList<>();
        collection.find(Filters.and(Filters.eq(RatingConstants.NODEID_KEY, nodeId), Filters.gte(RatingConstants.TIMESTAMP_KEY, after))).into(expected);

        Assertions.assertNotNull(ratingDetails, String.format("no rating found for node %s", nodeId));
        Assertions.assertNotNull(ratingDetails.getOverall(), "overall");
        Assertions.assertNotNull(ratingDetails.getAffiliation(), "affiliation");

        Assertions.assertEquals(expected.stream().map(x -> x.getDouble(RatingConstants.RATING_KEY)).reduce(0d, Double::sum), ratingDetails.getOverall().getSum(), "overall sum");
        Assertions.assertEquals(expected.size(), ratingDetails.getOverall().getCount(), "overall count");

        Map<String, List<Document>> expectedAffiliations = expected.stream().collect(Collectors.groupingBy(x -> Optional.ofNullable(x.getString(RatingConstants.AFFILIATION_KEY)).orElse("null")));
        Map<String, RatingBase.RatingData> actualAffiliations = ratingDetails.getAffiliation();

        Assertions.assertEquals(expectedAffiliations.size(), actualAffiliations.size(), "number of affiliations");
        for (Map.Entry<String, List<Document>> expectedAffiliation : expectedAffiliations.entrySet()) {
            RatingBase.RatingData actualRatingData = actualAffiliations.get(expectedAffiliation.getKey());

            Assertions.assertNotNull(actualRatingData, String.format("affiliation for %s", expectedAffiliation.getKey()));
            Assertions.assertEquals(expectedAffiliation.getValue().stream().map(x -> x.getDouble(RatingConstants.RATING_KEY)).reduce(0d, Double::sum), actualRatingData.getSum(), String.format("affiliation count for %s", expectedAffiliation.getKey()));
            Assertions.assertEquals(expectedAffiliation.getValue().size(), actualRatingData.getCount(), String.format("affiliation sum for %s", expectedAffiliation.getKey()));
        }
    }

    @Test
    void changeUserData() {
        // given
        String oldAuthority = "Schmidt";
        String newAuthority = "Hummels";
        List<Document> expected = new ArrayList<>();

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        collection.find(Filters.eq(RatingConstants.AUTHORITY_KEY, oldAuthority)).into(expected);

        // when
        underTest.changeUserData(oldAuthority, newAuthority);

        // then
        List<Document> actual = new ArrayList<>();
        collection.find(Filters.eq(RatingConstants.AUTHORITY_KEY, newAuthority)).into(actual);

        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.NODEID_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.NODEID_KEY)).toArray(), RatingConstants.NODEID_KEY);
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.RATING_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.RATING_KEY)).toArray(), RatingConstants.RATING_KEY);
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.REASON_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.REASON_KEY)).toArray(), RatingConstants.REASON_KEY);
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.TIMESTAMP_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.TIMESTAMP_KEY)).toArray(), RatingConstants.TIMESTAMP_KEY);
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.ID_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.ID_KEY)).toArray(), RatingConstants.ID_KEY);
        Assertions.assertArrayEquals(expected.stream().map(x -> x.get(RatingConstants.AFFILIATION_KEY)).toArray(), actual.stream().map(x -> x.get(RatingConstants.AFFILIATION_KEY)).toArray(), RatingConstants.AFFILIATION_KEY);
    }

    @Test
    void getAlteredNodeIds() {
        // given
        Date after = DateUtils.addDays(now, -2);

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);

        // when
        List<String> actual = underTest.getAlteredNodeIds(after);

        // then
        List<String> expected = new ArrayList<>();
        collection.distinct(RatingConstants.NODEID_KEY, Filters.gte(RatingConstants.TIMESTAMP_KEY, after), String.class)
                .into(expected);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    void getAccumulatedRatingHistoryWithoutDate() {
        // given
        String nodeId = "1";

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);

        // when
        List<RatingHistory> ratingHistories = underTest.getAccumulatedRatingHistory(nodeId, null);

        // then
        List<Document> expectedDocuments = new ArrayList<>();
        collection.find(Filters.eq(RatingConstants.NODEID_KEY, nodeId)).into(expectedDocuments);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<Map.Entry<String, List<Document>>> expectedSet = new ArrayList<>(expectedDocuments.stream().collect(Collectors.groupingBy(x -> dateFormat.format(x.getDate(RatingConstants.TIMESTAMP_KEY)))).entrySet());
        expectedSet.add(0, new AbstractMap.SimpleEntry<>(null, expectedDocuments));

        for (Map.Entry<String, List<Document>> entry : expectedSet) {
            final String key = entry.getKey();
            RatingHistory ratingHistory = ratingHistories.stream().filter(x -> Objects.equals(x.getTimestamp(), key)).findFirst().orElse(null);
            List<Document> expected = entry.getValue();

            String ratingKey = Optional.ofNullable(entry.getKey()).orElse("overall");
            Assertions.assertNotNull(ratingHistory, String.format("%s", ratingKey));
            Assertions.assertEquals(expected.stream().map(x -> x.getDouble(RatingConstants.RATING_KEY)).reduce(0d, Double::sum), ratingHistory.getOverall().getSum(), String.format("sum of %s", ratingKey));
            Assertions.assertEquals(expected.size(), ratingHistory.getOverall().getCount(), String.format("count of %s", ratingKey));

            Map<String, List<Document>> expectedAffiliations = expected.stream().collect(Collectors.groupingBy(x -> Optional.ofNullable(x.getString(RatingConstants.AFFILIATION_KEY)).orElse("null")));
            Map<String, RatingBase.RatingData> actualAffiliations = ratingHistory.getAffiliation();

            Assertions.assertEquals(expectedAffiliations.size(), actualAffiliations.size(), String.format("affiliations of %s", ratingKey));
            for (Map.Entry<String, List<Document>> expectedAffiliation : expectedAffiliations.entrySet()) {
                String affiliationKey = expectedAffiliation.getKey();
                RatingBase.RatingData actualRatingData = actualAffiliations.get(affiliationKey);

                Assertions.assertNotNull(actualRatingData, String.format("affiliation %s for %s", affiliationKey, ratingKey));
                Assertions.assertEquals(expectedAffiliation.getValue().stream().map(x -> x.getDouble(RatingConstants.RATING_KEY)).reduce(0d, Double::sum), actualRatingData.getSum(), String.format("sum of affiliation %s for %s", affiliationKey, ratingKey));
                Assertions.assertEquals(expectedAffiliation.getValue().size(), actualRatingData.getCount(), String.format("count of affiliation %s for %s", affiliationKey, ratingKey));
            }
        }
    }

    @Test
    void getAccumulatedRatingHistoryWithDate() {
        // given
        String nodeId = "1";
        Date after = DateUtils.addDays(now, -2);

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);

        // when
        List<RatingHistory> ratingHistories = underTest.getAccumulatedRatingHistory(nodeId, after);

        // then
        List<Document> expectedDocuments = new ArrayList<>();
        collection.find(Filters.and(Filters.eq(RatingConstants.NODEID_KEY, nodeId), Filters.gte(RatingConstants.TIMESTAMP_KEY, after))).into(expectedDocuments);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<Map.Entry<String, List<Document>>> expectedSet = new ArrayList<>(expectedDocuments.stream().collect(Collectors.groupingBy(x -> dateFormat.format(x.getDate(RatingConstants.TIMESTAMP_KEY)))).entrySet());
        expectedSet.add(0, new AbstractMap.SimpleEntry<>(null, expectedDocuments));

        for (Map.Entry<String, List<Document>> entry : expectedSet) {
            final String key = entry.getKey();
            RatingHistory ratingHistory = ratingHistories.stream().filter(x -> Objects.equals(x.getTimestamp(), key)).findFirst().orElse(null);
            List<Document> expected = entry.getValue();

            String ratingKey = Optional.ofNullable(entry.getKey()).orElse("overall");
            Assertions.assertNotNull(ratingHistory, String.format("RatingHistory for %s", ratingKey));
            Assertions.assertEquals(expected.stream().map(x -> x.getDouble(RatingConstants.RATING_KEY)).reduce(0d, Double::sum), ratingHistory.getOverall().getSum(), String.format("sum of %s", ratingKey));
            Assertions.assertEquals(expected.size(), ratingHistory.getOverall().getCount(), String.format("count of %s", ratingKey));

            Map<String, List<Document>> expectedAffiliations = expected.stream().collect(Collectors.groupingBy(x -> Optional.ofNullable(x.getString(RatingConstants.AFFILIATION_KEY)).orElse("null")));
            Map<String, RatingBase.RatingData> actualAffiliations = ratingHistory.getAffiliation();

            Assertions.assertEquals(expectedAffiliations.size(), actualAffiliations.size(), String.format("number of affiliations of %s", ratingKey));
            for (Map.Entry<String, List<Document>> expectedAffiliation : expectedAffiliations.entrySet()) {
                String affiliationKey = expectedAffiliation.getKey();
                RatingBase.RatingData actualRatingData = actualAffiliations.get(affiliationKey);

                Assertions.assertNotNull(actualRatingData, String.format("affiliation %s for %s", affiliationKey, ratingKey));
                Assertions.assertEquals(expectedAffiliation.getValue().stream().map(x -> x.getDouble(RatingConstants.RATING_KEY)).reduce(0d, Double::sum), actualRatingData.getSum(), String.format("sum of affiliation %s for %s", affiliationKey, ratingKey));
                Assertions.assertEquals(expectedAffiliation.getValue().size(), actualRatingData.getCount(), String.format("count of affiliation %s for %s", affiliationKey, ratingKey));
            }
        }
    }

}