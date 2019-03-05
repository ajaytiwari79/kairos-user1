package com.kairos.wrapper.phase;
import com.kairos.dto.activity.shift.ShiftTemplateDTO;
import com.kairos.dto.activity.phase.PhaseDTO;
import com.kairos.dto.activity.phase.PhaseWeeklyDTO;
import com.kairos.dto.user.access_group.UserAccessRoleDTO;
import com.kairos.dto.user.country.day_type.DayType;
import com.kairos.dto.user.reason_code.ReasonCodeDTO;
import com.kairos.wrapper.activity.ActivityWithCompositeDTO;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vipul on 19/9/17.
 */
public class PhaseActivityDTO {
    private List<ActivityWithCompositeDTO> activities;
    private List<PhaseWeeklyDTO> phases;
    private List<DayType> dayTypes= new ArrayList<>();
    private UserAccessRoleDTO staffAccessRole;
    private List<ShiftTemplateDTO> shiftTemplates;
    private List<PhaseDTO> applicablePhases;
    private List<PhaseDTO> actualPhases;
    private List<ReasonCodeDTO> reasonCodes;
    private LocalDate planningPeriodStartDate;
    private LocalDate planningPeriodEndDate;

    public PhaseActivityDTO() {
        //Default Constructor
    }

    public PhaseActivityDTO(List<ActivityWithCompositeDTO> activities, List<PhaseWeeklyDTO> phases, List<DayType> dayTypes,
                            UserAccessRoleDTO staffAccessRole, List<ShiftTemplateDTO> shiftTemplates, List<PhaseDTO> applicablePhases, List<PhaseDTO> actualPhases,List<ReasonCodeDTO> reasonCodes,LocalDate planningPeriodStartDate,LocalDate planningPeriodEndDate) {
        this.activities = activities;
        this.phases = phases;
        this.dayTypes = dayTypes;
        this.staffAccessRole = staffAccessRole;
        this.shiftTemplates = shiftTemplates;
        this.applicablePhases = applicablePhases;
        this.actualPhases = actualPhases;
        this.reasonCodes = reasonCodes;
        this.planningPeriodStartDate=planningPeriodStartDate;
        this.planningPeriodEndDate=planningPeriodEndDate;
    }
    
    public List<ActivityWithCompositeDTO> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityWithCompositeDTO> activities) {
        this.activities = activities;
    }

    public List<PhaseWeeklyDTO> getPhases() {
        return phases;
    }

    public void setPhases(List<PhaseWeeklyDTO> phases) {
        this.phases = phases;
    }

    public List<PhaseDTO> getApplicablePhases() {
        return applicablePhases;
    }

    public void setApplicablePhases(List<PhaseDTO> applicablePhases) {
        this.applicablePhases = applicablePhases;
    }

    public List<DayType> getDayTypes() {
        return dayTypes;
    }

    public void setDayTypes(List<DayType> dayTypes) {
        this.dayTypes = dayTypes;
    }

    public UserAccessRoleDTO getStaffAccessRole() {
        return staffAccessRole;
    }

    public void setStaffAccessRole(UserAccessRoleDTO staffAccessRole) {
        this.staffAccessRole = staffAccessRole;
    }

    public List<PhaseDTO> getActualPhases() {
        return actualPhases;
    }

    public void setActualPhases(List<PhaseDTO> actualPhases) {
        this.actualPhases = actualPhases;
    }

    public List<ShiftTemplateDTO> getShiftTemplates() {
        return shiftTemplates;
    }

    public void setShiftTemplates(List<ShiftTemplateDTO> shiftTemplates) {
        this.shiftTemplates = shiftTemplates;
    }

    public List<ReasonCodeDTO> getReasonCodes() {
        return reasonCodes;
    }

    public void setReasonCodes(List<ReasonCodeDTO> reasonCodes) {
        this.reasonCodes = reasonCodes;
    }

    public LocalDate getPlanningPeriodStartDate() {
        return planningPeriodStartDate;
    }

    public void setPlanningPeriodStartDate(LocalDate planningPeriodStartDate) {
        this.planningPeriodStartDate = planningPeriodStartDate;
    }

    public LocalDate getPlanningPeriodEndDate() {
        return planningPeriodEndDate;
    }

    public void setPlanningPeriodEndDate(LocalDate planningPeriodEndDate) {
        this.planningPeriodEndDate = planningPeriodEndDate;
    }
}

