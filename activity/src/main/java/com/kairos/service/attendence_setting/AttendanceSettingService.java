package com.kairos.service.attendence_setting;

import com.kairos.commons.utils.DateTimeInterval;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.constants.AppConstants;
import com.kairos.dto.activity.attendance.*;
import com.kairos.dto.activity.unit_settings.FlexibleTimeSettingDTO;
import com.kairos.dto.activity.unit_settings.UnitSettingDTO;
import com.kairos.dto.user.access_permission.AccessGroupRole;
import com.kairos.persistence.model.attendence_setting.AttendanceSetting;
import com.kairos.persistence.model.shift.Shift;
import com.kairos.persistence.model.shift.ShiftState;
import com.kairos.persistence.repository.attendence_setting.AttendanceSettingRepository;
import com.kairos.persistence.repository.attendence_setting.SickSettingsRepository;
import com.kairos.dto.user.staff.staff.StaffResultDTO;
import com.kairos.persistence.repository.shift.ShiftMongoRepository;
import com.kairos.persistence.repository.shift.ShiftStateMongoRepository;
import com.kairos.persistence.repository.unit_settings.UnitSettingRepository;
import com.kairos.rest_client.GenericIntegrationService;
import com.kairos.service.MongoBaseService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.shift.ShiftService;
import com.kairos.dto.user.reason_code.ReasonCodeDTO;
import com.kairos.commons.utils.DateUtils;
import com.kairos.utils.user_context.UserContext;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.sql.Date;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static javax.management.timer.Timer.ONE_MINUTE;


@Service
public class AttendanceSettingService extends MongoBaseService {

    @Inject
    private AttendanceSettingRepository attendanceSettingRepository;

    @Inject
    private GenericIntegrationService genericIntegrationService;

    @Inject
    private ShiftService shiftService;

    @Inject
    private ExceptionService exceptionService;
    @Inject
    private SickSettingsRepository sickSettingsRepository;
    @Inject
    private ShiftMongoRepository shiftMongoRepository;
    @Inject
    private UnitSettingRepository unitSettingRepository;
    @Inject private ShiftStateMongoRepository shiftStateMongoRepository;

    public AttendanceDTO getAttendanceSetting() {
        AttendanceSetting attendanceSetting = attendanceSettingRepository.findMaxAttendanceCheckIn(UserContext.getUserDetails().getId(), DateUtils.getDateFromLocalDate(LocalDate.now().minusDays(1)),AppConstants.TIME_AND_ATTENDANCE);
        SickSettingsDTO sickSettings = sickSettingsRepository.checkUserIsSick(UserContext.getUserDetails().getId());
        return (Optional.ofNullable(attendanceSetting).isPresent()) ? new AttendanceDTO(getAttendanceDTOObject(attendanceSetting.getAttendanceDuration()), sickSettings) : new AttendanceDTO(null, sickSettings);
    }

