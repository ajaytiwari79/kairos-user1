package com.kairos.service.pay_out;

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectUtils;
import com.kairos.constants.AppConstants;
import com.kairos.dto.activity.cta.CTARuleTemplateDTO;
import com.kairos.dto.activity.pay_out.PayOutCTADistributionDTO;
import com.kairos.dto.activity.pay_out.PayOutDTO;
import com.kairos.dto.activity.pay_out.PayOutIntervalDTO;
import com.kairos.dto.activity.pay_out.PayOutPerShiftCTADistributionDTO;
import com.kairos.dto.activity.shift.ShiftActivityDTO;
import com.kairos.dto.activity.shift.ShiftWithActivityDTO;
import com.kairos.dto.activity.time_bank.CTARuletemplateBonus;
import com.kairos.dto.activity.time_bank.EmploymentWithCtaDetailsDTO;
import com.kairos.dto.activity.time_bank.TimebankFilterDTO;
import com.kairos.dto.activity.time_bank.time_bank_basic.time_bank.CTADistributionDTO;
import com.kairos.dto.user.country.agreement.cta.CalculationFor;
import com.kairos.dto.user.country.agreement.cta.cta_response.DayTypeDTO;
import com.kairos.dto.user.employment.EmploymentLinesDTO;
import com.kairos.dto.user.user.staff.StaffAdditionalInfoDTO;
import com.kairos.enums.payout.PayOutTrasactionStatus;
import com.kairos.persistence.model.activity.Activity;
import com.kairos.persistence.model.activity.ActivityWrapper;
import com.kairos.persistence.model.pay_out.PayOutPerShift;
import com.kairos.persistence.model.pay_out.PayOutPerShiftCTADistribution;
import com.kairos.persistence.repository.pay_out.PayOutRepository;
import com.kairos.service.time_bank.CalculatePlannedHoursAndScheduledHours;
import com.kairos.service.time_bank.TimeBankCalculationService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Interval;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.AppConstants.*;
import static com.kairos.dto.user.country.agreement.cta.CalculationFor.*;
import static com.kairos.enums.cta.AccountType.PAID_OUT;


/*
* Created By Pradeep singh
*
* */

@Service
public class PayOutCalculationService {

    @Inject
    private TimeBankCalculationService timeBankCalculationService;
    @Inject private PayOutRepository payOutRepository;
//~ ======================================================================================================================

    /**
     * @param interval
     * @param shift
     * @param activityWrapperMap
     * @param payOutPerShift
     * @return PayOutPerShift
     */

    public PayOutPerShift calculateAndUpdatePayOut(DateTimeInterval interval, StaffAdditionalInfoDTO staffAdditionalInfoDTO, ShiftWithActivityDTO shift, Map<BigInteger, ActivityWrapper> activityWrapperMap, PayOutPerShift payOutPerShift, List<DayTypeDTO> dayTypeDTOS) {
        int scheduledMinutesOfPayout = 0;
        Map<BigInteger, Integer> ctaPayoutMinMap = new HashMap<>();
        Map<BigInteger,DayTypeDTO> dayTypeDTOMap = dayTypeDTOS.stream().collect(Collectors.toMap(DayTypeDTO::getId, v->v));
        boolean ruleTemplateValid = false;
        int ctaBonusMinutes = 0;
        for (CTARuleTemplateDTO ruleTemplate : staffAdditionalInfoDTO.getEmployment().getCtaRuleTemplates()) {
            int ctaScheduledOrCompensationMinutes = 0;
            CalculatePlannedHoursAndScheduledHours calculatePlannedHoursAndScheduledHours = new CalculatePlannedHoursAndScheduledHours(timeBankCalculationService,dayTypeDTOMap,staffAdditionalInfoDTO);
            calculatePlannedHoursAndScheduledHours.setStaffAdditionalInfoDTO(staffAdditionalInfoDTO);
            List<ShiftActivityDTO> shiftActivities = calculatePlannedHoursAndScheduledHours.getShiftActivityByBreak(shift.getActivities(),shift.getBreakActivities());
            for (ShiftActivityDTO shiftActivity : shiftActivities) {
                CalculatePayoutByShiftActivity calculatePayoutByShiftActivity = new CalculatePayoutByShiftActivity(interval, staffAdditionalInfoDTO, shift, activityWrapperMap, scheduledMinutesOfPayout, ctaPayoutMinMap, dayTypeDTOMap, ctaBonusMinutes, ruleTemplate, ctaScheduledOrCompensationMinutes, calculatePlannedHoursAndScheduledHours, shiftActivity).invoke();
                scheduledMinutesOfPayout = calculatePayoutByShiftActivity.getScheduledMinutesOfPayout();
                ruleTemplateValid = calculatePayoutByShiftActivity.isRuleTemplateValid();
                ctaBonusMinutes = calculatePayoutByShiftActivity.getCtaBonusMinutes();
                ctaScheduledOrCompensationMinutes = calculatePayoutByShiftActivity.getCtaScheduledOrCompensationMinutes();
            }
            if (CalculationFor.CONDITIONAL_BONUS.equals(ruleTemplate.getCalculationFor())) {
                ctaBonusMinutes = calculateConditionalCtaBonusMinutes(staffAdditionalInfoDTO, shift, ctaPayoutMinMap, ctaBonusMinutes, ruleTemplate, calculatePlannedHoursAndScheduledHours);
            }
            if (ruleTemplate.getCalculationFor().equals(FUNCTIONS) && ruleTemplateValid) {
                int value = timeBankCalculationService.getFunctionalBonusCompensation(staffAdditionalInfoDTO.getEmployment(),ruleTemplate,interval);
                ctaScheduledOrCompensationMinutes = value;
                ctaBonusMinutes += value;
                ctaPayoutMinMap.put(ruleTemplate.getId(), ctaScheduledOrCompensationMinutes);
            }
        }
        payOutPerShift.setCtaBonusMinutesOfPayOut(ctaBonusMinutes);
        payOutPerShift.setScheduledMinutes(scheduledMinutesOfPayout);
        payOutPerShift.setTotalPayOutMinutes(ctaBonusMinutes+scheduledMinutesOfPayout);
        shift.setPlannedMinutesOfPayout(ctaBonusMinutes+scheduledMinutesOfPayout);
        payOutPerShift.setPayOutPerShiftCTADistributions(getCTADistribution(staffAdditionalInfoDTO.getEmployment().getCtaRuleTemplates(), ctaPayoutMinMap));
        return payOutPerShift;
    }

