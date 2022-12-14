package com.kairos.counter;

import com.kairos.dto.activity.counter.enums.CounterType;
import com.kairos.enums.kpi.CalculationType;
import com.kairos.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/*
 * @author: mohit.shakya@oodlestechnologies.com
 * @dated: Jun/26/2018
 */

@Component
public class CounterServiceMapping {
    private final static Logger LOGGER = LoggerFactory.getLogger(CounterServiceMapping.class);
    private Map<CounterType, CounterService> counters = new ConcurrentHashMap<>();
    private Map<CalculationType, KPIService> kpiServiceMap = new ConcurrentHashMap<>();

    @Inject
    public void setCounterService(RestingHoursCalculationService restingHoursCalculationService) {
        LOGGER.info("Enum mapping for resting hours: "+this.counters);
        this.counters.put(CounterType.RESTING_HOURS_PER_PRESENCE_DAY, restingHoursCalculationService);
    }

    @Inject
    public void calculatePlannedHours(PlannedHoursCalculationService plannedHoursCalculationService) {
        LOGGER.info("Enum mapping for planned hours: "+this.counters);
        this.counters.put(CounterType.PLANNED_HOURS, plannedHoursCalculationService);
    }

    @Inject
    public void calculateContractualAndPlannedHours(ContractualAndPlannedHoursCalculationService contractualAndPlannedHoursCalculationService) {
        LOGGER.info("Enum mapping for contractual and planned hours : "+this.counters);
        this.counters.put(CounterType.CONTRACTUAL_AND_PLANNED_HOURS, contractualAndPlannedHoursCalculationService);
    }

    @Inject
    public void calculateTimeBankForUnit(TimeBankKpiCalculationService timeBankKpiCalculationService) {
        LOGGER.info("Enum mapping for time bank for unit : "+this.counters);
        this.counters.put(CounterType.TIMEBANK, timeBankKpiCalculationService);
    }

    @Inject
    public void durationOfShiftAndActivity(ShiftAndActivityDurationKpiService shiftAndActivityKpiService) {
        LOGGER.info("Enum mapping for Duration of shift and activity : "+this.counters);
        this.counters.put(CounterType.SHIFT_AND_ACTIVITY_DURATION, shiftAndActivityKpiService);
    }

    @Inject
    public void calculateDayTypeAndTimeSlotHours(DayTypeAndTimeSlotKpiService dayTypeAndTimeSlotKpiService) {
        LOGGER.info("Enum mapping for calculate daytype And timeslot Hours : "+this.counters);
        this.counters.put(CounterType.DAYTYPE_AND_TIMESLOT, dayTypeAndTimeSlotKpiService);
    }

    @Inject
    public void setCounterService(FibonacciKPIService fibonacciKPIService) {
        LOGGER.info("Enum mapping for resting hours: "+this.counters);
        this.counters.put(CounterType.FIBONACCI, fibonacciKPIService);
    }

    @Inject
    public void comparePlannedHoursVsTimeBank(PlannedHoursVsTimeBankService plannedHoursVsTimeBankService) {
        LOGGER.info("Enum mapping for planned hours and time bank : "+this.counters);
        this.counters.put(CounterType.PLANNED_HOURS_VS_TIMEBANK, plannedHoursVsTimeBankService);
    }

    @Inject
    public void setAbsencePlanningKPIService(AbsencePlanningKPIService absencePlanningKPIService) {
        LOGGER.info("Enum mapping for absences per interval : "+this.counters);
        this.counters.put(CounterType.ABSENCES_PER_INTERVAL, absencePlanningKPIService);
    }

    @Inject
    public void setPlannedTimePercentageKPIService(PlannedTimePercentageService plannedTimePercentageKPIService) {
        LOGGER.info("Enum mapping for planned time per interval : "+this.counters);
        this.counters.put(CounterType.PLANNED_TIME_PERCENTAGE, plannedTimePercentageKPIService);
    }

    @Inject
    public void setKPIBuilderCalculationService(KPIBuilderCalculationService kpiBuilderCalculationService) {
        this.counters.put(CounterType.ACTIVITY_KPI, kpiBuilderCalculationService);
    }

    @Inject
    public void setActualTimebankKPIService(ActualTimebankKPIService actualTimebankKPIService) {
        this.kpiServiceMap.put(CalculationType.ACTUAL_TIMEBANK,actualTimebankKPIService);
    }

    @Inject
    public void setPayLevelKPIService(PayLevelKPIService payLevelKPIService) {
        this.kpiServiceMap.put(CalculationType.PAY_LEVEL_GRADE,payLevelKPIService);
    }

