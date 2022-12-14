package com.kairos.service.staffing_level;

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.activity.shift.ShiftActivityDTO;
import com.kairos.dto.activity.staffing_level.StaffingLevelActivity;
import com.kairos.dto.activity.staffing_level.StaffingLevelActivityWithDuration;
import com.kairos.dto.activity.staffing_level.StaffingLevelInterval;
import com.kairos.dto.user_context.UserContext;
import com.kairos.persistence.model.activity.ActivityWrapper;
import com.kairos.persistence.model.country.GeneralSettings;
import com.kairos.persistence.model.phase.Phase;
import com.kairos.persistence.model.shift.Shift;
import com.kairos.persistence.model.shift.ShiftActivity;
import com.kairos.persistence.model.staffing_level.StaffingLevel;
import com.kairos.persistence.model.unit_settings.PhaseSettings;
import com.kairos.persistence.repository.staffing_level.StaffingLevelMongoRepository;
import com.kairos.persistence.repository.unit_settings.PhaseSettingsRepository;
import com.kairos.service.country.GeneralSettingsService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.phase.PhaseService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.ActivityMessagesConstants.*;
import static com.kairos.enums.TimeTypeEnum.PRESENCE;

@Service
public class StaffingLevelValidatorService {

    @Inject
    private PhaseSettingsRepository phaseSettingsRepository;
    @Inject
    private PhaseService phaseService;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private StaffingLevelMongoRepository staffingLevelMongoRepository;
    @Inject
    private StaffingLevelService staffingLevelService;
    @Inject private GeneralSettingsService generalSettingsService;


    public boolean validateStaffingLevel(Phase phase, Shift shift, Map<BigInteger, ActivityWrapper> activityWrapperMap, boolean checkOverStaffing, ShiftActivity shiftActivity, Map<BigInteger, StaffingLevelActivityWithDuration> staffingLevelActivityWithDurationMap, boolean gapFilling, GeneralSettings generalSettings, boolean byUpdate) {
        Date shiftStartDate = shiftActivity.getStartDate();
        Date shiftEndDate = shiftActivity.getEndDate();
        PhaseSettings phaseSettings = phaseSettingsRepository.getPhaseSettingsByUnitIdAndPhaseId(shift.getUnitId(), phase.getId());
        if (!Optional.ofNullable(phaseSettings).isPresent()) {
            exceptionService.dataNotFoundException(MESSAGE_PHASESETTINGS_ABSENT);
        }
        Date startDate = DateUtils.getDateByZoneDateTime(DateUtils.asZonedDateTime(shiftStartDate).truncatedTo(ChronoUnit.DAYS));
        Date endDate = DateUtils.getDateByZoneDateTime(DateUtils.asZonedDateTime(shiftEndDate).truncatedTo(ChronoUnit.DAYS));
        List<StaffingLevel> staffingLevels = newArrayList();
        if (!byUpdate && activityWrapperMap.get(shiftActivity.getActivityId()).getActivity().getActivityRulesSettings().isEligibleForStaffingLevel()) {
            if (isNotNull(generalSettings) && (UserContext.getUserDetails().isManagement() && !generalSettings.isShiftCreationAllowForManagement()) || (UserContext.getUserDetails().isStaff() && !generalSettings.isShiftCreationAllowForStaff())) {
                staffingLevels = staffingLevelMongoRepository.findByUnitIdAndActivityIdBetweenDates(shift.getUnitId(), startDate, endDate, shiftActivity.getActivityId());
                if (startDate.equals(endDate) && isCollectionEmpty(staffingLevels) || (!startDate.equals(endDate) && staffingLevels.size()!=2)) {
                    exceptionService.actionNotPermittedException(MESSAGE_STAFFINGLEVEL_ACTIVITY, shiftActivity.getActivityName());
                }
            }
        }
        boolean isStaffingLevelVerify = gapFilling || isVerificationRequired(checkOverStaffing, phaseSettings);
        if (isStaffingLevelVerify) {
            staffingLevels = isCollectionEmpty(staffingLevels) ? staffingLevelMongoRepository.getStaffingLevelsByUnitIdAndDate(shift.getUnitId(), startDate, endDate) : staffingLevels;
            validateUnderAndOverStaffing(shift, activityWrapperMap, checkOverStaffing, staffingLevels, shiftActivity, staffingLevelActivityWithDurationMap, gapFilling,generalSettings,byUpdate);
            staffingLevelMongoRepository.saveEntities(staffingLevels);
        }
        return isStaffingLevelVerify;
    }

