package com.kairos.repositories.task_type;

import com.kairos.dto.activity.task_type.TaskTypeSettingDTO;
import com.kairos.persistence.model.task_type.TaskTypeSetting;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author pradeep
 * @date - 29/6/18
 */
@Repository
public class TaskTypeSettingMongoRepositoryImpl implements CustomTaskTypeSettingMongoRepository {

    public static final String TASK_TYPE = "taskType";
    @Inject private MongoTemplate mongoTemplate;

    public List<TaskTypeSettingDTO> findByStaffIds(List<Long> staffIds){
        Aggregation aggregation = Aggregation.newAggregation(match(Criteria.where("staffId").in(staffIds).and("deleted").is(false)),
                lookup("task_types","taskTypeId","_id", TASK_TYPE),
                project("id","staffId","taskTypeId","efficiency","clientId","duration").and(TASK_TYPE).arrayElementAt(0).as(TASK_TYPE)
                );
        AggregationResults<TaskTypeSettingDTO> result = mongoTemplate.aggregate(aggregation,TaskTypeSetting.class,TaskTypeSettingDTO.class);
        return result.getMappedResults();
    };
}
