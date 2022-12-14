package com.kairos.service.shift;

import com.kairos.dto.activity.shift.ShiftDTO;
import com.kairos.enums.FilterType;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.kairos.commons.utils.ObjectUtils.isCollectionNotEmpty;
import static com.kairos.commons.utils.ObjectUtils.isNotNull;
import static com.kairos.enums.FilterType.ACTIVITY_TIMECALCULATION_TYPE;

/**
 * Created by pradeep
 * Created at 30/6/19
 **/

public class ActivityTimeCalculationTypeFilter <G> implements ShiftFilter {

    private Map<FilterType, Set<G>> filterCriteriaMap;

    public ActivityTimeCalculationTypeFilter(Map<FilterType, Set<G>> filterCriteriaMap) {
        this.filterCriteriaMap = filterCriteriaMap;
    }

    @Override
    public <T extends ShiftDTO> List<T> meetCriteria(List<T> shiftDTOS) {
        boolean validFilter = filterCriteriaMap.containsKey(ACTIVITY_TIMECALCULATION_TYPE) && isCollectionNotEmpty(filterCriteriaMap.get(ACTIVITY_TIMECALCULATION_TYPE));
        List<T> filteredShifts = validFilter ? new ArrayList<>() : shiftDTOS;
        if(validFilter){
            for (ShiftDTO shiftDTO : shiftDTOS) {
                Set<String> methodForCalulation = new HashSet<>();
                shiftDTO.getActivities().forEach(shiftActivityDTO -> {
                    methodForCalulation.add(shiftActivityDTO.getActivity().getActivityTimeCalculationSettings().getMethodForCalculatingTime());
                    shiftActivityDTO.getChildActivities().forEach(childActivityDTO -> {
                        if(isNotNull(childActivityDTO.getActivity())) {
                            methodForCalulation.add(childActivityDTO.getActivity().getActivityTimeCalculationSettings().getMethodForCalculatingTime());
                        }
                    });
                });
                if (CollectionUtils.containsAny(filterCriteriaMap.get(ACTIVITY_TIMECALCULATION_TYPE), methodForCalulation)) {
                    filteredShifts.add((T)shiftDTO);
                }
            }
        }
        return filteredShifts;
    }
}
