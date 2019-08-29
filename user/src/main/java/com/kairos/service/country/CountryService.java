package com.kairos.service.country;

import com.google.api.services.calendar.model.Event;
import com.kairos.commons.client.RestTemplateResponseEnvelope;
import com.kairos.commons.utils.DateUtils;
import com.kairos.dto.activity.presence_type.PresenceTypeDTO;
import com.kairos.dto.activity.time_type.TimeTypeDTO;
import com.kairos.dto.activity.wta.basic_details.WTADefaultDataInfoDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.*;
import com.kairos.dto.user.country.agreement.cta.cta_response.EmploymentTypeDTO;
import com.kairos.dto.user.country.basic_details.CountryDTO;
import com.kairos.dto.user.country.time_slot.TimeSlotDTO;
import com.kairos.enums.IntegrationOperation;
import com.kairos.enums.TimeTypes;
import com.kairos.persistence.model.agreement.cta.cta_response.CTARuleTemplateDefaultDataWrapper;
import com.kairos.persistence.model.country.Country;
import com.kairos.persistence.model.country.default_data.Currency;
import com.kairos.persistence.model.country.default_data.*;
import com.kairos.persistence.model.country.employment_type.EmploymentType;
import com.kairos.persistence.model.country.functions.FunctionDTO;
import com.kairos.persistence.model.country.holiday.CountryHolidayCalender;
import com.kairos.persistence.model.organization.Level;
import com.kairos.persistence.model.organization.OrganizationType;
import com.kairos.persistence.model.organization.OrganizationTypeHierarchyQueryResult;
import com.kairos.persistence.model.organization.union.UnionQueryResult;
import com.kairos.persistence.model.user.resources.Vehicle;
import com.kairos.persistence.model.user.resources.VehicleQueryResult;
import com.kairos.persistence.repository.organization.OrganizationTypeGraphRepository;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.country.CountryHolidayCalenderGraphRepository;
import com.kairos.persistence.repository.user.country.DayTypeGraphRepository;
import com.kairos.persistence.repository.user.country.default_data.RelationTypeGraphRepository;
import com.kairos.persistence.repository.user.country.default_data.VehicalGraphRepository;
import com.kairos.persistence.repository.user.region.LevelGraphRepository;
import com.kairos.rest_client.PhaseRestClient;
import com.kairos.rest_client.PlannedTimeTypeRestClient;
import com.kairos.rest_client.activity_types.ActivityTypesRestClient;
import com.kairos.rest_client.priority_group.GenericRestClient;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.google_calender.CountryCalenderService;
import com.kairos.service.organization.OrganizationService;
import com.kairos.utils.FormatUtil;
import com.kairos.wrapper.OrganizationLevelAndUnionWrapper;
import org.apache.commons.beanutils.PropertyUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.constants.ApiConstants.API_ALL_PHASES_URL;
import static com.kairos.constants.AppConstants.*;
import static com.kairos.constants.UserMessagesConstants.*;


/**
 * Created by oodles on 16/9/16.
 */
