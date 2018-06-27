package com.kairos.activity.persistence.repository.unit_settings;

import com.kairos.activity.persistence.model.unit_settings.ActivityConfiguration;
import com.kairos.activity.unit_settings.activity_configuration.ActivityConfigurationDTO;

import java.math.BigInteger;
import java.util.List;

public interface CustomActivityConfigurationRepository {
    ActivityConfiguration findAbsenceConfigurationByUnitIdAndPhaseId(Long unitId, BigInteger phaseId, Boolean exception);

    ActivityConfiguration findPresenceConfigurationByUnitIdAndPhaseId(Long unitId, BigInteger phaseId);

    List<ActivityConfigurationDTO> findPresenceConfigurationByUnitId(Long unitId);

    List<ActivityConfigurationDTO> findAbsenceConfigurationByUnitId(Long unitId);

    List<ActivityConfiguration> findAllAbsenceConfigurationByUnitIdAndPhaseId(Long unitId, BigInteger phaseId);

    List<ActivityConfiguration> findAllPresenceConfigurationByUnitIdAndPhaseId(Long unitId, BigInteger phaseId);

}
