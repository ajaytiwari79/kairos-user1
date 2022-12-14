package com.kairos.persistence.model.staffing_level;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.dto.activity.staffing_level.StaffingLevelInterval;
import com.kairos.dto.activity.staffing_level.StaffingLevelSetting;
import com.kairos.dto.activity.staffing_level.presence.StaffingLevelActivityDetails;
import com.kairos.persistence.model.common.MongoBaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "staffing_level")
@Getter
@Setter
@NoArgsConstructor
public class StaffingLevel extends MongoBaseEntity {
    private static final long serialVersionUID = 9078681960018781377L;
    @Indexed
    private Date currentDate;
    private Integer weekCount;
    @Indexed
    private Long unitId;
    private BigInteger phaseId;
    private StaffingLevelSetting staffingLevelSetting;
    private List<StaffingLevelInterval> presenceStaffingLevelInterval = new ArrayList<>();
    private List<StaffingLevelInterval> absenceStaffingLevelInterval = new ArrayList<>();
    private Set<StaffingLevelActivityDetails> staffingLevelActivityDetails =new LinkedHashSet<>();

    public StaffingLevel(Date currentDate, Integer weekCount,
                         Long organizationId, BigInteger phaseId, StaffingLevelSetting staffingLevelSetting) {
        this.currentDate = currentDate;
        this.weekCount = weekCount;
        this.unitId = organizationId;
        this.phaseId = phaseId;
        this.staffingLevelSetting = staffingLevelSetting;
    }

    public StaffingLevel(Date currentDate, int weekCount,
                         Long organizationId, BigInteger phaseId) {
        this.currentDate = currentDate;
        this.weekCount = weekCount;
        this.unitId = organizationId;
        this.phaseId = phaseId;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date getCurrentDate() {
        return currentDate;
    }


    public void addStaffingLevelTimeSlot(Set<StaffingLevelInterval> staffingLevelTimeSlots) {
        if (staffingLevelTimeSlots == null)
            throw new NullPointerException("Can't add null staffLevelActivity");

        this.getPresenceStaffingLevelInterval().addAll(staffingLevelTimeSlots);

    }

    public StaffingLevelSetting getStaffingLevelSetting() {
        return staffingLevelSetting = staffingLevelSetting == null ? new StaffingLevelSetting() : staffingLevelSetting;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof StaffingLevel)) return false;

        StaffingLevel that = (StaffingLevel) o;

        return new EqualsBuilder()
                .append(currentDate, that.currentDate)
                .append(weekCount, that.weekCount)
                .append(unitId, that.unitId)
                .append(phaseId, that.phaseId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(currentDate)
                .append(weekCount)
                .append(unitId)
                .append(phaseId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("currentDate", currentDate)
                .append("weekCount", weekCount)
                .append("unitId", unitId)
                .append("phaseId", phaseId)
                .append("staffingLevelSetting", staffingLevelSetting)
                .append("presenceStaffingLevelInterval", presenceStaffingLevelInterval)
                .toString();
    }

    public enum Type {
        PRESENCE, ABSENCE
    }
}
