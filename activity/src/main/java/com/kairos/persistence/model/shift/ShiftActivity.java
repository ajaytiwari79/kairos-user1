package com.kairos.persistence.model.shift;

import com.kairos.dto.user.reason_code.ReasonCodeDTO;
import com.kairos.enums.shift.ShiftStatus;
import com.kairos.persistence.model.time_bank.TimeBankCTADistribution;

import java.math.BigInteger;
import java.util.*;

/**
 * @author pradeep
 * @date - 10/9/18
 */
public class ShiftActivity {


    private BigInteger activityId;
    private Date startDate;
    private Date endDate;
    private int scheduledMinutes;
    private int durationMinutes;
    private String activityName;
    private long bid;
    private long pId;
    //used in T&A view
    private Long reasonCodeId;
    //used for adding absence type of activities.
    private Long absenceReasonCodeId;
    private String remarks;
    //please don't use this id for any functionality this only for frontend
    private BigInteger id;
    private String timeType;
    private String backgroundColor;
    private boolean haltBreak;
    private BigInteger plannedTimeId;
    private boolean breakShift;
    private boolean breakReplaced;
    private List<TimeBankCTADistribution> timeBankCTADistributions;
    private Long allowedBreakDurationInMinute;

    private Set<ShiftStatus> status = new HashSet<>(Arrays.asList(ShiftStatus.UNPUBLISHED));

    public ShiftActivity() {
    }



    public ShiftActivity( String activityName,Date startDate, Date endDate,BigInteger activityId) {
        this.activityId = activityId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.activityName = activityName;
    }

    public ShiftActivity( String activityName,Date startDate, Date endDate,BigInteger activityId,boolean breakShift,Long absenceReasonCodeId,Long allowedBreakDurationInMinute) {
        this.activityId = activityId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.activityName = activityName;
        this.breakShift=breakShift;
        this.absenceReasonCodeId = absenceReasonCodeId;
        this.allowedBreakDurationInMinute=allowedBreakDurationInMinute;
    }
    public ShiftActivity( String activityName,Date startDate, Date endDate,BigInteger activityId,boolean breakShift,Long absenceReasonCodeId,Long allowedBreakDurationInMinute,boolean breakReplaced) {
        this.activityId = activityId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.activityName = activityName;
        this.breakShift=breakShift;
        this.absenceReasonCodeId = absenceReasonCodeId;
        this.allowedBreakDurationInMinute=allowedBreakDurationInMinute;
        this.breakReplaced=breakReplaced;
    }
    public ShiftActivity(BigInteger activityId, String activityName) {
        this.activityId = activityId;
        this.activityName = activityName;
    }


    public Long getReasonCodeId() {
        return reasonCodeId;
    }

    public void setReasonCodeId(Long reasonCodeId) {
        this.reasonCodeId = reasonCodeId;
    }

    public BigInteger getPlannedTimeId() {
        return plannedTimeId;
    }

    public void setPlannedTimeId(BigInteger plannedTimeId) {
        this.plannedTimeId = plannedTimeId;
    }


    public Set<ShiftStatus> getStatus() {
        return status;
    }

    public void setStatus(Set<ShiftStatus> status) {
        this.status = status;
    }
    public boolean isHaltBreak() {
        return haltBreak;
    }

    public void setHaltBreak(boolean haltBreak) {
        this.haltBreak = haltBreak;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTimeType() {
        return timeType;
    }

    public void setTimeType(String timeType) {
        this.timeType = timeType;
    }


    public long getBid() {
        return bid;
    }

    public void setBid(long bid) {
        this.bid = bid;
    }

    public long getpId() {
        return pId;
    }

    public void setpId(long pId) {
        this.pId = pId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public BigInteger getActivityId() {
        return activityId;
    }

    public void setActivityId(BigInteger activityId) {
        this.activityId = activityId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getScheduledMinutes() {
        return scheduledMinutes;
    }

    public void setScheduledMinutes(int scheduledMinutes) {
        this.scheduledMinutes = scheduledMinutes;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public boolean isBreakShift() {
        return breakShift;
    }

    public void setBreakShift(boolean breakShift) {
        this.breakShift = breakShift;
    }

    public Long getAbsenceReasonCodeId() {
        return absenceReasonCodeId;
    }

    public void setAbsenceReasonCodeId(Long absenceReasonCodeId) {
        this.absenceReasonCodeId = absenceReasonCodeId;
    }

    public List<TimeBankCTADistribution> getTimeBankCTADistributions() {
        return timeBankCTADistributions;
    }

    public void setTimeBankCTADistributions(List<TimeBankCTADistribution> timeBankCTADistributions) {
        this.timeBankCTADistributions = timeBankCTADistributions;
    }

    public boolean isBreakReplaced() {
        return breakReplaced;
    }

    public void setBreakReplaced(boolean breakReplaced) {
        this.breakReplaced = breakReplaced;
    }

    public Long getAllowedBreakDurationInMinute() {
        return allowedBreakDurationInMinute;
    }

    public void setAllowedBreakDurationInMinute(Long allowedBreakDurationInMinute) {
        this.allowedBreakDurationInMinute = allowedBreakDurationInMinute;
    }
}
