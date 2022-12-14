package com.kairos.service;

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectUtils;
import com.kairos.constants.AppConstants;
import com.kairos.dto.activity.counter.chart.ClusteredBarChartKpiDataUnit;
import com.kairos.dto.activity.counter.chart.CommonKpiDataUnit;
import com.kairos.dto.activity.counter.data.CommonRepresentationData;
import com.kairos.dto.activity.counter.data.KPIAxisData;
import com.kairos.dto.activity.counter.data.KPIRepresentationData;
import com.kairos.dto.activity.counter.enums.RepresentationUnit;
import com.kairos.dto.activity.counter.enums.XAxisConfig;
import com.kairos.dto.activity.kpi.KPISetResponseDTO;
import com.kairos.dto.activity.kpi.StaffKpiFilterDTO;
import com.kairos.dto.activity.shift.ShiftActivityDTO;
import com.kairos.dto.activity.shift.ShiftDTO;
import com.kairos.dto.activity.shift.ShiftWithActivityDTO;
import com.kairos.enums.DurationType;
import com.kairos.enums.FilterType;
import com.kairos.enums.kpi.Direction;
import com.kairos.persistence.model.ApplicableKPI;
import com.kairos.persistence.model.FibonacciKPICalculation;
import com.kairos.persistence.model.KPI;
import com.kairos.persistence.repository.counter.CounterHelperRepository;
import com.kairos.utils.FibonacciCalculationUtil;
import com.kairos.utils.KPIUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.enums.kpi.KPIRepresentation.*;

@Service
public class ShiftAndActivityDurationKpiService implements CounterService {
    @Inject
    private CounterHelperService counterHelperService;
    @Inject private CounterHelperRepository counterHelperRepository;

    private List<CommonKpiDataUnit> getDurationOfShiftAndActivity(Long organizationId, Map<FilterType, List> filterBasedCriteria, ApplicableKPI applicableKPI) {
        List<CommonKpiDataUnit> kpiDataUnits = new ArrayList<>();
        Object[] filterCriteria = counterHelperService.getDataByFilterCriteria(filterBasedCriteria);
        List<Long> staffIds = (List<Long>)filterCriteria[0];
        List<LocalDate> filterDates = (List<LocalDate>)filterCriteria[1];
        List<Long> unitIds = (List<Long>)filterCriteria[2];
        List<Long> employmentTypeIds = (List<Long>)filterCriteria[3];
        Set<DayOfWeek> daysOfWeeks = (Set<DayOfWeek>)filterCriteria[4];
        List<BigInteger> activitiesIds = KPIUtils.getBigIntegerValue(filterBasedCriteria.getOrDefault(FilterType.ACTIVITY_IDS, new ArrayList<>()));
        Object[] kpiData = counterHelperService.getKPIdata(new HashMap(),applicableKPI,filterDates,staffIds,employmentTypeIds,unitIds,organizationId);
        List<DateTimeInterval> dateTimeIntervals = (List<DateTimeInterval>)kpiData[1];
        List<StaffKpiFilterDTO> staffKpiFilterDTOS = (List<StaffKpiFilterDTO>)kpiData[0];
        staffIds = (List<Long>) kpiData[2];
        List<Integer> dayOfWeeksNo = new ArrayList<>();
        daysOfWeeks.forEach(dayOfWeek -> dayOfWeeksNo.add((dayOfWeek.getValue() < 7) ? dayOfWeek.getValue() + 1 : 1));
        List<ShiftWithActivityDTO> shifts = counterHelperRepository.findShiftsByShiftAndActvityKpiFilters(staffIds, ObjectUtils.isCollectionNotEmpty(unitIds) ? unitIds : Arrays.asList(organizationId), activitiesIds, dayOfWeeksNo, dateTimeIntervals.get(0).getStartDate(), dateTimeIntervals.get(dateTimeIntervals.size() - 1).getEndDate(),null);
        Map<DateTimeInterval, List<ShiftWithActivityDTO>> dateTimeIntervalListMap = new HashMap<>();
        for (DateTimeInterval dateTimeInterval : dateTimeIntervals) {
            dateTimeIntervalListMap.put(dateTimeInterval, shifts.stream().filter(shift -> dateTimeInterval.contains(shift.getStartDate())).collect(Collectors.toList()));
        }
        Map<Object, List<ClusteredBarChartKpiDataUnit>> objectDoubleMap = calculateDataByKpiRepresentation(staffIds, dateTimeIntervalListMap, dateTimeIntervals, applicableKPI, shifts);
        KPIUtils.getKpiDataUnits(objectDoubleMap, kpiDataUnits, applicableKPI, staffKpiFilterDTOS);
        KPIUtils.sortKpiDataByDateTimeInterval(kpiDataUnits);
        return kpiDataUnits;
    }