    public AttendanceDTO updateAttendanceSetting(Long unitId, Long reasonCodeId, boolean checkIn) {
        AttendanceDTO attendanceDTO = null;
        AttendanceSetting attendanceSetting = null;
        Long userId = UserContext.getUserDetails().getId();
        List<StaffResultDTO> staffAndOrganizationIds = genericIntegrationService.getStaffIdsByUserId(userId);
        if (!Optional.ofNullable(staffAndOrganizationIds).isPresent()) {
            exceptionService.actionNotPermittedException("message.staff.notfound");
        }
        Map<Long,StaffResultDTO> unitIdAndStaffResultMap=staffAndOrganizationIds.stream().collect(Collectors.toMap(k->k.getUnitId(),v->v));
        List<Long> staffIds=staffAndOrganizationIds.stream().map(e -> e.getStaffId()).collect(Collectors.toList());
        Map<Long,FlexibleTimeSettingDTO> unitIdAndFlexibleTimeMap=new HashMap<>();
        List<OrganizationAndReasonCodeDTO> organizationAndReasonCodeDTOS=staffAndOrganizationIds.stream().map(reasonCode->new OrganizationAndReasonCodeDTO(reasonCode.getUnitId(),reasonCode.getUnitName(),reasonCode.getReasonCodes())).collect(Collectors.toList());
        Shift shift=null;
        List<Shift> shifts=shiftMongoRepository.findShiftsForCheckIn(staffIds, Date.from(ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant()), Date.from(ZonedDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant()));
        Map<BigInteger,Shift> shiftMap=shifts.stream().collect(Collectors.toMap(k->k.getId(),v->v));
        Map<Long,List<ReasonCodeDTO>> unitAndReasonCode=staffAndOrganizationIds.stream().collect(Collectors.toMap(StaffResultDTO::getUnitId,StaffResultDTO::getReasonCodes));
        if(!shifts.isEmpty()) {
            List<UnitSettingDTO> unitSettingDTOS=unitSettingRepository.getGlideTimeByUnitIds(staffAndOrganizationIds.stream().map(s->s.getUnitId()).collect(Collectors.toList()));
            for(UnitSettingDTO unitSettingDTO:unitSettingDTOS){
                    unitIdAndFlexibleTimeMap.put(unitSettingDTO.getUnitId(),unitSettingDTO.getFlexibleTimeSettings());
            }
        }
        if(checkIn) {
            for (Shift checkInshift : shifts) {
                boolean result;
                if (unitIdAndFlexibleTimeMap.containsKey(checkInshift.getUnitId())) {
                    if(unitIdAndFlexibleTimeMap.get(checkInshift.getUnitId())==null){
                        exceptionService.dataNotFoundException("error.glidetime.notfound",checkInshift.getUnitId());
                    }
                    DateTimeInterval interval = new DateTimeInterval(checkInshift.getStartDate(), checkInshift.getEndDate());
                    result=(Math.abs((Duration.between(DateUtils.getLocalDateTimeFromDate(checkInshift.getStartDate()), DateUtils.getLocalDateTimeFromZoneId(ZoneId.of(unitIdAndStaffResultMap.get(checkInshift.getUnitId()).getTimeZone())))).toMinutes()) < unitIdAndFlexibleTimeMap.get(checkInshift.getUnitId()).getCheckInFlexibleTime());
                    if (interval.contains(DateUtils.getCurrentMillistByTimeZone(unitIdAndStaffResultMap.get(checkInshift.getUnitId()).getTimeZone()))) {
                        result = (result || reasonCodeId != null) ? true : false;
                        if (!result) {
                                attendanceDTO = new AttendanceDTO(new ArrayList<>(),unitAndReasonCode.get(checkInshift.getUnitId()));
                        }
                    } else {
                        result = result ? true : false;
                        if (!result ) {
                            attendanceDTO = new AttendanceDTO(organizationAndReasonCodeDTOS,new ArrayList<>());
                        }
                    }
                    if (result) {
                        shift = checkInshift;
                            break;
                    }
                }
            }
            if(shift==null) {
                attendanceSetting = checkInWithoutShift(unitId, reasonCodeId, staffAndOrganizationIds);
                if (attendanceSetting == null) {
                    return (attendanceDTO!=null)?attendanceDTO:new AttendanceDTO(organizationAndReasonCodeDTOS,new ArrayList<>());
                }
            }
        }else{
            shift=shifts.stream().filter(shift1 -> shift1.getAttendanceDuration()!=null&&shift1.getAttendanceDuration().getFrom()!=null&&shift1.getAttendanceDuration().getTo()==null).findAny().orElse(null);
        }


        //If shift exist of User
         if(shift!=null && checkIn){
                attendanceSetting= checkInWithShift(shift,reasonCodeId,unitIdAndStaffResultMap.get(shift.getUnitId()));
            if(attendanceSetting==null){
                return new AttendanceDTO(new ArrayList<>(),unitAndReasonCode.get(shift.getUnitId()));
            }else {
                shift.setAttendanceDuration(attendanceSetting.getAttendanceDuration().get(0));
                shiftMongoRepository.save(shift);
            }
        }

        else if(!checkIn){
            AttendanceSetting oldAttendanceSetting=attendanceSettingRepository.findMaxAttendanceCheckIn(UserContext.getUserDetails().getId(), DateUtils.getDateFromLocalDate(LocalDate.now().minusDays(1)),AppConstants.TIME_AND_ATTENDANCE);;
            shift=shiftMap.get(oldAttendanceSetting.getShiftId());
            attendanceSetting= checkOut(staffAndOrganizationIds,shiftMap,oldAttendanceSetting,reasonCodeId);
            if(attendanceSetting==null){
                return new AttendanceDTO(new ArrayList<>(),unitAndReasonCode.get(shift.getUnitId()));
            }else {
                if(shift!=null){
                    shift.setAttendanceDuration(attendanceSetting.getAttendanceDuration().get(attendanceSetting.getAttendanceDuration().size()-1));
                    shiftMongoRepository.save(shift);
                }
            }
        }


        if (Optional.ofNullable(attendanceSetting).isPresent()) {
            save(attendanceSetting);
            if(shift!=null)
            createShiftState(Arrays.asList(shift),checkIn,Arrays.asList(attendanceSetting));
            attendanceDTO = new AttendanceDTO(getAttendanceDTOObject(attendanceSetting.getAttendanceDuration()), null);
        }
        return attendanceDTO;
    }