@Service
@Transactional
public class CountryService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Inject
    private CountryHolidayCalenderGraphRepository countryHolidayGraphRepository;

    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private UnitGraphRepository unitGraphRepository;
    @Inject
    private DayTypeGraphRepository dayTypeGraphRepository;
    @Inject
    private
    OrganizationTypeGraphRepository organizationTypeGraphRepository;
    private @Inject
    CurrencyService currencyService;
    private @Inject
    TimeTypeRestClient timeTypeRestClient;
    private @Inject
    DayTypeService dayTypeService;
    private @Inject
    PhaseRestClient phaseRestClient;
    private @Inject
    ActivityTypesRestClient activityTypesRestClient;
    private @Inject
    OrganizationService organizationService;
    @Inject
    private PlannedTimeTypeRestClient plannedTimeTypeRestClient;
    private @Inject
    FunctionService functionService;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private LevelGraphRepository levelGraphRepository;
    @Inject
    private RelationTypeGraphRepository relationTypeGraphRepository;
    @Inject
    private VehicalGraphRepository vehicalGraphRepository;
    @Inject
    private GenericRestClient genericRestClient;

    /**
     * @param country
     * @return
     */
    public Map<String, Object> createCountry(Country country) {
        String name = "(?i)" + country.getName();
        List<Country> countryFound = countryGraphRepository.checkDuplicateCountry(name);
        if (countryFound == null || countryFound.isEmpty()) {
            countryGraphRepository.save(country);
            return country.retrieveDetails();
        } else {
            exceptionService.duplicateDataException(MESSAGE_COUNTRY_NAME_DUPLICATE);

        }
        return null;
    }


    /**
     * @param id
     * @return
     */
    public CountryDTO getCountryById(Long id) {
        Country country = countryGraphRepository.findOne(id);
        if (country == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, id);
        }
        CountryDTO countryDTO = new CountryDTO(country.getId(), country.getName());
        Currency currency = currencyService.getCurrencyByCountryId(id);
        countryDTO.setCurrencyId(currency.getId());
        return countryDTO;
    }


    /**
     * @param country
     * @return
     */
    public Map<String, Object> updateCountry(Country country) {
        List<Country> duplicateCountryList = countryGraphRepository.checkDuplicateCountry("(?i)" + country.getName(), country.getId());
        if (!duplicateCountryList.isEmpty()) {
            exceptionService.duplicateDataException(MESSAGE_COUNTRY_NAME_DUPLICATE);

        }
        Country currentCountry = countryGraphRepository.findOne(country.getId());
        if (country == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, country.getId());

        }
        currentCountry.setName(country.getName());
        currentCountry.setCode(country.getCode());
        currentCountry.setGoogleCalendarCode(country.getGoogleCalendarCode());
        countryGraphRepository.save(currentCountry);
        return currentCountry.retrieveDetails();
    }


    /**
     * @param id
     */
    public boolean deleteCountry(Long id) {
        Country currentCountry = countryGraphRepository.findOne(id);
        if (currentCountry != null) {
            currentCountry.setEnabled(false);
            countryGraphRepository.save(currentCountry);
            return true;
        }
        return false;
    }


    /**
     * @return
     */
    public List<Map<String, Object>> getAllCountries() {
        return FormatUtil.formatNeoResponse(countryGraphRepository.findAllCountriesMinimum());
    }


    public List<Object> getAllCountryHolidaysByCountryIdAndYear(int year, Long countryId) {
        Long start = new DateTime().withYear(year).withDayOfYear(1).getMillis();
        List<Object> objectList = new ArrayList<>();
        Long end;
        GregorianCalendar cal = new GregorianCalendar();
        if (cal.isLeapYear(year)) {
            logger.info("Leap Year found");
            end = new DateTime().withYear(year).withDayOfYear(366).getMillis();
        } else {
            logger.info("No Leap Year found");
            end = new DateTime().withYear(year).withDayOfYear(365).getMillis();
        }
        logger.info("Year Start: " + start + "  Year end:" + end);
        List<Map<String, Object>> data = countryGraphRepository.getAllCountryHolidaysByYear(countryId, start, end);
        if (!data.isEmpty()) {
            for (Map<String, Object> map : data) {
                Object o = map.get("result");
                objectList.add(o);
            }
        }


        return objectList;
    }


    private void fetchHolidaysFromGoogleCalender(Long countryId) {
        List<CountryHolidayCalender> calenderList = new ArrayList<>();
        Country country = countryGraphRepository.findOne(countryId, 2);
        if (country == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, countryId);

        }
        try {
            List<Event> eventList = CountryCalenderService.getEventsFromGoogleCalender();
            if (eventList != null) {
                logger.info("No. of Events received are: " + eventList.size());
            }
            CountryHolidayCalender holidayCalender = null;
            for (Event event : eventList) {
                logger.info("StartTime: " + event.getStart().getDateTime() + "  End: " + event.getEnd().getDateTime() + "     Visibility: " + event.getVisibility() + ":" + event.getColorId() + "   Status:" + event.getStatus() + "    Kind: " + event.getKind() + event.getStart() + "  Title" + event.getSummary());

                holidayCalender = new CountryHolidayCalender();
                holidayCalender.setHolidayTitle(event.getSummary() != null ? event.getSummary() : "");
                holidayCalender.setHolidayDate(DateUtils.asLocalDate(DateTime.parse(event.getStart().get("date").toString()).getMillis()));
                holidayCalender.setHolidayType(event.getVisibility() != null ? event.getSummary() : "");
                holidayCalender.setGoogleCalId(event.getId());

                if (countryHolidayGraphRepository.checkIfHolidayExist(event.getId(), countryId) > 0) {
                    logger.info("Duplicate Holiday");
                } else {
                    logger.info("Unique Holiday");
                    holidayCalender.setGoogleCalId(event.getId());
                    calenderList.add(holidayCalender);
                }

                // Setting holiday to Country
                if (country.getCountryHolidayCalenderList() == null) {
                    logger.info("Adding holidays");
                    country.setCountryHolidayCalenderList(calenderList);
                } else {
                    logger.info("Adding holidays again");
                    List<CountryHolidayCalender> currentHolidayList = country.getCountryHolidayCalenderList();
                    currentHolidayList.addAll(calenderList);
                    country.setCountryHolidayCalenderList(currentHolidayList);
                }
                countryGraphRepository.save(country);


            }
        } catch (Exception e) {
            logger.info("Exception occured: " + e.getCause());
            e.printStackTrace();
        }

    }


    public List<Map<String, Object>> getAllCountryAllHolidaysByCountryId(Long countryId) {
        if (countryId == null) {
            return null;
        }
        return FormatUtil.formatNeoResponse(countryGraphRepository.getCountryAllHolidays(countryId));

    }

    public boolean triggerGoogleCalenderService(Long countryId) {
        try {

            if (DateTime.now().getYear() == 2017) {
                logger.info("Running google service in 2017");
                fetchHolidaysFromGoogleCalender(countryId);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;


    }


    public List<OrganizationType> getOrganizationTypes(Long countryId) {
        return countryGraphRepository.getOrganizationTypes(countryId);
    }

    public Country getCountryByName(String name) {
        return countryGraphRepository.getCountryByName(name);
    }

    public List<Map> getCountryNameAndCodeList() {
        return countryGraphRepository.getCountryNameAndCodeList();
    }

    /**
     * @param subServiceId
     * @param organizationSubTypes
     * @auther anil maurya
     */
    public Map<String, Object> getAllCountryWithOrganizationTypes(Long subServiceId, Set<Long> organizationSubTypes) {
        Map<String, Object> response = new HashMap<>();
        for (Map<String, Object> map : countryGraphRepository.getCountryAndOrganizationTypes()) {
            response.put("countries", map.get("countries"));
            response.put("organizationTypes", map.get("types"));
        }

        List<Map<String, Object>> organizationTypes = Collections.emptyList();
        Country country = countryGraphRepository.getCountryByOrganizationService(subServiceId);
        if (country != null) {
            OrganizationTypeHierarchyQueryResult organizationTypeHierarchyQueryResult = organizationTypeGraphRepository.getOrganizationTypeHierarchy(country.getId(), organizationSubTypes);
            organizationTypes = organizationTypeHierarchyQueryResult.getOrganizationTypes();
        }
        response.put("organizationTypes", organizationTypes);
        return response;
    }

    public Country getCountryByOrganizationService(long organizationServiceId) {
        return countryGraphRepository.getCountryByOrganizationService(organizationServiceId);
    }

    public Level addLevel(long countryId, Level level) {
        Country country = countryGraphRepository.findOne(countryId);
        if (country == null) {
            logger.debug("Finding country by id::" + countryId);
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, countryId);

        }
        if(levelGraphRepository.levelExistInCountryByName(countryId,"(?i)" + level.getName(),-1L)){
            exceptionService.duplicateDataException("message.country.level.name.exist");
        }
        country.addLevel(level);
        countryGraphRepository.save(country);
        return level;
    }

    public Level updateLevel(long countryId, long levelId, Level level) {
        Level levelToUpdate = countryGraphRepository.getLevel(countryId, levelId);
        if (levelToUpdate == null) {
            logger.debug("Finding level by id::" + levelId);
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_LEVEL_ID_NOTFOUND, levelId);

        }
        if(levelGraphRepository.levelExistInCountryByName(countryId,"(?i)" + level.getName(),levelToUpdate.getId())){
            exceptionService.duplicateDataException("message.country.level.name.exist");
        }
        levelToUpdate.setName(level.getName());
        levelToUpdate.setDescription(level.getDescription());
        // TODO FIX MAKE REPOS
        levelGraphRepository.save(levelToUpdate);
        return null;
    }

    public boolean deleteLevel(long countryId, long levelId) {
        Level levelToDelete = countryGraphRepository.getLevel(countryId, levelId);
        if (levelToDelete == null) {
            logger.debug("Finding level by id::" + levelId);
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_LEVEL_ID_NOTFOUND, levelId);

        }

        levelToDelete.setEnabled(false);
        levelGraphRepository.save(levelToDelete);
        return true;
    }

    public List<Level> getLevels(long countryId) {
        return countryGraphRepository.getLevelsByCountry(countryId);
    }

    public RelationTypeDTO addRelationType(Long countryId, RelationTypeDTO relationTypeDTO) {
        Country country = countryGraphRepository.findOne(countryId);
        if (country == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, countryId);
        }

        Boolean relationTypeExistInCountryByName = countryGraphRepository.relationTypeExistInCountryByName(countryId, "(?i)" + relationTypeDTO.getName(), -1L);
        if (relationTypeExistInCountryByName) {
            exceptionService.duplicateDataException("error.RelationType.name.exist");
        }

        List<RelationType> relationTypes = new ArrayList<RelationType>();
        //check if getRelationTypes is null then it will not add in array list.
        Optional.ofNullable(country.getRelationTypes()).ifPresent(relationTypesList -> relationTypes.addAll(relationTypesList));
        RelationType relationType = new RelationType(relationTypeDTO.getName(), relationTypeDTO.getDescription());
        relationTypes.add(relationType);
        country.setRelationTypes(relationTypes);
        countryGraphRepository.save(country);
        relationTypeDTO.setId(relationType.getId());
        return relationTypeDTO;
    }

    public List<RelationTypeDTO> getRelationTypes(Long countryId) {
        return countryGraphRepository.getRelationTypesByCountry(countryId);
    }

    public boolean deleteRelationType(Long countryId, Long relationTypeId) {
        RelationType relationType = countryGraphRepository.getRelationType(countryId, relationTypeId);
        if (relationType == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_REALTIONTYPE_ID_NOTFOUND, relationTypeId);
        }
        relationType.setEnabled(false);
        relationTypeGraphRepository.save(relationType);
        return true;
    }

    public Vehicle addVehicle(Long countryId, Vehicle vehicle) {
        Country country = (Optional.ofNullable(countryId).isPresent()) ? countryGraphRepository.findOne(countryId) :
                null;
        if (!Optional.ofNullable(country).isPresent()) {
            logger.error("Finding country by id::" + countryId);
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, countryId);

        }
        boolean vehicleExistInCountryByName = countryGraphRepository.vehicleExistInCountryByName(countryId, "(?i)" + vehicle.getName(), -1L);
        if (vehicleExistInCountryByName) {
            exceptionService.duplicateDataException(MESSAGE_COUNTRY_VEHICLE_NAME_ALREADYEXIST, vehicle.getName());
        }

        country.addResources(vehicle);
        countryGraphRepository.save(country);
        return vehicle;
    }

    public List<Vehicle> getVehicleList(Long countryId) {
        if (!Optional.ofNullable(countryId).isPresent()) {
            logger.error("Finding country by id::" + countryId);
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTNULL);
            //throw new DataNotFoundByIdException("Incorrect country id");
        }
        return countryGraphRepository.getResourcesByCountry(countryId);
    }

    public List<VehicleQueryResult> getAllVehicleListWithFeatures(Long countryId) {
        if (!Optional.ofNullable(countryId).isPresent()) {
            logger.error("Finding country by id::" + countryId);
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTNULL);
            //throw new DataNotFoundByIdException("Incorrect country id");
        }
        return countryGraphRepository.getResourcesWithFeaturesByCountry(countryId);
    }

    public boolean deleteVehicle(Long countryId, Long resourcesId) {
        Vehicle vehicle = (Optional.ofNullable(countryId).isPresent() && Optional.ofNullable(resourcesId).isPresent()) ?
                countryGraphRepository.getResources(countryId, resourcesId) : null;
        if (!Optional.ofNullable(vehicle).isPresent()) {
            logger.error("Finding vehicle by id::" + resourcesId + " Country id " + countryId);
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_VEHICLE_ID_NOTFOUND);
        }
        vehicle.setEnabled(false);
        vehicalGraphRepository.save(vehicle);
        return true;
    }

    public Vehicle updateVehicle(Long countryId, Long resourcesId, Vehicle vehicle) {
        Vehicle vehicleToUpdate = (Optional.ofNullable(countryId).isPresent() && Optional.ofNullable(resourcesId).isPresent()) ?
                countryGraphRepository.getResources(countryId, resourcesId) : null;
        if (!Optional.ofNullable(vehicleToUpdate).isPresent()) {
            logger.debug("Finding vehicle by id::" + resourcesId);
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_VEHICLE_ID_NOTFOUND);

        }
        boolean vehicleExistInCountryByName = countryGraphRepository.vehicleExistInCountryByName(countryId, "(?i)" + vehicle.getName(), -1L);
        if (vehicleExistInCountryByName) {
            exceptionService.duplicateDataException(MESSAGE_COUNTRY_VEHICLE_NAME_ALREADYEXIST, vehicle.getName());
        }
        vehicleToUpdate.setName(vehicle.getName());
        vehicleToUpdate.setDescription(vehicle.getDescription());
        vehicleToUpdate.setIcon(vehicle.getIcon());
        return vehicalGraphRepository.save(vehicleToUpdate);
    }

    /**
     * @param countryId
     * @return
     * @auther anil maurya
     */
    //TODO Reduce web service calls/multiple calls
    public CTARuleTemplateDefaultDataWrapper getDefaultDataForCTATemplate(Long countryId, Long unitId) {
        List<ActivityTypeDTO> activityTypeDTOS;
        List<PhaseResponseDTO> phases;
        if (Optional.ofNullable(unitId).isPresent()) {
            countryId = getCountryIdByUnitId(unitId);
            activityTypeDTOS = activityTypesRestClient.getActivitiesForUnit(unitId);
            phases = genericRestClient.publishRequest(null, unitId, true, IntegrationOperation.GET, API_ALL_PHASES_URL, null, new ParameterizedTypeReference<RestTemplateResponseEnvelope<List<PhaseResponseDTO>>>() {
            });
        } else {
            activityTypeDTOS = activityTypesRestClient.getActivitiesForCountry(countryId);
            phases = phaseRestClient.getPhases(countryId);

        }

        Set<BigInteger> activityCategoriesIds = activityTypeDTOS.stream().map(activityTypeDTO -> activityTypeDTO.getCategoryId()).collect(Collectors.toSet());
        List<ActivityCategoryDTO> activityCategories = activityTypesRestClient.getActivityCategoriesForCountry(countryId, activityCategoriesIds);

        List<CurrencyDTO> currencies = currencyService.getCurrencies(countryId);
        List<EmploymentType> employmentTypes = countryGraphRepository.getEmploymentTypeByCountry(countryId, false);
        TimeTypeDTO timeType = timeTypeRestClient.getAllTimeTypes(countryId).stream().filter(t -> t.getTimeTypes().equals(TimeTypes.WORKING_TYPE.toValue())).findFirst().get();
        List<TimeTypeDTO> timeTypes = Arrays.asList(timeType);
        List<PresenceTypeDTO> plannedTime = plannedTimeTypeRestClient.getAllPlannedTimeTypes(countryId);
        List<DayType> dayTypes = dayTypeService.getAllDayTypeByCountryId(countryId);

        List<FunctionDTO> functions = functionService.getFunctionsIdAndNameByCountry(countryId);

        //wrap data into wrapper class
        CTARuleTemplateDefaultDataWrapper ctaRuleTemplateDefaultDataWrapper = new CTARuleTemplateDefaultDataWrapper();
        ctaRuleTemplateDefaultDataWrapper.setCurrencies(currencies);
        ctaRuleTemplateDefaultDataWrapper.setPhases(phases);
        ctaRuleTemplateDefaultDataWrapper.setFunctions(functions);

        List<EmploymentTypeDTO> employmentTypeDTOS = employmentTypes.stream().map(employmentType -> {
            EmploymentTypeDTO employmentTypeDTO = new EmploymentTypeDTO();
            BeanUtils.copyProperties(employmentType, employmentTypeDTO);
            return employmentTypeDTO;
        }).collect(Collectors.toList());
        ctaRuleTemplateDefaultDataWrapper.setEmploymentTypes(employmentTypeDTOS);
        ctaRuleTemplateDefaultDataWrapper.setTimeTypes(timeTypes);
        ctaRuleTemplateDefaultDataWrapper.setPlannedTime(plannedTime);

        List<DayTypeDTO> dayTypeDTOS = dayTypes.stream().map(dayType -> {
            DayTypeDTO dayTypeDTO = new DayTypeDTO();
            BeanUtils.copyProperties(dayType, dayTypeDTO);
            return dayTypeDTO;
        }).collect(Collectors.toList());
        ctaRuleTemplateDefaultDataWrapper.setDayTypes(dayTypeDTOS);

        ctaRuleTemplateDefaultDataWrapper.setActivityTypes(activityTypeDTOS);
        ctaRuleTemplateDefaultDataWrapper.setActivityCategories(activityCategories);

        ctaRuleTemplateDefaultDataWrapper.setHolidayMapList(this.getAllCountryAllHolidaysByCountryId(countryId));
        return ctaRuleTemplateDefaultDataWrapper;
    }

    // For getting all OrganizationLevel and Unions
    public OrganizationLevelAndUnionWrapper getUnionAndOrganizationLevels(Long countryId) {
        List<UnionQueryResult> unions = unitGraphRepository.findAllUnionsByCountryId(countryId);
        List<Level> organizationLevels = countryGraphRepository.getLevelsByCountry(countryId);
        return new OrganizationLevelAndUnionWrapper(unions, organizationLevels);
    }

    public WTADefaultDataInfoDTO getWtaTemplateDefaultDataInfo(Long countryId) {
        List<PresenceTypeDTO> presenceTypeDTOS = plannedTimeTypeRestClient.getAllPlannedTimeTypes(countryId);
        List<DayType> dayTypes = dayTypeGraphRepository.findByCountryId(countryId);
        List<DayTypeDTO> dayTypeDTOS = new ArrayList<>();
        List<PresenceTypeDTO> presenceTypeDTOS1 = presenceTypeDTOS.stream().map(p -> new PresenceTypeDTO(p.getName(), p.getId())).collect(Collectors.toList());
        dayTypes.forEach(dayType -> {
            DayTypeDTO dayTypeDTO = new DayTypeDTO();
            try {
                PropertyUtils.copyProperties(dayTypeDTO, dayType);
                dayTypeDTOS.add(dayTypeDTO);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
        return new WTADefaultDataInfoDTO(dayTypeDTOS, presenceTypeDTOS1, getDefaultTimeSlot(), countryId);
    }

    public List<TimeSlotDTO> getDefaultTimeSlot() {
        List<TimeSlotDTO> timeSlotDTOS = new ArrayList<>(3);
        timeSlotDTOS.add(new TimeSlotDTO(DAY, DAY_START_HOUR, 00, DAY_END_HOUR, 00));
        timeSlotDTOS.add(new TimeSlotDTO(EVENING, EVENING_START_HOUR, 00, EVENING_END_HOUR, 00));
        timeSlotDTOS.add(new TimeSlotDTO(NIGHT, NIGHT_START_HOUR, 00, NIGHT_END_HOUR, 00));
        return timeSlotDTOS;
    }


    public boolean mappingPayRollListToCountry(long countryId, Set<BigInteger> payRollTypeIds) {
        Country country = countryGraphRepository.findOne(countryId);
        if (country != null && !country.isDeleted()) {
            country.setPayRollTypeIds(payRollTypeIds);
            countryGraphRepository.save(country);
        } else {
            exceptionService.dataNotFoundByIdException(MESSAGE_DATANOTFOUND, "Country", countryId);
        }
        return true;
    }

    public Long getCountryIdByUnitId(Long unitId) {
       return countryGraphRepository.getCountryIdByUnitId(unitId);
    }

    public List<Long> getAllUnits(long countryId) {
        return organizationService.getAllOrganizationIds();
    }
}
