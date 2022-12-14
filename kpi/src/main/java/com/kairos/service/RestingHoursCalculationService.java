package com.kairos.service;

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectUtils;
import com.kairos.constants.AppConstants;
import com.kairos.dto.activity.counter.chart.ClusteredBarChartKpiDataUnit;
import com.kairos.dto.activity.counter.chart.CommonKpiDataUnit;
import com.kairos.dto.activity.counter.data.KPIAxisData;
import com.kairos.dto.activity.counter.data.KPIRepresentationData;
import com.kairos.dto.activity.counter.enums.RepresentationUnit;
import com.kairos.dto.activity.counter.enums.XAxisConfig;
import com.kairos.dto.activity.kpi.KPIResponseDTO;
import com.kairos.dto.activity.kpi.StaffKpiFilterDTO;
import com.kairos.dto.activity.shift.ShiftDTO;
import com.kairos.enums.DurationType;
import com.kairos.enums.FilterType;
import com.kairos.enums.kpi.Direction;
import com.kairos.persistence.model.ApplicableKPI;
import com.kairos.persistence.model.FibonacciKPICalculation;
import com.kairos.persistence.model.KPI;
import com.kairos.persistence.repository.counter.CounterHelperRepository;
import com.kairos.utils.FibonacciCalculationUtil;
import com.kairos.utils.KPIUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.enums.kpi.KPIRepresentation.REPRESENT_PER_STAFF;

@Service
public class RestingHoursCalculationService implements CounterService {
    @Inject
    private CounterHelperService counterHelperService;
    @Inject private CounterHelperRepository counterHelperRepository;

    public double getTotalRestingHours(List<ShiftDTO> shifts, LocalDate startDate, LocalDate endDate) {
        //all shifts should be sorted on startDate
        DateTimeInterval dateTimeInterval = new DateTimeInterval(startDate, endDate);
        long totalrestingMinutes = dateTimeInterval.getMilliSeconds()/3600000;
        for (ShiftDTO shift : shifts) {
            DateTimeInterval shiftInterval = new DateTimeInterval(shift.getStartDate(), shift.getEndDate());
            if (dateTimeInterval.overlaps(shiftInterval)) {
                totalrestingMinutes -= (int)(dateTimeInterval.overlap(shiftInterval).getMinutes()/60);

            }
        }
        return totalrestingMinutes;
    }

    public Map<Object, Double> calculateRestingHours(List<Long> staffIds, ApplicableKPI applicableKPI, List<DateTimeInterval> dateTimeIntervals) {
        Map<DateTimeInterval, List<ShiftDTO>> dateTimeIntervalListMap = new HashMap<>();
        List<ShiftDTO> shifts = counterHelperRepository.findAllShiftsByStaffIdsAndDate(staffIds, DateUtils.getLocalDateTimeFromLocalDate(DateUtils.asLocalDate(dateTimeIntervals.get(0).getStartDate())), DateUtils.getLocalDateTimeFromLocalDate(DateUtils.asLocalDate(dateTimeIntervals.get(dateTimeIntervals.size() - 1).getEndDate())));
        for (DateTimeInterval dateTimeInterval : dateTimeIntervals) {
            dateTimeIntervalListMap.put(dateTimeInterval, shifts.stream().filter(shift -> dateTimeInterval.contains(shift.getStartDate())).collect(Collectors.toList()));
        }
        return calculateDataByKpiRepresentation(staffIds, dateTimeIntervalListMap, dateTimeIntervals, applicableKPI, shifts);
    }

