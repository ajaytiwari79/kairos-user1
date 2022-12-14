package com.kairos.dto.activity.staffing_level.absence;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kairos.dto.activity.staffing_level.StaffingLevelActivity;
import com.kairos.dto.activity.staffing_level.StaffingLevelIntervalLog;
import com.kairos.dto.activity.staffing_level.StaffingLevelSetting;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


/**
 * Created by yatharth on 23/4/18.
 */
@Getter
@Setter
@NoArgsConstructor
public class AbsenceStaffingLevelDto {

    BigInteger id;
    private BigInteger phaseId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date currentDate;
    private Integer weekCount;
    private int minNoOfStaff;
    private int maxNoOfStaff;
    private int absentNoOfStaff;
    private Date updatedAt;
    private StaffingLevelSetting staffingLevelSetting;
    private Set<StaffingLevelActivity> staffingLevelActivities=new HashSet<>();
    private Long unitId;
    private TreeSet<StaffingLevelIntervalLog> staffingLevelIntervalLogs=new TreeSet<>();


    public AbsenceStaffingLevelDto(BigInteger id, BigInteger phaseId, Date currentDate, Integer weekCount) {
        this.id = id;
        this.phaseId = phaseId;
        this.currentDate = currentDate;
        this.weekCount = weekCount;
    }
}
