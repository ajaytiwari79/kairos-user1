package com.kairos.persistence.repository.activity;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.constants.AppConstants;
import com.kairos.dto.activity.activity.ActivityCategoryListDTO;
import com.kairos.dto.activity.activity.ActivityDTO;
import com.kairos.dto.activity.activity.OrganizationActivityDTO;
import com.kairos.dto.activity.activity.activity_tabs.ActivityPhaseSettings;
import com.kairos.dto.activity.activity.activity_tabs.ActivityWithCTAWTASettingsDTO;
import com.kairos.dto.activity.time_type.TimeTypeAndActivityIdDTO;
import com.kairos.dto.user.staff.staff_settings.StaffActivitySettingDTO;
import com.kairos.enums.*;
import com.kairos.persistence.model.activity.Activity;
import com.kairos.persistence.model.activity.ActivityWrapper;
import com.kairos.persistence.model.activity.TimeType;
import com.kairos.persistence.model.common.MongoBaseEntity;
import com.kairos.persistence.model.staff_settings.StaffActivitySetting;
import com.kairos.persistence.repository.common.CustomAggregationOperation;
import com.kairos.wrapper.activity.ActivityTagDTO;
import com.kairos.wrapper.activity.ActivityTimeTypeWrapper;
import com.kairos.wrapper.activity.ActivityWithCompositeDTO;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.AppConstants.ACTIVITY_PRIORITY_ID;
import static com.kairos.constants.AppConstants.*;
import static com.kairos.enums.TimeTypeEnum.PAID_BREAK;
import static com.kairos.enums.TimeTypeEnum.UNPAID_BREAK;
import static com.kairos.enums.TimeTypeEnum.*;
import static com.kairos.enums.TimeTypes.WORKING_TYPE;
import static com.kairos.persistence.repository.activity.ActivityConstants.STAFF;
import static com.kairos.persistence.repository.activity.ActivityConstants.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


public class ActivityMongoRepositoryImpl implements CustomActivityMongoRepository {

    public static final String ACTIVITY_RULES_ACTIVITY_TAB = "activity.activityRulesSettings";
    public static final String ACTIVITY_INDIVIDUAL_POINTS_ACTIVITY_TAB = "activity.activityIndividualPointsSettings";
    public static final String UNIT_ID = "unitId";
    public static final String TIME_TYPE = "time_Type";
    public static final String DELETED = "deleted";
    public static final String BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID = "activityBalanceSettings.timeTypeId";
    public static final String TIME_TYPE_INFO = "timeTypeInfo";
    public static final String IS_PARENT_ACTIVITY = "isParentActivity";
    public static final String IS_CHILD_ACTIVITY = "isChildActivity";
    public static final String ORGANIZATION_TYPES = "organizationTypes";
    public static final String ORGANIZATION_SUB_TYPES = "organizationSubTypes";
    public static final String STATE = "state";
    public static final String TAGS_DATA = "tags_data";
    public static final String DESCRIPTION = "description";
    public static final String PARENT_ID = "parentId";
    public static final String GENERAL_ACTIVITY_TAB = "activityGeneralSettings";
    public static final String TIME_TYPE1 = "timeType";
    public static final String RULES_ACTIVITY_TAB = "activityRulesSettings";
    public static final String TIME_TYPE_ALLOW_CHILD_ACTIVITIES = "timeType.allowChildActivities";
    public static final String TIME_TYPE_SICKNESS_SETTING="timeType.sicknessSettingValid";
    public static final String ALLOW_CHILD_ACTIVITIES = "allowChildActivities";
    public static final String SICKNESS_SETTING = "sicknessSettingValid";
    public static final String CHILD_ACTIVITY_IDS = "childActivityIds";
    public static final String APPLICABLE_FOR_CHILD_ACTIVITIES = "applicableForChildActivities";
    public static final String TIME_CALCULATION_ACTIVITY_TAB_METHOD_FOR_CALCULATING_TIME = "activityTimeCalculationSettings.methodForCalculatingTime";
    public static final String ACTIVITIES = "activities";
    public static final String CTA_AND_WTA_SETTINGS_ACTIVITY_TAB = "activityCTAAndWTASettings";
    public static final String GENERAL_ACTIVITY_TAB_CATEGORY_ID = "activityGeneralSettings.categoryId";
    public static final String TIME_TYPE_TIME_TYPES = "timeType.timeTypes";
    public static final String BALANCE_SETTINGS_ACTIVITY_TAB = "activityBalanceSettings";
    public static final String EXPERTISES = "expertises";
    public static final String SKILL_ACTIVITY_TAB = "activitySkillSettings";
    public static final String BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_INFO = "activityBalanceSettings.timeTypeInfo";
    public static final String TIME_TYPE_INFO_LABEL = "timeTypeInfo.label";
    public static final String GENERAL_ACTIVITY_TAB_START_DATE = "activityGeneralSettings.startDate";
    public static final String GENERAL_ACTIVITY_TAB_END_DATE = "activityGeneralSettings.endDate";
    public static final String ACTIVITY_ID = "activity._id";
    public static final String ACTIVITY_NAME = "activity.name";
    public static final String ACTIVITY_COUNTRY_ID = "activity.countryId";
    public static final String ACTIVITY_EXPERTISES = "activity.expertises";
    public static final String EMPLOYMENT_TYPES = "employmentTypes";
    public static final String ACTIVITY_EMPLOYMENT_TYPES = "activity.employmentTypes";
    public static final String ACTIVITY_STATE = "activity.state";
    public static final String ACTIVITY_UNIT_ID = "activity.unitId";
    public static final String ACTIVITY_PARENT_ID = "activity.parentId";
    public static final String ACTIVITY_IS_PARENT_ACTIVITY = "activity.isParentActivity";
    public static final String ACTIVITY_GENERAL_ACTIVITY_TAB = "activity.activityGeneralSettings";
    public static final String ACTIVITY_BALANCE_SETTINGS_ACTIVITY_TAB = "activity.activityBalanceSettings";
    public static final String INDIVIDUAL_POINTS_ACTIVITY_TAB = "activityIndividualPointsSettings";
    public static final String TIME_CALCULATION_ACTIVITY_TAB = "activityTimeCalculationSettings";
    public static final String ACTIVITY_TIME_CALCULATION_ACTIVITY_TAB = "activity.activityTimeCalculationSettings";
    public static final String NOTES_ACTIVITY_TAB = "activityNotesSettings";
    public static final String ACTIVITY_NOTES_ACTIVITY_TAB = "activity.activityNotesSettings";
    public static final String COMMUNICATION_ACTIVITY_TAB = "activityCommunicationSettings";
    public static final String ACTIVITY_COMMUNICATION_ACTIVITY_TAB = "activity.activityCommunicationSettings";
    public static final String BONUS_ACTIVITY_TAB = "activityBonusSettings";
    public static final String ACTIVITY_BONUS_ACTIVITY_TAB = "activity.activityBonusSettings";
    public static final String ACTIVITY_SKILL_ACTIVITY_TAB = "activity.activitySkillSettings";
    public static final String OPTA_PLANNER_SETTING_ACTIVITY_TAB = "activityOptaPlannerSetting";
    public static final String ACTIVITY_OPTA_PLANNER_SETTING_ACTIVITY_TAB = "activity.activityOptaPlannerSetting";
    public static final String LOCATION_ACTIVITY_TAB = "activityLocationSettings";
    public static final String ACTIVITY_CTA_AND_WTA_SETTINGS_ACTIVITY_TAB = "activity.activityCTAAndWTASettings";
    public static final String ACTIVITY_LOCATION_ACTIVITY_TAB = "activity.activityLocationSettings";
    public static final String PHASE_SETTINGS_ACTIVITY_TAB = "activityPhaseSettings";
    public static final String ACTIVITY_PHASE_SETTINGS_ACTIVITY_TAB = "activity.activityPhaseSettings";
    public static final String CATEGORY = "category";
    public static final String CATEGORY_ID = "categoryId";
    public static final String CATEGORY_NAME = "categoryName";
    public static final String CHILD_ACTIVITIES = "childActivities";
    public static final String COMPOSITE_TIME_TYPE_INFO = "compositeTimeTypeInfo";
    public static final String CHILD_ACTIVITIES_ID = "childActivities._id";
    public static final String PHASE_TEMPLATE_VALUES_ACTIVITY_SHIFT_STATUS_SETTINGS = "phaseTemplateValues.activityShiftStatusSettings";
    public static final String ACTIVITY_SHIFT_STATUS_SETTINGS = "activityShiftStatusSettings";
    public static final String PHASE_ID = "phaseId";
    public static final String COUNTRY_ID = "countryId";
    private static final String PARENT_ACTIVITY_ID = "parentActivityId";
    private static final String NAME = "name";
    private static final String UNDERSCORE_ID = "_id";
    private static final String COUNTRY_PARENT_ID = "countryParentId";
    private static final String ID = "id";
    private static final String DOLLAR_ID = "$id";
    public static final String BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE = "activityBalanceSettings.timeType";
    public static final String TRANSLATIONS="translations";