    public boolean verifyStaffingLevel(Map<BigInteger, StaffingLevelActivityWithDuration> staffingLevelActivityWithDurationMapForUnderStaffing, Map<BigInteger, StaffingLevelActivityWithDuration> staffingLevelActivityWithDurationMap, Short allowedMaxOverStaffing, Shift oldShift, Map<BigInteger, ActivityWrapper> activityWrapperMap, Boolean gapFilling, ShiftActivityDTO replacedActivity,boolean coverShiftEligibility) {
        int totalUnderStaffingCreated = staffingLevelActivityWithDurationMapForUnderStaffing.values().stream().mapToInt(StaffingLevelActivityWithDuration::getUnderStaffingDurationInMinutes).sum();
        int totalOverStaffingLevelResolve = staffingLevelActivityWithDurationMapForUnderStaffing.values().stream().mapToInt(StaffingLevelActivityWithDuration::getResolvingUnderOrOverStaffingDurationInMinutes).sum();
        int totalUnderStaffingResolved = staffingLevelActivityWithDurationMap.values().stream().mapToInt(StaffingLevelActivityWithDuration::getResolvingUnderOrOverStaffingDurationInMinutes).sum();
        int totalOverStaffingCreated = staffingLevelActivityWithDurationMap.values().stream().mapToInt(StaffingLevelActivityWithDuration::getOverStaffingDurationInMinutes).sum();
        boolean validStaffingLevel = true;
        if (staffingLevelActivityWithDurationMapForUnderStaffing.isEmpty()) {
            if (totalUnderStaffingResolved < totalOverStaffingCreated) {
                if(coverShiftEligibility) return false;
                exceptionService.actionNotPermittedException(MESSAGE_SHIFT_OVERSTAFFING_ADD);
            }
        } else if (staffingLevelActivityWithDurationMap.isEmpty()) {
            if (totalOverStaffingLevelResolve < totalUnderStaffingCreated) {
                if(coverShiftEligibility) return false;
                exceptionService.actionNotPermittedException(MESSAGE_SHIFT_OVERSTAFFING_DELETE);
            }
        } else {
            validStaffingLevel = isValidStaffingLevel(allowedMaxOverStaffing, gapFilling, replacedActivity, coverShiftEligibility, totalUnderStaffingCreated, totalUnderStaffingResolved, totalOverStaffingCreated, oldShift, activityWrapperMap);
        }
        return validStaffingLevel;
    }

    private boolean isValidStaffingLevel(Short allowedMaxOverStaffing, Boolean gapFilling, ShiftActivityDTO replacedActivity, boolean coverShiftEligibility, int totalUnderStaffingCreated, int totalUnderStaffingResolved, int totalOverStaffingCreated, Shift oldShift, Map<BigInteger, ActivityWrapper> activityWrapperMap) {
        if (totalUnderStaffingCreated > totalUnderStaffingResolved || (allowedMaxOverStaffing != null && allowedMaxOverStaffing < totalOverStaffingCreated)) {
            if(coverShiftEligibility) return false;
            suggestError(totalUnderStaffingCreated, totalUnderStaffingResolved, replacedActivity == null ?MESSAGE_SHIFT_STAFFING_LEVEL_REPLACE_WITH_ACTIVITY   : gapFilling ? MESSAGE_SHIFT_OVERSTAFFING_GAP :MESSAGE_SHIFT_STAFFING_LEVEL_REPLACE_WITHOUT_ACTIVITY);
        } else if (totalUnderStaffingCreated == totalUnderStaffingResolved) {
            return isHigherActivity(oldShift, activityWrapperMap, replacedActivity,coverShiftEligibility);
        }
        return true;
    }

