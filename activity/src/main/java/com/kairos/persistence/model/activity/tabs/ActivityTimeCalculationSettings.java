package com.kairos.persistence.model.activity.tabs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.annotations.KPermissionField;
import com.kairos.constants.ApiConstants;
import com.kairos.enums.FixedDurationSetting;
import com.kairos.enums.TimeCalaculationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class ActivityTimeCalculationSettings implements Serializable {

    private static final long serialVersionUID = -443573021578255404L;
    @KPermissionField
    private String methodForCalculatingTime;
    @KPermissionField
    private TimeCalaculationType fullDayCalculationType;
    @KPermissionField
    private TimeCalaculationType fullWeekCalculationType;
    private Boolean allowBreakReduction;
    @KPermissionField
    private Long fixedTimeValue;
    private String methodForCalculatingTimeInMonths;
    private List<String> balanceType;
    @KPermissionField
    private Boolean multiplyWith;
    @KPermissionField
    private Double multiplyWithValue = ApiConstants.DEFAULT_VALUE;
    private Boolean multiplyByVacationFactor;
    private Boolean multiplyByFinalSchedule;
    @KPermissionField
    private DayOfWeek fullWeekStart;
    @KPermissionField
    private DayOfWeek fullWeekEnd;
    private String breakTemplates;
    @KPermissionField
    private int historyDuration;
    @KPermissionField
    private LocalTime defaultStartTime;
    @KPermissionField
    private List<BigInteger> dayTypes = new ArrayList<>();
    private boolean replaceWithPublishedShiftTime;
    private boolean replaceWithUnapprovedAbsenceRequest;
    private FixedDurationSetting fixedDurationSetting = FixedDurationSetting.FOR_TIME_CALCULATION;

    public ActivityTimeCalculationSettings(String methodForCalculatingTime, Long fixedTimeValue, Boolean multiplyWith, LocalTime defaultStartTime, Double multiplyWithValue) {
        this.methodForCalculatingTime = methodForCalculatingTime;
        this.fixedTimeValue = fixedTimeValue;
        this.multiplyWith = multiplyWith;
        this.defaultStartTime = defaultStartTime;
        this.multiplyWithValue = multiplyWithValue;
    }
}