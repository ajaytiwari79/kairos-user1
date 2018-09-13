package com.kairos.service.shift;

import com.kairos.constants.AppConstants;
import com.kairos.custom_exception.ActionNotPermittedException;
import com.kairos.dto.activity.cta.CTAResponseDTO;
import com.kairos.dto.activity.shift.StaffUnitPositionDetails;
import com.kairos.dto.activity.staffing_level.Duration;
import com.kairos.enums.TimeCalaculationType;
import com.kairos.persistence.model.activity.Activity;
import com.kairos.persistence.model.activity.ActivityWrapper;
import com.kairos.persistence.model.activity.tabs.TimeCalculationActivityTab;
import com.kairos.persistence.model.attendence_setting.SickSettings;
import com.kairos.persistence.model.period.PlanningPeriod;
import com.kairos.persistence.model.shift.Shift;
import com.kairos.persistence.repository.activity.ActivityMongoRepository;
import com.kairos.persistence.repository.attendence_setting.SickSettingsRepository;
import com.kairos.persistence.repository.cta.CostTimeAgreementRepository;
import com.kairos.persistence.repository.period.PlanningPeriodMongoRepository;
import com.kairos.persistence.repository.shift.ShiftMongoRepository;
import com.kairos.rest_client.StaffRestClient;
import com.kairos.service.MongoBaseService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.commons.utils.DateUtils;
import com.kairos.utils.user_context.UserContext;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalTime;
import java.util.*;

import static com.kairos.constants.AppConstants.*;
import static com.kairos.persistence.model.constants.RelationshipConstants.ORGANIZATION;
import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * CreatedBy vipulpandey on 31/8/18
 **/
@Service
public class ShiftSickService extends MongoBaseService {
    @Inject
    private ActivityMongoRepository activityRepository;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private StaffRestClient staffRestClient;
    @Inject
    private CostTimeAgreementRepository costTimeAgreementRepository;
    @Inject
    private ShiftMongoRepository shiftMongoRepository;
    @Inject
    PlanningPeriodMongoRepository planningPeriodMongoRepository;
    @Inject
    private SickSettingsRepository sickSettingsRepository;
    private static final Logger logger = LoggerFactory.getLogger(ShiftSickService.class);


    public Map<String, Long> createSicknessShiftsOfStaff(Long unitId, BigInteger activityId, Long staffId, Duration duration) {
        ActivityWrapper activityWrapper = activityRepository.findActivityAndTimeTypeByActivityId(activityId);
        Activity activity = activityWrapper.getActivity();
        if (!Optional.ofNullable(activity).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.activity.id", activity);
        }
        if (activity.getTimeCalculationActivityTab().getMethodForCalculatingTime().equals(ENTERED_MANUALLY)) {
            if (duration == null || duration.getFrom() == null || duration.getTo() == null) {
                exceptionService.actionNotPermittedException("error.startEnd.notBlank");
            }
        }
        if (!activity.getRulesActivityTab().isAllowedAutoAbsence()) {
            exceptionService.actionNotPermittedException("activity.notEligible.for.absence", activity.getName());
        }
        StaffUnitPositionDetails staffUnitPositionDetails = staffRestClient.verifyUnitEmploymentOfStaff(staffId, unitId, ORGANIZATION);
        if (!Optional.ofNullable(staffUnitPositionDetails).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.staffUnitPosition.notFound");
        }
        CTAResponseDTO ctaResponseDTO = costTimeAgreementRepository.getCTAByUnitPositionId(staffUnitPositionDetails.getId(), DateUtils.getDateFromLocalDate(null));
        if (!Optional.ofNullable(ctaResponseDTO).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.cta.notFound");
        }
        staffUnitPositionDetails.setCtaRuleTemplates(ctaResponseDTO.getRuleTemplates());
        PlanningPeriod planningPeriod = planningPeriodMongoRepository.findCurrentDatePlanningPeriod(unitId, DateUtils.getCurrentLocalDate(), DateUtils.getCurrentLocalDate());
        if (!Optional.ofNullable(planningPeriod).isPresent()) {
            exceptionService.actionNotPermittedException("message.periodsetting.notFound");
        }
        logger.info("The current planning period is {}", planningPeriod.getName());
        List<Shift> staffOriginalShiftsOfDates = shiftMongoRepository.findAllShiftsByStaffIds(Collections.singletonList(staffId), DateUtils.getDateFromLocalDate(null), DateUtils.addDays(DateUtils.getDateFromLocalDate(null), activity.getRulesActivityTab().getRecurrenceDays() - 1));
        //This method is used to fetch the shift of the days specified and marked them as disabled as the user is sick.
        createSicknessShiftsOfStaff(staffId, unitId, activity, staffUnitPositionDetails, staffOriginalShiftsOfDates, duration);
        SickSettings sickSettings = new SickSettings(staffId, unitId, UserContext.getUserDetails().getId(), activityId, DateUtils.getCurrentLocalDate(), staffUnitPositionDetails.getId());
        save(sickSettings);
        Map<String, Long> response = new HashMap<>();
        response.put("unitId", unitId);
        response.put("staffId", staffId);
        return response;

    }

