package com.kairos.service.shift;

import com.kairos.commons.custom_exception.DataNotFoundByIdException;
import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.constants.CommonConstants;
import com.kairos.dto.activity.activity.ActivityDTO;
import com.kairos.dto.activity.attendance.AttendanceTimeSlotDTO;
import com.kairos.dto.activity.attendance.TimeAndAttendanceDTO;
import com.kairos.dto.activity.cta.CTAResponseDTO;
import com.kairos.dto.activity.open_shift.OpenShiftResponseDTO;
import com.kairos.dto.activity.shift.*;
import com.kairos.dto.activity.staffing_level.StaffingLevelActivityWithDuration;
import com.kairos.dto.kpermissions.FieldPermissionUserData;
import com.kairos.dto.user.access_group.UserAccessRoleDTO;
import com.kairos.dto.user.access_permission.AccessGroupRole;
import com.kairos.dto.user.country.time_slot.TimeSlotSetDTO;
import com.kairos.dto.user.reason_code.ReasonCodeDTO;
import com.kairos.dto.user.staff.StaffFilterDTO;
import com.kairos.dto.user.staff.staff.StaffAccessRoleDTO;
import com.kairos.dto.user.user.staff.StaffAdditionalInfoDTO;
import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.TimeSlotType;
import com.kairos.enums.TimeTypeEnum;
import com.kairos.enums.kpermissions.FieldLevelPermission;
import com.kairos.enums.phase.PhaseDefaultName;
import com.kairos.enums.shift.*;
import com.kairos.enums.todo.TodoType;
import com.kairos.persistence.model.activity.Activity;
import com.kairos.persistence.model.activity.ActivityWrapper;
import com.kairos.persistence.model.activity.TimeType;
import com.kairos.persistence.model.common.MongoBaseEntity;
import com.kairos.persistence.model.open_shift.OpenShift;
import com.kairos.persistence.model.period.PlanningPeriod;
import com.kairos.persistence.model.phase.Phase;
import com.kairos.persistence.model.shift.CoverShiftSetting;
import com.kairos.persistence.model.shift.Shift;
import com.kairos.persistence.model.shift.ShiftActivity;
import com.kairos.persistence.model.shift.ShiftState;
import com.kairos.persistence.model.staff.personal_details.StaffDTO;
import com.kairos.persistence.model.staffing_level.StaffingLevel;
import com.kairos.persistence.model.todo.Todo;
import com.kairos.persistence.model.wta.WTAQueryResultDTO;
import com.kairos.persistence.repository.activity.ActivityMongoRepository;
import com.kairos.persistence.repository.attendence_setting.TimeAndAttendanceRepository;
import com.kairos.persistence.repository.cta.CostTimeAgreementRepository;
import com.kairos.persistence.repository.open_shift.OpenShiftMongoRepository;
import com.kairos.persistence.repository.open_shift.OpenShiftNotificationMongoRepository;
import com.kairos.persistence.repository.period.PlanningPeriodMongoRepository;
import com.kairos.persistence.repository.phase.PhaseMongoRepository;
import com.kairos.persistence.repository.reason_code.ReasonCodeRepository;
import com.kairos.persistence.repository.shift.ShiftMongoRepository;
import com.kairos.persistence.repository.shift.ShiftStateMongoRepository;
import com.kairos.persistence.repository.staffing_level.StaffingLevelMongoRepository;
import com.kairos.persistence.repository.time_slot.TimeSlotRepository;
import com.kairos.persistence.repository.todo.TodoRepository;
import com.kairos.persistence.repository.wta.WorkingTimeAgreementMongoRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.MongoBaseService;
import com.kairos.service.activity.ActivityService;
import com.kairos.service.activity.StaffActivityDetailsService;
import com.kairos.service.auto_gap_fill_settings.AutoFillGapSettingsService;
import com.kairos.service.day_type.DayTypeService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.pay_out.PayOutService;
import com.kairos.service.phase.PhaseService;
import com.kairos.service.scheduler_service.ActivitySchedulerJobService;
import com.kairos.service.staffing_level.StaffingLevelAvailableCountService;
import com.kairos.service.time_bank.TimeBankService;
import com.kairos.service.todo.TodoService;
import com.kairos.service.unit_settings.ActivityConfigurationService;
import com.kairos.service.wta.WTARuleTemplateCalculationService;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.CommonsExceptionUtil.convertMessage;
import static com.kairos.commons.utils.DateUtils.*;
import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.ActivityMessagesConstants.*;
import static com.kairos.dto.user.access_permission.AccessGroupRole.MANAGEMENT;
import static com.kairos.enums.FilterType.INCLUDE_DRAFT_SHIFT;
import static com.kairos.enums.TimeTypeEnum.GAP;
import static com.kairos.enums.reason_code.ReasonCodeType.TIME_TYPE;
import static com.kairos.enums.shift.ShiftType.SICK;
import static com.kairos.utils.worktimeagreement.RuletemplateUtils.isIgnoredAllRuletemplate;
import static com.kairos.utils.worktimeagreement.RuletemplateUtils.setDayTypeToCTARuleTemplate;

/**
 * Created by vipul on 30/8/17.
 */
@Service
public class ShiftService extends MongoBaseService {
    public static final int MULTIPLE_ACTIVITY_COUNT = 1;
    @Inject
    private ShiftMongoRepository shiftMongoRepository;
    @Inject
    private ActivityMongoRepository activityRepository;
    @Inject
    private TimeAndAttendanceRepository timeAndAttendanceRepository;
    @Inject
    private PhaseService phaseService;
    @Inject
    private TimeBankService timeBankService;
    @Inject
    private PayOutService payOutService;
    @Inject
    private WorkingTimeAgreementMongoRepository workingTimeAgreementMongoRepository;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private OpenShiftMongoRepository openShiftMongoRepository;
    @Inject
    private PlanningPeriodMongoRepository planningPeriodMongoRepository;
    @Inject
    private ShiftValidatorService shiftValidatorService;
    @Inject
    private OpenShiftNotificationMongoRepository openShiftNotificationMongoRepository;
    @Inject
    private CostTimeAgreementRepository costTimeAgreementRepository;
    @Inject
    private ShiftStateMongoRepository shiftStateMongoRepository;
    @Inject
    private ShiftBreakService shiftBreakService;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Inject
    private ShiftReminderService shiftReminderService;
    @Inject
    private PhaseMongoRepository phaseMongoRepository;
    @Inject
    private ShiftStateService shiftStateService;
    @Inject
    private ShiftStatusService shiftStatusService;
    @Inject
    private AbsenceShiftService absenceShiftService;
    @Inject
    private WTARuleTemplateCalculationService wtaRuleTemplateCalculationService;
    @Inject
    private ShiftDetailsService shiftDetailsService;
    @Inject
    private TodoService todoService;
    @Inject
    private TodoRepository todoRepository;
    @Inject
    private ShiftFilterService shiftFilterService;
    @Inject
    private ActivityConfigurationService activityConfigurationService;
    @Inject
    private StaffingLevelMongoRepository staffingLevelMongoRepository;
    @Inject
    private ShiftSickService shiftSickService;
    @Inject
    private ActivitySchedulerJobService activitySchedulerJobService;
    @Inject
    private ActivityService activityService;
    @Inject private ShiftFunctionService shiftFunctionService;
    @Inject private StaffActivityDetailsService staffActivityDetailsService;
    @Inject
    private ReasonCodeRepository reasonCodeRepository;
    @Inject private TimeSlotRepository timeSlotRepository;
    @Inject private DayTypeService dayTypeService;
    @Inject private AutoFillGapSettingsService gapSettingsService;
    @Inject private StaffingLevelAvailableCountService staffingLevelAvailableCountService;
    @Inject private FetchShiftService fetchShiftService;

    public List<ShiftWithViolatedInfoDTO> createShifts(Long unitId, List<ShiftDTO> shiftDTOS, ShiftActionType shiftActionType) {
        List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfoDTOS = new ArrayList<>(shiftDTOS.size());
        for (ShiftDTO shiftDTO : shiftDTOS) {
            shiftWithViolatedInfoDTOS.addAll(createShift(unitId, shiftDTO, shiftActionType));
        }
        return shiftWithViolatedInfoDTOS;
    }

    public List<ShiftWithViolatedInfoDTO> createShift(Long unitId, ShiftDTO shiftDTO, ShiftActionType shiftActionType) {
        shiftDTO.setUnitId(unitId);
        StaffAdditionalInfoDTO staffAdditionalInfoDTO = getStaffAdditionalInfoDTO(shiftDTO.getUnitId(), shiftDTO.getShiftDate(), shiftDTO.getStaffId(), shiftDTO.getEmploymentId());
        ActivityWrapper activityWrapper = activityRepository.findActivityAndTimeTypeByActivityId(shiftDTO.getActivities().get(0).getActivityId());
        updateCTADetailsOfEmployement(shiftDTO.getShiftDate(), staffAdditionalInfoDTO);
        List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfoDTOS;
        if (activityWrapper.getActivity().getActivityRulesSettings().isSicknessSettingValid()) {
            shiftWithViolatedInfoDTOS = shiftSickService.createSicknessShiftsOfStaff(shiftDTO, staffAdditionalInfoDTO, activityWrapper);
        } else {
            shiftValidatorService.checkAbsenceTypeShift(shiftDTO);
            shiftWithViolatedInfoDTOS = validateAndCreateShift(shiftDTO, shiftActionType, staffAdditionalInfoDTO, activityWrapper);
        }
        return shiftWithViolatedInfoDTOS;
    }

