package com.kairos.activity.spec;

import com.kairos.activity.client.dto.staff.StaffAdditionalInfoDTO;
import com.kairos.activity.custom_exception.ActionNotPermittedException;
import com.kairos.activity.custom_exception.DataNotFoundByIdException;
import com.kairos.activity.custom_exception.DataNotFoundException;
import com.kairos.activity.enums.RuleTemplates;
import com.kairos.activity.persistence.model.activity.Activity;
import com.kairos.activity.persistence.model.activity.Shift;
import com.kairos.activity.persistence.model.phase.Phase;
import com.kairos.activity.response.dto.shift.RuleTemplateCategoryDTO;
import com.kairos.activity.response.dto.shift.WTAResponseDTO;
import com.kairos.activity.util.DateUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

import static com.kairos.activity.enums.RuleTemplates.getByTemplateType;
/**
 * Created by vipul on 8/2/18.
 */
public class ActivityWTARulesSpecification extends AbstractActivitySpecification<Activity> {
    Logger logger = LoggerFactory.getLogger(ActivityWTARulesSpecification.class);
    private WTAResponseDTO wtaResponseDTO;
    private Shift shift;
    private Phase phase;
    private StaffAdditionalInfoDTO staffAdditionalInfoDTO;

    public ActivityWTARulesSpecification(WTAResponseDTO wtaResponseDTO, Phase phase, Shift shift, StaffAdditionalInfoDTO staffAdditionalInfoDTO) {
        this.wtaResponseDTO = wtaResponseDTO;
        this.shift = shift;
        this.phase = phase;
        this.staffAdditionalInfoDTO = staffAdditionalInfoDTO;
    }