    public void suggestError(int totalUnderStaffingCreated, int totalUnderStaffingResolved, String messageShiftStaffingLevelReplaceWithoutActivity) {
        if (totalUnderStaffingCreated > totalUnderStaffingResolved) {
            exceptionService.actionNotPermittedException(messageShiftStaffingLevelReplaceWithoutActivity);
        } else {
            exceptionService.actionNotPermittedException(MESSAGE_SHIFT_STAFFING_LEVEL_PHASE_SETTING);
        }
    }

    private ShiftActivity[] activityReplaced(Shift dbShift, ShiftActivityDTO replacedActivity, Map<BigInteger, ActivityWrapper> activityWrapperMap) {
        ShiftActivity dbShiftActivityForCheckingRank = getActivity(dbShift, replacedActivity, activityWrapperMap);
        return new ShiftActivity[]{dbShiftActivityForCheckingRank, ObjectMapperUtils.copyPropertiesByMapper(replacedActivity, ShiftActivity.class)};
    }

    private ShiftActivity getActivity(Shift dbShift, ShiftActivityDTO replacedActivity, Map<BigInteger, ActivityWrapper> activityWrapperMap) {
        ShiftActivity shiftActivityToCheck = null;
        long maxValue = 0;
        for (int i = 0; i < dbShift.getActivities().size(); i++) {
            long overLapDuration = replacedActivity.getInterval().overlapMinutes(dbShift.getActivities().get(i).getInterval());
            if (overLapDuration != 0 && overLapDuration >= maxValue) {
                if (overLapDuration > maxValue) {
                    shiftActivityToCheck = dbShift.getActivities().get(i);
                    maxValue = overLapDuration;
                } else {
                    shiftActivityToCheck = activityWrapperMap.get(dbShift.getActivities().get(i).getActivityId()).getRanking() < activityWrapperMap.get(dbShift.getActivities().get(i - 1).getActivityId()).getRanking() ? dbShift.getActivities().get(i) : shiftActivityToCheck;
                }
            }
        }
        return shiftActivityToCheck;
    }

    private boolean isHigherActivity(Shift oldShift, Map<BigInteger, ActivityWrapper> activityWrapperMap, ShiftActivityDTO replacedActivity, boolean coverShiftEligibity) {
        ShiftActivity[] shiftActivities = replacedActivity == null ? null : activityReplaced(oldShift, replacedActivity, activityWrapperMap);
        if (shiftActivities != null) {
            int rankOfOld = activityWrapperMap.get(shiftActivities[0].getActivityId()).getRanking();
            int rankOfNew = activityWrapperMap.get(shiftActivities[1].getActivityId()).getRanking();
            if (rankOfNew > rankOfOld) {
                if(coverShiftEligibity) return false;
                exceptionService.actionNotPermittedException(SHIFT_CAN_NOT_MOVE);
            }
        }
        return true;
    }

    public boolean isVerificationRequired(boolean checkOverStaffing, PhaseSettings phaseSettings) {
        boolean result = false;
        if (UserContext.getUserDetails().isStaff()) {
            result = checkOverStaffing ? !phaseSettings.isStaffEligibleForOverStaffing() : !phaseSettings.isStaffEligibleForUnderStaffing();
        } else if (UserContext.getUserDetails().isManagement()) {
            result = checkOverStaffing ? !phaseSettings.isManagementEligibleForOverStaffing() : !phaseSettings.isManagementEligibleForUnderStaffing();
        }
        return result;
    }


