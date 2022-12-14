package com.kairos.service.country;

import com.kairos.commons.custom_exception.DataNotFoundByIdException;
import com.kairos.commons.utils.CommonsExceptionUtil;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.user.country.LevelDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.CountryHolidayCalenderDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.EmploymentTypeDTO;
import com.kairos.dto.user.country.basic_details.CountryDTO;
import com.kairos.dto.user_context.UserContext;
import com.kairos.persistence.model.agreement.cta.cta_response.CTARuleTemplateDefaultDataWrapper;
import com.kairos.persistence.model.country.Country;
import com.kairos.persistence.model.country.default_data.Currency;
import com.kairos.persistence.model.country.default_data.CurrencyDTO;
import com.kairos.persistence.model.country.default_data.RelationType;
import com.kairos.persistence.model.country.default_data.RelationTypeDTO;
import com.kairos.persistence.model.country.employment_type.EmploymentType;
import com.kairos.persistence.model.country.functions.FunctionDTO;
import com.kairos.persistence.model.organization.Level;
import com.kairos.persistence.model.organization.union.UnionQueryResult;
import com.kairos.persistence.model.user.resources.Vehicle;
import com.kairos.persistence.model.user.resources.VehicleQueryResult;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.country.default_data.RelationTypeGraphRepository;
import com.kairos.persistence.repository.user.country.default_data.VehicalGraphRepository;
import com.kairos.persistence.repository.user.region.LevelGraphRepository;
import com.kairos.rest_client.PhaseRestClient;
import com.kairos.rest_client.PlannedTimeTypeRestClient;
import com.kairos.rest_client.activity_types.ActivityTypesRestClient;
import com.kairos.rest_client.priority_group.GenericRestClient;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.integration.ActivityIntegrationService;
import com.kairos.service.organization.OrganizationService;
import com.kairos.utils.FormatUtil;
import com.kairos.wrapper.OrganizationLevelAndUnionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.isNull;
import static com.kairos.constants.UserMessagesConstants.*;


/**
 * Created by oodles on 16/9/16.
 */