    @Override
    public boolean isSatisfied(Activity activity) {
        if (wtaResponseDTO.getEndDateMillis()!=null && new DateTime(wtaResponseDTO.getEndDateMillis()).isBefore(shift.getEndDate().getTime())) {
            throw new ActionNotPermittedException("WTA is Expired for unit employment.");
        }
        List<RuleTemplateCategoryDTO> ruleTemplates = wtaResponseDTO.getRuleTemplates();
        for (int i = 0; i < ruleTemplates.size(); i++) {
            RuleTemplateCategoryDTO currentWTARuleTemplate = ruleTemplates.get(i);
            String currentTemplateType=getTemplateType(currentWTARuleTemplate.getTemplateType());
            RuleTemplates tempType=getByTemplateType(currentTemplateType);
            if(!Optional.ofNullable(tempType).isPresent()){
                throw new DataNotFoundException("Template Type not Found");
            }
            switch (tempType) {
                case MAXIMUM_SHIFT_LENGTH: //MaximumShiftLengthWTATemplate
                    if (!currentWTARuleTemplate.getCheckAgainstTimeRules()) {
                        continue;
                    } else {
                        if (!Optional.ofNullable(currentWTARuleTemplate.getPhaseTemplateValues()).isPresent()) {
                            throw new NoSuchFieldError("Rule template matrix Not found");
                        }
                        for (int j = 0; j < currentWTARuleTemplate.getPhaseTemplateValues().size(); j++) {
                            logger.info(currentWTARuleTemplate.getPhaseTemplateValues().get(j).getPhaseName() + "  " + (phase.getName()));
                            if (currentWTARuleTemplate.getPhaseTemplateValues().get(j).getPhaseName().equalsIgnoreCase(phase.getName()) && !currentWTARuleTemplate.getPhaseTemplateValues().get(j).isDisabled()) {
                                Long differenceInMinutes = DateUtils.getDifferenceBetweenDatesInMinute(shift.getStartDate(), shift.getEndDate());
                                if (differenceInMinutes > currentWTARuleTemplate.getPhaseTemplateValues().get(j).getStaffValue())
                                    throw new ActionNotPermittedException("Incorrect Maximum allowed shift length " + currentWTARuleTemplate.getPhaseTemplateValues().get(j).getStaffValue());
                            }
                        }

                    }
                    break;
                case MINIMUM_SHIFT_LENGTH: //MinimumShiftLengthWTATemplate
                    if (!currentWTARuleTemplate.getCheckAgainstTimeRules()) {
                        continue;
                    } else {
                        if (!Optional.ofNullable(currentWTARuleTemplate.getPhaseTemplateValues()).isPresent()) {
                            throw new NoSuchFieldError("Rule template matrix Not found");
                        }
                        for (int j = 0; j < currentWTARuleTemplate.getPhaseTemplateValues().size(); j++) {
                            logger.info(currentWTARuleTemplate.getPhaseTemplateValues().get(j).getPhaseName() + "  " + (phase.getName()));
                            if (currentWTARuleTemplate.getPhaseTemplateValues().get(j).getPhaseName().equalsIgnoreCase(phase.getName()) && !currentWTARuleTemplate.getPhaseTemplateValues().get(j).isDisabled()) {
                                Long differenceInMinutes = DateUtils.getDifferenceBetweenDatesInMinute(shift.getStartDate(), shift.getEndDate());
                                if (differenceInMinutes < currentWTARuleTemplate.getPhaseTemplateValues().get(j).getStaffValue())
                                    throw new ActionNotPermittedException("Incorrect minimum allowed shift length " + currentWTARuleTemplate.getPhaseTemplateValues().get(j).getStaffValue());
                            }
                        }

                    }
                    break;
                case MAXIMUM_NUMBER_OF_CONSECUTIVE_DAYS:
                    break;
                case MINIMUM_REST_AFTER_CONSECUTIVE_DAYS_WORKED:
                    break;
                case MAXIMUM_NIGHT_SHIFTS_LENGTH:  //MaximumNightShiftLengthWTATemplate
//                    if (!currentWTARuleTemplate.getCheckAgainstTimeRules()) {
//                        continue;
//                    } else {
//                        Long differenceInMinutes = DateUtils.getDifferenceBetweenDatesInMinute(shift.getStartDate(), shift.getEndDate());
//                        TimeInterval interval = new TimeInterval(staffAdditionalInfoDTO.getOrganizationNightStartTimeFrom(), staffAdditionalInfoDTO.getOrganizationNightEndTimeTo());
//                        DateTime dateTime = new DateTime();
//                        dateTime.plusMinutes(staffAdditionalInfoDTO.getOrganizationNightStartTimeFrom().intValue());
//                        //Long organizationNightDurationInMinute = DateUtils.getDifferenceBetweenDatesInMinute(staffAdditionalInfoDTO.getOrganizationNightStartTimeFrom(), staffAdditionalInfoDTO.getOrganizationNightEndTimeTo());
                        //if (differenceInMinutes < currentWTARuleTemplate.getPhaseTemplateValues().get(j).getStaffValue())
                        //throw new ActionNotPermittedException("Incorrect minimum allowed shift length " + currentWTARuleTemplate.getPhaseTemplateValues().get(j).getStaffValue());
                    //}
                    break;
                case MINIMUM_NUMBER_OF_CONSECUTIVE_NIGHTS:
                    break;

                case MAXIMUM_NUMBER_OF_CONSECUTIVE_NIGHTS:
                    break;
                case MINIMUM_REST_AFTER_CONSECUTIVE_NIGHTS_WORKED:
                    break;
                case MAXIMUM_NUMBER_OF_WORK_NIGHTS:
                    break;
                case MINIMUM_NUMBER_OF_DAYS_OFF_PER_PERIOD:
                    break;
                case MAXIMUM_AVERAGE_SCHEDULED_TIME_PER_WEEK_WITHIN_AN_INTERVAL:
                    break;
                case MAXIMUM_VETO_PER_PERIOD:
                    break;
                case NUMBER_OF_WEEKEND_SHIFTS_IN_A_PERIOD_COMPARED_TO_AVERAGE:
                    break;
                case CARE_DAYS_CHECK:
                    break;
                case MINIMUM_DAILY_RESTING_TIME:
                    break;
                case MINIMUM_DURATION_BETWEEN_SHIFTS:
                    break;
                case MINIMUM_WEEKLY_REST_PERIOD_FIXED_WEEKS:
                    break;
                case SHORTEST_AND_AVERAGE_DAILY_REST_FIXED_TIMES:
                    break;
                case MAXIMUM_NUMBER_OF_SHIFTS_PER_INTERVAL:
                    break;
                case MAXIMUM_SENIOR_DAYS_PER_YEAR:
                    break;
                case MAXIMUM_TIME_BANK:
                    break;
                case MINIMUM_TIME_BANK:
                    break;
                case BREAKS_IN_SHIFT:
                    break;
                default:
                    throw new DataNotFoundByIdException("Invalid TEMPLATE");
            }
        }
        return true;
    }
    private String getTemplateType(String templateType){
        if(!templateType.contains("_")){
            return templateType;
        }
        int lastCharIndex=templateType.lastIndexOf("_");
        if(lastCharIndex>0){
            char nextCharacter=templateType.charAt(lastCharIndex+1);
            if(!Character.isDigit(templateType.charAt(lastCharIndex+1))){
                return templateType;
            }
            else{
                return templateType.substring(0,lastCharIndex);
            }
        }
        return null;
    }
}
