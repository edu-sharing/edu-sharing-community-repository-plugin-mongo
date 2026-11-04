package org.edu_sharing.plugin_mongo.scope;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.edu_sharing.service.scope.ScopeChangeType;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomScopeChangeRepositoryImpl implements CustomScopeChangeRepository {

    private static final String SEQ = "seq";
    private static final String AT = "at";

    private final MongoOperations mongoOperations;

    @Override
    public MongoScopeChange append(String scopeId, Collection<String> nodeIds, ScopeChangeType type) {
        MongoScopeChange change = MongoScopeChange.builder()
                .seq(nextSeq())
                .scopeId(scopeId)
                .nodeIds(nodeIds == null ? List.of() : new ArrayList<>(nodeIds))
                .type(type)
                .at(new Date())
                .build();
        return mongoOperations.insert(change);
    }

    private long nextSeq() {
        Document counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(ScopeConstants.COUNTER_CHANGE)),
                new Update().inc(SEQ, 1L),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Document.class,
                ScopeConstants.COLLECTION_COUNTER);
        return ((Number) counter.get(SEQ)).longValue();
    }

    @Override
    public List<MongoScopeChange> findChanges(Date after, Long afterSeq, Date until, int limit) {
        Criteria criteria;
        if (after != null) {
            long seq = afterSeq != null ? afterSeq : Long.MIN_VALUE;
            criteria = new Criteria().orOperator(
                    Criteria.where(AT).gt(after),
                    new Criteria().andOperator(Criteria.where(AT).is(after), Criteria.where(SEQ).gt(seq)));
        } else if (afterSeq != null) {
            criteria = Criteria.where(SEQ).gt(afterSeq);
        } else {
            criteria = new Criteria();
        }
        if (until != null) {
            criteria = new Criteria().andOperator(criteria, Criteria.where(AT).lte(until));
        }
        Query query = Query.query(criteria)
                .with(Sort.by(Sort.Order.asc(AT), Sort.Order.asc(SEQ)))
                .limit(limit);
        return mongoOperations.find(query, MongoScopeChange.class);
    }
}
