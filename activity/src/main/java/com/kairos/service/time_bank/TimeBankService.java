package com.kairos.service.time_bank;

import com.kairos.commons.utils.*;
import com.kairos.constants.AppConstants;
import com.kairos.dto.activity.cta.CTAResponseDTO;
import com.kairos.dto.activity.cta.CTARuleTemplateDTO;
import com.kairos.dto.activity.shift.*;
import com.kairos.dto.activity.time_bank.*;
import com.kairos.dto.activity.time_type.TimeTypeDTO;
import com.kairos.dto.user.access_group.UserAccessRoleDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.DayTypeDTO;
import com.kairos.dto.user.user.staff.StaffAdditionalInfoDTO;
import com.kairos.enums.wta.MinMaxSetting;
import com.kairos.enums.wta.WTATemplateType;
import com.kairos.persistence.model.activity.ActivityWrapper;
import com.kairos.persistence.model.activity.TimeType;
import com.kairos.persistence.model.pay_out.PayOutPerShift;
import com.kairos.persistence.model.period.PlanningPeriod;
import com.kairos.persistence.model.shift.Shift;
import com.kairos.persistence.model.shift.ShiftActivity;
import com.kairos.persistence.model.time_bank.DailyTimeBankEntry;
import com.kairos.persistence.model.time_bank.TimeBankCTADistribution;
import com.kairos.persistence.model.wta.WTAQueryResultDTO;
import com.kairos.persistence.model.wta.templates.template_types.TimeBankWTATemplate;
import com.kairos.persistence.repository.activity.ActivityMongoRepository;
import com.kairos.persistence.repository.cta.CostTimeAgreementRepository;
import com.kairos.persistence.repository.pay_out.PayOutRepository;
import com.kairos.persistence.repository.pay_out.PayOutTransactionMongoRepository;
import com.kairos.persistence.repository.period.PlanningPeriodMongoRepository;
import com.kairos.persistence.repository.shift.ShiftMongoRepository;
import com.kairos.persistence.repository.time_bank.TimeBankRepository;
import com.kairos.persistence.repository.wta.WorkingTimeAgreementMongoRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.activity.TimeTypeService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.pay_out.PayOutCalculationService;
import com.kairos.service.pay_out.PayOutTransaction;
import com.kairos.service.shift.ShiftService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.*;
import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.ActivityMessagesConstants.MESSAGE_CTA_NOTFOUND;
import static com.kairos.constants.ActivityMessagesConstants.MESSAGE_STAFFEMPLOYMENT_NOTFOUND;
import static com.kairos.constants.AppConstants.ONE_DAY_MINUTES;
import static com.kairos.constants.AppConstants.ORGANIZATION;
import static com.kairos.utils.worktimeagreement.RuletemplateUtils.setDayTypeToCTARuleTemplate;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/*
 * Created By Pradeep singh rajawat
 *  Date-27/01/2018
 *
 * */
