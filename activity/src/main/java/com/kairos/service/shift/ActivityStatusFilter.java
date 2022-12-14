package com.kairos.service.shift;

import com.kairos.dto.activity.shift.ShiftDTO;
import com.kairos.enums.FilterType;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.isCollectionNotEmpty;
import static com.kairos.commons.utils.ObjectUtils.isNotNull;
import static com.kairos.enums.FilterType.ACTIVITY_STATUS;

/**
 * Created by pradeep
 * Created at 30/6/19
 **/

public class ActivityStatusFilter <G> implements ShiftFilter {

    private Map<FilterType, Set<G>> filterCriteriaMap;

    public ActivityStatusFilter(Map<FilterType, Set<G>> filterCriteriaMap) {
        this.filterCriteriaMap = filterCriteriaMap;
    }

    @Override
    public <T extends ShiftDTO> List<T> meetCriteria(List<T> shiftDTOS) {
        boolean validFilter = filterCriteriaMap.containsKey(ACTIVITY_STATUS) && isCollectionNotEmpty(filterCriteriaMap.get(ACTIVITY_STATUS));
        List<T> filteredShifts = validFilter ? new ArrayList<>() : shiftDTOS;
        if(validFilter){
            for (ShiftDTO shiftDTO : shiftDTOS) {
                Set<String> activityStatus = new HashSet<>();
                shiftDTO.getActivities().forEach(shiftActivityDTO -> {
                    activityStatus.addAll(shiftActivityDTO.getStatus().stream().filter(shiftStatus -> isNotNull(shiftStatus)).map(shiftStatus -> shiftStatus.toString()).collect(Collectors.toSet()));
                    shiftActivityDTO.getChildActivities().forEach(childActivityDTO -> activityStatus.addAll(childActivityDTO.getStatus().stream().filter(shiftStatus -> isNotNull(shiftStatus)).map(shiftStatus -> shiftStatus.toString()).collect(Collectors.toSet())));
                });
                if(CollectionUtils.containsAny(filterCriteriaMap.get(ACTIVITY_STATUS),activityStatus)){
                    filteredShifts.add((T)shiftDTO);
                }
            }

        }
        return filteredShifts;
    }

}
