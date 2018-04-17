package com.kairos.activity.persistence.model.wta.templates;

import com.kairos.activity.custom_exception.DataNotFoundByIdException;
import com.kairos.activity.persistence.enums.WTATemplateType;
import com.kairos.activity.persistence.model.wta.WTAQueryResultDTO;
import com.kairos.activity.persistence.model.wta.WorkingTimeAgreement;
import com.kairos.activity.persistence.model.wta.templates.template_types.*;
import com.kairos.activity.persistence.repository.wta.WTABaseRuleTemplateMongoRepository;
import com.kairos.activity.service.MongoBaseService;
import com.kairos.response.dto.web.wta.WTARuleTemplateDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.kairos.activity.persistence.enums.WTATemplateType.*;

/**
 * @author pradeep
 * @date - 13/4/18
 */

@Service
public class WTABuilderService extends MongoBaseService {

    @Inject private WTABaseRuleTemplateMongoRepository wtaBaseRuleTemplateMongoRepository;

    public void copyRuleTemplates(WTAQueryResultDTO wtaQueryResultDTO, List<WTARuleTemplateDTO> WTARuleTemplateDTOS) {

        List<WTABaseRuleTemplate> wtaBaseRuleTemplates = new ArrayList<>();
        for (WTARuleTemplateDTO ruleTemplate : WTARuleTemplateDTOS) {
            wtaBaseRuleTemplates.add(copyRuleTemplate(ruleTemplate));

        }
       wtaQueryResultDTO.setWtaBaseRuleTemplates(wtaBaseRuleTemplates);

    }

    public WTABaseRuleTemplate copyRuleTemplate(WTARuleTemplateDTO ruleTemplate){
        WTABaseRuleTemplate wtaBaseRuleTemplate = new WTABaseRuleTemplate();
        switch (ruleTemplate.getWtaTemplateType()) {
            case SHIFT_LENGTH:
                ShiftLengthWTATemplate shiftLengthWTATemplate = new ShiftLengthWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,shiftLengthWTATemplate,"id");
                shiftLengthWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = shiftLengthWTATemplate;
                break;
            case CONSECUTIVE_WORKING_PARTOFDAY:
                ConsecutiveWorkWTATemplate consecutiveWorkWTATemplate = new ConsecutiveWorkWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,consecutiveWorkWTATemplate,"id");
                consecutiveWorkWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = consecutiveWorkWTATemplate;
                break;