    private int calculateConditionalCtaBonusMinutes(StaffAdditionalInfoDTO staffAdditionalInfoDTO, ShiftWithActivityDTO shift, Map<BigInteger, Integer> ctaPayoutMinMap, int ctaBonusMinutes, CTARuleTemplateDTO ruleTemplate, CalculatePlannedHoursAndScheduledHours calculatePlannedHoursAndScheduledHours) {
        int ctaScheduledOrCompensationMinutes;
        ctaScheduledOrCompensationMinutes = calculatePlannedHoursAndScheduledHours.calculateConditionalBonus(ruleTemplate,staffAdditionalInfoDTO.getEmployment(),shift, PAID_OUT);
        Optional<PayOutPerShiftCTADistributionDTO> payOutPerShiftCTADistributionDTO = shift.getPayoutPerShiftCTADistributions().stream().filter(distributionDTO -> distributionDTO.getCtaRuleTemplateId().equals(ruleTemplate.getId())).findAny();
        if (payOutPerShiftCTADistributionDTO.isPresent() && ctaScheduledOrCompensationMinutes > 0) {
            payOutPerShiftCTADistributionDTO.get().setMinutes(ctaScheduledOrCompensationMinutes);
        } else if(ctaScheduledOrCompensationMinutes > 0){
            PayOutPerShiftCTADistributionDTO payOutPerShiftCTADistribution = new PayOutPerShiftCTADistributionDTO(ruleTemplate.getName(),ctaScheduledOrCompensationMinutes, ruleTemplate.getId());
            shift.getPayoutPerShiftCTADistributions().add(payOutPerShiftCTADistribution);
        }
        ctaBonusMinutes += ctaScheduledOrCompensationMinutes;
        ctaPayoutMinMap.put(ruleTemplate.getId(), ctaPayoutMinMap.getOrDefault(ruleTemplate.getId(), 0) + ctaScheduledOrCompensationMinutes);
        return ctaBonusMinutes;
    }


    /**
     * @param ctaRuleTemplateCalculatedTimeBankDTOS
     * @param ctaTimeBankMinMap
     * @return List<PayOutPerShiftCTADistribution>s
     */
    private List<PayOutPerShiftCTADistribution> getCTADistribution(List<CTARuleTemplateDTO> ctaRuleTemplateCalculatedTimeBankDTOS, Map<BigInteger, Integer> ctaTimeBankMinMap) {
        List<PayOutPerShiftCTADistribution> timeBankCTADistributions = new ArrayList<>(ctaRuleTemplateCalculatedTimeBankDTOS.size());
        for (CTARuleTemplateDTO ruleTemplate : ctaRuleTemplateCalculatedTimeBankDTOS) {
            if (PAID_OUT.equals(ruleTemplate.getPlannedTimeWithFactor().getAccountType())) {
                timeBankCTADistributions.add(new PayOutPerShiftCTADistribution(ruleTemplate.getName(), ctaTimeBankMinMap.getOrDefault(ruleTemplate.getId(), 0), ruleTemplate.getId(),0f));
            }
        }
        return timeBankCTADistributions;
    }