    private List<CommonKpiDataUnit> getRestingHoursKpiData(Map<FilterType, List> filterBasedCriteria, Long organizationId, ApplicableKPI applicableKPI) {
        // TO BE USED FOR AVERAGE CALCULATION.
        double multiplicationFactor = 1;
        Object[] filterCriteria = counterHelperService.getDataByFilterCriteria(filterBasedCriteria);
        List<Long> staffIds = (List<Long>)filterCriteria[0];
        List<LocalDate> filterDates = (List<LocalDate>)filterCriteria[1];
        List<Long> unitIds = (List<Long>)filterCriteria[2];
        List<Long> employmentTypeIds = (List<Long>)filterCriteria[3];
        Object[] kpiData = counterHelperService.getKPIdata(new HashMap(),applicableKPI,filterDates,staffIds,employmentTypeIds,unitIds,organizationId);
        List<DateTimeInterval> dateTimeIntervals = (List<DateTimeInterval>)kpiData[1];
        List<StaffKpiFilterDTO> staffKpiFilterDTOS = (List<StaffKpiFilterDTO>)kpiData[0];
        staffIds=staffKpiFilterDTOS.stream().map(StaffKpiFilterDTO::getId).collect(Collectors.toList());
        Map<Object, Double> staffRestingHours = calculateRestingHours(staffIds, applicableKPI, dateTimeIntervals);
        List<CommonKpiDataUnit> kpiDataUnits = new ArrayList<>();
        getKpiDataUnits(multiplicationFactor, staffRestingHours, kpiDataUnits, applicableKPI, staffKpiFilterDTOS);
        KPIUtils.sortKpiDataByDateTimeInterval(kpiDataUnits);
        return kpiDataUnits;
    }

    private void getKpiDataUnits(double multiplicationFactor, Map<Object, Double> staffRestingHours, List<CommonKpiDataUnit> kpiDataUnits, ApplicableKPI applicableKPI, List<StaffKpiFilterDTO> staffKpiFilterDTOS) {
        for (Map.Entry<Object, Double> entry : staffRestingHours.entrySet()) {
                if(REPRESENT_PER_STAFF.equals(applicableKPI.getKpiRepresentation())) {
                    Map<Long, String> staffIdAndNameMap = staffKpiFilterDTOS.stream().collect(Collectors.toMap(StaffKpiFilterDTO::getId, StaffKpiFilterDTO::getFullName));
                    kpiDataUnits.add(new ClusteredBarChartKpiDataUnit(staffIdAndNameMap.get(entry.getKey()), Arrays.asList(new ClusteredBarChartKpiDataUnit(staffIdAndNameMap.get(entry.getKey()), entry.getValue() * multiplicationFactor))));
                }else{
                    kpiDataUnits.add(new ClusteredBarChartKpiDataUnit(KPIUtils.getKpiDateFormatByIntervalUnit(entry.getKey().toString(),applicableKPI.getFrequencyType(),applicableKPI.getKpiRepresentation()),entry.getKey().toString(), Arrays.asList(new ClusteredBarChartKpiDataUnit(entry.getKey().toString(), entry.getValue() * multiplicationFactor))));
            }
        }
    }


    @Override
    public KPIRepresentationData getCalculatedCounter(Map<FilterType, List> filterBasedCriteria, Long organizationId, KPI kpi) {
        List<CommonKpiDataUnit> dataList = getRestingHoursKpiData(filterBasedCriteria, organizationId, null);
        return new KPIRepresentationData(kpi.getId(), kpi.getTitle(), kpi.getChart(), XAxisConfig.HOURS, RepresentationUnit.DECIMAL, dataList, new KPIAxisData(AppConstants.STAFF, AppConstants.LABEL), new KPIAxisData(AppConstants.HOURS, AppConstants.VALUE_FIELD));
    }

    @Override
    public KPIRepresentationData getCalculatedKPI(Map<FilterType, List> filterBasedCriteria, Long organizationId, KPI kpi, ApplicableKPI applicableKPI) {
        List<CommonKpiDataUnit> dataList = getRestingHoursKpiData(filterBasedCriteria, organizationId, applicableKPI);
        return new KPIRepresentationData(kpi.getId(), kpi.getTitle(), kpi.getChart(), XAxisConfig.HOURS, RepresentationUnit.DECIMAL, dataList,  new KPIAxisData(applicableKPI.getKpiRepresentation().equals(REPRESENT_PER_STAFF) ? AppConstants.STAFF : AppConstants.DATE, AppConstants.LABEL), new KPIAxisData(AppConstants.HOURS, AppConstants.VALUE_FIELD));
    }