    @Override
    public CommonRepresentationData getCalculatedCounter(Map<FilterType, List> filterBasedCriteria, Long organizationId, KPI kpi) {
        List<CommonKpiDataUnit> dataList = getDurationOfShiftAndActivity(organizationId, filterBasedCriteria, null);
        return new KPIRepresentationData(kpi.getId(), kpi.getTitle(), kpi.getChart(), XAxisConfig.HOURS, RepresentationUnit.DECIMAL, dataList, new KPIAxisData(AppConstants.DATE, AppConstants.LABEL), new KPIAxisData(AppConstants.HOURS, AppConstants.VALUE_FIELD));
    }

    @Override
    public CommonRepresentationData getCalculatedKPI(Map<FilterType, List> filterBasedCriteria, Long organizationId, KPI kpi, ApplicableKPI applicableKPI) {
        List<CommonKpiDataUnit> dataList = getDurationOfShiftAndActivity(organizationId, filterBasedCriteria, applicableKPI);
        return new KPIRepresentationData(kpi.getId(), kpi.getTitle(), kpi.getChart(), XAxisConfig.HOURS, RepresentationUnit.DECIMAL, dataList, new KPIAxisData(applicableKPI.getKpiRepresentation().equals(REPRESENT_PER_STAFF) ? AppConstants.STAFF :AppConstants.DATE, AppConstants.LABEL), new KPIAxisData(AppConstants.HOURS, AppConstants.VALUE_FIELD));
    }

    @Override
    public TreeSet<FibonacciKPICalculation> getFibonacciCalculatedCounter(Map<FilterType, List> filterBasedCriteria, Long organizationId, Direction sortingOrder,List<StaffKpiFilterDTO> staffKpiFilterDTOS,KPI kpi,ApplicableKPI applicableKPI) {
        Object[] filterCriteria = counterHelperService.getDataByFilterCriteria(filterBasedCriteria);
        List<Long> staffIds = (List<Long>)filterCriteria[0];
        List<LocalDate> filterDates = (List<LocalDate>)filterCriteria[1];
        Object[] kpiData = counterHelperService.getKPIdata(new HashMap(),applicableKPI,filterDates,staffIds, ObjectUtils.newArrayList(), ObjectUtils.newArrayList(organizationId),organizationId);
        List<DateTimeInterval> dateTimeIntervals = (List<DateTimeInterval>)kpiData[1];
        List<ShiftWithActivityDTO> shifts = counterHelperRepository.findShiftsByShiftAndActvityKpiFilters(staffIds, ObjectUtils.newArrayList(organizationId), ObjectUtils.newArrayList(), ObjectUtils.newArrayList(), dateTimeIntervals.get(0).getStartDate(), dateTimeIntervals.get(dateTimeIntervals.size() - 1).getEndDate(),null);
        Map<Long,Integer> staffIdAndDurationMap = getStaffAndShiftDurationMap(shifts);
        return FibonacciCalculationUtil.getFibonacciCalculation(staffIdAndDurationMap,sortingOrder);
    }



