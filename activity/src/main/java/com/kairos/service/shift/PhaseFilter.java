package com.kairos.service.shift;

import com.kairos.dto.activity.shift.ShiftDTO;
import com.kairos.enums.FilterType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.isCollectionNotEmpty;
import static com.kairos.enums.FilterType.PHASE;

public class PhaseFilter <G> implements ShiftFilter {

    private Map<FilterType, Set<G>> filterCriteriaMap;

    public PhaseFilter(Map<FilterType, Set<G>> filterCriteriaMap) {
        this.filterCriteriaMap = filterCriteriaMap;
    }

    @Override
    public <T extends ShiftDTO> List<T> meetCriteria(List<T> shiftDTOS) {
        boolean validFilter = filterCriteriaMap.containsKey(PHASE) && isCollectionNotEmpty(filterCriteriaMap.get(PHASE));
        List<T> filteredShifts = validFilter ? new ArrayList<>() : shiftDTOS;
        if(validFilter){
            Set<BigInteger> phaseIds = filterCriteriaMap.get(PHASE).stream().map(s -> new BigInteger(s.toString())).collect(Collectors.toSet());
            for (ShiftDTO shiftDTO : shiftDTOS) {
                if(phaseIds.contains(shiftDTO.getPhaseId())){
                    filteredShifts.add((T)shiftDTO);
                }
            }

        }
        return filteredShifts;
    }

}
