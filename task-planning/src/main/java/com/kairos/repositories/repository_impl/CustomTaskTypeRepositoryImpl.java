package com.kairos.repositories.repository_impl;

import com.kairos.dto.activity.task_type.TaskTypeResponseDTO;
import com.kairos.dto.user.staff.client.ClientFilterDTO;
import com.kairos.persistence.model.task_demand.TaskDemand;
import com.kairos.persistence.model.task_type.TaskType;
import com.kairos.repositories.task_type.CustomTaskTypeRepository;
import com.kairos.wrapper.OrgTaskTypeAggregateResult;
import com.kairos.wrapper.TaskTypeAggregateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Created by prabjot on 16/5/17.
 */
@Repository
public class CustomTaskTypeRepositoryImpl implements CustomTaskTypeRepository {

    public static final String CITIZEN_ID = "citizenId";
    public static final String TASK_TYPE_ID = "taskTypeId";
    public static final String TASK_TYPE_IDS = "taskTypeIds";
    public static final String TASK_TYPES = "task_types";
    public static final String TITLE = "title";
    public static final String IS_ENABLED = "isEnabled";
    public static final String STATUS = "status";
    public static final String ORGANIZATION_ID = "organizationId";
    public static final String TAGS_DATA = "tags_data";
    public static final String DOLLOR_TITLE = "$title";
    public static final String DOLLOR_DESCRIPTION = "$description";
    public static final String DOLLOR_SUB_SERVICE_ID = "$subServiceId";
    public static final String DOLLOR_EXPIRES_ON = "$expiresOn";
    public static final String DOLLOR_IS_ENABLED = "$isEnabled";
    public static final String DOLLOR_ROOT_ID = "$rootId";
    public static final String DESCRIPTION = "description";
    public static final String SUB_SERVICE_ID = "subServiceId";
    public static final String EXPIRES_ON = "expiresOn";
    public static final String PARENT_TASK_TYPE_ID = "parentTaskTypeId";
    @Inject
    private MongoTemplate mongoTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<TaskTypeAggregateResult> getTaskTypesOfCitizens(List<Long> citizenIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria
                        .where(CITIZEN_ID).in(citizenIds).and("isDeleted").is(false)),
                group(CITIZEN_ID).addToSet(TASK_TYPE_ID).as(TASK_TYPE_IDS)
        );
        AggregationResults<TaskTypeAggregateResult> result = mongoTemplate.aggregate(aggregation, TaskDemand.class,TaskTypeAggregateResult.class);
        return result.getMappedResults();
    }

    @Override
    public List<OrgTaskTypeAggregateResult> getTaskTypesOfUnit(long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where("unitId").is(unitId)),
                lookup(TASK_TYPES, TASK_TYPE_ID,"_id", TASK_TYPES),
                unwind(TASK_TYPES),
                group("task_types._id").first("task_types.title").as(TITLE)
        );
        AggregationResults<OrgTaskTypeAggregateResult> result = mongoTemplate.aggregate(aggregation, TaskDemand.class,OrgTaskTypeAggregateResult.class);
        return result.getMappedResults();
    }

    @Override
    public void updateUnitTaskTypesStatus(BigInteger taskTypeId, boolean status) {
        Query query = Query.query(Criteria.where("rootId").is(taskTypeId));
        Update update = new Update();
        update.set(IS_ENABLED,status);
        mongoTemplate.updateMulti(query, update,TaskType.class);
    }


    public List<TaskTypeAggregateResult> getCitizenTaskTypesOfUnit(Long unitId, ClientFilterDTO clientFilterDTO, List<String> taskTypeIdsByServiceIds) {
        Criteria criteria = Criteria.where("unitId").is(unitId).and("isDeleted").is(false);
        Criteria c = new Criteria();
        Set taskTypesSet = new HashSet();
        List<BigInteger> taskTypes = new ArrayList();
        taskTypesSet.addAll(clientFilterDTO.getTaskTypes());
        if(clientFilterDTO.isNewDemands()){
            criteria.and(STATUS).ne(TaskDemand.Status.VISITATED);
        }

        taskTypesSet.addAll(taskTypeIdsByServiceIds);
        taskTypes.addAll(taskTypesSet);
        logger.info("taskTypes----------> {}",taskTypes);
        if(!taskTypes.isEmpty()){
          c =  c.where(TASK_TYPE_IDS).in(taskTypes);
        }
        if(!clientFilterDTO.getTimeSlots().isEmpty()){
            c.andOperator(Criteria.where("weekDayTimeSlotIds").in(clientFilterDTO.getTimeSlots()).orOperator(Criteria.where("weekEndTimeSlotIds").in(clientFilterDTO.getTimeSlots())));
        }

        Aggregation aggregation = Aggregation.newAggregation(

                match(
                        criteria
                ),
                group(CITIZEN_ID).addToSet(TASK_TYPE_ID).as(TASK_TYPE_IDS).addToSet("weekdayVisits.timeSlotId").as("weekDayTimeSlotIds").addToSet("weekendVisits.timeSlotId").as("weekEndTimeSlotIds"),
                unwind("$taskTypeIds"),
                unwind("$weekDayTimeSlotIds"),
                unwind("$weekEndTimeSlotIds"),
                match(
                        c
                )

        );
        AggregationResults<TaskTypeAggregateResult> result = mongoTemplate.aggregate(aggregation, TaskDemand.class,TaskTypeAggregateResult.class);
        return result.getMappedResults();
    }