    private Map<Object, List<ClusteredBarChartKpiDataUnit>> calculateDataByKpiRepresentation(List<Long> staffIds, Map<DateTimeInterval, List<ShiftWithActivityDTO>> dateTimeIntervalListMap, List<DateTimeInterval> dateTimeIntervals,  ApplicableKPI applicableKPI, List<ShiftWithActivityDTO> shifts) {
        Map<Object, List<ClusteredBarChartKpiDataUnit>> staffIdAndShiftAndActivityDurationMap;
        Map<String, Integer> activityNameAndTotalDurationMinutesMap = new HashMap<>();
        Integer shiftDurationMinutes = 0;
        Map<String, String> activityNameAndColorCodeMap = new HashMap<>();
        switch (applicableKPI.getKpiRepresentation()) {
            case REPRESENT_PER_STAFF:
                staffIdAndShiftAndActivityDurationMap = getShiftAndActivityByRepresentPerStaff(staffIds, shifts, activityNameAndColorCodeMap);
                break;
            case REPRESENT_TOTAL_DATA:
                staffIdAndShiftAndActivityDurationMap = getShiftAndActivityByRepresentTotalData(dateTimeIntervals, shifts, activityNameAndTotalDurationMinutesMap, shiftDurationMinutes, activityNameAndColorCodeMap);
                break;
            default:
                staffIdAndShiftAndActivityDurationMap = getShiftAndActivityByRepresentPerInterval(dateTimeIntervalListMap, dateTimeIntervals, activityNameAndColorCodeMap , applicableKPI.getFrequencyType());
                break;
        }
        return KPIUtils.verifyKPIResponseListData(staffIdAndShiftAndActivityDurationMap) ? staffIdAndShiftAndActivityDurationMap : new HashMap<>();
    }



    private Map<Object, List<ClusteredBarChartKpiDataUnit>> getShiftAndActivityByRepresentPerInterval(Map<DateTimeInterval, List<ShiftWithActivityDTO>> dateTimeIntervalListMap, List<DateTimeInterval> dateTimeIntervals, Map<String, String> activityNameAndColorCodeMap, DurationType frequencyType) {
        Map<Object, List<ClusteredBarChartKpiDataUnit>> staffIdAndShiftAndActivityDurationMap = new HashedMap();
        Map<String, Integer> activityNameAndTotalDurationMinutesMap;
        List<ClusteredBarChartKpiDataUnit> subClusteredBarValue;
        Integer shiftDurationMinutes;
        for (DateTimeInterval dateTimeInterval : dateTimeIntervals) {
            activityNameAndTotalDurationMinutesMap = new HashMap<>();
            subClusteredBarValue = new ArrayList<>();
            shiftDurationMinutes = 0;
            List<ShiftWithActivityDTO> shiftWithActivityDTO = dateTimeIntervalListMap.get(dateTimeInterval);
            if (CollectionUtils.isNotEmpty(shiftWithActivityDTO)) {
                subClusteredBarValue = getShiftAndActivityDurationMap(activityNameAndColorCodeMap, activityNameAndTotalDurationMinutesMap, shiftDurationMinutes,shiftWithActivityDTO);
            }
            staffIdAndShiftAndActivityDurationMap.put(DurationType.DAYS.equals(frequencyType) ? DateUtils.getStartDateTimeintervalString(dateTimeInterval) : DateUtils.getDateTimeintervalString(dateTimeInterval), subClusteredBarValue);
        }
        return staffIdAndShiftAndActivityDurationMap;
    }

    private Map<Object, List<ClusteredBarChartKpiDataUnit>> getShiftAndActivityByRepresentTotalData(List<DateTimeInterval> dateTimeIntervals, List<ShiftWithActivityDTO> shifts, Map<String, Integer> activityNameAndTotalDurationMinutesMap, Integer shiftDurationMinutes, Map<String, String> activityNameAndColorCodeMap) {
        Map<Object, List<ClusteredBarChartKpiDataUnit>> staffIdAndShiftAndActivityDurationMap = new HashedMap();
        staffIdAndShiftAndActivityDurationMap.put(DateUtils.getDateTimeintervalString(new DateTimeInterval(dateTimeIntervals.get(0).getStartDate(), dateTimeIntervals.get(dateTimeIntervals.size() - 1).getEndDate())), getShiftAndActivityDurationMap(activityNameAndColorCodeMap, activityNameAndTotalDurationMinutesMap, shiftDurationMinutes,shifts));
        return staffIdAndShiftAndActivityDurationMap;
    }