    private List<ShiftWithViolatedInfoDTO> validateAndCreateShift(ShiftDTO shiftDTO, ShiftActionType shiftActionType, StaffAdditionalInfoDTO staffAdditionalInfoDTO, ActivityWrapper activityWrapper) {
        List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfoDTOS = new ArrayList<>();
        if ((CommonConstants.FULL_WEEK.equals(activityWrapper.getActivity().getActivityTimeCalculationSettings().getMethodForCalculatingTime()) || CommonConstants.FULL_DAY_CALCULATION.equals(activityWrapper.getActivity().getActivityTimeCalculationSettings().getMethodForCalculatingTime()))) {
            shiftDTO.setStartDate(asDate(shiftDTO.getShiftDate()));
            Object[] shiftOverlapInfo = shiftValidatorService.validateStaffDetailsAndShiftOverlapping(staffAdditionalInfoDTO, shiftDTO, activityWrapper, false);
            shiftWithViolatedInfoDTOS = absenceShiftService.createAbsenceTypeShift(activityWrapper, shiftDTO, staffAdditionalInfoDTO, shiftOverlapInfo, shiftActionType);
        } else {
            Object[] shiftOverlapInfo = shiftValidatorService.validateStaffDetailsAndShiftOverlapping(staffAdditionalInfoDTO, shiftDTO, activityWrapper, false);
            Phase phase = phaseService.getCurrentPhaseByUnitIdAndDate(shiftDTO.getUnitId(), shiftDTO.getActivities().get(0).getStartDate(), null);
            ShiftWithViolatedInfoDTO shiftWithViolatedInfoDTO = saveShift(staffAdditionalInfoDTO, shiftDTO, phase, shiftOverlapInfo, shiftActionType);
            addReasonCode(shiftWithViolatedInfoDTO.getShifts());
            shiftWithViolatedInfoDTOS.add(shiftWithViolatedInfoDTO);
        }
        return shiftWithViolatedInfoDTOS;
    }

    public void addReasonCode(List<ShiftDTO> shiftDTOS) {
        Set<BigInteger> reasonCodeIds = shiftDTOS.stream().flatMap(shift1 -> shift1.getActivities().stream()).map(ShiftActivityDTO::getAbsenceReasonCodeId).collect(Collectors.toSet());
        List<ReasonCodeDTO> reasonCodes=reasonCodeRepository.findAllByIdAndDeletedFalse(reasonCodeIds);
        Map<BigInteger, ReasonCodeDTO> reasonCodeDTOMap = reasonCodes.stream().collect(Collectors.toMap(ReasonCodeDTO::getId, v -> v));
        for (ShiftDTO shift : shiftDTOS) {
            shift.setMultipleActivity(shift.getActivities().size()> MULTIPLE_ACTIVITY_COUNT);
            for (ShiftActivityDTO activity : shift.getActivities()) {
                activity.setReasonCode(reasonCodeDTOMap.get(activity.getAbsenceReasonCodeId()));
            }
        }
    }

    public ShiftWithViolatedInfoDTO saveShift(StaffAdditionalInfoDTO staffAdditionalInfoDTO, ShiftDTO shiftDTO, Phase phase, Object[] shiftOverlapInfo, ShiftActionType shiftActionType) {
        Long functionId = shiftDTO.getFunctionId();
        shiftDTO.setUnitId(staffAdditionalInfoDTO.getUnitId());
        PlanningPeriod planningPeriod = planningPeriodMongoRepository.getPlanningPeriodContainsDate(shiftDTO.getUnitId(), DateUtils.asLocalDate(shiftDTO.getActivities().get(0).getStartDate()));
        if (isNull(planningPeriod)) {
            exceptionService.actionNotPermittedException(MESSAGE_PERIODSETTING_NOTFOUND);
        }
        Shift mainShift = ObjectMapperUtils.copyPropertiesByMapper(shiftDTO, Shift.class);
        WTAQueryResultDTO wtaQueryResultDTO = getWtaQueryResultByEmploymentIdAndDate(staffAdditionalInfoDTO, shiftDTO);
        Map<BigInteger, ActivityWrapper> activityWrapperMap = activityService.getActivityWrapperMap(newArrayList(), shiftDTO);
        mainShift.setPlanningPeriodId(planningPeriod.getId());
        mainShift.setPhaseId(phase.getId());
        List<ShiftActivity> breakActivities = shiftBreakService.updateBreakInShift(shiftDTO.getId() != null, mainShift, activityWrapperMap, staffAdditionalInfoDTO, wtaQueryResultDTO.getBreakRule(), staffAdditionalInfoDTO.getTimeSlotSets(), mainShift,null);
        mainShift.setBreakActivities(breakActivities);
        activityConfigurationService.addPlannedTimeInShift(mainShift, activityWrapperMap, staffAdditionalInfoDTO, false);
        shiftDTO = ObjectMapperUtils.copyPropertiesByMapper(mainShift, ShiftDTO.class);
        ShiftType shiftType =updateShiftType(activityWrapperMap,mainShift);
        shiftDTO.setShiftType(shiftType);
        ShiftWithActivityDTO shiftWithActivityDTO = getShiftWithActivityDTO(shiftDTO, activityWrapperMap, null);
        ShiftWithViolatedInfoDTO shiftWithViolatedInfoDTO = shiftValidatorService.validateShiftWithActivity(phase, wtaQueryResultDTO, shiftWithActivityDTO, staffAdditionalInfoDTO, null, activityWrapperMap, false, false, true);
        if(isNotNull(shiftOverlapInfo[1])){
            shiftWithViolatedInfoDTO.getViolatedRules().setOverlapWithShiftId((BigInteger) shiftOverlapInfo[1]);
            shiftWithViolatedInfoDTO.getViolatedRules().setOverlapMessage(convertMessage(MESSAGE_SHIFT_DATE_STARTANDEND,shiftDTO.getStartDate(),shiftDTO.getEndDate()));
        } else  if ((PhaseDefaultName.TIME_ATTENDANCE.equals(phase.getPhaseEnum()) || shiftWithViolatedInfoDTO.getViolatedRules().getWorkTimeAgreements().isEmpty()) && shiftWithViolatedInfoDTO.getViolatedRules().getActivities().isEmpty()) {
            mainShift = saveShiftWithActivity(activityWrapperMap, mainShift, staffAdditionalInfoDTO, false, functionId, phase, shiftActionType, null);
            shiftDTO = ObjectMapperUtils.copyPropertiesByMapper(isNotNull(mainShift.getDraftShift()) ? mainShift.getDraftShift() : mainShift, ShiftDTO.class);
            todoService.createOrUpdateTodo(mainShift, TodoType.APPROVAL_REQUIRED);
            payOutService.updatePayOut(staffAdditionalInfoDTO, mainShift, activityWrapperMap);
            shiftDTO.setId(mainShift.getId());
            shiftValidatorService.validateShiftViolatedRules(mainShift, (boolean) shiftOverlapInfo[0], shiftWithViolatedInfoDTO, PhaseDefaultName.DRAFT.equals(phase.getPhaseEnum()) ? ShiftActionType.SAVE_AS_DRAFT : null);
            shiftDTO = wtaRuleTemplateCalculationService.updateRestingTimeInShifts(Arrays.asList(shiftDTO)).get(0);
        }
        shiftWithViolatedInfoDTO.setShifts(newArrayList(shiftDTO));
        return shiftWithViolatedInfoDTO;
    }