    public void createSicknessShiftsOfStaff(Activity activity, List<Shift> staffOriginalShiftsOfDates, Shift previousDaySickShift) {
        short shiftNeedsToAddForDays = activity.getRulesActivityTab().getRecurrenceDays();
        logger.info(staffOriginalShiftsOfDates.size() + "", " shifts found for days");
        staffOriginalShiftsOfDates.forEach(s -> s.setDisabled(true));
        List<Shift> shifts = new ArrayList<>();
        while (shiftNeedsToAddForDays != 0 && activity.getRulesActivityTab().getRecurrenceTimes() > 0) {
            shiftNeedsToAddForDays--;
            Shift currentShift = new Shift(null, null, previousDaySickShift.getStaffId(), activity.getId(), activity.getName(), previousDaySickShift.getUnitPositionId(), previousDaySickShift.getUnitId());
            calculateShiftStartAndEndTime(currentShift, previousDaySickShift, shiftNeedsToAddForDays);
            shifts.add(currentShift);

        }
        // Adding all shifts to the same.
        shifts.addAll(staffOriginalShiftsOfDates);
        if (!shifts.isEmpty())
            save(shifts);

    }

    private void calculateShiftStartAndEndTime(Shift currentShift, Shift previousDayShift, short shiftNeedsToAddForDays) {
        LocalTime shiftStartTime = DateUtils.asLocalTime(previousDayShift.getStartDate());
        LocalTime shiftEndTime = DateUtils.asLocalTime(previousDayShift.getEndDate());
        Duration duration = new Duration(shiftStartTime, shiftEndTime);
        currentShift.setStartDate(DateUtils.getDateAfterDaysWithTime(shiftNeedsToAddForDays, duration.getFrom()));
        currentShift.setEndDate(DateUtils.getDateAfterDaysWithTime(shiftNeedsToAddForDays, duration.getTo()));
        currentShift.setScheduledMinutes(previousDayShift.getScheduledMinutes());
        currentShift.setDurationMinutes(previousDayShift.getDurationMinutes());

    }

