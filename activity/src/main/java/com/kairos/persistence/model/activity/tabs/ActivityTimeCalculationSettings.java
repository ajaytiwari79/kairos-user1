package com.kairos.persistence.model.activity.tabs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.annotations.KPermissionField;
import com.kairos.constants.ApiConstants;
import com.kairos.enums.TimeCalaculationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class ActivityTimeCalculationSettings {

    @KPermissionField
    private String methodForCalculatingTime;
    private TimeCalaculationType fullDayCalculationType;
    private TimeCalaculationType fullWeekCalculationType;
    private Boolean allowBreakReduction;
    private Long fixedTimeValue;
    private String methodForCalculatingTimeInMonths;
    private List<String> balanceType;
    private Boolean multiplyWith;
    private Double multiplyWithValue = ApiConstants.DEFAULT_VALUE;
    private Boolean multiplyByVacationFactor;
    private Boolean multiplyByFinalSchedule;
    private DayOfWeek fullWeekStart;
    private DayOfWeek fullWeekEnd;
    private String breakTemplates;
    private int historyDuration;
    private LocalTime defaultStartTime;
    private List<Long> dayTypes = new ArrayList<>();
    private boolean replaceWithPublishedShiftTime;
    private boolean replaceWithUnapprovedAbsenceRequest;

    public ActivityTimeCalculationSettings(String methodForCalculatingTime, Long fixedTimeValue, Boolean multiplyWith, LocalTime defaultStartTime, Double multiplyWithValue) {
        this.methodForCalculatingTime = methodForCalculatingTime;
        this.fixedTimeValue = fixedTimeValue;
        this.multiplyWith = multiplyWith;
        this.defaultStartTime = defaultStartTime;
        this.multiplyWithValue = multiplyWithValue;
    }
}