    public Shift saveShiftWithActivity(Map<BigInteger, ActivityWrapper> activityWrapperMap, Shift shift, StaffAdditionalInfoDTO staffAdditionalInfoDTO, boolean updateShift, Long functionId, Phase phase, ShiftActionType shiftAction, Shift oldShift) {
        PlanningPeriod planningPeriod = planningPeriodMongoRepository.findOne(shift.getPlanningPeriodId());
        timeBankService.updateScheduledAndDurationHours(activityWrapperMap, shift, staffAdditionalInfoDTO);
        if(!ShiftActionType.SAVE_AS_DRAFT.equals(shiftAction)) {
            shiftStatusService.updateStatusOfShiftIfPhaseValid(planningPeriod, phase, shift, activityWrapperMap, staffAdditionalInfoDTO);
        }
        //As discuss with Arvind Presence and Absence type of activity cann't be perform in a Shift
        ShiftType shiftType = updateShiftType(activityWrapperMap, shift);
        shift.setShiftType(shiftType);
        shift.setPlanningPeriodPublished(planningPeriod.getPublishEmploymentIds().contains(staffAdditionalInfoDTO.getEmployment().getEmploymentType().getId()));
        shiftFunctionService.updateAppliedFunctionDetail(activityWrapperMap, shift, functionId);
        if (!shift.isSickShift() && updateShift && isNotNull(shiftAction) && newHashSet(PhaseDefaultName.CONSTRUCTION, PhaseDefaultName.DRAFT, PhaseDefaultName.TENTATIVE).contains(phase.getPhaseEnum())) {
            shift = updateShiftAfterPublish(shift, shiftAction);
        }
        if(isNull(shift.getDraftShift())) {
            if (!shift.isSickShift() && isValidForDraftShiftFunctionality(staffAdditionalInfoDTO, updateShift, phase, shiftAction, planningPeriod)) {
                Shift draftShift = ObjectMapperUtils.copyPropertiesByMapper(shift, Shift.class);
                ShiftType draftShiftType = updateShiftType(activityWrapperMap, draftShift);
                draftShift.setShiftType(draftShiftType);
                draftShift.setDraft(true);
                shift.setDraftShift(draftShift.getDraftShift());
                shift.setDraft(true);
            }
        }
        updateTimeTypeDetails(activityWrapperMap,shift);
        shift.setOldShiftTimeSlot(isNotNull(oldShift) ? staffAdditionalInfoDTO.getTimeSlotByShiftStartTime(oldShift.getActivities().get(0).getStartDate()) : null);
        shift.setStaffUserId(staffAdditionalInfoDTO.getStaffUserId());
        shift.setId(isNull(shift.getId()) ? shiftMongoRepository.nextSequence(Shift.class.getSimpleName()) : shift.getId());
        todoService.updateStatusOfShiftActivityIfApprovalRequired(activityWrapperMap, shift, updateShift,shiftAction,phase,planningPeriod,staffAdditionalInfoDTO);
        payOutService.updatePayOut(staffAdditionalInfoDTO, shift, activityWrapperMap);
        timeBankService.updateTimeBank(staffAdditionalInfoDTO, shift, false);
        shiftMongoRepository.save(shift);
        staffingLevelAvailableCountService.updateStaffingLevelAvailableCount(shift,oldShift,staffAdditionalInfoDTO);
        staffActivityDetailsService.updateStaffActivityDetails(shift.getStaffId(), shift.getActivities().stream().map(ShiftActivity::getActivityId).collect(Collectors.toList()), (isNull(shift.getId()) || isNull(oldShift) || isCollectionEmpty(oldShift.getActivities())) ? null : oldShift.getActivities().stream().map(ShiftActivity::getActivityId).collect(Collectors.toList()));
        shiftStateService.createShiftStateByPhase(Arrays.asList(shift), phase);
        return shift;
    }

    private void updateTimeTypeDetails(Map<BigInteger, ActivityWrapper> activityWrapperMap, Shift shift) {
        for (ShiftActivity shiftActivity : shift.getActivities()) {
            ActivityWrapper activityWrapper = activityWrapperMap.get(shiftActivity.getActivityId());
            shiftActivity.setTimeTypeId(activityWrapper.getTimeTypeInfo().getId());
            shiftActivity.setSecondLevelTimeType(activityWrapper.getTimeTypeInfo().getSecondLevelType());
            shiftActivity.setTimeType(activityWrapper.getTimeTypeInfo().getTimeTypes().toValue());
            shiftActivity.setMethodForCalculatingTime(activityWrapper.getActivity().getActivityTimeCalculationSettings().getMethodForCalculatingTime());
            shiftActivity.getChildActivities().forEach(shiftActivity1 -> {
                ActivityWrapper wrapper = activityWrapperMap.get(shiftActivity1.getActivityId());
                shiftActivity1.setTimeTypeId(wrapper.getTimeTypeInfo().getId());
                shiftActivity.setSecondLevelTimeType(activityWrapper.getTimeTypeInfo().getSecondLevelType());
                shiftActivity.setTimeType(activityWrapper.getTimeTypeInfo().getTimeTypes().toValue());
                shiftActivity.setMethodForCalculatingTime(activityWrapper.getActivity().getActivityTimeCalculationSettings().getMethodForCalculatingTime());
            });
        }
    }

    private boolean isValidForDraftShiftFunctionality(StaffAdditionalInfoDTO staffAdditionalInfoDTO, boolean updateShift, Phase phase, ShiftActionType shiftAction, PlanningPeriod planningPeriod) {
        boolean isValidActionType = (PhaseDefaultName.CONSTRUCTION.equals(phase.getPhaseEnum()) || !updateShift) && ShiftActionType.SAVE_AS_DRAFT.equals(shiftAction);
        boolean isValidPhase = (PhaseDefaultName.CONSTRUCTION.equals(phase.getPhaseEnum()) || (planningPeriod.getPublishEmploymentIds().contains(staffAdditionalInfoDTO.getEmployment().getEmploymentType().getId())
                && newHashSet(PhaseDefaultName.DRAFT, PhaseDefaultName.TENTATIVE).contains(phase.getPhaseEnum())));
        return isValidActionType && isValidPhase;
    }

    public ShiftType updateShiftType(Map<BigInteger, ActivityWrapper> activityWrapperMap, Shift shift) {
        ShiftType shiftType = null;
        if(isSickShift(shift,activityWrapperMap)){
            return SICK;
        }
        ACTIVITIES_LOOP: for (ShiftActivity shiftActivity : shift.getActivities()) {
            Activity activity = activityWrapperMap.get(shiftActivity.getActivityId()).getActivity();
            TimeTypeEnum timeTypeEnum = activity.getActivityBalanceSettings().getTimeType();
            switch (timeTypeEnum){
                case PLANNED_SICK_ON_FREE_DAYS :
                    shiftType = ShiftType.SICK;
                    break;
                case ABSENCE :
                    shiftType = activity.getActivityRulesSettings().isSicknessSettingValid() ? ShiftType.SICK : ShiftType.ABSENCE;
                    break;
                case PRESENCE :
                    shiftType = ShiftType.PRESENCE;
                    break ACTIVITIES_LOOP;
                default:
                    shiftType = ShiftType.NON_WORKING;
            }
        }
        return shiftType;
    }

    private boolean isSickShift(Shift shift, Map<BigInteger, ActivityWrapper> activityWrapperMap){
        for(ShiftActivity shiftActivity:shift.getActivities()){
            Activity activity = activityWrapperMap.get(shiftActivity.getActivityId()).getActivity();
            if(activity.getActivityRulesSettings().isSicknessSettingValid()){
                return true;
            }
        }
        return false;
    }

    public void saveShiftWithActivity(Map<Date, Phase> phaseListByDate, List<Shift> shifts, StaffAdditionalInfoDTO staffAdditionalInfoDTO) {
        Map<BigInteger, ActivityWrapper> activityWrapperMap = activityService.getActivityWrapperMap(shifts, null);

        for (Shift shift : shifts) {
            Map<BigInteger,StaffingLevelActivityWithDuration> staffingLevelActivityWithDurationMap = new HashMap<>();
            List<ShiftActivity>[] shiftActivities = shift.getShiftActivitiesForValidatingStaffingLevel(null);

            for (ShiftActivity shiftActivity : shiftActivities[1]) {
                shiftValidatorService.validateStaffingLevel(phaseListByDate.get(shift.getStartDate()), shift, activityWrapperMap, true, shiftActivity, null, staffingLevelActivityWithDurationMap,false);
            }
            int scheduledMinutes = 0;
            int durationMinutes = 0;
            for (ShiftActivity shiftActivity : shift.getActivities()) {
                int[] scheduledAndDurationMinutes = timeBankService.updateScheduledHoursAndActivityDetailsInShiftActivity(shiftActivity, activityWrapperMap, staffAdditionalInfoDTO);
                scheduledMinutes += scheduledAndDurationMinutes[0];
                durationMinutes += scheduledAndDurationMinutes[1];
                for (ShiftActivity childActivity : shiftActivity.getChildActivities()) {
                    timeBankService.updateScheduledHoursAndActivityDetailsInShiftActivity(childActivity, activityWrapperMap, staffAdditionalInfoDTO);
                }
            }
            shift.setPhaseId(phaseListByDate.get(shift.getActivities().get(0).getStartDate()).getId());
            shift.setScheduledMinutes(scheduledMinutes);
            shift.setDurationMinutes(durationMinutes);
            shift.setStartDate(shift.getActivities().get(0).getStartDate());
            shift.setEndDate(shift.getActivities().get(shift.getActivities().size() - 1).getEndDate());
            activityConfigurationService.addPlannedTimeInShift(shift, activityWrapperMap, staffAdditionalInfoDTO, false);
            updateTimeTypeDetails(activityWrapperMap,shift);
        }
        shifts.forEach(shift -> {
            timeBankService.updateTimeBank(staffAdditionalInfoDTO, shift, false);
            payOutService.updatePayOut(staffAdditionalInfoDTO, shift, activityWrapperMap);
            staffingLevelAvailableCountService.updateStaffingLevelAvailableCount(shift,null, staffAdditionalInfoDTO);
        });
        shiftMongoRepository.saveEntities(shifts);
        todoService.createOrUpdateTodo(shifts.get(0), TodoType.APPROVAL_REQUIRED);
    }

    public List<ShiftWithViolatedInfoDTO> deleteShiftsAfterValidation(List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfoDTOS) {
        List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfoDTOS1 = new ArrayList<>();
        for (ShiftWithViolatedInfoDTO shiftWithViolatedInfoDTO : shiftWithViolatedInfoDTOS) {
            shiftWithViolatedInfoDTOS1.add(deleteShiftAfterValidation(shiftWithViolatedInfoDTO));
        }
        return shiftWithViolatedInfoDTOS1;
    }

