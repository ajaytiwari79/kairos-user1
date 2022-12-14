package com.kairos.utils.service_util;

import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.custom_exception.InvalidRequestException;
import com.kairos.dto.activity.common.UserInfo;
import com.kairos.dto.activity.staffing_level.*;
import com.kairos.dto.activity.staffing_level.absence.AbsenceStaffingLevelDto;
import com.kairos.dto.activity.staffing_level.presence.StaffingLevelDTO;
import com.kairos.dto.activity.staffing_level.presence.StaffingLevelActivityDetails;
import com.kairos.dto.user.access_permission.AccessGroupRole;
import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.SkillLevel;
import com.kairos.persistence.model.staffing_level.StaffingLevel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.getDate;
import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.ActivityMessagesConstants.MESSAGE_MIN_GREATER_THAN_MAX;
import static com.kairos.dto.activity.staffing_level.presence.StaffingLevelDTO.StaffingLevelAction.REMOVE;
import static com.kairos.dto.activity.staffing_level.presence.StaffingLevelDTO.StaffingLevelAction.UPDATE;
import static com.kairos.service.shift.ShiftValidatorService.throwException;
import static com.kairos.service.staffing_level.StaffingLevelAvailableCountService.getStaffingLevelActivityDetails;
import static java.util.stream.Collectors.toMap;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StaffingLevelUtil {

    public static StaffingLevel buildPresenceStaffingLevels(StaffingLevelDTO staffingLevelDTO, Long orgId) {
        StaffingLevel staffingLevel = new StaffingLevel(staffingLevelDTO.getCurrentDate(), staffingLevelDTO.getWeekCount()
                , orgId, staffingLevelDTO.getPhaseId(), staffingLevelDTO.getStaffingLevelSetting());

        Set<StaffingLevelInterval> staffingLevelIntervals = new LinkedHashSet<>();
        for (StaffingLevelInterval staffingLevelInterval : staffingLevelDTO.getPresenceStaffingLevelInterval()) {
            StaffingLevelInterval presenceStaffingLevelInterval = new StaffingLevelInterval(staffingLevelInterval.getSequence(), staffingLevelInterval.getStaffingLevelDuration());
            if (staffingLevelDTO.isDraft()) {
                initializeUserWiseLogs(presenceStaffingLevelInterval);
            } else {
                presenceStaffingLevelInterval.addStaffLevelActivity(staffingLevelInterval.getStaffingLevelActivities());
                presenceStaffingLevelInterval.addStaffLevelSkill(staffingLevelInterval.getStaffingLevelSkills());
                presenceStaffingLevelInterval.setMinNoOfStaff(presenceStaffingLevelInterval.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMinNoOfStaff())));
                presenceStaffingLevelInterval.setMaxNoOfStaff(presenceStaffingLevelInterval.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMaxNoOfStaff())));

            }
            staffingLevelIntervals.add(presenceStaffingLevelInterval);
        }

        staffingLevel.addStaffingLevelTimeSlot(staffingLevelIntervals);
        return staffingLevel;

    }

    public static StaffingLevel buildAbsenceStaffingLevels(AbsenceStaffingLevelDto absenceStaffingLevelDto, Long unitId) {

        StaffingLevel staffingLevel = new StaffingLevel(absenceStaffingLevelDto.getCurrentDate(), absenceStaffingLevelDto.getWeekCount()
                , unitId, absenceStaffingLevelDto.getPhaseId());

        Duration staffingLevelDuration = new Duration(LocalTime.MIN, LocalTime.MAX);
        List<StaffingLevelInterval> absenceStaffingLevelIntervals = new ArrayList<>();
        StaffingLevelInterval absenceStaffingLevelInterval = new StaffingLevelInterval(0, absenceStaffingLevelDto.getMinNoOfStaff(),
                absenceStaffingLevelDto.getMaxNoOfStaff(), staffingLevelDuration);
        absenceStaffingLevelInterval.setStaffingLevelActivities(absenceStaffingLevelDto.getStaffingLevelActivities());
        absenceStaffingLevelIntervals.add(absenceStaffingLevelInterval);
        staffingLevel.setAbsenceStaffingLevelInterval(absenceStaffingLevelIntervals);
        return staffingLevel;
    }


    private static Set<StaffingLevelActivity> getStaffingLevelActivities(Map<BigInteger, BigInteger> childAndParentActivityIdMap, StaffingLevelInterval staffingLevelTimeSlotDTO, Map<BigInteger, StaffingLevelActivity> staffingLevelActivityMap, Map<BigInteger, StaffingLevelActivity> staffingLevelActivityMapOfLogs) {
        Set<StaffingLevelActivity> staffingLevelActivities = new HashSet<>();
        Map<BigInteger, StaffingLevelStaffMinMax> activityIdStaffMinMaxMap = new HashMap<>();
        for (StaffingLevelActivity staffingLevelActivity : staffingLevelTimeSlotDTO.getStaffingLevelActivities()) {
            validateParentChildActivityStaffingLevelMinMaxNumberOfStaff(childAndParentActivityIdMap, activityIdStaffMinMaxMap, staffingLevelActivity);
            StaffingLevelActivity staffingLevelActivityNew = new StaffingLevelActivity(staffingLevelActivity.getActivityId(), staffingLevelActivity.getName(),
                    staffingLevelActivity.getMinNoOfStaff(), staffingLevelActivity.getMaxNoOfStaff());
            if (staffingLevelActivityMap.containsKey(staffingLevelActivity.getActivityId())) {
                staffingLevelActivityNew.setAvailableNoOfStaff(staffingLevelActivityMap.get(staffingLevelActivity.getActivityId()).getAvailableNoOfStaff());
            }
            if (isEmpty(staffingLevelActivityMapOfLogs) || !staffingLevelActivityMapOfLogs.containsKey(staffingLevelActivity.getActivityId()) || staffingLevelActivityMapOfLogs.get(staffingLevelActivity.getActivityId()).getMaxNoOfStaff() != staffingLevelActivity.getMaxNoOfStaff()) {
                staffingLevelActivityNew.setMaxUpdatedAt(getDate());
            } else {
                staffingLevelActivityNew.setMaxUpdatedAt(staffingLevelActivityMapOfLogs.get(staffingLevelActivity.getActivityId()).getMaxUpdatedAt());
            }
            if (isEmpty(staffingLevelActivityMapOfLogs) || !staffingLevelActivityMapOfLogs.containsKey(staffingLevelActivity.getActivityId()) || staffingLevelActivityMapOfLogs.get(staffingLevelActivity.getActivityId()).getMinNoOfStaff() != staffingLevelActivity.getMinNoOfStaff()) {
                staffingLevelActivityNew.setMinUpdatedAt(getDate());
            } else {
                staffingLevelActivityNew.setMinUpdatedAt(staffingLevelActivityMapOfLogs.get(staffingLevelActivity.getActivityId()).getMinUpdatedAt());
            }
            staffingLevelActivities.add(staffingLevelActivityNew);

        }
        activityIdStaffMinMaxMap.values().forEach(staffingLevelStaffMinMax -> {
            if (staffingLevelStaffMinMax != null && (staffingLevelStaffMinMax.getMinNoOfStaffParentActivity() < staffingLevelStaffMinMax.getMinNoOfStaffChildActivities() || staffingLevelStaffMinMax.getMaxNoOfStaffParentActivity() < staffingLevelStaffMinMax.getMaxNoOfStaffChildActivities())) {
                throw new InvalidRequestException("child staffing level should be less than or equal to parent count for interval : " + staffingLevelTimeSlotDTO.getStaffingLevelDuration().getFrom() + " to " + staffingLevelTimeSlotDTO.getStaffingLevelDuration().getTo());
            }
        });
        return staffingLevelActivities;
    }

    private static StaffingLevelStaffMinMax validateParentChildActivityStaffingLevelMinMaxNumberOfStaff(Map<BigInteger, BigInteger> childAndParentActivityIdMap, Map<BigInteger, StaffingLevelStaffMinMax> activityIdStaffMinMaxMap, StaffingLevelActivity staffingLevelActivity) {
        BigInteger parentActivityId = childAndParentActivityIdMap.getOrDefault(staffingLevelActivity.getActivityId(), null);
        StaffingLevelStaffMinMax staffingLevelStaffMinMax;
        if (staffingLevelActivity.getMinNoOfStaff() > staffingLevelActivity.getMaxNoOfStaff()) {
            throwException(MESSAGE_MIN_GREATER_THAN_MAX);
        }
        if (parentActivityId == null) {
            staffingLevelStaffMinMax = activityIdStaffMinMaxMap.getOrDefault(staffingLevelActivity.getActivityId(), new StaffingLevelStaffMinMax());
            staffingLevelStaffMinMax.setMinNoOfStaffParentActivity(staffingLevelActivity.getMinNoOfStaff());
            staffingLevelStaffMinMax.setMaxNoOfStaffParentActivity(staffingLevelActivity.getMaxNoOfStaff());
            activityIdStaffMinMaxMap.put(staffingLevelActivity.getActivityId(), staffingLevelStaffMinMax);
        } else {
            staffingLevelStaffMinMax = activityIdStaffMinMaxMap.get(parentActivityId);
            if (staffingLevelStaffMinMax == null) {
                staffingLevelStaffMinMax = new StaffingLevelStaffMinMax(staffingLevelActivity.getMinNoOfStaff(), staffingLevelActivity.getMaxNoOfStaff());
            } else {
                staffingLevelStaffMinMax.setMinNoOfStaffChildActivities(staffingLevelStaffMinMax.getMinNoOfStaffChildActivities() + staffingLevelActivity.getMinNoOfStaff());
                staffingLevelStaffMinMax.setMaxNoOfStaffChildActivities(staffingLevelStaffMinMax.getMaxNoOfStaffChildActivities() + staffingLevelActivity.getMaxNoOfStaff());
            }
            activityIdStaffMinMaxMap.put(parentActivityId, staffingLevelStaffMinMax);
        }
        return staffingLevelStaffMinMax;

    }


    public static List<AbsenceStaffingLevelDto> buildAbsenceStaffingLevelDto(List<StaffingLevel> staffingLevels) {
        List<AbsenceStaffingLevelDto> absenceStaffingLevelDtos = new ArrayList<>();

        for (StaffingLevel staffingLevel : staffingLevels) {
            AbsenceStaffingLevelDto absenceStaffingLevelDto = new AbsenceStaffingLevelDto(staffingLevel.getId(), staffingLevel.getPhaseId(), staffingLevel.getCurrentDate(), staffingLevel.getWeekCount());
            absenceStaffingLevelDto.setMinNoOfStaff(staffingLevel.getAbsenceStaffingLevelInterval().get(0).getMinNoOfStaff());
            absenceStaffingLevelDto.setMaxNoOfStaff(staffingLevel.getAbsenceStaffingLevelInterval().get(0).getMaxNoOfStaff());
            absenceStaffingLevelDto.setAbsentNoOfStaff(staffingLevel.getAbsenceStaffingLevelInterval().get(0).getAvailableNoOfStaff());
            absenceStaffingLevelDto.setStaffingLevelActivities(staffingLevel.getAbsenceStaffingLevelInterval().get(0).getStaffingLevelActivities());
            absenceStaffingLevelDto.setUpdatedAt(staffingLevel.getUpdatedAt());
            absenceStaffingLevelDto.setStaffingLevelIntervalLogs(staffingLevel.getAbsenceStaffingLevelInterval().get(0).getStaffingLevelIntervalLogs());
            absenceStaffingLevelDtos.add(absenceStaffingLevelDto);

        }

        return absenceStaffingLevelDtos;

    }


    public static void setUserWiseLogs(StaffingLevel staffingLevel, StaffingLevelDTO staffingLevelDTO) {
        Set<ActivityRemoveLog> activityRemoveLogs = new HashSet<>();
        Set<SkillRemoveLog> skillRemoveLogs = new HashSet<>();
        Set<BigInteger> newlyAddedActivities = new HashSet<>();
        Set<Long> newlyAddedSkills = new HashSet<>();
        prepareIntervals(staffingLevel, staffingLevelDTO, activityRemoveLogs, skillRemoveLogs, newlyAddedActivities, newlyAddedSkills);
        staffingLevel.setStaffingLevelSetting(staffingLevelDTO.getStaffingLevelSetting());
        staffingLevel.setPhaseId(staffingLevelDTO.getPhaseId());
        List<StaffingLevelInterval> staffingLevelIntervals = staffingLevelDTO.getPresenceStaffingLevelInterval();
        for (int i = 0; i < staffingLevelIntervals.size(); i++) {
            StaffingLevelIntervalLog staffingLevelIntervalLog = staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelIntervalLogs().stream().filter(k -> k.getUserInfo().getId().equals(UserContext.getUserDetails().getId())).findFirst().orElse(new StaffingLevelIntervalLog());
            Map<BigInteger, StaffingLevelActivity> staffingLevelActivityMap = staffingLevelIntervals.get(i).getStaffingLevelActivities().stream().collect(toMap(StaffingLevelActivity::getActivityId, v -> v));
            StaffingLevelIntervalLog lastStaffingLevelIntervalLog = isCollectionEmpty(staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelIntervalLogs()) ? new StaffingLevelIntervalLog() : staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelIntervalLogs().last();
            Map<BigInteger, StaffingLevelActivity> staffingLevelActivityMapOfLogs = lastStaffingLevelIntervalLog.getStaffingLevelActivities().stream().collect(toMap(StaffingLevelActivity::getActivityId, v -> v));
            Set<StaffingLevelActivity> staffingLevelActivities = getStaffingLevelActivities(new HashMap<>(), staffingLevelIntervals.get(i), staffingLevelActivityMap, staffingLevelActivityMapOfLogs);
            staffingLevelIntervalLog.setStaffingLevelActivities(staffingLevelActivities);
            Map<Long, Map<SkillLevel, SkillLevelSetting>> staffingLevelSkillMapOfLogs = getMapOfStaffingLevelSkills(lastStaffingLevelIntervalLog);
            updateStaffingLevelSkills(staffingLevelDTO.getPresenceStaffingLevelInterval().get(i).getStaffingLevelSkills(), staffingLevelSkillMapOfLogs);
            staffingLevelIntervalLog.setStaffingLevelSkills(staffingLevelDTO.getPresenceStaffingLevelInterval().get(i).getStaffingLevelSkills());
            staffingLevelIntervalLog.setMinNoOfStaff(staffingLevelIntervalLog.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMinNoOfStaff())));
            staffingLevelIntervalLog.setMaxNoOfStaff(staffingLevelIntervalLog.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMaxNoOfStaff())));
            staffingLevelIntervalLog.setUserInfo(new UserInfo(UserContext.getUserDetails().getId(), UserContext.getUserDetails().getEmail(), UserContext.getUserDetails().getFullName(), UserContext.getUserDetails().isManagement() ? AccessGroupRole.MANAGEMENT : AccessGroupRole.STAFF));
            staffingLevelIntervalLog.setUpdatedAt(getDate());
            staffingLevelIntervalLog.setActivityRemoveLogs(activityRemoveLogs);
            staffingLevelIntervalLog.setSkillRemoveLogs(skillRemoveLogs);
            staffingLevelIntervalLog.setNewlyAddedActivityIds(newlyAddedActivities);
            staffingLevelIntervalLog.setNewlyAddedSkillIds(newlyAddedSkills);
            staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelIntervalLogs().add(staffingLevelIntervalLog);
        }
    }

    private static Map<Long, Map<SkillLevel, SkillLevelSetting>> getMapOfStaffingLevelSkills(StaffingLevelIntervalLog lastStaffingLevelIntervalLog) {
        Map<Long, Map<SkillLevel, SkillLevelSetting>> staffingLevelSkillMapOfLogs = new HashMap<>();
        for (StaffingLevelSkill staffingLevelSkill : lastStaffingLevelIntervalLog.getStaffingLevelSkills()) {
            staffingLevelSkillMapOfLogs.put(staffingLevelSkill.getSkillId(), staffingLevelSkill.getSkillLevelSettings().stream().collect(toMap(SkillLevelSetting::getSkillLevel, v -> v)));
        }
        return staffingLevelSkillMapOfLogs;
    }

    private static void prepareIntervals(StaffingLevel staffingLevel, StaffingLevelDTO staffingLevelDTO, Set<ActivityRemoveLog> activityRemoveLogs, Set<SkillRemoveLog> skillRemoveLogs, Set<BigInteger> newlyAddedActivities, Set<Long> newlyAddedSkills) {
        List<StaffingLevelInterval> staffingLevelIntervals = new ArrayList<>();
        if (REMOVE.equals(staffingLevelDTO.getStaffingLevelAction())) {
            removeDataFromStaffingLevel(staffingLevelDTO, staffingLevel, activityRemoveLogs, skillRemoveLogs, staffingLevelIntervals, newlyAddedActivities, newlyAddedSkills);
        } else if (UPDATE.equals(staffingLevelDTO.getStaffingLevelAction())) {
            addOrUpdateDataInStaffingLevel(staffingLevelDTO, staffingLevel, newlyAddedActivities, newlyAddedSkills, staffingLevelIntervals, activityRemoveLogs, skillRemoveLogs);
        } else {
            for (int i = 0; i < 96; i++) {
                StaffingLevelIntervalLog staffingLevelIntervalLog = isCollectionEmpty(staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelIntervalLogs()) ? null : staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelIntervalLogs().last();
                StaffingLevelInterval interval = ObjectMapperUtils.copyPropertiesByMapper(isNull(staffingLevelIntervalLog) ? staffingLevel.getPresenceStaffingLevelInterval().get(i) : staffingLevelIntervalLog, StaffingLevelInterval.class);
                if (staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getSequence() == i) {
                    staffingLevelIntervals.add(staffingLevelDTO.getPresenceStaffingLevelInterval().get(0));
                } else {
                    getActivityRemoveLogs(activityRemoveLogs, interval, staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelActivities());
                    if (isNotNull(staffingLevelIntervalLog)) {
                        newlyAddedActivities.addAll(staffingLevelIntervalLog.getNewlyAddedActivityIds());
                        activityRemoveLogs.addAll(staffingLevelIntervalLog.getActivityRemoveLogs());
                    }
                    getSkillRemoveLogs(staffingLevelDTO, skillRemoveLogs, interval);
                    if (isNotNull(staffingLevelIntervalLog)) {
                        newlyAddedSkills.addAll(staffingLevelIntervalLog.getNewlyAddedSkillIds());
                        skillRemoveLogs.addAll(staffingLevelIntervalLog.getSkillRemoveLogs());
                    }

                    staffingLevelIntervals.add(interval);
                }
                updateAddedSkillAndActivity(staffingLevel, staffingLevelDTO, i, activityRemoveLogs, skillRemoveLogs, interval, newlyAddedActivities, newlyAddedSkills);
            }
        }
        staffingLevelDTO.setPresenceStaffingLevelInterval(staffingLevelIntervals);
    }
    private static void getActivityRemoveLogs(Set<ActivityRemoveLog> activityRemoveLogs, StaffingLevelInterval interval, Set<StaffingLevelActivity> staffingLevelActivities) {
        if (staffingLevelActivities.size() < interval.getStaffingLevelActivities().size()) {
            for (Iterator<StaffingLevelActivity> iterator = interval.getStaffingLevelActivities().iterator(); iterator.hasNext(); ) {
                StaffingLevelActivity staffingLevelActivity = iterator.next();
                if (!staffingLevelActivities.contains(staffingLevelActivity)) {
                    iterator.remove();
                    activityRemoveLogs.add(new ActivityRemoveLog(staffingLevelActivity.getActivityId(), new Date(), UserContext.getUserDetails().getFirstName()));
                }
            }
        }
    }
    private static void getSkillRemoveLogs(StaffingLevelDTO staffingLevelDTO, Set<SkillRemoveLog> skillRemoveLogs, StaffingLevelInterval interval) {
        if (staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelSkills().size() < interval.getStaffingLevelSkills().size()) {
            for (Iterator<StaffingLevelSkill> iterator = interval.getStaffingLevelSkills().iterator(); iterator.hasNext(); ) {
                StaffingLevelSkill staffingLevelSkill = iterator.next();
                if (!staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelSkills().contains(staffingLevelSkill)) {
                    iterator.remove();
                    skillRemoveLogs.add(new SkillRemoveLog(staffingLevelSkill.getSkillId(), new Date(), UserContext.getUserDetails().getFirstName()));
                }
            }
        }
    }

    private static void addOrUpdateDataInStaffingLevel(StaffingLevelDTO staffingLevelDTO, StaffingLevel staffingLevel, Set<BigInteger> newlyAddedActivities, Set<Long> newlyAddedSkills, List<StaffingLevelInterval> staffingLevelIntervals, Set<ActivityRemoveLog> activityRemoveLogs, Set<SkillRemoveLog> skillRemoveLogs) {
        ZonedDateTime currentDate = DateUtils.asZonedDateTime(staffingLevel.getCurrentDate());
        StaffingLevelActivity staffingLevelActivityToAddOrUpdate = isCollectionNotEmpty(staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelActivities()) ? staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelActivities().iterator().next() : null;
        StaffingLevelSkill staffingLevelSkillToAddOrUpdate = isCollectionNotEmpty(staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelSkills()) ? staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelSkills().iterator().next() : null;
        for (int i = 0; i < 96; i++) {
            StaffingLevelIntervalLog staffingLevelIntervalLog = isCollectionEmpty(staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelIntervalLogs()) ? null : staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelIntervalLogs().last();
            StaffingLevelInterval interval = ObjectMapperUtils.copyPropertiesByMapper(isNull(staffingLevelIntervalLog) ? staffingLevel.getPresenceStaffingLevelInterval().get(i) : staffingLevelIntervalLog, StaffingLevelInterval.class);
            if (staffingLevelActivityToAddOrUpdate != null) {
                if (!staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelActivities().contains(staffingLevelActivityToAddOrUpdate)) {
                    staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelActivities().add(new StaffingLevelActivity(staffingLevelActivityToAddOrUpdate.getActivityId(), 0, 0));
                    newlyAddedActivities.add(staffingLevelActivityToAddOrUpdate.getActivityId());
                    activityRemoveLogs.removeIf(k -> k.getActivityId().equals(staffingLevelActivityToAddOrUpdate.getActivityId()));
                }
                interval.getStaffingLevelActivities().add(new StaffingLevelActivity(staffingLevelActivityToAddOrUpdate.getActivityId(), 0, 0));
            }
            if (isNotNull(staffingLevelIntervalLog)) {
                newlyAddedActivities.addAll(staffingLevelIntervalLog.getNewlyAddedActivityIds());
                activityRemoveLogs.addAll(staffingLevelIntervalLog.getActivityRemoveLogs());
            }

            if (staffingLevelSkillToAddOrUpdate != null) {
                if (!staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelSkills().contains(staffingLevelSkillToAddOrUpdate)) {
                    staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelSkills().add(new StaffingLevelSkill(staffingLevelSkillToAddOrUpdate.getSkillId()));
                    newlyAddedSkills.add(staffingLevelSkillToAddOrUpdate.getSkillId());
                    skillRemoveLogs.removeIf(k -> k.getSkillId().equals(staffingLevelSkillToAddOrUpdate.getSkillId()));
                }
                interval.getStaffingLevelSkills().add(new StaffingLevelSkill(staffingLevelSkillToAddOrUpdate.getSkillId()));
            }
            if (isNotNull(staffingLevelIntervalLog)) {
                newlyAddedSkills.addAll(staffingLevelIntervalLog.getNewlyAddedSkillIds());
                skillRemoveLogs.addAll(staffingLevelIntervalLog.getSkillRemoveLogs());
            }
            if (staffingLevelDTO.getInterval().contains(currentDate)) {
                staffingLevelIntervals.add(updateInterval(interval, staffingLevelDTO));
            } else {
                staffingLevelIntervals.add(interval);
            }
            currentDate = currentDate.plusMinutes(15);
        }
    }

    private static void updateAddedSkillAndActivity(StaffingLevel staffingLevel, StaffingLevelDTO staffingLevelDTO, int i, Set<ActivityRemoveLog> activityRemoveLogs, Set<SkillRemoveLog> skillRemoveLogs, StaffingLevelInterval interval, Set<BigInteger> newlyAddedActivities, Set<Long> newlyAddedSkills) {
        if (staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelActivities().size() > interval.getStaffingLevelActivities().size()) {
            Map<BigInteger, StaffingLevelActivity> staffingLevelActivityMap = staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelActivities().stream().collect(toMap(StaffingLevelActivity::getActivityId, v -> v));
            for (StaffingLevelActivity staffingLevelActivity : staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelActivities()) {
                if (!staffingLevelActivityMap.containsKey(staffingLevelActivity.getActivityId())) {
                    staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelActivities().add(staffingLevelActivity);
                    newlyAddedActivities.add(staffingLevelActivity.getActivityId());
                }
                activityRemoveLogs.removeIf(k -> k.getActivityId().equals(staffingLevelActivity.getActivityId()));
                interval.getStaffingLevelActivities().add(staffingLevelActivity);
            }
        }
        if (staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelSkills().size() > interval.getStaffingLevelSkills().size()) {
            Map<Long, StaffingLevelSkill> staffingLevelSkillMap = staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelSkills().stream().collect(toMap(StaffingLevelSkill::getSkillId, v -> v));
            for (StaffingLevelSkill staffingLevelSkill : staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelSkills()) {
                if (!staffingLevelSkillMap.containsKey(staffingLevelSkill.getSkillId())) {
                    staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelSkills().add(staffingLevelSkill);
                    newlyAddedSkills.add(staffingLevelSkill.getSkillId());
                }
                skillRemoveLogs.removeIf(k -> k.getSkillId().equals(staffingLevelSkill.getSkillId()));
                interval.getStaffingLevelSkills().add(staffingLevelSkill);
            }
        }
        interval.setMinNoOfStaff(interval.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMinNoOfStaff())));
        interval.setMaxNoOfStaff(interval.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMaxNoOfStaff())));
    }

    public static void setUserWiseLogsInAbsence(StaffingLevel staffingLevel, AbsenceStaffingLevelDto absenceStaffingLevelDto) {
        Set<ActivityRemoveLog> activityRemoveLogs = new HashSet<>();
        Set<BigInteger> newlyAddedActivities = new HashSet<>();
        prepareAbsenceInterval(staffingLevel, absenceStaffingLevelDto, activityRemoveLogs, newlyAddedActivities);
        staffingLevel.setPhaseId(absenceStaffingLevelDto.getPhaseId());
        staffingLevel.setWeekCount(absenceStaffingLevelDto.getWeekCount());
        StaffingLevelIntervalLog staffingLevelIntervalLog = staffingLevel.getAbsenceStaffingLevelInterval().get(0).getStaffingLevelIntervalLogs().stream().filter(k -> k.getUserInfo().getId().equals(UserContext.getUserDetails().getId())).findFirst().orElse(new StaffingLevelIntervalLog());
        Map<BigInteger, StaffingLevelActivity> staffingLevelActivityMapOfLogs = staffingLevelIntervalLog.getStaffingLevelActivities().stream().collect(toMap(StaffingLevelActivity::getActivityId, v -> v));
        Set<StaffingLevelActivity> staffingLevelActivities = getActivities(staffingLevelActivityMapOfLogs, absenceStaffingLevelDto.getStaffingLevelActivities());
        staffingLevelIntervalLog.setStaffingLevelActivities(staffingLevelActivities);
        staffingLevelIntervalLog.setMinNoOfStaff(staffingLevelIntervalLog.getStaffingLevelActivities().stream().mapToInt(StaffingLevelActivity::getMinNoOfStaff).sum());
        staffingLevelIntervalLog.setMaxNoOfStaff(staffingLevelIntervalLog.getStaffingLevelActivities().stream().mapToInt(StaffingLevelActivity::getMaxNoOfStaff).sum());
        staffingLevelIntervalLog.setUserInfo(new UserInfo(UserContext.getUserDetails().getId(), UserContext.getUserDetails().getEmail(), UserContext.getUserDetails().getFullName(), UserContext.getUserDetails().isManagement() ? AccessGroupRole.MANAGEMENT : AccessGroupRole.STAFF));
        staffingLevelIntervalLog.setActivityRemoveLogs(activityRemoveLogs);
        staffingLevelIntervalLog.setNewlyAddedActivityIds(newlyAddedActivities);
        staffingLevelIntervalLog.setUpdatedAt(getDate());
        staffingLevel.getAbsenceStaffingLevelInterval().get(0).getStaffingLevelIntervalLogs().add(staffingLevelIntervalLog);
    }

    private static Set<StaffingLevelActivity> getActivities(Map<BigInteger, StaffingLevelActivity> staffingLevelActivityMapOfLogs, Set<StaffingLevelActivity> staffingLevelActivities) {
        for (StaffingLevelActivity staffingLevelActivity : staffingLevelActivities) {
            if (isEmpty(staffingLevelActivityMapOfLogs) || !staffingLevelActivityMapOfLogs.containsKey(staffingLevelActivity.getActivityId()) || staffingLevelActivityMapOfLogs.get(staffingLevelActivity.getActivityId()).getMaxNoOfStaff() != staffingLevelActivity.getMaxNoOfStaff()) {
                staffingLevelActivity.setMaxUpdatedAt(getDate());
            } else {
                staffingLevelActivity.setMaxUpdatedAt(staffingLevelActivityMapOfLogs.get(staffingLevelActivity.getActivityId()).getMaxUpdatedAt());
            }
            if (isEmpty(staffingLevelActivityMapOfLogs) || !staffingLevelActivityMapOfLogs.containsKey(staffingLevelActivity.getActivityId()) || staffingLevelActivityMapOfLogs.get(staffingLevelActivity.getActivityId()).getMinNoOfStaff() != staffingLevelActivity.getMinNoOfStaff()) {
                staffingLevelActivity.setMinUpdatedAt(getDate());
            } else {
                staffingLevelActivity.setMinUpdatedAt(staffingLevelActivityMapOfLogs.get(staffingLevelActivity.getActivityId()).getMinUpdatedAt());
            }
        }
        return staffingLevelActivities;
    }

    public static void updateStaffingLevelToPublish(StaffingLevelPublishDTO staffingLevelPublishDTO, StaffingLevel staffingLevel) {
        List<StaffingLevelInterval> staffingLevelIntervals = isNotNull(staffingLevelPublishDTO.getInterval()) ? getFilteredIntervals(staffingLevelPublishDTO, staffingLevel) : staffingLevel.getPresenceStaffingLevelInterval();
        Set<StaffingLevelActivityDetails> staffingLevelActivityDetails = new HashSet<>();
        for (StaffingLevelInterval staffingLevelInterval : staffingLevelIntervals) {
            updateActivities(staffingLevelPublishDTO, staffingLevelInterval,staffingLevelActivityDetails);
        }
        for(StaffingLevelInterval absenceStaffingLevelInterval:staffingLevel.getAbsenceStaffingLevelInterval()){
            updateActivities(staffingLevelPublishDTO, absenceStaffingLevelInterval,staffingLevelActivityDetails);
        }
        staffingLevel.setStaffingLevelActivityDetails(staffingLevelActivityDetails);
    }

    private static List<StaffingLevelInterval> getFilteredIntervals(StaffingLevelPublishDTO staffingLevelPublishDTO, StaffingLevel staffingLevel) {
        List<StaffingLevelInterval> staffingLevelIntervals = new ArrayList<>();
        ZonedDateTime currentDate = DateUtils.asZonedDateTime(staffingLevel.getCurrentDate());
        for (StaffingLevelInterval staffingLevelInterval : staffingLevel.getPresenceStaffingLevelInterval()) {
            if (staffingLevelPublishDTO.getInterval().contains(currentDate)) {
                staffingLevelIntervals.add(staffingLevelInterval);
            }
            currentDate = currentDate.plusMinutes(15);
        }
        return staffingLevelIntervals;
    }

    private static void updateActivities(StaffingLevelPublishDTO staffingLevelPublishDTO, StaffingLevelInterval staffingLevelInterval, Set<StaffingLevelActivityDetails> staffingLevelActivityDetails) {
        if(isCollectionNotEmpty(staffingLevelInterval.getStaffingLevelIntervalLogs())){
            StaffingLevelIntervalLog staffingLevelIntervalLog = staffingLevelInterval.getStaffingLevelIntervalLogs().last();
            Set<StaffingLevelActivity> staffingLevelActivities = getStaffingLevelActivitiesToPublish(staffingLevelPublishDTO, staffingLevelIntervalLog);
            staffingLevelPublishDTO.setActivityIds(staffingLevelActivities.stream().map(StaffingLevelActivity::getActivityId).collect(Collectors.toSet()));

            Map<BigInteger, StaffingLevelActivity> staffingLevelActivityMap = staffingLevelInterval.getStaffingLevelActivities().stream().collect(toMap(k -> k.getActivityId(), v -> v));
            staffingLevelActivities.forEach(staffingLevelActivity -> {
                StaffingLevelActivity levelActivity = null;
                if (staffingLevelActivityMap.containsKey(staffingLevelActivity.getActivityId())) {
                    levelActivity = staffingLevelActivityMap.get(staffingLevelActivity.getActivityId());
                    levelActivity.getAvailableNoOfStaff();
                    staffingLevelInterval.getStaffingLevelActivities().remove(levelActivity);
                }
                if(isNotNull(levelActivity)){
                    staffingLevelActivity.setAvailableNoOfStaff(levelActivity.getAvailableNoOfStaff());
                    resetStaffingLevelActivityDetails(levelActivity,staffingLevelActivity);
                    staffingLevelActivityDetails.add(getStaffingLevelActivityDetails(staffingLevelActivityDetails,staffingLevelActivity));
                }
                staffingLevelInterval.getStaffingLevelActivities().add(staffingLevelActivity);
            });
            staffingLevelInterval.setMaxNoOfStaff(staffingLevelInterval.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMaxNoOfStaff())));
            staffingLevelInterval.setMinNoOfStaff(staffingLevelInterval.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMinNoOfStaff())));
            Set<StaffingLevelSkill> staffingLevelSkills = getStaffingLevelSkillsToPublish(staffingLevelPublishDTO,staffingLevelIntervalLog);
            Map<Long, StaffingLevelSkill> staffingLevelSkillLogMap = staffingLevelInterval.getStaffingLevelSkills().stream().collect(toMap(k -> k.getSkillId(), v -> v));
            staffingLevelSkills.forEach(staffingLevelSkill -> {
                if (staffingLevelSkillLogMap.containsKey(staffingLevelSkill.getSkillId())) {
                    staffingLevelInterval.getStaffingLevelSkills().remove(staffingLevelSkillLogMap.get(staffingLevelSkill.getSkillId()));
                }
                Optional<StaffingLevelSkill> optionalStaffingLevelSkill = staffingLevelInterval.getStaffingLevelSkills().stream().filter(staffingLevelSkill1 -> staffingLevelSkill1.getSkillId().equals(staffingLevelSkill.getSkillId())).findFirst();
                if(optionalStaffingLevelSkill.isPresent()){
                    for (SkillLevelSetting skillLevelSetting : optionalStaffingLevelSkill.get().getSkillLevelSettings()) {
                        Optional<SkillLevelSetting> skillLevelSettingOptional = staffingLevelSkill.getSkillLevelSettings().stream().filter(skillLevelSetting1 -> skillLevelSetting1.getSkillLevel().equals(skillLevelSetting.getSkillLevel())).findFirst();
                        if(skillLevelSettingOptional.isPresent()){
                            skillLevelSettingOptional.get().setAvailableNoOfStaff(skillLevelSetting.getAvailableNoOfStaff());
                        }
                    }
                }
                staffingLevelInterval.getStaffingLevelSkills().add(staffingLevelSkill);
            });
            staffingLevelPublishDTO.setSkillIds(staffingLevelSkills.stream().map(StaffingLevelSkill::getSkillId).collect(Collectors.toSet()));
            removeActivityAndSkills(staffingLevelInterval, staffingLevelIntervalLog, staffingLevelPublishDTO);
            resetIntervalLogs(staffingLevelInterval, staffingLevelActivities, staffingLevelSkills);
        }
    }
    private static void resetStaffingLevelActivityDetails(StaffingLevelActivity oldStaffingLevelActivity, StaffingLevelActivity staffingLevelActivity) {
        int minNoOfStaffDifference = Math.max(oldStaffingLevelActivity.getMinNoOfStaff() - staffingLevelActivity.getMinNoOfStaff(),0);
        int maxNoOfStaffDifference = Math.max(oldStaffingLevelActivity.getMaxNoOfStaff() - staffingLevelActivity.getMaxNoOfStaff(),0);
        if(minNoOfStaffDifference>0){
            oldStaffingLevelActivity.setInitialUnderStaffing(Math.max(staffingLevelActivity.getInitialUnderStaffing()-minNoOfStaffDifference,0));
            oldStaffingLevelActivity.setRemainingUnderStaffing(Math.max(staffingLevelActivity.getRemainingUnderStaffing()-minNoOfStaffDifference,0));
        }
        if(maxNoOfStaffDifference>0){
            oldStaffingLevelActivity.setInitialOverStaffing(Math.max(staffingLevelActivity.getInitialOverStaffing() - maxNoOfStaffDifference,0));
            oldStaffingLevelActivity.setRemainingOverStaffing(Math.max(staffingLevelActivity.getRemainingOverStaffing() - maxNoOfStaffDifference,0));
        }
        staffingLevelActivity.setInitialUnderStaffing(oldStaffingLevelActivity.getInitialUnderStaffing());
        staffingLevelActivity.setRemainingUnderStaffing(oldStaffingLevelActivity.getRemainingUnderStaffing());
        staffingLevelActivity.setSolvedUnderStaffing(oldStaffingLevelActivity.getSolvedUnderStaffing());
        staffingLevelActivity.setInitialOverStaffing(oldStaffingLevelActivity.getInitialOverStaffing());
        staffingLevelActivity.setRemainingOverStaffing(oldStaffingLevelActivity.getRemainingOverStaffing());
        staffingLevelActivity.setSolvedOverStaffing(oldStaffingLevelActivity.getSolvedOverStaffing());
    }

    private static Set<StaffingLevelActivity> getStaffingLevelActivitiesToPublish(StaffingLevelPublishDTO staffingLevelPublishDTO, StaffingLevelIntervalLog staffingLevelIntervalLog) {
        Set<StaffingLevelActivity> staffingLevelActivities;
        if (isCollectionEmpty(staffingLevelPublishDTO.getActivityIds()) && isCollectionEmpty(staffingLevelPublishDTO.getSkillIds())) {
            staffingLevelActivities = staffingLevelIntervalLog.getStaffingLevelActivities();
            staffingLevelIntervalLog.getActivityRemoveLogs().forEach(removeLog -> {
                staffingLevelActivities.add(new StaffingLevelActivity(removeLog.getActivityId(), 0, 0));
            });
        } else {
            staffingLevelActivities = staffingLevelIntervalLog.getStaffingLevelActivities().stream().filter(k -> staffingLevelPublishDTO.getActivityIds().contains(k.getActivityId())).collect(Collectors.toSet());
            staffingLevelIntervalLog.getActivityRemoveLogs().forEach(removeLog -> {
                if (staffingLevelPublishDTO.getActivityIds().contains(removeLog.getActivityId())) {
                    staffingLevelActivities.add(new StaffingLevelActivity(removeLog.getActivityId(), 0, 0));
                }
            });
        }
        return staffingLevelActivities;
    }

    private static Set<StaffingLevelSkill> getStaffingLevelSkillsToPublish(StaffingLevelPublishDTO staffingLevelPublishDTO, StaffingLevelIntervalLog staffingLevelIntervalLog) {
        Set<StaffingLevelSkill> staffingLevelSkills;
        if (isCollectionEmpty(staffingLevelPublishDTO.getActivityIds()) && isCollectionEmpty(staffingLevelPublishDTO.getSkillIds())) {
            staffingLevelSkills = staffingLevelIntervalLog.getStaffingLevelSkills();
            staffingLevelIntervalLog.getSkillRemoveLogs().forEach(removeLog -> {
                staffingLevelSkills.add(new StaffingLevelSkill(removeLog.getSkillId()));
            });
        } else {
            staffingLevelSkills = staffingLevelIntervalLog.getStaffingLevelSkills().stream().filter(k -> staffingLevelPublishDTO.getSkillIds().contains(k.getSkillId())).collect(Collectors.toSet());
            staffingLevelIntervalLog.getSkillRemoveLogs().forEach(removeLog -> {
                if (staffingLevelPublishDTO.getSkillIds().contains(removeLog.getSkillId())) {
                    staffingLevelSkills.add(new StaffingLevelSkill(removeLog.getSkillId()));
                }
            });
        }
        return staffingLevelSkills;
    }

    private static void resetIntervalLogs(StaffingLevelInterval staffingLevelInterval, Set<StaffingLevelActivity> staffingLevelActivities, Set<StaffingLevelSkill> staffingLevelSkills) {
        if (staffingLevelInterval.getStaffingLevelActivities().size() == staffingLevelActivities.size() && staffingLevelInterval.getStaffingLevelSkills().size() == staffingLevelSkills.size()) {
            staffingLevelInterval.setStaffingLevelIntervalLogs(new TreeSet<>());
        } else {
            Map<BigInteger, StaffingLevelActivity> staffingLevelActivityMap = staffingLevelInterval.getStaffingLevelActivities().stream().collect(Collectors.toMap(StaffingLevelActivity::getActivityId, Function.identity()));
            Map<Long, StaffingLevelSkill> staffingLevelSkillMap = staffingLevelInterval.getStaffingLevelSkills().stream().collect(Collectors.toMap(StaffingLevelSkill::getSkillId, Function.identity()));
            List<StaffingLevelIntervalLog> staffingLevelIntervalLogs=new CopyOnWriteArrayList<>(staffingLevelInterval.getStaffingLevelIntervalLogs());
            for (StaffingLevelIntervalLog staffingLevelIntervalLog : staffingLevelIntervalLogs) {
                List<StaffingLevelActivity> staffingLevelActivityIterator = new CopyOnWriteArrayList<>(staffingLevelActivities);
                List<StaffingLevelSkill> staffingLevelSkillIterator = new CopyOnWriteArrayList<>(staffingLevelSkills);
                for (StaffingLevelActivity staffingLevelActivity : staffingLevelActivityIterator) {
                    staffingLevelIntervalLog.getStaffingLevelActivities().remove(staffingLevelActivity);
                    if(staffingLevelActivityMap.containsKey(staffingLevelActivity.getActivityId())) {
                        staffingLevelIntervalLog.getStaffingLevelActivities().add(staffingLevelActivityMap.get(staffingLevelActivity.getActivityId()));
                    }
                }
                for (StaffingLevelSkill staffingLevelSkill : staffingLevelSkillIterator) {
                    staffingLevelIntervalLog.getStaffingLevelSkills().remove(staffingLevelSkill);
                    if(staffingLevelSkillMap.containsKey(staffingLevelSkill.getSkillId())) {
                        staffingLevelIntervalLog.getStaffingLevelSkills().add(staffingLevelSkillMap.get(staffingLevelSkill.getSkillId()));
                    }
                }
            }
        }
        staffingLevelInterval.setMinNoOfStaff(staffingLevelInterval.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMinNoOfStaff())));
        staffingLevelInterval.setMaxNoOfStaff(staffingLevelInterval.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMaxNoOfStaff())));
    }

    private static void removeActivityAndSkills(StaffingLevelInterval staffingLevelInterval, StaffingLevelIntervalLog staffingLevelIntervalLog, StaffingLevelPublishDTO staffingLevelPublishDTO) {
        Set<BigInteger> activityIdsToRemove = staffingLevelInterval.getStaffingLevelActivities().stream().map(StaffingLevelActivity::getActivityId).collect(Collectors.toSet());
        Map<BigInteger, StaffingLevelActivity> staffingLevelActivityLogMap = staffingLevelIntervalLog.getStaffingLevelActivities().stream().collect(toMap(k -> k.getActivityId(), v -> v));
        for (BigInteger activityId : activityIdsToRemove) {
            if (!staffingLevelActivityLogMap.containsKey(activityId) && staffingLevelPublishDTO.getActivityIds().contains(activityId)) {
                staffingLevelInterval.getStaffingLevelActivities().removeIf(k -> k.getActivityId().equals(activityId));
                staffingLevelIntervalLog.getActivityRemoveLogs().removeIf(k -> k.getActivityId().equals(activityId));
            }
        }
        Set<Long> skillIdsToRemove = staffingLevelInterval.getStaffingLevelSkills().stream().map(StaffingLevelSkill::getSkillId).collect(Collectors.toSet());
        Map<Long, StaffingLevelSkill> staffingLevelSkillLogMap = staffingLevelIntervalLog.getStaffingLevelSkills().stream().collect(toMap(k -> k.getSkillId(), v -> v));
        for (Long skillId : skillIdsToRemove) {
            if (!staffingLevelSkillLogMap.containsKey(skillId) && staffingLevelPublishDTO.getSkillIds().contains(skillId)) {
                staffingLevelInterval.getStaffingLevelSkills().removeIf(k -> k.getSkillId().equals(skillId));
                staffingLevelIntervalLog.getSkillRemoveLogs().removeIf(k -> k.getSkillId().equals(skillId));
            }
        }
        staffingLevelIntervalLog.getNewlyAddedActivityIds().removeIf(k -> staffingLevelPublishDTO.getActivityIds().contains(k));
        staffingLevelIntervalLog.getNewlyAddedSkillIds().removeIf(k -> staffingLevelPublishDTO.getSkillIds().contains(k));
    }

    public static void initializeUserWiseLogs(StaffingLevelInterval staffingLevelInterval) {
        StaffingLevelIntervalLog staffingLevelIntervalLog = new StaffingLevelIntervalLog(new UserInfo(UserContext.getUserDetails().getId(), UserContext.getUserDetails().getEmail(), UserContext.getUserDetails().getFullName(), UserContext.getUserDetails().isManagement() ? AccessGroupRole.MANAGEMENT : AccessGroupRole.STAFF), new Date(), staffingLevelInterval.getStaffingLevelActivities(), staffingLevelInterval.getStaffingLevelSkills());
        staffingLevelIntervalLog.setMinNoOfStaff(staffingLevelIntervalLog.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMinNoOfStaff())));
        staffingLevelIntervalLog.setMaxNoOfStaff(staffingLevelIntervalLog.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMaxNoOfStaff())));
        staffingLevelInterval.getStaffingLevelIntervalLogs().add(staffingLevelIntervalLog);
    }

    private static void updateStaffingLevelSkills(Set<StaffingLevelSkill> staffingLevelSkills, Map<Long, Map<SkillLevel, SkillLevelSetting>> staffingLevelSkillMapOfLogs) {
        for (StaffingLevelSkill staffingLevelSkill : staffingLevelSkills) {
            Map<SkillLevel, Integer> skillLevelIntegerMap = staffingLevelSkill.getSkillLevelSettings().stream().collect(toMap(k -> k.getSkillLevel(), v -> v.getNoOfStaff()));
            for (SkillLevelSetting skillLevelSetting : staffingLevelSkill.getSkillLevelSettings()) {
                if (isEmpty(staffingLevelSkillMapOfLogs) || !staffingLevelSkillMapOfLogs.containsKey(staffingLevelSkill.getSkillId()) || !skillLevelIntegerMap.get(skillLevelSetting.getSkillLevel()).equals(staffingLevelSkillMapOfLogs.get(staffingLevelSkill.getSkillId()).get(skillLevelSetting.getSkillLevel()).getNoOfStaff())) {
                    skillLevelSetting.setUpdatedAt(getDate());
                } else {
                    skillLevelSetting.setUpdatedAt(staffingLevelSkillMapOfLogs.get(staffingLevelSkill.getSkillId()).get(skillLevelSetting.getSkillLevel()).getUpdatedAt());
                }
            }
        }
    }

    private static void prepareAbsenceInterval(StaffingLevel staffingLevel, AbsenceStaffingLevelDto absenceStaffingLevelDto, Set<ActivityRemoveLog> activityRemoveLogs, Set<BigInteger> newlyAddedActivities) {
        StaffingLevelIntervalLog staffingLevelIntervalLog = isCollectionEmpty(staffingLevel.getAbsenceStaffingLevelInterval()) || isCollectionEmpty(staffingLevel.getAbsenceStaffingLevelInterval().get(0).getStaffingLevelIntervalLogs()) ? null : staffingLevel.getAbsenceStaffingLevelInterval().get(0).getStaffingLevelIntervalLogs().last();
        StaffingLevelInterval interval = isCollectionEmpty(staffingLevel.getAbsenceStaffingLevelInterval()) ? new StaffingLevelInterval(0, 0,
                0, new Duration(LocalTime.MIN, LocalTime.MAX)) : ObjectMapperUtils.copyPropertiesByMapper(isNull(staffingLevelIntervalLog) ? staffingLevel.getAbsenceStaffingLevelInterval().get(0) : staffingLevelIntervalLog, StaffingLevelInterval.class);
        getActivityRemoveLogs(activityRemoveLogs, interval, absenceStaffingLevelDto.getStaffingLevelActivities());
        if (isNotNull(staffingLevelIntervalLog)) {
            newlyAddedActivities.addAll(staffingLevelIntervalLog.getNewlyAddedActivityIds());
            activityRemoveLogs.addAll(staffingLevelIntervalLog.getActivityRemoveLogs());
        }
        updateAddedActivities(staffingLevel, absenceStaffingLevelDto, activityRemoveLogs, interval, newlyAddedActivities);
    }

    private static void updateAddedActivities(StaffingLevel staffingLevel, AbsenceStaffingLevelDto absenceStaffingLevelDto, Set<ActivityRemoveLog> activityRemoveLogs, StaffingLevelInterval interval, Set<BigInteger> newlyAddedActivities) {
        if (absenceStaffingLevelDto.getStaffingLevelActivities().size() > interval.getStaffingLevelActivities().size()) {
            Map<BigInteger, StaffingLevelActivity> staffingLevelActivityMap = isCollectionEmpty(staffingLevel.getAbsenceStaffingLevelInterval()) ? new HashMap<>() : staffingLevel.getAbsenceStaffingLevelInterval().get(0).getStaffingLevelActivities().stream().collect(toMap(StaffingLevelActivity::getActivityId, v -> v));
            for (StaffingLevelActivity staffingLevelActivity : absenceStaffingLevelDto.getStaffingLevelActivities()) {
                if (!staffingLevelActivityMap.containsKey(staffingLevelActivity.getActivityId())) {
                    StaffingLevelActivity newActivity = new StaffingLevelActivity(staffingLevelActivity.getActivityId(), staffingLevelActivity.getName(), 0, 0);
                    if (isCollectionEmpty(staffingLevel.getAbsenceStaffingLevelInterval())) {
                        staffingLevel.getAbsenceStaffingLevelInterval().add(interval);
                        staffingLevel.getAbsenceStaffingLevelInterval().get(0).getStaffingLevelActivities().add(newActivity);
                    } else {
                        staffingLevel.getAbsenceStaffingLevelInterval().get(0).getStaffingLevelActivities().add(newActivity);
                    }
                    newlyAddedActivities.add(staffingLevelActivity.getActivityId());
                }
                activityRemoveLogs.removeIf(k -> k.getActivityId().equals(staffingLevelActivity.getActivityId()));
                interval.getStaffingLevelActivities().add(staffingLevelActivity);
            }
        }
        interval.setMinNoOfStaff(interval.getStaffingLevelActivities().stream().mapToInt(StaffingLevelActivity::getMinNoOfStaff).sum());
        interval.setMaxNoOfStaff(interval.getStaffingLevelActivities().stream().mapToInt(StaffingLevelActivity::getMaxNoOfStaff).sum());
    }

    private static void removeDataFromStaffingLevel(StaffingLevelDTO staffingLevelDTO, StaffingLevel staffingLevel, Set<ActivityRemoveLog> activityRemoveLogs, Set<SkillRemoveLog> skillRemoveLogs, List<StaffingLevelInterval> staffingLevelIntervals, Set<BigInteger> newlyAddedActivities, Set<Long> newlyAddedSkills) {
        for (int i = 0; i < 96; i++) {
            BigInteger activityId = null;
            StaffingLevelIntervalLog staffingLevelIntervalLog = isCollectionEmpty(staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelIntervalLogs()) ? null : staffingLevel.getPresenceStaffingLevelInterval().get(i).getStaffingLevelIntervalLogs().last();
            StaffingLevelInterval interval = ObjectMapperUtils.copyPropertiesByMapper(isNull(staffingLevelIntervalLog) ? staffingLevel.getPresenceStaffingLevelInterval().get(i) : staffingLevelIntervalLog, StaffingLevelInterval.class);
            if (isCollectionNotEmpty(staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelActivities())) {
                activityId = staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelActivities().iterator().next().getActivityId();
                interval.getStaffingLevelActivities().remove(staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelActivities().iterator().next());
                activityRemoveLogs.add(new ActivityRemoveLog(activityId, new Date(), UserContext.getUserDetails().getFirstName()));
            } else {
                Set<Long> skillIds  = staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelSkills().stream().map(StaffingLevelSkill::getSkillId).collect(Collectors.toSet());
                interval.getStaffingLevelSkills().removeIf(s-> skillIds.contains(s.getSkillId()));
                skillIds.forEach(k->skillRemoveLogs.add(new SkillRemoveLog(k, new Date(), UserContext.getUserDetails().getFirstName())));
            }
            if (isNotNull(staffingLevelIntervalLog)) {
                activityRemoveLogs.addAll(staffingLevelIntervalLog.getActivityRemoveLogs());
                skillRemoveLogs.addAll(staffingLevelIntervalLog.getSkillRemoveLogs());
                newlyAddedActivities.addAll(staffingLevelIntervalLog.getNewlyAddedActivityIds());
                newlyAddedSkills.addAll(staffingLevelIntervalLog.getNewlyAddedSkillIds());
                //newlyAddedActivities.remove(activityId);
                //newlyAddedSkills.remove(skillId);
            }
            interval.setMinNoOfStaff(interval.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMinNoOfStaff())));
            interval.setMaxNoOfStaff(interval.getStaffingLevelActivities().stream().collect(Collectors.summingInt(k -> k.getMaxNoOfStaff())));
            staffingLevelIntervals.add(interval);
        }

    }

    private static StaffingLevelInterval updateInterval(StaffingLevelInterval interval, StaffingLevelDTO staffingLevelDTO) {
        StaffingLevelInterval staffingLevelInterval = ObjectMapperUtils.copyPropertiesByMapper(interval, StaffingLevelInterval.class);
        StaffingLevelActivity changedStaffingLevelActivityFE;
        StaffingLevelActivity staffingLevelActivity;
        StaffingLevelSkill changedStaffingLevelSkillFE;
        StaffingLevelSkill staffingLevelSkill;
        switch (staffingLevelDTO.getStaffingLevelChange()) {
            case ACTIVITY_MIN_AND_MAX:
                changedStaffingLevelActivityFE = staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelActivities().iterator().next();
                staffingLevelActivity = staffingLevelInterval.getStaffingLevelActivities().stream().filter(k -> k.getActivityId().equals(changedStaffingLevelActivityFE.getActivityId())).findAny().orElse(new StaffingLevelActivity(changedStaffingLevelActivityFE.getActivityId(), 0, 0));
                staffingLevelActivity.setMinNoOfStaff(changedStaffingLevelActivityFE.getMinNoOfStaff());
                staffingLevelActivity.setMaxNoOfStaff(changedStaffingLevelActivityFE.getMaxNoOfStaff());
                staffingLevelInterval.getStaffingLevelActivities().add(staffingLevelActivity);
                break;
            case ACTIVITY_MIN:
                changedStaffingLevelActivityFE = staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelActivities().iterator().next();
                staffingLevelActivity = staffingLevelInterval.getStaffingLevelActivities().stream().filter(k -> k.getActivityId().equals(changedStaffingLevelActivityFE.getActivityId())).findAny().orElse(new StaffingLevelActivity(changedStaffingLevelActivityFE.getActivityId(), 0, 0));
                staffingLevelActivity.setMinNoOfStaff(changedStaffingLevelActivityFE.getMinNoOfStaff());
                staffingLevelInterval.getStaffingLevelActivities().add(staffingLevelActivity);
                break;
            case ACTIVITY_MAX:
                changedStaffingLevelActivityFE = staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelActivities().iterator().next();
                staffingLevelActivity = staffingLevelInterval.getStaffingLevelActivities().stream().filter(k -> k.getActivityId().equals(changedStaffingLevelActivityFE.getActivityId())).findAny().orElse(new StaffingLevelActivity(changedStaffingLevelActivityFE.getActivityId(), 0, 0));
                staffingLevelActivity.setMaxNoOfStaff(changedStaffingLevelActivityFE.getMaxNoOfStaff());
                staffingLevelInterval.getStaffingLevelActivities().add(staffingLevelActivity);
                break;
            case SKILL_BASIC:
                changedStaffingLevelSkillFE = staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelSkills().iterator().next();
                staffingLevelSkill = staffingLevelInterval.getStaffingLevelSkills().stream().filter(k -> k.getSkillId().equals(changedStaffingLevelSkillFE.getSkillId())).findAny().orElse(new StaffingLevelSkill(changedStaffingLevelSkillFE.getSkillId()));
                staffingLevelSkill.getSkillLevelSettings().stream().filter(k -> k.getSkillLevel().equals(SkillLevel.BASIC)).findAny().get().setNoOfStaff(changedStaffingLevelSkillFE.getSkillLevelSettings().stream().filter(k -> k.getSkillLevel().equals(SkillLevel.BASIC)).findAny().get().getNoOfStaff());
                staffingLevelInterval.getStaffingLevelSkills().add(staffingLevelSkill);
                break;
            case SKILL_EXPERT:
                changedStaffingLevelSkillFE = staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelSkills().iterator().next();
                staffingLevelSkill = staffingLevelInterval.getStaffingLevelSkills().stream().filter(k -> k.getSkillId().equals(changedStaffingLevelSkillFE.getSkillId())).findAny().orElse(new StaffingLevelSkill(changedStaffingLevelSkillFE.getSkillId()));
                staffingLevelSkill.getSkillLevelSettings().stream().filter(k -> k.getSkillLevel().equals(SkillLevel.EXPERT)).findAny().get().setNoOfStaff(changedStaffingLevelSkillFE.getSkillLevelSettings().stream().filter(k -> k.getSkillLevel().equals(SkillLevel.EXPERT)).findAny().get().getNoOfStaff());
                staffingLevelInterval.getStaffingLevelSkills().add(staffingLevelSkill);
                break;
            case SKILL_ADVANCE:
                changedStaffingLevelSkillFE = staffingLevelDTO.getPresenceStaffingLevelInterval().get(0).getStaffingLevelSkills().iterator().next();
                staffingLevelSkill = staffingLevelInterval.getStaffingLevelSkills().stream().filter(k -> k.getSkillId().equals(changedStaffingLevelSkillFE.getSkillId())).findAny().orElse(new StaffingLevelSkill(changedStaffingLevelSkillFE.getSkillId()));
                staffingLevelSkill.getSkillLevelSettings().stream().filter(k -> k.getSkillLevel().equals(SkillLevel.ADVANCE)).findAny().get().setNoOfStaff(changedStaffingLevelSkillFE.getSkillLevelSettings().stream().filter(k -> k.getSkillLevel().equals(SkillLevel.ADVANCE)).findAny().get().getNoOfStaff());
                staffingLevelInterval.getStaffingLevelSkills().add(staffingLevelSkill);
                break;
            default:
                break;
        }
        return staffingLevelInterval;
    }

    public static  void setStaffingLevelDetails(StaffingLevel staffingLevel){
        staffingLevel.getPresenceStaffingLevelInterval().forEach(staffingLevelInterval -> staffingLevelInterval.setStaffingLevelSkills(newHashSet()));
        staffingLevel.getAbsenceStaffingLevelInterval().forEach(staffingLevelInterval -> staffingLevelInterval.setStaffingLevelSkills(newHashSet()));
    }
}
