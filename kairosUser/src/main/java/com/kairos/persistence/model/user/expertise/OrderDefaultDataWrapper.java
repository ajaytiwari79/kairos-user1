package com.kairos.persistence.model.user.expertise;

import com.kairos.activity.response.dto.ActivityDTO;
import com.kairos.persistence.model.organization.DayType;
import com.kairos.persistence.model.timetype.PresenceTypeDTO;
import com.kairos.persistence.model.user.agreement.cta.PlannedTimeWithFactor;
import com.kairos.persistence.model.user.country.*;
import com.kairos.persistence.model.user.skill.Skill;
import com.kairos.persistence.model.user.staff.StaffPersonalDetailDTO;
import com.kairos.response.dto.web.open_shift.OrderResponseDTO;

import java.util.List;

public class OrderDefaultDataWrapper {
    private List<OrderResponseDTO> orders;
    private List<ActivityDTO> activities;
    private List<Skill> skills;
    private List<Expertise> expertise;
    private List<TimeType> timeTypes;
    private List<StaffPersonalDetailDTO> staffList;
    private List<PresenceType> plannedTime;
    private List<FunctionDTO> functions;
    private List<ReasonCodeResponseDTO> reasonCodes;
    private List<com.kairos.persistence.model.user.country.DayType> dayTypes;

    public OrderDefaultDataWrapper() {
        //Default Constructor
    }

    public List<OrderResponseDTO> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderResponseDTO> orders) {
        this.orders = orders;
    }

    public List<ActivityDTO> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityDTO> activities) {
        this.activities = activities;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Expertise> getExpertise() {
        return expertise;
    }

    public void setExpertise(List<Expertise> expertise) {
        this.expertise = expertise;
    }

    public List<TimeType> getTimeTypes() {
        return timeTypes;
    }

    public void setTimeTypes(List<TimeType> timeTypes) {
        this.timeTypes = timeTypes;
    }

    public List<StaffPersonalDetailDTO> getStaffList() {
        return staffList;
    }

    public void setStaffList(List<StaffPersonalDetailDTO> staffList) {
        this.staffList = staffList;
    }

    public List<PresenceType> getPlannedTime() {
        return plannedTime;
    }

    public void setPlannedTime(List<PresenceType> plannedTime) {
        this.plannedTime = plannedTime;
    }

    public List<FunctionDTO> getFunctions() {
        return functions;
    }

    public void setFunctions(List<FunctionDTO> functions) {
        this.functions = functions;
    }

    public List<ReasonCodeResponseDTO> getReasonCodes() {
        return reasonCodes;
    }

    public void setReasonCodes(List<ReasonCodeResponseDTO> reasonCodes) {
        this.reasonCodes = reasonCodes;
    }

    public List<com.kairos.persistence.model.user.country.DayType> getDayTypes() {
        return dayTypes;
    }

    public void setDayTypes(List<com.kairos.persistence.model.user.country.DayType> dayTypes) {
        this.dayTypes = dayTypes;
    }
}