    public ShiftWithViolatedInfoDTO deleteShiftAfterValidation(ShiftWithViolatedInfoDTO shiftWithViolatedInfo) {
        List<ShiftDTO> responseShiftDTOS = new ArrayList<>();
        List<Shift> shifts = shiftMongoRepository.findAllByIdInAndDeletedFalseOrderByStartDateAsc(shiftWithViolatedInfo.getShifts().stream().map(s -> s.getId()).collect(Collectors.toList()));
        if (isCollectionEmpty(shifts)){
            exceptionService.dataNotFoundException(SHIFT_NOT_EXISTS);
        }
        StaffAdditionalInfoDTO staffAdditionalInfoDTO = userIntegrationService.verifyUnitEmploymentOfStaff(DateUtils.asLocalDate(shifts.get(0).getActivities().get(0).getStartDate()), shifts.get(0).getStaffId(), shifts.get(0).getEmploymentId());
        boolean updateWTACounterFlag = true;
        for (Shift shift : shifts) {
            if (updateWTACounterFlag) {
                shiftValidatorService.updateWTACounter(staffAdditionalInfoDTO, shiftWithViolatedInfo, shift);
                updateWTACounterFlag = false;
            }
            shift.setDeleted(true);
            responseShiftDTOS.add(deleteShift(shift, staffAdditionalInfoDTO));
        }
        shiftWithViolatedInfo.setShifts(responseShiftDTOS);
        return shiftWithViolatedInfo;
    }

    public List<ShiftWithViolatedInfoDTO> saveShiftsAfterValidation(List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfos, Boolean validatedByStaff, boolean updateShiftState, Long unitId, ShiftActionType shiftActionType, TodoType todoType) {
        List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfoDTOS = new ArrayList<>();
        for (ShiftWithViolatedInfoDTO shiftWithViolatedInfo : shiftWithViolatedInfos) {
            shiftWithViolatedInfoDTOS.add(saveShiftAfterValidation(shiftWithViolatedInfo, validatedByStaff, updateShiftState, unitId, shiftActionType, todoType));
        }
        return shiftWithViolatedInfoDTOS;
    }

    public ShiftWithViolatedInfoDTO saveShiftAfterValidation(ShiftWithViolatedInfoDTO shiftWithViolatedInfo, Boolean validatedByStaff, boolean updateShiftState, Long unitId, ShiftActionType shiftActionType, TodoType todoType) {
        boolean updateWTACounterFlag = true;
        List<ShiftDTO> responseShiftDTOS = new ArrayList<>();
        for (ShiftDTO shiftDTO : shiftWithViolatedInfo.getShifts()) {
            Long functionId = shiftDTO.getFunctionId();
            Shift shift = ObjectMapperUtils.copyPropertiesByMapper(shiftDTO, Shift.class);
            Date shiftStartDate = DateUtils.onlyDate(shiftDTO.getActivities().get(0).getStartDate());
            //reason code will be sanem for all shifts
            StaffAdditionalInfoDTO staffAdditionalInfoDTO = getStaffAdditionalInfoDTO(unitId, DateUtils.asLocalDate(shiftStartDate), shiftDTO.getStaffId(), shift.getEmploymentId());
            Shift oldShift = getOldShift(shift.getId());
            Map<BigInteger, ActivityWrapper> activityWrapperMap = activityService.getActivityWrapperMap(isNotNull(oldShift) ? newArrayList(oldShift) : newArrayList(), shiftDTO);
            updateCTADetailsOfEmployement(shiftDTO.getShiftDate(), staffAdditionalInfoDTO);
            Object[] shiftOverlapInfo = shiftValidatorService.validateStaffDetailsAndShiftOverlapping(staffAdditionalInfoDTO, shiftDTO, activityWrapperMap.get(shift.getActivities().get(0).getActivityId()), false);
            if(isNotNull(shiftOverlapInfo[1])){
                exceptionService.actionNotPermittedException(MESSAGE_SHIFT_DATE_STARTANDEND, shiftDTO.getStartDate(),shiftDTO.getEndDate());
            }
            boolean shiftOverLappedWithNonWorkingTime = (boolean) shiftOverlapInfo[0];
            Phase phase = phaseService.getCurrentPhaseByUnitIdAndDate(shift.getUnitId(), shift.getActivities().get(0).getStartDate(), shift.getActivities().get(shift.getActivities().size() - 1).getEndDate());
            shift.setPhaseId(phase.getId());
            WTAQueryResultDTO wtaQueryResultDTO = getWtaQueryResultByEmploymentIdAndDate(staffAdditionalInfoDTO, shiftWithViolatedInfo.getShifts().get(0));
            // replace id from shift id if request come form detailed view and compact view
            if (isNotNull(shiftDTO.getShiftId())) {
                shift.setId(shiftDTO.getShiftId());
            }
            ShiftWithActivityDTO shiftWithActivityDTO = getShiftWithActivityDTO(shiftDTO, activityWrapperMap, null);
            PlanningPeriod planningPeriod = planningPeriodMongoRepository.getPlanningPeriodContainsDate(shiftDTO.getUnitId(), shiftDTO.getShiftDate());
            List<ShiftActivity> breakActivities = shiftBreakService.updateBreakInShift(shift.isShiftUpdated(isNotNull(oldShift) ? oldShift : shift), shift, activityWrapperMap, staffAdditionalInfoDTO, wtaQueryResultDTO.getBreakRule(), staffAdditionalInfoDTO.getTimeSlotSets(), isNotNull(oldShift) ? oldShift : shift,null);
            shift.setBreakActivities(breakActivities);
            ShiftWithViolatedInfoDTO updatedShiftWithViolatedInfo = shiftValidatorService.validateShiftWithActivity(phase, wtaQueryResultDTO, shiftWithActivityDTO, staffAdditionalInfoDTO, oldShift, activityWrapperMap, isNotNull(shiftWithActivityDTO.getId()), isNull(shiftDTO.getShiftId()), true);
            List<ShiftDTO> shiftDTOS = newArrayList(shiftDTO);
            if (isIgnoredAllRuletemplate(shiftWithViolatedInfo, updatedShiftWithViolatedInfo)) {
                if (updateWTACounterFlag && !ShiftActionType.SAVE_AS_DRAFT.equals(shiftActionType)) {
                    shiftValidatorService.updateWTACounter(staffAdditionalInfoDTO, updatedShiftWithViolatedInfo, shift);
                    updateWTACounterFlag = false;
                }
                shift.setPlanningPeriodId(planningPeriod.getId());
                updateShiftViolatedOnIgnoreCounter(shift, shiftOverLappedWithNonWorkingTime, updatedShiftWithViolatedInfo);
                shift = saveShiftWithActivity(activityWrapperMap, shift, staffAdditionalInfoDTO, isNotNull(shift.getId()), functionId, phase, shiftActionType, oldShift);
                shiftDTO = ObjectMapperUtils.copyPropertiesByMapper(shift, ShiftDTO.class);
                updateTodoStatus(todoType, shift);
                activitySchedulerJobService.updateJobForShiftReminder(activityWrapperMap, shift);
                if (updateShiftState && shiftDTO.getId()!=null) {
                    shiftDTO = shiftStateService.updateShiftStateAfterValidatingWtaRule(shiftDTO, shiftDTO.getId(), shiftDTO.getShiftStatePhaseId());
                } else if (isNotNull(validatedByStaff)) {
                    Phase actualPhases = phaseMongoRepository.findByUnitIdAndPhaseEnum(unitId, PhaseDefaultName.TIME_ATTENDANCE.toString());
                    shiftDTO = shiftValidatorService.validateShiftStateAfterValidatingWtaRule(shiftDTO, validatedByStaff, actualPhases);
                }
                shiftDTOS = wtaRuleTemplateCalculationService.updateRestingTimeInShifts(newArrayList(shiftDTO));
            } else {
                shiftWithViolatedInfo = updatedShiftWithViolatedInfo;
            }
            shiftDTO = shiftDTOS.get(0);
            if (isNotNull(shiftDTO.getDraftShift())) {
                shiftDTO.getDraftShift().setId(shift.getId());
            }
            responseShiftDTOS.addAll(Arrays.asList(isNotNull(shiftDTO.getDraftShift()) ? shiftDTO.getDraftShift() : shiftDTO));
        }
        shiftWithViolatedInfo.setShifts(responseShiftDTOS);
        return shiftWithViolatedInfo;
    }

    private WTAQueryResultDTO getWtaQueryResultByEmploymentIdAndDate(StaffAdditionalInfoDTO staffAdditionalInfoDTO, ShiftDTO shiftDTO2) {
        WTAQueryResultDTO wtaQueryResultDTO = workingTimeAgreementMongoRepository.getWTAByEmploymentIdAndDate(staffAdditionalInfoDTO.getEmployment().getId(), DateUtils.onlyDate(shiftDTO2.getActivities().get(0).getStartDate()));
        if (!Optional.ofNullable(wtaQueryResultDTO).isPresent()) {
            exceptionService.actionNotPermittedException(MESSAGE_WTA_NOTFOUND);
        }
        return wtaQueryResultDTO;
    }