    public  Map<Long, Integer> getStaffAndWithRestingHour(Map<FilterType, List> filterBasedCriteria, Long organizationId,ApplicableKPI applicableKPI) {
        Object[] filterCriteria = counterHelperService.getDataByFilterCriteria(filterBasedCriteria);
        List<Long> staffIds = new ArrayList<>();
        List<LocalDate> filterDates = (List<LocalDate>)filterCriteria[1];
        Object[] kpiData = counterHelperService.getKPIdata(new HashMap(),applicableKPI,filterDates,staffIds, ObjectUtils.newArrayList(), ObjectUtils.newArrayList(organizationId),organizationId);
        staffIds = (List<Long>)kpiData[2];
        List<DateTimeInterval> dateTimeIntervals = (List<DateTimeInterval>)kpiData[1];
        List<ShiftDTO> shifts = counterHelperRepository.findAllShiftsByStaffIdsAndDate(staffIds, DateUtils.getLocalDateTimeFromLocalDate(DateUtils.asLocalDate(dateTimeIntervals.get(0).getStartDate())), DateUtils.getLocalDateTimeFromLocalDate(DateUtils.asLocalDate(dateTimeIntervals.get(dateTimeIntervals.size() - 1).getEndDate())));
        Map<Object, Double> restingHoursMap = calculateDataByKpiRepresentation(staffIds, null, dateTimeIntervals, applicableKPI, shifts);
        return restingHoursMap.entrySet().stream().collect(Collectors.toMap(k->(Long)k.getKey(),v->v.getValue().intValue()));
    }


    @Override
    public TreeSet<FibonacciKPICalculation> getFibonacciCalculatedCounter(Map<FilterType, List> filterBasedCriteria, Long organizationId, Direction sortingOrder,List<StaffKpiFilterDTO> staffKpiFilterDTOS,KPI kpi,ApplicableKPI applicableKPI) {
        Map<Long, Integer> staffAndRestingHoursMap =  getStaffAndWithRestingHour(filterBasedCriteria, organizationId, applicableKPI);
        return FibonacciCalculationUtil.getFibonacciCalculation(staffAndRestingHoursMap,sortingOrder);
    }

    private Map<Object, Double> calculateDataByKpiRepresentation(List<Long> staffIds, Map<DateTimeInterval, List<ShiftDTO>> dateTimeIntervalListMap, List<DateTimeInterval> dateTimeIntervals, ApplicableKPI applicableKPI, List<ShiftDTO> shifts) {
        Map<Object, Double> staffRestingHours ;
        Double restingHours = 0d;
        switch (applicableKPI.getKpiRepresentation()) {
            case REPRESENT_PER_STAFF:
                staffRestingHours = getStaffRestingHoursByRepresentPerStaff(staffIds, dateTimeIntervals, shifts);
                break;
            case REPRESENT_TOTAL_DATA:
                staffRestingHours = getStaffRestingHoursByRepresentTotalData(staffIds, dateTimeIntervals, shifts, restingHours);
                break;
            default:
                staffRestingHours = getStaffRestingHoursByRepresentPerInterval(staffIds, dateTimeIntervalListMap, dateTimeIntervals ,applicableKPI.getFrequencyType());
                break;
        }
        return KPIUtils.verifyKPIResponseData(staffRestingHours) ? staffRestingHours : new HashMap<>();
    }

    private Map<Object, Double> getStaffRestingHoursByRepresentTotalData(List<Long> staffIds, List<DateTimeInterval> dateTimeIntervals, List<ShiftDTO> shifts, Double restingHours) {
        Map<Long, List<ShiftDTO>> staffShiftMapping;
        Map<Object, Double> staffRestingHours = new HashMap<>();
        staffShiftMapping = shifts.parallelStream().collect(Collectors.groupingBy(ShiftDTO::getStaffId, Collectors.toList()));
        for (DateTimeInterval dateTimeInterval : dateTimeIntervals) {
            for (Long staffId : staffIds) {
                restingHours += getTotalRestingHours(staffShiftMapping.getOrDefault(staffId, new ArrayList<>()), DateUtils.asLocalDate(dateTimeInterval.getStartDate()), dateTimeInterval.getEndLocalDate());
            }
        }
        staffRestingHours.put(DateUtils.getDateTimeintervalString(new DateTimeInterval(dateTimeIntervals.get(0).getStartDate(), dateTimeIntervals.get(dateTimeIntervals.size() - 1).getEndDate())), restingHours);
        return staffRestingHours;
    }

