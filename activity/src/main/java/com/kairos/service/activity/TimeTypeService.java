package com.kairos.service.activity;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.constants.AppConstants;
import com.kairos.constants.CommonConstants;
import com.kairos.dto.TranslationInfo;
import com.kairos.dto.activity.activity.activity_tabs.*;
import com.kairos.dto.activity.phase.PhaseDTO;
import com.kairos.dto.activity.time_type.TimeTypeDTO;
import com.kairos.dto.user.access_permission.AccessGroupRole;
import com.kairos.dto.user.country.agreement.cta.cta_response.DayTypeDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.EmploymentTypeDTO;
import com.kairos.dto.user.country.day_type.DayTypeEmploymentTypeWrapper;
import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.OrganizationHierarchy;
import com.kairos.enums.PriorityFor;
import com.kairos.enums.TimeTypeEnum;
import com.kairos.enums.TimeTypes;
import com.kairos.persistence.model.activity.Activity;
import com.kairos.persistence.model.activity.TimeType;
import com.kairos.persistence.model.activity.tabs.ActivitySkillSettings;
import com.kairos.persistence.model.activity.tabs.ActivityTimeCalculationSettings;
import com.kairos.persistence.model.activity.tabs.rules_activity_tab.ActivityRulesSettings;
import com.kairos.persistence.model.activity.tabs.rules_activity_tab.PQLSettings;
import com.kairos.persistence.model.period.PlanningPeriod;
import com.kairos.persistence.repository.activity.ActivityMongoRepository;
import com.kairos.persistence.repository.shift.ShiftMongoRepository;
import com.kairos.persistence.repository.time_type.TimeTypeMongoRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.day_type.DayTypeService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.organization.OrganizationActivityService;
import com.kairos.service.period.PlanningPeriodService;
import com.kairos.service.phase.PhaseService;
import com.kairos.service.reason_code.ReasonCodeService;
import com.kairos.service.unit_settings.ActivityRankingService;
import com.kairos.wrapper.activity.ActivitySettingsWrapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.ActivityMessagesConstants.*;
import static com.kairos.constants.AppConstants.ENTERED_TIMES;
import static com.kairos.enums.TimeTypeEnum.*;
import static com.kairos.service.activity.ActivityUtil.getCutoffInterval;
import static com.kairos.service.activity.ActivityUtil.getPhaseForRulesActivity;

@Service
public class TimeTypeService {
    public static final String PRESENCE = "Presence";
    public static final String ABSENCE = "Absence";
    @Inject
    private TimeTypeMongoRepository timeTypeMongoRepository;
    @Inject
    private DayTypeService dayTypeService;
    @Inject
    private ActivityMongoRepository activityMongoRepository;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private ActivityCategoryService activityCategoryService;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Inject private ShiftMongoRepository shiftMongoRepository;
    @Inject @Lazy private OrganizationActivityService organizationActivityService;
    @Inject private PlanningPeriodService planningPeriodService;
    @Inject private ReasonCodeService reasonCodeService;
    @Inject private PhaseService phaseService;
    @Inject private ActivityHelperService activityHelperService;
    @Inject @Lazy private ActivityRankingService activityRankingService;

    public List<TimeTypeDTO> createTimeType(List<TimeTypeDTO> timeTypeDTOs, Long countryId) {
        List<String> timeTypeLabels = timeTypeDTOs.stream().map(timeTypeDTO -> timeTypeDTO.getLabel()).collect(Collectors.toList());
        TimeType timeTypeResult = timeTypeMongoRepository.findByLabelsAndCountryId(timeTypeLabels, countryId);
        if (Optional.ofNullable(timeTypeResult).isPresent()) {
            exceptionService.duplicateDataException(MESSAGE_TIMETYPE_NAME_ALREADYEXIST);
        }
        BigInteger upperLevelTimeTypeId = timeTypeDTOs.get(0).getUpperLevelTimeTypeId();
        if (activityMongoRepository.existsByTimeTypeId(upperLevelTimeTypeId)) {
            exceptionService.actionNotPermittedException(ACTIVITY_ALREADY_EXISTS_TIME_TYPE);
        }
        TimeType upperTimeType = timeTypeMongoRepository.findOneById(upperLevelTimeTypeId);
        timeTypeDTOs.forEach(timeTypeDTO -> saveTimeType(countryId, upperTimeType, timeTypeDTO));
        return timeTypeDTOs;
    }

    private void initializeTimeTypeSettings(TimeType timeType, Long countryId) {
        timeType.setCountryId(countryId);
        List<PhaseDTO> phases = phaseService.getPhasesByCountryId(countryId);
        if (CollectionUtils.isEmpty(phases)) {
            exceptionService.actionNotPermittedException(MESSAGE_COUNTRY_PHASE_NOTFOUND);
        }
        List<PhaseTemplateValue> phaseTemplateValues = getPhaseForRulesActivity(phases);
        initializeActivitySettings(timeType, phaseTemplateValues);
    }