    private Shift getOldShift(BigInteger shiftId) {
        Shift oldShift = null;
        if (isNotNull(shiftId)) {
            oldShift = shiftMongoRepository.findOne(shiftId);
        }
        return oldShift;
    }

    private void updateTodoStatus(TodoType todoType, Shift shift) {
        if (isNotNull(todoType)) {
            Todo todo = todoRepository.findByEntityIdAndType(shift.getId(), TodoType.REQUEST_ABSENCE);
            todo.setStatus(TodoStatus.APPROVE);
            todo.setApprovedOn(getDate());
            todoRepository.save(todo);
        }
    }

    private StaffAdditionalInfoDTO getStaffAdditionalInfoDTO(Long unitId, LocalDate localDate, Long staffId, Long employmentId) {
        StaffAdditionalInfoDTO staffAdditionalInfoDTO = userIntegrationService.verifyUnitEmploymentOfStaff(localDate, staffId, employmentId);
        TimeSlotSetDTO timeSlotSetDTO = timeSlotRepository.findByUnitIdAndTimeSlotTypeOrderByStartDate(unitId, TimeSlotType.SHIFT_PLANNING);
        if(isNull(timeSlotSetDTO)){
            exceptionService.dataNotFoundException(TIMESLOT_NOT_FOUND_FOR_UNIT);
        }
        staffAdditionalInfoDTO.setTimeSlotSets(timeSlotSetDTO.getTimeSlots());
        staffAdditionalInfoDTO.setDayTypes(dayTypeService.getDayTypeWithCountryHolidayCalender(UserContext.getUserDetails().getCountryId()));
        return staffAdditionalInfoDTO;
    }

    private void updateShiftViolatedOnIgnoreCounter(Shift shift, boolean shiftOverLappedWithNonWorkingTime, ShiftWithViolatedInfoDTO updatedShiftWithViolatedInfo) {
        ShiftViolatedRules shiftViolatedRules = shift.getShiftViolatedRules();
        if (isNull(shiftViolatedRules)) {
            shiftViolatedRules = new ShiftViolatedRules(shift.getId());
            shiftViolatedRules.setDraft(isNotNull(shift.getDraftShift()));
        }
        shiftViolatedRules.setEscalationReasons(shiftOverLappedWithNonWorkingTime ? newHashSet(ShiftEscalationReason.SHIFT_OVERLAPPING, ShiftEscalationReason.WORK_TIME_AGREEMENT) : newHashSet(ShiftEscalationReason.WORK_TIME_AGREEMENT));
        shiftViolatedRules.setEscalationResolved(false);
        shiftViolatedRules.setActivities(updatedShiftWithViolatedInfo.getViolatedRules().getActivities());
        shiftViolatedRules.setWorkTimeAgreements(updatedShiftWithViolatedInfo.getViolatedRules().getWorkTimeAgreements());
        shiftViolatedRules.setEscalationCausedBy(UserContext.getUserDetails().isManagement() ? MANAGEMENT : AccessGroupRole.STAFF);
        shift.setShiftViolatedRules(shiftViolatedRules);
    }

    public Map<String, Object> saveAndCancelDraftShift(Long unitId, Long staffId, LocalDate startDate, LocalDate endDate, Long employmentId, ViewType viewType, ShiftFilterParam shiftFilterParam, ShiftActionType shiftActionType, StaffFilterDTO staffFilterDTO) {
        List<PlanningPeriod> planningPeriods = new ArrayList<>();
        if (isNotNull(staffFilterDTO.getPlanningPeriodIds())) {
            planningPeriods = planningPeriodMongoRepository.findAllByUnitIdAndIds(unitId, staffFilterDTO.getPlanningPeriodIds());
            planningPeriods.sort(Comparator.comparing(PlanningPeriod::getStartDate));
        }
        List<Shift> draftShifts = getDraftShifts(unitId, staffId, staffFilterDTO.getPlanningPeriodIds(), startDate, endDate, employmentId);
        List<Shift> saveShifts;
        List<BigInteger> deletedShiftIds = new ArrayList<>();
        if (ShiftActionType.SAVE.equals(shiftActionType)) {
            saveShifts = updateShiftAndShiftViolatedRules(draftShifts);
            shiftStateService.createShiftState(saveShifts, true, unitId);
        } else {
            List[] saveShiftsDeletedShiftIds = deleteDraftShiftAndViolatedRules(draftShifts);
            deletedShiftIds = saveShiftsDeletedShiftIds[1];
        }
        Map<String, Object> response = new HashMap<>();
        response.put("shiftDetails", fetchShiftService.getAllShiftAndStates(unitId, staffId, isNull(startDate) ? planningPeriods.get(0).getStartDate() : startDate, isNull(endDate) ? planningPeriods.get(planningPeriods.size() - 1).getEndDate() : endDate, employmentId, viewType, shiftFilterParam, null, staffFilterDTO));
        response.put("deletedShiftIds", deletedShiftIds);
        return response;
    }

    private List<Shift> getDraftShifts(Long unitId, Long staffId, List<BigInteger> planningPeriodIds, LocalDate startDate, LocalDate endDate, Long employmentId) {
        List<Shift> draftShifts;
        if (isNotNull(staffId) && isNotNull(employmentId) && isCollectionNotEmpty(planningPeriodIds)) {
            draftShifts = shiftMongoRepository.getAllDraftShiftBetweenDuration(employmentId, staffId, planningPeriodIds, unitId);
        } else {
            draftShifts = shiftMongoRepository.findDraftShiftBetweenDurationAndUnitIdAndDeletedFalse(asDate(startDate), getEndOfDay(asDate(endDate)), unitId);
        }
        if (isCollectionEmpty(draftShifts)) {
            exceptionService.actionNotPermittedException(MESSAGE_SHIFT_DRAFT_NOTFOUND);
        }
        return draftShifts;
    }

    public List[] deleteDraftShiftAndViolatedRules(List<Shift> draftShifts) {
        List<Shift> saveShifts = new ArrayList<>();
        List<Shift> deleteShift = new ArrayList<>();
        List<BigInteger> deletedShiftIds = new ArrayList<>();
        for (Shift draftShift : draftShifts) {
            if (draftShift.isDraft()) {
                deleteShift.add(draftShift);
                deletedShiftIds.add(draftShift.getId());
            } else {
                draftShift.setDraftShift(null);
                saveShifts.add(draftShift);
            }
        }
        if (isCollectionNotEmpty(saveShifts)) {
            shiftMongoRepository.saveEntities(saveShifts);
        }
        if (isCollectionNotEmpty(deleteShift)) {
            shiftMongoRepository.deleteAll(deleteShift);
        }
        return new List[]{saveShifts, deletedShiftIds};
    }

    private List<Shift> updateShiftAndShiftViolatedRules(List<Shift> draftShifts) {
        List<Shift> saveShifts = new ArrayList<>();
        for (Shift draftShift : draftShifts) {
            if(isNotNull(draftShift.getDraftShift())||draftShift.isDraft()){
                Shift shift = isNotNull(draftShift.getDraftShift())?draftShift.getDraftShift():draftShift;
                shift.setDraftShift(null);
                shift.setId(draftShift.getId());
                shift.setDraft(false);
                for (ShiftActivity shiftActivity : shift.getActivities()) {
                    shiftActivity.getStatus().add(ShiftStatus.PUBLISH);
                }
                if(isNotNull(shift.getShiftViolatedRules())){
                    ShiftViolatedRules violatedRules = shift.getShiftViolatedRules();
                    violatedRules.setEscalationCausedBy(UserContext.getUserDetails().isManagement() ? MANAGEMENT : AccessGroupRole.STAFF);
                }
                saveShifts.add(shift);
            }
        }
        if (isCollectionNotEmpty(saveShifts)) {
            shiftMongoRepository.saveEntities(saveShifts);
        }
        return saveShifts;
    }

    public List<ShiftWithViolatedInfoDTO> updateShifts(List<ShiftDTO> shiftDTOS, boolean byTAndAView, boolean validatedByPlanner, ShiftActionType shiftAction) {
        List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfoDTOS = new ArrayList<>(shiftDTOS.size());
        for (ShiftDTO shiftDTO : shiftDTOS) {
            shiftWithViolatedInfoDTOS.addAll(updateShift(shiftDTO, byTAndAView, validatedByPlanner, shiftAction));
        }
        return shiftWithViolatedInfoDTOS;
    }

