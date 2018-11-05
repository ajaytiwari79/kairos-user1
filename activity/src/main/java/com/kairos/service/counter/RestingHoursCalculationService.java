package com.kairos.service.counter;

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.dto.activity.counter.chart.DataUnit;
import com.kairos.dto.activity.counter.data.FilterCriteria;
import com.kairos.dto.activity.counter.data.FilterCriteriaDTO;
import com.kairos.dto.activity.counter.data.RawRepresentationData;
import com.kairos.dto.activity.counter.enums.DisplayUnit;
import com.kairos.dto.activity.counter.enums.RepresentationUnit;
import com.kairos.enums.FilterType;
import com.kairos.enums.TimeTypes;
import com.kairos.persistence.model.counter.KPI;
import com.kairos.persistence.model.shift.Shift;
import com.kairos.persistence.repository.shift.ShiftMongoRepository;
import com.kairos.service.activity.ActivityService;
import com.kairos.service.activity.TimeTypeService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RestingHoursCalculationService implements CounterService {
    @Inject
    private TimeTypeService timeTypeService;
    @Inject
    private ShiftMongoRepository shiftMongoRepository;
    @Inject
    private RepresentationService representationService;
    @Inject
    private ActivityService activityService;

    public double getTotalRestingHours(List<Shift> shifts, long initTs, long endTs, long restingHoursMillis, boolean dayOffAllowed) {
        //all shifts should be sorted on startDate
        Map<Long, Integer> shiftDayCollisionMap = new HashMap<>();
        long baseInitTs = initTs;
        long durationMillis = endTs - initTs;
        DateTimeInterval dateTimeInterval = new DateTimeInterval(initTs, endTs);
        long totalrestingMinutes = dateTimeInterval.getMilliSeconds();
        for (Shift shift : shifts) {
            DateTimeInterval shiftInterval = new DateTimeInterval(shift.getStartDate(), shift.getEndDate());
            if (dateTimeInterval.overlaps(shiftInterval)) {
                totalrestingMinutes -= dateTimeInterval.overlap(shiftInterval).getMilliSeconds();
            }
        }
        return totalrestingMinutes;
    }

    public Map<Long, Double> calculateRestingHours(List<Long> staffIds, Long countryId, Date startDate, Date endDate) {
        Map<Long, Double> staffRestingHours = new HashMap<>();
        List<BigInteger> activityIds = getPresenceTimeTypeActivitiesIds(countryId);
        List<Shift> shifts = shiftMongoRepository.findAllShiftsByStaffIdsAndDate(staffIds, activityIds, startDate, endDate);
        Map<Long, List<Shift>> staffShiftMapping = shifts.parallelStream().collect(Collectors.groupingBy(shift -> shift.getStaffId(), Collectors.toList()));
        staffIds.stream().forEach(staffId -> {
            Double restingHours = getTotalRestingHours(shifts, startDate.getTime(), endDate.getTime(), 0, false);
            staffRestingHours.put(staffId, restingHours);
        });
        return staffRestingHours;
    }

    private List<BigInteger> getPresenceTimeTypeActivitiesIds(Long countryId) {
        List supportedTimeTypeIdList = timeTypeService.getTimeTypesByTimeTypesAndByCountryId(countryId, TimeTypes.WORKING_TYPE);
        return activityService.getActivitiesIdByTimeTypes(supportedTimeTypeIdList);
    }

    private void getCalculatedDataUnits(List<Long> staffIds){

    }

    private void setShiftDayCollisionMap(long shiftCornerTs, long initTs, Map<Long, Integer> shiftDayCollisionMap) {
        if ((shiftCornerTs) > initTs) {
            long key = (shiftCornerTs - initTs) / (24 * 3600 * 1000);
            if (shiftDayCollisionMap.get(key) == null) {
                shiftDayCollisionMap.put(key, 0);
            }
            shiftDayCollisionMap.put(key, shiftDayCollisionMap.get(key) + 1);
        }
    }

    private List<DataUnit> getDataList(FilterCriteriaDTO filterCriteria) {
        Map<FilterType, List> filterTypeListMap = new HashMap<>();
        for (FilterCriteria criteria : filterCriteria.getFilters()) {
            filterTypeListMap.put(criteria.getType(), criteria.getValues());
        }
        List staffIds = filterTypeListMap.get(FilterType.STAFF_IDS);
        List dates = filterTypeListMap.get(FilterType.TIME_INTERVAL);
        Map<Long, Double> staffRestingHours = calculateRestingHours(staffIds, filterCriteria.getCurrentCountryId(), (Date) dates.get(0), (Date) dates.get(1));
        List<DataUnit> dataList = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : staffRestingHours.entrySet()) {
            dataList.add(new DataUnit(entry.getKey() + "", entry.getValue()));
        }
        return dataList;
    }

    @Override
    public RawRepresentationData getCalculatedCounter(FilterCriteriaDTO filterCriteria, KPI kpi) {
        List<DataUnit> dataList = getDataList(filterCriteria);
        return new RawRepresentationData(kpi.getId(), kpi.getTitle(), kpi.getChart().getType(), DisplayUnit.HOURS, RepresentationUnit.DECIMAL, dataList);
    }

    @Override
    public RawRepresentationData getCalculatedKPI(FilterCriteriaDTO filterCriteriaDTO, KPI kpi) {
        List<DataUnit> dataList = getDataList(filterCriteriaDTO);
        return new RawRepresentationData(kpi.getId(), kpi.getTitle(), kpi.getChart().getType(), DisplayUnit.HOURS, RepresentationUnit.DECIMAL, dataList);
    }
}