//    @Query(value="{'isEnabled':true,'organizationId':0}")
    @Override
    public List<TaskTypeResponseDTO> getAllTaskType(){

        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(IS_ENABLED).is(true).and(ORGANIZATION_ID).is(0)),
                unwind("tags", true),
                lookup("tag","tags","_id", TAGS_DATA),
                unwind(TAGS_DATA,true),
                group("$id")
                        .first(DOLLOR_TITLE).as(TITLE)
                        .first(DOLLOR_DESCRIPTION).as(DESCRIPTION)
                        .first(DOLLOR_SUB_SERVICE_ID).as(SUB_SERVICE_ID)
                        .first(DOLLOR_EXPIRES_ON).as(EXPIRES_ON)
                        .first(DOLLOR_IS_ENABLED).as(STATUS)
                        .first(DOLLOR_ROOT_ID).as(PARENT_TASK_TYPE_ID)
                        .push(TAGS_DATA).as("tags")
        );

        AggregationResults<TaskTypeResponseDTO> result = mongoTemplate.aggregate(aggregation, TaskType.class,TaskTypeResponseDTO.class);
        return result.getMappedResults();
    }

    @Override
    public List<TaskTypeResponseDTO> getAllTaskTypeBySubServiceAndOrganizationAndIsEnabled(long subServiceId, long organizationId, boolean isEnabled){

        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(IS_ENABLED).is(true).and(ORGANIZATION_ID).is(organizationId).and(SUB_SERVICE_ID).is(subServiceId)),
                unwind("tags", true),
                lookup("tag","tags","_id", TAGS_DATA),
                unwind(TAGS_DATA,true),
                group("$id")
                        .first(DOLLOR_TITLE).as(TITLE)
                        .first(DOLLOR_DESCRIPTION).as(DESCRIPTION)
                        .first(DOLLOR_SUB_SERVICE_ID).as(SUB_SERVICE_ID)
                        .first(DOLLOR_EXPIRES_ON).as(EXPIRES_ON)
                        .first(DOLLOR_IS_ENABLED).as(STATUS)
                        .first(DOLLOR_ROOT_ID).as(PARENT_TASK_TYPE_ID)
                        .push(TAGS_DATA).as("tags")
        );

        AggregationResults<TaskTypeResponseDTO> result = mongoTemplate.aggregate(aggregation, TaskType.class,TaskTypeResponseDTO.class);
        return result.getMappedResults();
    }

    @Override
    public List<TaskTypeResponseDTO> getAllTaskTypeByTeamIdAndSubServiceAndIsEnabled(long teamId, long subServiceId, boolean isEnabled){

        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(IS_ENABLED).is(true).and("teamId").is(teamId).and(SUB_SERVICE_ID).is(subServiceId)),
                unwind("tags", true),
                lookup("tag","tags","_id", TAGS_DATA),
                unwind(TAGS_DATA,true),
                group("$id")
                        .first(DOLLOR_TITLE).as(TITLE)
                        .first(DOLLOR_DESCRIPTION).as(DESCRIPTION)
                        .first(DOLLOR_SUB_SERVICE_ID).as(SUB_SERVICE_ID)
                        .first(DOLLOR_EXPIRES_ON).as(EXPIRES_ON)
                        .first(DOLLOR_IS_ENABLED).as(STATUS)
                        .first(DOLLOR_ROOT_ID).as(PARENT_TASK_TYPE_ID)
                        .push(TAGS_DATA).as("tags")
        );

        AggregationResults<TaskTypeResponseDTO> result = mongoTemplate.aggregate(aggregation, TaskType.class,TaskTypeResponseDTO.class);
        return result.getMappedResults();
    }

    @Override
    public List<TaskTypeResponseDTO> findAllBySubServiceIdAndOrganizationId(long subServiceId, long organizationId){

        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(IS_ENABLED).is(true).and(SUB_SERVICE_ID).is(subServiceId).and(ORGANIZATION_ID).is(0)),
                unwind("tags", true),
                lookup("tag","tags","_id", TAGS_DATA),
                unwind(TAGS_DATA,true),
                group("$id")
                        .first(DOLLOR_TITLE).as(TITLE)
                        .first(DOLLOR_DESCRIPTION).as(DESCRIPTION)
                        .first(DOLLOR_SUB_SERVICE_ID).as(SUB_SERVICE_ID)
                        .first(DOLLOR_EXPIRES_ON).as(EXPIRES_ON)
                        .first(DOLLOR_IS_ENABLED).as(STATUS)
                        .first(DOLLOR_ROOT_ID).as(PARENT_TASK_TYPE_ID)
                        .push(TAGS_DATA).as("tags")
        );

        AggregationResults<TaskTypeResponseDTO> result = mongoTemplate.aggregate(aggregation, TaskType.class,TaskTypeResponseDTO.class);
        return result.getMappedResults();
    }
}