    /**
     * @param intervals
     * @param payOutPerShifts
     * @param payoutTransactionAndIntervalMap
     * @param employmentWithCtaDetailsDTOS
     * @param startDate
     * @return PayOutDTO
     */
    public PayOutDTO getAdvanceViewPayout(TimebankFilterDTO timebankFilterDTO, String sortingOrder, List<DateTimeInterval> intervals, List<PayOutPerShift> payOutPerShifts, Map<DateTimeInterval, List<PayOutTransaction>> payoutTransactionAndIntervalMap, List<EmploymentWithCtaDetailsDTO> employmentWithCtaDetailsDTOS, String query, Date startDate) {
        double payoutMinutesBefore = getPayoutMinutesBefore(timebankFilterDTO, employmentWithCtaDetailsDTOS.get(0), startDate);
        Map<DateTimeInterval, List<PayOutPerShift>> payoutsIntervalMap = getPayoutIntervalsMap(intervals, payOutPerShifts);
        List<PayOutIntervalDTO> payoutIntervalDTOS = getPayoutIntervals(timebankFilterDTO,sortingOrder,intervals, payoutsIntervalMap,payoutMinutesBefore, payoutTransactionAndIntervalMap, employmentWithCtaDetailsDTOS, query);
        List<CTADistributionDTO> scheduledCTADistributions = payoutIntervalDTOS.stream().flatMap(ti -> ti.getPayOutDistribution().getScheduledCTADistributions().stream()).collect(Collectors.toList());
        Map<String, Double> ctaDistributionMap = scheduledCTADistributions.stream().collect(Collectors.groupingBy(CTADistributionDTO::getName, Collectors.summingDouble(CTADistributionDTO::getMinutes)));
        scheduledCTADistributions = getScheduledCTADistributions(ctaDistributionMap, employmentWithCtaDetailsDTOS.get(0));
        List<CTADistributionDTO> ctaBonusDistributions = payoutIntervalDTOS.stream().flatMap(ti -> ti.getPayOutDistribution().getCtaRuletemplateBonus().getCtaDistributions().stream()).collect(Collectors.toList());
        Map<String, Double> ctaBonusDistributionMap = ctaBonusDistributions.stream().collect(Collectors.groupingBy(CTADistributionDTO::getName, Collectors.summingDouble(CTADistributionDTO::getMinutes)));
        double[] payoutCalculatedValue = calculatePayoutForInterval(payoutIntervalDTOS);
        double payoutChange = payoutCalculatedValue[0];
        double payoutBefore = payoutCalculatedValue[1];
        double payoutAfter = payoutCalculatedValue[2];
        double payoutFromCTA = payoutCalculatedValue[3];
        double protectedDaysOffMinutes = payoutCalculatedValue[4];
        PayOutCTADistributionDTO payOutCTADistributionDTO = new PayOutCTADistributionDTO(scheduledCTADistributions, getCTABonusDistributions(ctaBonusDistributionMap, employmentWithCtaDetailsDTOS.get(0)),payoutFromCTA);
        return new PayOutDTO(intervals.get(0).getStartDate(), intervals.get(intervals.size() - 1).getEndDate(), payoutAfter, payoutBefore, payoutChange, payoutIntervalDTOS, payOutCTADistributionDTO,protectedDaysOffMinutes);
    }
    private double getPayoutMinutesBefore(TimebankFilterDTO timebankFilterDTO, EmploymentWithCtaDetailsDTO employmentWithCtaDetailsDTO, Date startDate) {
        List<PayOutPerShift> payOutPerShiftBeforestartDate = payOutRepository.findAllByEmploymentAndBeforeDate(timebankFilterDTO.getDates(),timebankFilterDTO.getDayOfWeeks(),employmentWithCtaDetailsDTO.getId(), startDate);
        return isCollectionNotEmpty(payOutPerShiftBeforestartDate) ? payOutPerShiftBeforestartDate.stream().mapToDouble(payOutPerShift -> {
            if(isNotNull(timebankFilterDTO) && !timebankFilterDTO.isShowTime()){
                return timeBankCalculationService.getCostByByMinutes(employmentWithCtaDetailsDTO.getEmploymentLines(),(int) payOutPerShift.getTotalPayOutMinutes(),payOutPerShift.getDate()).doubleValue();
            }else {
                return (double) payOutPerShift.getTotalPayOutMinutes();
            }
        }).sum() : 0;
    }

