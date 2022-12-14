package com.kairos.service;

/*
 * @author: mohit.shakya@oodlestechnologies.com
 * @dated: Jun/27/2018
 */

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.commons.utils.ObjectUtils;
import com.kairos.constants.AppConstants;
import com.kairos.constants.KPIMessagesConstants;
import com.kairos.counter.CounterServiceMapping;
import com.kairos.dto.activity.activity.ActivityDTO;
import com.kairos.dto.activity.counter.configuration.CounterDTO;
import com.kairos.dto.activity.counter.configuration.KPIDTO;
import com.kairos.dto.activity.counter.configuration.KPIFilterDefaultDataDTO;
import com.kairos.dto.activity.counter.data.CommonRepresentationData;
import com.kairos.dto.activity.counter.data.FilterCriteria;
import com.kairos.dto.activity.counter.data.FilterCriteriaDTO;
import com.kairos.dto.activity.counter.distribution.access_group.AccessGroupPermissionCounterDTO;
import com.kairos.dto.activity.counter.distribution.tab.KPIPosition;
import com.kairos.dto.activity.counter.distribution.tab.TabKPIDTO;
import com.kairos.dto.activity.counter.enums.*;
import com.kairos.dto.activity.kpi.DefaultKpiDataDTO;
import com.kairos.dto.activity.kpi.KPIResponseDTO;
import com.kairos.dto.activity.kpi.KPISetResponseDTO;
import com.kairos.dto.activity.presence_type.PresenceTypeDTO;
import com.kairos.dto.activity.time_type.TimeTypeDTO;
import com.kairos.dto.user.access_permission.AccessGroupRole;
import com.kairos.dto.user.country.time_slot.TimeSlotSetDTO;
import com.kairos.dto.user.organization.OrganizationCommonDTO;
import com.kairos.dto.user.team.TeamDTO;
import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.DurationType;
import com.kairos.enums.EmploymentSubType;
import com.kairos.enums.FilterType;
import com.kairos.enums.TimeSlotType;
import com.kairos.enums.kpi.CalculationType;
import com.kairos.enums.kpi.KPIRepresentation;
import com.kairos.enums.kpi.YAxisConfig;
import com.kairos.enums.phase.PhaseDefaultName;
import com.kairos.enums.reason_code.ReasonCodeType;
import com.kairos.enums.shift.ShiftStatus;
import com.kairos.enums.shift.TodoStatus;
import com.kairos.enums.team.TeamType;
import com.kairos.persistence.model.*;
import com.kairos.persistence.repository.counter.CounterHelperRepository;
import com.kairos.persistence.repository.counter.CounterRepository;

import com.kairos.utils.KPIUtils;
import com.mindscapehq.raygun4java.core.RaygunClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.kairos.enums.FilterType.*;
import static java.util.stream.Collectors.toList;



