package com.kairos.response.dto.web.cta;
import com.kairos.user.agreement.cta.CalculateValueIfPlanned;
import com.kairos.user.country.FunctionDTO;
import com.kairos.response.dto.web.presence_type.PresenceTypeDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CTARuleTemplateDefaultDataWrapper {
    private List<CalculateValueIfPlanned> calculateValueIfPlanned=new ArrayList<>();
    private List<DayTypeDTO> dayTypes=new ArrayList<>();
    private List<PhaseResponseDTO> phases=new ArrayList<>();
    private List<com.kairos.dto.activity.TimeTypeDTO> timeTypes=new ArrayList<>();
    private List<ActivityTypeDTO> activityTypes=new ArrayList<>();
    private List<EmploymentTypeDTO> employmentTypes=new ArrayList<>();
    private List<PresenceTypeDTO> plannedTime=new ArrayList<>();
    private List<Map<String, Object>>currencies=new ArrayList<>();
    private List<Map<String, Object>>holidayMapList=new ArrayList<>();
    private List<FunctionDTO> functions = new ArrayList<FunctionDTO>();
    List<ActivityCategoryDTO> activityCategories;

    public CTARuleTemplateDefaultDataWrapper() {
        //default
    }

    public List<FunctionDTO> getFunctions() {
        return functions;
    }

    public void setFunctions(List<FunctionDTO> functions) {
        this.functions = functions;
    }
    public List<CalculateValueIfPlanned> getCalculateValueIfPlanned() {
        return calculateValueIfPlanned;
    }

    public void setCalculateValueIfPlanned(List<CalculateValueIfPlanned> calculateValueIfPlanned) {
        this.calculateValueIfPlanned = calculateValueIfPlanned;
    }

    public List<DayTypeDTO> getDayTypes() {
        return dayTypes;
    }

    public void setDayTypes(List<DayTypeDTO> dayTypes) {
        this.dayTypes = dayTypes;
    }

    public List<PhaseResponseDTO> getPhases() {
        return phases;
    }

    public void setPhases(List<PhaseResponseDTO> phases) {
        this.phases = phases;
    }

    public List<com.kairos.dto.activity.TimeTypeDTO> getTimeTypes() {
        return timeTypes;
    }

    public void setTimeTypes(List<com.kairos.dto.activity.TimeTypeDTO> timeTypes) {
        this.timeTypes = timeTypes;
    }

    public List<ActivityTypeDTO> getActivityTypes() {
        return activityTypes;
    }

    public void setActivityTypes(List<ActivityTypeDTO> activityTypes) {
        this.activityTypes = activityTypes;
    }

    public List<EmploymentTypeDTO> getEmploymentTypes() {
        return employmentTypes;
    }

    public void setEmploymentTypes(List<EmploymentTypeDTO> employmentTypes) {
        this.employmentTypes = employmentTypes;
    }

    public List<Map<String, Object>> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Map<String, Object>> currencies) {
        this.currencies = currencies;
    }

    public List<Map<String, Object>> getHolidayMapList() {
        return holidayMapList;
    }

    public void setHolidayMapList(List<Map<String, Object>> holidayMapList) {
        this.holidayMapList = holidayMapList;
    }

    public List<PresenceTypeDTO> getPlannedTime() {
        return plannedTime;
    }

    public void setPlannedTime(List<PresenceTypeDTO> plannedTime) {
        this.plannedTime = plannedTime;
    }

    public List<ActivityCategoryDTO> getActivityCategories() {
        return activityCategories;
    }

    public void setActivityCategories(List<ActivityCategoryDTO> activityCategories) {
        this.activityCategories = activityCategories;
    }


}