    private CTARuletemplateBonus getCTABonusDistributions(Map<String, Double> ctaDistributionMap, EmploymentWithCtaDetailsDTO employmentWithCtaDetailsDTO) {
        List<CTADistributionDTO> ctaBonusDistributions = new ArrayList<>();
        double ctaBonusMinutes = 0;
        for (CTARuleTemplateDTO ctaRuleTemplate : employmentWithCtaDetailsDTO.getCtaRuleTemplates()) {
            if(ctaRuleTemplate.getPlannedTimeWithFactor().getAccountType().equals(PAID_OUT) && ObjectUtils.newHashSet(CONDITIONAL_BONUS,BONUS_HOURS,FUNCTIONS,UNUSED_DAYOFF_LEAVES).contains(ctaRuleTemplate.getCalculationFor())) {
                CTADistributionDTO ctaDistributionDTO = new CTADistributionDTO(ctaRuleTemplate.getId(), ctaRuleTemplate.getName(), ctaDistributionMap.getOrDefault(ctaRuleTemplate.getName(), 0d),0);
                ctaBonusDistributions.add(ctaDistributionDTO);
                ctaBonusMinutes += ctaDistributionDTO.getMinutes();
            }
        }
        return new CTARuletemplateBonus(ctaBonusDistributions, ctaBonusMinutes);
    }

    private List<CTADistributionDTO> getScheduledCTADistributions(Map<String, Double> ctaDistributionMap, EmploymentWithCtaDetailsDTO employmentWithCtaDetailsDTO) {
        List<CTADistributionDTO> scheduledCTADistributions = new ArrayList<>();
        employmentWithCtaDetailsDTO.getCtaRuleTemplates().forEach(cta -> {
            if(cta.getPlannedTimeWithFactor().getAccountType().equals(PAID_OUT) && cta.getCalculationFor().equals(SCHEDULED_HOURS)) {
                scheduledCTADistributions.add(new CTADistributionDTO(cta.getId(), cta.getName(), ctaDistributionMap.getOrDefault(cta.getName(), 0d),0f));

            }
        });
        return scheduledCTADistributions;
    }

    /**
     * @param intervals
     * @param payOutPerShifts
     * @return Map<Interval, List<PayOutPerShift>>
     */
    private Map<DateTimeInterval, List<PayOutPerShift>> getPayoutIntervalsMap(List<DateTimeInterval> intervals, List<PayOutPerShift> payOutPerShifts) {
        Map<DateTimeInterval, List<PayOutPerShift>> timeBanksIntervalMap = new HashMap<>(intervals.size());
        intervals.forEach(i -> timeBanksIntervalMap.put(i, getPayoutsByInterval(i, payOutPerShifts)));
        return timeBanksIntervalMap;
    }


    /**
     * @param interval
     * @param payOutPerShifts
     * @return List<PayOutPerShift>
     */
    private List<PayOutPerShift> getPayoutsByInterval(DateTimeInterval interval, List<PayOutPerShift> payOutPerShifts) {
        List<PayOutPerShift> payOutPerShiftList = new ArrayList<>();
        payOutPerShifts.forEach(payOut -> {
            if (interval.contains(DateUtils.asDate(payOut.getDate()).getTime()) || interval.getStart().equals(DateUtils.toJodaDateTime(payOut.getDate()))) {
                payOutPerShiftList.add(payOut);
            }
        });
        return payOutPerShiftList;
    }


