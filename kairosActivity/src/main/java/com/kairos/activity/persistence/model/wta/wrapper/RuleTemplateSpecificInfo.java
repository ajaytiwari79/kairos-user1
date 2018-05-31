package com.kairos.activity.persistence.model.wta.wrapper;

import com.kairos.activity.client.dto.TimeSlotWrapper;
import com.kairos.activity.response.dto.ShiftWithActivityDTO;
import com.kairos.activity.util.DateTimeInterval;
import com.kairos.response.dto.web.access_group.UserAccessRoleDTO;
import com.kairos.response.dto.web.cta.DayTypeDTO;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @author pradeep
 * @date - 23/5/18
 */

public class RuleTemplateSpecificInfo {

    private List<ShiftWithActivityDTO> shifts;
    private ShiftWithActivityDTO shift;
    private List<TimeSlotWrapper> timeSlotWrappers;
    private String phase;
    private DateTimeInterval planningPeriod;
    private Map<BigInteger,Integer> counterMap;
    private List<DayTypeDTO> dayTypes;
    private UserAccessRoleDTO user;


    public RuleTemplateSpecificInfo(List<ShiftWithActivityDTO> shifts, ShiftWithActivityDTO shift, List<TimeSlotWrapper> timeSlotWrappers, String phase, DateTimeInterval planningPeriod, Map<BigInteger,Integer> counterMap, List<DayTypeDTO> dayTypes,UserAccessRoleDTO user) {
        this.shifts = shifts;
        this.shift = shift;
        this.timeSlotWrappers = timeSlotWrappers;
        this.phase = phase;
        this.planningPeriod = planningPeriod;
        this.counterMap = counterMap;
        this.dayTypes = dayTypes;
        this.user = user;
    }

    public UserAccessRoleDTO getUser() {
        return user;
    }

    public void setUser(UserAccessRoleDTO user) {
        this.user = user;
    }

    public List<DayTypeDTO> getDayTypes() {
        return dayTypes;
    }

    public void setDayTypes(List<DayTypeDTO> dayTypes) {
        this.dayTypes = dayTypes;
    }

    public Map<BigInteger, Integer> getCounterMap() {
        return counterMap;
    }

    public void setCounterMap(Map<BigInteger, Integer> counterMap) {
        this.counterMap = counterMap;
    }

    public List<ShiftWithActivityDTO> getShifts() {
        return shifts;
    }

    public void setShifts(List<ShiftWithActivityDTO> shifts) {
        this.shifts = shifts;
    }

    public ShiftWithActivityDTO getShift() {
        return shift;
    }

    public void setShift(ShiftWithActivityDTO shift) {
        this.shift = shift;
    }

    public List<TimeSlotWrapper> getTimeSlotWrappers() {
        return timeSlotWrappers;
    }

    public void setTimeSlotWrappers(List<TimeSlotWrapper> timeSlotWrappers) {
        this.timeSlotWrappers = timeSlotWrappers;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public DateTimeInterval getPlanningPeriod() {
        return planningPeriod;
    }

    public void setPlanningPeriod(DateTimeInterval planningPeriod) {
        this.planningPeriod = planningPeriod;
    }
}