            case CONSECUTIVE_NIGHTS_AND_DAYS:
                ConsecutiveRestPartOfDayWTATemplate consecutiveRestPartOfDayWTATemplate = new ConsecutiveRestPartOfDayWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,consecutiveRestPartOfDayWTATemplate,"id");
                consecutiveRestPartOfDayWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = consecutiveRestPartOfDayWTATemplate;
                break;
            case REST_IN_CONSECUTIVE_DAYS_AND_NIGHTS:
                ConsecutiveRestPartOfDayWTATemplate consecutiveRestPartOfDayWTATemplate1 = new ConsecutiveRestPartOfDayWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,consecutiveRestPartOfDayWTATemplate1,"id");
                consecutiveRestPartOfDayWTATemplate1.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = consecutiveRestPartOfDayWTATemplate1;
                break;
            case NUMBER_OF_PARTOFDAY:
                NumberOfPartOfDayShiftsWTATemplate numberOfPartOfDayShiftsWTATemplate = new NumberOfPartOfDayShiftsWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,numberOfPartOfDayShiftsWTATemplate,"id");
                numberOfPartOfDayShiftsWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = numberOfPartOfDayShiftsWTATemplate;
                break;
            case DAYS_OFF_IN_PERIOD:
                DaysOffInPeriodWTATemplate daysOffInPeriodWTATemplate = new DaysOffInPeriodWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,daysOffInPeriodWTATemplate,"id");
                daysOffInPeriodWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = daysOffInPeriodWTATemplate;
                break;
            case AVERAGE_SHEDULED_TIME:
                AverageScheduledTimeWTATemplate averageScheduledTimeWTATemplate = new AverageScheduledTimeWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,averageScheduledTimeWTATemplate,"id");
                averageScheduledTimeWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = averageScheduledTimeWTATemplate;
                break;
            case VETO_PER_PERIOD:
                VetoPerPeriodWTATemplate vetoPerPeriodWTATemplate = new VetoPerPeriodWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,vetoPerPeriodWTATemplate,"id");
                vetoPerPeriodWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = vetoPerPeriodWTATemplate;
                break;
            case NUMBER_OF_WEEKEND_SHIFT_IN_PERIOD:
                NumberOfWeekendShiftsInPeriodWTATemplate numberOfWeekendShiftsInPeriodWTATemplate = new NumberOfWeekendShiftsInPeriodWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,numberOfWeekendShiftsInPeriodWTATemplate,"id");
                numberOfWeekendShiftsInPeriodWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = numberOfWeekendShiftsInPeriodWTATemplate;
                break;
            case CARE_DAYS_CHECK:
                CareDayCheckWTATemplate careDayCheckWTATemplate = new CareDayCheckWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,careDayCheckWTATemplate,"id");
                careDayCheckWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = careDayCheckWTATemplate;
                break;
            case DAILY_RESTING_TIME:
                DailyRestingTimeWTATemplate dailyRestingTimeWTATemplate = new DailyRestingTimeWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,dailyRestingTimeWTATemplate,"id");
                dailyRestingTimeWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = dailyRestingTimeWTATemplate;
                break;
            case DURATION_BETWEEN_SHIFTS:
                DurationBetweenShiftsWTATemplate durationBetweenShiftsWTATemplate = new DurationBetweenShiftsWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,durationBetweenShiftsWTATemplate,"id");
                durationBetweenShiftsWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = durationBetweenShiftsWTATemplate;
                break;
            case WEEKLY_REST_PERIOD:
                WeeklyRestPeriodWTATemplate weeklyRestPeriodWTATemplate = new WeeklyRestPeriodWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,weeklyRestPeriodWTATemplate,"id");
                weeklyRestPeriodWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = weeklyRestPeriodWTATemplate;
                break;
            case SHORTEST_AND_AVERAGE_DAILY_REST:
                ShortestAndAverageDailyRestWTATemplate shortestAndAverageDailyRestWTATemplate = new ShortestAndAverageDailyRestWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,shortestAndAverageDailyRestWTATemplate,"id");
                shortestAndAverageDailyRestWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = shortestAndAverageDailyRestWTATemplate;
                break;
            case NUMBER_OF_SHIFTS_IN_INTERVAL:
                ShiftsInIntervalWTATemplate shiftsInIntervalWTATemplate = new ShiftsInIntervalWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,shiftsInIntervalWTATemplate,"id");
                shiftsInIntervalWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = shiftsInIntervalWTATemplate;
                break;
            case MAXIMUM_SENIOR_DAYS_IN_YEAR:
                SeniorDaysInYearWTATemplate seniorDaysInYearWTATemplate = new SeniorDaysInYearWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,seniorDaysInYearWTATemplate,"id");
                seniorDaysInYearWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = seniorDaysInYearWTATemplate;
                break;
            case TIME_BANK:
                TimeBankWTATemplate timeBankWTATemplate = new TimeBankWTATemplate();
                BeanUtils.copyProperties(ruleTemplate,timeBankWTATemplate,"id");
                timeBankWTATemplate.setWTARuleTemplateCategory(ruleTemplate.getRuleTemplateCategory().getId());
                wtaBaseRuleTemplate = timeBankWTATemplate;
                break;

            default:
                throw new DataNotFoundByIdException("Invalid TEMPLATE");
        }
        return wtaBaseRuleTemplate;
    }


    public static void copyWTARuleTemplateToWTA(WorkingTimeAgreement workingTimeAgreement, WTAQueryResultDTO wtaQueryResultDTO){


    }

    /*public static List<T> copyPhaseTemplateValue(List<Object> source,List<Object> phaseTemplateValues) {
        List<T> phases = null;
        if (phaseTemplateValues != null) {
            phases = new ArrayList<>(4);
            for (T phaseTemplateValueDTO : phaseTemplateValues) {
                T newPhaseTemplateValue = new PhaseTemplateValue();
                BeanUtils.copyProperties(phaseTemplateValueDTO,newPhaseTemplateValue);
                phases.add(newPhaseTemplateValue);
            }
        }
        return phases;
    }*/


    public static List<WTARuleTemplateDTO> getRuleTemplateDTO(WorkingTimeAgreement workingTimeAgreement) {
        List<WTARuleTemplateDTO> wtaRuleTemplateDTOS = new ArrayList<>();
        BeanUtils.copyProperties(workingTimeAgreement.getWtaBaseRuleTemplates(),wtaRuleTemplateDTOS);
        return wtaRuleTemplateDTOS;
    }

    public void copyRuleTemplateToNewWTA(WorkingTimeAgreement oldWta,WorkingTimeAgreement newWTA) {
        List<WTABaseRuleTemplate> wtaBaseRuleTemplates = (List<WTABaseRuleTemplate>) wtaBaseRuleTemplateMongoRepository.findAllById(oldWta.getRuleTemplates());
        List<WTABaseRuleTemplate> wtaBaseRuleTemplates1 = new ArrayList<>();
        BeanUtils.copyProperties(wtaBaseRuleTemplates,wtaBaseRuleTemplates1,"id");
        save(wtaBaseRuleTemplates1);
    }




}