    public void createSicknessShiftsOfStaff(Long staffId, Long unitId, Activity activity, StaffUnitPositionDetails staffUnitPositionDetails, List<Shift> staffOriginalShiftsOfDates, Duration duration) {
        short shiftNeedsToAddForDays = activity.getRulesActivityTab().getRecurrenceDays();
        logger.info(staffOriginalShiftsOfDates.size() + "", " shifts found for days");
        staffOriginalShiftsOfDates.forEach(s -> s.setDisabled(true));

        List<Shift> shifts = new ArrayList<>();
        while (shiftNeedsToAddForDays != 0 && activity.getRulesActivityTab().getRecurrenceTimes() > 0) {
            shiftNeedsToAddForDays--;
            Shift currentShift = new Shift(null, null, staffId, activity.getId(), activity.getName(), staffUnitPositionDetails.getId(), unitId);
            Duration calculatedDuration = calculateShiftStartAndEndTime(currentShift, activity.getTimeCalculationActivityTab(), staffUnitPositionDetails, duration);
            Date startDate = DateUtils.getDateAfterDaysWithTime(shiftNeedsToAddForDays, calculatedDuration.getFrom());
            Date endDate = DateUtils.getDateAfterDaysWithTime(shiftNeedsToAddForDays, calculatedDuration.getTo());
            currentShift.setStartDate(startDate);
            currentShift.setEndDate(endDate);
            shifts.add(currentShift);
        }
        // Adding all shifts to the same.
        shifts.addAll(staffOriginalShiftsOfDates);
        if (!shifts.isEmpty())
            save(shifts);

    }

    public Duration calculateShiftStartAndEndTime(Shift shift, TimeCalculationActivityTab timeCalculationActivityTab, StaffUnitPositionDetails unitPosition, Duration duration) {
        int scheduledMinutes = 0;
        int weeklyMinutes;
        int shiftDurationInMinute = 0;
        switch (timeCalculationActivityTab.getMethodForCalculatingTime()) {
            case ENTERED_MANUALLY:
                shiftDurationInMinute = (int) MINUTES.between(duration.getFrom(), duration.getTo());
                scheduledMinutes = new Double(shiftDurationInMinute * timeCalculationActivityTab.getMultiplyWithValue()).intValue();
                shift.setDurationMinutes(shiftDurationInMinute);
                shift.setScheduledMinutes(scheduledMinutes);
                break;
            case FULL_DAY_CALCULATION:
            case FULL_WEEK:
                weeklyMinutes = (TimeCalaculationType.FULL_TIME_WEEKLY_HOURS_TYPE.equals(timeCalculationActivityTab.getFullDayCalculationType()))
                        ? unitPosition.getFullTimeWeeklyMinutes() : unitPosition.getTotalWeeklyMinutes();
                int shiftEndHour = weeklyMinutes / unitPosition.getWorkingDaysInWeek();
                duration.setFrom(timeCalculationActivityTab.getDefaultStartTime());
                duration.setTo(duration.getFrom().plusMinutes(shiftEndHour));
                shiftDurationInMinute = (int) MINUTES.between(duration.getFrom(), duration.getTo());
                scheduledMinutes = new Double(shiftDurationInMinute * timeCalculationActivityTab.getMultiplyWithValue()).intValue();
                break;
            default:
                exceptionService.illegalArgumentException("error.activity.timeCalculation.InvalidArgument");
        }
        shift.setDurationMinutes(shiftDurationInMinute);
        shift.setScheduledMinutes(scheduledMinutes);

        return duration;
    }

    public void disableSicknessShiftsOfStaff(Long staffId, Long unitId) {

        StaffUnitPositionDetails staffUnitPositionDetails = staffRestClient.verifyUnitEmploymentOfStaff(staffId, unitId, ORGANIZATION);
        if (!Optional.ofNullable(staffUnitPositionDetails).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.staffUnitPosition.notFound");
        }
        List<Shift> shifts = shiftMongoRepository.findAllDisabledOrSickShiftsByUnitPositionIdAndUnitId(staffUnitPositionDetails.getId(), unitId, DateUtils.getCurrentLocalDate());
        shifts.forEach(s -> {
            if (s.isSickShift()) {
                s.setDeleted(true);// delete the sick shift.
            } else if (s.isDisabled()) {
                s.setDisabled(false);
            }
        });
        if (!shifts.isEmpty())
            save(shifts);
    }

}