@Service
public class CounterDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CounterDataService.class);

    @Inject
    private ExceptionService exceptionService;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Inject
    private CounterRepository counterRepository;
    @Inject
    private CounterServiceMapping counterServiceMapping;
    @Inject
    private ExecutorService executorService;
    @Inject
    private CounterDistService counterDistService;
    @Inject
    private CounterHelperRepository counterHelperRepository;
    @Inject
    private CounterHelperService counterHelperService;
    @Inject private RaygunClient raygunClient;


    public Map generateKPIData(FilterCriteriaDTO filters, Long organizationId, Long staffId) {
        Map<BigInteger, ApplicableKPI> kpiIdAndApplicableKPIMap=new HashMap<>();
        List<KPI> kpis = counterRepository.getKPIsByIds(filters.getKpiIds());
        Map<BigInteger, KPI> kpiMap = kpis.stream().collect(Collectors.toMap(MongoBaseEntity::getId, kpi -> kpi));
        List<Future<CommonRepresentationData>> kpiResults = new ArrayList<>();
        Map<FilterType, List> filterBasedCriteria = new HashMap<>();
        Map<BigInteger, Map<FilterType, List>> staffKpiFilterCritera = new HashMap<>();
        if (filters.getFilters() != null && ObjectUtils.isCollectionNotEmpty(filters.getFilters())) {
            filters.getFilters().forEach(filter -> filterBasedCriteria.put(filter.getType(), filter.getValues()));
            kpiIdAndApplicableKPIMap.put(kpis.get(0).getId(),new ApplicableKPI(filters.getKpiRepresentation(),filters.getValue(),filters.getInterval(),filters.getFrequencyType()));
        } else {
            getStaffKPiFilterAndApplicableKpi(filters, staffId, kpiIdAndApplicableKPIMap, kpis, staffKpiFilterCritera,null);
        }
        for (BigInteger kpiId : new HashSet<>(filters.getKpiIds())) {
            if(kpiIdAndApplicableKPIMap.containsKey(kpiId)) {
                Callable<CommonRepresentationData> data = () -> {
                    KPI kpi = kpiMap.get(kpiId);
                    kpi.setMultiDimensional(filters.isMultiDimensional());
                    return counterServiceMapping.getService(kpi.getType()).getCalculatedKPI(staffKpiFilterCritera.getOrDefault(kpiId, filterBasedCriteria), organizationId, kpi, kpiIdAndApplicableKPIMap.get(kpiId));
                };
                Future<CommonRepresentationData> responseData = executorService.submit(data);
                kpiResults.add(responseData);
            }
        }
        List<CommonRepresentationData> kpisData = new ArrayList<>();
        for (Future<CommonRepresentationData> data : kpiResults) {
            try {
                if(ObjectUtils.isNotNull(data))kpisData.add(data.get());
            } catch (InterruptedException | ExecutionException ex) {
                raygunClient.send(ex);
                LOGGER.error("error while generate KPI  data",ex);
                String[] messages = ex.getMessage().split(": ");
                throw new RuntimeException(messages.length > 1 ? messages[1] : messages.length==1 ? messages[0] : "");
            }
        }
        return ObjectUtils.isNotNull(kpisData) ? kpisData.stream().collect(Collectors.toMap(CommonRepresentationData::getCounterId, kpiData -> kpiData)) : new HashMap<>();
    }


    private void getStaffKPiFilterAndApplicableKpi(FilterCriteriaDTO filters, Long staffId, Map<BigInteger, ApplicableKPI> kpiIdAndApplicableKPIMap, List<KPI> kpis, Map<BigInteger, Map<FilterType, List>> staffKpiFilterCritera,LocalDate startDate) {
        List<ApplicableKPI> staffApplicableKPIS = getApplicableKPIS(filters, staffId, kpis);
        for (ApplicableKPI staffApplicableKPI : staffApplicableKPIS) {
            getStaffFIlterBasedCriteria(filters, staffId, staffKpiFilterCritera, staffApplicableKPI,startDate);
            kpiIdAndApplicableKPIMap.put(staffApplicableKPI.getActiveKpiId(),staffApplicableKPI);
        }
    }

    private void getStaffFIlterBasedCriteria(FilterCriteriaDTO filters, Long staffId, Map<BigInteger, Map<FilterType, List>> staffKpiFilterCritera, ApplicableKPI staffApplicableKPI,LocalDate startDate) {
         Map<FilterType, List> staffFilterBasedCriteria=new HashMap<>();
        if (ObjectUtils.isNotNull(staffApplicableKPI.getApplicableFilter())) {
            staffApplicableKPI.getApplicableFilter().getCriteriaList().forEach(filterCriteria -> staffFilterBasedCriteria.put(filterCriteria.getType(), filterCriteria.getValues()));
            if(KPIRepresentation.INDIVIDUAL_STAFF.equals(staffApplicableKPI.getKpiRepresentation()) && !filters.isCountryAdmin()){
                staffFilterBasedCriteria.put(STAFF_IDS, Arrays.asList(ObjectUtils.isNotNull(filters.getStaffId()) ?filters.getStaffId().intValue() : staffId.intValue()));
            }
            if(ObjectUtils.isNotNull(filters.getFrequencyType())){
                staffApplicableKPI.setInterval(filters.getInterval());
                staffApplicableKPI.setValue(filters.getValue());
                staffApplicableKPI.setFrequencyType(filters.getFrequencyType());
            }
            if(ObjectUtils.isNotNull(filters.getStartDate()) && ObjectUtils.isNotNull( filters.getEndDate())) {
                staffFilterBasedCriteria.put(TIME_INTERVAL,Arrays.asList(filters.getStartDate(),filters.getEndDate()));
                staffApplicableKPI.setFrequencyType(DurationType.MONTHS);
            }
            updateFibonacciConfig(staffApplicableKPI, startDate, staffFilterBasedCriteria);
            staffKpiFilterCritera.put(staffApplicableKPI.getActiveKpiId(), staffFilterBasedCriteria);
        }
    }

    private void updateFibonacciConfig(ApplicableKPI staffApplicableKPI, LocalDate startDate, Map<FilterType, List> staffFilterBasedCriteria) {
        if(ObjectUtils.isCollectionNotEmpty(staffApplicableKPI.getFibonacciKPIConfigs())){
            staffFilterBasedCriteria.put(FIBONACCI,staffApplicableKPI.getFibonacciKPIConfigs());
            if(!staffFilterBasedCriteria.containsKey(TIME_INTERVAL) && ObjectUtils.isNotNull(startDate)){
                List<DateTimeInterval> dateTimeIntervals = KPIUtils.getDateTimeIntervals(staffApplicableKPI.getInterval(),  staffApplicableKPI.getValue(), staffApplicableKPI.getFrequencyType(), null,startDate);
                staffFilterBasedCriteria.put(TIME_INTERVAL, ObjectUtils.newArrayList(dateTimeIntervals.get(0).getStartLocalDate(), dateTimeIntervals.get(dateTimeIntervals.size() - 1).getEndLocalDate()));
            }
        }
    }

    private List<ApplicableKPI> getApplicableKPIS(FilterCriteriaDTO filters, Long staffId, List<KPI> kpis) {
        List<ApplicableKPI> staffApplicableKPIS;
        if (filters.isCountryAdmin()) {
            staffApplicableKPIS = counterRepository.getApplicableKPI(kpis.stream().map(MongoBaseEntity::getId).collect(Collectors.toList()), ConfLevel.UNIT, filters.getUnitId());
        } else {
            staffApplicableKPIS = counterRepository.getApplicableKPI(kpis.stream().map(MongoBaseEntity::getId).collect(Collectors.toList()), ConfLevel.STAFF, staffId);
        }
        return staffApplicableKPIS;
    }

    //kpi default data and copy and save filter
    public KPIDTO getDefaultFilterDataOfKpi(String tabId, BigInteger kpiId, Long refId, ConfLevel level) {
        AccessGroupPermissionCounterDTO accessGroupPermissionCounterDTO = userIntegrationService.getAccessGroupIdsAndCountryAdmin(UserContext.getUserDetails().getLastSelectedOrganizationId());
        List<ApplicableKPI> applicableKPIS;
        if (!accessGroupPermissionCounterDTO.isManagement()) {
            exceptionService.actionNotPermittedException(KPIMessagesConstants.MESSAGE_KPI_PERMISSION);
        }
        if (ObjectUtils.isNotNull(tabId) && !accessGroupPermissionCounterDTO.isCountryAdmin() && !accessGroupPermissionCounterDTO.isManagement()) {
            level = ConfLevel.STAFF;
            applicableKPIS = counterRepository.getApplicableKPI(Arrays.asList(kpiId), level, accessGroupPermissionCounterDTO.getStaffId());
        } else {
            applicableKPIS = counterRepository.getApplicableKPI(Arrays.asList(kpiId), level, refId);
        }
        if (applicableKPIS.isEmpty()) {
            exceptionService.dataNotFoundByIdException(KPIMessagesConstants.MESSAGE_COUNTER_KPI_NOTFOUND);
        }
        List<FilterCriteria> criteriaList = new ArrayList<>();
        KPIDTO kpi = ObjectMapperUtils.copyPropertiesByMapper(counterRepository.getKPIByid(kpiId), KPIDTO.class);
        DefaultKpiDataDTO defaultKpiDataDTO = userIntegrationService.getKpiFilterDefaultData(ConfLevel.COUNTRY.equals(level) ? UserContext.getUserDetails().getLastSelectedOrganizationId() : refId);
        defaultKpiDataDTO.setDayTypeDTOS(counterHelperRepository.findAllByCountryIdAndDeletedFalse(UserContext.getUserDetails().getCountryId()));
        defaultKpiDataDTO.setReasonCodeDTOS(counterHelperRepository.getReasonCodesByUnitId(refId, ReasonCodeType.FORCEPLAN));
        TimeSlotSetDTO timeSlotSetDTO = counterHelperRepository.findByUnitIdAndTimeSlotTypeOrderByStartDate(refId, TimeSlotType.SHIFT_PLANNING);
        if(ObjectUtils.isNull(timeSlotSetDTO)){
            exceptionService.dataNotFoundException(KPIMessagesConstants.TIMESLOT_NOT_FOUND_FOR_UNIT);
        }
        defaultKpiDataDTO.setTimeSlotDTOS(timeSlotSetDTO.getTimeSlots());
        getSelectedFilterDefaultData(level, criteriaList, kpi, defaultKpiDataDTO);
        setKpiProperty(applicableKPIS.get(0), criteriaList, kpi);
        return kpi;
    }

    private void setKpiProperty(ApplicableKPI applicableKPI, List<FilterCriteria> criteriaList, KPIDTO kpiDTO) {
        kpiDTO.setDefaultFilters(criteriaList);
        kpiDTO.setTitle(applicableKPI.getTitle());
        kpiDTO.setValue(applicableKPI.getValue());
        kpiDTO.setFrequencyType(applicableKPI.getFrequencyType());

        kpiDTO.setInterval(applicableKPI.getInterval());
        kpiDTO.setKpiRepresentation(applicableKPI.getKpiRepresentation());
        if (ObjectUtils.isNotNull(applicableKPI.getApplicableFilter())) {
            kpiDTO.setSelectedFilters(applicableKPI.getApplicableFilter().getCriteriaList());
        }
    }

    private void getSelectedFilterDefaultData(ConfLevel level, List<FilterCriteria> criteriaList, KPIDTO kpi, DefaultKpiDataDTO defaultKpiDataDTO) {
        List<Long> unitIds = ObjectUtils.isCollectionNotEmpty(defaultKpiDataDTO.getOrganizationCommonDTOS()) ? defaultKpiDataDTO.getOrganizationCommonDTOS().stream().map(OrganizationCommonDTO::getId).collect(toList()) : ObjectUtils.newArrayList(UserContext.getUserDetails().getLastSelectedOrganizationId());
        for (FilterType filterType : kpi.getFilterTypes()) {
            switch (filterType){
                case EMPLOYMENT_TYPE:
                    getEmploymentTypeDefaultData(criteriaList, defaultKpiDataDTO);
                    break;
                case TIME_SLOT:
                    getTimeSlotDefaultData(criteriaList, defaultKpiDataDTO);
                    break;
                case DAY_TYPE:
                    getDayTypeDefaultData(criteriaList, defaultKpiDataDTO);
                    break;
                case UNIT_IDS:
                    getUnitIdsDefaultData(criteriaList, defaultKpiDataDTO, level);
                    break;
                case STAFF_IDS:
                    getStaffDefaultData(criteriaList, defaultKpiDataDTO, level);
                    break;
                case ACTIVITY_STATUS:
                    if (CounterType.ABSENCES_PER_INTERVAL.equals(kpi.getType())) {
                        getTodoStatusDefaultData(criteriaList);
                    } else {
                        getActivityStatusDefaultData(criteriaList);
                    }
                    break;
                case DAYS_OF_WEEK:
                    getDayOfWeekDefaultData(criteriaList);
                    break;
                case PLANNED_TIME_TYPE:
                    getPlannedTimeDefaultData(criteriaList);
                    break;
                case TIME_TYPE:
                    getTimeTypesDefaultData(criteriaList, defaultKpiDataDTO);
                    break;
                case PHASE:
                    getPhaseDefaultData(criteriaList);
                    break;
                case TIME_INTERVAL:
                    criteriaList.add(new FilterCriteria(TIME_INTERVAL.getValue(), TIME_INTERVAL, new ArrayList<>()));
                    break;
                case ACTIVITY_IDS:
                    getActivityDefaultData(criteriaList, unitIds);
                    break;
                case CALCULATION_TYPE:
                    getCalculationTypeData(criteriaList);
                    break;
                case CALCULATION_BASED_ON:
                    getCalculationBasedOnData(criteriaList);
                    break;
                case CALCULATION_UNIT:
                    getCalculationUnitData(criteriaList);
                    break;
                case REASON_CODE:
                    getReasonCodeData(criteriaList, defaultKpiDataDTO);
                    break;
                case PLANNED_BY:
                    getPlannedByUnitData(criteriaList);
                    break;
                case TEAM:
                    if (ObjectUtils.isCollectionNotEmpty(unitIds)) {
                        getTeamUnitData(criteriaList, unitIds.get(0));
                    }
                    break;
                case EMPLOYMENT_SUB_TYPE:
                    getEmploymentSubTypeData(criteriaList);
                    break;
                case TEAM_TYPE:
                    getTeamTypeData(criteriaList);
                    break;
                case TAGS:getTagsData(defaultKpiDataDTO,criteriaList);
                break;
                default:break;
            }
        }

    }

    private void getTagsData(DefaultKpiDataDTO defaultKpiDataDTO,List<FilterCriteria> criteriaList) {
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        defaultKpiDataDTO.getTags().forEach(tagDTO -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(tagDTO.getId(), tagDTO.getName())));
        criteriaList.add(new FilterCriteria(TAGS.getValue(), TAGS, (List) kpiFilterDefaultDataDTOS));
    }

    private void getPlannedByUnitData(List<FilterCriteria> criteriaList) {
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        for (AccessGroupRole accessGroupRole : AccessGroupRole.values()) {
            kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(accessGroupRole.name(), accessGroupRole.name().toLowerCase()));
        }
        criteriaList.add(new FilterCriteria(PLANNED_BY.getValue(), PLANNED_BY, (List) kpiFilterDefaultDataDTOS));
    }

    private void getActivityDefaultData(List<FilterCriteria> criteriaList, List<Long> unitIds) {
        List<ActivityDTO> activityDTOS = counterHelperRepository.findAllActivityByDeletedFalseAndUnitId(unitIds);
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        activityDTOS.forEach(activityDTO -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(activityDTO.getId().longValue(), activityDTO.getName(), activityDTO.getUnitId())));
        criteriaList.add(new FilterCriteria(ACTIVITY_IDS.getValue(), ACTIVITY_IDS, (List) kpiFilterDefaultDataDTOS));
    }

    private void getPhaseDefaultData(List<FilterCriteria> criteriaList) {
        List<PhaseDefaultName> phases = Arrays.asList(PhaseDefaultName.values());
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        phases.forEach(phase -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(phase.toString(), phase.toString())));
        criteriaList.add(new FilterCriteria(PHASE.getValue(), PHASE, (List) kpiFilterDefaultDataDTOS));
    }

    private void getTimeTypesDefaultData(List<FilterCriteria> criteriaList, DefaultKpiDataDTO defaultKpiDataDTO) {
        List<TimeTypeDTO> timeTypes = counterHelperRepository.getAllTimeTypesByCountryId(defaultKpiDataDTO.getCountryId());
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        timeTypes.forEach(timeType -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(timeType.getId().longValue(), timeType.getLabel())));
        criteriaList.add(new FilterCriteria(TIME_TYPE.getValue(), TIME_TYPE, (List) kpiFilterDefaultDataDTOS));
    }

    private void getDayOfWeekDefaultData(List<FilterCriteria> criteriaList) {
        List<DayOfWeek> dayOfWeeks = Arrays.asList(DayOfWeek.values());
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        dayOfWeeks.forEach(dayOfWeek -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(dayOfWeek.toString(), dayOfWeek.toString())));
        criteriaList.add(new FilterCriteria(DAYS_OF_WEEK.getValue(), DAYS_OF_WEEK, (List) kpiFilterDefaultDataDTOS));
    }

    private void getPlannedTimeDefaultData(List<FilterCriteria> criteriaList) {
        List<PresenceTypeDTO> plannedTimes=counterHelperRepository.getAllPresenceTypeByCountry(UserContext.getUserDetails().getCountryId());
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        plannedTimes.forEach(presenceTypeDTO -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(presenceTypeDTO.getId().toString(), presenceTypeDTO.getName())));
        criteriaList.add(new FilterCriteria(PLANNED_TIME_TYPE.getValue(), PLANNED_TIME_TYPE, (List) kpiFilterDefaultDataDTOS));
    }

    private void getActivityStatusDefaultData(List<FilterCriteria> criteriaList) {
        List<ShiftStatus> activityStatus = Arrays.asList(ShiftStatus.values());
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        activityStatus.forEach(shiftStatus -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(shiftStatus.toString(), shiftStatus.toString())));
        criteriaList.add(new FilterCriteria(ACTIVITY_STATUS.getValue(), ACTIVITY_STATUS, (List) kpiFilterDefaultDataDTOS));
    }

    private void getTodoStatusDefaultData(List<FilterCriteria> criteriaList) {
        List<TodoStatus> todoStatuses = TodoStatus.getAllStatusExceptViewed();
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        todoStatuses.forEach(shiftStatus -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(shiftStatus.toString(), shiftStatus.toString())));
        criteriaList.add(new FilterCriteria(ACTIVITY_STATUS.getValue(), ACTIVITY_STATUS, (List) kpiFilterDefaultDataDTOS));
    }

    private void getStaffDefaultData(List<FilterCriteria> criteriaList, DefaultKpiDataDTO defaultKpiDataDTO,ConfLevel level) {
        if(ConfLevel.UNIT.equals(level)) {
            List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
            defaultKpiDataDTO.getStaffKpiFilterDTOs().forEach(staffKpiFilterDTO -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(staffKpiFilterDTO.getId(), staffKpiFilterDTO.getFullName(), staffKpiFilterDTO.getUnitIds())));
            criteriaList.add(new FilterCriteria(STAFF_IDS.getValue(), STAFF_IDS, (List) kpiFilterDefaultDataDTOS));
        }
    }

    private void getCalculationTypeData(List<FilterCriteria> criteriaList) {
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        for (CalculationType calculationType : CalculationType.values()) {
            kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(calculationType.toString(), calculationType.value));
        }
        criteriaList.add(new FilterCriteria(CALCULATION_TYPE.getValue(), CALCULATION_TYPE, (List) kpiFilterDefaultDataDTOS));
    }

    private void getTeamUnitData(List<FilterCriteria> criteriaList, Long unitId) {
        List<TeamDTO> teamDTOS = userIntegrationService.getTeamByUnitId(unitId);
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        for (TeamDTO teamDTO : teamDTOS) {
            kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(teamDTO.getId(), teamDTO.getName()));
        }
        criteriaList.add(new FilterCriteria(TEAM.getValue(), TEAM, (List) kpiFilterDefaultDataDTOS));
    }

    private void getCalculationUnitData(List<FilterCriteria> criteriaList) {
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        for (XAxisConfig XAxisConfig : XAxisConfig.values()) {
            kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(XAxisConfig.toString(), XAxisConfig.getDisplayValue()));
        }
        criteriaList.add(new FilterCriteria(CALCULATION_UNIT.getValue(), CALCULATION_UNIT, (List) kpiFilterDefaultDataDTOS));
    }
    private void getEmploymentSubTypeData(List<FilterCriteria> criteriaList){
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        for (EmploymentSubType employmentSubType : EmploymentSubType.values()) {
            kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(employmentSubType.toString(), employmentSubType.value));
        }
        criteriaList.add(new FilterCriteria(EMPLOYMENT_SUB_TYPE.getValue(), EMPLOYMENT_SUB_TYPE, (List) kpiFilterDefaultDataDTOS));
    }

    private void getTeamTypeData(List<FilterCriteria> criteriaList){
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        for (TeamType teamType : TeamType.values()) {
            kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(teamType.toString(), teamType.value));
        }
        criteriaList.add(new FilterCriteria(TEAM_TYPE.getValue(), TEAM_TYPE, (List) kpiFilterDefaultDataDTOS));
    }


    private void getReasonCodeData(List<FilterCriteria> criteriaList, DefaultKpiDataDTO defaultKpiDataDTO) {
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        defaultKpiDataDTO.getReasonCodeDTOS().forEach(reasonCodeDTO -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(reasonCodeDTO.getId(), reasonCodeDTO.getName())));
        criteriaList.add(new FilterCriteria(REASON_CODE.getValue(), REASON_CODE, (List) kpiFilterDefaultDataDTOS));
    }

    private void getCalculationBasedOnData(List<FilterCriteria> criteriaList) {
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        for (YAxisConfig calculationType : YAxisConfig.values()) {
            kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(calculationType.toString(), calculationType.value));
        }
        criteriaList.add(new FilterCriteria(CALCULATION_BASED_ON.getValue(), CALCULATION_BASED_ON, (List) kpiFilterDefaultDataDTOS));
    }

    private void getUnitIdsDefaultData(List<FilterCriteria> criteriaList, DefaultKpiDataDTO defaultKpiDataDTO,ConfLevel confLevel) {
        if(ConfLevel.UNIT.equals(confLevel)) {
            List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
            defaultKpiDataDTO.getOrganizationCommonDTOS().forEach(organizationCommonDTO -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(organizationCommonDTO.getId(), organizationCommonDTO.getName())));
            criteriaList.add(new FilterCriteria(UNIT_IDS.getValue(), UNIT_IDS, (List) kpiFilterDefaultDataDTOS));
        }
    }

    private void getDayTypeDefaultData(List<FilterCriteria> criteriaList, DefaultKpiDataDTO defaultKpiDataDTO) {
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        defaultKpiDataDTO.getDayTypeDTOS().forEach(dayTypeDTO -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(dayTypeDTO.getId(), dayTypeDTO.getName())));
        criteriaList.add(new FilterCriteria(DAY_TYPE.getValue(), DAY_TYPE, (List) kpiFilterDefaultDataDTOS));
    }

    private void getTimeSlotDefaultData(List<FilterCriteria> criteriaList, DefaultKpiDataDTO defaultKpiDataDTO) {
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        defaultKpiDataDTO.getTimeSlotDTOS().forEach(timeSlotDTO -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(timeSlotDTO.getId(), timeSlotDTO.getName())));
        criteriaList.add(new FilterCriteria(TIME_SLOT.getValue(), TIME_SLOT, (List) kpiFilterDefaultDataDTOS));
    }

    private void getEmploymentTypeDefaultData(List<FilterCriteria> criteriaList, DefaultKpiDataDTO defaultKpiDataDTO) {
        List<KPIFilterDefaultDataDTO> kpiFilterDefaultDataDTOS = new ArrayList<>();
        defaultKpiDataDTO.getEmploymentTypeKpiDTOS().forEach(employmentTypeKpiDTO -> kpiFilterDefaultDataDTOS.add(new KPIFilterDefaultDataDTO(employmentTypeKpiDTO.getId(), employmentTypeKpiDTO.getName())));
        criteriaList.add(new FilterCriteria(EMPLOYMENT_TYPE.getValue(), EMPLOYMENT_TYPE, (List) kpiFilterDefaultDataDTOS));
    }

    public TabKPIDTO saveKpiFilterData(String tabId, Long refId, BigInteger kpiId, CounterDTO counterDTO, ConfLevel level) {
        AccessGroupPermissionCounterDTO accessGroupPermissionCounterDTO = userIntegrationService.getAccessGroupIdsAndCountryAdmin(UserContext.getUserDetails().getLastSelectedOrganizationId());
        if (!accessGroupPermissionCounterDTO.isManagement()) {
            exceptionService.actionNotPermittedException(KPIMessagesConstants.MESSAGE_KPI_PERMISSION);
        }
        if (ObjectUtils.isNotNull(tabId) && !accessGroupPermissionCounterDTO.isCountryAdmin()) {
            level = ConfLevel.STAFF;
            refId = accessGroupPermissionCounterDTO.getStaffId();
        }
        List<ApplicableKPI> applicableKPIS = counterRepository.getApplicableKPI(Arrays.asList(kpiId), level, refId);
        if (applicableKPIS.isEmpty()) {
            exceptionService.dataNotFoundByIdException(KPIMessagesConstants.MESSAGE_COUNTER_KPI_NOTFOUND);
        }
        KPI kpi = validateAndGetKpi(refId, kpiId, counterDTO, level, accessGroupPermissionCounterDTO, applicableKPIS);
        kpi.setMultiDimensional(counterDTO.isMultiDimensional());
        kpi.setCalculationFormula(counterDTO.getCalculationFormula());
        applicableKPIS.get(0).setApplicableFilter(new ApplicableFilter(counterDTO.getSelectedFilters(), true));
        List<ApplicableKPI> updateApplicableKPI = counterRepository.getFilterBaseApplicableKPIByKpiIdsOrUnitId(Arrays.asList(kpiId), Arrays.asList(ConfLevel.UNIT, ConfLevel.STAFF), ConfLevel.COUNTRY.equals(level) ? null : refId);
        updateIntervalConfig(counterDTO, applicableKPIS, updateApplicableKPI);
        applicableKPIS.get(0).setTitle(counterDTO.getTitle());
        setIntervalConfigurationOfKpi(counterDTO, applicableKPIS.get(0));
        applicableKPIS.addAll(updateApplicableKPI);
        counterRepository.saveEntities(applicableKPIS);
        counterRepository.save(kpi);
        kpi.setTitle(counterDTO.getTitle());
        return getTabKpiData(kpi, counterDTO, accessGroupPermissionCounterDTO);
    }

    private KPI validateAndGetKpi(Long refId, BigInteger kpiId, CounterDTO counterDTO, ConfLevel level, AccessGroupPermissionCounterDTO accessGroupPermissionCounterDTO, List<ApplicableKPI> applicableKPIS) {
        KPI kpi = counterRepository.getKPIByid(kpiId);
        if (ObjectUtils.isNotNull(kpi.getCalculationFormula()) && !kpi.getCalculationFormula().equals(counterDTO.getCalculationFormula()) && !accessGroupPermissionCounterDTO.isCountryAdmin()) {
            exceptionService.actionNotPermittedException(KPIMessagesConstants.MESSAGE_KPI_PERMISSION);
        }
        if (!applicableKPIS.get(0).getTitle().equals(counterDTO.getTitle()) && Optional.ofNullable(counterRepository.getKpiByTitleAndUnitId(counterDTO.getTitle(), refId, level)).isPresent()) {
            exceptionService.duplicateDataException(KPIMessagesConstants.ERROR_KPI_NAME_DUPLICATE);
        }
        return kpi;
    }

    private void updateIntervalConfig(CounterDTO counterDTO, List<ApplicableKPI> applicableKPIS, List<ApplicableKPI> updateApplicableKPI) {
        for (ApplicableKPI applicableKPI : updateApplicableKPI) {
            applicableKPI.setApplicableFilter(new ApplicableFilter(counterDTO.getSelectedFilters(), false));
            if (applicableKPI.getTitle().equals(applicableKPIS.get(0).getTitle())) {
                applicableKPI.setTitle(counterDTO.getTitle().trim());
            }
            setIntervalConfigurationOfKpi(counterDTO, applicableKPI);
        }
    }

    private void setIntervalConfigurationOfKpi(CounterDTO counterDTO, ApplicableKPI applicableKPIS) {
        applicableKPIS.setKpiRepresentation(counterDTO.getKpiRepresentation());
        applicableKPIS.setValue(counterDTO.getValue());
        applicableKPIS.setFrequencyType(counterDTO.getFrequencyType());
        applicableKPIS.setInterval(counterDTO.getInterval());
    }

    public TabKPIDTO copyKpiFilterData(String tabId, Long refId, BigInteger kpiId, CounterDTO counterDTO, ConfLevel level) {
        boolean copy = ObjectUtils.isNotNull(tabId);
        AccessGroupPermissionCounterDTO accessGroupPermissionCounterDTO = userIntegrationService.getAccessGroupIdsAndCountryAdmin(UserContext.getUserDetails().getLastSelectedOrganizationId());
        if (!accessGroupPermissionCounterDTO.isManagement()) {
            exceptionService.actionNotPermittedException(KPIMessagesConstants.MESSAGE_KPI_PERMISSION);
        }
        List<ApplicableKPI> applicableKPIS = validateAndApplicableKPIS(level,refId, tabId,accessGroupPermissionCounterDTO,kpiId);
        TabKPIConf tabKPIConf = null;
        KPI kpi = validateAndGetKpiForCopy(refId, kpiId, counterDTO, level, accessGroupPermissionCounterDTO);
        KPI copyKpi = ObjectMapperUtils.copyPropertiesByMapper(kpi, KPI.class);
        copyKpi.setId(null);
        copyKpi.setTitle(counterDTO.getTitle());
        copyKpi.setCalculationFormula(counterDTO.getCalculationFormula());
        copyKpi.setFilterTypes(counterDTO.getSelectedFilters().stream().map(FilterCriteria::getType).collect(toList()));
        counterRepository.save(copyKpi);
        tabKPIConf = getTabKPIConf(tabId, refId, counterDTO, level, copy, accessGroupPermissionCounterDTO, applicableKPIS, tabKPIConf, kpi, copyKpi);
        linkKpiToUncategorized(refId, level, copyKpi);
        TabKPIDTO tabKPIDTO = getTabKpiData(copyKpi, counterDTO, accessGroupPermissionCounterDTO);
        if(ObjectUtils.isNotNull(tabKPIConf)){
            tabKPIDTO.setId(tabKPIConf.getId());
        }
        return tabKPIDTO;
    }

    private TabKPIConf getTabKPIConf(String tabId, Long refId, CounterDTO counterDTO, ConfLevel level, boolean copy, AccessGroupPermissionCounterDTO accessGroupPermissionCounterDTO, List<ApplicableKPI> applicableKPIS, TabKPIConf tabKPIConf, KPI kpi, KPI copyKpi) {
        List<ApplicableKPI> applicableKPIs = new ArrayList<>();
        if (ConfLevel.COUNTRY.equals(level) || accessGroupPermissionCounterDTO.isCountryAdmin()) {
            applicableKPIs.add(new ApplicableKPI(copyKpi.getId(), kpi.getId(), refId, null, null, level, new ApplicableFilter(counterDTO.getSelectedFilters(), false), counterDTO.getTitle(), copy,counterDTO.getKpiRepresentation(),counterDTO.getInterval(),counterDTO.getValue(),counterDTO.getFrequencyType(),null));
        } else if (ConfLevel.UNIT.equals(level)) {
            applicableKPIs.add(new ApplicableKPI(copyKpi.getId(), applicableKPIS.get(0).getBaseKpiId(), null, refId, null, level, new ApplicableFilter(counterDTO.getSelectedFilters(), false), counterDTO.getTitle(), copy,counterDTO.getKpiRepresentation(),counterDTO.getInterval(),counterDTO.getValue(),counterDTO.getFrequencyType(),null));
            applicableKPIs.add(new ApplicableKPI(copyKpi.getId(), applicableKPIS.get(0).getBaseKpiId(), null, refId, accessGroupPermissionCounterDTO.getStaffId(), ConfLevel.STAFF, new ApplicableFilter(counterDTO.getSelectedFilters(), false), counterDTO.getTitle(), copy,counterDTO.getKpiRepresentation(),counterDTO.getInterval(),counterDTO.getValue(),counterDTO.getFrequencyType(),null));
        } else if (ObjectUtils.isNotNull(tabId) && ConfLevel.STAFF.equals(level)) {
            applicableKPIs.add(new ApplicableKPI(copyKpi.getId(), applicableKPIS.get(0).getBaseKpiId(), null, refId, accessGroupPermissionCounterDTO.getStaffId(), ConfLevel.STAFF, new ApplicableFilter(counterDTO.getSelectedFilters(), false), counterDTO.getTitle(), copy,counterDTO.getKpiRepresentation(),counterDTO.getInterval(),counterDTO.getValue(),counterDTO.getFrequencyType(),null));
            tabKPIConf = new TabKPIConf(tabId, copyKpi.getId(), null, refId, accessGroupPermissionCounterDTO.getStaffId(), level, new KPIPosition(0, 0), KPIValidity.BASIC, LocationType.FIX, counterDistService.calculatePriority(ConfLevel.UNIT, KPIValidity.BASIC, LocationType.FIX));
            counterRepository.save(tabKPIConf);
        }
        applicableKPIS.addAll(applicableKPIs);
        counterRepository.saveEntities(applicableKPIS);
        return tabKPIConf;
    }

    private List<ApplicableKPI> validateAndApplicableKPIS(ConfLevel level, Long refId, String tabId, AccessGroupPermissionCounterDTO accessGroupPermissionCounterDTO, BigInteger kpiId) {
        List<ApplicableKPI> applicableKPIS;
        if (ObjectUtils.isNotNull(tabId) && !accessGroupPermissionCounterDTO.isCountryAdmin()) {
            level = ConfLevel.STAFF;
            applicableKPIS = counterRepository.getApplicableKPI(Arrays.asList(kpiId), level, accessGroupPermissionCounterDTO.getStaffId());
        } else {
            applicableKPIS = counterRepository.getApplicableKPI(Arrays.asList(kpiId), level, refId);
        }
        if (applicableKPIS.isEmpty()) {
            exceptionService.dataNotFoundByIdException(KPIMessagesConstants.MESSAGE_COUNTER_KPI_NOTFOUND);
        }
        return applicableKPIS;
    }

    private KPI validateAndGetKpiForCopy(Long refId, BigInteger kpiId, CounterDTO counterDTO, ConfLevel level, AccessGroupPermissionCounterDTO accessGroupPermissionCounterDTO) {
        KPI kpi = counterRepository.getKPIByid(kpiId);
        if ((ObjectUtils.isNotNull(kpi.getCalculationFormula()) && !kpi.getCalculationFormula().equals(counterDTO.getCalculationFormula())) && !accessGroupPermissionCounterDTO.isCountryAdmin()) {
            exceptionService.actionNotPermittedException(KPIMessagesConstants.MESSAGE_KPI_PERMISSION);
        }
        if (Optional.ofNullable(counterRepository.getKpiByTitleAndUnitId(counterDTO.getTitle(), refId, level)).isPresent()) {
            exceptionService.duplicateDataException(KPIMessagesConstants.ERROR_KPI_NAME_DUPLICATE);
        }
        return kpi;
    }

    private void linkKpiToUncategorized(Long refId, ConfLevel level, KPI copyKpi) {
        KPICategory kpiCategory = counterRepository.getKPICategoryByName(AppConstants.UNCATEGORIZED, level, refId);
        if(ObjectUtils.isNotNull(kpiCategory)) {
            CategoryKPIConf categoryKPIConf = null;
            if (ConfLevel.UNIT.equals(level)) {
                categoryKPIConf = new CategoryKPIConf(copyKpi.getId(), kpiCategory.getId(), null, refId, level);
            } else if (ConfLevel.COUNTRY.equals(level)) {
                categoryKPIConf = new CategoryKPIConf(copyKpi.getId(), kpiCategory.getId(), refId, null, level);
            }
            if (ObjectUtils.isNotNull(categoryKPIConf)) counterRepository.save(categoryKPIConf);
        }
    }


    public TabKPIDTO getKpiPreviewWithFilter(BigInteger kpiId, Long refId, FilterCriteriaDTO filterCriteria, ConfLevel level ) {
        AccessGroupPermissionCounterDTO  accessGroupPermissionCounterDTO = userIntegrationService.getAccessGroupIdsAndCountryAdmin(UserContext.getUserDetails().getLastSelectedOrganizationId());
        TabKPIDTO tabKPIDTO = new TabKPIDTO();
        KPI kpi = counterRepository.getKPIByid(kpiId);
        tabKPIDTO.setKpi(ObjectMapperUtils.copyPropertiesByMapper(kpi, KPIDTO.class));
        filterCriteria.setKpiIds(Arrays.asList(kpiId));
        refId = ConfLevel.UNIT.equals(level) ? refId : UserContext.getUserDetails().getLastSelectedOrganizationId();
        Map<BigInteger, CommonRepresentationData> data = generateKPIData(filterCriteria, refId, accessGroupPermissionCounterDTO.getStaffId());

            tabKPIDTO.setData(data.get(kpiId));
        return tabKPIDTO;

    }

    public TabKPIDTO getKpiDataByInterval(BigInteger kpiId, Long refId, FilterCriteriaDTO filterCriteria,Long staffId ) {
        filterCriteria.setStaffId(staffId);
        return getKpiPreviewWithFilter(kpiId,refId,filterCriteria,ConfLevel.UNIT);
    }

    private TabKPIDTO getTabKpiData(KPI copyKpi, CounterDTO counterDTO, AccessGroupPermissionCounterDTO accessGroupPermissionCounterDTO) {
        TabKPIDTO tabKPIDTO = new TabKPIDTO();
        tabKPIDTO.setKpi(ObjectMapperUtils.copyPropertiesByMapper(copyKpi, KPIDTO.class));
        tabKPIDTO.getKpi().setSelectedFilters(counterDTO.getSelectedFilters());
        FilterCriteriaDTO filterCriteriaDTO = new FilterCriteriaDTO(counterDTO.getSelectedFilters(), Arrays.asList(copyKpi.getId()), accessGroupPermissionCounterDTO.getCountryId(), accessGroupPermissionCounterDTO.isCountryAdmin(), counterDTO.getKpiRepresentation(), counterDTO.getInterval(), counterDTO.getValue(), counterDTO.getFrequencyType(),counterDTO.isMultiDimensional());
        Map<BigInteger, CommonRepresentationData> data = generateKPIData(filterCriteriaDTO, UserContext.getUserDetails().getLastSelectedOrganizationId(), accessGroupPermissionCounterDTO.getStaffId());
        if(ObjectUtils.isNotNull(data)) {
            tabKPIDTO.setData(data.get(copyKpi.getId()));
        }
        tabKPIDTO.getKpi().setKpiRepresentation(counterDTO.getKpiRepresentation());
        return tabKPIDTO;
    }

    public  KPIResponseDTO generateKPISetCalculationData(FilterCriteriaDTO filters, Long organizationId, Long staffId, LocalDate startDate) {
        Map<BigInteger,ApplicableKPI> kpiIdAndApplicableKPIMap=new HashMap<>();
        List<KPI> kpis = counterRepository.getKPIsByIds(filters.getKpiIds());
        Map<BigInteger, KPI> kpiMap = kpis.stream().collect(Collectors.toMap(MongoBaseEntity::getId, kpi -> kpi));
        Map<FilterType, List> filterBasedCriteria = new HashMap<>();
        Map<BigInteger, Map<FilterType, List>> staffKpiFilterCritera = new HashMap<>();
        if (filters.getFilters() != null && ObjectUtils.isCollectionNotEmpty(filters.getFilters())) {
            for (FilterCriteria filter : filters.getFilters()) {
                filterBasedCriteria.put(filter.getType(), filter.getValues());
            }
            for(KPI kpi : kpis) {
                kpiIdAndApplicableKPIMap.put(kpi.getId(),new ApplicableKPI(filters.getKpiRepresentation(),filters.getValue(),filters.getInterval(),filters.getFrequencyType()));
            }
        } else {
            getStaffKPiFilterAndApplicableKpi(filters, staffId, kpiIdAndApplicableKPIMap, kpis, staffKpiFilterCritera,startDate);
        }
        KPIResponseDTO kpiResponseDTO = new KPISetResponseDTO();
        List<Future<KPIResponseDTO>> kpiResults = getKPIResults(filters, organizationId, kpiIdAndApplicableKPIMap, kpiMap, filterBasedCriteria, staffKpiFilterCritera,startDate);
        updateResponse(kpiResults, kpiResponseDTO);
        return kpiResponseDTO;
    }

    private void updateResponse(List<Future<KPIResponseDTO>> kpiResults, KPIResponseDTO kpiResponseDTO) {
        for (Future<KPIResponseDTO> data : kpiResults) {
            try {
                if(ObjectUtils.isNotNull(data.get())) {
                    kpiResponseDTO.setKpiId(data.get().getKpiId());
                    kpiResponseDTO.setKpiName(data.get().getKpiName());
                    kpiResponseDTO.setStaffKPIValue(data.get().getStaffKPIValue());
                    kpiResponseDTO.setKpiValue(data.get().getKpiValue());
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("error while generate KPI calculation data",e);
            }
        }
    }

    private  List<Future<KPIResponseDTO>> getKPIResults(FilterCriteriaDTO filters, Long organizationId, Map<BigInteger, ApplicableKPI> kpiIdAndApplicableKPIMap, Map<BigInteger, KPI> kpiMap,  Map<FilterType, List> filterBasedCriteria, Map<BigInteger, Map<FilterType, List>> staffKpiFilterCritera,LocalDate startDate) {
        List<Future<KPIResponseDTO>> kpiResults = new ArrayList<>();
        for (BigInteger kpiId : filters.getKpiIds()) {
            if(!counterRepository.getKPIByid(kpiId).isMultiDimensional() && ObjectUtils.isNotNull(kpiIdAndApplicableKPIMap.get(kpiId))) {
                ApplicableKPI applicableKPI = kpiIdAndApplicableKPIMap.get(kpiId);
                applicableKPI.setDateForKPISetCalculation(startDate);
                applicableKPI.setKpiRepresentation(DurationType.HOURS.equals(filters.getFrequencyType())? KPIRepresentation.REPRESENT_PER_INTERVAL:KPIRepresentation.REPRESENT_PER_STAFF);
                Callable<KPIResponseDTO> data = () -> counterServiceMapping.getService(kpiMap.get(kpiId).getType()).getCalculatedDataOfKPI(staffKpiFilterCritera.getOrDefault(kpiId, filterBasedCriteria), organizationId, kpiMap.get(kpiId), kpiIdAndApplicableKPIMap.get(kpiId));
                Future<KPIResponseDTO> responseData = executorService.submit(data);
                kpiResults.add(responseData);
            }
        }
        return kpiResults;
    }
}