    public List<ShiftWithViolatedInfoDTO> updateShift(ShiftDTO shiftDTO, boolean byTAndAView, boolean validatedByPlanner, ShiftActionType shiftAction) {
        Long functionId = shiftDTO.getFunctionId();
        Shift shift = findOneByShiftId(byTAndAView ? shiftDTO.getShiftId() : shiftDTO.getId());
        if (shift.isDraft() && ShiftActionType.CANCEL.equals(shiftAction)) {
            shiftMongoRepository.delete(shift);
            return new ArrayList<>();
        }
        StaffAdditionalInfoDTO staffAdditionalInfoDTO = userIntegrationService.verifyUnitEmploymentOfStaff(shiftDTO.getShiftDate(), shiftDTO.getStaffId(), shiftDTO.getEmploymentId());
        Phase phase = phaseService.getCurrentPhaseByUnitIdAndDate(shift.getUnitId(), shiftDTO.getStartDate(), shiftDTO.getEndDate());
        gapSettingsService.adjustGapByActivity(shiftDTO,shift,phase,staffAdditionalInfoDTO);
        Shift oldShift = ObjectMapperUtils.copyPropertiesByMapper(shift, Shift.class);
        boolean ruleCheckRequired = shift.isShiftUpdated(ObjectMapperUtils.copyPropertiesByMapper(shiftDTO, Shift.class));
        Date currentShiftStartDate = shift.getStartDate();
        Date currentShiftEndDate = shift.getEndDate();

        staffAdditionalInfoDTO.setTimeSlotSets(timeSlotRepository.findByUnitIdAndTimeSlotTypeOrderByStartDate(shiftDTO.getUnitId(), TimeSlotType.SHIFT_PLANNING).getTimeSlots());
        staffAdditionalInfoDTO.setDayTypes(dayTypeService.getDayTypeWithCountryHolidayCalender(UserContext.getUserDetails().getCountryId()));
        if (UserContext.getUserDetails().isManagement() && isNotNull(shift.getDraftShift()) && !byTAndAView) {
            shift = shift.getDraftShift();
        }
        Map<BigInteger, ActivityWrapper> activityWrapperMap = activityService.getActivityWrapperMap(newArrayList(shift), shiftDTO);
        Object[] shiftOverlapInfo = shiftValidatorService.validateStaffDetailsAndShiftOverlapping(staffAdditionalInfoDTO, shiftDTO, activityWrapperMap.get(shiftDTO.getActivities().get(0).getActivityId()), byTAndAView);
        updateCTADetailsOfEmployement(shiftDTO.getShiftDate(), staffAdditionalInfoDTO);
        List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfoDTOS = new ArrayList<>();
        ActivityWrapper absenceActivityWrapper = getAbsenceTypeOfActivityIfPresent(shiftDTO.getActivities(), activityWrapperMap);
        boolean updatingSameActivity = true;
        if (isNotNull(absenceActivityWrapper)) {
            updatingSameActivity = shift.getActivities().stream().filter(shiftActivity -> shiftActivity.getActivityId().equals(absenceActivityWrapper.getActivity().getId())).findAny().isPresent();
        }
        if (!updatingSameActivity) {
            shiftWithViolatedInfoDTOS = absenceShiftService.createAbsenceTypeShift(absenceActivityWrapper, shiftDTO, staffAdditionalInfoDTO, shiftOverlapInfo, shiftAction);
        }else {
            if (isNull(staffAdditionalInfoDTO.getUnitId())) {
                exceptionService.dataNotFoundByIdException(MESSAGE_STAFF_UNIT, shiftDTO.getStaffId(), shiftDTO.getUnitId());
            }
            WTAQueryResultDTO wtaQueryResultDTO = workingTimeAgreementMongoRepository.getWTAByEmploymentIdAndDate(staffAdditionalInfoDTO.getEmployment().getId(), shiftDTO.getActivities().get(0).getStartDate());
            if (!Optional.ofNullable(wtaQueryResultDTO).isPresent()) {
                exceptionService.actionNotPermittedException(MESSAGE_WTA_NOTFOUND);
            }
            //copy old state of activity object
            Shift oldStateOfShift = ObjectMapperUtils.copyPropertiesByMapper(shift, Shift.class);
            if (!byTAndAView) {
                shiftValidatorService.updateStatusOfShiftActvity(oldStateOfShift, shiftDTO,activityWrapperMap,phase);

            }
            shiftDTO.setUnitId(staffAdditionalInfoDTO.getUnitId());
            shift = ObjectMapperUtils.copyPropertiesByMapper(shiftDTO, Shift.class);
            shift.setTimeBankCTADistributions(oldShift.getTimeBankCTADistributions());
            shift.setPayoutPerShiftCTADistributions(oldShift.getPayoutPerShiftCTADistributions());
            ShiftType shiftType =updateShiftType(activityWrapperMap, shift);
            shift.setShiftType(shiftType);
            shift.setPhaseId(phase.getId());
            if (byTAndAView) {
                shift.setId(shiftDTO.getShiftId());
            }
            shift.setDraftShift(oldStateOfShift.getDraftShift());
            shift.setPlanningPeriodId(oldStateOfShift.getPlanningPeriodId());
            List<ShiftActivity> breakActivities = new ArrayList<>();
            List<Shift> shiftList = getShiftsOnTheBasisOfGapActivity(shift, activityWrapperMap);
            for (Shift currentShift : shiftList) {
                List<ShiftActivity> breakActivityList = shiftBreakService.updateBreakInShift(shift.isShiftUpdated(oldStateOfShift), currentShift, activityWrapperMap, staffAdditionalInfoDTO, wtaQueryResultDTO.getBreakRule(), staffAdditionalInfoDTO.getTimeSlotSets(), oldStateOfShift,phase);
                breakActivities.addAll(breakActivityList);
            }
            shift.setBreakActivities(breakActivities);

            activityConfigurationService.addPlannedTimeInShift(shift, activityWrapperMap, staffAdditionalInfoDTO, !oldStateOfShift.getShiftType().equals(shift.getShiftType()));
            ShiftWithActivityDTO shiftWithActivityDTO = getShiftWithActivityDTO(null, activityWrapperMap, shift);
            boolean valid = isNotNull(shiftAction) && !shiftAction.equals(ShiftActionType.CANCEL) && shift.getActivities().stream().anyMatch(activity -> !activity.getStatus().contains(ShiftStatus.PUBLISH)) && UserContext.getUserDetails().isManagement();

            ShiftWithViolatedInfoDTO shiftWithViolatedInfoDTO = shiftValidatorService.validateRuleCheck(shift, ruleCheckRequired, staffAdditionalInfoDTO, activityWrapperMap, phase, valid, wtaQueryResultDTO, shiftWithActivityDTO, oldStateOfShift);
            shiftWithViolatedInfoDTO.getViolatedRules().setOverlapWithShiftId((BigInteger) shiftOverlapInfo[1]);
            List<ShiftDTO> shiftDTOS = newArrayList(shiftDTO);
            if (PhaseDefaultName.TIME_ATTENDANCE.equals(phase.getPhaseEnum()) || (isNull(shiftOverlapInfo[1]) && shiftWithViolatedInfoDTO.getViolatedRules().getActivities().isEmpty() && shiftWithViolatedInfoDTO.getViolatedRules().getWorkTimeAgreements().isEmpty())) {
                shift = saveShiftWithActivity(activityWrapperMap, shift, staffAdditionalInfoDTO, true, functionId, phase, shiftAction, oldShift);
                wtaRuleTemplateCalculationService.updateWTACounter(shift, staffAdditionalInfoDTO);
                shiftDTO = UserContext.getUserDetails().isManagement() ? ObjectMapperUtils.copyPropertiesByMapper(isNotNull(shift.getDraftShift()) ? shift.getDraftShift() : shift, ShiftDTO.class) : ObjectMapperUtils.copyPropertiesByMapper(shift, ShiftDTO.class);
                timeBankService.updateTimeBank(staffAdditionalInfoDTO, shift, validatedByPlanner);
                if (ShiftActionType.SAVE.equals(shiftAction)) {
                    shiftStateService.createShiftState(Arrays.asList(shift), true, shift.getUnitId());
                }
                // TODO VIPUL WE WILL UNCOMMENTS AFTER FIX mailing servive
                //shiftReminderService.updateReminderTrigger(activityWrapperMap,shift);
                if (ruleCheckRequired) {
                    activityConfigurationService.addPlannedTimeInShift(shift, activityWrapperMap, staffAdditionalInfoDTO, !oldStateOfShift.getShiftType().equals(shift.getShiftType()));
                    shiftValidatorService.validateShiftViolatedRules(shift, (boolean) shiftOverlapInfo[0], shiftWithViolatedInfoDTO, shiftAction);
                }
                shiftDTOS = wtaRuleTemplateCalculationService.updateRestingTimeInShifts(newArrayList(shiftDTO));
            }
            shiftWithViolatedInfoDTO.setShifts(shiftDTOS);
            shiftWithViolatedInfoDTOS.add(shiftWithViolatedInfoDTO);

        }
        addReasonCode(shiftWithViolatedInfoDTOS.stream().flatMap(shiftWithViolatedInfoDTO -> shiftWithViolatedInfoDTO.getShifts().stream()).collect(Collectors.toList()));
        if (!shiftDTO.isDraft()) {
            if (byTAndAView) {
                shiftDTO.setId(shiftDTO.getShiftId());
            }
            shiftValidatorService.escalationCorrectionInShift(shiftDTO, currentShiftStartDate, currentShiftEndDate, shift);
        }
        shiftDetailsService.updateTimingChanges(oldShift,shiftDTO,shiftWithViolatedInfoDTOS.get(0));
        return shiftWithViolatedInfoDTOS;
    }