    /**
     * @param intervals
     * @param payoutsIntervalMap
     * @param payoutTransactionAndIntervalMap
     * @param employmentWithCtaDetailsDTOS
     * @return List<PayOutIntervalDTO>
     */
    private List<PayOutIntervalDTO> getPayoutIntervals(TimebankFilterDTO timebankFilterDTO,String sortingOrder,List<DateTimeInterval> intervals, Map<DateTimeInterval, List<PayOutPerShift>> payoutsIntervalMap, double payoutMinutesBefore, Map<DateTimeInterval, List<PayOutTransaction>> payoutTransactionAndIntervalMap, List<EmploymentWithCtaDetailsDTO> employmentWithCtaDetailsDTOS, String query) {
        List<PayOutIntervalDTO> payOutIntervalDTOS = new ArrayList<>(intervals.size());
        Map<Long,List<EmploymentLinesDTO>> employmentWithCtaDetailsHourlyCostMap = employmentWithCtaDetailsDTOS.stream().filter(distinctByKey(employmentWithCtaDetailsDTO -> employmentWithCtaDetailsDTO.getId())).collect(Collectors.toMap(k->k.getId(), v->v.getEmploymentLines()));
        List<CTARuleTemplateDTO> ctaRuleTemplateDTOS = employmentWithCtaDetailsDTOS.stream().flatMap(employmentWithCtaDetailsDTO -> employmentWithCtaDetailsDTO.getCtaRuleTemplates().stream()).filter(distinctByKey(ctaRuleTemplateDTO -> ctaRuleTemplateDTO.getName())).collect(Collectors.toList());
        int sequence=1;
        for (DateTimeInterval interval : intervals) {
            List<PayOutPerShift> payOutPerShifts = payoutsIntervalMap.get(interval);
            List<PayOutTransaction> payOutTransactionList = payoutTransactionAndIntervalMap.get(interval);
            double[] calculatedValues = calculatePayout(timebankFilterDTO,payOutPerShifts,employmentWithCtaDetailsDTOS.get(0));
            double payoutChange = calculatedValues[0];
            double protectedDaysOffMinutes = calculatedValues[1];
            double payoutCost = payOutPerShifts.stream().mapToDouble(payOutPerShift -> timeBankCalculationService.getCostByByMinutes(employmentWithCtaDetailsHourlyCostMap.get(payOutPerShift.getEmploymentId()),(int)payOutPerShift.getTotalPayOutMinutes(),payOutPerShift.getDate()).doubleValue()).sum();
            double approvePayOut = payOutTransactionList.stream().filter(p -> p.getPayOutTrasactionStatus().equals(PayOutTrasactionStatus.APPROVED)).mapToDouble(p -> (long) p.getMinutes()).sum();
            payoutChange += approvePayOut;
            double payoutAfter = payoutMinutesBefore + payoutChange;
            //updateCostInDistribution(employmentWithCtaDetailsHourlyCostMap, payOutPerShifts);
            List<PayOutPerShiftCTADistribution> payOutPerShiftCTADistributions = payOutPerShifts.stream().flatMap(payOutPerShift -> payOutPerShift.getPayOutPerShiftCTADistributions().stream()).collect(Collectors.toList());
            Map<String, Double> ctaDistributionMap = payOutPerShiftCTADistributions.stream().collect(Collectors.groupingBy(PayOutPerShiftCTADistribution::getCtaName, Collectors.summingDouble(PayOutPerShiftCTADistribution::getMinutes)));
            double payoutFromCTA = payOutPerShifts.stream().mapToDouble(payOutPerShift->payOutPerShift.getScheduledMinutes()+payOutPerShift.getCtaBonusMinutesOfPayOut()).sum();
            Map<String, Double> ctaCostDistributionMap = payOutPerShiftCTADistributions.stream().collect(Collectors.groupingBy(PayOutPerShiftCTADistribution::getCtaName, Collectors.summingDouble(PayOutPerShiftCTADistribution::getCost)));
            PayOutCTADistributionDTO payOutCTADistributionDTO = getDistributionOfPayout(timebankFilterDTO,ctaDistributionMap, ctaRuleTemplateDTOS,payoutFromCTA,ctaCostDistributionMap);
            String title = getTitle(query, interval);
            PayOutIntervalDTO payOutIntervalDTO = new PayOutIntervalDTO(interval.getStartDate(), interval.getEndDate(), payoutAfter, payoutMinutesBefore, payoutChange, payOutCTADistributionDTO, interval.getStart().getDayOfWeek(), title,payoutCost);
            payoutMinutesBefore+=payoutChange;
            payOutIntervalDTO.setProtectedDaysOffMinutes(protectedDaysOffMinutes);
            payOutIntervalDTO.setSequence(sequence++);
            payOutIntervalDTOS.add(payOutIntervalDTO);
        }
        if(isNull(sortingOrder) || sortingOrder.equals("DSC")){
            Collections.reverse(payOutIntervalDTOS);
        }
        return payOutIntervalDTOS;
    }

