package com.kairos.service.counter;

import com.kairos.dto.activity.counter.data.CommonRepresentationData;
import com.kairos.dto.activity.counter.enums.CounterType;
import com.kairos.dto.activity.kpi.KPISetResponseDTO;
import com.kairos.dto.activity.kpi.StaffKpiFilterDTO;
import com.kairos.dto.activity.shift.ShiftDTO;
import com.kairos.enums.FilterType;
import com.kairos.enums.kpi.Direction;
import com.kairos.persistence.model.counter.*;
import com.kairos.persistence.model.shift.Shift;
import com.kairos.persistence.repository.counter.CounterRepository;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/*
 * @author: mohit.shakya@oodlestechnologies.com
 * @dated: Jun/26/2018
 */

@Service
public class ScheduledHoursService implements CounterService {

    @Inject
    CounterRepository counterRepository;
    @Inject
    CounterFilterService counterFilterService;

    private Map getApplicableCriteria(Map availableCriteria){
        Counter counter = counterRepository.getCounterByType(CounterType.SCHEDULED_HOURS_NET);
        Map<FilterType, List> applicableCriteria = getApplicableFilters(counter.getCriteriaList(), availableCriteria);
        return applicableCriteria;
    }

    //TODO: Implimantation is pending as functionality chnaged.
    private int calculateData(Map applicableCriteria){
        List<AggregationOperation> operations = counterFilterService.getShiftFilterCriteria(applicableCriteria);
        List<Shift> shifts = counterRepository.getMappedValues(operations, Shift.class, ShiftDTO.class);
        //shifts.stream().map((shift.getEndDate().getTime(),shift.getStartDate().getTime()) -> }).collect(Collectors.toList())

        shifts.forEach(shift -> { });
        return 0;
    }

    @Override
    public CommonRepresentationData getCalculatedCounter(Map<FilterType, List> filterBasedCriteria, Long countryId, KPI kpi) {
        return null;
    }

    @Override
    public CommonRepresentationData getCalculatedKPI(Map<FilterType, List> filterBasedCriteria, Long countryId, KPI kpi,ApplicableKPI applicableKPI) {
        return null;
    }

    @Override
    public TreeSet<FibonacciKPICalculation> getFibonacciCalculatedCounter(Map<FilterType, List> filterBasedCriteria, Long organizationId, Direction sortingOrder, List<StaffKpiFilterDTO> staffKpiFilterDTOS, ApplicableKPI applicableKPI){
        return new TreeSet<>();
    }

    @Override
    public KPISetResponseDTO getCalculatedDataOfKPI(Map<FilterType, List> filterBasedCriteria, Long organizationId, KPI kpi, ApplicableKPI applicableKPI){
        Map<Object,Object> objectMap = new HashedMap();
        KPISetResponseDTO kpiSetResponseDTO = new KPISetResponseDTO();
        return kpiSetResponseDTO;

    }


}
