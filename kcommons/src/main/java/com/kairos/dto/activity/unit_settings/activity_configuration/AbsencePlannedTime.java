package com.kairos.dto.activity.unit_settings.activity_configuration;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.math.BigInteger;
import java.util.List;

import static com.kairos.constants.CommonMessageConstants.PLANNED_TIME_CANNOT_EMPTY;

@Getter
@Setter
public class AbsencePlannedTime {
    private BigInteger phaseId;
    private BigInteger timeTypeId;  // if exception is true then time type is null
    @NotEmpty(message = PLANNED_TIME_CANNOT_EMPTY)
    private List<BigInteger> plannedTimeIds;
    private boolean exception;

    public AbsencePlannedTime() {
        // DC
    }


    public AbsencePlannedTime(BigInteger phaseId,List<BigInteger> plannedTimeIds, boolean exception) {
        this.phaseId = phaseId;
        this.plannedTimeIds = plannedTimeIds;
        this.exception = exception;
    }

    public AbsencePlannedTime(BigInteger phaseId, BigInteger timeTypeId, List<BigInteger> plannedTimeIds, boolean exception) {
        this.phaseId = phaseId;
        this.timeTypeId = timeTypeId;
        this.plannedTimeIds = plannedTimeIds;
        this.exception = exception;
    }
}
