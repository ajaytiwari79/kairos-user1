package com.kairos.service.resources;

/**
 * Created by oodles on 17/10/16.
 */

import com.kairos.commons.utils.DateUtils;
import com.kairos.dto.user_context.UserContext;
import com.kairos.persistence.model.organization.Unit;
import com.kairos.persistence.model.user.resources.*;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import com.kairos.persistence.repository.user.resources.ResourceGraphRepository;
import com.kairos.persistence.repository.user.resources.ResourceUnAvailabilityGraphRepository;
import com.kairos.persistence.repository.user.resources.ResourceUnavailabilityRelationshipRepository;
import com.kairos.persistence.repository.user.resources.VehicleGraphRepository;
import com.kairos.service.country.CountryService;
import com.kairos.service.exception.ExceptionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import static com.kairos.commons.utils.DateUtils.MONGODB_QUERY_DATE_FORMAT;
import static com.kairos.constants.UserMessagesConstants.*;

/**
 * Calls ResourceGraphRepository to perform CRUD operation on Resources.
 */
@Service
@Transactional
public class ResourceService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    ResourceGraphRepository resourceGraphRepository;
    @Inject
    UnitGraphRepository unitGraphRepository;
    @Inject
    CountryService countryService;
    @Inject
    VehicleGraphRepository vehicleGraphRepository;
    @Inject
    ResourceUnavailabilityRelationshipRepository unavailabilityRelationshipRepository;
    @Inject
    private ExceptionService exceptionService;
