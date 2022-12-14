package com.kairos.persistence.repository.phase;

import com.kairos.dto.activity.phase.PhaseDTO;
import com.kairos.enums.phase.PhaseDefaultName;
import com.kairos.persistence.model.phase.Phase;
import org.springframework.data.domain.Sort;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by vipul on 26/9/17.
 */
public interface CustomPhaseMongoRepository {
     List<PhaseDTO> getPlanningPhasesByUnit(Long unitId, Sort.Direction direction);

     List<PhaseDTO> getPhasesByUnit(Long unitId, Sort.Direction direction);
     List<PhaseDTO> getPhasesByCountryId(Long countryId, Sort.Direction direction);
     List<PhaseDTO> getApplicablePlanningPhasesByUnit(Long unitId, Sort.Direction direction);
     List<PhaseDTO> getApplicablePlanningPhasesByUnitIds(List<Long> unitIds, Sort.Direction direction);
     List<PhaseDTO> getActualPhasesByUnit(Long unitId);
     Boolean checkPhaseByPhaseIdAndPhaseEnum(BigInteger phaseId, PhaseDefaultName phaseEnum);
     List<PhaseDTO> getNextApplicablePhasesOfUnitBySequence(Long unitId, int sequence);
     List<Phase> getPlanningPhasesByUnit(Long unitId);
}