    private AttendanceSetting checkInWithoutShift(Long unitId, Long reasonCodeId, List<StaffResultDTO> staffAndOrganizationIds) {
        AttendanceSetting attendanceSetting = null;
        StaffResultDTO staffAndOrganizationId;
        if (Optional.ofNullable(unitId).isPresent() && !Optional.ofNullable(reasonCodeId).isPresent()) {
            exceptionService.actionNotPermittedException("message.unitid.reasoncodeid.notnull", "");
        } else if (Optional.ofNullable(unitId).isPresent() && Optional.ofNullable(reasonCodeId).isPresent()) {
            staffAndOrganizationId = staffAndOrganizationIds.stream().filter(e -> e.getUnitId().equals(unitId)).findAny().get();
            if (!Optional.ofNullable(staffAndOrganizationId).isPresent()) {
                exceptionService.actionNotPermittedException("message.staff.unitid.notfound");
            }
            AttendanceDuration attendanceDuration = new AttendanceDuration(DateUtils.getTimezonedCurrentDateTime(staffAndOrganizationId.getTimeZone()));
            attendanceSetting = new AttendanceSetting(unitId, staffAndOrganizationId.getStaffId(), UserContext.getUserDetails().getId(), reasonCodeId, Arrays.asList(attendanceDuration));
        }
        return attendanceSetting;
    }

    private AttendanceSetting checkOut(List<StaffResultDTO> staffAndOrganizationIds,Map<BigInteger,Shift> shiftMap,AttendanceSetting oldAttendanceSetting,Long reasonCodeId) {
        AttendanceDuration duration ;
        AttendanceSetting attendanceSetting=oldAttendanceSetting;
        if(attendanceSetting.getShiftId()!=null&&shiftMap.get(attendanceSetting.getShiftId())!=null){
            boolean result= validateFlexibleTimeWhileCheckOut(shiftMap.get(attendanceSetting.getShiftId()),reasonCodeId);
            if(!result){
                return null;
            }

        }
        final Long unitId=attendanceSetting.getUnitId();
        if (Optional.ofNullable(attendanceSetting).isPresent()) {
            attendanceSetting.getAttendanceDuration().sort((s1,s2)->s1.getFrom().compareTo(s2.getFrom()));
            duration = attendanceSetting.getAttendanceDuration().get(attendanceSetting.getAttendanceDuration().size()-1);
            if (!Optional.ofNullable(duration.getTo()).isPresent()) {
                StaffResultDTO staffAndOrganizationId = staffAndOrganizationIds.stream().filter(e -> e.getUnitId().equals(unitId)).findAny().get();
                duration.setTo(DateUtils.getTimezonedCurrentDateTime(staffAndOrganizationId.getTimeZone()));
            } else {
                exceptionService.actionNotPermittedException("message.checkout.exists");
            }
        } else {
            exceptionService.actionNotPermittedException("message.attendance.notexists");
        }
        return attendanceSetting;
    }


