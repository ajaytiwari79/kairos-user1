package com.kairos.persistence.model.shift;

import com.kairos.commons.audit_logging.IgnoreLogging;
import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.dto.activity.shift.PlannedTime;
import com.kairos.enums.shift.ShiftStatus;
import com.kairos.persistence.model.pay_out.PayOutPerShiftCTADistribution;
import com.kairos.persistence.model.time_bank.TimeBankCTADistribution;
import lombok.*;

import java.math.BigInteger;
import java.sql.Time;
import java.util.*;

import static com.kairos.commons.utils.ObjectUtils.isNullOrElse;

/**
 * @author pradeep
 * @date - 10/9/18
 */
@Getter
@Setter
@NoArgsConstructor
public class ShiftActivity implements Comparable<ShiftActivity>{


    private BigInteger activityId;
    private Date startDate;
    private Time startTime;

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
    //please don't use this id for any functionality this on ly for frontend
    private BigInteger id;
    private String timeType;
    private String backgroundColor;
    private boolean breakReplaced;
    private List<TimeBankCTADistribution> timeBankCTADistributions;
    private List<PayOutPerShiftCTADistribution> payoutPerShiftCTADistributions;
    private int payoutCtaBonusMinutes;
    private int timeBankCtaBonusMinutes;
    private String startLocation; // this is for the location from where activity will gets starts
    private String endLocation;   // this is for the location from where activity will gets ends
    private int plannedMinutesOfTimebank;
    private int plannedMinutesOfPayout;
    private int scheduledMinutesOfTimebank;
    private int scheduledMinutesOfPayout;
    private List<PlannedTime> plannedTimes;
    private List<ShiftActivity> childActivities;
    private boolean breakNotHeld;
    private Set<ShiftStatus> status = new HashSet<>();


    @IgnoreLogging
    public DateTimeInterval getInterval() {
        return new DateTimeInterval(this.getStartDate().getTime(), this.getEndDate().getTime());
    }


    public ShiftActivity( String activityName,Date startDate, Date endDate,BigInteger activityId,String timeType) {
        this.activityId = activityId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.activityName = activityName;
        this.timeType = timeType;
    }

    public ShiftActivity(BigInteger activityId, String activityName) {
        this.activityId = activityId;
        this.activityName = activityName;
    }

    public void setPayoutPerShiftCTADistributions(List<PayOutPerShiftCTADistribution> payoutPerShiftCTADistributions) {
        this.payoutPerShiftCTADistributions = isNullOrElse(payoutPerShiftCTADistributions,new ArrayList<>());
    }

    public List<PlannedTime> getPlannedTimes() {
        return plannedTimes=Optional.ofNullable(plannedTimes).orElse(new ArrayList<>());
    }

    public List<ShiftActivity> getChildActivities() {
        return this.childActivities = isNullOrElse(this.childActivities,new ArrayList<>());
    }

    public void setChildActivities(List<ShiftActivity> childActivities) {
        this.childActivities = isNullOrElse(childActivities,new ArrayList<>());
    }

    @Override
    public int compareTo(ShiftActivity shiftActivity) {
        return this.startDate.compareTo(shiftActivity.startDate);
    }
}
