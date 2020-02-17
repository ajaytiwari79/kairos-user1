package com.kairos.service.shift;

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.constants.CommonConstants;
import com.kairos.dto.activity.shift.ShiftActivityDTO;
import com.kairos.dto.activity.shift.ShiftDTO;
import com.kairos.dto.activity.shift.ShiftWithViolatedInfoDTO;
import com.kairos.dto.user.user.staff.StaffAdditionalInfoDTO;
import com.kairos.enums.TimeTypeEnum;
import com.kairos.enums.phase.PhaseDefaultName;
import com.kairos.enums.shift.ShiftActionType;
import com.kairos.enums.shift.ShiftStatus;
import com.kairos.enums.shift.ShiftType;
import com.kairos.persistence.model.activity.Activity;
import com.kairos.persistence.model.activity.ActivityWrapper;
import com.kairos.persistence.model.activity.tabs.rules_activity_tab.SicknessSetting;
import com.kairos.persistence.model.common.MongoBaseEntity;
import com.kairos.persistence.model.phase.Phase;
import com.kairos.persistence.model.shift.Shift;
import com.kairos.persistence.model.shift.ShiftActivity;
import com.kairos.persistence.repository.activity.ActivityMongoRepository;
import com.kairos.persistence.repository.attendence_setting.SickSettingsRepository;
import com.kairos.persistence.repository.cta.CostTimeAgreementRepository;
import com.kairos.persistence.repository.period.PlanningPeriodMongoRepository;
import com.kairos.persistence.repository.shift.ShiftMongoRepository;
import com.kairos.persistence.repository.time_bank.TimeBankRepository;
import com.kairos.persistence.repository.wta.WorkingTimeAgreementMongoRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.MongoBaseService;
import com.kairos.service.activity.ActivityService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.phase.PhaseService;
import com.kairos.service.time_bank.TimeBankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.*;
import static com.kairos.commons.utils.ObjectUtils.isCollectionEmpty;
import static com.kairos.commons.utils.ObjectUtils.newHashSet;
import static com.kairos.constants.ActivityMessagesConstants.*;
import static com.kairos.enums.EmploymentSubType.MAIN;
import static com.kairos.enums.sickness.ReplaceSickShift.*;

/**
 * CreatedBy vipulpandey on 31/8/18
 **/
@Service
public class ShiftSickService extends MongoBaseService {
    private static final Logger logger = LoggerFactory.getLogger(ShiftSickService.class);

    @Inject
    private ActivityMongoRepository activityRepository;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private CostTimeAgreementRepository costTimeAgreementRepository;
    @Inject
    private ShiftMongoRepository shiftMongoRepository;
    @Inject
    private PlanningPeriodMongoRepository planningPeriodMongoRepository;
    @Inject
    private SickSettingsRepository sickSettingsRepository;
    @Inject
    private ShiftService shiftService;
    @Inject
    private PhaseService phaseService;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Inject
    private WorkingTimeAgreementMongoRepository workingTimeAgreementMongoRepository;
    @Inject
    private TimeBankRepository timeBankRepository;
    @Inject
    private TimeBankService timeBankService;
    @Inject
    private ShiftBreakService shiftBreakService;
    @Inject
    private AbsenceShiftService absenceShiftService;
    @Inject private ActivityService activityService;