@Transactional
@Service
public class TimeBankService{

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeBankService.class);

    @Inject
    private TimeBankRepository timeBankRepository;
    @Inject
    private ShiftMongoRepository shiftMongoRepository;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Inject
    private TimeBankCalculationService timeBankCalculationService;
    @Inject
    private ActivityMongoRepository activityMongoRepository;
    @Inject
    private TimeTypeService timeTypeService;
    @Inject
    private PayOutRepository payOutRepository;
    @Inject
    private PayOutTransactionMongoRepository payOutTransactionMongoRepository;
    @Inject
    private PayOutCalculationService payOutCalculationService;
    @Inject
    private CostTimeAgreementRepository costTimeAgreementRepository;
    @Inject
    private ShiftService shiftService;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private PlanningPeriodMongoRepository planningPeriodMongoRepository;
    @Inject private WorkingTimeAgreementMongoRepository workingTimeAgreementMongoRepository;
    /**
     * @param staffAdditionalInfoDTO
     * @param shift
     * @Description This method is used for update DailyTimebankEntry when Shift Create,Update,Delete
     */
    public void updateTimeBank(StaffAdditionalInfoDTO staffAdditionalInfoDTO, Shift shift, boolean validatedByPlanner) {
        staffAdditionalInfoDTO.getEmployment().setStaffId(shift.getStaffId());
        DailyTimeBankEntry dailyTimeBankEntry = renewDailyTimeBank(staffAdditionalInfoDTO, shift, validatedByPlanner);
        if(isNotNull(dailyTimeBankEntry)) {
            timeBankRepository.save(dailyTimeBankEntry);
        }
    }

    public boolean updateTimeBankForMultipleShifts(StaffAdditionalInfoDTO staffAdditionalInfoDTO, Date startDate, Date endDate) {
        setDayTypeToCTARuleTemplate(staffAdditionalInfoDTO);
        List<DailyTimeBankEntry> updatedDailyTimeBankEntries = new ArrayList<>();
        List<DailyTimeBankEntry> dailyTimeBankEntries = renewDailyTimeBank(staffAdditionalInfoDTO, startDate, endDate, staffAdditionalInfoDTO.getUnitId());
        updatedDailyTimeBankEntries.addAll(dailyTimeBankEntries);
        if(isCollectionNotEmpty(updatedDailyTimeBankEntries)) {
            timeBankRepository.saveEntities(updatedDailyTimeBankEntries);
        }
        return true;
    }

    public void saveTimeBanksAndPayOut(List<StaffAdditionalInfoDTO> staffAdditionalInfoDTOS, List<Shift> shifts, Map<BigInteger, ActivityWrapper> activityWrapperMap, Date startDate, Date endDate) {
        Date startDateTime = new DateTime(startDate).withTimeAtStartOfDay().toDate();
        Date endDateTime = new DateTime(endDate).plusDays(1).withTimeAtStartOfDay().toDate();
        Date shiftEndTime = new DateTime(shifts.get(shifts.size() - 1).getEndDate()).plusDays(1).withTimeAtStartOfDay().toDate();
        List<Long> employmentIds = new ArrayList<>(staffAdditionalInfoDTOS.stream().map(staffAdditionalInfoDTO -> staffAdditionalInfoDTO.getEmployment().getId()).collect(Collectors.toSet()));
        timeBankRepository.deleteDailyTimeBank(employmentIds, startDateTime, endDateTime);
        List<ShiftWithActivityDTO> shiftsList = shiftMongoRepository.findAllShiftsBetweenDurationByEmployments(employmentIds, startDateTime, endDateTime);
        Map<String, List<ShiftWithActivityDTO>> shiftDateMap = shiftsList.stream().collect(Collectors.groupingBy(k -> k.getEmploymentId() + "-" + DateUtils.asLocalDate(k.getStartDate())));
        List<DailyTimeBankEntry> dailyTimeBanks = new ArrayList<>();
        List<PayOutPerShift> payOutPerShiftList = payOutRepository.findAllByEmploymentsAndDate(employmentIds, startDateTime, endDateTime);
        Map<BigInteger, PayOutPerShift> shiftAndPayOutMap = payOutPerShiftList.stream().collect(Collectors.toMap(k -> k.getShiftId(), v -> v));
        List<PayOutPerShift> payOutPerShifts = new ArrayList<>();
        Set<DateTimeInterval> dateTimeIntervals = timeBankCalculationService.getPlanningPeriodIntervals(shifts.get(0).getUnitId(), startDateTime, endDateTime);
        while (startDateTime.before(shiftEndTime)) {
            for (StaffAdditionalInfoDTO staffAdditionalInfoDTO : staffAdditionalInfoDTOS) {
                List<ShiftWithActivityDTO> shiftWithActivityDTOS = shiftDateMap.getOrDefault(staffAdditionalInfoDTO.getEmployment().getId() + "-" + DateUtils.asLocalDate(startDateTime), new ArrayList<>());
                DateTimeInterval interval = new DateTimeInterval(startDateTime.getTime(), DateUtils.asDate(DateUtils.asZoneDateTime(startDateTime).plusDays(1)).getTime());
                staffAdditionalInfoDTO.getEmployment().setStaffId(staffAdditionalInfoDTO.getId());
                DailyTimeBankEntry dailyTimeBank = timeBankCalculationService.calculateDailyTimeBank(staffAdditionalInfoDTO, interval, shiftWithActivityDTOS, null, dateTimeIntervals, staffAdditionalInfoDTO.getDayTypes(), false,false);
                if(dailyTimeBank != null) {
                    dailyTimeBanks.add(dailyTimeBank);
                }
                DateTimeInterval dateTimeInterval = new DateTimeInterval(startDateTime.getTime(), plusDays(startDateTime, 1).getTime());
                List<Shift> shiftList = ObjectMapperUtils.copyPropertiesOfListByMapper(shiftWithActivityDTOS, Shift.class);
                List<BigInteger> activityIdsList = shiftList.stream().flatMap(s -> s.getActivities().stream().map(ShiftActivity::getActivityId)).distinct().collect(Collectors.toList());
                List<ActivityWrapper> activities = activityMongoRepository.findActivitiesAndTimeTypeByActivityId(activityIdsList);
                activityWrapperMap = activities.stream().collect(Collectors.toMap(k -> k.getActivity().getId(), v -> v));
                for (Shift shift : shiftList) {
                    PayOutPerShift payOutPerShift = shiftAndPayOutMap.getOrDefault(shift.getId(), new PayOutPerShift(shift.getId(), shift.getEmploymentId(), shift.getStaffId(), dateTimeInterval.getStartLocalDate(), shift.getUnitId()));
                    payOutPerShift = payOutCalculationService.calculateAndUpdatePayOut(dateTimeInterval, staffAdditionalInfoDTO.getEmployment(), shift, activityWrapperMap, payOutPerShift, staffAdditionalInfoDTO.getDayTypes());
                    if(payOutPerShift.getTotalPayOutMinutes() > 0) {
                        payOutPerShifts.add(payOutPerShift);
                    }
                }
            }
            startDateTime = plusDays(startDateTime, 1);
        }
        Map<BigInteger, Shift> shiftIdAndShiftMap = shifts.stream().collect(Collectors.toMap(k -> k.getId(), v -> v));
        shiftAndPayOutMap.entrySet().forEach(k -> {
            if(!shiftIdAndShiftMap.containsKey(k.getKey())) {
                PayOutPerShift deletePayOutPerShift = shiftAndPayOutMap.get(k.getKey());
                deletePayOutPerShift.setDeleted(true);
                payOutPerShifts.add(deletePayOutPerShift);
            }
        });
        if(!payOutPerShifts.isEmpty()) {
            payOutRepository.saveEntities(payOutPerShifts);
        }
        if(!dailyTimeBanks.isEmpty()) {
            timeBankRepository.saveEntities(dailyTimeBanks);
        }
        if(CollectionUtils.isNotEmpty(dailyTimeBanks)) {
            updateBonusHoursOfTimeBankInShift(shiftsList, shifts);
        }
    }

    private DailyTimeBankEntry renewDailyTimeBank(StaffAdditionalInfoDTO staffAdditionalInfoDTO, Shift shift, boolean validatedByPlanner) {
        DateTime startDate = new DateTime(shift.getStartDate()).withTimeAtStartOfDay();
        DateTime endDate = startDate.plusDays(1);
        DailyTimeBankEntry dailyTimeBankEntry = timeBankRepository.findByEmploymentAndDate(staffAdditionalInfoDTO.getEmployment().getId(), asLocalDate(startDate));
        Set<DateTimeInterval> dateTimeIntervals = timeBankCalculationService.getPlanningPeriodIntervals(shift.getUnitId(), startDate.toDate(), endDate.toDate());
        List<ShiftWithActivityDTO> shiftWithActivityDTOS = shiftMongoRepository.findAllShiftsBetweenDurationByEmploymentId(staffAdditionalInfoDTO.getEmployment().getId(), startDate.toDate(), endDate.toDate(),false);
        DateTimeInterval interval = new DateTimeInterval(startDate.getMillis(), endDate.getMillis());
        List<ShiftWithActivityDTO> shiftWithActivityDTOList = getShiftsByInterval(shiftWithActivityDTOS, interval);
        staffAdditionalInfoDTO.getEmployment().setStaffId(staffAdditionalInfoDTO.getId());
        dailyTimeBankEntry = timeBankCalculationService.calculateDailyTimeBank(staffAdditionalInfoDTO, interval, shiftWithActivityDTOList, dailyTimeBankEntry, dateTimeIntervals, staffAdditionalInfoDTO.getDayTypes(), validatedByPlanner,false);
        updateBonusHoursOfTimeBankInShift(shiftWithActivityDTOS, Arrays.asList(shift));
        if(staffAdditionalInfoDTO.getUserAccessRoleDTO().getManagement()){
            dailyTimeBankEntry = calculateTimebankForDraftShift(staffAdditionalInfoDTO,startDate.toDate(),endDate.toDate(),dailyTimeBankEntry);
        }
        return dailyTimeBankEntry;
    }

    private List<DailyTimeBankEntry> renewDailyTimeBank(StaffAdditionalInfoDTO staffAdditionalInfoDTO, Date startDateTime, @Nullable Date endDateTime, Long unitId) {
        Date startDate = getStartOfDay(startDateTime);
        Date endDate = isNotNull(endDateTime) ? getEndOfDay(endDateTime) : null;
        List<DailyTimeBankEntry> dailyTimeBankEntries = timeBankRepository.findAllDailyTimeBankByEmploymentIdAndBetweenDates(staffAdditionalInfoDTO.getEmployment().getId(), startDate, endDate);
        Map<LocalDate,DailyTimeBankEntry> dateDailyTimeBankEntryMap = dailyTimeBankEntries.stream().collect(Collectors.toMap(k->k.getDate(),v->v));
        List<DailyTimeBankEntry> dailyTimeBanks = new ArrayList<>();
        List<ShiftWithActivityDTO> shiftWithActivityDTOS = shiftMongoRepository.findAllShiftsBetweenDurationByEmploymentId(staffAdditionalInfoDTO.getEmployment().getId(), startDate, endDate,false);
        if(isCollectionNotEmpty(shiftWithActivityDTOS)) {
            if(isNull(endDate)) {
                endDate = getEndOfDay(shiftWithActivityDTOS.get(shiftWithActivityDTOS.size() - 1).getEndDate());
            }
            Set<DateTimeInterval> dateTimeIntervals = timeBankCalculationService.getPlanningPeriodIntervals(unitId, startDate, endDate);
            List<Shift> shifts = shiftMongoRepository.findAllOverlappedShiftsAndEmploymentId(staffAdditionalInfoDTO.getEmployment().getId(), startDate, endDate);
            while (startDate.before(endDate)) {
                DateTimeInterval interval = new DateTimeInterval(startDate.getTime(), plusDays(startDate, 1).getTime());
                List<ShiftWithActivityDTO> shiftWithActivityDTOList = getShiftsByInterval(shiftWithActivityDTOS, interval);
                staffAdditionalInfoDTO.getEmployment().setStaffId(staffAdditionalInfoDTO.getId());
                DailyTimeBankEntry dailyTimeBank = timeBankCalculationService.calculateDailyTimeBank(staffAdditionalInfoDTO, interval, shiftWithActivityDTOList, dateDailyTimeBankEntryMap.get(asLocalDate(startDate)), dateTimeIntervals, staffAdditionalInfoDTO.getDayTypes(), false,false);
                if(dailyTimeBank != null) {
                    if(staffAdditionalInfoDTO.getUserAccessRoleDTO().getManagement()){
                        dailyTimeBank = calculateTimebankForDraftShift(staffAdditionalInfoDTO,interval.getStartDate(),interval.getEndDate(),dailyTimeBank);
                    }
                    dailyTimeBanks.add(dailyTimeBank);
                }
                startDate = plusDays(startDate, 1);
            }
            if(CollectionUtils.isNotEmpty(dailyTimeBanks)) {
                updateBonusHoursOfTimeBankInShift(shiftWithActivityDTOS, shifts);
            }
        }
        return dailyTimeBanks;
    }

    /**
     * @param employmentId
     * @return employmentWithCtaDetailsDTO
     */
    public EmploymentWithCtaDetailsDTO getCostTimeAgreement(Long employmentId, Date startDate, Date endDate) {
        EmploymentWithCtaDetailsDTO employmentWithCtaDetailsDTO = userIntegrationService.getEmploymentDetails(employmentId);
        if(!Optional.ofNullable(employmentWithCtaDetailsDTO).isPresent()) {
            exceptionService.dataNotFoundException(MESSAGE_STAFFEMPLOYMENT_NOTFOUND);
        }
        List<CTAResponseDTO> ctaResponseDTOS = costTimeAgreementRepository.getCTAByEmploymentIdBetweenDate(employmentId, startDate, endDate);
        List<CTARuleTemplateDTO> ruleTemplates = ctaResponseDTOS.stream().flatMap(ctaResponseDTO -> ctaResponseDTO.getRuleTemplates().stream()).collect(toList());
        ruleTemplates = ruleTemplates.stream().filter(ObjectUtils.distinctByKey(CTARuleTemplateDTO::getName)).collect(toList());
        employmentWithCtaDetailsDTO.setCtaRuleTemplates(ruleTemplates);
        return employmentWithCtaDetailsDTO;
    }

    /**
     * @param unitId
     * @param employmentId
     * @param query
     * @param startDate
     * @param endDate
     * @return TimeBankAndPayoutDTO
     */
    public TimeBankAndPayoutDTO getAdvanceViewTimeBank(Long unitId, Long employmentId, String query, Date startDate, Date endDate) {
        endDate = asDate(DateUtils.asLocalDate(endDate).plusDays(1));
        List<DailyTimeBankEntry> dailyTimeBanks = timeBankRepository.findAllByEmploymentAndDate(employmentId, startDate, endDate);
        List<ShiftWithActivityDTO> shiftQueryResultWithActivities = shiftMongoRepository.findAllShiftsBetweenDurationByEmploymentId(employmentId, startDate, endDate,false);
        EmploymentWithCtaDetailsDTO employmentWithCtaDetailsDTO = getCostTimeAgreement(employmentId, startDate, endDate);
        long totalTimeBankBeforeStartDate = 0;
        List<TimeTypeDTO> timeTypeDTOS = timeTypeService.getAllTimeTypeByCountryId(employmentWithCtaDetailsDTO.getCountryId());
        if(new DateTime(startDate).isAfter(toJodaDateTime(employmentWithCtaDetailsDTO.getStartDate()))) {
            Interval interval = new Interval(toJodaDateTime(employmentWithCtaDetailsDTO.getStartDate()), new DateTime(startDate));
            //totaltimebank is timebank without daily timebank entries
            List<DailyTimeBankEntry> dailyTimeBanksBeforeStartDate = timeBankRepository.findAllByEmploymentIdAndStartDate(employmentId, new DateTime(startDate).toDate());
            Set<DateTimeInterval> planningPeriodIntervals = timeBankCalculationService.getPlanningPeriodIntervals(unitId, interval.getStart().toDate(), interval.getEnd().toDate());
            int totalTimeBank = timeBankCalculationService.calculateTimeBankForInterval(planningPeriodIntervals, interval, employmentWithCtaDetailsDTO, false, dailyTimeBanksBeforeStartDate, false);
            totalTimeBankBeforeStartDate = isCollectionNotEmpty(dailyTimeBanksBeforeStartDate) ? dailyTimeBanksBeforeStartDate.stream().mapToInt(dailyTimeBank -> dailyTimeBank.getDeltaTimeBankMinutes()).sum() : 0;
            totalTimeBankBeforeStartDate = totalTimeBankBeforeStartDate - totalTimeBank;
        }
        totalTimeBankBeforeStartDate += employmentWithCtaDetailsDTO.getAccumulatedTimebankMinutes();
        List<PayOutTransaction> payOutTransactions = payOutTransactionMongoRepository.findAllByEmploymentIdAndDate(employmentId, startDate, endDate);
        List<Interval> intervals = timeBankCalculationService.getAllIntervalsBetweenDates(startDate, endDate, query);
        Map<Interval, List<PayOutTransaction>> payoutTransactionIntervalMap = timeBankCalculationService.getPayoutTrasactionIntervalsMap(intervals, payOutTransactions);
        return timeBankCalculationService.getTimeBankAdvanceView(intervals, unitId, totalTimeBankBeforeStartDate, startDate, endDate, query, shiftQueryResultWithActivities, dailyTimeBanks, employmentWithCtaDetailsDTO, timeTypeDTOS, payoutTransactionIntervalMap);
    }

    /**
     * @param employmentId
     * @param year
     * @return TimeBankDTO
     */
    public TimeBankDTO getOverviewTimeBank(Long unitId, Long employmentId, Integer year) {
        EmploymentWithCtaDetailsDTO employmentWithCtaDetailsDTO = userIntegrationService.getEmploymentDetails(employmentId);
        Interval interval = getIntervalByDateTimeBank(employmentWithCtaDetailsDTO, year);
        List<DailyTimeBankEntry> dailyTimeBankEntries = new ArrayList<>();
        if(interval.getStart().getYear() <= new DateTime().getYear()) {
            dailyTimeBankEntries = timeBankRepository.findAllByEmploymentAndDate(employmentId, interval.getStart().toDate(), interval.getEnd().toDate());
        }
        TimeBankDTO timeBankDTO = timeBankCalculationService.getTimeBankOverview(unitId, employmentId, interval.getStart().dayOfYear().withMinimumValue(), interval.getEnd().dayOfYear().withMaximumValue(), dailyTimeBankEntries, employmentWithCtaDetailsDTO);
        Long actualTimebankMinutes = getAccumulatedTimebankAndDelta(employmentId,unitId,true);
        timeBankDTO.setActualTimebankMinutes(actualTimebankMinutes);
        return timeBankDTO;
    }

    public TimeBankVisualViewDTO getTimeBankForVisualView(Long unitId, Long employmentId, String query, Integer value, Date startDate, Date endDate) {
        ZonedDateTime endZonedDate = null;
        ZonedDateTime startZonedDate = null;
        if(StringUtils.isNotEmpty(query)) {
            if(query.equals(AppConstants.WEEK)) {
                startZonedDate = ZonedDateTime.now().with(ChronoField.ALIGNED_WEEK_OF_YEAR, value).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)).truncatedTo(ChronoUnit.DAYS);
                endZonedDate = startZonedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            } else if(query.equals(AppConstants.MONTH)) {
                startZonedDate = ZonedDateTime.now().with(ChronoField.MONTH_OF_YEAR, value).with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
                endZonedDate = startZonedDate.with(TemporalAdjusters.lastDayOfMonth());

            }
            startDate = DateUtils.getDateByZoneDateTime(startZonedDate);
            endDate = DateUtils.getDateByZoneDateTime(endZonedDate);
        }
        EmploymentWithCtaDetailsDTO employmentWithCtaDetailsDTO = getCostTimeAgreement(employmentId, startDate, endDate);
        DateTimeInterval interval = new DateTimeInterval(startDate, endDate);
        DailyTimeBankEntry dailyTimeBankEntry = timeBankRepository.findLastTimeBankByEmploymentId(employmentId);
        List<ShiftWithActivityDTO> shifts = shiftMongoRepository.findAllShiftsBetweenDurationByEmploymentId(employmentId, startDate, endDate,false);
        List<DailyTimeBankEntry> dailyTimeBankEntries = timeBankRepository.findAllByEmploymentAndDate(employmentId, startDate, endDate);
        Long countryId = userIntegrationService.getCountryIdOfOrganization(unitId);
        Map<String, List<TimeType>> presenceAbsenceTimeTypeMap = timeTypeService.getPresenceAbsenceTimeType(countryId);
        return timeBankCalculationService.getVisualViewTimeBank(interval, dailyTimeBankEntry, shifts, dailyTimeBankEntries, presenceAbsenceTimeTypeMap, employmentWithCtaDetailsDTO);
    }

    /**
     * @param employmentWithCtaDetailsDTO
     * @param year
     * @return Interval
     */
    private Interval getIntervalByDateTimeBank(EmploymentWithCtaDetailsDTO employmentWithCtaDetailsDTO, Integer year) {
        ZonedDateTime startDate = ZonedDateTime.now().withYear(year).with(TemporalAdjusters.firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime endDate = startDate.with(TemporalAdjusters.lastDayOfYear());
        Date employmentStartDate = employmentWithCtaDetailsDTO.getStartDate().isAfter(LocalDate.now()) ? new DateTime().withTimeAtStartOfDay().toDate() : asDate(employmentWithCtaDetailsDTO.getStartDate());
        Date employmentEndDate = employmentWithCtaDetailsDTO.getEndDate() == null || employmentWithCtaDetailsDTO.getEndDate().isAfter(LocalDate.now()) ? new DateTime().withTimeAtStartOfDay().toDate() : asDate(employmentWithCtaDetailsDTO.getEndDate());
        Interval employmentInterval = new Interval(employmentStartDate.getTime(), employmentEndDate.getTime());
        Interval selectedInterval = new Interval(startDate.toInstant().toEpochMilli(), endDate.toInstant().toEpochMilli());
        Interval interval = selectedInterval.overlap(employmentInterval);
        if(interval == null) {
            interval = new Interval(new DateTime().withTimeAtStartOfDay(), new DateTime().withTimeAtStartOfDay());
        }
        return interval;
    }

    /**
     * @param startDate
     * @param staffAdditionalInfoDTO
     * @return
     * @Desc to update Time Bank after applying function in Employment
     */
    public boolean updateTimeBankOnFunctionChange(Date startDate, StaffAdditionalInfoDTO staffAdditionalInfoDTO) {
        Date endDate = plusMinutes(startDate, (int) ONE_DAY_MINUTES);
        CTAResponseDTO ctaResponseDTO = costTimeAgreementRepository.getCTAByEmploymentIdAndDate(staffAdditionalInfoDTO.getEmployment().getId(), startDate);
        if(ctaResponseDTO == null) {
            exceptionService.dataNotFoundException(MESSAGE_CTA_NOTFOUND);
        }
        staffAdditionalInfoDTO.getEmployment().setCtaRuleTemplates(ctaResponseDTO.getRuleTemplates());
        setDayTypeToCTARuleTemplate(staffAdditionalInfoDTO);
        List<DailyTimeBankEntry> dailyTimeBanks = renewDailyTimeBank(staffAdditionalInfoDTO, startDate, endDate, staffAdditionalInfoDTO.getUnitId());
        if(!dailyTimeBanks.isEmpty()) {
            timeBankRepository.saveEntities(dailyTimeBanks);
        }
        return true;
    }

    /**
     * This function is used to update TimeBank when Staff Personalized CTA
     * or individual employmentLine is changed at a time
     *
     * @param employmentId
     * @param startDate
     * @param staffAdditionalInfoDTO
     * @return
     */
    public boolean updateTimeBankOnEmploymentModification(BigInteger ctaId, Long employmentId, Date startDate, Date endDate, StaffAdditionalInfoDTO staffAdditionalInfoDTO) {
        Map<LocalDate, CTAResponseDTO> ctaResponseDTOMap = new HashMap<>();
        List<Shift> shifts = shiftMongoRepository.findAllShiftByIntervalAndEmploymentId(staffAdditionalInfoDTO.getEmployment().getId(), startDate, endDate);
        List<DailyTimeBankEntry> dailyTimeBanks = new ArrayList<>(shifts.size());
        for (Shift shift : shifts) {
            LocalDate shiftDate = DateUtils.asLocalDate(shift.getStartDate());
            CTAResponseDTO ctaResponseDTO;
            if(Optional.ofNullable(ctaId).isPresent()) {
                ctaResponseDTO = costTimeAgreementRepository.getOneCtaById(ctaId);
            } else {
                ctaResponseDTO = ctaResponseDTOMap.getOrDefault(shiftDate, costTimeAgreementRepository.getCTAByEmploymentIdAndDate(employmentId, DateUtils.asDate(shiftDate)));
            }
            if(ctaResponseDTO == null) {
                exceptionService.dataNotFoundException(MESSAGE_CTA_NOTFOUND);
            }
            staffAdditionalInfoDTO.getEmployment().setCtaRuleTemplates(ctaResponseDTO.getRuleTemplates());
            setDayTypeToCTARuleTemplate(staffAdditionalInfoDTO);
            dailyTimeBanks.add(renewDailyTimeBank(staffAdditionalInfoDTO, shift, false));
            ctaResponseDTOMap.put(shiftDate, ctaResponseDTO);
        }
        if(!dailyTimeBanks.isEmpty()) {
            timeBankRepository.saveEntities(dailyTimeBanks);
        }
        return true;
    }

    private List<ShiftWithActivityDTO> getShiftsByInterval(List<ShiftWithActivityDTO> shiftWithActivityDTOS, DateTimeInterval interval) {
        DateTimeInterval dateTimeInterval = new DateTimeInterval(interval.getStartMillis(), interval.getEndMillis());
        List<ShiftWithActivityDTO> shifts = new ArrayList<>();
        shiftWithActivityDTOS.forEach(shift -> {
            if(dateTimeInterval.contains(shift.getStartDate())) {
                shifts.add(shift);
            }
        });
        return shifts;
    }

    private void updateBonusHoursOfTimeBankInShift(List<ShiftWithActivityDTO> shiftWithActivityDTOS, List<Shift> shifts) {
        if(CollectionUtils.isNotEmpty(shifts)) {
            Map<BigInteger, ShiftActivityDTO> shiftActivityDTOMap = shiftWithActivityDTOS.stream().flatMap(shift1 -> shift1.getActivities().stream()).collect(Collectors.toMap(k -> k.getId(), v -> v));
            for (Shift shift : shifts) {
                int timeBankCtaBonusMinutes = 0;
                int plannedMinutesOfTimebank = 0;
                int timeBankScheduledMinutes = 0;
                for (ShiftActivity shiftActivity : shift.getActivities()) {
                    if(shiftActivityDTOMap.containsKey(shiftActivity.getId())) {
                        ShiftActivityDTO shiftActivityDTO = shiftActivityDTOMap.get(shiftActivity.getId());
                        shiftActivity.setTimeBankCtaBonusMinutes(shiftActivityDTO.getTimeBankCtaBonusMinutes());
                        timeBankCtaBonusMinutes += shiftActivityDTO.getTimeBankCtaBonusMinutes();
                        shiftActivity.setTimeBankCTADistributions(ObjectMapperUtils.copyPropertiesOfListByMapper(shiftActivityDTO.getTimeBankCTADistributions(), TimeBankCTADistribution.class));
                        shiftActivity.setPlannedMinutesOfTimebank(shiftActivityDTO.getScheduledMinutesOfTimebank() + shiftActivityDTO.getTimeBankCtaBonusMinutes());
                        plannedMinutesOfTimebank += shiftActivity.getPlannedMinutesOfTimebank();
                        shiftActivity.setScheduledMinutesOfTimebank(shiftActivityDTO.getScheduledMinutesOfTimebank());
                        timeBankScheduledMinutes+=shiftActivity.getScheduledMinutesOfTimebank();
                    }
                }
                shift.setScheduledMinutesOfTimebank(timeBankScheduledMinutes);
                shift.setTimeBankCtaBonusMinutes(timeBankCtaBonusMinutes);
                shift.setPlannedMinutesOfTimebank(plannedMinutesOfTimebank);
            }
            shiftMongoRepository.saveEntities(shifts);
        }
    }

    public boolean renewTimeBankOfShifts() {
        List<Shift> shifts = shiftMongoRepository.findAllByDeletedFalse();
        Map<Long, StaffAdditionalInfoDTO> staffAdditionalInfoDTOMap = new HashMap<>();
        List<DailyTimeBankEntry> dailyTimeBanks = new ArrayList<>(shifts.size());
        for (Shift shift : shifts) {
            try {
                StaffAdditionalInfoDTO staffAdditionalInfoDTO = userIntegrationService.verifyUnitEmploymentOfStaff(DateUtils.asLocalDate(shift.getActivities().get(0).getStartDate()), shift.getStaffId(), ORGANIZATION, shift.getEmploymentId(), new HashSet<>());
                CTAResponseDTO ctaResponseDTO = costTimeAgreementRepository.getCTAByEmploymentIdAndDate(staffAdditionalInfoDTO.getEmployment().getId(), shift.getStartDate());
                if(Optional.ofNullable(ctaResponseDTO).isPresent() && CollectionUtils.isNotEmpty(ctaResponseDTO.getRuleTemplates())) {
                    staffAdditionalInfoDTO.getEmployment().setCtaRuleTemplates(ctaResponseDTO.getRuleTemplates());
                    setDayTypeToCTARuleTemplate(staffAdditionalInfoDTO);
                    staffAdditionalInfoDTOMap.put(staffAdditionalInfoDTO.getEmployment().getId(), staffAdditionalInfoDTO);
                    if(staffAdditionalInfoDTOMap.containsKey(shift.getEmploymentId())) {
                        dailyTimeBanks.add(renewDailyTimeBank(staffAdditionalInfoDTOMap.get(shift.getEmploymentId()), shift, false));
                    }
                }
            } catch (Exception e) {
                LOGGER.info("staff is not the part of this Unit");
            }
            if(staffAdditionalInfoDTOMap.containsKey(shift.getEmploymentId()) && CollectionUtils.isNotEmpty(staffAdditionalInfoDTOMap.get(shift.getEmploymentId()).getEmployment().getCtaRuleTemplates())) {
                DailyTimeBankEntry dailyTimeBankEntries = renewDailyTimeBank(staffAdditionalInfoDTOMap.get(shift.getEmploymentId()), shift, false);
                dailyTimeBanks.add(dailyTimeBankEntries);
            }
        }
        if(CollectionUtils.isNotEmpty(dailyTimeBanks)) {
            timeBankRepository.saveEntities(dailyTimeBanks);
        }
        return true;

    }

    public void updateDailyTimeBankEntries(List<Shift> shifts, StaffEmploymentDetails staffEmploymentDetails, List<DayTypeDTO> dayTypeDTOS) {
        StaffAdditionalInfoDTO staffAdditionalInfoDTO = new StaffAdditionalInfoDTO(staffEmploymentDetails, dayTypeDTOS);
        if(isCollectionNotEmpty(shifts)) {
            shifts.sort(Comparator.comparing(Shift::getStartDate));
            Date startDate = shifts.get(0).getStartDate();
            Date endDate = shifts.get(shifts.size() - 1).getEndDate();
            updateTimeBankForMultipleShifts(staffAdditionalInfoDTO, startDate, endDate);
        }
    }

    public List<ShiftDTO> updateTimebankDetailsInShiftDTO(List<ShiftDTO> shiftDTOS) {
        if(isCollectionNotEmpty(shiftDTOS)) {
            for (ShiftDTO shiftDTO : shiftDTOS) {
                int plannedMinutes = 0;
                int timeBankCtaBonusMinutes = 0;
                int scheduledMinutes = 0;
                for (ShiftActivityDTO activity : shiftDTO.getActivities()) {
                    activity.setPlannedMinutesOfTimebank(activity.getScheduledMinutesOfTimebank() + activity.getTimeBankCtaBonusMinutes());
                    plannedMinutes += activity.getPlannedMinutesOfTimebank();
                    timeBankCtaBonusMinutes += activity.getTimeBankCtaBonusMinutes();
                    scheduledMinutes += activity.getScheduledMinutes();
                }
                shiftDTO.setPlannedMinutesOfTimebank(plannedMinutes);
                shiftDTO.setTimeBankCtaBonusMinutes(timeBankCtaBonusMinutes);
                shiftDTO.setScheduledMinutes(scheduledMinutes);
            }
        }
        return shiftDTOS;
    }

    private EmploymentWithCtaDetailsDTO getEmploymentDetailDTO(StaffAdditionalInfoDTO staffAdditionalInfoDTO, Long unitId) {
        return new EmploymentWithCtaDetailsDTO(staffAdditionalInfoDTO.getEmployment().getId(), staffAdditionalInfoDTO.getEmployment().getTotalWeeklyHours(), staffAdditionalInfoDTO.getEmployment().getTotalWeeklyMinutes(), staffAdditionalInfoDTO.getEmployment().getWorkingDaysInWeek(), staffAdditionalInfoDTO.getEmployment().getStaffId(), staffAdditionalInfoDTO.getEmployment().getStartDate(), staffAdditionalInfoDTO.getEmployment().getEndDate(), staffAdditionalInfoDTO.getEmployment().getEmploymentLines(), staffAdditionalInfoDTO.getEmployment().getAccumulatedTimebankMinutes(), staffAdditionalInfoDTO.getEmployment().getAccumulatedTimebankDate(),unitId);
    }

    public void deleteDuplicateEntry() {
        List<DailyTimeBankEntry> dailyTimeBankEntries = timeBankRepository.findAllAndDeletedFalse();
        Map<Long, TreeMap<LocalDate, DailyTimeBankEntry>> employmentIdAndDateMap = new TreeMap<>();
        List<DailyTimeBankEntry> duplicateEntry = new ArrayList<>();
        for (DailyTimeBankEntry dailyTimeBankEntry : dailyTimeBankEntries) {
            if(employmentIdAndDateMap.containsKey(dailyTimeBankEntry.getEmploymentId())) {
                Map<LocalDate, DailyTimeBankEntry> localDateDateMap = employmentIdAndDateMap.get(dailyTimeBankEntry.getEmploymentId());
                if(localDateDateMap.containsKey(dailyTimeBankEntry.getDate())) {
                    DailyTimeBankEntry dailyTimeBankEntry1 = localDateDateMap.get(dailyTimeBankEntry.getDate());
                    if(dailyTimeBankEntry1.getUpdatedAt().after(dailyTimeBankEntry.getUpdatedAt())) {
                        duplicateEntry.add(dailyTimeBankEntry);
                    } else {
                        duplicateEntry.add(dailyTimeBankEntry1);
                    }
                } else {
                    localDateDateMap.put(dailyTimeBankEntry.getDate(), dailyTimeBankEntry);
                    LOGGER.info("Date Map :" + localDateDateMap.size());
                    LOGGER.info("employmentId Map :" + employmentIdAndDateMap.get(dailyTimeBankEntry.getEmploymentId()).size());
                }

            } else {
                employmentIdAndDateMap.put(dailyTimeBankEntry.getEmploymentId(), new TreeMap<>());
            }
        }
        LOGGER.info("Duplicate remove entry count is " + duplicateEntry.size());
        timeBankRepository.deleteAll(duplicateEntry);
    }

    public boolean updateDailyTimeBankOnCTAChangeOfEmployment(StaffAdditionalInfoDTO staffAdditionalInfoDTO, CTAResponseDTO ctaResponseDTO) {
        Date startDate = asDate(ctaResponseDTO.getStartDate());
        Date endDate = isNotNull(ctaResponseDTO.getEndDate()) ? asDate(ctaResponseDTO.getEndDate()) : null;
        staffAdditionalInfoDTO.getEmployment().setCtaRuleTemplates(ctaResponseDTO.getRuleTemplates());
        return updateTimeBankForMultipleShifts(staffAdditionalInfoDTO, startDate, endDate);
    }

    public boolean updateDailyTimeBankEntriesForStaffs(List<Shift> shifts) {
        if(isCollectionNotEmpty(shifts)) {
            Set<Long> staffIds = shifts.stream().map(shift -> shift.getStaffId()).collect(Collectors.toSet());
            Set<Long> employmentIds = shifts.stream().map(shift -> shift.getEmploymentId()).collect(Collectors.toSet());
            List<NameValuePair> requestParam = new ArrayList<>();
            requestParam.add(new BasicNameValuePair("staffIds", staffIds.toString()));
            requestParam.add(new BasicNameValuePair("employmentIds", employmentIds.toString()));
            List<StaffAdditionalInfoDTO> staffAdditionalInfoDTOS = userIntegrationService.getStaffAditionalDTOS(shifts.get(0).getUnitId(), requestParam);
            Date startDateTime = new DateTime(shifts.get(0).getStartDate()).withTimeAtStartOfDay().toDate();
            Date endDateTime = new DateTime(shifts.get(shifts.size() - 1).getEndDate()).plusDays(1).withTimeAtStartOfDay().toDate();
            List<DailyTimeBankEntry> updateDailyTimeBanks = new ArrayList<>();
            List<CTAResponseDTO> ctaResponseDTOS = costTimeAgreementRepository.getCTAByEmploymentIdsAndDate(new ArrayList<>(employmentIds), startDateTime, endDateTime);
            Map<Long, List<CTAResponseDTO>> employmentAndCTAResponseMap = ctaResponseDTOS.stream().collect(groupingBy(CTAResponseDTO::getEmploymentId));
            Map<Long, StaffAdditionalInfoDTO> staffAdditionalInfoMap = staffAdditionalInfoDTOS.stream().collect(Collectors.toMap(s -> s.getEmployment().getId(), v -> v));
            for (Shift shift : shifts) {
                StaffAdditionalInfoDTO staffAdditionalInfoDTO = staffAdditionalInfoMap.get(shift.getEmploymentId());
                CTAResponseDTO ctaResponseDTO = getCTAByDate(employmentAndCTAResponseMap.get(shift.getEmploymentId()), asLocalDate(shift.getStartDate()));
                staffAdditionalInfoDTO.getEmployment().setCtaRuleTemplates(ctaResponseDTO.getRuleTemplates());
                staffAdditionalInfoDTO.setUnitId(shifts.get(0).getUnitId());
                setDayTypeToCTARuleTemplate(staffAdditionalInfoDTO);
                updateDailyTimeBanks.add(renewDailyTimeBank(staffAdditionalInfoDTO, shift, false));
            }
            if(isCollectionNotEmpty(updateDailyTimeBanks)) {
                timeBankRepository.saveEntities(updateDailyTimeBanks);
            }
        }
        return true;
    }

    private CTAResponseDTO getCTAByDate(List<CTAResponseDTO> ctaResponseDTOS, LocalDate shiftDate) {
        CTAResponseDTO ctaResponse = null;
        for (CTAResponseDTO ctaResponseDTO : ctaResponseDTOS) {
            DateTimeInterval dateTimeInterval = new DateTimeInterval(asDate(ctaResponseDTO.getStartDate()),isNotNull(ctaResponseDTO.getEndDate()) ? asDateEndOfDay(ctaResponseDTO.getEndDate()) : asDateEndOfDay(shiftDate));
            if(dateTimeInterval.contains(asDate(shiftDate))) {
                ctaResponse = ctaResponseDTO;
                break;
            }
        }
        return ctaResponse;
    }

    public <T> T getAccumulatedTimebankAndDelta(Long employmentId, Long unitId, Boolean includeActualTimebank) {
        StaffAdditionalInfoDTO staffAdditionalInfoDTO = userIntegrationService.verifyUnitEmploymentOfStaffByEmploymentId(unitId, null, ORGANIZATION, employmentId, new HashSet<>());
        EmploymentWithCtaDetailsDTO employmentWithCtaDetailsDTO = getEmploymentDetailDTO(staffAdditionalInfoDTO, unitId);
        T object;
        PlanningPeriod planningPeriod = planningPeriodMongoRepository.findLastPlaningPeriodEndDate(unitId);
        LocalDate endDate = planningPeriod.getEndDate();
        LocalDate startDate = employmentWithCtaDetailsDTO.getStartDate();
        List<DailyTimeBankEntry> dailyTimeBankEntries = timeBankRepository.findAllByEmploymentIdAndBeforeDate(employmentId, asDate(endDate));
        Set<DateTimeInterval> planningPeriodIntervals = timeBankCalculationService.getPlanningPeriodIntervals(unitId, asDate(startDate), asDate(endDate));
        object = (T)timeBankCalculationService.calculateActualTimebank(planningPeriodIntervals,dailyTimeBankEntries,employmentWithCtaDetailsDTO,endDate,startDate);
        if(isNull(includeActualTimebank)) {
            UserAccessRoleDTO userAccessRoleDTO = userIntegrationService.getAccessOfCurrentLoggedInStaff();
            dailyTimeBankEntries = timeBankRepository.findAllByEmploymentIdAndBeforeDate(employmentId, asDate(endDate));
            planningPeriodIntervals = timeBankCalculationService.getPlanningPeriodIntervals(unitId, asDate(startDate), asDate(endDate));
            object = (T)timeBankCalculationService.getAccumulatedTimebankDTO(planningPeriodIntervals, dailyTimeBankEntries, employmentWithCtaDetailsDTO, startDate, endDate,(Long)object,userAccessRoleDTO);
        }

        return object;
    }

    private List<WTAQueryResultDTO> getTimebankDetails(Long employmentId,Date startDate,Date endDate){
        List<WTAQueryResultDTO> wtaWithMinimumTimebankRuletemplateDetails = new ArrayList<>();
        List<WTAQueryResultDTO> workingTimeAgreements = workingTimeAgreementMongoRepository.getWTAByEmploymentIdAndDatesWithRuleTemplateType(employmentId, startDate, endDate, WTATemplateType.TIME_BANK);
        for (WTAQueryResultDTO workingTimeAgreement : workingTimeAgreements) {
            boolean validWTA = workingTimeAgreement.getRuleTemplates().stream().filter(wtaBaseRuleTemplate -> ((TimeBankWTATemplate)wtaBaseRuleTemplate).getMinMaxSetting().equals(MinMaxSetting.MINIMUM)).findAny().isPresent();
            if(validWTA){
                wtaWithMinimumTimebankRuletemplateDetails.add(workingTimeAgreement);
            }
        }
        return wtaWithMinimumTimebankRuletemplateDetails;
    }

    public void updateDailyTimebank(Long unitId){
        List<Shift> shifts = shiftMongoRepository.findAllByUnitId(unitId);
        for (Shift shift : shifts) {
            StaffAdditionalInfoDTO staffAdditionalInfoDTO = userIntegrationService.verifyUnitEmploymentOfStaff(asLocalDate(shift.getStartDate()), shift.getStaffId(), ORGANIZATION, shift.getEmploymentId(), new HashSet<>());
            CTAResponseDTO ctaResponseDTO = costTimeAgreementRepository.getCTAByEmploymentIdAndDate(staffAdditionalInfoDTO.getEmployment().getId(), shift.getStartDate());
            if(isNotNull(ctaResponseDTO) && isCollectionNotEmpty(ctaResponseDTO.getRuleTemplates()) && isNotNull(staffAdditionalInfoDTO) && isNotNull(staffAdditionalInfoDTO.getEmployment())) {
                staffAdditionalInfoDTO.getEmployment().setCtaRuleTemplates(ctaResponseDTO.getRuleTemplates());
                setDayTypeToCTARuleTemplate(staffAdditionalInfoDTO);
                updateTimeBank(staffAdditionalInfoDTO,shift, false);
            }
        }
    }

    public DailyTimeBankEntry calculateTimebankForDraftShift(StaffAdditionalInfoDTO staffAdditionalInfoDTO,Date startDate,Date endDate,DailyTimeBankEntry dailyTimeBankEntry){
        List<ShiftWithActivityDTO> shiftWithActivityDTOS = shiftMongoRepository.findAllShiftsBetweenDurationByEmploymentIdAndDraftShiftExists(staffAdditionalInfoDTO.getEmployment().getId(), startDate, endDate,false);
        List<ShiftWithActivityDTO> draftShifts = shiftMongoRepository.findAllShiftsBetweenDurationByEmploymentIdAndDraftShiftExists(staffAdditionalInfoDTO.getEmployment().getId(), startDate, endDate,true);
        if(isCollectionNotEmpty(draftShifts)) {
            shiftWithActivityDTOS.addAll(draftShifts);
        }
        Set<DateTimeInterval> dateTimeIntervals = timeBankCalculationService.getPlanningPeriodIntervals(staffAdditionalInfoDTO.getUnitId(), startDate, endDate);
        DateTimeInterval interval = new DateTimeInterval(startDate, endDate);
        staffAdditionalInfoDTO.getEmployment().setStaffId(staffAdditionalInfoDTO.getId());
        if(isCollectionNotEmpty(shiftWithActivityDTOS)) {
            dailyTimeBankEntry = timeBankCalculationService.calculateDailyTimeBank(staffAdditionalInfoDTO, interval, shiftWithActivityDTOS, dailyTimeBankEntry, dateTimeIntervals, staffAdditionalInfoDTO.getDayTypes(), false, true);
            List<BigInteger> shiftIds = shiftWithActivityDTOS.stream().map(shift -> shift.getId()).collect(toList());
            Iterable<Shift> shifts = shiftMongoRepository.findAllById(shiftIds);
            updateBonusHoursOfTimeBankInShift(shiftWithActivityDTOS,(List<Shift>) shifts);
        }else {
            dailyTimeBankEntry.setDraftDeltaTimebankMinutes(0);
            dailyTimeBankEntry.setAnyShiftInDraft(false);
        }
        return dailyTimeBankEntry;
    }
}