    private Map<Object, List<ClusteredBarChartKpiDataUnit>> getShiftAndActivityByRepresentPerStaff(List<Long> staffIds, List<ShiftWithActivityDTO> shifts, Map<String, String> activityNameAndColorCodeMap) {
        Map<Object, List<ClusteredBarChartKpiDataUnit>> staffIdAndShiftAndActivityDurationMap = new HashedMap();
        Map<String, Integer> activityNameAndTotalDurationMinutesMap;
        Integer shiftDurationMinutes;
        Map<Long, List<ShiftWithActivityDTO>> staffShiftMapping = shifts.parallelStream().collect(Collectors.groupingBy(ShiftDTO::getStaffId, Collectors.toList()));
        for (Long staffId : staffIds) {
            activityNameAndTotalDurationMinutesMap = new HashMap<>();
            shiftDurationMinutes = 0;
            staffIdAndShiftAndActivityDurationMap.put(staffId, getShiftAndActivityDurationMap(activityNameAndColorCodeMap, activityNameAndTotalDurationMinutesMap, shiftDurationMinutes, staffShiftMapping.getOrDefault(staffId,new ArrayList<>())));
        }
        return staffIdAndShiftAndActivityDurationMap;
    }

    private List<ClusteredBarChartKpiDataUnit> getShiftAndActivityDurationMap(Map<String, String> activityNameAndColorCodeMap, Map<String, Integer> activityNameAndTotalDurationMinutesMap, Integer shiftDurationMinutes, List<ShiftWithActivityDTO> shifts) {
        List<ClusteredBarChartKpiDataUnit> subClusteredBarValue = new ArrayList<>();
        for (ShiftWithActivityDTO shift : shifts) {
            for (ShiftActivityDTO activity : shift.getActivities()) {
                int activityDuration = activityNameAndTotalDurationMinutesMap.getOrDefault(activity.getActivityName(), 0);
                activityNameAndTotalDurationMinutesMap.put(activity.getActivityName(), activityDuration + activity.getDurationMinutes());
                activityNameAndColorCodeMap.putIfAbsent(activity.getActivityName(), (ObjectUtils.isNotNull(activity.getBackgroundColor()) && !AppConstants.BLANK_STRING.equals(activity.getBackgroundColor())) ? activity.getBackgroundColor() : AppConstants.KPI_DEFAULT_COLOR);
            }
            shiftDurationMinutes += shift.getDurationMinutes();
        }
        subClusteredBarValue.add(new ClusteredBarChartKpiDataUnit(AppConstants.SHIFT, DateUtils.getHoursByMinutes(shiftDurationMinutes.doubleValue())));
        for (Map.Entry<String, Integer> stringIntegerEntry : activityNameAndTotalDurationMinutesMap.entrySet()) {
            subClusteredBarValue.add(new ClusteredBarChartKpiDataUnit(stringIntegerEntry.getKey(), activityNameAndColorCodeMap.get(stringIntegerEntry.getKey()), DateUtils.getHoursByMinutes(stringIntegerEntry.getValue())));
        }
        return subClusteredBarValue;
    }

    private Map<Long,Integer> getStaffAndShiftDurationMap(List<ShiftWithActivityDTO> shiftDTOS){
        Map<Long, List<ShiftWithActivityDTO>> staffShiftsMap = shiftDTOS.parallelStream().collect(Collectors.groupingBy(ShiftDTO::getStaffId, Collectors.toList()));
        Map<Long,Integer> staffAndShiftDurationMap = new HashMap<>();
        for (Map.Entry<Long, List<ShiftWithActivityDTO>> staffIdAndShiftsEntry : staffShiftsMap.entrySet()) {
            Integer shiftDuration = staffIdAndShiftsEntry.getValue().stream().mapToInt(ShiftWithActivityDTO::getDurationMinutes).sum();
            staffAndShiftDurationMap.put(staffIdAndShiftsEntry.getKey(),shiftDuration);
        }
        return staffAndShiftDurationMap;
    }

    public KPISetResponseDTO getCalculatedDataOfKPI(Map<FilterType, List> filterBasedCriteria, Long organizationId, KPI kpi, ApplicableKPI applicableKPI){
        return  new KPISetResponseDTO();

    }

}