    public List<ShiftWithViolatedInfoDTO> createSicknessShiftsOfStaff(ShiftDTO shiftDTO,StaffAdditionalInfoDTO staffAdditionalInfoDTO,ActivityWrapper activityWrapper) {
        byte shiftNeedsToAddForDays = activityWrapper.getActivity().getRulesActivityTab().isAllowedAutoAbsence() ? activityWrapper.getActivity().getRulesActivityTab().getRecurrenceDays() : 0;
        if(shiftNeedsToAddForDays==0){
            shiftNeedsToAddForDays = 1;
        }
        List<ActivityWrapper> nonWorkingSicknessActivityWrappers = activityRepository.getAllActivityWrapperBySecondLevelTimeType(TimeTypeEnum.PLANNED_SICK_ON_FREE_DAYS.toString(),shiftDTO.getUnitId());
        if(isCollectionEmpty(nonWorkingSicknessActivityWrappers)){
            exceptionService.dataNotFoundException(MESSAGE_NON_WORKING_SICKNESS_ACTIVITY_NOT_FOUND);
        }
        if(!MAIN.equals(staffAdditionalInfoDTO.getEmployment().getEmploymentSubType())){
            exceptionService.dataNotFoundException(EMPLOYMENT_NOT_VALID_TO_MARK_SICK);
        }

        Date startDate = asDate(shiftDTO.getShiftDate(), LocalTime.MIDNIGHT);
        Date endDate = asDate(shiftDTO.getShiftDate().plusDays(CommonConstants.FULL_WEEK.equals(activityWrapper.getActivity().getTimeCalculationActivityTab().getMethodForCalculatingTime()) ? 7 * shiftNeedsToAddForDays : 1 * shiftNeedsToAddForDays), LocalTime.MIDNIGHT);
        setStartAndEndDate(shiftDTO, startDate);
        Phase phase=phaseService.getCurrentPhaseByUnitIdAndDate(shiftDTO.getUnitId(),startDate,asDate(shiftDTO.getShiftDate().plusDays(1), LocalTime.MIDNIGHT));
        List<Shift> shifts = shiftMongoRepository.findAllShiftsByStaffIdBetweenDate(shiftDTO.getStaffId(),startDate,endDate);
        Map<LocalDate,List<Shift>> shiftMap = shifts.stream().collect(Collectors.groupingBy(shift->asLocalDate(shift.getStartDate())));
        Map<BigInteger,ActivityWrapper> activityWrapperMap = activityService.getActivityWrapperMap(shifts,shiftDTO);
        List<ShiftDTO> shiftDTOS = new ArrayList<>();
        List<ShiftWithViolatedInfoDTO> shiftWithViolatedInfoDTOS = new ArrayList<>();
            int i=0;
            while ( i<shiftNeedsToAddForDays) {
                ShiftDTO shiftDTO1 = ObjectMapperUtils.copyPropertiesByMapper(shiftDTO,ShiftDTO.class);
                shiftDTO1 = updateShiftWithSetting(nonWorkingSicknessActivityWrappers.get(0),activityWrapperMap,shiftDTO1.getShiftDate().plusDays(i),shiftDTO1,shiftMap, PhaseDefaultName.REALTIME.equals(phase.getPhaseEnum()));
                shiftDTO1.setShiftDate(shiftDTO1.getShiftDate().plusDays(i));
                ShiftWithViolatedInfoDTO shiftWithViolatedInfoDTO = shiftService.saveShift(staffAdditionalInfoDTO, shiftDTO1, phase, false, ShiftActionType.SAVE);
                shiftWithViolatedInfoDTOS.add(shiftWithViolatedInfoDTO);
                shiftDTOS.add(shiftDTO1);
                i++;
            }
        return null;//shiftService.createShifts(shiftDTO.getUnitId(),shiftDTOS,ShiftActionType.SAVE);

    }

    public void setStartAndEndDate(ShiftDTO shiftDTO, Date startDate) {
        shiftDTO.setStartDate(startDate);
        shiftDTO.setEndDate(asDate(shiftDTO.getShiftDate().plusDays(1), LocalTime.MIDNIGHT));
        shiftDTO.getActivities().get(0).setStartDate(startDate);
        shiftDTO.getActivities().get(0).setEndDate(asDate(shiftDTO.getShiftDate().plusDays(1), LocalTime.MIDNIGHT));
    }

    private ShiftDTO updateShiftWithSetting(ActivityWrapper nonWorkingSicknessActivityWrapper,Map<BigInteger,ActivityWrapper> activityWrapperMap, LocalDate shiftDate,ShiftDTO shiftDTO,Map<LocalDate,List<Shift>> shiftMap,boolean realTime) {
        List<Shift> shifts = shiftMap.getOrDefault(shiftDate,new ArrayList<>());
        DateTimeInterval dateTimeInterval = new DateTimeInterval(shiftDate,shiftDate.plusDays(1));
        List<ShiftActivityDTO> shiftActivityDTOS = new ArrayList<>();
        for (Shift shift : shifts) {
            shiftActivityDTOS.addAll(updateShiftOnTheBasisOfLayerSetting(nonWorkingSicknessActivityWrapper,activityWrapperMap, shift,shiftDTO,dateTimeInterval));
        }
        shiftDTO.setActivities(shiftActivityDTOS);
        shiftDTO.mergeShiftActivity();
        shiftDTO.setStartDate(dateTimeInterval.getStartDate());
        shiftDTO.setEndDate(dateTimeInterval.getEndDate());
        return shiftDTO;
    }