    private void validateUnderAndOverStaffing(Shift shift, Map<BigInteger, ActivityWrapper> activityWrapperMap, boolean checkOverStaffing, List<StaffingLevel> staffingLevels, ShiftActivity shiftActivity, Map<BigInteger, StaffingLevelActivityWithDuration> staffingLevelActivityWithDurationMap, boolean gapFilling, GeneralSettings generalSettingsForUnit, boolean byUpdate) {
        ActivityWrapper activityWrapper = activityWrapperMap.get(shiftActivity.getActivityId());
        if (activityWrapper.getActivity().getActivityRulesSettings().isEligibleForStaffingLevel()) {
            if (CollectionUtils.isEmpty(staffingLevels)) {
                if(gapFilling){
                    return;
                }
                if(isShiftCreationAllowedWithOutStaffinglevel(generalSettingsForUnit,byUpdate)) {
                    return;
                }
                exceptionService.actionNotPermittedException(MESSAGE_STAFFINGLEVEL_ABSENT);
            }
            StaffingLevel staffingLevel = staffingLevels.get(0);
            int lowerLimit;
            int upperLimit;
            if (PRESENCE.equals(activityWrapper.getActivity().getActivityBalanceSettings().getTimeType())) {
                List<StaffingLevelInterval> applicableIntervals = staffingLevel.getPresenceStaffingLevelInterval();
                if (!DateUtils.getLocalDateFromDate(shiftActivity.getStartDate()).equals(DateUtils.getLocalDateFromDate(shiftActivity.getEndDate()))) {
                    lowerLimit = staffingLevelService.getLowerIndex(shiftActivity.getStartDate());
                    upperLimit = 95;
                    checkStaffingLevelInterval(lowerLimit, upperLimit, applicableIntervals, staffingLevel, shift, checkOverStaffing, shiftActivity, staffingLevelActivityWithDurationMap, gapFilling,generalSettingsForUnit,byUpdate);
                    lowerLimit = 0;
                    upperLimit = staffingLevelService.getUpperIndex(shiftActivity.getEndDate());
                    if (staffingLevels.size() < 2) {
                        if(gapFilling){
                            return;
                        }
                        if(isShiftCreationAllowedWithOutStaffinglevel(generalSettingsForUnit,byUpdate)) {
                            return;
                        }
                        exceptionService.actionNotPermittedException(MESSAGE_STAFFINGLEVEL_ABSENT);
                    }
                    staffingLevel = staffingLevels.get(1);
                    applicableIntervals = staffingLevel.getPresenceStaffingLevelInterval();

                    checkStaffingLevelInterval(lowerLimit, upperLimit, applicableIntervals, staffingLevel, shift, checkOverStaffing, shiftActivity, staffingLevelActivityWithDurationMap, gapFilling, generalSettingsForUnit, byUpdate);

                } else {
                    lowerLimit = staffingLevelService.getLowerIndex(shiftActivity.getStartDate());
                    upperLimit = staffingLevelService.getUpperIndex(shiftActivity.getEndDate());
                    checkStaffingLevelInterval(lowerLimit, upperLimit, applicableIntervals, staffingLevel, shift, checkOverStaffing, shiftActivity, staffingLevelActivityWithDurationMap, gapFilling, generalSettingsForUnit, byUpdate);
                }
            } else if(!gapFilling){
                validateStaffingLevelForAbsenceTypeOfShift(staffingLevel, shiftActivity, checkOverStaffing, shift,generalSettingsForUnit,byUpdate);
            }
        }

    }
    private boolean isShiftCreationAllowedWithOutStaffinglevel(GeneralSettings generalSettingsForUnit, boolean byUpdate) {
        return isNotNull(generalSettingsForUnit) && !byUpdate && ((UserContext.getUserDetails().isStaff() && generalSettingsForUnit.isShiftCreationAllowForStaff()) || (UserContext.getUserDetails().isManagement() && generalSettingsForUnit.isShiftCreationAllowForManagement()));
    }

