package com.kairos.dto.staffing_level;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.activity.staffing_level.StaffingLevelSetting;
//import com.kairos.activity.util.validator.ExistingStaffingLevel;
//import org.springframework.format.annotation.DateTimeFormat;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PresenceStaffingLevelDto {
    private BigInteger id;
    @NotNull
    private Long phaseId;
    //@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    //@ExistingStaffingLevel
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date currentDate;
    private Long weekCount;
    private StaffingLevelSetting staffingLevelSetting;
    private List<StaffingLevelTimeSlotDTO> presenceStaffingLevelInterval =new ArrayList<>();

    public PresenceStaffingLevelDto() {
        //default constructor
    }

    public PresenceStaffingLevelDto(Long phaseId, Date currentDate, Long weekCount,
                                    StaffingLevelSetting staffingLevelSetting) {
        this.phaseId = phaseId;
        this.currentDate = currentDate;
        this.weekCount = weekCount;
        this.staffingLevelSetting = staffingLevelSetting;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Long getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(Long phaseId) {
        this.phaseId = phaseId;
    }
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public Long getWeekCount() {
        return weekCount;
    }

    public void setWeekCount(Long weekCount) {
        this.weekCount = weekCount;
    }

    public StaffingLevelSetting getStaffingLevelSetting() {
        return staffingLevelSetting;
    }

    public void setStaffingLevelSetting(StaffingLevelSetting staffingLevelSetting) {
        this.staffingLevelSetting = staffingLevelSetting;
    }

    public List<StaffingLevelTimeSlotDTO> getPresenceStaffingLevelInterval() {
        return presenceStaffingLevelInterval;
    }

    public void setPresenceStaffingLevelInterval(List<StaffingLevelTimeSlotDTO> presenceStaffingLevelInterval) {
        this.presenceStaffingLevelInterval = presenceStaffingLevelInterval;
    }




}
