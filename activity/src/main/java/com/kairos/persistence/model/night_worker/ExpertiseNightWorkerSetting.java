package com.kairos.persistence.model.night_worker;

import com.kairos.dto.activity.counter.enums.XAxisConfig;
import com.kairos.dto.user.country.time_slot.TimeSlot;
import com.kairos.enums.DurationType;
import com.kairos.persistence.model.common.MongoBaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ExpertiseNightWorkerSetting extends MongoBaseEntity implements Serializable {

    private static final long serialVersionUID = -351941897142204264L;
    private TimeSlot timeSlot;
    private int minMinutesToCheckNightShift;
    private DurationType intervalUnitToCheckNightWorker;
    private int intervalValueToCheckNightWorker;
    private int minShiftsValueToCheckNightWorker;
    private XAxisConfig minShiftsUnitToCheckNightWorker;
    private Long countryId;
    private Long unitId;
    private Long expertiseId;

    public ExpertiseNightWorkerSetting(){
        // default constructor
    }

    public ExpertiseNightWorkerSetting(TimeSlot timeSlot, Integer minMinutesToCheckNightShift, DurationType intervalUnitToCheckNightWorker, Integer intervalValueToCheckNightWorker,
                                       Integer minShiftsValueToCheckNightWorker, XAxisConfig minShiftsUnitToCheckNightWorker, Long countryId, Long expertiseId){
        this.timeSlot = timeSlot;
        this.minMinutesToCheckNightShift = minMinutesToCheckNightShift;
        this.intervalUnitToCheckNightWorker = intervalUnitToCheckNightWorker;
        this.intervalValueToCheckNightWorker = intervalValueToCheckNightWorker;
        this.minShiftsValueToCheckNightWorker = minShiftsValueToCheckNightWorker;
        this.minShiftsUnitToCheckNightWorker = minShiftsUnitToCheckNightWorker;
        this.countryId = countryId;
        this.expertiseId = expertiseId;
    }

    public ExpertiseNightWorkerSetting(Long unitId, Long expertiseId,TimeSlot timeSlot, Integer minMinutesToCheckNightShift, DurationType intervalUnitToCheckNightWorker, Integer intervalValueToCheckNightWorker, Integer minShiftsValueToCheckNightWorker, XAxisConfig minShiftsUnitToCheckNightWorker) {
        this.timeSlot = timeSlot;
        this.minMinutesToCheckNightShift = minMinutesToCheckNightShift;
        this.intervalUnitToCheckNightWorker = intervalUnitToCheckNightWorker;
        this.intervalValueToCheckNightWorker = intervalValueToCheckNightWorker;
        this.minShiftsValueToCheckNightWorker = minShiftsValueToCheckNightWorker;
        this.minShiftsUnitToCheckNightWorker = minShiftsUnitToCheckNightWorker;
        this.unitId = unitId;
        this.expertiseId = expertiseId;
    }

    @Override
    public String toString() {
        return "ExpertiseNightWorkerSetting{" +
                "timeSlot=" + timeSlot +
                ", minMinutesToCheckNightShift=" + minMinutesToCheckNightShift +
                ", intervalUnitToCheckNightWorker=" + intervalUnitToCheckNightWorker +
                ", intervalValueToCheckNightWorker=" + intervalValueToCheckNightWorker +
                ", minShiftsValueToCheckNightWorker=" + minShiftsValueToCheckNightWorker +
                ", minShiftsUnitToCheckNightWorker=" + minShiftsUnitToCheckNightWorker +
                ", countryId=" + countryId +
                ", unitId=" + unitId +
                ", expertiseId=" + expertiseId +
                ", id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deleted=" + deleted +
                '}';
    }
}