    private Map<Object, Double> getStaffRestingHoursByRepresentPerStaff(List<Long> staffIds, List<DateTimeInterval> dateTimeIntervals, List<ShiftDTO> shifts) {
        Double restingHours;
        Map<Object, Double> staffRestingHours=new HashMap<>();
        Map<Long, List<ShiftDTO>> staffShiftMapping = shifts.parallelStream().collect(Collectors.groupingBy(ShiftDTO::getStaffId, Collectors.toList()));
        for (Long staffId : staffIds) {
            restingHours = getTotalRestingHours(staffShiftMapping.getOrDefault(staffId, new ArrayList<>()), DateUtils.asLocalDate(dateTimeIntervals.get(0).getStartDate()), DateUtils.asLocalDate(dateTimeIntervals.get(dateTimeIntervals.size() - 1).getEndDate()));
            staffRestingHours.put(staffId, restingHours);
        }
        return staffRestingHours;
    }

    private Map<Object, Double> getStaffRestingHoursByRepresentPerInterval(List<Long> staffIds, Map<DateTimeInterval, List<ShiftDTO>> dateTimeIntervalListMap, List<DateTimeInterval> dateTimeIntervals , DurationType frequencyType) {
        Double restingHours;
        Map<Object, Double> staffRestingHours = new HashMap<>();
        Map<Long, List<ShiftDTO>> staffShiftMapping;
        Map<DateTimeInterval, Map<Long, List<ShiftDTO>>> dateTimeIntervalListMap1 = new HashedMap();
        dateTimeIntervalListMap.keySet().stream().forEach(dateTimeInterval -> dateTimeIntervalListMap1.put(dateTimeInterval, dateTimeIntervalListMap.get(dateTimeInterval).stream().collect(Collectors.groupingBy(ShiftDTO::getStaffId, Collectors.toList()))));
        for (DateTimeInterval dateTimeInterval : dateTimeIntervals) {
            restingHours = 0d;
            staffShiftMapping = dateTimeIntervalListMap1.get(dateTimeInterval);
            for (Long staffId : staffIds) {
                restingHours += getTotalRestingHours(staffShiftMapping.getOrDefault(staffId, new ArrayList<>()), DateUtils.asLocalDate(dateTimeInterval.getStartDate()), DateUtils.asLocalDate(dateTimeInterval.getEndDate()));
            }
            staffRestingHours.put(DurationType.DAYS.equals(frequencyType) ? DateUtils.getStartDateTimeintervalString(dateTimeInterval) : DateUtils.getDateTimeintervalString(dateTimeInterval), restingHours);
        }
        return staffRestingHours;
    }

    public KPIResponseDTO getCalculatedDataOfKPI(Map<FilterType, List> filterBasedCriteria, Long organizationId, KPI kpi, ApplicableKPI applicableKPI){
        KPIResponseDTO kpiResponseDTO = new KPIResponseDTO();
        Map<Long, Integer> restingHoursMap =  getStaffAndWithRestingHour(filterBasedCriteria, organizationId, applicableKPI);
        Map<Long, Double> staffAndRestingHoursMap = restingHoursMap.entrySet().stream().collect(Collectors.toMap(k->k.getKey(),v-> v.getValue().doubleValue()));
        kpiResponseDTO.setKpiName(kpi.getTitle());
        kpiResponseDTO.setKpiId(kpi.getId());
        kpiResponseDTO.setStaffKPIValue(staffAndRestingHoursMap);
        return kpiResponseDTO;
    }

}