    private double[] calculatePayout(TimebankFilterDTO timebankFilterDTO,List<PayOutPerShift> payOutPerShifts,EmploymentWithCtaDetailsDTO employmentWithCtaDetailsDTO){
        double totalPayOutMinutes = 0;
        double protectedDaysOffMinutes = 0;
        for (PayOutPerShift payOutPerShift : payOutPerShifts) {
            EmploymentLinesDTO employmentLinesDTO = timeBankCalculationService.getEmploymentLineByDate(employmentWithCtaDetailsDTO.getEmploymentLines(),payOutPerShift.getDate());
            if(isNotNull(timebankFilterDTO) && !timebankFilterDTO.isShowTime()){
                totalPayOutMinutes += timeBankCalculationService.getCostByByMinutes(employmentLinesDTO.getHourlyCost(),(int)payOutPerShift.getTotalPayOutMinutes()).doubleValue();
                protectedDaysOffMinutes += timeBankCalculationService.getCostByByMinutes(employmentLinesDTO.getHourlyCost(),(int)payOutPerShift.getProtectedDaysOffMinutes()).doubleValue();
            }else {
                totalPayOutMinutes += payOutPerShift.getTotalPayOutMinutes();
                protectedDaysOffMinutes += payOutPerShift.getProtectedDaysOffMinutes();
            }
            payOutPerShift.getPayOutPerShiftCTADistributions().forEach(payOutPerShiftCTADistribution -> {
                double cost = timeBankCalculationService.getCostByByMinutes(employmentLinesDTO.getHourlyCost(),(int)payOutPerShiftCTADistribution.getMinutes()).doubleValue();
                payOutPerShiftCTADistribution.setCost(cost);
            });
        }
        return new double[]{totalPayOutMinutes,protectedDaysOffMinutes};
    }

    private void updateCostInDistribution(Map<Long, List<EmploymentLinesDTO>> employmentWithCtaDetailsHourlyCostMap, List<PayOutPerShift> payOutPerShifts) {
        payOutPerShifts.forEach(payOutPerShift -> {
    payOutPerShift.getPayOutPerShiftCTADistributions().forEach(payOutPerShiftCTADistribution -> {
        double cost = timeBankCalculationService.getCostByByMinutes(employmentWithCtaDetailsHourlyCostMap.get(payOutPerShift.getEmploymentId()),(int)payOutPerShiftCTADistribution.getMinutes(),payOutPerShift.getDate()).floatValue();
        payOutPerShiftCTADistribution.setCost(cost);
    });
});
    }

    public PayOutCTADistributionDTO getDistributionOfPayout(TimebankFilterDTO timebankFilterDTO,Map<String, Double> ctaDistributionMap, List<CTARuleTemplateDTO> ctaRuleTemplateDTOS, double plannedMinutesOfPayOut,Map<String, Double> ctaCostDistributionMap) {
        List<CTADistributionDTO> timeBankCTADistributionDTOS = new ArrayList<>();
        List<CTADistributionDTO> scheduledCTADistributions = new ArrayList<>();
        double ctaBonusMinutes = 0;
        for (CTARuleTemplateDTO ctaRuleTemplate : ctaRuleTemplateDTOS) {
            if(ctaRuleTemplate.getPlannedTimeWithFactor().getAccountType().equals(PAID_OUT)) {
                if(ObjectUtils.newHashSet(BONUS_HOURS,FUNCTIONS,UNUSED_DAYOFF_LEAVES,CONDITIONAL_BONUS).contains(ctaRuleTemplate.getCalculationFor())) {
                    CTADistributionDTO ctaDistributionDTO = new CTADistributionDTO(ctaRuleTemplate.getId(), ctaRuleTemplate.getName(), ctaDistributionMap.getOrDefault(ctaRuleTemplate.getName(), 0d),ctaCostDistributionMap.getOrDefault(ctaRuleTemplate.getName(), new Double(0.0)));
                    if(isNotNull(timebankFilterDTO) && !timebankFilterDTO.isShowTime()){
                        double minutes = ctaCostDistributionMap.getOrDefault(ctaRuleTemplate.getName(), Double.valueOf(0.0));
                        ctaDistributionDTO.setMinutes(minutes);
                    }
                    timeBankCTADistributionDTOS.add(ctaDistributionDTO);
                    ctaBonusMinutes += ctaDistributionDTO.getMinutes();
                } else if(ctaRuleTemplate.getCalculationFor().equals(SCHEDULED_HOURS)) {
                    CTADistributionDTO ctaDistributionDTO = new CTADistributionDTO(ctaRuleTemplate.getId(), ctaRuleTemplate.getName(), ctaDistributionMap.getOrDefault(ctaRuleTemplate.getName(), 0d), ctaCostDistributionMap.getOrDefault(ctaRuleTemplate.getName(), new Double(0.0)));
                    if(isNotNull(timebankFilterDTO) && !timebankFilterDTO.isShowTime()){
                        double minutes = ctaCostDistributionMap.getOrDefault(ctaRuleTemplate.getName(), Double.valueOf(0.0));
                        ctaDistributionDTO.setMinutes(minutes);
                    }
                    scheduledCTADistributions.add(ctaDistributionDTO);
                }
            }
        }
        return new PayOutCTADistributionDTO(scheduledCTADistributions, new CTARuletemplateBonus(timeBankCTADistributionDTOS, ctaBonusMinutes),plannedMinutesOfPayOut);
    }

