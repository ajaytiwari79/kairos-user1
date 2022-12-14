package com.kairos.persistence.repository.open_shift;
/*
 *Created By Pavan on 17/8/18
 *
 */

import com.kairos.persistence.model.open_shift.OpenShift;
import com.kairos.persistence.model.open_shift.OpenShiftNotification;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

public class OpenShiftNotificationMongoRepositoryImpl implements CustomOpenShiftNotificationMongoRepository {
    public static final String OPEN_SHIFTS = "openShifts";
    @Inject private MongoTemplate mongoTemplate;
    @Override
    public List<OpenShift> findValidOpenShiftsForStaff(Long staffId, Date startDate, Date endDate) {
        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.match(Criteria.where("staffId").is(staffId).and("deleted").is(false)),
                Aggregation.lookup("openShift","openShiftId","_id", OPEN_SHIFTS),
                Aggregation.unwind(OPEN_SHIFTS),
                Aggregation.match(Criteria.where("openShifts.startDate").gte(startDate).and("openShifts.endDate").lte(endDate)),
                Aggregation.replaceRoot(OPEN_SHIFTS),
                Aggregation.group("_id").addToSet("$$ROOT").as("data"),
                Aggregation.project().and("data").arrayElementAt(0),
                Aggregation.replaceRoot("data")
        );
        AggregationResults<OpenShift> result=mongoTemplate.aggregate(aggregation,OpenShiftNotification.class,OpenShift.class);
        return result.getMappedResults();
        }
}