    private List<ShiftActivityDTO> updateShiftOnTheBasisOfLayerSetting(ActivityWrapper nonWorkingSicknessActivityWrapper,Map<BigInteger,ActivityWrapper> activityWrapperMap, Shift shift,ShiftDTO shiftDTO,DateTimeInterval dateTimeInterval) {
        ActivityWrapper sickActivityWrapper = activityWrapperMap.get(shiftDTO.getActivities().get(0).getActivityId());
        SicknessSetting sicknessSetting = sickActivityWrapper.getActivity().getRulesActivityTab().getSicknessSetting();
        boolean isLayerSettingEnabled = isLayerSettingEnabled(shift, sicknessSetting);
        List<ShiftActivityDTO> shiftActivityDTOS = new ArrayList<>();
        if(isLayerSettingEnabled) {
            for (ShiftActivity shiftActivity : shift.getActivities()) {
                ActivityWrapper activityWrapper = activityWrapperMap.get(shiftActivity.getActivityId());
                ShiftActivityDTO updatedShiftActivity;
                if(newHashSet(TimeTypeEnum.ABSENCE,TimeTypeEnum.PRESENCE).contains(activityWrapper.getActivity().getBalanceSettingsActivityTab().getTimeType())){
                    shiftActivityDTOS.add(new ShiftActivityDTO(sickActivityWrapper.getActivity().getName(),shiftActivity.getStartDate(),shiftActivity.getEndDate(),sickActivityWrapper.getActivity().getId(),null));
                }else {
                    updatedShiftActivity = new ShiftActivityDTO(sickActivityWrapper.getActivity().getName(),shiftActivity.getStartDate(),shiftActivity.getEndDate(),sickActivityWrapper.getActivity().getId(),null);
                    shiftActivityDTOS.add(updatedShiftActivity);
                }
            }
            shift.setDisabled(true);

        } else if(shift.getInterval().contains(getCurrentDate())){
            replaceWithSick(shift,sickActivityWrapper);
        }

        else {
            shift.setDeleted(true);
        }
        shiftActivityDTOS.addAll(getShiftActivityDTOByIntervals(dateTimeInterval,nonWorkingSicknessActivityWrapper,shift.getInterval(),isLayerSettingEnabled));
        return shiftActivityDTOS;
    }

    private void replaceWithSick(Shift shift, ActivityWrapper sickActivityWrapper) {
        List<ShiftActivity> shiftActivities=new ArrayList<>();
        for (int i = 0; i < shift.getActivities().size(); i++) {
            if(shift.getActivities().get(i).getInterval().contains(getCurrentDate())){
                shift.getActivities().get(i).setEndDate(getCurrentDate());
                shiftActivities.add(shift.getActivities().get(i));
                ShiftActivity shiftActivity=new ShiftActivity(sickActivityWrapper.getActivity().getName(),shift.getActivities().get(i).getStartDate(),shift.getActivities().get(i).getEndDate(),sickActivityWrapper.getActivity().getId(),null);
                shiftActivities.add(shiftActivity);
                break;
            }
            else {
                shiftActivities.add(shift.getActivities().get(i));
            }
        }
        shift.setActivities(shiftActivities);
    }