    private String getTitle(String query, DateTimeInterval interval) {
        switch (query) {
            case DAILY:
                return interval.getStart().toLocalDate().toString();
            case WEEKLY:
                return StringUtils.capitalize(AppConstants.WEEKLY) + " " + interval.getStart().get(ChronoField.ALIGNED_WEEK_OF_YEAR);
            case MONTHLY:
                return interval.getStart().getMonth().toString();
            case ANNUALLY:
                return StringUtils.capitalize(AppConstants.YEAR) + " " + interval.getStart().getYear();
            case QUATERLY:
                return StringUtils.capitalize(AppConstants.QUARTER) + " " + (interval.getStart().truncatedTo(ChronoUnit.DAYS).equals(interval.getStart()) ? interval.getStart().getMonthValue() / 3 : (interval.getStart().getMonthValue() / 3) + 1);
            default:
                break;
        }
        return "";
    }

    /**
     * @param ctaDistributionMap
     * @param employmentWithCtaDetailsDTO
     * @return List<CTADistributionDTO>
     */
    private List<CTADistributionDTO> getDistributionOfPayout(Map<BigInteger, Long> ctaDistributionMap, EmploymentWithCtaDetailsDTO employmentWithCtaDetailsDTO) {
        List<CTADistributionDTO> distributionDTOS = new ArrayList<>();
        employmentWithCtaDetailsDTO.getCtaRuleTemplates().forEach(cta -> {
            if (!cta.getCalculationFor().equals(CalculationFor.SCHEDULED_HOURS) && cta.getPlannedTimeWithFactor().getAccountType().equals(PAID_OUT)) {
                distributionDTOS.add(new CTADistributionDTO(cta.getId(), cta.getName(), ctaDistributionMap.getOrDefault(cta.getId(), 0l).intValue(),0f));
            }
        });
        return distributionDTOS;
    }

    /**
     * @param timeBankIntervalDTOS
     * @return long[]
     */
    private double[] calculatePayoutForInterval(List<PayOutIntervalDTO> timeBankIntervalDTOS) {
        double payoutChange = 0l;
        double payoutBefore = timeBankIntervalDTOS.get(timeBankIntervalDTOS.size() - 1).getTotalPayOutBeforeCtaMin();
        double payoutFromCTA = 0l;
        double protectedDaysOffMinutes = 0l;
        for (PayOutIntervalDTO payOutIntervalDTO : timeBankIntervalDTOS) {
            payoutChange += payOutIntervalDTO.getPayoutChange();
            payoutFromCTA += payOutIntervalDTO.getPayOutDistribution().getPlannedMinutesOfPayout();
            protectedDaysOffMinutes += payOutIntervalDTO.getProtectedDaysOffMinutes();
        }
        double payoutAfter = payoutBefore + payoutChange;
        return new double[]{payoutChange, payoutBefore, payoutAfter, payoutFromCTA,protectedDaysOffMinutes};

    }

    private class CalculatePayoutByShiftActivity {
        private DateTimeInterval interval;
        private StaffAdditionalInfoDTO staffAdditionalInfoDTO;
        private ShiftWithActivityDTO shift;
        private Map<BigInteger, ActivityWrapper> activityWrapperMap;
        private int scheduledMinutesOfPayout;
        private Map<BigInteger, Integer> ctaPayoutMinMap;
        private Map<BigInteger, DayTypeDTO> dayTypeDTOMap;
        private int ctaBonusMinutes;
        private CTARuleTemplateDTO ruleTemplate;
        private int ctaScheduledOrCompensationMinutes;
        private CalculatePlannedHoursAndScheduledHours calculatePlannedHoursAndScheduledHours;
        private ShiftActivityDTO shiftActivity;
        private boolean ruleTemplateValid;