    @Inject
    public void setShiftBreakService(ShiftBreakService shiftBreakService) {
        this.kpiServiceMap.put(CalculationType.BREAK_INTERRUPT,shiftBreakService);
    }

    @Inject
    public void setShiftEscalationService(ShiftEscalationService shiftEscalationService) {
        this.kpiServiceMap.put(CalculationType.ESCALATION_RESOLVED_SHIFTS,shiftEscalationService);
        this.kpiServiceMap.put(CalculationType.ESCALATED_SHIFTS,shiftEscalationService);
    }

    @Inject
    public void setSkillKPIService(SkillKPIService skillKPIService) {
        this.kpiServiceMap.put(CalculationType.STAFF_SKILLS_COUNT,skillKPIService);
    }

    @Inject
    public void setStaffingLevelCalculationKPIService(StaffingLevelCalculationKPIService staffingLevelCalculationKPIService) {
        this.kpiServiceMap.put(CalculationType.PRESENCE_OVER_STAFFING,staffingLevelCalculationKPIService);
        this.kpiServiceMap.put(CalculationType.PRESENCE_UNDER_STAFFING,staffingLevelCalculationKPIService);
        this.kpiServiceMap.put(CalculationType.ABSENCE_OVER_STAFFING,staffingLevelCalculationKPIService);
        this.kpiServiceMap.put(CalculationType.ABSENCE_UNDER_STAFFING,staffingLevelCalculationKPIService);
    }

    @Inject
    public void setTimeBankService(TimeBankService timeBankService) {
        this.kpiServiceMap.put(CalculationType.DELTA_TIMEBANK,timeBankService);
        this.kpiServiceMap.put(CalculationType.STAFFING_LEVEL_CAPACITY,timeBankService);
    }

    @Inject
    public void setCareBubbleKPICalculationService(CareBubbleKPICalculationService careBubbleKPICalculationService) {
        this.kpiServiceMap.put(CalculationType.CARE_BUBBLE,careBubbleKPICalculationService);
    }

    @Inject
    public void setWorkOnPublicHolidayKPICalculationService(WorkOnPublicHolidayKPICalculationService workOnPublicHolidayKPICalculationService) {
        this.kpiServiceMap.put(CalculationType.WORKED_ON_PUBLIC_HOLIDAY,workOnPublicHolidayKPICalculationService);
    }

    @Inject
    public void setUnavailabilityCalculationKPIService(UnavailabilityCalculationKPIService unavailabilityCalculationKPIService) {
        this.kpiServiceMap.put(CalculationType.UNAVAILABILITY,unavailabilityCalculationKPIService);
    }

    @Inject
    public void setWeeklyEmploymentHoursKPIService(WeeklyEmploymentHoursKPIService weeklyEmploymentHoursKPIService) {
        this.kpiServiceMap.put(CalculationType.TOTAL_WEEKLY_HOURS,weeklyEmploymentHoursKPIService);
    }

    @Inject
    public void setStaffAgeKPIService(StaffAgeKPIService staffAgeKPIService) {
        this.kpiServiceMap.put(CalculationType.STAFF_AGE,staffAgeKPIService);
    }

    @Inject
    public void setStaffChildCountKPIService(StaffChildCountKPIService staffChildCountKPIService) {
        this.kpiServiceMap.put(CalculationType.SUM_OF_CHILDREN,staffChildCountKPIService);
    }

    @Inject
    public void setWorkTimeAgreementBalancesCalculationService(WorkTimeAgreementBalancesCalculationService workTimeAgreementBalancesCalculationService) {
        this.kpiServiceMap.put(CalculationType.PROTECTED_DAYS_OFF,workTimeAgreementBalancesCalculationService);
        this.kpiServiceMap.put(CalculationType.CARE_DAYS,workTimeAgreementBalancesCalculationService);
        this.kpiServiceMap.put(CalculationType.SENIORDAYS,workTimeAgreementBalancesCalculationService);
        this.kpiServiceMap.put(CalculationType.TOTAL_ABSENCE_DAYS,workTimeAgreementBalancesCalculationService);
        this.kpiServiceMap.put(CalculationType.CHILD_CARE_DAYS,workTimeAgreementBalancesCalculationService);

    }

    @Inject
    public void setTimeBankOffKPI(TimeBankOffKPIService timeBankOffKPIService){
        this.kpiServiceMap.put(CalculationType.TODO_STATUS,timeBankOffKPIService);
    }

    public KPIService getKpiServiceMap(CalculationType calculationType) {
        return kpiServiceMap.get(calculationType);
    }

    public CounterService getService(CounterType counterType){
        return (CounterService) this.counters.get(counterType);
    }
}