    public void initializeActivitySettings(TimeType timeType, List<PhaseTemplateValue> phaseTemplateValues){
        ActivityRulesSettings activityRulesSettings = new ActivityRulesSettings();
        activityRulesSettings.setPqlSettings(new PQLSettings());
        activityRulesSettings.setCutOffBalances(CutOffIntervalUnit.CutOffBalances.EXPIRE);
        activityRulesSettings.setCutOffIntervals(new ArrayList<>());
        activityRulesSettings.setDayTypes(new ArrayList<>());
        ActivityTimeCalculationSettings activityTimeCalculationSettings = new ActivityTimeCalculationSettings(ENTERED_TIMES, 0L, true, LocalTime.of(7, 0), 1d);
        activityTimeCalculationSettings.setDayTypes(new ArrayList<>());
        ActivityPhaseSettings activityPhaseSettings =new ActivityPhaseSettings(phaseTemplateValues);
        ActivitySkillSettings activitySkillSettings = new ActivitySkillSettings();
        timeType.setActivityRulesSettings(activityRulesSettings);
        timeType.setActivityTimeCalculationSettings(activityTimeCalculationSettings);
        timeType.setActivityPhaseSettings(activityPhaseSettings);
        timeType.setActivitySkillSettings(activitySkillSettings);
        timeType.setChildTimeTypeIds(new ArrayList<>());
        timeType.setExpertises(new ArrayList<>());
        timeType.setEmploymentTypes(new ArrayList<>());
        timeType.setOrganizationSubTypes(new ArrayList<>());
        timeType.setOrganizationTypes(new ArrayList<>());
        timeType.setActivityCanBeCopiedForOrganizationHierarchy(new HashSet<>());
        timeType.setLeafNode(true);
        timeType.setLevels(new ArrayList<>());
    }

    private void saveTimeType(Long countryId, TimeType upperTimeType, TimeTypeDTO timeTypeDTO) {
        TimeType timeType;
        if (timeTypeDTO.getTimeTypes() != null && timeTypeDTO.getUpperLevelTimeTypeId() != null) {
            timeType = new TimeType(TimeTypes.getByValue(timeTypeDTO.getTimeTypes()), timeTypeDTO.getLabel(), timeTypeDTO.getDescription(), timeTypeDTO.getBackgroundColor(), upperTimeType.getSecondLevelType(), countryId, timeTypeDTO.getActivityCanBeCopiedForOrganizationHierarchy());
            timeType.setCountryId(countryId);
            timeType.setUpperLevelTimeTypeId(timeTypeDTO.getUpperLevelTimeTypeId());
            initializeTimeTypeSettings(timeType,countryId);
            timeType = timeTypeMongoRepository.save(timeType);
            if (timeTypeDTO.getUpperLevelTimeTypeId() != null) {
                upperTimeType.getChildTimeTypeIds().add(timeType.getId());
                upperTimeType.setLeafNode(false);
                initializeTimeTypeSettings(timeType,countryId);
                timeTypeMongoRepository.save(upperTimeType);
            }
            timeTypeDTO.setId(timeType.getId());
        }
    }

    public TimeTypeDTO updateTimeType(TimeTypeDTO timeTypeDTO, Long countryId) {
        TimeType timeType = getAndValidateTimeType(timeTypeDTO, countryId);
        List<TimeType> timeTypes = new ArrayList<>();
        List<TimeType> childTimeTypes = timeTypeMongoRepository.findAllChildByParentId(timeType.getId(), countryId);
        Map<BigInteger, List<TimeType>> childTimeTypesMap = childTimeTypes.stream().collect(Collectors.groupingBy(t -> t.getUpperLevelTimeTypeId(), Collectors.toList()));
        List<BigInteger> childTimeTypeIds = childTimeTypes.stream().map(timetype -> timetype.getId()).collect(Collectors.toList());
        List<TimeType> leafTimeTypes = timeTypeMongoRepository.findAllChildTimeTypeByParentId(childTimeTypeIds);
        Map<BigInteger, List<TimeType>> leafTimeTypesMap = leafTimeTypes.stream().collect(Collectors.groupingBy(timetype -> timetype.getUpperLevelTimeTypeId(), Collectors.toList()));
        organizationActivityService.updateBackgroundColorInShifts(timeTypeDTO, timeType.getBackgroundColor(),timeType.getId());
        PriorityFor oldPriorityFor = timeType.getPriorityFor();
        updateDetailsTimeType(timeTypeDTO, timeType);
        updateOrganizationHierarchyDetailsInTimeType(timeTypeDTO, timeType);
        List<TimeType> childTimeTypeList = childTimeTypesMap.get(timeTypeDTO.getId());
        if (Optional.ofNullable(childTimeTypeList).isPresent()) {
            setPropertiesInChildren(timeTypeDTO, timeType, timeTypes, leafTimeTypesMap, childTimeTypeList);
            timeTypes.addAll(childTimeTypeList);
        }
        timeTypes.add(timeType);
        if (timeType.isLeafNode()) {
            activityCategoryService.updateActivityCategoryForTimeType(countryId, timeType);
        }
        timeTypeMongoRepository.saveEntities(timeTypes);
        if(isCollectionEmpty(childTimeTypeIds) && !oldPriorityFor.equals(timeType.getPriorityFor())){
            activityRankingService.updateRankingListOnUpdatePriorityFor(timeType.getId(), timeType.getPriorityFor(), oldPriorityFor);
        }
        return timeTypeDTO;
    }