        public CalculatePayoutByShiftActivity(DateTimeInterval interval, StaffAdditionalInfoDTO staffAdditionalInfoDTO, ShiftWithActivityDTO shift, Map<BigInteger, ActivityWrapper> activityWrapperMap, int scheduledMinutesOfPayout, Map<BigInteger, Integer> ctaPayoutMinMap, Map<BigInteger, DayTypeDTO> dayTypeDTOMap, int ctaBonusMinutes, CTARuleTemplateDTO ruleTemplate, int ctaScheduledOrCompensationMinutes, CalculatePlannedHoursAndScheduledHours calculatePlannedHoursAndScheduledHours, ShiftActivityDTO shiftActivity) {
            this.interval = interval;
            this.staffAdditionalInfoDTO = staffAdditionalInfoDTO;
            this.shift = shift;
            this.activityWrapperMap = activityWrapperMap;
            this.scheduledMinutesOfPayout = scheduledMinutesOfPayout;
            this.ctaPayoutMinMap = ctaPayoutMinMap;
            this.dayTypeDTOMap = dayTypeDTOMap;
            this.ctaBonusMinutes = ctaBonusMinutes;
            this.ruleTemplate = ruleTemplate;
            this.ctaScheduledOrCompensationMinutes = ctaScheduledOrCompensationMinutes;
            this.calculatePlannedHoursAndScheduledHours = calculatePlannedHoursAndScheduledHours;
            this.shiftActivity = shiftActivity;
        }

        public int getScheduledMinutesOfPayout() {
            return scheduledMinutesOfPayout;
        }

        public boolean isRuleTemplateValid() {
            return ruleTemplateValid;
        }

        public int getCtaBonusMinutes() {
            return ctaBonusMinutes;
        }

        public int getCtaScheduledOrCompensationMinutes() {
            return ctaScheduledOrCompensationMinutes;
        }

        public CalculatePayoutByShiftActivity invoke() {
            ShiftActivityDTO shiftActivityDTO = calculatePlannedHoursAndScheduledHours.getShiftActivityDTO(shift,shiftActivity);
            Activity activity = activityWrapperMap.get(shiftActivity.getActivityId()).getActivity();
            ruleTemplateValid = ruleTemplate.getPlannedTimeWithFactor().getAccountType().equals(PAID_OUT) && timeBankCalculationService.validateCTARuleTemplate(ruleTemplate, staffAdditionalInfoDTO.getEmployment(), shift.getPhaseId(), newHashSet(activity.getId()), newHashSet(activity.getActivityBalanceSettings().getTimeTypeId()), shiftActivity.getPlannedTimes());
            if (ruleTemplateValid) {
                if (ruleTemplate.getCalculationFor().equals(CalculationFor.SCHEDULED_HOURS) && timeBankCalculationService.isDayTypeValid(shiftActivity.getStartDate(),ruleTemplate,dayTypeDTOMap)) {
                    scheduledMinutesOfPayout += shiftActivity.getScheduledMinutes();
                    ctaScheduledOrCompensationMinutes = shiftActivity.getScheduledMinutes();
                    shiftActivityDTO.setScheduledMinutesOfPayout(shiftActivity.getScheduledMinutes() + shiftActivityDTO.getScheduledMinutesOfPayout());
                } else if (ruleTemplate.getCalculationFor().equals(BONUS_HOURS)) {
                    ctaScheduledOrCompensationMinutes = (int)Math.round(calculatePlannedHoursAndScheduledHours.getAndUpdateCtaBonusMinutes(interval, ruleTemplate, shiftActivity,staffAdditionalInfoDTO.getEmployment(),dayTypeDTOMap));
                    ctaBonusMinutes += ctaScheduledOrCompensationMinutes;
                    Optional<PayOutPerShiftCTADistributionDTO> payOutPerShiftCTADistributionDTOOptional = shiftActivityDTO.getPayoutPerShiftCTADistributions().stream().filter(distributionDTO -> distributionDTO.getCtaRuleTemplateId().equals(ruleTemplate.getId())).findAny();
                    if (payOutPerShiftCTADistributionDTOOptional.isPresent()) {
                        payOutPerShiftCTADistributionDTOOptional.get().setMinutes(payOutPerShiftCTADistributionDTOOptional.get().getMinutes() + ctaScheduledOrCompensationMinutes);
                    } else {
                        PayOutPerShiftCTADistributionDTO payOutPerShiftCTADistributionDTO = new PayOutPerShiftCTADistributionDTO(ruleTemplate.getName(),  ctaScheduledOrCompensationMinutes, ruleTemplate.getId());
                        shiftActivityDTO.getPayoutPerShiftCTADistributions().add(payOutPerShiftCTADistributionDTO);
                    }
                    shiftActivityDTO.setPayoutCtaBonusMinutes(shiftActivityDTO.getPayoutCtaBonusMinutes() + ctaScheduledOrCompensationMinutes);
                }
                ctaPayoutMinMap.put(ruleTemplate.getId(), ctaPayoutMinMap.getOrDefault(ruleTemplate.getId(), 0) + ctaScheduledOrCompensationMinutes);
            }
            return this;
        }
    }
}
