package com.kairos.response.dto.web.shift;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalTime;
import java.util.*;


public class IndividualShiftTemplateDTO {
    private BigInteger id;
    private String name;
    private String remarks;
    @Range(min = 0)
    @NotNull(message = "error.shiftTemplate.activityId.notnull")
    private BigInteger activityId;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    private boolean mainShift;
    private List<IndividualShiftTemplateDTO> subShifts;

    public IndividualShiftTemplateDTO() {
        //Default Constructor
    }

    public IndividualShiftTemplateDTO(BigInteger id, String name, String remarks, @Range(min = 0) @NotNull(message = "error.shiftTemplate.activityId.notnull") BigInteger activityId
                               , LocalTime startTime, LocalTime endTime,
                                      List<IndividualShiftTemplateDTO> subShifts) {
        this.id = id;
        this.name = name;
        this.remarks = remarks;
        this.activityId = activityId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subShifts = subShifts;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public BigInteger getActivityId() {
        return activityId;
    }

    public void setActivityId(BigInteger activityId) {
        this.activityId = activityId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public List<IndividualShiftTemplateDTO> getSubShifts() {
        return subShifts=Optional.ofNullable(subShifts).orElse(new ArrayList<>());
    }

    public void setSubShifts(List<IndividualShiftTemplateDTO> subShifts) {
        this.subShifts = subShifts;
    }

    public boolean isMainShift() {
        return mainShift;
    }

    public void setMainShift(boolean mainShift) {
        this.mainShift = mainShift;
    }
}