    private TimeType getAndValidateTimeType(TimeTypeDTO timeTypeDTO, Long countryId) {
        boolean timeTypesExists = timeTypeMongoRepository.timeTypeAlreadyExistsByLabelAndCountryId(timeTypeDTO.getId(), timeTypeDTO.getLabel(), countryId);
        if (timeTypesExists) {
            exceptionService.duplicateDataException(MESSAGE_TIMETYPE_NAME_ALREADYEXIST);
        }

        TimeType timeType = timeTypeMongoRepository.findOneById(timeTypeDTO.getId());
        if (!Optional.ofNullable(timeType).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TIMETYPE_NOTFOUND);
        }
        if (timeType.getUpperLevelTimeTypeId() == null && !timeType.getLabel().equalsIgnoreCase(timeTypeDTO.getLabel())) {
            //User Cannot Update NAME for TimeTypes of Second Level
            exceptionService.actionNotPermittedException(MESSAGE_TIMETYPE_RENAME_NOTALLOWED, timeType.getLabel());
        }
        return timeType;
    }



    private void updateOrganizationHierarchyDetailsInTimeType(TimeTypeDTO timeTypeDTO, TimeType timeType) {
        Set<OrganizationHierarchy> activityCanBeCopiedForOrganizationHierarchy = timeTypeDTO.getActivityCanBeCopiedForOrganizationHierarchy();
        if (isCollectionNotEmpty(activityCanBeCopiedForOrganizationHierarchy)) {
            if (activityCanBeCopiedForOrganizationHierarchy.size() == 1 && activityCanBeCopiedForOrganizationHierarchy.contains(OrganizationHierarchy.UNIT)) { //user cannot allow copy acitivity for Unit, without allowing copy activity for Organization
                exceptionService.actionNotPermittedException("message.timetype.copy.activity.withoutOrganization.notAllowed");
            }
            timeType.setActivityCanBeCopiedForOrganizationHierarchy(activityCanBeCopiedForOrganizationHierarchy);
        } else {
            timeType.setActivityCanBeCopiedForOrganizationHierarchy(Collections.emptySet());
        }
    }

    private void updateDetailsTimeType(TimeTypeDTO timeTypeDTO, TimeType timeType) {
        timeType.setLabel(timeTypeDTO.getLabel());
        timeType.setDescription(timeTypeDTO.getDescription());
        timeType.setBackgroundColor(timeTypeDTO.getBackgroundColor());
        timeType.setPartOfTeam(timeTypeDTO.isPartOfTeam());
        timeType.setPriorityFor(timeTypeDTO.getPriorityFor());
        timeType.setAllowedConflicts(timeTypeDTO.isAllowedConflicts());
        timeType.setAllowChildActivities(timeTypeDTO.isAllowChildActivities());
        timeType.setBreakNotHeldValid(timeTypeDTO.isBreakNotHeldValid());
        timeType.setSicknessSettingValid(timeTypeDTO.isSicknessSettingValid());
        timeType.setUnityActivitySetting(timeTypeDTO.getUnityActivitySetting());
    }


    private void setPropertiesInChildren(TimeTypeDTO timeTypeDTO, TimeType timeType, List<TimeType> timeTypes, Map<BigInteger, List<TimeType>> leafTimeTypesMap, List<TimeType> childTimeTypeList) {
        boolean partOfTeamUpdated = false;
        boolean allowedChildActivityUpdated = false;
        boolean allowedConflictsUpdate = false;
        boolean sicknessSettingUpdate  = false;
        for (TimeType childTimeType : childTimeTypeList) {
            organizationActivityService.updateBackgroundColorInShifts(timeTypeDTO, childTimeType.getBackgroundColor(),childTimeType.getId());
            partOfTeamUpdated = isPartOfTeamUpdated(timeTypeDTO, partOfTeamUpdated, childTimeType);
            allowedChildActivityUpdated = isAllowedChildActivityUpdated(timeTypeDTO, allowedChildActivityUpdated, childTimeType);
            allowedConflictsUpdate = isAllowedConflictsUpdate(timeTypeDTO, allowedConflictsUpdate, childTimeType);
            sicknessSettingUpdate=isSicknessUpdated(timeTypeDTO,sicknessSettingUpdate,childTimeType);
            childTimeType.setBackgroundColor(timeTypeDTO.getBackgroundColor());
            List<TimeType> leafTimeTypeList = leafTimeTypesMap.get(childTimeType.getId());
            if (Optional.ofNullable(leafTimeTypeList).isPresent()) {
                setPropertiesInLeafTimeTypes(timeTypeDTO, timeType, leafTimeTypeList, partOfTeamUpdated, allowedChildActivityUpdated, allowedConflictsUpdate, childTimeType,sicknessSettingUpdate);
                timeTypes.addAll(leafTimeTypeList);
            }
        }
    }

    private boolean isAllowedConflictsUpdate(TimeTypeDTO timeTypeDTO, boolean allowedConflictsUpdate, TimeType childTimeType) {
        if (childTimeType.isAllowedConflicts() != timeTypeDTO.isAllowedConflicts() && childTimeType.getChildTimeTypeIds().isEmpty()) {
            childTimeType.setAllowedConflicts(timeTypeDTO.isAllowedConflicts());
            allowedConflictsUpdate = true;
        }
        return allowedConflictsUpdate;
    }

    private boolean isAllowedChildActivityUpdated(TimeTypeDTO timeTypeDTO, boolean allowedChildActivityUpdated, TimeType childTimeType) {
        if (childTimeType.isAllowChildActivities() != timeTypeDTO.isAllowChildActivities() && childTimeType.getChildTimeTypeIds().isEmpty()) {
            childTimeType.setAllowChildActivities(timeTypeDTO.isAllowChildActivities());
            allowedChildActivityUpdated = true;
        }
        return allowedChildActivityUpdated;
    }

    private boolean isPartOfTeamUpdated(TimeTypeDTO timeTypeDTO, boolean partOfTeamUpdated, TimeType childTimeType) {
        if (childTimeType.isPartOfTeam() != timeTypeDTO.isPartOfTeam() && childTimeType.getChildTimeTypeIds().isEmpty()) {
            childTimeType.setPartOfTeam(timeTypeDTO.isPartOfTeam());
            partOfTeamUpdated = true;
        }
        return partOfTeamUpdated;
    }

    private boolean isSicknessUpdated(TimeTypeDTO timeTypeDTO, boolean sicknessUpdated, TimeType childTimeType) {
        if (childTimeType.isSicknessSettingValid() != timeTypeDTO.isSicknessSettingValid() && childTimeType.getChildTimeTypeIds().isEmpty()) {
            childTimeType.setSicknessSettingValid(timeTypeDTO.isSicknessSettingValid());
            sicknessUpdated = true;
        }
        return sicknessUpdated;
    }

    private void setPropertiesInLeafTimeTypes(TimeTypeDTO timeTypeDTO, TimeType timeType, List<TimeType> childTimeTypeList, boolean partOfTeamUpdated, boolean allowedChildActivityUpdated, boolean allowedConflictsUpdate, TimeType childTimeType,boolean sicknessSettingUpdate) {
        for (TimeType leafTimeType : childTimeTypeList) {
            organizationActivityService.updateBackgroundColorInShifts(timeTypeDTO, leafTimeType.getBackgroundColor(),leafTimeType.getId());
            leafTimeType.setBackgroundColor(timeTypeDTO.getBackgroundColor());
            if (leafTimeType.isPartOfTeam() != timeTypeDTO.isPartOfTeam() && !partOfTeamUpdated && timeType.getUpperLevelTimeTypeId() != null) {
                childTimeType.setPartOfTeam(timeTypeDTO.isPartOfTeam());
            }
            if (leafTimeType.isAllowChildActivities() != timeTypeDTO.isAllowChildActivities() && !allowedChildActivityUpdated && timeType.getUpperLevelTimeTypeId() != null) {
                childTimeType.setAllowChildActivities(timeTypeDTO.isAllowChildActivities());
            }
            if (leafTimeType.isAllowedConflicts() != timeTypeDTO.isAllowedConflicts() && !allowedConflictsUpdate && timeType.getUpperLevelTimeTypeId() != null) {
                childTimeType.setAllowedConflicts(timeTypeDTO.isAllowedConflicts());
            }
            if (leafTimeType.isSicknessSettingValid() != timeTypeDTO.isSicknessSettingValid() && !sicknessSettingUpdate && timeType.getUpperLevelTimeTypeId() != null) {
                childTimeType.setSicknessSettingValid(timeTypeDTO.isSicknessSettingValid());
            }
        }
    }

    public List<TimeTypeDTO> getAllTimeType(BigInteger timeTypeId, Long countryId) {
        List<TimeTypeDTO> topLevelTimeTypes = timeTypeMongoRepository.getTopLevelTimeType(countryId);
        List<TimeTypeDTO> timeTypeDTOS = new ArrayList<>(2);
        TimeTypeDTO workingTimeTypeDTO = new TimeTypeDTO(TimeTypes.WORKING_TYPE.toValue(), AppConstants.WORKING_TYPE_COLOR);
        TimeTypeDTO nonWorkingTimeTypeDTO = new TimeTypeDTO(TimeTypes.NON_WORKING_TYPE.toValue(), AppConstants.NON_WORKING_TYPE_COLOR);
        if (topLevelTimeTypes.isEmpty()) {
            timeTypeDTOS.add(workingTimeTypeDTO);
            timeTypeDTOS.add(nonWorkingTimeTypeDTO);
            return timeTypeDTOS;
        }
        List<TimeTypeDTO> timeTypes = timeTypeMongoRepository.findAllLowerLevelTimeType(countryId);
        List<TimeTypeDTO> parentOfWorkingTimeType = new ArrayList<>();
        List<TimeTypeDTO> parentOfNonWorkingTimeType = new ArrayList<>();
        for (TimeTypeDTO timeType : topLevelTimeTypes) {
            updateChildTimeTypeDetailsBeforeResponse(timeTypeId, timeTypes, parentOfWorkingTimeType, parentOfNonWorkingTimeType, timeType);
        }
        workingTimeTypeDTO.setChildren(parentOfWorkingTimeType);
        nonWorkingTimeTypeDTO.setChildren(parentOfNonWorkingTimeType);
        timeTypeDTOS.add(workingTimeTypeDTO);
        timeTypeDTOS.add(nonWorkingTimeTypeDTO);
        return timeTypeDTOS;
    }

    private void updateChildTimeTypeDetailsBeforeResponse(BigInteger timeTypeId, List<TimeTypeDTO> timeTypes, List<TimeTypeDTO> parentOfWorkingTimeType, List<TimeTypeDTO> parentOfNonWorkingTimeType, TimeTypeDTO timeType) {
        TimeTypeDTO timeTypeDTO = timeType;
        timeTypeDTO.setSecondLevelType(timeType.getSecondLevelType());
        if ( timeType.getId().equals(timeTypeId)) {
            timeTypeDTO.setSelected(true);
        }
        timeTypeDTO.setTimeTypes(timeType.getTimeTypes());
        timeTypeDTO.setChildren(getLowerLevelTimeTypeDTOs(timeTypeId, timeType.getId(), timeTypes));
        if (TimeTypes.WORKING_TYPE.toString().equals(timeType.getTimeTypes())) {
            parentOfWorkingTimeType.add(timeTypeDTO);
        } else {
            parentOfNonWorkingTimeType.add(timeTypeDTO);
        }
    }

    public Map<String, List<TimeType>> getPresenceAbsenceTimeType(Long countryId) {
        List<TimeType> timeTypes = timeTypeMongoRepository.findAllTimeTypeByCountryId(countryId);
        Map<BigInteger, List<TimeType>> timeTypeMap = timeTypes.stream().filter(t -> t.getUpperLevelTimeTypeId() != null).collect(Collectors.groupingBy(TimeType::getUpperLevelTimeTypeId, Collectors.toList()));
        Map<String, List<TimeType>> presenceAbsenceTimeTypeMap = new HashMap<>();
        timeTypes.forEach(t -> {
            if (t.getLabel().equals(PRESENCE)) {
                List<TimeType> presenceTimeTypes = getChildOfTimeType(t, timeTypeMap);
                presenceTimeTypes.add(t);
                presenceAbsenceTimeTypeMap.put(PRESENCE, presenceTimeTypes);
            } else if (t.getLabel().equals(ABSENCE)) {
                List<TimeType> absenceTimeTypes = getChildOfTimeType(t, timeTypeMap);
                absenceTimeTypes.add(t);
                presenceAbsenceTimeTypeMap.put(ABSENCE, absenceTimeTypes);
            }
        });
        return presenceAbsenceTimeTypeMap;
    }

    private List<TimeType> getChildOfTimeType(TimeType timeType, Map<BigInteger, List<TimeType>> timeTypeMap) {
        List<TimeType> timeTypes = new ArrayList<>();
        List<TimeType> secondLevelTimeTypes = null;
        secondLevelTimeTypes = timeTypeMap.get(timeType.getId());
        timeTypes.addAll(secondLevelTimeTypes);
        if (secondLevelTimeTypes != null) {
            secondLevelTimeTypes.forEach(t -> {
                List<TimeType> leaftimeTypes = timeTypeMap.get(t.getId());
                if (leaftimeTypes != null) {
                    timeTypes.addAll(leaftimeTypes);
                }
            });
        }
        return timeTypes;
    }

    public List<TimeTypeDTO> getAllTimeTypeByCountryId(Long countryId) {
        List<TimeType> timeTypes = timeTypeMongoRepository.findAllTimeTypeByCountryId(countryId);
        List<TimeTypeDTO> timeTypeDTOS = new ArrayList<>(timeTypes.size());
        timeTypes.forEach(t -> {
            TimeTypeDTO timeTypeDTO = new TimeTypeDTO(t.getId(), t.getTimeTypes().toValue(), t.getUpperLevelTimeTypeId());
            timeTypeDTO.setLabel(t.getLabel());
            timeTypeDTOS.add(timeTypeDTO);
        });
        return timeTypeDTOS;
    }

    private List<TimeTypeDTO> getLowerLevelTimeTypeDTOs(BigInteger timeTypeId, BigInteger upperlevelTimeTypeId, List<TimeTypeDTO> timeTypes) {
        List<TimeTypeDTO> lowerLevelTimeTypeDTOS = new ArrayList<>();
        timeTypes.forEach(timeType -> {
            if (timeType.getUpperLevelTimeTypeId().equals(upperlevelTimeTypeId)) {
                TimeTypeDTO levelTwoTimeTypeDTO = timeType;
                if (timeTypeId != null && timeType.getId().equals(timeTypeId)) {
                    levelTwoTimeTypeDTO.setSelected(true);
                }
                levelTwoTimeTypeDTO.setSecondLevelType(timeType.getSecondLevelType());
                levelTwoTimeTypeDTO.setTimeTypes(timeType.getTimeTypes());
                levelTwoTimeTypeDTO.setChildren(getLowerLevelTimeTypeDTOs(timeTypeId, timeType.getId(), timeTypes));
                levelTwoTimeTypeDTO.setUpperLevelTimeTypeId(upperlevelTimeTypeId);
                lowerLevelTimeTypeDTOS.add(levelTwoTimeTypeDTO);
            }
        });
        return lowerLevelTimeTypeDTOS;
    }

    public boolean deleteTimeType(BigInteger timeTypeId, Long countryId) {
        List<Activity> activity = activityMongoRepository.findAllByTimeTypeId(timeTypeId);
        List<TimeType> timeTypes = timeTypeMongoRepository.findAllChildByParentId(timeTypeId, countryId);
        boolean reasonCodeExists = reasonCodeService.anyReasonCodeLinkedWithTimeType(timeTypeId);
        if (reasonCodeExists) {
            exceptionService.actionNotPermittedException(MESSAGE_TIMETYPE_LINKED_REASON_CODE);
        }
        if (activity.isEmpty() && timeTypes.isEmpty()) {
            TimeType timeType = timeTypeMongoRepository.findOne(timeTypeId);
            if (timeType != null && timeType.getUpperLevelTimeTypeId() == null) {
                //User Cannot Delete TimeType of Second Level
                exceptionService.actionNotPermittedException(MESSAGE_TIMETYPE_DELETION_NOTALLOWED, timeType.getLabel());
            } else if(isNotNull(timeType)){
                activityCategoryService.removeTimeTypeRelatedCategory(countryId, timeTypeId);
                timeType.setDeleted(true);
                timeTypeMongoRepository.save(timeType);
            }
        } else exceptionService.timeTypeLinkedException(MESSAGE_TIMETYPE_LINKED);
        return true;
    }


    public Boolean createDefaultTimeTypes(Long countryId) {
        List<TimeType> allTimeTypes = new ArrayList<>();
        List<TimeType> workingTimeTypes = new ArrayList<>();
        getWorkingTimeType(countryId, workingTimeTypes);

        List<TimeType> nonWorkingTimeTypes = getNonWorkingTimeTypeTimeTypes(countryId);
        allTimeTypes.addAll(workingTimeTypes);
        allTimeTypes.addAll(nonWorkingTimeTypes);

        timeTypeMongoRepository.saveEntities(allTimeTypes);

        return true;
    }

    private void getWorkingTimeType(Long countryId, List<TimeType> workingTimeTypes) {
        TimeType presenceTimeType = new TimeType(TimeTypes.WORKING_TYPE, PRESENCE, "", AppConstants.WORKING_TYPE_COLOR, TimeTypeEnum.PRESENCE, countryId, Collections.emptySet());
        TimeType absenceTimeType = new TimeType(TimeTypes.WORKING_TYPE, ABSENCE, "", AppConstants.WORKING_TYPE_COLOR, TimeTypeEnum.ABSENCE, countryId, Collections.emptySet());
        TimeType breakTimeType = new TimeType(TimeTypes.WORKING_TYPE, "Paid Break", "", AppConstants.WORKING_TYPE_COLOR, PAID_BREAK, countryId, Collections.emptySet());
        workingTimeTypes.add(presenceTimeType);
        workingTimeTypes.add(absenceTimeType);
        workingTimeTypes.add(breakTimeType);
    }

    private List<TimeType> getNonWorkingTimeTypeTimeTypes(Long countryId) {
        List<TimeType> nonWorkingTimeTypes = new ArrayList<>();
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Volunteer Time", "", AppConstants.NON_WORKING_TYPE_COLOR, VOLUNTEER, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Timebank Off Time", "", AppConstants.NON_WORKING_TYPE_COLOR, TIME_BANK, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Unpaid Break", "", AppConstants.NON_WORKING_TYPE_COLOR, UNPAID_BREAK, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Time between Split Shifts", "", AppConstants.NON_WORKING_TYPE_COLOR, SHIFT_SPLIT_TIME, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Duty-free, Self-Paid", "", AppConstants.NON_WORKING_TYPE_COLOR, SELF_PAID, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Planned Sickness on Freedays", "", AppConstants.NON_WORKING_TYPE_COLOR, PLANNED_SICK_ON_FREE_DAYS, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Unavailable Time", "", AppConstants.NON_WORKING_TYPE_COLOR, UNAVAILABLE_TIME, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Resting Time", "", AppConstants.NON_WORKING_TYPE_COLOR, RESTING_TIME, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Veto", "", AppConstants.NON_WORKING_TYPE_COLOR, VETO, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Stopbrick", "", AppConstants.NON_WORKING_TYPE_COLOR, STOP_BRICK, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Available Time", "", AppConstants.NON_WORKING_TYPE_COLOR, AVAILABLE_TIME, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Protected Days off", "", AppConstants.NON_WORKING_TYPE_COLOR, GAP, countryId, Collections.emptySet()));
        nonWorkingTimeTypes.add(new TimeType(TimeTypes.NON_WORKING_TYPE, "Gap", "", AppConstants.NON_WORKING_TYPE_COLOR, VOLUNTEER, countryId, Collections.emptySet()));
        return nonWorkingTimeTypes;
    }


    public List<TimeType> getAllTimeTypesByCountryId(Long countryId) {
        return timeTypeMongoRepository.findAllTimeTypeByCountryId(countryId);
    }


    public Boolean existsByIdAndCountryId(BigInteger id, Long countryId) {
        return timeTypeMongoRepository.existsByIdAndCountryIdAndDeletedFalse(id, countryId);
    }

    public Map<BigInteger,TimeTypeDTO> getAllTimeTypeWithItsLowerLevel(Long countryId, Collection<BigInteger> timeTypeIds){
        List<TimeTypeDTO> timeTypeDTOS =  getAllTimeType(null,countryId);
        Map<BigInteger,TimeTypeDTO> resultTimeTypeDTOS = new HashMap<>();
        updateTimeTypeList(resultTimeTypeDTOS,timeTypeDTOS.get(0).getChildren(), timeTypeIds,false);
        updateTimeTypeList(resultTimeTypeDTOS,timeTypeDTOS.get(1).getChildren(), timeTypeIds,false);
        return resultTimeTypeDTOS;
    }

    private void updateTimeTypeList(Map<BigInteger,TimeTypeDTO> resultTimeTypeDTOS, List<TimeTypeDTO> timeTypeDTOS, Collection<BigInteger> timeTypeIds, boolean addAllLowerLevelChildren){
        for(TimeTypeDTO timeTypeDTO : timeTypeDTOS) {
            if(timeTypeIds.contains(timeTypeDTO.getId())){
                resultTimeTypeDTOS.put(timeTypeDTO.getId(),timeTypeDTO);
                if(isCollectionNotEmpty(timeTypeDTO.getChildren())){
                    updateTimeTypeList(resultTimeTypeDTOS,timeTypeDTO.getChildren(), timeTypeIds,true);
                }
            }else if(addAllLowerLevelChildren){
                if(isCollectionNotEmpty(timeTypeDTO.getChildren())) {
                    updateTimeTypeList(resultTimeTypeDTOS, timeTypeDTO.getChildren(), timeTypeIds, true);
                }else{
                    resultTimeTypeDTOS.put(timeTypeDTO.getId(),timeTypeDTO);
                }
            }else{
                updateTimeTypeList(resultTimeTypeDTOS, timeTypeDTO.getChildren(), timeTypeIds, false);
            }
        }
    }

    public TimeCalculationActivityDTO updateTimeCalculationTabOfTimeType(TimeCalculationActivityDTO timeCalculationActivityDTO, BigInteger timeTypeId) {
        ActivityTimeCalculationSettings activityTimeCalculationSettings = ObjectMapperUtils.copyPropertiesByMapper(timeCalculationActivityDTO, ActivityTimeCalculationSettings.class);
        TimeType timeType = timeTypeMongoRepository.findOne(timeTypeId);
        if (!Optional.ofNullable(timeType).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TIMETYPE_NOTFOUND, timeTypeId);
        }
        if (!timeCalculationActivityDTO.isAvailableAllowActivity()) {
            timeType.setActivityTimeCalculationSettings(activityTimeCalculationSettings);
            if (!activityTimeCalculationSettings.getMethodForCalculatingTime().equals(CommonConstants.FULL_WEEK)) {
                activityTimeCalculationSettings.setDayTypes(timeType.getActivityRulesSettings().getDayTypes());
            }
            timeTypeMongoRepository.save(timeType);
        }
        return timeCalculationActivityDTO;
    }

    public ActivitySettingsWrapper getTimeCalculationTabOfTimeType(BigInteger timeTypeId, Long countryId) {
        List<DayTypeDTO> dayTypes = dayTypeService.getDayTypeWithCountryHolidayCalender(countryId);
        TimeType timeType = timeTypeMongoRepository.findOne(timeTypeId);
        ActivityTimeCalculationSettings activityTimeCalculationSettings = timeType.getActivityTimeCalculationSettings();
        List<BigInteger> rulesTabDayTypes = timeType.getActivityRulesSettings().getDayTypes();
        return new ActivitySettingsWrapper(activityTimeCalculationSettings, dayTypes, rulesTabDayTypes);
    }

    public ActivitySettingsWrapper updateRulesTab(ActivityRulesSettingsDTO rulesActivityDTO, BigInteger timeTypeId) {
        organizationActivityService.validateActivityTimeRules(rulesActivityDTO.getShortestTime(), rulesActivityDTO.getLongestTime());
        ActivityRulesSettings activityRulesSettings = ObjectMapperUtils.copyPropertiesByMapper(rulesActivityDTO, ActivityRulesSettings.class);
        TimeType timeType = timeTypeMongoRepository.findOne(timeTypeId);
        if (!Optional.ofNullable(timeType).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TIMETYPE_NOTFOUND, timeTypeId);
        }
        if (rulesActivityDTO.getCutOffIntervalUnit() != null && rulesActivityDTO.getCutOffStartFrom() != null) {
            if (CutOffIntervalUnit.DAYS.equals(rulesActivityDTO.getCutOffIntervalUnit()) && rulesActivityDTO.getCutOffdayValue() == 0) {
                exceptionService.invalidRequestException(ERROR_DAYVALUE_ZERO);
            }
            PlanningPeriod planningPeriod = planningPeriodService.getLastPlanningPeriod(UserContext.getUserDetails().getLastSelectedOrganizationId());
            List<CutOffInterval> cutOffIntervals = getCutoffInterval(rulesActivityDTO.getCutOffStartFrom(), rulesActivityDTO.getCutOffIntervalUnit(), rulesActivityDTO.getCutOffdayValue(),planningPeriod.getEndDate());
            activityRulesSettings.setCutOffIntervals(cutOffIntervals);
            rulesActivityDTO.setCutOffIntervals(cutOffIntervals);
        }
        timeType.setActivityRulesSettings(activityRulesSettings);
        if (!timeType.getActivityTimeCalculationSettings().getMethodForCalculatingTime().equals(CommonConstants.FULL_WEEK)) {
            timeType.getActivityTimeCalculationSettings().setDayTypes(timeType.getActivityRulesSettings().getDayTypes());
        }
        activityHelperService.updateColorInActivity(new TimeTypeDTO(timeType.getBackgroundColor(),rulesActivityDTO.isSicknessSettingValid(),rulesActivityDTO),timeTypeId);
        timeTypeMongoRepository.save(timeType);
        return new ActivitySettingsWrapper(activityRulesSettings);
    }

    public ActivitySettingsWrapper getPhaseSettingTabOfTimeType(BigInteger timeTypeId, Long countryId) {
        TimeType timeType = timeTypeMongoRepository.findOne(timeTypeId);
        if (!Optional.ofNullable(timeType).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TIMETYPE_NOTFOUND, timeTypeId);
        }
        DayTypeEmploymentTypeWrapper dayTypeEmploymentTypeWrapper = userIntegrationService.getDayTypesAndEmploymentTypes(countryId);
        List<DayTypeDTO> dayTypes = dayTypeEmploymentTypeWrapper.getDayTypes();
        List<EmploymentTypeDTO> employmentTypeDTOS = dayTypeEmploymentTypeWrapper.getEmploymentTypes();
        Set<AccessGroupRole> roles = AccessGroupRole.getAllRoles();
        ActivityPhaseSettings activityPhaseSettings = timeType.getActivityPhaseSettings();
        return new ActivitySettingsWrapper(roles, activityPhaseSettings, dayTypes, employmentTypeDTOS);
    }

    public ActivityPhaseSettings updatePhaseSettingTab(ActivityPhaseSettings activityPhaseSettings, BigInteger timeTypeId) {
        TimeType timeType = timeTypeMongoRepository.findOne(timeTypeId);
        if (!Optional.ofNullable(timeType).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TIMETYPE_NOTFOUND, activityPhaseSettings.getActivityId());
        }
        timeType.setActivityPhaseSettings(activityPhaseSettings);
        timeTypeMongoRepository.save(timeType);
        return activityPhaseSettings;
    }

    public ActivitySettingsWrapper getRulesTabOfTimeType(BigInteger timeTypeId, Long countryId) {
        DayTypeEmploymentTypeWrapper dayTypeEmploymentTypeWrapper = userIntegrationService.getDayTypesAndEmploymentTypes(countryId);
        List<DayTypeDTO> dayTypes = dayTypeEmploymentTypeWrapper.getDayTypes();
        List<EmploymentTypeDTO> employmentTypeDTOS = dayTypeEmploymentTypeWrapper.getEmploymentTypes();
        TimeType timeType = timeTypeMongoRepository.findOne(timeTypeId);
        ActivityRulesSettings activityRulesSettings = timeType.getActivityRulesSettings();
        return new ActivitySettingsWrapper(activityRulesSettings, dayTypes, employmentTypeDTOS);
    }


    public ActivitySettingsWrapper updateSkillTabOfTimeType(SkillActivityDTO skillActivityDTO, BigInteger timeTypeId) {
        TimeType timeType = timeTypeMongoRepository.findOne(timeTypeId);
        if (!Optional.ofNullable(timeType).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TIMETYPE_NOTFOUND, skillActivityDTO.getActivityId());
        }
        ActivitySkillSettings activitySkillSettings = new ActivitySkillSettings(skillActivityDTO.getActivitySkills());
        timeType.setActivitySkillSettings(activitySkillSettings);
        timeTypeMongoRepository.save(timeType);
        return new ActivitySettingsWrapper(activitySkillSettings);
    }

    public ActivitySettingsWrapper getSkillTabOfTimeType(BigInteger timeTypeId) {
        TimeType timeType = timeTypeMongoRepository.findOne(timeTypeId);
        return new ActivitySettingsWrapper(timeType.getActivitySkillSettings());
    }

    public void updateOrgMappingDetailOfActivity(OrganizationMappingDTO organizationMappingDTO, BigInteger timeTypeId) {
        TimeType timeType = timeTypeMongoRepository.findOne(timeTypeId);
        if (!Optional.ofNullable(timeType).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TIMETYPE_NOTFOUND, timeTypeId);
        }
        boolean isSuccess = userIntegrationService.verifyOrganizationExpertizeAndRegions(organizationMappingDTO);
        if (!isSuccess) {
            exceptionService.dataNotFoundException(MESSAGE_PARAMETERS_INCORRECT);
        }
        timeType.setRegions(organizationMappingDTO.getRegions());
        timeType.setExpertises(organizationMappingDTO.getExpertises());
        timeType.setOrganizationSubTypes(organizationMappingDTO.getOrganizationSubTypes());
        timeType.setOrganizationTypes(organizationMappingDTO.getOrganizationTypes());
        timeType.setLevels(organizationMappingDTO.getLevel());
        timeType.setEmploymentTypes(organizationMappingDTO.getEmploymentTypes());
        timeTypeMongoRepository.save(timeType);
    }

    public OrganizationMappingDTO getOrgMappingDetailOfTimeType(BigInteger timeTypeId) {
        TimeType timeType = timeTypeMongoRepository.findOne(timeTypeId);
        if (!Optional.ofNullable(timeType).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TIMETYPE_NOTFOUND, timeTypeId);
        }
        OrganizationMappingDTO organizationMappingDTO = new OrganizationMappingDTO();
        organizationMappingDTO.setOrganizationSubTypes(timeType.getOrganizationSubTypes());
        organizationMappingDTO.setExpertises(timeType.getExpertises());
        organizationMappingDTO.setRegions(timeType.getRegions());
        organizationMappingDTO.setLevel(timeType.getLevels());
        organizationMappingDTO.setOrganizationTypes(timeType.getOrganizationTypes());
        organizationMappingDTO.setEmploymentTypes(timeType.getEmploymentTypes());
        return organizationMappingDTO;

    }

    public List<BigInteger> getTimeTypeIdsByTimeTypeEnum(String timeTypeEnum){
        return timeTypeMongoRepository.findAllByDeletedFalseAndTimeType(timeTypeEnum);
    }

    public void getAllLeafTimeTypeByParentTimeTypeIds(Collection<BigInteger> timeTypeIds, List<TimeTypeDTO> leafTimeTypes){
        List<TimeType> timeTypes = timeTypeMongoRepository.findAllByTimeTypeIds(timeTypeIds);
        for (TimeType timeType : timeTypes) {
            if(timeType.isLeafNode()){
                leafTimeTypes.add(ObjectMapperUtils.copyPropertiesByMapper(timeType, TimeTypeDTO.class));
            }else{
                getAllLeafTimeTypeByParentTimeTypeIds(timeType.getChildTimeTypeIds(), leafTimeTypes);
            }
        }
    }

    public Map<String, TranslationInfo> updateTranslation(BigInteger timeTypeId, Map<String,TranslationInfo> translations) {
        TimeType timeType =timeTypeMongoRepository.findOne(timeTypeId);
        timeType.setTranslations(translations);
        timeTypeMongoRepository.save(timeType);
        return timeType.getTranslations();
    }

    public List<TimeType> getAllSickTimeTypes(){
        return timeTypeMongoRepository.findAllSickTimeTypes();
    }

    public TimeType getTimeTypeById(BigInteger timeTypeId){
        return timeTypeMongoRepository.findOne(timeTypeId);
    }

}
