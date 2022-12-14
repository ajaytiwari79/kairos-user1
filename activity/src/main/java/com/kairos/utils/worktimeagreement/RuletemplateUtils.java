package com.kairos.utils.worktimeagreement;

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.TimeInterval;
import com.kairos.dto.activity.activity.ActivityDTO;
import com.kairos.dto.activity.activity.activity_tabs.CutOffInterval;
import com.kairos.dto.activity.activity.activity_tabs.CutOffIntervalUnit;
import com.kairos.dto.activity.cta.CTARuleTemplateDTO;
import com.kairos.dto.activity.shift.*;
import com.kairos.dto.activity.wta.templates.ActivityCareDayCount;
import com.kairos.dto.activity.wta.templates.PhaseTemplateValue;
import com.kairos.dto.user.country.agreement.cta.cta_response.CountryHolidayCalenderDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.DayTypeDTO;
import com.kairos.dto.user.country.time_slot.TimeSlot;
import com.kairos.dto.user.expertise.CareDaysDTO;
import com.kairos.dto.user.user.staff.StaffAdditionalInfoDTO;
import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.Day;
import com.kairos.enums.phase.PhaseDefaultName;
import com.kairos.enums.wta.MinMaxSetting;
import com.kairos.enums.wta.PartOfDay;
import com.kairos.enums.wta.ShiftLengthAndAverageSetting;
import com.kairos.persistence.model.activity.Activity;
import com.kairos.persistence.model.activity.ActivityWrapper;
import com.kairos.persistence.model.wta.templates.WTABaseRuleTemplate;
import com.kairos.persistence.model.wta.templates.template_types.*;
import com.kairos.wrapper.wta.RuleTemplateSpecificInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.*;
import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.ActivityMessagesConstants.*;
import static com.kairos.constants.AppConstants.*;
import static com.kairos.service.shift.ShiftValidatorService.throwException;
import static com.kairos.service.wta.WorkTimeAgreementBalancesCalculationService.isDayTypeValid;
import static org.apache.commons.lang.StringUtils.isEmpty;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RuletemplateUtils {


    public static List<ShiftWithActivityDTO> getShiftsByIntervalAndActivityIds(Activity activity, ShiftWithActivityDTO shiftwithactivitydto, List<ShiftWithActivityDTO> shifts, List<BigInteger> activitieIds, Map<BigInteger, DayTypeDTO> dayTypeMap) {
        List<ShiftWithActivityDTO> updatedShifts = new ArrayList<>();
        LocalDate shiftStartLocalDate = DateUtils.asLocalDate(shiftwithactivitydto.getStartDate());
        Optional<CutOffInterval> cutOffIntervalOptional;
        ActivityDTO activityDTO = shiftwithactivitydto.getActivities().get(0).getActivity();
        if(isNull(activity)){
            cutOffIntervalOptional = activityDTO.getActivityRulesSettings().getCutOffIntervals().stream().filter(interval -> ((interval.getStartDate().isBefore(shiftStartLocalDate) || interval.getStartDate().isEqual(shiftStartLocalDate)) && (interval.getEndDate().isAfter(shiftStartLocalDate) || interval.getEndDate().isEqual(shiftStartLocalDate)))).findAny();
        }else {
            cutOffIntervalOptional = activity.getActivityRulesSettings().getCutOffIntervals().stream().filter(interval -> ((interval.getStartDate().isBefore(shiftStartLocalDate) || interval.getStartDate().isEqual(shiftStartLocalDate)) && (interval.getEndDate().isAfter(shiftStartLocalDate) || interval.getEndDate().isEqual(shiftStartLocalDate)))).findAny();
        }
        if (cutOffIntervalOptional.isPresent()) {
            getShiftsByCutOff(shifts, activitieIds, updatedShifts, cutOffIntervalOptional);
        }
        boolean isDayTypeExist;
        List<ShiftWithActivityDTO> shiftWithActivityDTOS = new ArrayList<>();
        for(ShiftWithActivityDTO shiftWithActivityDTO :updatedShifts){
            for(ShiftActivityDTO shiftActivityDTO :shiftWithActivityDTO.getActivities()) {
                List<BigInteger> daytypeIds = isNull(activity) ? activityDTO.getActivityTimeCalculationSettings().getDayTypes() : activity.getActivityTimeCalculationSettings().getDayTypes();
                isDayTypeExist =isDayTypeValid(shiftActivityDTO.getStartDate(), daytypeIds,dayTypeMap);
                if (isDayTypeExist) {
                    shiftWithActivityDTOS.add(shiftWithActivityDTO);
                }
            }
        }
        return shiftWithActivityDTOS;
    }

    private static void getShiftsByCutOff(List<ShiftWithActivityDTO> shifts, List<BigInteger> activitieIds, List<ShiftWithActivityDTO> updatedShifts, Optional<CutOffInterval> cutOffIntervalOptional) {
        CutOffInterval cutOffInterval = cutOffIntervalOptional.get();
        for (ShiftWithActivityDTO shift : shifts) {
            DateTimeInterval interval = new DateTimeInterval(DateUtils.asDate(cutOffInterval.getStartDate()), DateUtils.asDate(cutOffInterval.getEndDate().plusDays(1)));
            if (CollectionUtils.containsAny(shift.getActivityIds(), activitieIds) && interval.contains(shift.getStartDate())) {
                updatedShifts.add(shift);
            }
        }
    }

    public static DateTimeInterval getIntervalByNumberOfWeeks(Date startDate, int numberOfWeeks, LocalDate validationStartDate, LocalDate planningPeriodEndDate) {
        if (numberOfWeeks == 0 || validationStartDate == null) {
            throwException(MESSAGE_RULETEMPLATE_WEEKS_NOTNULL);
        }
        DateTimeInterval dateTimeInterval = null;
        while (validationStartDate.isBefore(planningPeriodEndDate) || validationStartDate.equals(planningPeriodEndDate)) {
            dateTimeInterval = new DateTimeInterval(asDate(validationStartDate), asDate(validationStartDate.plusWeeks(numberOfWeeks)));
            if (dateTimeInterval.contains(startDate)) {
                break;
            }
            validationStartDate = validationStartDate.plusWeeks(numberOfWeeks);

        }
        return dateTimeInterval;
    }


    public static DateTimeInterval[] getIntervalsByRuleTemplate(ShiftWithActivityDTO shift, String intervalUnit, long intervalValue) {
        DateTimeInterval[] interval = new DateTimeInterval[2];
        if (intervalValue == 0 || StringUtils.isEmpty(intervalUnit)) {
            throwException("message.ruleTemplate.interval.notNull");
        }
        switch (intervalUnit) {
            case DAYS:
                interval[0] = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).minusDays((int) intervalValue).truncatedTo(ChronoUnit.DAYS).plusDays(1), DateUtils.asZonedDateTime(shift.getStartDate()).plusDays(1).truncatedTo(ChronoUnit.DAYS));
                interval[1] = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).truncatedTo(ChronoUnit.DAYS), DateUtils.asZonedDateTime(shift.getStartDate()).plusDays(intervalValue).truncatedTo(ChronoUnit.DAYS));
                break;
            case WEEKS:
                interval[0] = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).minusWeeks((int) intervalValue).truncatedTo(ChronoUnit.DAYS).plusDays(1), DateUtils.asZonedDateTime(shift.getStartDate()).plusDays(1).truncatedTo(ChronoUnit.DAYS));
                interval[1] = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).truncatedTo(ChronoUnit.DAYS), DateUtils.asZonedDateTime(shift.getStartDate()).plusWeeks(intervalValue).truncatedTo(ChronoUnit.DAYS));
                break;
            case MONTHS:
                interval[0] = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).minusMonths((int) intervalValue).truncatedTo(ChronoUnit.DAYS).plusDays(1), DateUtils.asZonedDateTime(shift.getStartDate()).plusDays(1).truncatedTo(ChronoUnit.DAYS));
                interval[1] = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).truncatedTo(ChronoUnit.DAYS), DateUtils.asZonedDateTime(shift.getStartDate()).plusMonths(intervalValue).truncatedTo(ChronoUnit.DAYS));
                break;
            case YEARS:
                interval[0] = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).minusYears((int) intervalValue).truncatedTo(ChronoUnit.DAYS).plusDays(1), DateUtils.asZonedDateTime(shift.getStartDate()).plusDays(1).truncatedTo(ChronoUnit.DAYS));
                interval[1] = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).truncatedTo(ChronoUnit.DAYS), DateUtils.asZonedDateTime(shift.getStartDate()).plusYears(intervalValue).truncatedTo(ChronoUnit.DAYS));
                break;
            default:
                break;
        }
        return interval;
    }

    public static List<TimeInterval> getTimeSlotByPartOfDay(List<PartOfDay> partOfDays, Map<String, TimeSlot> timeSlotWrapperMap, ShiftWithActivityDTO shift) {
        List<TimeInterval> timeIntervals = new ArrayList<>();
        for (PartOfDay partOfDay : partOfDays) {
            if (timeSlotWrapperMap.containsKey(partOfDay.getValue())) {
                TimeSlot timeSlotWrapper = timeSlotWrapperMap.get(partOfDay.getValue());
                if (partOfDay.getValue().equals(timeSlotWrapper.getName())) {
                    int endMinutesOfInterval = (timeSlotWrapper.getEndHour() * 60) + timeSlotWrapper.getEndMinute();
                    int startMinutesOfInterval = (timeSlotWrapper.getStartHour() * 60) + timeSlotWrapper.getStartMinute();
                    TimeInterval interval = new TimeInterval(startMinutesOfInterval, endMinutesOfInterval);
                    if(isNotNull(shift)) {
                        int minuteOfTheDay = DateUtils.asZonedDateTime(shift.getStartDate()).get(ChronoField.MINUTE_OF_DAY);
                        if (minuteOfTheDay == (int) interval.getStartFrom() || interval.contains(minuteOfTheDay)) {
                            timeIntervals.add(interval);
                            break;
                        }
                    }else {
                        timeIntervals.add(interval);
                    }
                }
            }
        }
        return timeIntervals;
    }

    public static TimeInterval[] getTimeSlotsByPartOfDay(List<PartOfDay> partOfDays, Map<String, TimeSlot> timeSlotWrapperMap, ShiftWithActivityDTO shift) {
        TimeInterval[] timeIntervals = new TimeInterval[partOfDays.size()];
        int i=0;
        boolean valid = false;
        for (PartOfDay partOfDay : partOfDays) {
            if (timeSlotWrapperMap.containsKey(partOfDay.getValue())) {
                TimeSlot timeSlotWrapper = timeSlotWrapperMap.get(partOfDay.getValue());
                if (partOfDay.getValue().equals(timeSlotWrapper.getName())) {
                    int endMinutesOfInterval = (timeSlotWrapper.getEndHour() * 60) + timeSlotWrapper.getEndMinute();
                    int startMinutesOfInterval = (timeSlotWrapper.getStartHour() * 60) + timeSlotWrapper.getStartMinute();
                    TimeInterval interval = new TimeInterval(startMinutesOfInterval, endMinutesOfInterval);
                    int minuteOfTheDay = DateUtils.asZonedDateTime(shift.getStartDate()).get(ChronoField.MINUTE_OF_DAY);
                    timeIntervals[i] = interval;
                    i++;
                    if (!valid && (minuteOfTheDay == (int) interval.getStartFrom() || interval.contains(minuteOfTheDay))) {
                        valid = true;
                    }
                }
            }
        }
        return valid ? timeIntervals : new TimeInterval[0];
    }

    public static List<DateTimeInterval> getDaysIntervals(DateTimeInterval dateTimeInterval) {
        List<DateTimeInterval> intervals = new ArrayList<>();
        ZonedDateTime endDate;
        ZonedDateTime startDate = dateTimeInterval.getStart();
        while (true) {
            if (startDate.isBefore(dateTimeInterval.getEnd())) {
                endDate = startDate.plusDays(1);
                intervals.add(new DateTimeInterval(startDate, endDate));
                startDate = endDate;
            } else {
                break;
            }
        }
        return intervals;
    }

    public static DateTimeInterval getIntervalByRuleTemplate(ShiftWithActivityDTO shift, String intervalUnit, long intervalValue) {
        DateTimeInterval interval = null;
        if (intervalValue == 0 || StringUtils.isEmpty(intervalUnit)) {
            throwException(MESSAGE_RULETEMPLATE_INTERVAL_NOTNULL);
        }
        switch (intervalUnit) {
            case DAYS:
                interval = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).minusDays((int) intervalValue).truncatedTo(ChronoUnit.DAYS).plusDays(1), DateUtils.asZonedDateTime(shift.getEndDate()).plusDays((int) intervalValue).truncatedTo(ChronoUnit.DAYS).minusDays(1).plusDays(1));
                break;
            case WEEKS:
                interval = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).minusWeeks((int) intervalValue).truncatedTo(ChronoUnit.DAYS).plusDays(1), DateUtils.asZonedDateTime(shift.getEndDate()).plusWeeks((int) intervalValue).truncatedTo(ChronoUnit.DAYS).minusDays(1).plusDays(1));
                break;
            case MONTHS:
                interval = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).minusMonths((int) intervalValue).truncatedTo(ChronoUnit.DAYS).plusDays(1), DateUtils.asZonedDateTime(shift.getEndDate()).plusMonths((int) intervalValue).truncatedTo(ChronoUnit.DAYS).minusDays(1).plusDays(1));
                break;
            case YEARS:
                interval = new DateTimeInterval(DateUtils.asZonedDateTime(shift.getStartDate()).minusYears((int) intervalValue).truncatedTo(ChronoUnit.DAYS).plusDays(1), DateUtils.asZonedDateTime(shift.getEndDate()).plusYears((int) intervalValue).truncatedTo(ChronoUnit.DAYS).minusDays(1).plusDays(1));
                break;
            default:
                break;
        }
        return interval;
    }

    public static List<ShiftWithActivityDTO> getShiftsByInterval(DateTimeInterval dateTimeInterval, List<ShiftWithActivityDTO> shifts, List<TimeInterval> timeIntervals) {
        List<ShiftWithActivityDTO> updatedShifts = new ArrayList<>();
        shifts.forEach(s -> {
            if ((dateTimeInterval.contains(s.getStartDate()) || dateTimeInterval.getEndLocalDate().equals(s.getEndLocalDate())) && isTimeIntervalValid(timeIntervals, s)) {
                updatedShifts.add(s);
            }
        });
        return updatedShifts;
    }

    private static boolean isTimeIntervalValid(List<TimeInterval> timeIntervals, ShiftWithActivityDTO s) {
        return timeIntervals == null || timeIntervals.stream().anyMatch(timeInterval->timeInterval.contains(DateUtils.asZonedDateTime(s.getStartDate()).get(ChronoField.MINUTE_OF_DAY)));
    }

    public static Set<BigInteger> getShiftIdsByInterval(DateTimeInterval dateTimeInterval, List<ShiftWithActivityDTO> shifts, TimeInterval[] timeIntervals) {
        Set<BigInteger> updatedShifts = new HashSet<>();
        shifts.forEach(s -> {
            for (TimeInterval timeInterval : timeIntervals) {
                if ((dateTimeInterval.contains(s.getStartDate()) || dateTimeInterval.getEndDate().equals(s.getStartDate())) && (timeInterval == null || timeInterval.contains(DateUtils.asZonedDateTime(s.getStartDate()).get(ChronoField.MINUTE_OF_DAY)))) {
                    updatedShifts.add(s.getId());
                }
            }
        });
        return updatedShifts;
    }

    public static void brakeRuleTemplateAndUpdateViolationDetails(RuleTemplateSpecificInfo infoWrapper,
                                                                  Integer counterCount, boolean isValid,
                                                                  WTABaseRuleTemplate wtaBaseRuleTemplate,
                                                                  Integer totalCounter, String unitType,
                                                                  String unitValue) {
        if (!isValid) {
            WorkTimeAgreementRuleViolation workTimeAgreementRuleViolation;
            if (PhaseDefaultName.TIME_ATTENDANCE.equals(infoWrapper.getPhaseEnum())){
                workTimeAgreementRuleViolation = new WorkTimeAgreementRuleViolation(wtaBaseRuleTemplate.getId(), wtaBaseRuleTemplate.getName(), counterCount, true, false,totalCounter,unitType,unitValue);
            }else if (counterCount != null) {
                int counterValue = counterCount - 1;
                boolean canBeIgnore = true;
                if (counterValue < 0) {
                    counterCount = 0;
                    canBeIgnore = false;
                }
                workTimeAgreementRuleViolation = new WorkTimeAgreementRuleViolation(wtaBaseRuleTemplate.getId(), wtaBaseRuleTemplate.getName(), counterCount, true, canBeIgnore,totalCounter,unitType,unitValue);
            }
            else {
                workTimeAgreementRuleViolation = new WorkTimeAgreementRuleViolation(wtaBaseRuleTemplate.getId(), wtaBaseRuleTemplate.getName(), null, true, false,totalCounter,unitType,unitValue);
            }
            infoWrapper.getViolatedRules().getWorkTimeAgreements().add(workTimeAgreementRuleViolation);
        }
    }



    public static Integer[] getValueByPhaseAndCounter(RuleTemplateSpecificInfo infoWrapper, List<PhaseTemplateValue> phaseTemplateValues, WTABaseRuleTemplate ruleTemplate) {
        Integer[] limitAndCounter = new Integer[3];
        for (PhaseTemplateValue phaseTemplateValue : phaseTemplateValues) {
            if (infoWrapper.getPhaseId().equals(phaseTemplateValue.getPhaseId())) {
                limitAndCounter[0] = (int) (UserContext.getUserDetails().isStaff() ? phaseTemplateValue.getStaffValue() : phaseTemplateValue.getManagementValue());
                Integer[] counterValue = getCounterValue(infoWrapper, phaseTemplateValue, ruleTemplate);
                limitAndCounter[1] = counterValue[0];
                limitAndCounter[2] = counterValue[1];
                break;
            }
        }
        return limitAndCounter;
    }



    public static boolean isValidForPhase(BigInteger phaseId, List<PhaseTemplateValue> phaseTemplateValues) {
        for (PhaseTemplateValue phaseTemplateValue : phaseTemplateValues) {
            if (phaseId.equals(phaseTemplateValue.getPhaseId())) {
                return !phaseTemplateValue.isDisabled();
            }
        }
        return false;
    }

    public static boolean isValidShift(BigInteger phaseId,ShiftWithActivityDTO shift, List<PhaseTemplateValue> phaseTemplateValues, Set<BigInteger> timeTypeIds, Set<BigInteger> plannedTimeIds) {
        boolean valid = false;
        for (PhaseTemplateValue phaseTemplateValue : phaseTemplateValues) {
            if (phaseId.equals(phaseTemplateValue.getPhaseId()) && !phaseTemplateValue.isDisabled()) {
                valid = (CollectionUtils.isNotEmpty(timeTypeIds) && CollectionUtils.containsAny(timeTypeIds, shift.getActivitiesTimeTypeIds())) && (isCollectionNotEmpty(plannedTimeIds) && CollectionUtils.containsAny(plannedTimeIds, shift.getActivitiesPlannedTimeIds()));
                break;
            }
        }
        return valid;
    }

    public static Integer[] getCounterValue(RuleTemplateSpecificInfo infoWrapper, PhaseTemplateValue phaseTemplateValue, WTABaseRuleTemplate ruleTemplate) {
        Integer totalCounterValue = null;
        if (UserContext.getUserDetails().isStaff() && phaseTemplateValue.isStaffCanIgnore()) {
            totalCounterValue = ruleTemplate.getStaffCanIgnoreCounter();
            if (totalCounterValue == null) {
                throwException(MESSAGE_RULETEMPLATE_COUNTER_VALUE_NOTNULL, ruleTemplate.getName());
            }
        } else if (UserContext.getUserDetails().isManagement() && phaseTemplateValue.isManagementCanIgnore()) {
            totalCounterValue = ruleTemplate.getManagementCanIgnoreCounter();
            if (totalCounterValue == null) {
                throwException(MESSAGE_RULETEMPLATE_COUNTER_VALUE_NOTNULL, ruleTemplate.getName());
            }
        }
        Integer availableCounter = totalCounterValue != null ? infoWrapper.getCounterMap().getOrDefault(ruleTemplate.getId(), totalCounterValue) : null;
        return new Integer[]{availableCounter,totalCounterValue};

    }


    public static int getConsecutiveDaysInDate(List<LocalDate> localDates) {
        if (localDates.size() < 2) return 0;
        int count = 1;
        int max = 0;
        int l = 1;
        while (l < localDates.size()) {
            LocalDate previousDay = localDates.get(l - 1);
            LocalDate nextDay = localDates.get(l);
            if (previousDay.equals(nextDay.minusDays(1))) {
                count++;
            } else {
                count = 1;
            }
            if (count > max) {
                max = count;
            }
            l++;
        }
        return max;
    }




    public static List<DateTimeInterval> getSortedIntervals(DateTimeInterval dateTimeInterval, List<ShiftWithActivityDTO> shifts) {
        List<DateTimeInterval> intervals = new ArrayList<>();
        for (ShiftWithActivityDTO s : sortShifts(shifts)) {
            intervals.add(s.getDateTimeInterval());
        }
        return intervals;
    }

    public static List<ShiftWithActivityDTO> sortShifts(List<ShiftWithActivityDTO> shifts) {
        shifts.sort(Comparator.comparing(ShiftDTO::getStartDate));
        return shifts;
    }

    public static boolean isValid(MinMaxSetting minMaxSetting, int limitValue, int calculatedValue) {
        return minMaxSetting.equals(MinMaxSetting.MINIMUM) ? limitValue <= calculatedValue  : limitValue >= calculatedValue;
    }

    public static Set<LocalDate> getSortedAndUniqueDates(List<ShiftWithActivityDTO> shifts) {
        Set<LocalDate> dates = shifts.stream().map(s -> DateUtils.asLocalDate(s.getStartDate())).collect(Collectors.toSet());
        return new TreeSet<>(dates);
    }


    public static Set<DayOfWeek> getValidDays(Map<BigInteger, DayTypeDTO> dayTypeMap, List<BigInteger> dayTypeIds, LocalDate startDate) {
        Set<DayOfWeek> dayOfWeeks = new HashSet<>();
        List<Day> days = new ArrayList<>();
        for (BigInteger dayTypeId : dayTypeIds) {
            if(dayTypeMap.containsKey(dayTypeId)){
                DayTypeDTO dayTypeDTO = dayTypeMap.get(dayTypeId);
                if(dayTypeDTO.getValidDays().contains(Day.EVERYDAY)){
                    List<LocalDate> holidayList = dayTypeDTO.getCountryHolidayCalenderData().stream().map(CountryHolidayCalenderDTO::getHolidayDate).collect(Collectors.toList());
                    if(holidayList.contains(startDate)){
                        days.addAll(dayTypeDTO.getValidDays());
                    }
                }else{
                    days.addAll(dayTypeDTO.getValidDays());
                }
            }
        }
        days.forEach(day -> {
            if (!day.equals(Day.EVERYDAY)) {
                dayOfWeeks.add(DayOfWeek.valueOf(day.name()));
            } else {
                dayOfWeeks.addAll(Arrays.asList(DayOfWeek.values()));
            }
        });
        return new HashSet<>(dayOfWeeks);
    }

    public static void validateRuleTemplate(int numberOfWeeks, LocalDate validationStartDate) {
        if (numberOfWeeks == 0 || validationStartDate == null) {
            throwException(MESSAGE_RULETEMPLATE_WEEKS_NOTNULL);
        }
    }

    public static void validateRuleTemplate(long intervalLength, String intervalUnit) {
        if (intervalLength == 0 || isEmpty(intervalUnit)) {
            throwException(MESSAGE_RULETEMPLATE_INTERVAL_NOTNULL);
        }
    }

    public static DateTimeInterval getIntervalByRuleTemplates(ShiftWithActivityDTO shift, List<WTABaseRuleTemplate> wtaBaseRuleTemplates, Map<BigInteger, ActivityWrapper> activityWrapperMap, LocalDate planningPeriodEndDate) {
        DateTimeInterval interval = new DateTimeInterval(shift.getStartDate(), shift.getEndDate());
        for (WTABaseRuleTemplate ruleTemplate : wtaBaseRuleTemplates) {
            switch (ruleTemplate.getWtaTemplateType()) {
                case NUMBER_OF_PARTOFDAY:
                    interval = getDateTimeIntervalByNumberOfPartOftheDay(shift, interval, (NumberOfPartOfDayShiftsWTATemplate) ruleTemplate);
                    break;
                case DAYS_OFF_IN_PERIOD:
                    interval = getDateTimeIntervalByDaysOffInPeriodWTATemplate(shift, interval, (DaysOffInPeriodWTATemplate) ruleTemplate);
                    break;
                case AVERAGE_SHEDULED_TIME:
                    interval = getDateTimeIntervalByAverageScheduledTimeWTATemplate(shift, interval, (AverageScheduledTimeWTATemplate) ruleTemplate);
                    break;
                case VETO_AND_STOP_BRICKS:
                    interval = getDateTimeIntervalByVetoAndStopBricksWTATemplate(shift, planningPeriodEndDate, interval, (VetoAndStopBricksWTATemplate) ruleTemplate);
                    break;
                case NUMBER_OF_WEEKEND_SHIFT_IN_PERIOD:
                    interval = getDateTimeIntervalByNumberOfWeekendShiftsInPeriodWTATemplate(shift, interval, (NumberOfWeekendShiftsInPeriodWTATemplate) ruleTemplate);
                    break;
                case WEEKLY_REST_PERIOD:
                    interval = getDateTimeIntervalByRestPeriodInAnIntervalWTATemplate(shift, interval, (RestPeriodInAnIntervalWTATemplate) ruleTemplate);
                    break;
                case SHORTEST_AND_AVERAGE_DAILY_REST:
                    interval = getDateTimeIntervalByShortestAndAverageDailyRestWTATemplate(shift, interval, (ShortestAndAverageDailyRestWTATemplate) ruleTemplate);
                    break;
                case SENIOR_DAYS_PER_YEAR:
                    interval = getDateTimeIntervalBySeniorDaysPerYearWTATemplate(shift, activityWrapperMap, interval, (SeniorDaysPerYearWTATemplate) ruleTemplate);
                    break;
                case CHILD_CARE_DAYS_CHECK:
                    interval = getDateTimeIntervalByChildCareDaysCheckWTATemplate(shift, activityWrapperMap, interval, (ChildCareDaysCheckWTATemplate) ruleTemplate);
                    break;
                case WTA_FOR_CARE_DAYS:
                    WTAForCareDays wtaForCareDays = (WTAForCareDays) ruleTemplate;
                    interval = interval.addInterval(getIntervalByWTACareDaysRuleTemplate(shift, wtaForCareDays));
                    break;
                case CONSECUTIVE_WORKING_PARTOFDAY:
                    interval = getDateTimeIntervalByConsecutiveWorkWTATemplate(shift, interval, (ConsecutiveWorkWTATemplate) ruleTemplate);
                    break;
                case NO_OF_SEQUENCE_SHIFT:
                    interval = getDateTimeIntervalByNoOfSequenceShiftWTATemplate(shift, interval, (NoOfSequenceShiftWTATemplate) ruleTemplate);
                    break;
                case DURATION_BETWEEN_SHIFTS:
                    DateTimeInterval dateTimeInterval = new DateTimeInterval(minusMonths(shift.getStartDate(), 1), plusMonths(shift.getStartDate(), 1));
                    interval = interval.addInterval(dateTimeInterval);
                break;
                case DAYS_OFF_AFTER_A_SERIES:
                    interval = getDateTimeIntervalByDaysOffAfterASeriesWTATemplate(shift, interval, (DaysOffAfterASeriesWTATemplate) ruleTemplate);
                    break;
                default:
                    break;
            }
        }
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalByChildCareDaysCheckWTATemplate(ShiftWithActivityDTO shift, Map<BigInteger, ActivityWrapper> activityWrapperMap, DateTimeInterval interval, ChildCareDaysCheckWTATemplate ruleTemplate) {
        ChildCareDaysCheckWTATemplate childCareDaysCheckWTATemplate = ruleTemplate;
        DateTimeInterval intervalByActivity = getIntervalByActivity(activityWrapperMap, shift, childCareDaysCheckWTATemplate.getActivityIds());
        interval = interval.addInterval(intervalByActivity);
        ruleTemplate.setInterval(intervalByActivity);
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalBySeniorDaysPerYearWTATemplate(ShiftWithActivityDTO shift, Map<BigInteger, ActivityWrapper> activityWrapperMap, DateTimeInterval interval, SeniorDaysPerYearWTATemplate ruleTemplate) {
        SeniorDaysPerYearWTATemplate seniorDaysPerYearWTATemplate = ruleTemplate;
        DateTimeInterval intervalByActivity = getIntervalByActivity(activityWrapperMap, shift, seniorDaysPerYearWTATemplate.getActivityIds());
        interval = interval.addInterval(intervalByActivity);
        ruleTemplate.setInterval(intervalByActivity);
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalByNumberOfWeekendShiftsInPeriodWTATemplate(ShiftWithActivityDTO shift, DateTimeInterval interval, NumberOfWeekendShiftsInPeriodWTATemplate ruleTemplate) {
        NumberOfWeekendShiftsInPeriodWTATemplate numberOfWeekendShiftsInPeriodWTATemplate = ruleTemplate;
        validateRuleTemplate(numberOfWeekendShiftsInPeriodWTATemplate.getIntervalLength(), numberOfWeekendShiftsInPeriodWTATemplate.getIntervalUnit());
        DateTimeInterval intervalByRuleTemplate = getIntervalByRuleTemplate(shift, numberOfWeekendShiftsInPeriodWTATemplate.getIntervalUnit(), numberOfWeekendShiftsInPeriodWTATemplate.getIntervalLength());
        interval = interval.addInterval(intervalByRuleTemplate);
        ruleTemplate.setInterval(intervalByRuleTemplate);
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalByShortestAndAverageDailyRestWTATemplate(ShiftWithActivityDTO shift, DateTimeInterval interval, ShortestAndAverageDailyRestWTATemplate ruleTemplate) {
        ShortestAndAverageDailyRestWTATemplate shortestAndAverageDailyRestWTATemplate = ruleTemplate;
        validateRuleTemplate(shortestAndAverageDailyRestWTATemplate.getIntervalLength(), shortestAndAverageDailyRestWTATemplate.getIntervalUnit());
        DateTimeInterval intervalByRuleTemplate = getIntervalByRuleTemplate(shift, shortestAndAverageDailyRestWTATemplate.getIntervalUnit(), shortestAndAverageDailyRestWTATemplate.getIntervalLength());
        interval = interval.addInterval(intervalByRuleTemplate);
        ruleTemplate.setInterval(intervalByRuleTemplate);
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalByRestPeriodInAnIntervalWTATemplate(ShiftWithActivityDTO shift, DateTimeInterval interval, RestPeriodInAnIntervalWTATemplate ruleTemplate) {
        RestPeriodInAnIntervalWTATemplate restPeriodInAnIntervalWTATemplate = ruleTemplate;
        validateRuleTemplate(restPeriodInAnIntervalWTATemplate.getIntervalLength(), restPeriodInAnIntervalWTATemplate.getIntervalUnit());
        DateTimeInterval dateTimeInterval = getIntervalByRuleTemplate(shift, restPeriodInAnIntervalWTATemplate.getIntervalUnit(), restPeriodInAnIntervalWTATemplate.getIntervalLength());
        interval = interval.addInterval(dateTimeInterval);
        ruleTemplate.setInterval(dateTimeInterval);
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalByConsecutiveWorkWTATemplate(ShiftWithActivityDTO shift, DateTimeInterval interval, ConsecutiveWorkWTATemplate ruleTemplate) {
        ConsecutiveWorkWTATemplate consecutiveWorkWTATemplate = ruleTemplate;
        validateRuleTemplate(consecutiveWorkWTATemplate.getIntervalLength(), consecutiveWorkWTATemplate.getIntervalUnit());
        DateTimeInterval intervalByRuleTemplate = getIntervalByRuleTemplate(shift, consecutiveWorkWTATemplate.getIntervalUnit(), consecutiveWorkWTATemplate.getIntervalLength());
        interval = interval.addInterval(intervalByRuleTemplate);
        ruleTemplate.setInterval(intervalByRuleTemplate);
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalByNoOfSequenceShiftWTATemplate(ShiftWithActivityDTO shift, DateTimeInterval interval, NoOfSequenceShiftWTATemplate ruleTemplate) {
        NoOfSequenceShiftWTATemplate noOfSequenceShiftWTATemplate = ruleTemplate;
        validateRuleTemplate(noOfSequenceShiftWTATemplate.getIntervalLength(), noOfSequenceShiftWTATemplate.getIntervalUnit());
        DateTimeInterval intervalByRuleTemplate = getIntervalByRuleTemplate(shift, noOfSequenceShiftWTATemplate.getIntervalUnit(), noOfSequenceShiftWTATemplate.getIntervalLength());
        interval = interval.addInterval(intervalByRuleTemplate);
        ruleTemplate.setInterval(intervalByRuleTemplate);
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalByDaysOffAfterASeriesWTATemplate(ShiftWithActivityDTO shift, DateTimeInterval interval, DaysOffAfterASeriesWTATemplate ruleTemplate) {
        DateTimeInterval dateTimeInterval;
        DaysOffAfterASeriesWTATemplate daysOffAfterASeriesWTATemplate = ruleTemplate;
        validateRuleTemplate(daysOffAfterASeriesWTATemplate.getIntervalLength(), daysOffAfterASeriesWTATemplate.getIntervalUnit());
        dateTimeInterval = getIntervalByRuleTemplate(shift, daysOffAfterASeriesWTATemplate.getIntervalUnit(), daysOffAfterASeriesWTATemplate.getIntervalLength());
        interval = interval.addInterval(dateTimeInterval);
        ruleTemplate.setInterval(interval);
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalByVetoAndStopBricksWTATemplate(ShiftWithActivityDTO shift, LocalDate planningPeriodEndDate, DateTimeInterval interval, VetoAndStopBricksWTATemplate ruleTemplate) {
        VetoAndStopBricksWTATemplate vetoAndStopBricksWTATemplate = ruleTemplate;
        validateRuleTemplate(vetoAndStopBricksWTATemplate.getNumberOfWeeks(), vetoAndStopBricksWTATemplate.getValidationStartDate());
        DateTimeInterval intervalByNumberOfWeeks = getIntervalByNumberOfWeeks(shift.getStartDate(), vetoAndStopBricksWTATemplate.getNumberOfWeeks(), vetoAndStopBricksWTATemplate.getValidationStartDate(), planningPeriodEndDate);
        interval = interval.addInterval(intervalByNumberOfWeeks);
        ruleTemplate.setInterval(intervalByNumberOfWeeks);
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalByAverageScheduledTimeWTATemplate(ShiftWithActivityDTO shift, DateTimeInterval interval, AverageScheduledTimeWTATemplate ruleTemplate) {
        AverageScheduledTimeWTATemplate averageScheduledTimeWTATemplate = ruleTemplate;
        validateRuleTemplate(averageScheduledTimeWTATemplate.getIntervalLength(), averageScheduledTimeWTATemplate.getIntervalUnit());
        DateTimeInterval intervalByRuleTemplate = getIntervalByRuleTemplate(shift, averageScheduledTimeWTATemplate.getIntervalUnit(), averageScheduledTimeWTATemplate.getIntervalLength());
        interval = interval.addInterval(intervalByRuleTemplate);
        ruleTemplate.setInterval(intervalByRuleTemplate);
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalByDaysOffInPeriodWTATemplate(ShiftWithActivityDTO shift, DateTimeInterval interval, DaysOffInPeriodWTATemplate ruleTemplate) {
        DaysOffInPeriodWTATemplate daysOffInPeriodWTATemplate = ruleTemplate;
        validateRuleTemplate(daysOffInPeriodWTATemplate.getIntervalLength(), daysOffInPeriodWTATemplate.getIntervalUnit());
        DateTimeInterval intervalByRuleTemplate = getIntervalByRuleTemplate(shift, daysOffInPeriodWTATemplate.getIntervalUnit(), daysOffInPeriodWTATemplate.getIntervalLength());
        interval = interval.addInterval(intervalByRuleTemplate);
        ruleTemplate.setInterval(intervalByRuleTemplate);
        return interval;
    }

    private static DateTimeInterval getDateTimeIntervalByNumberOfPartOftheDay(ShiftWithActivityDTO shift, DateTimeInterval interval, NumberOfPartOfDayShiftsWTATemplate ruleTemplate) {
        NumberOfPartOfDayShiftsWTATemplate numberOfPartOfDayShiftsWTATemplate = ruleTemplate;
        validateRuleTemplate(numberOfPartOfDayShiftsWTATemplate.getIntervalLength(), numberOfPartOfDayShiftsWTATemplate.getIntervalUnit());
        DateTimeInterval intervalByRuleTemplate = getIntervalByRuleTemplate(shift, numberOfPartOfDayShiftsWTATemplate.getIntervalUnit(), numberOfPartOfDayShiftsWTATemplate.getIntervalLength());
        interval = interval.addInterval(intervalByRuleTemplate);
        ruleTemplate.setInterval(intervalByRuleTemplate);
        return interval;
    }

    public static DateTimeInterval getIntervalByWTACareDaysRuleTemplate(ShiftWithActivityDTO shift, WTAForCareDays wtaForCareDays) {
        Map<BigInteger, ActivityCareDayCount> careDayCountMap = wtaForCareDays.careDaysCountMap();
        DateTimeInterval dateTimeInterval = new DateTimeInterval(shift.getStartDate(), shift.getEndDate());
        for (ShiftActivityDTO shiftActivityDTO : shift.getActivities()) {
            if (careDayCountMap.containsKey(shiftActivityDTO.getActivityId())) {
                dateTimeInterval = getCutoffInterval(shiftActivityDTO.getActivity().getActivityRulesSettings().getCutOffStartFrom(), shiftActivityDTO.getActivity().getActivityRulesSettings().getCutOffIntervalUnit(), shiftActivityDTO.getActivity().getActivityRulesSettings().getCutOffdayValue(),shift.getStartDate());
            }
        }
        return dateTimeInterval;
    }

    public static DateTimeInterval getIntervalByActivity(Map<BigInteger, ActivityWrapper> activityWrapperMap, ShiftWithActivityDTO shiftWithActivityDTO, List<BigInteger> activityIds) {
        LocalDate shiftDate = DateUtils.asLocalDate(shiftWithActivityDTO.getStartDate());
        DateTimeInterval dateTimeInterval = new DateTimeInterval(shiftWithActivityDTO.getStartDate(), DateUtils.asDate(shiftDate.plusDays(1)));
        for (BigInteger activityId : activityIds) {
            if (activityWrapperMap.containsKey(activityId) || shiftWithActivityDTO.getActivities().stream().anyMatch(shiftActivityDTO -> shiftActivityDTO.getActivityId().equals(activityIds))) {
                Activity activity = activityWrapperMap.get(activityId).getActivity();
                if(isNull(activity)){
                    ActivityDTO activityDTO = shiftWithActivityDTO.getActivities().stream().filter(shiftActivityDTO -> shiftActivityDTO.getActivityId().equals(activityIds)).findFirst().get().getActivity();
                    dateTimeInterval = getCutoffInterval(activityDTO.getActivityRulesSettings().getCutOffStartFrom(), activityDTO.getActivityRulesSettings().getCutOffIntervalUnit(), activityDTO.getActivityRulesSettings().getCutOffdayValue(),shiftWithActivityDTO.getStartDate());
                }else {
                    dateTimeInterval = getCutoffInterval(activity.getActivityRulesSettings().getCutOffStartFrom(), activity.getActivityRulesSettings().getCutOffIntervalUnit(), activity.getActivityRulesSettings().getCutOffdayValue(), shiftWithActivityDTO.getStartDate());
                }
            }
        }
        return dateTimeInterval;
    }

    public static DateTimeInterval getCutoffInterval(LocalDate dateFrom, CutOffIntervalUnit cutOffIntervalUnit, Integer dayValue,Date shiftStartDate) {
        LocalDate startDate = dateFrom;
        DateTimeInterval dateTimeInterval = null;
        if(startDate.isBefore(asLocalDate(shiftStartDate))) {
            while (isNull(dateTimeInterval) || !dateTimeInterval.contains(shiftStartDate)) {
                LocalDate nextEndDate = startDate;
                switch (cutOffIntervalUnit) {
                    case DAYS:
                        nextEndDate = startDate.plusDays((long)dayValue - 1);
                        break;
                    case HALF_YEARLY:
                        nextEndDate = startDate.plusMonths(6).minusDays(1);
                        break;
                    case WEEKS:
                        nextEndDate = startDate.plusWeeks(1).minusDays(1);
                        break;
                    case MONTHS:
                        nextEndDate = startDate.plusMonths(1).minusDays(1);
                        break;
                    case QUARTERS:
                        nextEndDate = startDate.plusMonths(3).minusDays(1);
                        break;
                    case YEARS:
                        nextEndDate = startDate.plusYears(1).minusDays(1);
                        break;
                    default:
                        break;
                }
                dateTimeInterval = new DateTimeInterval(startDate, nextEndDate.plusDays(1));
                startDate = nextEndDate.plusDays(1);
            }
        }
        return dateTimeInterval;
    }


    public static boolean isValidForDay(List<BigInteger> dayTypeIds, RuleTemplateSpecificInfo infoWrapper) {
        DayOfWeek shiftDay = DateUtils.asLocalDate(infoWrapper.getShift().getStartDate()).getDayOfWeek();
        return getValidDays(infoWrapper.getDayTypeMap(), dayTypeIds, asLocalDate(infoWrapper.getShift().getStartDate())).stream().anyMatch(day -> day.equals(shiftDay));
    }

    public static boolean validateVetoAndStopBrickRules(float totalBlockingPoints, int totalVeto, int totalStopBricks) {
        return totalBlockingPoints >= totalVeto * VETO_BLOCKING_POINT + totalStopBricks * STOP_BRICK_BLOCKING_POINT;
    }

    public static CareDaysDTO getCareDays(List<CareDaysDTO> careDaysDTOS, int age) {
        CareDaysDTO staffCareDaysDTO = null;
        for (CareDaysDTO careDaysDTO : careDaysDTOS) {
            if (careDaysDTO.getTo() == null && age >= careDaysDTO.getFrom() || (isNotNull(careDaysDTO.getTo()) && careDaysDTO.getFrom() <= age && careDaysDTO.getTo() > age)) {
                staffCareDaysDTO = careDaysDTO;
            }
        }
        return staffCareDaysDTO;
    }


    public static void setDayTypeToCTARuleTemplate(StaffAdditionalInfoDTO staffAdditionalInfoDTO) {
        Map<BigInteger, List<Day>> daytypesMap = staffAdditionalInfoDTO.getDayTypes().stream().collect(Collectors.toMap(k -> k.getId(), v -> v.getValidDays()));
        staffAdditionalInfoDTO.getEmployment().getCtaRuleTemplates().forEach(ctaRuleTemplateDTO -> updateDayTypeDetailInCTARuletemplate(daytypesMap, ctaRuleTemplateDTO));
    }


    public static void updateDayTypeDetailInCTARuletemplate(Map<BigInteger, List<Day>> daytypesMap, CTARuleTemplateDTO ctaRuleTemplateDTO) {
        Set<DayOfWeek> dayOfWeeks = new HashSet<>();
        for (BigInteger dayTypeId : ctaRuleTemplateDTO.getDayTypeIds()) {
            List<Day> applicableDaysOfWeek = daytypesMap.get(dayTypeId);
            if (applicableDaysOfWeek == null) {
                throwException(ERROR_DAYTYPE_NOTFOUND, dayTypeId);
            }
            applicableDaysOfWeek.forEach(day -> {
                if (!day.name().equals(EVERYDAY)) {
                    dayOfWeeks.add(DayOfWeek.valueOf(day.name()));
                }
            });
        }
        ctaRuleTemplateDTO.setPublicHolidays(new ArrayList<>());
        ctaRuleTemplateDTO.setDays(new ArrayList<>(dayOfWeeks));
    }

    public static Integer getValueByPhase( List<PhaseTemplateValue> phaseTemplateValues,BigInteger phaseId) {
        Integer limitAndCounter = null;
        for (PhaseTemplateValue phaseTemplateValue : phaseTemplateValues) {
            if (phaseId.equals(phaseTemplateValue.getPhaseId())) {
                limitAndCounter = (int) (UserContext.getUserDetails().isStaff()? phaseTemplateValue.getStaffValue() : phaseTemplateValue.getManagementValue());
                break;
            }
        }
        return limitAndCounter;
    }

    public static String getHoursByMinutes(Integer hour,String name){
        if(isNull(hour) || hour==0){
            throwException(MESSAGE_RULETEMPLATE_HOURS_NOTZERO,name); // 12.34
        }
        int hours = hour / 60; //since both are ints, you get an int
        int minutes = hour % 60;
        return minutes!=0? hours + "." + minutes:String.valueOf(hours);
    }

    public static int getValueAccordingShiftLengthAndAverageSetting(ShiftLengthAndAverageSetting shiftLengthAndAverageSetting, ShiftWithActivityDTO shift){
        int returnValue;
        switch (shiftLengthAndAverageSetting){
            case DURATION:returnValue = shift.getDurationMinutes();
                break;
            case SCHEDULED_HOURS:returnValue = shift.getScheduledMinutes();
                break;
            case PLANNED_HOURS:returnValue = shift.getPlannedMinutesOfTimebank();
                break;
            default:returnValue = shift.getMinutes();
        }
        return returnValue;
    }

    public static boolean isIgnoredAllRuletemplate(ShiftWithViolatedInfoDTO shiftWithViolatedInfoDTO, ShiftWithViolatedInfoDTO updatedShiftWithViolatedInfoDTO) {
        Set<BigInteger> violatedRuleTemplateIds = shiftWithViolatedInfoDTO.getViolatedRules().getWorkTimeAgreements().stream().map(WorkTimeAgreementRuleViolation::getRuleTemplateId).collect(Collectors.toSet());
        Set<BigInteger> updatedViolatedRuleTemplateIds = updatedShiftWithViolatedInfoDTO.getViolatedRules().getWorkTimeAgreements().stream().map(WorkTimeAgreementRuleViolation::getRuleTemplateId).collect(Collectors.toSet());
        return violatedRuleTemplateIds.containsAll(updatedViolatedRuleTemplateIds);
    }

}
