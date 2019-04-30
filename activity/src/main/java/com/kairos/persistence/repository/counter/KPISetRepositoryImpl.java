package com.kairos.persistence.repository.counter;
/*
 *Created By Pavan on 30/4/19
 *
 */

import com.kairos.persistence.model.counter.KPISet;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import javax.inject.Inject;
import java.util.List;

import static com.kairos.constants.AppConstants.DELETED;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.lookup;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

public class KPISetRepositoryImpl implements CustomKPISetRepository{
    @Inject
    private MongoTemplate mongoTemplate;
    @Override
    public List<KPISet> findAllByCountryIdAndDeletedFalse(List<Long> orgSubTypeIds, Long countryId) {
        Criteria criteria=Criteria.where("referenceId").is(countryId).and(DELETED).is(false);
        Aggregation aggregation=Aggregation.newAggregation(
                match(criteria),
                unwind("kpiIds",true),
                lookup("orgTypeKPIEntry","kpiIds","_id","orgTypeKPIEntry"),
                match(Criteria.where("orgTypeKPIEntry.orgTypeId").in(orgSubTypeIds))
        );
        AggregationResults<KPISet> result = mongoTemplate.aggregate(aggregation, KPISet.class, KPISet.class);
        return result.getMappedResults();
    }
}