    private AttendanceDurationDTO getAttendanceDTOObject(List<AttendanceDuration> attendanceDuration) {
        attendanceDuration.sort((a1,a2)->a1.getFrom().compareTo(a2.getFrom()));
        AttendanceDurationDTO attendanceDurationDTO = new AttendanceDurationDTO();
        attendanceDurationDTO.setClockInDate(DateUtils.getLocalDateFromLocalDateTime(attendanceDuration.get(attendanceDuration.size()-1).getFrom()));
        attendanceDurationDTO.setClockInTime(DateUtils.getLocalTimeFromLocalDateTime(attendanceDuration.get(attendanceDuration.size()-1).getFrom()));
        if (Optional.ofNullable(attendanceDuration.get(attendanceDuration.size()-1).getTo()).isPresent()) {
            attendanceDurationDTO.setClockOutDate(DateUtils.getLocalDateFromLocalDateTime(attendanceDuration.get(attendanceDuration.size()-1).getTo()));
            attendanceDurationDTO.setClockOutTime(DateUtils.getLocalTimeFromLocalDateTime(attendanceDuration.get(attendanceDuration.size()-1).getTo()));
        }
        return attendanceDurationDTO;
    }

        private AttendanceSetting checkInWithShift(Shift shift, Long reasonCodeId, StaffResultDTO staffAndOrganizationId) {
            AttendanceSetting attendanceSetting=null;
                AttendanceSetting oldAttendanceSetting=attendanceSettingRepository.findByShiftId(shift.getId(),AppConstants.REALTIME);
                AttendanceDuration attendanceDuration = new AttendanceDuration(DateUtils.getLocalDateTimeFromZoneId(ZoneId.of(staffAndOrganizationId.getTimeZone())));
                if(oldAttendanceSetting!=null){
                    oldAttendanceSetting.getAttendanceDuration().add(attendanceDuration);
                    attendanceSetting=oldAttendanceSetting;
                }else{
                    attendanceSetting= new AttendanceSetting(shift.getId(),shift.getUnitId(), shift.getStaffId(), UserContext.getUserDetails().getId(),reasonCodeId,Arrays.asList(attendanceDuration), AppConstants.REALTIME);
                }
        return attendanceSetting;

    }

    private boolean validateFlexibleTimeWhileCheckOut(Shift shift,Long reasonCodeId){
        FlexibleTimeSettingDTO flexibleTimeSettingDTO = unitSettingRepository.getFlexibleTimingByUnit(shift.getUnitId()).getFlexibleTimeSettings();
        if (flexibleTimeSettingDTO != null) {
            Short checkInFlexibleTime = flexibleTimeSettingDTO.getCheckInFlexibleTime();
             return  (Math.abs((shift.getEndDate().getTime() - DateUtils.getCurrentMillis()) / ONE_MINUTE) < checkInFlexibleTime || reasonCodeId != null);
            }
        return false;
    }