@Inject private ResourceUnAvailabilityGraphRepository resourceUnAvailabilityGraphRepository;
    /**
     * Get the List of Resources in Organization
     *
     * @param organizationId
     * @return list of Resources
     */
    public List<Map<String, Object>> getAllResourcesByOrgId(Long organizationId) {
        return resourceGraphRepository.getResourceByOrganizationId(organizationId);

    }


    /**
     * Get a Resource by Id
     *
     * @param resourceId
     * @return resource
     */
    public Resource getResourceById(Long resourceId) {
        return resourceGraphRepository.findOne(resourceId);
    }


    /**
     * Calls ResourceGraphRepository , find Resource by id as provided in method argument
     * and return updated Resource
     *
     * @param resource
     * @return resource
     */
    public Resource updateResource(Resource resource) {
        Resource currentResource = resourceGraphRepository.findOne(resource.getId());
        if (currentResource != null) {
            currentResource = resource;
        }
        return resourceGraphRepository.save(currentResource);
    }

    /**
     * Add a Resource to Organization
     *
     * @param organizationId
     * @param resource
     * @return resource
     */
    public Resource addResourceToOrganization(Long organizationId, Resource resource) {
        Unit currentUnit = unitGraphRepository.findOne(organizationId);

        if (currentUnit.getResourceList() == null) {
            currentUnit.setResourceList(Arrays.asList(resourceGraphRepository.save(resource)));
            unitGraphRepository.save(currentUnit);
            return resource;
        }
        List<Resource> resourceList = currentUnit.getResourceList();
        resourceList.add(resource);
        unitGraphRepository.save(currentUnit);
        return resource;
    }

    /**
     * Safe-Delete a Resource by id
     *
     * @param resourceId
     */
    public boolean deleteResource(Long resourceId) {
        Resource resource = resourceGraphRepository.findOne(resourceId);
        if (Optional.ofNullable(resource).isPresent()) {
            resource.setDeleted(true);
            return resourceGraphRepository.save(resource) != null;
        }
        exceptionService.dataNotFoundByIdException(MESSAGE_RESOURCE_ID_NOTFOUND);
        return false;
    }

    public List<ResourceWrapper> getUnitResources(Long unitId) {
        return resourceGraphRepository.getResources(unitId);
    }

    public List<ResourceWrapper> getOrganizationResourcesWithUnAvailability(Long unitId, String date) {
        Instant instant = Instant.parse(date);
        LocalDateTime startDate = LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
        return resourceGraphRepository.getResourcesWithUnAvailability(unitId, startDate.getMonth().getValue(), startDate.getYear());
    }

    public ResourceTypeWrapper getUnitResourcesTypes(Long unitId) {
        Long countryId = UserContext.getUserDetails().getCountryId();
        List<Vehicle> vehicleTypes = countryService.getVehicleList(countryId);
        return new ResourceTypeWrapper(vehicleTypes, Arrays.asList(FuelType.values()));
    }


    public Resource addResource(ResourceDTO resourceDTO, Long unitId) throws ParseException {
        Unit unit = (Optional.ofNullable(unitId).isPresent()) ? unitGraphRepository.findOne(unitId) : null;
        if (!Optional.ofNullable(unit).isPresent()) {
            logger.error("Incorrect unit id " + unitId);
            exceptionService.dataNotFoundByIdException(MESSAGE_UNIT_ID_NOTFOUND,unitId);

        }
        Resource dbResourceObject = resourceGraphRepository.getResourceByRegistrationNumberAndUnit(unitId,resourceDTO.getRegistrationNumber());

        if(Optional.ofNullable(dbResourceObject).isPresent()){
            exceptionService.duplicateDataException(MESSAGE_RESOURCE_ALREADYEXIST,resourceDTO.getRegistrationNumber());

        }

        Vehicle vehicle = vehicleGraphRepository.findOne(resourceDTO.getVehicleTypeId());
        if (!Optional.ofNullable(vehicle).isPresent()) {
            logger.error("Vehicle type not found " + resourceDTO.getVehicleTypeId());
            exceptionService.dataNotFoundByIdException(MESSAGE_RESOURCE_VEHICLETYPE_NOTFOUND);

        }
        Resource resource = new Resource(vehicle, resourceDTO.getRegistrationNumber(), resourceDTO.getNumber(),
                resourceDTO.getModelDescription(), resourceDTO.getCostPerKM(), resourceDTO.getFuelType());
        if(!StringUtils.isBlank(resourceDTO.getDecommissionDate())){
            LocalDateTime decommissionDate = LocalDateTime.ofInstant(DateUtils.convertToOnlyDate(resourceDTO.getDecommissionDate(),
                    MONGODB_QUERY_DATE_FORMAT).toInstant(), ZoneId.systemDefault()).
                    withHour(0).withMinute(0).withSecond(0).withNano(0);
            resource.setDecommissionDate(decommissionDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        unit.addResource(resource);
        unitGraphRepository.save(unit);
        return resource;
    }

    public Resource updateResource(ResourceDTO resourceDTO, Long resourceId) throws ParseException {
        Resource resource = (Optional.ofNullable(resourceId).isPresent()) ? resourceGraphRepository.findOne(resourceId) : null;
        if (!Optional.ofNullable(resource).isPresent()) {
            logger.error("Incorrect resource id" + resourceId);
            exceptionService.dataNotFoundByIdException(MESSAGE_RESOURCE_ID_NOTFOUND);

        }
        Vehicle vehicle = vehicleGraphRepository.findOne(resourceDTO.getVehicleTypeId());
        if (!Optional.ofNullable(vehicle).isPresent()) {
            logger.error("Vehicle type not found " + resourceDTO.getVehicleTypeId());
            exceptionService.dataNotFoundByIdException(MESSAGE_RESOURCE_VEHICLETYPE_NOTFOUND);

        }
        resource.setVehicleType(vehicle);
        resource.setNumber(resourceDTO.getNumber());
        resource.setRegistrationNumber(resourceDTO.getRegistrationNumber());
        resource.setModelDescription(resourceDTO.getModelDescription());
        resource.setCostPerKM(resourceDTO.getCostPerKM());
        resource.setFuelType(resourceDTO.getFuelType());
        if(!StringUtils.isBlank(resourceDTO.getDecommissionDate())){
            LocalDateTime decommissionDate = LocalDateTime.ofInstant(DateUtils.convertToOnlyDate(resourceDTO.getDecommissionDate(),
                    MONGODB_QUERY_DATE_FORMAT).toInstant(), ZoneId.systemDefault()).
                    withHour(0).withMinute(0).withSecond(0).withNano(0);
            resource.setDecommissionDate(decommissionDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        return resourceGraphRepository.save(resource);
    }

    public List<ResourceUnAvailability> setResourceUnavailability(ResourceUnavailabilityDTO unavailabilityDTO, Long resourceId) {
        Resource resource = resourceGraphRepository.findOne(resourceId);
        if (!Optional.ofNullable(resource).isPresent()) {
            logger.error("Resource not found by id " + resource);
            exceptionService.dataNotFoundByIdException(MESSAGE_RESOURCE_ID_NOTFOUND);

        }
        List<ResourceUnavailabilityRelationship> resourceUnavailabilityRelationships = new ArrayList<>
                (unavailabilityDTO.getUnavailabilityDates().size());

        List<ResourceUnAvailability> resourceUnAvailabilities = new ArrayList<>();
        for (String unavailabilityDate : unavailabilityDTO.getUnavailabilityDates()) {
            ResourceUnAvailability resourceUnAvailability = new ResourceUnAvailability(unavailabilityDTO.isFullDay()).
                    setUnavailability(unavailabilityDTO, unavailabilityDate);
            try {
                LocalDateTime startDateIncludeTime = LocalDateTime.ofInstant(DateUtils.convertToOnlyDate(unavailabilityDate,
                        MONGODB_QUERY_DATE_FORMAT).toInstant(), ZoneId.systemDefault());
                ResourceUnavailabilityRelationship resourceUnavailabilityRelationship = new ResourceUnavailabilityRelationship(resource,
                        resourceUnAvailability, startDateIncludeTime.getMonth().getValue(), startDateIncludeTime.getYear());
                resourceUnavailabilityRelationships.add(resourceUnavailabilityRelationship);
                resourceUnAvailabilities.add(resourceUnAvailability);
            } catch (ParseException e) {
                exceptionService.internalServerError(ERROR_RESOURCE_DATE_INCORRECT);

            }
        }
        unavailabilityRelationshipRepository.saveAll(resourceUnavailabilityRelationships);
        return resourceUnAvailabilities;
    }

    public ResourceUnAvailability updateResourceUnavailability(ResourceUnavailabilityDTO unavailabilityDTO, Long unAvailabilityId,
                                                                  Long resourceId) throws ParseException {
        ResourceUnAvailability resourceUnAvailability = resourceGraphRepository.getResourceUnavailabilityById(resourceId,unAvailabilityId);
        if(!Optional.ofNullable(resourceUnAvailability).isPresent()){
            logger.error("Incorrect id of Resource unavailability " + unAvailabilityId);
            exceptionService.dataNotFoundByIdException(MESSAGE_RESOURCE_UNAVAILABILITY_NOTFOUND);

        }
        if(unavailabilityDTO.isFullDay()){
            resourceUnAvailability.setFullDay(unavailabilityDTO.isFullDay());
            resourceUnAvailability.setStartTime(null);
            resourceUnAvailability.setEndTime(null);
        } else {
            if(!unavailabilityDTO.isFullDay() && !StringUtils.isBlank(unavailabilityDTO.getStartTime())){
                LocalDateTime timeFrom = LocalDateTime.ofInstant(DateUtils.convertToOnlyDate(unavailabilityDTO.getStartTime(),
                        MONGODB_QUERY_DATE_FORMAT).toInstant(), ZoneId.systemDefault());
                resourceUnAvailability.setStartTime(timeFrom.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }

            if(!unavailabilityDTO.isFullDay() && !StringUtils.isBlank(unavailabilityDTO.getEndTime())){
                LocalDateTime timeTo = LocalDateTime.ofInstant(DateUtils.convertToOnlyDate(unavailabilityDTO.getEndTime(),
                        MONGODB_QUERY_DATE_FORMAT).toInstant(), ZoneId.systemDefault());
                resourceUnAvailability.setEndTime(timeTo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
        }
        return resourceUnAvailabilityGraphRepository.save(resourceUnAvailability);
    }

    public void deleteUnavailability(Long resourceId, Long unavailableDateId) {
        resourceGraphRepository.deleteResourceUnavailability(resourceId, unavailableDateId);
    }

    public List<ResourceUnAvailability> getResourceUnAvailability(Long resourceId,String date){
        Resource resource = resourceGraphRepository.findOne(resourceId);
        if(!Optional.ofNullable(resource).isPresent()){
            logger.error("Resource not found by id " + resource);
                exceptionService.dataNotFoundByIdException(MESSAGE_RESOURCE_ID_NOTFOUND);

        }
        Instant instant = Instant.parse(date);
        LocalDateTime startDate = LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
        return resourceGraphRepository.getResourceUnavailability(resourceId,startDate.getMonth().getValue(),startDate.getYear());
    }


}