    public void updateCTADetailsOfEmployement(LocalDate shiftDate, StaffAdditionalInfoDTO staffAdditionalInfoDTO) {
        CTAResponseDTO ctaResponseDTO = costTimeAgreementRepository.getCTAByEmploymentIdAndDate(staffAdditionalInfoDTO.getEmployment().getId(), asDate(shiftDate));
        if (!Optional.ofNullable(ctaResponseDTO).isPresent()) {
            exceptionService.dataNotFoundByIdException("error.cta.notFound", shiftDate);
        }
        staffAdditionalInfoDTO.getEmployment().setCtaRuleTemplates(ctaResponseDTO.getRuleTemplates());
        setDayTypeToCTARuleTemplate(staffAdditionalInfoDTO);
    }

    private Shift updateShiftAfterPublish(Shift shift, ShiftActionType shiftActionType) {
        Shift originalShift = shiftMongoRepository.findOne(shift.getId());
        boolean valid = UserContext.getUserDetails().isManagement();
        if (valid && ShiftActionType.SAVE_AS_DRAFT.equals(shiftActionType)) {
            Shift draftShift = ObjectMapperUtils.copyPropertiesByMapper(shift, Shift.class);
            draftShift.setPlannedMinutesOfTimebank(originalShift.getPlannedMinutesOfTimebank());
            draftShift.setTimeBankCtaBonusMinutes(originalShift.getTimeBankCtaBonusMinutes());
            draftShift.setScheduledMinutes(originalShift.getScheduledMinutes());
            originalShift.setDraftShift(draftShift);
            originalShift.getDraftShift().setDraft(true);
        } else if (valid && ShiftActionType.SAVE.equals(shiftActionType)) {
            originalShift = shift;
            originalShift.setDraft(false);
        } else {
            originalShift.setDraft(false);
            originalShift.setDraftShift(null);
        }
        return originalShift;
    }

    public List<ShiftWithViolatedInfoDTO> deleteAllShifts(List<BigInteger> shiftIds){
        List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfoDTOS = new ArrayList<>();
        shiftIds.forEach(shiftId->shiftWithViolatedInfoDTOS.add(deleteAllLinkedShifts(shiftId)));
        return shiftWithViolatedInfoDTOS;
    }

    public ShiftWithViolatedInfoDTO deleteAllLinkedShifts(BigInteger shiftId) {
        List<ShiftDTO> shiftDTOS = new ArrayList<>();
        Shift shift = shiftMongoRepository.findById(shiftId).orElseThrow(()->new DataNotFoundByIdException(convertMessage(MESSAGE_SHIFT_IDS)));
        Activity activity = activityRepository.findOne(shift.getActivities().get(0).getActivityId());
        StaffAdditionalInfoDTO staffAdditionalInfoDTO = getStaffAdditionalInfoDTO(shift.getUnitId(), DateUtils.asLocalDate(shift.getActivities().get(0).getStartDate()), shift.getStaffId(), shift.getEmploymentId());
        if (staffAdditionalInfoDTO.getUserAccessRoleDTO().isStaff() && !staffAdditionalInfoDTO.getUserAccessRoleDTO().getStaffId().equals(shift.getStaffId())) {
            exceptionService.actionNotPermittedException(MESSAGE_SHIFT_PERMISSION);
        }
        ViolatedRulesDTO violatedRulesDTO;
        if (CommonConstants.FULL_WEEK.equals(activity.getActivityTimeCalculationSettings().getMethodForCalculatingTime())) {
            violatedRulesDTO = deleteShifts(shiftDTOS, getFullWeekShiftsByDate(shift.getStartDate(), shift.getEmploymentId(), activity), staffAdditionalInfoDTO);
        } else {
            violatedRulesDTO = shiftValidatorService.validateRule(shift, staffAdditionalInfoDTO);
            if(isCollectionEmpty(violatedRulesDTO.getWorkTimeAgreements()) && isCollectionEmpty(violatedRulesDTO.getActivities())) {
                shift.setDeleted(true);
                shiftDTOS.add(deleteShift(shift, staffAdditionalInfoDTO));
            } else {
                ShiftDTO shiftDTO = ObjectMapperUtils.copyPropertiesByMapper(shift, ShiftDTO.class);
                shiftDTO.setShiftDate(asLocalDate(shift.getStartDate()));
                shiftDTOS.add(shiftDTO);
            }
        }
        return new ShiftWithViolatedInfoDTO(shiftDTOS, violatedRulesDTO);
    }

    public ViolatedRulesDTO deleteShifts(List<ShiftDTO> shiftDTOS, List<Shift> shifts, StaffAdditionalInfoDTO staffAdditionalInfoDTO) {
        ViolatedRulesDTO violatedRulesDTO = new ViolatedRulesDTO();
        for (Shift shift : shifts) {
            violatedRulesDTO = shiftValidatorService.validateRule(shift, staffAdditionalInfoDTO);
            if (isCollectionNotEmpty(violatedRulesDTO.getWorkTimeAgreements())) {
                break;
            }
            shift.setDeleted(true);
            staffingLevelAvailableCountService.updateStaffingLevelAvailableCount(null,shift, staffAdditionalInfoDTO);
            shiftMongoRepository.save(shift);
        }
        if (isCollectionNotEmpty(violatedRulesDTO.getWorkTimeAgreements())) {
            shifts.forEach(shift -> {
                shift.setDeleted(false);
                ShiftDTO shiftDTO = ObjectMapperUtils.copyPropertiesByMapper(shift, ShiftDTO.class);
                shiftDTO.setShiftDate(asLocalDate(shift.getStartDate()));
                shiftDTOS.add(shiftDTO);
            });
            shiftMongoRepository.saveAll(shifts);
        } else {
            shifts.forEach(shift -> shiftDTOS.add(deleteShift(shift, staffAdditionalInfoDTO)));
        }
        return violatedRulesDTO;
    }

    public List<Shift> getFullWeekShiftsByDate(Date shiftStartDate, Long employmentId, Activity activity) {
        ZonedDateTime startDate = asZonedDateTime(shiftStartDate).with(TemporalAdjusters.previousOrSame(activity.getActivityTimeCalculationSettings().getFullWeekStart())).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime endDate = startDate.plusDays(7);
        return shiftMongoRepository.findShiftBetweenDurationByEmploymentId(employmentId, asDate(startDate), asDate(endDate));
    }

    private ShiftDTO deleteShift(Shift shift, StaffAdditionalInfoDTO staffAdditionalInfoDTO) {
        ShiftDTO shiftDTO = new ShiftDTO();
        List<Shift> shifts = shiftMongoRepository.findShiftBetweenDurationByStaffId(shift.getStaffId(), DateUtils.getStartOfDay(shift.getStartDate()), DateUtils.getEndOfDay(shift.getEndDate()));
        if (shifts.size() == 1 && CollectionUtils.isNotEmpty(staffAdditionalInfoDTO.getEmployment().getAppliedFunctions())) {
            Long functionId = userIntegrationService.removeFunctionFromEmploymentByDate(shift.getUnitId(), shift.getEmploymentId(), shift.getStartDate());
            shiftDTO.setFunctionDeleted(true);
            shift.setFunctionId(functionId);
        }
        PlanningPeriod planningPeriod = planningPeriodMongoRepository.findOne(shift.getPlanningPeriodId());
        shift.setPlanningPeriodPublished(planningPeriod.getPublishEmploymentIds().contains(staffAdditionalInfoDTO.getEmployment().getEmploymentType().getId()));
        shiftMongoRepository.save(shift);
        staffingLevelAvailableCountService.updateStaffingLevelAvailableCount(null,shift, staffAdditionalInfoDTO);
        //TODO call this method only if violation in shift
        wtaRuleTemplateCalculationService.updateWTACounter(shift, staffAdditionalInfoDTO);
        shiftDTO.setId(shift.getId());
        shiftDTO.setStartDate(shift.getStartDate());
        shiftDTO.setEndDate(shift.getEndDate());
        shiftDTO.setUnitId(shift.getUnitId());
        shiftDTO.setDeleted(true);
        shiftDTO.setShiftDate(asLocalDate(shift.getStartDate()));
        shiftDTO.setActivities(ObjectMapperUtils.copyCollectionPropertiesByMapper(shift.getActivities(), ShiftActivityDTO.class));
        setDayTypeToCTARuleTemplate(staffAdditionalInfoDTO);
        todoService.deleteTodo(shift.getId(), null);
        timeBankService.updateTimeBank(staffAdditionalInfoDTO, shift, false);
        payOutService.deletePayOut(shift.getId());
        List<BigInteger> jobIds = shift.getActivities().stream().map(ShiftActivity::getId).collect(Collectors.toList());
        shiftReminderService.deleteReminderTrigger(jobIds, shift.getUnitId());
        staffActivityDetailsService.decreaseActivityCount(shift.getStaffId(), shift.getActivities().stream().map(ShiftActivity::getActivityId).collect(Collectors.toList()));
        return shiftDTO;
    }

    public Long countByActivityId(BigInteger activityId) {
        return shiftMongoRepository.countByActivityId(activityId);
    }

