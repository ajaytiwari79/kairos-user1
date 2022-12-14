package com.kairos.shiftplanning.domain.unit;

import com.kairos.dto.activity.unit_settings.activity_configuration.EmploymentWisePlannedTimeConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.math.BigInteger;
import java.util.List;

import static com.kairos.constants.CommonMessageConstants.PLANNED_TIME_CANNOT_EMPTY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresencePlannedTime {
    private BigInteger phaseId;
    @Valid
    private List<EmploymentWisePlannedTimeConfiguration> employmentWisePlannedTimeConfigurations;
    @NotEmpty(message = PLANNED_TIME_CANNOT_EMPTY)
    private List<BigInteger> managementPlannedTimeIds;

    public PresencePlannedTime(BigInteger phaseId, @NotEmpty(message = PLANNED_TIME_CANNOT_EMPTY) List<BigInteger> managementPlannedTimeIds) {
        this.phaseId = phaseId;
        this.managementPlannedTimeIds = managementPlannedTimeIds;
    }
}