    private List<ShiftActivityDTO> getShiftActivityDTOByIntervals(DateTimeInterval dateTimeInterval, ActivityWrapper sickActivityWrapper, DateTimeInterval otherInterval,boolean isLayerSatingEnabled) {
        List<DateTimeInterval> dateTimeIntervals =isLayerSatingEnabled ? dateTimeInterval.minusInterval(otherInterval): Collections.singletonList(dateTimeInterval);
        List<ShiftActivityDTO> shiftActivityDTOS = new ArrayList<>();
        for (DateTimeInterval timeInterval : dateTimeIntervals) {
            ShiftActivityDTO updatedShiftActivity = new ShiftActivityDTO(sickActivityWrapper.getActivity().getName(),timeInterval.getStartDate(),timeInterval.getEndDate(),sickActivityWrapper.getActivity().getId(),null);
            shiftActivityDTOS.add(updatedShiftActivity);
        }
        return shiftActivityDTOS;
    }

    private boolean isLayerSettingEnabled(Shift shift, SicknessSetting sicknessSetting) {
        boolean publishedShiftOverlayEnabled = shift.getShiftStatuses().contains(ShiftStatus.PUBLISH) && sicknessSetting.isShowAslayerOnTopOfPublishedShift();
        boolean unpublisedShiftOverlayEnabled = sicknessSetting.isShowAslayerOnTopOfUnPublishedShift();
        return publishedShiftOverlayEnabled || unpublisedShiftOverlayEnabled;
    }

    public void disableSicknessShiftsOfStaff(Long employmentId, Long unitId,LocalDate localDate) {
        List<Activity> sicknessActivity = activityRepository.findAllSicknessActivity(unitId);
        Set<BigInteger> activityIds = sicknessActivity.stream().map(MongoBaseEntity::getId).collect(Collectors.toSet());
        List<Shift> shifts = shiftMongoRepository.findAllSicknessShiftByEmploymentIdAndActivityIds(employmentId,activityIds,asDate(localDate));
        if(isCollectionEmpty(shifts)){
            exceptionService.dataNotFoundException("Data not found");
        }
        Map<LocalDate,List<Shift>> shiftMap = shifts.stream().collect(Collectors.groupingBy(shift->asLocalDate(shift.getStartDate())));
        BigInteger activityId = shifts.get(0).getActivities().get(0).getActivityId();
        Optional<Activity> activityOptional = sicknessActivity.stream().filter(activity -> activity.getId().equals(activityId)).findFirst();
        LocalDate startDate = localDate;
        LocalDate endDate = localDate;
        if(activityOptional.isPresent()){
            Activity activity = activityOptional.get();
            if(PROTECTED_DAYS_OFF.equals(activity.getRulesActivityTab().getSicknessSetting().getReplaceSickShift())){
                List<ActivityWrapper> protectDaysOffActivity = activityRepository.getAllActivityWrapperBySecondLevelTimeType(TimeTypeEnum.PROTECTED_DAYS_OFF.toString(),unitId);
                if(isCollectionEmpty(protectDaysOffActivity)){
                    exceptionService.dataNotFoundException(MESSAGE_PROTECTEDDAYSOFF_ACTIVITY_NOT_FOUND);
                }
            }
            Map<LocalDate,List<Shift>> shiftDateMap = shifts.stream().filter(shift -> shift.isActivityMatch(activity.getId(),false)).collect(Collectors.groupingBy(shift->asLocalDate(shift.getStartDate())));
            while (true){
                if(shiftDateMap.containsKey(localDate)){
                    shifts = shiftDateMap.get(localDate);
                    localDate = localDate.plusDays(1);
                }else {
                    endDate = localDate;
                    break;
                }
            }
        }
    }


    private void UpdateSickShift(Activity activity, Map<LocalDate,List<Shift>> shiftMap, Activity protectedDaysOffActivity) {
        shiftMap.forEach((date,shifts)->{
            Shift shift=shifts.stream().filter(k->k.getShiftType().equals(ShiftType.SICK)).findAny().orElse(null);
            switch (activity.getRulesActivityTab().getSicknessSetting().getReplaceSickShift()) {
                case PROTECTED_DAYS_OFF:
                    shift.getActivities().forEach(shiftActivity -> shiftActivity.setActivityId(protectedDaysOffActivity.getId()));
                    break;
                case FREE_DAY:
                    shift.setDeleted(true);
                    break;
                case PUBLISHED_ACTIVITY:

                    break;
                case UNPUBLISHED_ACTIVITY:
                    break;
                default:
                    break;
            }
        });

    }
}