    public <T> ShiftWithActivityDTO getShiftWithActivityDTO(ShiftDTO shiftDTO, Map<BigInteger, T> activityWrapperMap, Shift shift) {
        ShiftWithActivityDTO shiftWithActivityDTO = ObjectMapperUtils.copyPropertiesByMapper(isNull(shiftDTO) ? shift : shiftDTO, ShiftWithActivityDTO.class);
        shiftWithActivityDTO.setOldShiftTimeSlot(isNotNull(shift) ? shift.getOldShiftTimeSlot() : null);
        updateActivityDetails(activityWrapperMap, shiftWithActivityDTO);
        if (isNotNull(shiftWithActivityDTO.getDraftShift())) {
            updateActivityDetails(activityWrapperMap, shiftWithActivityDTO.getDraftShift());
        }
        if (isNotNull(shiftDTO)) {
            shiftDTO.getActivities().forEach(shiftActivityDTO -> {
                        T activity = activityWrapperMap.get(shiftActivityDTO.getActivityId());
                        shiftActivityDTO.setActivityName(activity instanceof ActivityDTO ? ((ActivityDTO) activity).getName() : ((ActivityWrapper) activity).getActivity().getName());
                    }
            );
            shiftWithActivityDTO.setStartDate(shiftDTO.getActivities().get(0).getStartDate());
            shiftWithActivityDTO.setEndDate(shiftDTO.getActivities().get(shiftDTO.getActivities().size() - 1).getEndDate());
        }
        return shiftWithActivityDTO;
    }

    private <T> void updateActivityDetails(Map<BigInteger, T> activityWrapperMap, ShiftWithActivityDTO shiftWithActivityDTO) {
        shiftWithActivityDTO.getActivities().forEach(shiftActivityDTO -> {
            T activity = activityWrapperMap.get(shiftActivityDTO.getActivityId());
            ActivityDTO activityDTO = activity instanceof ActivityDTO ? (ActivityDTO) activity : ObjectMapperUtils.copyPropertiesByMapper(((ActivityWrapper) activity).getActivity(), ActivityDTO.class);
            shiftActivityDTO.setActivity(activityDTO);
            shiftActivityDTO.setTimeType(activity instanceof ActivityDTO ? ((ActivityDTO) activity).getTimeType().getTimeTypes() : ((ActivityWrapper)activity).getTimeType());
            for (ShiftActivityDTO childActivityDTO : shiftActivityDTO.getChildActivities()) {
                if(activityWrapperMap.containsKey(childActivityDTO.getActivityId())) {
                    activity = activityWrapperMap.get(childActivityDTO.getActivityId());
                    activityDTO = activity instanceof ActivityDTO ? (ActivityDTO) activity : ObjectMapperUtils.copyPropertiesByMapper(((ActivityWrapper) activity).getActivity(), ActivityDTO.class);
                    childActivityDTO.setActivity(activityDTO);
                    childActivityDTO.setTimeType(activity instanceof ActivityDTO ? ((ActivityDTO) activity).getTimeType().getTimeTypes() : ((ActivityWrapper)activity).getTimeType());

                }
            }
        });
    }

    public void deleteShiftsAfterEmploymentEndDate(Long employmentId, LocalDate employmentEndDate, StaffAdditionalInfoDTO staffAdditionalInfoDTO) {
        List<Shift> shiftList = shiftMongoRepository.findAllShiftsByEmploymentIdAfterDate(employmentId, asDate(employmentEndDate));
        timeBankService.updateDailyTimebankForShifts(null, employmentId, staffAdditionalInfoDTO, shiftList);
    }

    //TODO need to optimize this method
    public List<ShiftWithViolatedInfoDTO> updateShiftByTandA(Long unitId, ShiftDTO shiftDTO) {
        Phase phase = phaseMongoRepository.findByUnitIdAndPhaseEnum(unitId, PhaseDefaultName.REALTIME.toString());
        Map<String, Phase> phaseMap = new HashMap<String, Phase>() {{
            put(phase.getPhaseEnum().toString(), phase);
        }};
        if (shiftDTO.getShiftStatePhaseId().equals(phase.getId())) {
            shiftValidatorService.validateRealTimeShift(unitId, shiftDTO, phaseMap);
        }
        if (isNull(shiftDTO.getShiftId())) {
            shiftDTO.setShiftId(shiftDTO.getId());
        }
        BigInteger shiftStateId = shiftDTO.getId();
        BigInteger shiftStatePhaseId = shiftDTO.getShiftStatePhaseId();
        shiftDTO.getActivities().forEach(a -> a.setId(mongoSequenceRepository.nextSequence(ShiftActivity.class.getSimpleName())));
        List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfoDTOS = updateShift(shiftDTO, true, false, null);
        for (ShiftWithViolatedInfoDTO shiftWithViolatedInfoDTO : shiftWithViolatedInfoDTOS) {
            ShiftDTO updatedShiftDto = shiftStateService.updateShiftStateAfterValidatingWtaRule(shiftWithViolatedInfoDTO.getShifts().get(0), shiftStateId, shiftStatePhaseId);
            List<ShiftDTO> shiftDTOS = wtaRuleTemplateCalculationService.updateRestingTimeInShifts(newArrayList(updatedShiftDto));
            shiftWithViolatedInfoDTO.setShifts(shiftDTOS);
            shiftWithViolatedInfoDTO.getShifts().get(0).setEditable(true);
            shiftWithViolatedInfoDTO.getShifts().get(0).setDurationMinutes((int) shiftWithViolatedInfoDTO.getShifts().get(0).getInterval().getMinutes());
        }
        return shiftWithViolatedInfoDTOS;
    }

    private ActivityWrapper getAbsenceTypeOfActivityIfPresent(List<ShiftActivityDTO> shiftActivityDTOS, Map<BigInteger, ActivityWrapper> activityWrapperMap) {
        ActivityWrapper activityWrapper = null;
        for (ShiftActivityDTO shiftActivityDTO : shiftActivityDTOS) {
            if (CommonConstants.FULL_WEEK.equals(activityWrapperMap.get(shiftActivityDTO.getActivityId()).getActivity().getActivityTimeCalculationSettings().getMethodForCalculatingTime()) || CommonConstants.FULL_DAY_CALCULATION.equals(activityWrapperMap.get(shiftActivityDTO.getActivityId()).getActivity().getActivityTimeCalculationSettings().getMethodForCalculatingTime())) {
                activityWrapper = activityWrapperMap.get(shiftActivityDTO.getActivityId());
            }
        }
        return activityWrapper;
    }

    public boolean updatePlanningPeriodInShifts() {
        List<Shift> shifts = shiftMongoRepository.findAllAbsenceShifts(ShiftType.ABSENCE.toString());
        for (Shift shift : shifts) {
            PlanningPeriod planningPeriod = planningPeriodMongoRepository.findOneByUnitIdAndDate(shift.getUnitId(), getStartOfDay(shift.getStartDate()));
            shift.setPlanningPeriodId(isNotNull(planningPeriod) ? planningPeriod.getId() : null);
        }
        if (isCollectionNotEmpty(shifts)) {
            shiftMongoRepository.saveEntities(shifts);
        }
        return true;
    }

    public Long getPublishShiftCount(Long employmentId) {
        return shiftMongoRepository.getCountOfPublishShiftByEmploymentId(employmentId);
    }

    private List<Shift> getShiftsOnTheBasisOfGapActivity(Shift shift, Map<BigInteger, ActivityWrapper> activityWrapperMap) {
        List<Shift> shiftList = new ArrayList<>();
        Shift shift1 = ObjectMapperUtils.copyPropertiesByMapper(shift, Shift.class);
        shift1.setActivities(new ArrayList<>());
        shift1.setBreakActivities(new ArrayList<>());
        Iterator iterator = shift.getActivities().iterator();
        while (iterator.hasNext()) {
            ShiftActivity shiftActivity = (ShiftActivity) iterator.next();
            if (GAP.equals(activityWrapperMap.get(shiftActivity.getActivityId()).getTimeTypeInfo().getSecondLevelType())) {
                shift1.setEndDate(shiftActivity.getStartDate());
                shiftBreakService.updateBreak(shift, shift1, shiftActivity);
                shiftList.add(shift1);
                shift1 = ObjectMapperUtils.copyPropertiesByMapper(shift, Shift.class);
                shift1.setActivities(new ArrayList<>());
                shift1.setBreakActivities(new ArrayList<>());
                continue;
            }
            shift1.getActivities().add(shiftActivity);
            if (!iterator.hasNext()) {
                shift1.setStartDate(shift1.getActivities().get(0).getStartDate());
                shift1.setEndDate(shift1.getActivities().get(shift1.getActivities().size() - 1).getEndDate());
                shiftBreakService.updateBreak(shift, shift1, shiftActivity);
                shiftList.add(shift1);
            }
        }
        return shiftList;
    }

    public Shift findOneByShiftId(BigInteger shiftId){
        Optional<Shift> shiftOptional = shiftMongoRepository.findById(shiftId);
        if(!shiftOptional.isPresent()){
            exceptionService.dataNotFoundByIdException(MESSAGE_SHIFT_ID, shiftId);
        }
        return shiftOptional.get();
    }

    public List<Shift> findShiftBetweenDurationByStaffId(Long staffId, Date startDate, Date endDate){
        return shiftMongoRepository.findShiftBetweenDurationByStaffId(staffId, startDate, endDate);
    }

    public Set<Long> getNotEligibleStaffsForCoverShifts(Date startDate, Date endDate, CoverShiftSetting coverShiftSetting, List<Long> staffIds){
        return shiftMongoRepository.getNotEligibleStaffsForCoverShifts(startDate,endDate,coverShiftSetting,staffIds);
    }

}