    public void createShiftState(List<Shift> shifts,boolean checkIn,List<AttendanceSetting> attendanceSettings){
        List<ShiftState> realtimeShiftStates;
        List<ShiftState> timeAndAttendanceShiftStates=null;
       List<AttendanceSetting> attendanceSettingForTimeAndAttendance=null;
       Map<BigInteger,Shift> shiftMap=shifts.stream().collect(Collectors.toMap(k->k.getId(),v->v));
        realtimeShiftStates=shiftStateMongoRepository.findShiftStateByShiftIdsAndActualPhase(shifts.stream().map(s->s.getId()).collect(Collectors.toList()), AppConstants.REALTIME);
        if(shifts!=null&&checkIn) {
            if(realtimeShiftStates.isEmpty()) {
                ShiftState shiftState=null;
                shiftState=createRealTimeShiftState(shiftState,shifts.get(0),attendanceSettings);
                realtimeShiftStates.add(shiftState);
            }
            shiftStateMongoRepository.saveEntities(realtimeShiftStates);
        } else{
            if(!realtimeShiftStates.isEmpty()){
                realtimeShiftStates.forEach(realtimeShiftState->{
                    realtimeShiftState.setAttendanceDuration(shiftMap.get(realtimeShiftState.getShiftId()).getAttendanceDuration());
                });
                shiftStateMongoRepository.saveEntities(realtimeShiftStates);
            }
            attendanceSettingForTimeAndAttendance=attendanceSettingForTimeAndAttendance(attendanceSettingForTimeAndAttendance,attendanceSettings,shifts);
            if(!attendanceSettingForTimeAndAttendance.isEmpty()) attendanceSettingRepository.saveEntities(attendanceSettingForTimeAndAttendance);
            timeAndAttendanceShiftStates=createTimeAndAttendanceShiftState(timeAndAttendanceShiftStates,realtimeShiftStates,shifts,attendanceSettingForTimeAndAttendance,attendanceSettings);
            if(!timeAndAttendanceShiftStates.isEmpty()) shiftStateMongoRepository.saveEntities(timeAndAttendanceShiftStates);
     }
    }

    public ShiftState createRealTimeShiftState(ShiftState realtimeShiftState,Shift shift,List<AttendanceSetting> attendanceSetting){
        realtimeShiftState = ObjectMapperUtils.copyPropertiesByMapper(shift, ShiftState.class);
        realtimeShiftState.setId(null);
        realtimeShiftState.setShiftId(shift.getId());
        realtimeShiftState.setAttendanceSettingId(attendanceSetting.get(0).getId());
        realtimeShiftState.setActualPhaseState(AppConstants.REALTIME);
        realtimeShiftState.setAccessGroupRole(AccessGroupRole.STAFF);
        return realtimeShiftState;
    }

    public List<ShiftState> createTimeAndAttendanceShiftState(List<ShiftState> timeAndAttendanceShiftStates,List<ShiftState> realtimeShiftStates,List<Shift> shifts,List<AttendanceSetting> attendanceSettingForTimeAndAttendance,List<AttendanceSetting> attendanceSetting){
        timeAndAttendanceShiftStates=shiftStateMongoRepository.findShiftStateByShiftIdsAndActualPhase(shifts.stream().map(shift -> shift.getId()).collect(Collectors.toList()),AppConstants.TIME_AND_ATTENDANCE);
        Map<BigInteger,ShiftState> realtimeShiftStateMap=realtimeShiftStates.stream().collect(Collectors.toMap(k->k.getShiftId(),v->v));
        Map<BigInteger,ShiftState> timeAndAttendanceShiftStateMap=timeAndAttendanceShiftStates.stream().collect(Collectors.toMap(k->k.getShiftId(),v->v));
        Map<BigInteger,AttendanceSetting> tAndAttendanceMap=attendanceSettingForTimeAndAttendance.stream().collect(Collectors.toMap(k->k.getShiftId(),v->v));
        for (Shift shift:shifts) {
            if (timeAndAttendanceShiftStateMap.get(shift.getId()) != null) {
                ObjectMapperUtils.copyPropertiesExceptSpecific(realtimeShiftStateMap.get(shift.getId()), timeAndAttendanceShiftStateMap.get(shift.getId()), "id", "actualPhaseState", "accessGroupRole","attendanceSettingId");
                timeAndAttendanceShiftStates.add(timeAndAttendanceShiftStateMap.get(shift.getId()));
            } else {
                if (realtimeShiftStateMap.get(shift.getId()) != null) {
                    ShiftState timeAndAttendanceShiftState = ObjectMapperUtils.copyPropertiesByMapper(realtimeShiftStateMap.get(shift.getId()), ShiftState.class);
                    timeAndAttendanceShiftState.setId(null);
                    timeAndAttendanceShiftState.setAttendanceSettingId(tAndAttendanceMap.get(shift.getId()).getId());
                    timeAndAttendanceShiftState.setAccessGroupRole(AccessGroupRole.STAFF);
                    timeAndAttendanceShiftState.setActualPhaseState(AppConstants.TIME_AND_ATTENDANCE);
                    timeAndAttendanceShiftStates.add(timeAndAttendanceShiftState);
                }
            }
        }
        return timeAndAttendanceShiftStates;
    }