@Service
@Transactional
public class CountryService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private UnitGraphRepository unitGraphRepository;
    private @Inject
    CurrencyService currencyService;
    private @Inject
    TimeTypeRestClient timeTypeRestClient;
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
    @Inject
    private ActivityIntegrationService activityIntegrationService;


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



    public CountryDTO getCountryById(Long id) {
        Country country = findById(id);
        CountryDTO countryDTO = new CountryDTO(country.getId(), country.getName());
        Currency currency = currencyService.getCurrencyByCountryId(id);
        countryDTO.setCurrencyId(currency.getId());
        return countryDTO;
    }



    public Map<String, Object> updateCountry(Country country) {
        List<Country> duplicateCountryList = countryGraphRepository.checkDuplicateCountry("(?i)" + country.getName(), country.getId());
        if (!duplicateCountryList.isEmpty()) {
            exceptionService.duplicateDataException(MESSAGE_COUNTRY_NAME_DUPLICATE);

        }
        Country currentCountry = countryGraphRepository.findOne(country.getId());
        currentCountry.setName(country.getName());
        currentCountry.setCode(country.getCode());
        currentCountry.setGoogleCalendarCode(country.getGoogleCalendarCode());
        countryGraphRepository.save(currentCountry);
        return currentCountry.retrieveDetails();
    }



    public boolean deleteCountry(Long id) {
        Country currentCountry = countryGraphRepository.findOne(id);
        if (currentCountry != null) {
            currentCountry.setEnabled(false);
            countryGraphRepository.save(currentCountry);
            return true;
        }
        return false;
    }

    public List<Map<String, Object>> getAllCountries() {
        return FormatUtil.formatNeoResponse(countryGraphRepository.findAllCountriesMinimum());
    }

    public List<CountryHolidayCalenderDTO> getAllCountryAllHolidaysByCountryId(Long countryId) {
       return activityIntegrationService.getCountryHolidaysByCountryId(countryId);

    }

    public List<Map> getCountryNameAndCodeList() {
        return countryGraphRepository.getCountryNameAndCodeList();
    }


    public Country getCountryByOrganizationService(long organizationServiceId) {
        return countryGraphRepository.getCountryByOrganizationService(organizationServiceId);
    }

    public Level addLevel(long countryId, Level level) {
        Country country = findById(countryId);
        if(levelGraphRepository.levelExistInCountryByName(countryId,"(?i)" + level.getName(),-1L)){
            exceptionService.duplicateDataException("message.country.level.name.exist");
        }
        country.addLevel(level);
        countryGraphRepository.save(country);
        return level;
    }

    public Level updateLevel(long countryId, long levelId, Level level) {
        Level levelToUpdate = countryGraphRepository.getLevel(countryId, levelId);
        if (levelToUpdate != null) {
            if(levelGraphRepository.levelExistInCountryByName(countryId,"(?i)" + level.getName(),levelToUpdate.getId())){
                exceptionService.duplicateDataException("message.country.level.name.exist");
            }
            levelToUpdate.setName(level.getName());
            levelToUpdate.setDescription(level.getDescription());
            levelGraphRepository.save(levelToUpdate);

        }
        return levelToUpdate;
    }

    public boolean deleteLevel(long countryId, long levelId) {
        if(countryGraphRepository.isLinkedPayTablePublished(countryId, levelId)){
            exceptionService.actionNotPermittedException(MESSAGE_COUNTRY_LEVEL_CANNOT_DELETE);
        }
        Level levelToDelete = countryGraphRepository.getLevel(countryId, levelId);
        if (levelToDelete != null) {
            levelToDelete.setEnabled(false);
            levelGraphRepository.save(levelToDelete);
        }
        return true;
    }

    public List<LevelDTO> getLevels(long countryId) {
        List<Level> levels = countryGraphRepository.getLevelsByCountry(countryId);
        return ObjectMapperUtils.copyCollectionPropertiesByMapper(levels,LevelDTO.class);
    }

    public RelationTypeDTO addRelationType(Long countryId, RelationTypeDTO relationTypeDTO) {
        Country country = findById(countryId);

        boolean relationTypeExistInCountryByName = countryGraphRepository.relationTypeExistInCountryByName(countryId, "(?i)" + relationTypeDTO.getName(), -1L);
        if (relationTypeExistInCountryByName) {
            exceptionService.duplicateDataException("error.RelationType.name.exist");
        }
        List<RelationType> relationTypes = new ArrayList<>();
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
        List<RelationType> relationTypes = countryGraphRepository.getRelationTypesByCountry(countryId);
        return ObjectMapperUtils.copyCollectionPropertiesByMapper(relationTypes, RelationTypeDTO.class);
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
            logger.error("Finding country by id::{}" , countryId);
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
        }
        return countryGraphRepository.getResourcesByCountry(countryId);
    }

    public List<VehicleQueryResult> getAllVehicleListWithFeatures(Long countryId) {
        if (!Optional.ofNullable(countryId).isPresent()) {
            logger.error("Finding country by id::" + countryId);
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTNULL);
        }
        return countryGraphRepository.getResourcesWithFeaturesByCountry(countryId);
    }

    public boolean deleteVehicle(Long countryId, Long resourcesId) {
        Vehicle vehicle = (Optional.ofNullable(countryId).isPresent() && Optional.ofNullable(resourcesId).isPresent()) ?
                countryGraphRepository.getResources(countryId, resourcesId) : null;
        if (!Optional.ofNullable(vehicle).isPresent()) {
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


    public CTARuleTemplateDefaultDataWrapper getDefaultDataForCTA(Long countryId, Long unitId) {
        if(isNull(countryId)){
            countryId = UserContext.getUserDetails().getCountryId();
        }
        List<CurrencyDTO> currencies = currencyService.getCurrencies(countryId);
        List<EmploymentType> employmentTypes = countryGraphRepository.getEmploymentTypeByCountry(countryId, false);
        List<FunctionDTO> functions = functionService.getFunctionsIdAndNameByCountry(countryId);
        List<EmploymentTypeDTO> employmentTypeDTOS = getEmploymentTypeDTOS(employmentTypes);
        return CTARuleTemplateDefaultDataWrapper.builder().functions(functions).employmentTypes(employmentTypeDTOS).currencies(currencies).build();
    }


    private List<EmploymentTypeDTO> getEmploymentTypeDTOS(List<EmploymentType> employmentTypes) {
        return employmentTypes.stream().map(employmentType -> {
                EmploymentTypeDTO employmentTypeDTO = new EmploymentTypeDTO();
                BeanUtils.copyProperties(employmentType, employmentTypeDTO);
                return employmentTypeDTO;
            }).collect(Collectors.toList());
    }

    // For getting all OrganizationLevel and Unions
    public OrganizationLevelAndUnionWrapper getUnionAndOrganizationLevels(Long countryId) {
        List<UnionQueryResult> unions = unitGraphRepository.findAllUnionsByCountryId(countryId);
        List<Level> organizationLevels = countryGraphRepository.getLevelsByCountry(countryId);
        return new OrganizationLevelAndUnionWrapper(unions, organizationLevels);
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

    public List<Long> getAllUnits() {
        return organizationService.getAllUnitIds();
    }

    public Country findById(Long countryId){
        return countryGraphRepository.findById(countryId).orElseThrow(()->new DataNotFoundByIdException(CommonsExceptionUtil.convertMessage(MESSAGE_COUNTRY_ID_NOTFOUND, countryId)));
    }

}
