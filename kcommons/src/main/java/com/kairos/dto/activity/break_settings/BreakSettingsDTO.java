package com.kairos.dto.activity.break_settings;

import javax.validation.constraints.Min;
import java.math.BigInteger;

public class BreakSettingsDTO {
    private BigInteger id;
    private Long unitId;
    @Min(value = 1, message = "error.breakSettings.shiftDuration.must.greaterThanZero")
    private Long shiftDurationInMinute;
    @Min(value = 1, message = "error.breakSettings.breakDuration.must.greaterThanZero")
    private Long breakDurationInMinute;
    private Long numberOfBreaks;
    private BigInteger paidActivityId;
    private BigInteger unpaidActivityId;

    public BreakSettingsDTO() {
        //Default Constructor
    }

    public BreakSettingsDTO(Long unitId, Long shiftDurationInMinute, Long breakDurationInMinute, Long numberOfBreaks) {
        this.unitId = unitId;
        this.shiftDurationInMinute = shiftDurationInMinute;
        this.breakDurationInMinute = breakDurationInMinute;
        this.numberOfBreaks = numberOfBreaks;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Long getShiftDurationInMinute() {
        return shiftDurationInMinute;
    }

    public void setShiftDurationInMinute(Long shiftDurationInMinute) {
        this.shiftDurationInMinute = shiftDurationInMinute;
    }

    public Long getBreakDurationInMinute() {
        return breakDurationInMinute;
    }

    public void setBreakDurationInMinute(Long breakDurationInMinute) {
        this.breakDurationInMinute = breakDurationInMinute;
    }

    public Long getNumberOfBreaks() {
        return numberOfBreaks;
    }

    public void setNumberOfBreaks(Long numberOfBreaks) {
        this.numberOfBreaks = numberOfBreaks;
    }

    public BigInteger getPaidActivityId() {
        return paidActivityId;
    }

    public void setPaidActivityId(BigInteger paidActivityId) {
        this.paidActivityId = paidActivityId;
    }

    public BigInteger getUnpaidActivityId() {
        return unpaidActivityId;
    }

    public void setUnpaidActivityId(BigInteger unpaidActivityId) {
        this.unpaidActivityId = unpaidActivityId;
    }
}
