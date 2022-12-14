package com.kairos.service.staffing_level;

import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.commons.utils.ObjectUtils;
import com.kairos.dto.activity.activity.ActivityValidationError;
import com.kairos.dto.activity.staffing_level.StaffingLevelInterval;
import com.kairos.dto.activity.staffing_level.StaffingLevelTemplateDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.DayTypeDTO;
import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.Day;
import com.kairos.persistence.model.activity.Activity;
import com.kairos.persistence.model.staffing_level.StaffingLevel;
import com.kairos.persistence.model.staffing_level.StaffingLevelTemplate;
import com.kairos.persistence.repository.activity.ActivityMongoRepository;
import com.kairos.persistence.repository.staffing_level.StaffingLevelMongoRepository;
import com.kairos.persistence.repository.staffing_level.StaffingLevelTemplateRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.day_type.DayTypeService;
import com.kairos.service.exception.ExceptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kairos.commons.utils.DateUtils.asDate;
import static com.kairos.constants.ActivityMessagesConstants.*;
import static org.mockito.ArgumentMatchers.isNotNull;

@Service
@Transactional
public class StaffingLevelTemplateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaffingLevelService.class);
    @Inject
    private StaffingLevelTemplateRepository staffingLevelTemplateRepository;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private ActivityMongoRepository activityMongoRepository;
    @Inject private StaffingLevelMongoRepository staffingLevelMongoRepository;
    @Inject
    private DayTypeService dayTypeService;

    /**
     * @param staffingLevelTemplateDTO
     * @return
     */
    public StaffingLevelTemplateDTO createStaffingLevelTemplate(Long unitId,StaffingLevelTemplateDTO staffingLevelTemplateDTO) {
        LOGGER.info("saving staffing level Template  {}", staffingLevelTemplateDTO);
        boolean alreadyExists=staffingLevelTemplateRepository.existsByNameIgnoreCaseAndDeletedFalseAndUnitId(staffingLevelTemplateDTO.getName(),unitId);
        if(alreadyExists){
            exceptionService.duplicateDataException(ERROR_NAME_DUPLICATE,staffingLevelTemplateDTO.getName());
        }
        //validating Activities
        List<ActivityValidationError> errors= validateActivityRules(new HashSet<>(),staffingLevelTemplateDTO,null);
        if(!errors.isEmpty()){
            staffingLevelTemplateDTO.setErrors(errors);
            return staffingLevelTemplateDTO;
        }

        StaffingLevelTemplate staffingLevelTemplate = ObjectMapperUtils.copyPropertiesByMapper(staffingLevelTemplateDTO, StaffingLevelTemplate.class);
        if(ObjectUtils.isNotNull(staffingLevelTemplateDTO.getSelectedDate())){
            StaffingLevel staffingLevel = staffingLevelMongoRepository.findByUnitIdAndCurrentDateAndDeletedFalse(unitId,asDate(staffingLevelTemplateDTO.getSelectedDate()));
            staffingLevelTemplate.setPresenceStaffingLevelInterval(staffingLevel.getPresenceStaffingLevelInterval());
            staffingLevelTemplate.setStaffingLevelSetting(staffingLevel.getStaffingLevelSetting());
            removeLogsFromTemplate(staffingLevelTemplate);
        }
        staffingLevelTemplateRepository.save(staffingLevelTemplate);
        staffingLevelTemplate.getPresenceStaffingLevelInterval().sort(Comparator.comparing(staffingLevelInterval -> staffingLevelInterval.getSequence()));
        return ObjectMapperUtils.copyPropertiesByMapper(staffingLevelTemplate,StaffingLevelTemplateDTO.class);

    }

    private void removeLogsFromTemplate(StaffingLevelTemplate staffingLevelTemplate) {
        for (StaffingLevelInterval staffingLevelInterval : staffingLevelTemplate.getPresenceStaffingLevelInterval()) {
            staffingLevelInterval.setStaffingLevelIntervalLogs(new TreeSet<>());
        }
    }
                                                                                                                                                                        
    public StaffingLevelTemplateDTO updateStaffingLevelTemplte(StaffingLevelTemplateDTO staffingLevelTemplateDTO,
                                                           BigInteger staffingTemplateId) {
        LOGGER.info("updating staffing level Template ID={}", staffingTemplateId);

        //validating Activities
        List<ActivityValidationError> errors= validateActivityRules(new HashSet<>(),staffingLevelTemplateDTO,null);
        if(!errors.isEmpty()){
            staffingLevelTemplateDTO.setErrors(errors);
            return staffingLevelTemplateDTO;
        }

        StaffingLevelTemplate staffingLevelTemplate = staffingLevelTemplateRepository.findOne(staffingTemplateId);
        if (Optional.ofNullable(staffingLevelTemplate).isPresent()) {
            BeanUtils.copyProperties(staffingLevelTemplateDTO, staffingLevelTemplate);
            staffingLevelTemplate.setId(staffingTemplateId);
            staffingLevelTemplateRepository.save(staffingLevelTemplate);
            staffingLevelTemplateDTO.setPresenceStaffingLevelInterval(staffingLevelTemplate.getPresenceStaffingLevelInterval().stream()
                    .sorted(Comparator.comparing(StaffingLevelInterval::getSequence)).collect(Collectors.toList()));
            } else {
            exceptionService.dataNotFoundByIdException(MESSAGE_STAFFLEVELTEMPLATE, staffingTemplateId);
        }
        return staffingLevelTemplateDTO;

    }

    public List<StaffingLevelTemplateDTO> getStaffingLevelTemplates(Long unitId, Date proposedDate) {
        if(!Optional.ofNullable(proposedDate).isPresent()){
            return staffingLevelTemplateRepository.findAllByUnitIdAndDeletedFalse(unitId);
        }
        List<DayTypeDTO> dayTypes = dayTypeService.getDayTypeByDate(UserContext.getUserDetails().getCountryId(),proposedDate);
        List<BigInteger> dayTypeIds = dayTypes.stream().map(DayTypeDTO::getId).collect(Collectors.toList());

        Optional<DayTypeDTO> holidayDayType = dayTypes.stream().filter(DayTypeDTO::isHolidayType).findFirst();
        LocalDate localDate = DateUtils.asLocalDate(proposedDate);

        String day = localDate.getDayOfWeek().name();
        Day dayEnum = Day.valueOf(day);
        if(!holidayDayType.isPresent()) {
            return staffingLevelTemplateRepository.                                                                                                                 findByUnitIdAndDayTypeAndDate(unitId, proposedDate, proposedDate, dayTypeIds, Stream.of(dayEnum.toString()).collect(Collectors.toList()));
        }else {
            return staffingLevelTemplateRepository.findByUnitIdAndDayTypeAndDate(unitId,proposedDate, proposedDate, dayTypeIds,null);
        }
        }

    /**
     *
     * @param staffingLevelTemplateDTO
     * @return
     */
    public List<ActivityValidationError> validateActivityRules(Set<BigInteger> activityIds,StaffingLevelTemplateDTO staffingLevelTemplateDTO,List<BigInteger> parentActivityIds){
        if(activityIds.isEmpty()) {
            staffingLevelTemplateDTO.getPresenceStaffingLevelInterval().forEach(staffingLevelInterval -> {
                staffingLevelInterval.getStaffingLevelActivities().forEach(staffingLevelActivity -> {
                    activityIds.add(staffingLevelActivity.getActivityId());
                });
            });
        }

        List<Activity> activities=activityMongoRepository.findAllActivitiesByIds(activityIds);
        List<ActivityValidationError> activityValidationErrors =new ArrayList<>();
        activities.forEach(activity -> {
            if(parentActivityIds!=null && activities.stream().noneMatch(phaseTemplateValue -> phaseTemplateValue.getChildActivityIds().contains(activity.getId()))){
                parentActivityIds.add(activity.getId());
            }
                List<String> errors=new ArrayList<>();
                if(!Optional.ofNullable(staffingLevelTemplateDTO.getValidity().getEndDate()).isPresent()) {
                    if (Optional.ofNullable(activity.getActivityGeneralSettings().getEndDate()).isPresent() &&
                            activity.getActivityGeneralSettings().getEndDate().isBefore(staffingLevelTemplateDTO.getValidity().getStartDate())) {
                        errors.add(exceptionService.getLanguageSpecificText(ACTIVITY_OUT_OF_RANGE, activity.getName()));
                    }
                }else {
                    if(Optional.ofNullable(activity.getActivityGeneralSettings().getEndDate()).isPresent() &&
                            (activity.getActivityGeneralSettings().getEndDate().isBefore(staffingLevelTemplateDTO.getValidity().getStartDate()) ||
                                    activity.getActivityGeneralSettings().getStartDate().isAfter(staffingLevelTemplateDTO.getValidity().getEndDate()))){
                        errors.add(exceptionService.getLanguageSpecificText(ACTIVITY_OUT_OF_RANGE,activity.getName()));
                    } else if(!Optional.ofNullable(activity.getActivityGeneralSettings().getEndDate()).isPresent() &&
                            activity.getActivityGeneralSettings().getStartDate().isAfter(staffingLevelTemplateDTO.getValidity().getEndDate())){
                        errors.add(exceptionService.getLanguageSpecificText(ACTIVITY_OUT_OF_RANGE,activity.getName()));
                    }
                }

                if(!activity.getActivityRulesSettings().isEligibleForStaffingLevel())  {
                    errors.add(exceptionService.getLanguageSpecificText(ACTIVITY_NOT_ELIGIBLE_FOR_STAFFING_LEVEL,activity.getName()));
                }
                if(!CollectionUtils.containsAny(staffingLevelTemplateDTO.getDayType(),activity.getActivityRulesSettings().getDayTypes())){
                    errors.add(exceptionService.getLanguageSpecificText(ACTIVITY_NOT_ELIGIBLE_DAYTYPE,activity.getName()));
                }

                if(!errors.isEmpty()){
                activityValidationErrors.add(new ActivityValidationError(activity.getId(),activity.getName(),activity.getActivityGeneralSettings().getStartDate(),
                        activity.getActivityGeneralSettings().getEndDate(),errors));
            } });
        return activityValidationErrors;

    }

    public boolean deleteStaffingLevelTemplate(BigInteger staffingLevelTemplateId){
       boolean result= staffingLevelTemplateRepository.deleteStaffingLevelTemplate(staffingLevelTemplateId);
       if(!result){
           exceptionService.dataNotFoundException(MESSAGE_DATANOTFOUND,"StaffingLevelTemplate",staffingLevelTemplateId);
       }
        return true;
    }

}