    public static final String CHILD_ACTIVITIES_ACTIVITY_PRIORITY_ID = "childActivities.activityPriorityId";
    public static final String CHILD_ACTIVITIES_CATEGORY_ID = "childActivities.categoryId";
    public static final String STAFF_ID = "staffId";
    public static final String ACTIVITY_IDS = "activityIds";
    public static final String ACTIVITYID = "activityId";
    @Inject
    private MongoTemplate mongoTemplate;

    public List<ActivityCategoryListDTO> findAllActivityByOrganizationGroupWithCategoryName(Long unitId, boolean deleted) {
        List<AggregationOperation> customAgregationForCompositeActivity = new ArrayList<>();
        customAgregationForCompositeActivity.add(match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(deleted).and("activityRulesSettings.eligibleForStaffingLevel").is(true)));
        customAgregationForCompositeActivity.add(lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE_INFO));
        customAgregationForCompositeActivity.add(lookup(ACTIVITY_PRIORITY, ACTIVITY_PRIORITY_ID, UNDERSCORE_ID, ACTIVITY_PRIORITY));
        customAgregationForCompositeActivity.addAll(getCustomAgregationForCompositeActivityWithCategory(true,true));
        customAgregationForCompositeActivity.add(new CustomAggregationOperation(GROUP_BY_ACTIVITY_CATEGORY));
        customAgregationForCompositeActivity.add(new CustomAggregationOperation(PROJECT_ACTIVITY_CATEGORY));
        Aggregation aggregation = Aggregation.newAggregation(customAgregationForCompositeActivity);

        AggregationResults<ActivityCategoryListDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityCategoryListDTO.class);
        return result.getMappedResults();
    }

    public List<ActivityTagDTO> findAllActivitiesByOrganizationType(List<Long> orgTypeIds, List<Long> orgSubTypeIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(DELETED).is(false).and(IS_PARENT_ACTIVITY).is(true)
                        .and(ORGANIZATION_TYPES).in(orgTypeIds).orOperator(Criteria.where(ORGANIZATION_SUB_TYPES).in(orgSubTypeIds))
                        .and(STATE).nin("DRAFT")),
                unwind(TAGS, true),
                lookup(TAG, TAGS, UNDERSCORE_ID, TAGS_DATA),
                unwind(TAGS_DATA, true),
                group(DOLLAR_ID)
                        .first(ActivityConstants.NAME).as(NAME)
                        .first(ActivityConstants.DESCRIPTION).as(DESCRIPTION)
                        .first(ActivityConstants.UNIT_ID).as(UNIT_ID)
                        .first(ActivityConstants.PARENT_ID).as(PARENT_ID)
                        .first(GENERAL_ACTIVITY_TAB).as(GENERAL_ACTIVITY_TAB)
                        .push(TAGS_DATA).as(TAGS));
        AggregationResults<ActivityTagDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityTagDTO.class);
        return result.getMappedResults();
    }

    public List<ActivityTagDTO> findAllowChildActivityByUnitIdAndDeleted(Long unitId, boolean deleted) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(deleted)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                project(CHILD_ACTIVITY_IDS)
                        .and(TIME_TYPE_ACTIVITY_CAN_BE_COPIED_FOR_ORGANIZATION_HIERARCHY).arrayElementAt(0).as(ACTIVITY_CAN_BE_COPIED_FOR_ORGANIZATION_HIERARCHY)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(ALLOW_CHILD_ACTIVITIES)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(APPLICABLE_FOR_CHILD_ACTIVITIES),
                match(Criteria.where(APPLICABLE_FOR_CHILD_ACTIVITIES).is(true)),
                lookup(ACTIVITIES, UNDERSCORE_ID, CHILD_ACTIVITY_IDS, PARENT_ACTIVITY),
                project(CHILD_ACTIVITY_IDS).and("parentActivity._id").arrayElementAt(0).as(PARENT_ACTIVITY_ID)
        );
        AggregationResults<ActivityTagDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityTagDTO.class);
        return result.getMappedResults();
    }

    public List<ActivityTagDTO> findAllActivityByUnitIdAndDeleted(Long unitId, Long countryId, List<Long> orgSubTypes) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(DELETED).is(false).orOperator(Criteria.where(UNIT_ID).is(unitId), Criteria.where(COUNTRY_ID).is(countryId).and("organizationSubTypes").in(orgSubTypes))),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                match(Criteria.where(DELETED).is(false).orOperator(Criteria.where(UNIT_ID).is(unitId),Criteria.where(COUNTRY_ID).is(countryId).and(TIME_TYPE_PART_OF_TEAM).is(true))),
                lookup(TAG, TAGS, UNDERSCORE_ID, TAGS),
                project(NAME, DESCRIPTION, UNIT_ID, RULES_ACTIVITY_TAB,STATE,COUNTRY_ID, COUNTRY_PARENT_ID, PARENT_ID, GENERAL_ACTIVITY_TAB, TAGS,TRANSLATIONS).and(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID).as(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID).and(TIME_CALCULATION_ACTIVITY_TAB).as(TIME_CALCULATION_ACTIVITY_TAB)
                        .and(TIME_CALCULATION_ACTIVITY_TAB_METHOD_FOR_CALCULATING_TIME).as(METHOD_FOR_CALCULATING_TIME)
                        .and(TIME_TYPE_ACTIVITY_CAN_BE_COPIED_FOR_ORGANIZATION_HIERARCHY).arrayElementAt(0).as(ACTIVITY_CAN_BE_COPIED_FOR_ORGANIZATION_HIERARCHY)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(ALLOW_CHILD_ACTIVITIES)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(APPLICABLE_FOR_CHILD_ACTIVITIES)
                        .and(TIME_TYPE_SICKNESS_SETTING).arrayElementAt(0).as(SICKNESS_SETTING)
        );
        AggregationResults<ActivityTagDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityTagDTO.class);
        return result.getMappedResults();
    }


    public List<ActivityTagDTO> findAllActivityByCountry(long countryId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(COUNTRY_ID).is(countryId).and(DELETED).is(false).and(IS_PARENT_ACTIVITY).is(true)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                lookup(TAG, TAGS, UNDERSCORE_ID, TAGS),
                project(NAME, STATE, DESCRIPTION, COUNTRY_ID, IS_PARENT_ACTIVITY, GENERAL_ACTIVITY_TAB,TRANSLATIONS, "tags", CHILD_ACTIVITY_IDS).and(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID).as(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID)
                        .and(TIME_CALCULATION_ACTIVITY_TAB_METHOD_FOR_CALCULATING_TIME).as(METHOD_FOR_CALCULATING_TIME)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(ALLOW_CHILD_ACTIVITIES)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(APPLICABLE_FOR_CHILD_ACTIVITIES)
                        .and(TIME_TYPE_SICKNESS_SETTING).arrayElementAt(0).as(SICKNESS_SETTING)
        );
        AggregationResults<ActivityTagDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityTagDTO.class);
        return result.getMappedResults();
    }

    public List<ActivityTagDTO> findAllowChildActivityByCountryId(long countryId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(COUNTRY_ID).is(countryId).and(DELETED).is(false).and(IS_PARENT_ACTIVITY).is(true)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                project(NAME, COUNTRY_ID, IS_PARENT_ACTIVITY, GENERAL_ACTIVITY_TAB, CHILD_ACTIVITY_IDS).and(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID).as(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(ALLOW_CHILD_ACTIVITIES)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(APPLICABLE_FOR_CHILD_ACTIVITIES)
                        .and(TIME_TYPE_SICKNESS_SETTING).arrayElementAt(0).as(SICKNESS_SETTING),
                match(Criteria.where(APPLICABLE_FOR_CHILD_ACTIVITIES).is(true))
        );
        AggregationResults<ActivityTagDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityTagDTO.class);
        return result.getMappedResults();
    }

    public ActivityWithCompositeDTO findActivityByActivityId(BigInteger activityId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNDERSCORE_ID).is(activityId).and(DELETED).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                lookup(ACTIVITIES, UNDERSCORE_ID, CHILD_ACTIVITY_IDS, PARENT_ACTIVITY),
                project(NAME, STATE, DESCRIPTION, COUNTRY_ID, IS_PARENT_ACTIVITY, GENERAL_ACTIVITY_TAB, CHILD_ACTIVITY_IDS).and(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID).as(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID)
                        .and("parentActivity._id").as(AppConstants.PARENT_ACTIVITY_ID)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(ALLOW_CHILD_ACTIVITIES)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(APPLICABLE_FOR_CHILD_ACTIVITIES)
                        .and(TIME_TYPE_SICKNESS_SETTING).arrayElementAt(0).as(SICKNESS_SETTING)
        );
        AggregationResults<ActivityWithCompositeDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityWithCompositeDTO.class);
        return isCollectionNotEmpty(result.getMappedResults()) ? result.getMappedResults().get(0) : null;
    }

    public List<ActivityWithCTAWTASettingsDTO> findAllActivityWithCtaWtaSettingByCountry(long countryId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(COUNTRY_ID).is(countryId).and(DELETED).is(false).and(IS_PARENT_ACTIVITY).is(true).and(STATE).is(ActivityStateEnum.PUBLISHED)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                project(DOLLAR_ID, NAME, DESCRIPTION, CTA_AND_WTA_SETTINGS_ACTIVITY_TAB, GENERAL_ACTIVITY_TAB_CATEGORY_ID)
                        .and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE1),
                match(Criteria.where(TIME_TYPE_TIME_TYPES).is(TimeTypes.WORKING_TYPE))
        );
        AggregationResults<ActivityWithCTAWTASettingsDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityWithCTAWTASettingsDTO.class);
        return result.getMappedResults();
    }

    public List<ActivityWithCTAWTASettingsDTO> findAllActivityWithCtaWtaSettingByUnit(long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false).and(IS_PARENT_ACTIVITY).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                project(DOLLAR_ID, NAME, DESCRIPTION, CTA_AND_WTA_SETTINGS_ACTIVITY_TAB, GENERAL_ACTIVITY_TAB_CATEGORY_ID).and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE1),
                match(Criteria.where(TIME_TYPE_TIME_TYPES).is(TimeTypes.WORKING_TYPE))
        );
        AggregationResults<ActivityWithCTAWTASettingsDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityWithCTAWTASettingsDTO.class);
        return result.getMappedResults();
    }

    public List<OrganizationActivityDTO> findAllActivityOfUnitsByParentActivity(List<BigInteger> parentActivityIds, List<Long> unitIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(DELETED).is(false).and(IS_PARENT_ACTIVITY).is(false).and(UNIT_ID).in(unitIds).and(PARENT_ID).in(parentActivityIds)),
                project(DOLLAR_ID, UNIT_ID, PARENT_ID)
        );
        AggregationResults<OrganizationActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, OrganizationActivityDTO.class);
        return result.getMappedResults();
    }


    public List<ActivityTagDTO> findAllActivityByParentOrganization(long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false)),
                project(NAME, GENERAL_ACTIVITY_TAB, BALANCE_SETTINGS_ACTIVITY_TAB));

        AggregationResults<ActivityTagDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityTagDTO.class);
        return result.getMappedResults();

    }

    public List<ActivityDTO> getAllActivityWithTimeType(List<BigInteger> activityIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                //"unitId").is(unitId).and(
                match(Criteria.where(DELETED).is(false).and(UNDERSCORE_ID).in(activityIds)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE_INFO)
                , project(UNIT_ID)
                        .andInclude(DELETED)
                        .andInclude(NAME)
                        .andInclude(EXPERTISES)
                        .andInclude(SKILL_ACTIVITY_TAB)
                        .and(TIME_TYPE_INFO).arrayElementAt(0).as(TIME_TYPE1));
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }

    public List<ActivityDTO> findAllActivityByUnitId(Long unitId) {
        List<AggregationOperation> customAgregationForCompositeActivity = new ArrayList<>();
        customAgregationForCompositeActivity.add(match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false)));
        customAgregationForCompositeActivity.add(lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE_INFO));
        customAgregationForCompositeActivity.addAll(getCustomAgregationForCompositeActivityWithCategory(false,false));
        customAgregationForCompositeActivity.add(match(Criteria.where(TIME_TYPE_INFO_PART_OF_TEAM).is(true)));
        Aggregation aggregation = Aggregation.newAggregation(customAgregationForCompositeActivity);
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }

    @Override
    public List<ActivityDTO> findAllActivitiesByTimeType(Long refId, TimeTypeEnum timeType) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(ABSENCE.equals(timeType)?COUNTRY_ID:UNIT_ID).is(refId).and(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE).is(timeType).and(DELETED).is(false)),
                sort(Sort.Direction.ASC, "createdAt"),
                project(NAME,COUNTRY_PARENT_ID,TIME_CALCULATION_ACTIVITY_TAB,EXPERTISES,GENERAL_ACTIVITY_TAB));
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }

    @Override
    public List[] findAllNonProductiveTypeActivityIdsAndAssignedStaffIds(Collection<BigInteger> activityIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(AppConstants.ID).in(activityIds).and(DELETED).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE_INFO),
                match(Criteria.where(TIME_TYPE_INFO_PART_OF_TEAM).is(false)),
                group().push(ID).as(ACTIVITY_IDS),
                lookup(STAFF_ACTIVITY_SETTING, ACTIVITY_IDS, ACTIVITY_ID, STAFF),
                unwind(STAFF,true),
                group(ACTIVITY_IDS).push(STAFF_STAFF_ID).as(STAFF_IDS)
        );
        List<Map> result = mongoTemplate.aggregate(aggregation, Activity.class, Map.class).getMappedResults();
        List<BigInteger> nonProductiveTypeActivityIds = null;
        List<Long> staffIds = null;
        if(isCollectionNotEmpty(result)){
            Map<String,List> stringListMap = result.get(0);
             nonProductiveTypeActivityIds = (List<BigInteger>)ObjectMapperUtils.copyCollectionPropertiesByMapper(stringListMap.get(ID1),BigInteger.class);
            staffIds = stringListMap.get(STAFF_IDS);
        }
        return new List[]{nonProductiveTypeActivityIds,staffIds};
    }

    @Override
    public Set<BigInteger> findAllProductiveTypeActivityIdsByUnitId(Long unitId){
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE_INFO),
                match(Criteria.where(TIME_TYPE_INFO_PART_OF_TEAM).is(true)),
                project(AppConstants.ID,NAME)
        );
        return mongoTemplate.aggregate(aggregation,Activity.class,Activity.class).getMappedResults().stream().map(MongoBaseEntity::getId).collect(Collectors.toSet());
    }

    //Ignorecase

    public Activity getActivityByNameAndUnitId(Long unitId, String name) {
        Query query = new Query(Criteria.where(DELETED).is(false).and(UNIT_ID).is(unitId).and(NAME).regex(Pattern.compile("^" + name + "$", Pattern.CASE_INSENSITIVE)));
        return mongoTemplate.findOne(query, Activity.class);
    }


    public List<ActivityDTO> findAllActivitiesWithBalanceSettings(long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID,
                        BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_INFO),
                project(BALANCE_SETTINGS_ACTIVITY_TAB, NAME, EXPERTISES)
                        .and(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_INFO).arrayElementAt(0).as(TIME_TYPE1).andInclude(TIME_TYPE_INFO_LABEL)

        );
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }

    public List<ActivityDTO> findAllActivitiesWithTimeTypes(long countryId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(COUNTRY_ID).is(countryId).and(DELETED).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID,
                        BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_INFO),
                match(Criteria.where(ACTIVITY_BALANCE_SETTINGS_TIME_TYPE_INFO_TIME_TYPES).is(WORKING_TYPE)),
                project(BALANCE_SETTINGS_ACTIVITY_TAB, NAME).and(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_INFO).arrayElementAt(0).as(TIME_TYPE1).andInclude(TIME_TYPE_INFO_LABEL)

        );
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }

    public List<ActivityDTO> findAllActivitiesWithTimeTypesByUnit(Long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID,
                        BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_INFO),
                match(Criteria.where(ACTIVITY_BALANCE_SETTINGS_TIME_TYPE_INFO_TIME_TYPES).is(WORKING_TYPE)),
                project(BALANCE_SETTINGS_ACTIVITY_TAB, NAME).and(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_INFO).arrayElementAt(0).as(TIME_TYPE1).andInclude(TIME_TYPE_INFO_LABEL)

        );
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }


    public Activity findByNameExcludingCurrentInCountryAndDate(String name, BigInteger activityId, Long countryId, LocalDate startDate, LocalDate endDate) {
        Criteria criteria = Criteria.where(AppConstants.ID).ne(activityId).and(NAME).regex(Pattern.compile("^" + name + "$", Pattern.CASE_INSENSITIVE)).and(DELETED).is(false).and(COUNTRY_ID).is(countryId);
        if (endDate == null) {
            Criteria startDateCriteria = new Criteria().orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_START_DATE).gte(startDate), Criteria.where(GENERAL_ACTIVITY_TAB_START_DATE).lte(startDate));
            Criteria endDateCriteria = new Criteria().orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).exists(false), Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).gte(startDate));
            criteria.andOperator(startDateCriteria, endDateCriteria);
        } else {
            criteria.and(GENERAL_ACTIVITY_TAB_START_DATE).lte(endDate).orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).exists(false), Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).gte(startDate));
        }
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, Activity.class);
    }

    public Activity findByNameExcludingCurrentInUnitAndDate(String name, BigInteger activityId, Long unitId, LocalDate startDate, LocalDate endDate) {
        Criteria criteria = Criteria.where(AppConstants.ID).ne(activityId).and(NAME).regex(Pattern.compile("^" + name + "$", Pattern.CASE_INSENSITIVE)).and(DELETED).is(false).and(UNIT_ID).is(unitId);
        if (endDate == null) {
            Criteria startDateCriteria = new Criteria().orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_START_DATE).gte(startDate), Criteria.where(GENERAL_ACTIVITY_TAB_START_DATE).lte(startDate));
            Criteria endDateCriteria = new Criteria().orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).exists(false), Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).gte(startDate));
            criteria.andOperator(startDateCriteria, endDateCriteria);
        } else {
            criteria.and(GENERAL_ACTIVITY_TAB_START_DATE).lte(endDate).orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).exists(false), Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).gte(startDate));
        }
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, Activity.class);
    }

    public Set<BigInteger> findAllActivitiesByUnitIdAndUnavailableTimeType(long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID,
                        BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE),
                match(Criteria.where(DELETED).is(false).and(ACTIVITY_BALANCE_SETTINGS_TIME_TYPE_TIME_TYPES).is(NON_WORKING_TYPE)),
                // match(Criteria.where("unitId").is(unitId).and("deleted").is(false).and("activityBalanceSettings.timeType.timeTypes").is("NON_WORKING_TYPE")),
                //group("unitId").addToSet("id").as("ids"),
                project(AppConstants.ID)

        );
        AggregationResults<Map> result = mongoTemplate.aggregate(aggregation, Activity.class, Map.class);
        List<Map> activityIdMap = result.getMappedResults();
        Set<BigInteger> activityIds = new HashSet<>();
        for (Map activityMap : activityIdMap) {
            activityIds.add(new BigInteger(activityMap.get(UNDERSCORE_ID).toString()));
        }
        return activityIds;
    }

    public Activity findByNameIgnoreCaseAndCountryIdAndByDate(String name, Long countryId, LocalDate startDate, LocalDate endDate) {
        Criteria criteria = Criteria.where(NAME).regex(Pattern.compile("^" + name + "$", Pattern.CASE_INSENSITIVE)).and(DELETED).is(false).and(COUNTRY_ID).is(countryId);
        if (endDate == null) {
            Criteria startDateCriteria = new Criteria().orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_START_DATE).gte(startDate), Criteria.where(GENERAL_ACTIVITY_TAB_START_DATE).lte(startDate));
            Criteria endDateCriteria = new Criteria().orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).exists(false), Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).gte(startDate));
            criteria.andOperator(startDateCriteria, endDateCriteria);
        } else {
            criteria.and(GENERAL_ACTIVITY_TAB_START_DATE).lte(endDate).orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).exists(false), Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).gte(startDate));
        }
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, Activity.class);
    }


    public Activity findByNameIgnoreCaseAndUnitIdAndByDate(String name, Long unitId, LocalDate startDate, LocalDate endDate) {
        Criteria criteria = Criteria.where(NAME).regex(Pattern.compile("^" + name + "$", Pattern.CASE_INSENSITIVE)).and(DELETED).is(false).and(UNIT_ID).is(unitId);
        if (endDate == null) {
            Criteria startDateCriteria = new Criteria().orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_START_DATE).gte(startDate), Criteria.where(GENERAL_ACTIVITY_TAB_START_DATE).lte(startDate));
            Criteria endDateCriteria = new Criteria().orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).exists(false), Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).gte(startDate));
            criteria.andOperator(startDateCriteria, endDateCriteria);
        } else {
            criteria.and(GENERAL_ACTIVITY_TAB_START_DATE).lte(endDate).orOperator(Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).exists(false), Criteria.where(GENERAL_ACTIVITY_TAB_END_DATE).gte(startDate));
        }
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, Activity.class);
    }

    public ActivityWrapper findActivityAndTimeTypeByActivityId(BigInteger activityId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(AppConstants.ID).is(activityId).and(DELETED).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID,
                        TIME_TYPE1),
                getProject()
        );
        AggregationResults<ActivityWrapper> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityWrapper.class);
        return (result.getMappedResults().isEmpty()) ? null : result.getMappedResults().get(0);
    }

    private ProjectionOperation getProject() {
        return project().and(AppConstants.ID).as(AppConstants.ACTIVITY_ID).and(NAME).as(ACTIVITY_NAME).and(DESCRIPTION).as(ACTIVITY_DESCRIPTION)
                .and(COUNTRY_ID).as(ACTIVITY_COUNTRY_ID).and(EXPERTISES).as(ACTIVITY_EXPERTISES)
                .and(AppConstants.ID).as(AppConstants.ACTIVITY_ID)
                .and(ORGANIZATION_TYPES).as(ACTIVITY_ORGANIZATION_TYPES).and(ORGANIZATION_SUB_TYPES).as(ACTIVITY_ORGANIZATION_SUB_TYPES)
                .and(REGIONS).as(ACTIVITY_REGIONS).and(LEVELS).as(ACTIVITY_LEVELS)
                .and(EMPLOYMENT_TYPES).as(ACTIVITY_EMPLOYMENT_TYPES).and(TAGS).as(ACTIVITY_TAGS)
                .and(STATE).as(ACTIVITY_STATE).and(UNIT_ID).as(ACTIVITY_UNIT_ID)
                .and(PARENT_ID).as(ACTIVITY_PARENT_ID).and(IS_PARENT_ACTIVITY).as(ACTIVITY_IS_PARENT_ACTIVITY).and(GENERAL_ACTIVITY_TAB).as(ACTIVITY_GENERAL_ACTIVITY_TAB)
                .and(BALANCE_SETTINGS_ACTIVITY_TAB).as(ACTIVITY_BALANCE_SETTINGS_ACTIVITY_TAB)
                .and(RULES_ACTIVITY_TAB).as(ACTIVITY_RULES_ACTIVITY_TAB).and(INDIVIDUAL_POINTS_ACTIVITY_TAB).as(ACTIVITY_INDIVIDUAL_POINTS_ACTIVITY_TAB)
                .and(TIME_CALCULATION_ACTIVITY_TAB).as(ACTIVITY_TIME_CALCULATION_ACTIVITY_TAB)
                .and(NOTES_ACTIVITY_TAB).as(ACTIVITY_NOTES_ACTIVITY_TAB)
                .and(COMMUNICATION_ACTIVITY_TAB).as(ACTIVITY_COMMUNICATION_ACTIVITY_TAB)
                .and(BONUS_ACTIVITY_TAB).as(ACTIVITY_BONUS_ACTIVITY_TAB)
                .and(SKILL_ACTIVITY_TAB).as(ACTIVITY_SKILL_ACTIVITY_TAB)
                .and(OPTA_PLANNER_SETTING_ACTIVITY_TAB).as(ACTIVITY_OPTA_PLANNER_SETTING_ACTIVITY_TAB)
                .and(CTA_AND_WTA_SETTINGS_ACTIVITY_TAB).as(ACTIVITY_CTA_AND_WTA_SETTINGS_ACTIVITY_TAB)
                .and(LOCATION_ACTIVITY_TAB).as(ACTIVITY_LOCATION_ACTIVITY_TAB)
                .and(PHASE_SETTINGS_ACTIVITY_TAB).as(ACTIVITY_PHASE_SETTINGS_ACTIVITY_TAB)
                .and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE1).and(TIME_TYPE_TIME_TYPES).as(TIME_TYPE1)
                .and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE_INFO);
    }

    @Override
    public List<ActivityWrapper> findActivitiesAndTimeTypeByActivityId(Collection<BigInteger> activityIds) {
        return getActivityWrappersByCriteria(Criteria.where("id").in(activityIds).and(DELETED).is(false));
    }

    @Override
    public List<ActivityWrapper> findParentActivitiesAndTimeTypeByActivityId(Collection<BigInteger> activityIds) {
        return getParentActivityWrappersByCriteria(Criteria.where("id").in(activityIds).and(DELETED).is(false));
    }

    private List<ActivityWrapper> getParentActivityWrappersByCriteria(Criteria criteria) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(criteria),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, ID1,
                        TIME_TYPE1),
                getProjectForParentActivityWrapper()
        );
        AggregationResults<ActivityWrapper> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityWrapper.class);
        return result.getMappedResults();
    }

    private ProjectionOperation getProjectForParentActivityWrapper() {
        return project().and(AppConstants.ID).as(AppConstants.ACTIVITY_ID).and(NAME).as(ACTIVITY_NAME).and(CHILD_ACTIVITY_IDS).as(ACTIVITY_CHILD_ACTIVITY_IDS)
                .and(AppConstants.ID).as(AppConstants.ACTIVITY_ID).and(UNIT_ID).as(ACTIVITY_UNIT_ID)
                .and(BALANCE_SETTINGS_ACTIVITY_TAB).as(ACTIVITY_BALANCE_SETTINGS_ACTIVITY_TAB).and(GENERAL_ACTIVITY_TAB).as(ACTIVITY_GENERAL_ACTIVITY_TAB).and(RULES_ACTIVITY_TAB).as(ACTIVITY_RULES_ACTIVITY_TAB)
                .and(TIME_CALCULATION_ACTIVITY_TAB).as(ACTIVITY_TIME_CALCULATION_ACTIVITY_TAB).and(BONUS_ACTIVITY_TAB).as(ACTIVITY_BONUS_ACTIVITY_TAB)
                .and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE1).and(TIME_TYPE_TIME_TYPES).as(TIME_TYPE1).and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE_INFO);
    }

    @Override
    public List<Activity> findActivitiesSickSettingByActivityIds(Collection<BigInteger> activityIds){
        Query query = new Query(Criteria.where(ID).in(activityIds).and(DELETED).is(false).and(ACTIVITY_RULES_SETTINGS_SICKNESS_SETTING_VALID).is(true));
        query.fields().include(ACTIVITY_RULES_SETTINGS);
        return mongoTemplate.find(query,Activity.class);
    }

    private List<ActivityWrapper> getActivityWrappersByCriteria(Criteria criteria) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(criteria),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, ID1,
                        TIME_TYPE1),
                getProjectForActivityWrapper()
        );
        AggregationResults<ActivityWrapper> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityWrapper.class);
        return result.getMappedResults();
    }



    private ProjectionOperation getProjectForActivityWrapper() {
        return project().and(AppConstants.ID).as(AppConstants.ACTIVITY_ID).and(NAME).as(ACTIVITY_NAME).and(DESCRIPTION).as(ACTIVITY_DESCRIPTION)
                .and(COUNTRY_ID).as(ACTIVITY_COUNTRY_ID).and(EXPERTISES).as(ACTIVITY_EXPERTISES).and(CHILD_ACTIVITY_IDS).as(ACTIVITY_CHILD_ACTIVITY_IDS)
                .and(AppConstants.ID).as(AppConstants.ACTIVITY_ID)
                .and(ORGANIZATION_TYPES).as(ACTIVITY_ORGANIZATION_TYPES).and(ORGANIZATION_SUB_TYPES).as(ACTIVITY_ORGANIZATION_SUB_TYPES)
                .and(REGIONS).as(ACTIVITY_REGIONS).and(LEVELS).as(ACTIVITY_LEVELS)
                .and(EMPLOYMENT_TYPES).as(ACTIVITY_EMPLOYMENT_TYPES).and(TAGS).as(ACTIVITY_TAGS)
                .and(STATE).as(ACTIVITY_STATE).and(UNIT_ID).as(ACTIVITY_UNIT_ID)
                .and(PARENT_ID).as(ACTIVITY_PARENT_ID).and(IS_PARENT_ACTIVITY).as(ACTIVITY_IS_PARENT_ACTIVITY).and(GENERAL_ACTIVITY_TAB).as(ACTIVITY_GENERAL_ACTIVITY_TAB)
                .and(BALANCE_SETTINGS_ACTIVITY_TAB).as(ACTIVITY_BALANCE_SETTINGS_ACTIVITY_TAB)
                .and(RULES_ACTIVITY_TAB).as(ACTIVITY_RULES_ACTIVITY_TAB).and(INDIVIDUAL_POINTS_ACTIVITY_TAB).as(ACTIVITY_INDIVIDUAL_POINTS_ACTIVITY_TAB)
                .and(TIME_CALCULATION_ACTIVITY_TAB).as(ACTIVITY_TIME_CALCULATION_ACTIVITY_TAB)
                .and(NOTES_ACTIVITY_TAB).as(ACTIVITY_NOTES_ACTIVITY_TAB)
                .and(COMMUNICATION_ACTIVITY_TAB).as(ACTIVITY_COMMUNICATION_ACTIVITY_TAB)
                .and(BONUS_ACTIVITY_TAB).as(ACTIVITY_BONUS_ACTIVITY_TAB)
                .and(SKILL_ACTIVITY_TAB).as(ACTIVITY_SKILL_ACTIVITY_TAB)
                .and(OPTA_PLANNER_SETTING_ACTIVITY_TAB).as(ACTIVITY_OPTA_PLANNER_SETTING_ACTIVITY_TAB)
                .and(CTA_AND_WTA_SETTINGS_ACTIVITY_TAB).as(ACTIVITY_CTA_AND_WTA_SETTINGS_ACTIVITY_TAB)
                .and(LOCATION_ACTIVITY_TAB).as(ACTIVITY_LOCATION_ACTIVITY_TAB)
                .and(PHASE_SETTINGS_ACTIVITY_TAB).as(ACTIVITY_PHASE_SETTINGS_ACTIVITY_TAB)
                .and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE1).and(TIME_TYPE_TIME_TYPES).as(TIME_TYPE1)
                .and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE_INFO);
    }
    @Override
    public List<ActivityWrapper> getAllActivityWrapperBySecondLevelTimeType(TimeTypeEnum secondLevelTimeType,Long unitId){
        return getActivityWrappersByCriteria(Criteria.where(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE).is(secondLevelTimeType).and(UNIT_ID).is(unitId).and(DELETED).is(false));
    }

    public List<TimeTypeAndActivityIdDTO> findAllTimeTypeByActivityIds(Set<BigInteger> activityIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(AppConstants.ID).in(activityIds).and(DELETED).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID,
                        TIME_TYPE1), project().and(AppConstants.ID).as(ActivityConstants.ACTIVITY_ID)
                        .and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE1).and(TIME_TYPE_TIME_TYPES).as(TIME_TYPE1));
        AggregationResults<TimeTypeAndActivityIdDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, TimeTypeAndActivityIdDTO.class);
        return result.getMappedResults();
    }

    public StaffActivitySettingDTO findStaffPersonalizedSettings(Long unitId, BigInteger activityId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false).and(UNDERSCORE_ID).is(activityId)),
                project(ACTIVITY_RULES_SETTINGS_SHORTEST_TIME, ACTIVITY_RULES_SETTINGS_LONGEST_TIME, ACTIVITY_RULES_SETTINGS_EARLIEST_START_TIME, ACTIVITY_RULES_SETTINGS_LATEST_START_TIME, "activityRulesSettings.maximumEndTime", "activityOptaPlannerSetting.maxThisActivityPerShift", "activityOptaPlannerSetting.minLength", "activityOptaPlannerSetting.eligibleForMove", "activityTimeCalculationSettings.defaultStartTime").and("activityTimeCalculationSettings.dayTypes").as("dayTypeIds")
        );
        AggregationResults<StaffActivitySettingDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, StaffActivitySettingDTO.class);
        return (result.getMappedResults().isEmpty()) ? null : result.getMappedResults().get(0);
    }

    public List<ActivityDTO> findAllByTimeTypeIdAndUnitId(Set<BigInteger> timeTypeIds, Long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID).in(timeTypeIds).and(ACTIVITY_RULES_SETTINGS_ALLOWED_AUTO_ABSENCE).is(true).and(DELETED).is(false).and(UNIT_ID).is(unitId)),
                project().and(AppConstants.ID).as(AppConstants.ID).and(NAME).as(NAME).and(TIME_CALCULATION_ACTIVITY_TAB).as(TIME_CALCULATION_ACTIVITY_TAB));
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }

    @Override
    public List<ActivityWrapper> findActivitiesAndTimeTypeByParentIdsAndUnitId(List<BigInteger> activityIds, Long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(DELETED).is(false).and(UNIT_ID).is(unitId).orOperator(Criteria.where(COUNTRY_PARENT_ID).in(activityIds), Criteria.where(AppConstants.ID).in(activityIds))),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                project().and(AppConstants.ID).as(AppConstants.ACTIVITY_ID).and(NAME).as(ACTIVITY_NAME)
                        .and(COUNTRY_ID).as(ACTIVITY_COUNTRY_ID).and(EXPERTISES).as(ACTIVITY_EXPERTISES)
                        .and(PARENT_ID).as(ACTIVITY_PARENT_ID)
                        .and(COUNTRY_PARENT_ID).as(ACTIVITY_COUNTRY_PARENT_ID)
                        .and(EMPLOYMENT_TYPES).as(ACTIVITY_EMPLOYMENT_TYPES)
                        .and(STATE).as(ACTIVITY_STATE).and(UNIT_ID).as(ACTIVITY_UNIT_ID)
                        .and(IS_PARENT_ACTIVITY).as(ACTIVITY_IS_PARENT_ACTIVITY).and(GENERAL_ACTIVITY_TAB).as(ACTIVITY_GENERAL_ACTIVITY_TAB)
                        .and(BALANCE_SETTINGS_ACTIVITY_TAB).as(ACTIVITY_BALANCE_SETTINGS_ACTIVITY_TAB)
                        .and(RULES_ACTIVITY_TAB).as(ACTIVITY_RULES_ACTIVITY_TAB).and(INDIVIDUAL_POINTS_ACTIVITY_TAB).as(ACTIVITY_INDIVIDUAL_POINTS_ACTIVITY_TAB)
                        .and(TIME_CALCULATION_ACTIVITY_TAB).as(ACTIVITY_TIME_CALCULATION_ACTIVITY_TAB)
                        .and(NOTES_ACTIVITY_TAB).as(ACTIVITY_NOTES_ACTIVITY_TAB)
                        .and(COMMUNICATION_ACTIVITY_TAB).as(ACTIVITY_COMMUNICATION_ACTIVITY_TAB)
                        .and(BONUS_ACTIVITY_TAB).as(ACTIVITY_BONUS_ACTIVITY_TAB)
                        .and(SKILL_ACTIVITY_TAB).as(ACTIVITY_SKILL_ACTIVITY_TAB)
                        .and(OPTA_PLANNER_SETTING_ACTIVITY_TAB).as(ACTIVITY_OPTA_PLANNER_SETTING_ACTIVITY_TAB)
                        .and(CTA_AND_WTA_SETTINGS_ACTIVITY_TAB).as(ACTIVITY_CTA_AND_WTA_SETTINGS_ACTIVITY_TAB)
                        .and(LOCATION_ACTIVITY_TAB).as(ACTIVITY_LOCATION_ACTIVITY_TAB)
                        .and(PHASE_SETTINGS_ACTIVITY_TAB).as(ACTIVITY_PHASE_SETTINGS_ACTIVITY_TAB)
                        .and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE1).and(TIME_TYPE_TIME_TYPES).as(TIME_TYPE1)
                        .and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE_INFO)
        );
        AggregationResults<ActivityWrapper> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityWrapper.class);
        return result.getMappedResults();
    }

    @Override
    public List<ActivityDTO> findAllActivitiesByCountryIdAndTimeTypes(Long countryId, List<BigInteger> timeTypeIds) {
        Criteria criteria =isNull(countryId)?Criteria.where(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID).in(timeTypeIds):Criteria.where(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID).in(timeTypeIds).and(DELETED).is(false).and(COUNTRY_ID).is(countryId);
        Aggregation aggregation = Aggregation.newAggregation(match(criteria)
                ,project().and(ID).as(ID).and(NAME).as(NAME));
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();

    }

    @Override
    public List<Activity> findAllActivitiesByOrganizationTypeOrSubTypeOrBreakTypes(Long orgTypeIds, List<Long> orgSubTypeIds) {
        List<TimeTypeEnum> breakTypes = new ArrayList<>();
        breakTypes.add(PAID_BREAK);
        breakTypes.add(UNPAID_BREAK);
        Aggregation aggregation = Aggregation.newAggregation(match(Criteria.where(IS_PARENT_ACTIVITY).is(true).and(DELETED).is(false))
                , lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                match(Criteria.where(STATE).nin("DRAFT").orOperator(Criteria.where(ORGANIZATION_SUB_TYPES).in(orgSubTypeIds), Criteria.where(TIME_TYPE_SECOND_LEVEL_TYPE).in(breakTypes))));
        AggregationResults<Activity> result = mongoTemplate.aggregate(aggregation, Activity.class, Activity.class);
        return result.getMappedResults();
    }

    @Override
    public List<Activity> findAllBreakActivitiesByOrganizationId(Long unitId) {
        List<TimeTypeEnum> breakTypes = new ArrayList<>();
        breakTypes.add(PAID_BREAK);
        breakTypes.add(UNPAID_BREAK);
        Aggregation aggregation = Aggregation.newAggregation(match(Criteria.where(DELETED).is(false).and(UNIT_ID).is(unitId).and(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE).in(breakTypes)),
                project("id")
                );
        AggregationResults<Activity> result = mongoTemplate.aggregate(aggregation, Activity.class, Activity.class);
        return result.getMappedResults();
    }


    @Override
    public List<ActivityDTO> findChildActivityActivityIds(Set<BigInteger> activityIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(AppConstants.ID).in(activityIds).and(DELETED).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                project(NAME, AppConstants.ID, CHILD_ACTIVITY_IDS)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(ALLOW_CHILD_ACTIVITIES)
                        .and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(APPLICABLE_FOR_CHILD_ACTIVITIES),
                match(Criteria.where(APPLICABLE_FOR_CHILD_ACTIVITIES).is(true))
        );
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }

    @Override
    public List<ActivityDTO> findChildActivityIdsByActivityIds(Collection<BigInteger> activityIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(AppConstants.ID).in(activityIds).and(DELETED).is(false)),
                project(CHILD_ACTIVITY_IDS)
        );
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }

    public boolean existsByActivityIdInChildActivities(BigInteger activityId) {
        Query query = new Query(Criteria.where(CHILD_ACTIVITY_IDS).is(activityId).and(DELETED).is(false).and(STATE).is(ActivityStateEnum.PUBLISHED));
        return isNotNull(mongoTemplate.findOne(query, Activity.class));
    }

    private List<AggregationOperation> getCustomAgregationForCompositeActivityWithCategory(boolean isChildActivityEligibleForStaffingLevel,boolean groupByCategory) {
        String group = getGroup();
        String projection = getProjection(isChildActivityEligibleForStaffingLevel,groupByCategory);
        List<AggregationOperation> aggregationOperations = new ArrayList<>();
        aggregationOperations.add(lookup(ACTIVITY_CATEGORY, GENERAL_ACTIVITY_TAB_CATEGORY_ID, UNDERSCORE_ID,
                CATEGORY));
        aggregationOperations.add(project(CHILD_ACTIVITY_IDS, TIME_CALCULATION_ACTIVITY_TAB,TRANSLATIONS, BALANCE_SETTINGS_ACTIVITY_TAB, NAME).and(CATEGORY).arrayElementAt(0).as(CATEGORY).and(TIME_TYPE_INFO).arrayElementAt(0).as(TIME_TYPE_INFO));
        aggregationOperations.add(project(CHILD_ACTIVITY_IDS, TIME_CALCULATION_ACTIVITY_TAB,TRANSLATIONS, BALANCE_SETTINGS_ACTIVITY_TAB, NAME, TIME_TYPE_INFO).and("category._id").as(CATEGORY_ID).and("category.name").as(CATEGORY_NAME));
        aggregationOperations.add(unwind(CHILD_ACTIVITY_IDS, true));
        aggregationOperations.add(lookup(ACTIVITIES, CHILD_ACTIVITY_IDS, UNDERSCORE_ID,
                CHILD_ACTIVITIES));
        aggregationOperations.add(lookup(TIME_TYPE, CHILD_ACTIVITIES_ACTIVITY_BALANCE_SETTINGS_TIME_TYPE_ID, UNDERSCORE_ID,
                COMPOSITE_TIME_TYPE_INFO));
        aggregationOperations.add(lookup(ACTIVITY_PRIORITY, CHILD_ACTIVITIES_ACTIVITY_PRIORITY_ID, UNDERSCORE_ID, CHILD_ACTIVITY_PRIORITY));
        aggregationOperations.add(project(CHILD_ACTIVITIES, TIME_CALCULATION_ACTIVITY_TAB,TRANSLATIONS, TIME_TYPE_INFO, BALANCE_SETTINGS_ACTIVITY_TAB, NAME, CATEGORY_ID, CATEGORY_NAME).and(CHILD_ACTIVITIES).arrayElementAt(0).as(CHILD_ACTIVITIES).and(COMPOSITE_TIME_TYPE_INFO).arrayElementAt(0).as(COMPOSITE_TIME_TYPE_INFO));
        aggregationOperations.add(project(TIME_CALCULATION_ACTIVITY_TAB,TRANSLATIONS, COMPOSITE_TIME_TYPE_INFO, TIME_TYPE_INFO, BALANCE_SETTINGS_ACTIVITY_TAB, NAME, CATEGORY_ID, CATEGORY_NAME, CHILD_ACTIVITIES));
        aggregationOperations.add(project(TIME_CALCULATION_ACTIVITY_TAB,TRANSLATIONS, TIME_TYPE_INFO, BALANCE_SETTINGS_ACTIVITY_TAB, NAME, CATEGORY_ID, CATEGORY_NAME).and("compositeTimeTypeInfo.allowChildActivities").as("compositeActivities.allowChildActivities")
                .and(CHILD_ACTIVITIES_ACTIVITY_TIME_CALCULATION_SETTINGS).as(CHILD_ACTIVITIES_ACTIVITY_TIME_CALCULATION_SETTINGS)
                .and(CHILD_ACTIVITIES_ACTIVITY_BALANCE_SETTINGS).as(CHILD_ACTIVITIES_ACTIVITY_BALANCE_SETTINGS)
                .and(CHILD_ACTIVITIES_ACTIVITY_RULES_SETTINGS).as(CHILD_ACTIVITIES_ACTIVITY_RULES_SETTINGS)
                .and(CHILD_ACTIVITIES_ID).as(CHILD_ACTIVITIES_ID)
                .and(CHILD_ACTIVITIES_NAME).as(CHILD_ACTIVITIES_NAME)
                .and(CHILD_ACTIVITIES_CATEGORY_ID).as(CHILD_ACTIVITIES_CATEGORY_ID)
                .and(CHILD_ACTIVITIES_CATEGORY_ID).as(CHILD_ACTIVITIES_CATEGORY_ID)
                .and(CHILD_ACTIVITIES_TRANSLATIONS).as(CHILD_ACTIVITIES_TRANSLATIONS));
        aggregationOperations.add(new CustomAggregationOperation(Document.parse(group)));
        aggregationOperations.add(new CustomAggregationOperation(Document.parse(projection)));
        return aggregationOperations;
    }

    private String getProjection(boolean isChildActivityEligibleForStaffingLevel, boolean groupByCategory) {
        if(groupByCategory){
            return new StringBuffer("{'$project':{'childActivities':").append(isChildActivityEligibleForStaffingLevel ? "{'$filter':{  'input':'$childActivities','as':'childActivity','cond':{'$eq':['$$childActivity.activityRulesSettings.eligibleForStaffingLevel',true]} }}" : "'$childActivities'").append(",'activityTimeCalculationSettings':'$_id.activityTimeCalculationSettings','activityBalanceSettings':'$_id.activityBalanceSettings','_id':'$_id.id','name':'$_id.name','activityPriorityId':'$_id.activityPriorityId','activityPriority':'$_id.activityPriority','timeTypeInfo':'$_id.timeTypeInfo','allowChildActivities':'$_id.timeTypeInfo.allowChildActivities','activityCategory.categoryId':'$_id.categoryId','activityCategory.categoryName':'$_id.categoryName','translations':'$_id.translations'}}").toString();
        }
        return new StringBuffer("{'$project':{'childActivities':").append(isChildActivityEligibleForStaffingLevel ? "{'$filter':{  'input':'$childActivities','as':'childActivity','cond':{'$eq':['$$childActivity.activityRulesSettings.eligibleForStaffingLevel',true]} }}" : "'$childActivities'").append(",'activityTimeCalculationSettings':'$_id.activityTimeCalculationSettings','activityBalanceSettings':'$_id.activityBalanceSettings','_id':'$_id.id','name':'$_id.name','activityPriorityId':'$_id.activityPriorityId','activityPriority':'$_id.activityPriority','timeTypeInfo':'$_id.timeTypeInfo','allowChildActivities':'$_id.timeTypeInfo.allowChildActivities','categoryId':'$_id.categoryId','categoryName':'$_id.categoryName','translations':'$_id.translations'}}").toString();
    }

    private String getGroup() {
        return GROUP;
    }


    public List<ActivityWithCompositeDTO> findAllActivityByUnitIdWithCompositeActivities(Long unitId,Collection<BigInteger> activitIds) {
        Criteria criteria = Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false);
        Aggregation aggregation = getParentActivityAggregation(criteria,getBigIntegerString(activitIds.iterator()));
        AggregationResults<ActivityWithCompositeDTO> result = mongoTemplate.aggregate(aggregation, StaffActivitySetting.class, ActivityWithCompositeDTO.class);
        return result.getMappedResults();
    }

    private Aggregation getParentActivityAggregation(Criteria criteria,String activityIdsArray) {
        return Aggregation.newAggregation(
                    match(criteria),
                    group().addToSet(ACTIVITYID).as(ACTIVITY_IDS),
                    new CustomAggregationOperation("{ $project: { \n" +
                            "       \"activityIds\": { $concatArrays: [ \"$activityIds\", "+activityIdsArray+"] },\n" +
                            "       \"_id\":0\n" +
                            "       \n" +
                            "       } }"),
                    lookup(ACTIVITIES,ACTIVITY_IDS,"_id",ACTIVITIES),
                    unwind(ACTIVITIES,true),
                    replaceRoot(ACTIVITIES),
                    lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                    lookup(ACTIVITIES, CHILD_ACTIVITY_IDS, UNDERSCORE_ID,PARENT_ACTIVITY),
                    project(NAME, GENERAL_ACTIVITY_TAB, TIME_CALCULATION_ACTIVITY_TAB, EXPERTISES, EMPLOYMENT_TYPES, RULES_ACTIVITY_TAB, SKILL_ACTIVITY_TAB,
                            PHASE_SETTINGS_ACTIVITY_TAB,
                            BALANCE_SETTINGS_ACTIVITY_TAB,
                            UNIT_ID,
                            CHILD_ACTIVITY_IDS).and("parentActivity._id").as(AppConstants.PARENT_ACTIVITY_ID).and(TIME_TYPE_ALLOW_CHILD_ACTIVITIES).arrayElementAt(0).as(ALLOW_CHILD_ACTIVITIES));
    }

    @Override
    public List<ActivityPhaseSettings> findActivityIdAndStatusByUnitAndAccessGroupIds(Long unitId, List<Long> accessGroupIds) {
        String group = getGroupString();
        String project = PROJECT;
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false)),
                project(AppConstants.ID).and(ACTIVITY_PHASE_SETTINGS_PHASE_TEMPLATE_VALUES).as(PHASE_TEMPLATE_VALUES),
                unwind(PHASE_TEMPLATE_VALUES),
                match(Criteria.where(PHASE_TEMPLATE_VALUES_ACTIVITY_SHIFT_STATUS_SETTINGS_ACCESS_GROUP_IDS).in(accessGroupIds)),
                unwind(PHASE_TEMPLATE_VALUES_ACTIVITY_SHIFT_STATUS_SETTINGS),
                match(Criteria.where(PHASE_TEMPLATE_VALUES_ACTIVITY_SHIFT_STATUS_SETTINGS_ACCESS_GROUP_IDS).in(accessGroupIds)),
                project(AppConstants.ID).and(PHASE_TEMPLATE_VALUES_ACTIVITY_SHIFT_STATUS_SETTINGS).as(ACTIVITY_SHIFT_STATUS_SETTINGS).and(PHASE_TEMPLATE_VALUES_PHASE_ID).as(PHASE_ID),
                group(AppConstants.ID, PHASE_ID).addToSet(ACTIVITY_SHIFT_STATUS_SETTINGS).as(ACTIVITY_SHIFT_STATUS_SETTINGS),
                project().and(AppConstants.ID).as(UNDERSCORE_ID).and(PHASE_ID).as(PHASE_TEMPLATE_VALUES_PHASE_ID).and(ACTIVITY_SHIFT_STATUS_SETTINGS).as(PHASE_TEMPLATE_VALUES_ACTIVITY_SHIFT_STATUS_SETTINGS),
                new CustomAggregationOperation(Document.parse(group)),
                new CustomAggregationOperation(Document.parse(project))
        );
        AggregationResults<ActivityPhaseSettings> results = mongoTemplate.aggregate(aggregation, Activity.class, ActivityPhaseSettings.class);
        return results.getMappedResults();
    }

    private String getGroupString() {
        return GROUP_BY_PHASETEMPLATES;
    }

    @Override
    public boolean unassignExpertiseFromActivitiesByExpertiesId(Long expertiseId) {
        Update update = new Update().pull(EXPERTISES, expertiseId);
        return mongoTemplate.updateMulti(new Query(), update, Activity.class).wasAcknowledged();
    }

    @Override
    public boolean unassignCompositeActivityFromActivitiesByactivityId(BigInteger activityId) {
        Update update = new Update();
        return mongoTemplate.updateMulti(new Query(), update, Activity.class).wasAcknowledged();
    }

    @Override
    public List<ActivityTagDTO> findAllActivityByUnitIdAndNotPartOfTeam(Long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                project(NAME, DESCRIPTION, UNIT_ID,STATE, RULES_ACTIVITY_TAB, PARENT_ID, GENERAL_ACTIVITY_TAB,TRANSLATIONS).and(TIME_CALCULATION_ACTIVITY_TAB).as(TIME_CALCULATION_ACTIVITY_TAB)
                        .and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE1),
                match(Criteria.where(TIME_TYPE_PART_OF_TEAM).is(false))
        );
        AggregationResults<ActivityTagDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityTagDTO.class);
        return result.getMappedResults();
    }

    @Override
    public TimeTypeEnum findTimeTypeByActivityId(BigInteger activityId){
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNDERSCORE_ID).is(activityId).and(DELETED).is(false)),
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                project().andExclude(UNDERSCORE_ID).and(TIME_TYPE1).arrayElementAt(0).as(TIME_TYPE1),
                project().and(TIME_TYPE_SECOND_LEVEL_TYPE).as(SECOND_LEVEL_TYPE)
        );
        AggregationResults<TimeType> result = mongoTemplate.aggregate(aggregation, Activity.class, TimeType.class);
        return result.getMappedResults().get(0).getSecondLevelType();
    }

    @Override
    //TODO it will be done in @Query
    public List<ActivityDTO> findAbsenceActivityByUnitId(Long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false).and(BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE).is(TimeTypeEnum.ABSENCE)),
                project(NAME, DESCRIPTION, UNIT_ID, RULES_ACTIVITY_TAB, PARENT_ID, GENERAL_ACTIVITY_TAB)
                );
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }

    @Override
    //TODO it will be done in @Query
    public List<ActivityDTO> getActivityRankWithRankByUnitId(Long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false)),
                project(NAME, DESCRIPTION, UNIT_ID, RULES_ACTIVITY_TAB, PARENT_ID, GENERAL_ACTIVITY_TAB)
        );
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }

    public List<ActivityTimeTypeWrapper> getActivityPath(final String activityId) {
        Document groupDocument = Document.parse(TIMETYPE_HIERACHY_GROUP);
        CustomAggregationOperation customGroupAggregationOperation = new CustomAggregationOperation(groupDocument);
        Document projectionDocument = Document.parse(TIMETYPE_HIERACHY);
        CustomAggregationOperation customProjectAggregationOperation = new CustomAggregationOperation(projectionDocument);

        Document pathArrayProject = Document.parse(PATH_ARRAY);
        CustomAggregationOperation pathArrayProjection = new CustomAggregationOperation(pathArrayProject);
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNDERSCORE_ID).is(activityId)),
                graphLookup(TIME_TYPE).
                        startWith("$activityBalanceSettings.timeTypeId")
                        .connectFrom("upperLevelTimeTypeId")
                        .connectTo(ID1)
                        .maxDepth(3)
                        .depthField("numofchild")
                        .as("patharray"),
                unwind("$patharray"),
                sort(Sort.Direction.ASC, "patharray._id"),
                pathArrayProjection,
                customGroupAggregationOperation,
                customProjectAggregationOperation

        ).withOptions(new AggregationOptions(true, false, null, null));

        return mongoTemplate.aggregate(aggregation, Activity.class, ActivityTimeTypeWrapper.class).getMappedResults();
    }

    @Override
    //TODO it will be done in @Query
    public List<ActivityDTO> getActivityDetailsWithRankByUnitId(Long unitId){
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false)),
                project(NAME, GENERAL_ACTIVITY_TAB,TRANSLATIONS)
        );
        AggregationResults<ActivityDTO> result = mongoTemplate.aggregate(aggregation, Activity.class, ActivityDTO.class);
        return result.getMappedResults();
    }

    @Override
    public  Set<BigInteger> findAllShowOnCallAndStandByActivitiesByUnitId(Long unitId, UnityActivitySetting unityActivitySetting){
        Criteria criteriaDefinition = Criteria.where(UNIT_ID).is(unitId).and(DELETED).is(false).and(TIME_TYPE1+".unityActivitySetting").is(unityActivitySetting);
        Aggregation aggregation = Aggregation.newAggregation(
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                unwind(TIME_TYPE1),
                match(criteriaDefinition),
                project(AppConstants.ID,NAME)
        );
        return mongoTemplate.aggregate(aggregation,Activity.class,Activity.class).getMappedResults().stream().map(activity -> activity.getId()).collect(Collectors.toSet());
    }

    @Override
    public List<Activity> findAllActivityByCountryAndPriorityFor(long countryOrUnitId, boolean countryId, PriorityFor priorityFor) {
        Criteria criteria = Criteria.where(DELETED).is(false).and(countryId?COUNTRY_ID:UNIT_ID).is(countryOrUnitId).and(TIME_TYPE1+".priorityFor").is(priorityFor);
        Aggregation aggregation = Aggregation.newAggregation(
                lookup(TIME_TYPE, BALANCE_SETTINGS_ACTIVITY_TAB_TIME_TYPE_ID, UNDERSCORE_ID, TIME_TYPE1),
                unwind(TIME_TYPE1),
                match(criteria),
                project(NAME,TIME_CALCULATION_ACTIVITY_TAB,STATE,IS_CHILD_ACTIVITY,EXPERTISES,COUNTRY_ID,GENERAL_ACTIVITY_TAB,TRANSLATIONS)
        );
        AggregationResults<Activity> result = mongoTemplate.aggregate(aggregation, Activity.class, Activity.class);
        return result.getMappedResults();
    }

    @Override
    public List<ActivityWithCompositeDTO> findAllActivityByIdsAndIncludeChildActivitiesWithMostUsedCountOfActivity(Collection<BigInteger> activityIds,Long unitId, Long staffId, boolean isActivityType) {
        String activityIdString = getBigIntegerString(activityIds.iterator());
        AggregationOperation[] aggregations = new AggregationOperation[10];
        int i=0;
        Criteria criteria = Criteria.where(STAFF_ID).in(staffId).and(DELETED).is(false);
        if(!mongoTemplate.exists(new Query(Criteria.where(STAFF_ID).is(staffId).and(DELETED).is(false)),
                StaffActivitySetting.class)){
            criteria = Criteria.where(STAFF_ID).in(newArrayList(0l)).and(DELETED).is(false);
        }else {
            criteria = Criteria.where(STAFF_ID).in(staffId).and(DELETED).is(false).and(UNIT_ID).is(unitId);
        }
        aggregations[i++] = match(criteria);
        aggregations[i++] = group(STAFF_ID).addToSet(ACTIVITYID).as(ACTIVITY_IDS);
        aggregations[i++] = getCustomLookUpForActivityAggregationOperation(activityIdString,isActivityType,unitId);
        aggregations[i++] = getCustomAggregationOperationForChildActivitiyIds();
        aggregations[i++] = getCustomAggregationOperationForConcatArray();
        aggregations[i++] = getCustomAggregationOperationForActivities();
        aggregations[i++] = new CustomAggregationOperation("{\"$unwind\": \"$activities\"}");
        aggregations[i++] = getCustomAggregationOperationForReplaceActivity();
        aggregations[i++] = getCustomAggregationOperationForStaffActivitySetting(staffId);
        aggregations[i++] = getCustomAggregationOperationForMatchCount();
        return mongoTemplate.aggregate(Aggregation.newAggregation(aggregations), STAFF_ACTIVITY_SETTING, ActivityWithCompositeDTO.class).getMappedResults();
    }

    private CustomAggregationOperation getCustomAggregationOperationForMatchCount() {
        return new CustomAggregationOperation(USED_COUNT);
    }

    private CustomAggregationOperation getCustomAggregationOperationForStaffActivitySetting(Long staffId) {
        return new CustomAggregationOperation(getMostlyUsedCount(staffId));
    }

    private CustomAggregationOperation getCustomAggregationOperationForReplaceActivity() {
        return new CustomAggregationOperation(REPLACE_ROOT);
    }

    private CustomAggregationOperation getCustomAggregationOperationForChildActivitiyIds() {
        return new CustomAggregationOperation(ARRAY_ASSIGNMENT);
    }

    private CustomAggregationOperation getCustomAggregationOperationForConcatArray() {
        return new CustomAggregationOperation(CONCATE_ARRAY);
    }

    private CustomAggregationOperation getCustomAggregationOperationForActivities() {
        return new CustomAggregationOperation(CUSTOM_AGGREGATION_OPERATION_FOR_ACTIVITIES);
    }

    private CustomAggregationOperation getCustomLookUpForActivityAggregationOperation(String activityString,boolean isActivityType,Long unitId) {
        String condition = !isActivityType ? $_NE_$_CHILD_ACTIVITY_IDS : "";
        return new CustomAggregationOperation("{\n" +
                "    \"$lookup\": {\n" +
                "      \"from\": \"activities\",\n" +
                "      \"let\": {\n" +
                "        \"activityIds\": \"$activityIds\"\n" +
                "      },\n" +
                "      \"pipeline\": [\n" +
                "        {\n" +
                "          \"$match\": {\n" +
                "            \"$expr\": {\n" +
                "              \"$and\": [\n" +
                condition+
                "                {\n" +
                "                  \"$in\": [\n" +
                "                    \"$_id\",\n" +
                "                    "+activityString+"\n" +
                "                  ]\n" +
                "                },\n" +
                "{ $eq: [ \"$unitId\",  "+unitId+" ] }"+
                "              ]\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"$group\": {\n" +
                "            \"_id\": \"$unitId\",\n" +
                "            \"activityIds\": {\n" +
                "              \"$addToSet\": \"$_id\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"as\": \"activities\"\n" +
                "    }\n" +
                "  }");
    }


    private String getMostlyUsedCount(Long staffId){
                return "{\n" +
                "      \"$lookup\": {\n" +
                "        \"from\": \"staffActivityDetails\",\n" +
                "        \"let\": {\n" +
                "          \"staffId\": "+staffId+",\n" +
                "          \"activityId\": \"$_id\"\n" +
                "        },\n" +
                "        \"pipeline\": [\n" +
                "          {\n" +
                "            \"$match\": {\n" +
                "              \"$expr\": {\n" +
                "                \"$and\": [\n" +
                "                  {\n" +
                "                    \"$eq\": [\n" +
                "                      \"$staffId\",\n" +
                "                      \"$$staffId\"\n" +
                "                    ]\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"$eq\": [\n" +
                "                      \"$activityId\",\n" +
                "                      \"$$activityId\"\n" +
                "                    ]\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          {\n" +
                "            \"$project\": {\n" +
                "              \"useActivityCount\": 1,\n" +
                "              \"_id\": 0\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"as\": \"useActivityCount\"\n" +
                "      }\n" +
                "    }";
    }


}