    private void checkStaffingLevelInterval(int lowerLimit, int upperLimit, List<StaffingLevelInterval> applicableIntervals, StaffingLevel staffingLevel, Shift shift, boolean checkOverStaffing, ShiftActivity shiftActivity, Map<BigInteger, StaffingLevelActivityWithDuration> staffingLevelActivityWithDurationMap, boolean gapFilling, GeneralSettings generalSettingsForUnit, boolean byUpdate) {
        for (int currentIndex = lowerLimit; currentIndex <= upperLimit && currentIndex < applicableIntervals.size(); currentIndex++) {
            int shiftsCount;
            Optional<StaffingLevelActivity> staffingLevelActivity = applicableIntervals.get(currentIndex).getStaffingLevelActivities().stream().filter(sa -> sa.getActivityId().equals(shiftActivity.getActivityId())).findFirst();
            if (staffingLevelActivity.isPresent()) {
                ZonedDateTime startDate = ZonedDateTime.ofInstant(staffingLevel.getCurrentDate().toInstant(), ZoneId.systemDefault()).with(staffingLevel.getPresenceStaffingLevelInterval().get(currentIndex).getStaffingLevelDuration().getFrom());
                ZonedDateTime endDate = ZonedDateTime.ofInstant(staffingLevel.getCurrentDate().toInstant(), ZoneId.systemDefault()).with(staffingLevel.getPresenceStaffingLevelInterval().get(currentIndex).getStaffingLevelDuration().getTo());
                DateTimeInterval interval = new DateTimeInterval(startDate, endDate);
                shiftsCount = applicableIntervals.get(currentIndex).getStaffingLevelActivities().stream().filter(k -> k.getActivityId().equals(shiftActivity.getActivityId())).findAny().get().getAvailableNoOfStaff();
                boolean breakValid = shift.getBreakActivities().stream().anyMatch(shiftActivity1 -> !shiftActivity1.isBreakNotHeld() && interval.overlaps(shiftActivity1.getInterval()));
                shiftsCount = updateShiftCount(shift, shiftActivity, shiftsCount, interval, breakValid, checkOverStaffing);
                int totalCount = shiftsCount - (checkOverStaffing ? staffingLevelActivity.get().getMaxNoOfStaff() : staffingLevelActivity.get().getMinNoOfStaff());
                checkOverStaffing(checkOverStaffing, shiftActivity, staffingLevelActivityWithDurationMap, breakValid, totalCount);
                checkUnderStaffing(checkOverStaffing, shiftActivity, staffingLevelActivityWithDurationMap, breakValid, totalCount);
            } else if(!gapFilling){
                if(isShiftCreationAllowedWithOutStaffinglevel(generalSettingsForUnit,byUpdate)) {
                    return;
                }
                exceptionService.actionNotPermittedException(MESSAGE_STAFFINGLEVEL_ACTIVITY, shiftActivity.getActivityName());
            }
        }

    }
    private void checkUnderStaffing(boolean checkOverStaffing, ShiftActivity shiftActivity, Map<BigInteger, StaffingLevelActivityWithDuration> staffingLevelActivityWithDurationMap, boolean breakValid, int totalCount) {
        if (!checkOverStaffing) {
            StaffingLevelActivityWithDuration staffingLevelActivityWithDuration = staffingLevelActivityWithDurationMap.getOrDefault(shiftActivity.getActivityId(), new StaffingLevelActivityWithDuration(shiftActivity.getActivityId()));
            if (totalCount <= 0 && !breakValid) {
                staffingLevelActivityWithDuration.setUnderStaffingDurationInMinutes((short) (staffingLevelActivityWithDuration.getUnderStaffingDurationInMinutes() + 15));
            } else if (!breakValid) {
                staffingLevelActivityWithDuration.setResolvingUnderOrOverStaffingDurationInMinutes((short) (staffingLevelActivityWithDuration.getResolvingUnderOrOverStaffingDurationInMinutes() + 15));
            }
            staffingLevelActivityWithDurationMap.put(shiftActivity.getActivityId(), staffingLevelActivityWithDuration);
        }
    }
    private void checkOverStaffing(boolean checkOverStaffing, ShiftActivity shiftActivity, Map<BigInteger, StaffingLevelActivityWithDuration> staffingLevelActivityWithDurationMap, boolean breakValid, int totalCount) {
        if (checkOverStaffing) {
            StaffingLevelActivityWithDuration staffingLevelActivityWithDuration = staffingLevelActivityWithDurationMap.getOrDefault(shiftActivity.getActivityId(), new StaffingLevelActivityWithDuration(shiftActivity.getActivityId()));
            if (totalCount > 0 && !breakValid) {
                staffingLevelActivityWithDuration.setOverStaffingDurationInMinutes((short) (staffingLevelActivityWithDuration.getOverStaffingDurationInMinutes() + 15));
            } else if (!breakValid) {
                staffingLevelActivityWithDuration.setResolvingUnderOrOverStaffingDurationInMinutes((short) (staffingLevelActivityWithDuration.getResolvingUnderOrOverStaffingDurationInMinutes() + 15));
            }
            staffingLevelActivityWithDurationMap.put(shiftActivity.getActivityId(), staffingLevelActivityWithDuration);
        }
    }