    public List<AttendanceSetting> attendanceSettingForTimeAndAttendance(List<AttendanceSetting> attendanceSettingForTimeAndAttendance,List<AttendanceSetting> attendanceSettings,List<Shift> shifts){
        List<AttendanceSetting> timeAndAttendanceSetting=new ArrayList<>();
        attendanceSettingForTimeAndAttendance=attendanceSettingRepository.findAllByShiftIds(shifts.stream().map(s->s.getId()).collect(Collectors.toList()), AppConstants.TIME_AND_ATTENDANCE);
        Map<BigInteger,AttendanceSetting> attendanceSettingForTAndAMap=attendanceSettingForTimeAndAttendance.stream().collect(Collectors.toMap(k->k.getShiftId(),v->v));
        Map<BigInteger,AttendanceSetting> attendanceSettingMap=attendanceSettings.stream().collect(Collectors.toMap(k->k.getShiftId(),v->v));
        for (Shift shift:shifts){
            if(attendanceSettingForTAndAMap.get(shift.getId())!=null) {
                ObjectMapperUtils.copyPropertiesExceptSpecific(attendanceSettingMap.get(shift.getId()),attendanceSettingForTAndAMap.get(shift.getId()),"id","shiftState");
                timeAndAttendanceSetting.add(attendanceSettingForTAndAMap.get(shift.getId()));
            }else{
                if(attendanceSettingMap.get(shift.getId())!=null) {
                    AttendanceSetting attendanceSetting = attendanceSettingMap.get(shift.getId());
                    attendanceSetting.setId(null);
                    attendanceSetting.setShiftState(AppConstants.TIME_AND_ATTENDANCE);
                    timeAndAttendanceSetting.add(attendanceSetting);
                }
            }
        }
        return timeAndAttendanceSetting;
    }

    public void checkOutBySchedulerJob(Long unitId){
        List<Shift> saveShifts=new ArrayList<>();
        List<AttendanceSetting> attendanceSettings=attendanceSettingRepository.findAllbyUnitIdAndDate(unitId,DateUtils.asDate(DateUtils.getEndOfDayFromLocalDateTime()));
        List<Shift> shifts=shiftMongoRepository.findAllShiftByIds(attendanceSettings.stream().map(attendanceSetting -> attendanceSetting.getShiftId()).collect(Collectors.toList()));
             Map<BigInteger, Shift> staffIdAndShifts = shifts.stream().collect(Collectors.toMap(k -> k.getId(), v -> v));
             attendanceSettings.forEach(attendanceSetting -> {
                 if (staffIdAndShifts.get(attendanceSetting.getShiftId()) != null) {
                     Shift shift = staffIdAndShifts.get(attendanceSetting.getShiftId());
                     if (!shift.getEndDate().after(DateUtils.getCurrentDate())) {
                         attendanceSetting.getAttendanceDuration().sort((a1, a2) -> a1.getFrom().compareTo(a2.getFrom()));
                         attendanceSetting.getAttendanceDuration().get(attendanceSetting.getAttendanceDuration().size() - 1).setTo(DateUtils.asLocalDateTime(shift.getEndDate()));
                         shift.getAttendanceDuration().setTo(DateUtils.asLocalDateTime(shift.getEndDate()));
                         saveShifts.add(shift);
                     }
                 } else {
                     attendanceSetting.getAttendanceDuration().get(attendanceSetting.getAttendanceDuration().size() - 1).setTo(DateUtils.getEndOfDayFromLocalDateTime());
                 }
             });
             if (!attendanceSettings.isEmpty()) attendanceSettingRepository.saveEntities(attendanceSettings);
             if (!saveShifts.isEmpty()) shiftMongoRepository.saveEntities(saveShifts);
             createShiftState(shifts, false, attendanceSettings);
         }
}
