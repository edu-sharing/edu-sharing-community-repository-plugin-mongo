package org.edu_sharing.plugin_mongo.rating;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.bson.Document;
import org.edu_sharing.plugin_mongo.util.AbstractMongoDbContainerTest;
import org.edu_sharing.repository.client.rpc.User;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.service.authority.AuthorityService;
import org.edu_sharing.service.nodeservice.NodeServiceHelper;
import org.edu_sharing.service.permission.PermissionService;
import org.edu_sharing.service.rating.Rating;
import org.edu_sharing.service.toolpermission.ToolPermissionHelper;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;


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
                createRatingObject("1", "Schmidt", "student", "missing some content", 3d, new Date()),
                createRatingObject("1", "Schulz", "student", "the content isn't visible to me", 1d, new Date()),
                createRatingObject("2", "Schiller", "", "bad poetry", 2d, new Date()),
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
        String affiliation = "teacher";
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

        Assertions.assertEquals(beforeCount-1, afterCount);
        Assertions.assertNull(result);
    }

    @Test
    @Disabled
    void getRatingsWithoutDate() {
        // given
        String nodeId = "1";

        MongoCollection<Document> collection = db.getCollection(RatingConstants.RATINGS_COLLECTION_KEY);
        long beforeCount = collection.countDocuments(Filters.eq(RatingConstants.NODEID_KEY, nodeId));


        // when
        List<Rating> ratings = underTest.getRatings(nodeId, null);

        // then
        List<Document> results = new ArrayList<>();
        collection.find( Filters.eq(RatingConstants.NODEID_KEY, nodeId)).into(results);

        Assertions.assertEquals(results.size(), ratings.size());
        //Assertions.assertArrayEquals();
        //TODO assertions
    }

    @Test
    @Disabled
    void getAccumulatedRatings() {
    }

    @Test
    @Disabled
    void changeUserData() {
    }
}