    private void validateStaffingLevelForAbsenceTypeOfShift(StaffingLevel staffingLevel, ShiftActivity shiftActivity, boolean checkOverStaffing, Shift shift, GeneralSettings generalSettingsForUnit, boolean byUpdate) {
        if (CollectionUtils.isEmpty(staffingLevel.getAbsenceStaffingLevelInterval())) {
            if(isShiftCreationAllowedWithOutStaffinglevel(generalSettingsForUnit,byUpdate)) {
                return;
            }
            exceptionService.actionNotPermittedException(MESSAGE_STAFFINGLEVEL_ABSENT);
        }
        int shiftsCount = 0;
        Optional<StaffingLevelActivity> staffingLevelActivity = staffingLevel.getAbsenceStaffingLevelInterval().get(0).getStaffingLevelActivities().stream().filter(sa -> sa.getActivityId().equals(shiftActivity.getActivityId())).findFirst();
        if (!staffingLevelActivity.isPresent()) {
            if(isShiftCreationAllowedWithOutStaffinglevel(generalSettingsForUnit,byUpdate)) {
                return;
            }
            exceptionService.actionNotPermittedException(MESSAGE_STAFFINGLEVEL_ACTIVITY, shiftActivity.getActivityName());
        }
        shiftsCount = staffingLevelActivity.get().getAvailableNoOfStaff();
        for (ShiftActivity currentShiftActivity : shift.getActivities()) {
            if (currentShiftActivity.getActivityId().equals(shiftActivity.getActivityId())) {
                shiftsCount++;
            }
        }
        int totalCount = shiftsCount - (checkOverStaffing ? staffingLevelActivity.get().getMaxNoOfStaff() : staffingLevelActivity.get().getMinNoOfStaff());
        if ((checkOverStaffing && totalCount >= 0)) {
            exceptionService.actionNotPermittedException(MESSAGE_SHIFT_OVERSTAFFING);
        } else if (!checkOverStaffing && totalCount <= 0) {
            exceptionService.actionNotPermittedException(MESSAGE_SHIFT_UNDERSTAFFING);
        }
    }

    private int updateShiftCount(Shift shift, ShiftActivity shiftActivity, int shiftsCount, DateTimeInterval interval, boolean breakValid, boolean checkOverStaffing) {
        if (!checkOverStaffing) {
            return shiftsCount;
        }
        for (ShiftActivity shiftActivityDB : shift.getActivities()) {
            if (!breakValid && shiftActivityDB.getActivityId().equals(shiftActivity.getActivityId()) && interval.overlaps(shiftActivityDB.getInterval())) {
                shiftsCount++;
            }
        }
        return shiftsCount;
    }

}
