package com.kairos.service.organization;

import com.kairos.commons.utils.CommonsExceptionUtil;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.constants.AppConstants;
import com.kairos.dto.user.organization.MunicipalityDTO;
import com.kairos.dto.user.organization.ProvinceDTO;
import com.kairos.dto.user.organization.RegionDTO;
import com.kairos.dto.user.organization.ZipCodeDTO;
import com.kairos.dto.user.organization.union.*;
import com.kairos.dto.user.reason_code.ReasonCodeDTO;
import com.kairos.dto.user.staff.client.ContactAddressDTO;
import com.kairos.enums.UnionState;
import com.kairos.persistence.model.address.MunicipalityQueryResult;
import com.kairos.persistence.model.address.ZipCodeSectorQueryResult;
import com.kairos.persistence.model.client.ContactAddress;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.country.Country;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.organization.OrganizationBaseEntity;
import com.kairos.persistence.model.organization.OrganizationBasicResponse;
import com.kairos.persistence.model.organization.union.*;
import com.kairos.persistence.model.staff.StaffExperienceInExpertiseDTO;
import com.kairos.persistence.model.staff.personal_details.Staff;
import com.kairos.persistence.model.user.region.Municipality;
import com.kairos.persistence.model.user.region.ZipCode;
import com.kairos.persistence.repository.organization.OrganizationBaseRepository;
import com.kairos.persistence.repository.organization.OrganizationGraphRepository;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import com.kairos.persistence.repository.organization.union.LocationGraphRepository;
import com.kairos.persistence.repository.organization.union.SectorGraphRepository;
import com.kairos.persistence.repository.user.client.ContactAddressGraphRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.region.MunicipalityGraphRepository;
import com.kairos.persistence.repository.user.region.RegionGraphRepository;
import com.kairos.persistence.repository.user.region.ZipCodeGraphRepository;
import com.kairos.persistence.repository.user.staff.StaffGraphRepository;
import com.kairos.service.access_permisson.AccessGroupService;
import com.kairos.service.country.CountryService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.integration.ActivityIntegrationService;
import com.kairos.service.staff.StaffRetrievalService;
import com.kairos.wrapper.StaffUnionWrapper;
import io.jsonwebtoken.lang.Assert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.isCollectionEmpty;
import static com.kairos.constants.UserMessagesConstants.*;
import static com.kairos.enums.reason_code.ReasonCodeType.ORDER;

/**
 * Created by vipul on 13/2/18.
 */
@Service
@Transactional
public class UnionService {
    public static final String MESSAGE_LOCATION_NOT_FOUND = "message.location.notFound";
    @Inject
    private UnitGraphRepository unitGraphRepository;
    @Inject
    private OrganizationGraphRepository organizationGraphRepository;
    @Inject
    private CountryService countryService;
    @Inject
    private ZipCodeGraphRepository zipCodeGraphRepository;
    @Inject
    private RegionGraphRepository regionGraphRepository;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private AccessGroupService accessGroupService;
    @Inject
    private SectorGraphRepository sectorGraphRepository;
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private LocationGraphRepository locationGraphRepository;
    @Inject
    private MunicipalityGraphRepository municipalityGraphRepository;
    @Inject
    private StaffGraphRepository staffGraphRepository;
    @Inject
    private OrganizationBaseRepository organizationBaseRepository;
    @Inject
    private StaffRetrievalService staffRetrievalService;
    @Inject
    private OrganizationService organizationService;
    @Inject
    private ContactAddressGraphRepository contactAddressGraphRepository;
    @Inject
    private ActivityIntegrationService activityIntegrationService;


    public List<Sector> findAllSectorsByCountry(Long countryId) {
        List<Sector> sectors = sectorGraphRepository.findAllSectorsByCountryAndDeletedFalse(countryId);
        if (CollectionUtils.isEmpty(sectors)) {
            exceptionService.dataNotFoundByIdException(MESSAGE_SECTOR_NOTFOUND, countryId);
        }
        return sectors;
    }

    public SectorDTO createSector(SectorDTO sectorDto, Long countryId) {
        if (sectorGraphRepository.existsByName("(?i)" + sectorDto.getName(), -1l)) {
            exceptionService.duplicateDataException(MESSAGE_SECTOR_ALREADYEXISTS, sectorDto.getName());
        }
        Sector sector = new Sector(sectorDto.getName());
        Country country = countryGraphRepository.findCountryById(countryId);
        if (!Optional.ofNullable(country).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, countryId);
        }
        sector.setCountry(country);
        sectorGraphRepository.save(sector);
        sectorDto.setId(sector.getId());
        return sectorDto;
    }

    public SectorDTO updateSector(SectorDTO sectorDto, Long sectorId) {
        if (sectorGraphRepository.existsByName("(?i)" + sectorDto.getName(), sectorId)) {
            exceptionService.duplicateDataException(MESSAGE_SECTOR_ALREADYEXISTS, sectorDto.getName());
        }
        Sector sector = sectorGraphRepository.findSectorById(sectorId);
        if (!Optional.ofNullable(sector).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_SECTOR_ID_NOTFOUND, sectorId);
        }
        sector.setName(sectorDto.getName());
        sectorGraphRepository.save(sector);
        sectorDto.setId(sector.getId());
        return sectorDto;
    }

    public Boolean deleteSector(Long sectorId) {
        Sector sector = sectorGraphRepository.findSectorById(sectorId);
        if (!Optional.ofNullable(sector).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_SECTOR_ID_NOTFOUND, sectorId);
        }

        sector.setDeleted(true);
        sectorGraphRepository.save(sector);
        return true;
    }

    public List<Location> findAllLocationsByUnion(Long unionId) {
        List<Location> locations = locationGraphRepository.findLocationsByUnion(unionId);
        if (CollectionUtils.isEmpty(locations)) {
            exceptionService.dataNotFoundByIdException(MESSAGE_LOCATION_NOT_FOUND, unionId);
        }
        return locations;
    }

    public LocationDTO createLocation(LocationDTO locationDTO, Long unionId) {
        if (locationGraphRepository.existsByName("(?i)" + locationDTO.getName(), unionId, -1l)) {
            exceptionService.duplicateDataException(MESSAGE_LOCATION_NAME_ALREADYEXISTS, locationDTO.getName());
        }
        Organization union = organizationGraphRepository.findByIdAndUnionTrueAndIsEnableTrue(unionId);
        if (!Optional.ofNullable(union).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_EXPERTISE_UNIONID_NOTFOUND, unionId);
        }

        ContactAddress address = null;
        if (Optional.ofNullable(locationDTO.getAddress()).isPresent()) {
            address = getAddress(locationDTO.getAddress(), false, false, null, null, null);
        }
        Location location = new Location(locationDTO.getName(), address);
        union.getLocations().add(location);
        organizationGraphRepository.save(union);
        locationGraphRepository.save(location);
        locationDTO.setId(location.getId());


        return locationDTO;
    }

    public LocationDTO updateLocation(LocationDTO locationDTO, Long unionId, Long locationId) {
        if (locationGraphRepository.existsByName("(?i)" + locationDTO.getName(), unionId, locationId)) {
            exceptionService.duplicateDataException(MESSAGE_LOCATION_NAME_ALREADYEXISTS, locationDTO.getName());
        }
        List<LocationQueryResult> locationqueryResults = locationGraphRepository.findByIdOrNameAndDeletedFalse(locationId, locationDTO.getName(), unionId);
        if (CollectionUtils.isEmpty(locationqueryResults) || !locationqueryResults.get(0).getLocation().getId().equals(locationId)) {
            exceptionService.dataNotFoundByIdException("message.location.not.found", locationId);
        }
        if (!locationqueryResults.get(0).getUnionId().equals(unionId)) {
            exceptionService.invalidRequestException("message.unionId.invalid", unionId);
        }


        boolean zipCodeUpdated = false;
        boolean municipalityUpdated = false;

        if (Optional.ofNullable(locationqueryResults.get(0).getZipCodeId()).isPresent()) {
            zipCodeUpdated = !locationqueryResults.get(0).getZipCodeId().equals(locationDTO.getAddress().getZipCodeId());
        }
        if (Optional.ofNullable(locationqueryResults.get(0).getMunicipalityId()).isPresent()) {
            municipalityUpdated = !locationqueryResults.get(0).getMunicipalityId().equals(locationDTO.getAddress().getMunicipalityId());
        }


        Location location = locationqueryResults.get(0).getLocation();
        Long addressIdDb = locationqueryResults.get(0).getAddressId();

        if (Optional.ofNullable(locationDTO.getAddress()).isPresent()) {
            location.setAddress(getAddress(locationDTO.getAddress(), zipCodeUpdated, municipalityUpdated, addressIdDb,
                    locationqueryResults.get(0).getZipCodeId(), locationqueryResults.get(0).getMunicipalityId()));
        }
        location.setName(locationDTO.getName());

        locationGraphRepository.save(location);
        return locationDTO;
    }


    public boolean deleteLocation(Long locationId) {
        Location location = locationGraphRepository.findByIdAndDeletedFalse(locationId);
        if (!Optional.ofNullable(location).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_LOCATION_ID_NOTFOUND, locationId);
        }

        location.setDeleted(true);
        locationGraphRepository.save(location);
        return true;
    }


    public UnionDTO createUnion(UnionDTO unionData, long countryId, boolean publish) {
        Country country = validateDetails(unionData, countryId,publish);
        ContactAddress address = getContactAddress(unionData, publish);
        boolean boardingCompleted = false;
        if (publish) {
            validateAddress(unionData.getMainAddress());
            boardingCompleted = true;
            unionData.setState(UnionState.PUBLISHED);
        }
        List<Long> sectorIds = new ArrayList<>();
        List<SectorDTO> sectorDTOS = new ArrayList<>();
        filterSectorsWithIdsAndSectorWithOutId(unionData, sectorIds, sectorDTOS);
        List<Sector> sectors = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sectorIds)) {
            sectors.addAll(sectorGraphRepository.findSectorsById(sectorIds));
        }
        sectors.addAll(createSectors(countryId, sectorDTOS));
        Organization union = new Organization(unionData.getName(), sectors, address, boardingCompleted, country, true);
        if (isCollectionEmpty(union.getLocations()) && publish) {
            union.getLocations().add(new Location(AppConstants.MAIN_LOCATION, true, address));
        }
        organizationGraphRepository.save(union);
        unionData.setSectors(sectors.stream().map(sector -> new SectorDTO(sector.getId(), sector.getName())).collect(Collectors.toList()));
        unionData.setId(union.getId());
        return unionData;
    }

    private ContactAddress getContactAddress(UnionDTO unionData, boolean publish) {
        ContactAddress address = null;
        if (!Optional.ofNullable(unionData.getMainAddress()).isPresent() && publish) {
            exceptionService.invalidRequestException(MESSAGE_PUBLISH_ADDRESS_MISSING);
        } else if (Optional.ofNullable(unionData.getMainAddress()).isPresent()) {

            address = getAddress(unionData.getMainAddress(), false, false, null, null, null);
        }
        return address;
    }

    private Country validateDetails(UnionDTO unionData, long countryId,boolean publish) {
        Country country = countryGraphRepository.findOne(countryId);
        if (country == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, countryId);
        }

        if (organizationGraphRepository.existsByName("(?i)" + unionData.getName(), -1l)) {
            exceptionService.duplicateDataException(MESSAGE_UNION_NAME_EXISTS, unionData.getName());

        }
        if (publish && isCollectionEmpty(unionData.getLocationIds())) {
            exceptionService.dataNotFoundByIdException(MESSAGE_LOCATION_NOT_FOUND);
        }
        return country;
    }


    private List<Sector> createSectors(Long countryId, List<SectorDTO> sectorDTOS) {
        List<Sector> sectors = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sectorDTOS)) {
            Country country = countryGraphRepository.findCountryById(countryId);
            if (!Optional.ofNullable(country).isPresent()) {
                exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, countryId);
            }
            List<String> sectorsNames = new ArrayList<>();
            for (SectorDTO sectorDTO : sectorDTOS) {
                Sector sector = new Sector(sectorDTO.getName());
                sectorsNames.add(sectorDTO.getName().toLowerCase());
                sector.setCountry(country);
                sectors.add(sector);
            }
            sectorsNames = sectorGraphRepository.existsByNames(sectorsNames);
            if (CollectionUtils.isNotEmpty(sectorsNames)) {
                exceptionService.duplicateDataException(MESSAGE_SECTOR_ALREADYEXISTS, StringUtils.join(sectorsNames, ","));
            }
            sectorGraphRepository.saveAll(sectors);
        }
        return sectors;
    }

    private void filterSectorsWithIdsAndSectorWithOutId(UnionDTO unionData, List<Long> sectorIds, List<SectorDTO> sectorDTOS) {
        List<SectorDTO> sectorDTOSWithIds = new ArrayList<>();
        unionData.getSectors().forEach(sectorDTO -> {
            if (Optional.ofNullable(sectorDTO.getId()).isPresent()) {
                sectorIds.add(sectorDTO.getId());
                sectorDTOSWithIds.add(sectorDTO);
            } else {
                sectorDTOS.add(sectorDTO);
            }
        });
        unionData.setSectors(sectorDTOSWithIds);
    }

    public UnionDTO updateUnion(UnionDTO unionData, long countryId, Long unionId, boolean publish) {
        List<UnionDataQueryResult> unionDataQueryResults = organizationGraphRepository.getUnionCompleteById(unionId, unionData.getName());
        validateDetails(unionData, unionId, unionDataQueryResults);
        Organization union = unionDataQueryResults.get(0).getUnion();
        union.setLocations(unionDataQueryResults.get(0).getLocations());
        if (!publish && union.isBoardingCompleted()) {
            exceptionService.invalidRequestException(MESSAGE_PUBLISH_UNION_UNPUBLISH);
        }
        if (publish && isCollectionEmpty(unionDataQueryResults.get(0).getLocations())) {
            exceptionService.dataNotFoundByIdException(MESSAGE_LOCATION_NOT_FOUND, unionId);
        }
        List<Long> sectorIDsToBeCreated = new ArrayList<>();
        List<SectorDTO> sectorDTOS = new ArrayList<>();
        filterSectorsWithIdsAndSectorWithOutId(unionData, sectorIDsToBeCreated, sectorDTOS);
        Set<Long> sectorIdsDb = unionDataQueryResults.get(0).getSectors().stream().map(UserBaseEntity::getId).collect(Collectors.toSet());
        List<Long> sectorIds = new ArrayList<>(sectorIDsToBeCreated);
        sectorIDsToBeCreated.removeAll(sectorIdsDb);
        sectorIdsDb.removeAll(sectorIds);
        setSectorInfo(unionData, countryId, unionId, union, sectorIDsToBeCreated, sectorDTOS, sectorIdsDb);
        ContactAddress address = null;
        boolean zipCodeUpdated = false;
        boolean municipalityUpdated = false;
        UnionDataQueryResult unionDataQueryResult = unionDataQueryResults.get(0);
        updateDataInUnion(unionData, publish, union, address, zipCodeUpdated, municipalityUpdated, unionDataQueryResult);
        unionData.setId(union.getId());
        return unionData;
    }

    private void updateDataInUnion(UnionDTO unionData, boolean publish, Organization union, ContactAddress address, boolean zipCodeUpdated, boolean municipalityUpdated, UnionDataQueryResult unionDataQueryResult) {
        if (!Optional.ofNullable(unionData.getMainAddress()).isPresent() && publish) {
            exceptionService.invalidRequestException(MESSAGE_PUBLISH_ADDRESS_MISSING);
        } else if (Optional.ofNullable(unionData.getMainAddress()).isPresent()) {
            zipCodeUpdated = isZipCodeUpdated(unionData, zipCodeUpdated, unionDataQueryResult);
            municipalityUpdated = isMunicipalityUpdated(unionData, municipalityUpdated, unionDataQueryResult);
            Long zipCodeIdDB = Optional.ofNullable(unionDataQueryResult.getZipCode()).isPresent() ? unionDataQueryResult.getZipCode().getId() : null;
            Long municipalityIdDB = Optional.ofNullable(unionDataQueryResult.getMunicipality()).isPresent() ? unionDataQueryResult.getMunicipality().getId() : null;
            address = getAddress(unionData.getMainAddress(), zipCodeUpdated, municipalityUpdated, Optional.ofNullable(unionDataQueryResult.getAddress()).isPresent() ?
                    unionDataQueryResult.getAddress().getId() : null, zipCodeIdDB, municipalityIdDB);
            if (publish) {
                union.setBoardingCompleted(true);
                unionData.setState(UnionState.PUBLISHED);
            }
        }

        union.setName(unionData.getName());
        union.setContactAddress(address);
        if (isCollectionEmpty(union.getLocations()) && publish) {
            union.getLocations().add(new Location(AppConstants.MAIN_LOCATION, true, address));
        }
        organizationGraphRepository.save(union);
    }

    private boolean isMunicipalityUpdated(UnionDTO unionData, boolean municipalityUpdated, UnionDataQueryResult unionDataQueryResult) {
        if (Optional.ofNullable(unionDataQueryResult.getMunicipality()).isPresent()) {
            municipalityUpdated = !unionDataQueryResult.getMunicipality().getId().equals(unionData.getMainAddress().getMunicipalityId());
        }
        return municipalityUpdated;
    }

    private boolean isZipCodeUpdated(UnionDTO unionData, boolean zipCodeUpdated, UnionDataQueryResult unionDataQueryResult) {
        if (Optional.ofNullable(unionDataQueryResult.getZipCode()).isPresent()) {
            zipCodeUpdated = !unionDataQueryResult.getZipCode().getId().equals(unionData.getMainAddress().getZipCodeId());
        }
        return zipCodeUpdated;
    }

    private void validateDetails(UnionDTO unionData, Long unionId, List<UnionDataQueryResult> unionDataQueryResults) {
        if (organizationGraphRepository.existsByName("(?i)" + unionData.getName(), unionId)) {
            exceptionService.duplicateDataException(MESSAGE_UNION_NAME_EXISTS, unionData.getName());
        }

        if (CollectionUtils.isEmpty(unionDataQueryResults) || (unionDataQueryResults.size() == 1 && !unionDataQueryResults.get(0).getUnion().getId().equals(unionId))) {
            exceptionService.dataNotFoundByIdException("message.union.not.found", unionId);
        }
    }

    private void setSectorInfo(UnionDTO unionData, long countryId, Long unionId, Organization union, List<Long> sectorIDsToBeCreated, List<SectorDTO> sectorDTOS, Set<Long> sectorIdsDb) {
        if (!sectorIdsDb.isEmpty() && !union.isBoardingCompleted()) {
            unitGraphRepository.deleteUnionSectorRelationShip(new ArrayList<>(sectorIdsDb), unionId);
        } else if (!sectorIdsDb.isEmpty() && union.isBoardingCompleted()) {
            exceptionService.unsupportedOperationException(MESSAGE_SECTOR_UNLINKED);
        }
        if (!sectorIDsToBeCreated.isEmpty()) {
            unitGraphRepository.createUnionSectorRelationShip(sectorIDsToBeCreated, unionId);
        }
        if (!sectorDTOS.isEmpty()) {
            List<Sector> sectors = createSectors(countryId, sectorDTOS);
            union.getSectors().addAll(sectors);
            unionData.getSectors().addAll(sectors.stream().map(sector -> new SectorDTO(sector.getId(), sector.getName())).collect(Collectors.toList()));

        }
    }

    public boolean validateAddress(ContactAddressDTO addressDTO) {
        Assert.isTrue(StringUtils.isNotEmpty(addressDTO.getHouseNumber()), CommonsExceptionUtil.convertMessage(MESSAGE_HOUSENUMBER_NULL));
        Assert.isTrue(StringUtils.isNotEmpty(addressDTO.getProvince()), CommonsExceptionUtil.convertMessage("message.province.null"));
        Assert.isTrue(StringUtils.isNotEmpty(addressDTO.getStreet()), CommonsExceptionUtil.convertMessage("message.street.null"));
        Assert.isTrue(StringUtils.isNotEmpty(addressDTO.getCity()), CommonsExceptionUtil.convertMessage("message.city.null"));
        Assert.isTrue(StringUtils.isNotEmpty(addressDTO.getRegionName()), CommonsExceptionUtil.convertMessage("message.region.null"));
        Assert.notNull(addressDTO.getZipCodeId(), CommonsExceptionUtil.convertMessage("message.zipCodeId.null"));
        Assert.notNull(addressDTO.getMunicipalityId(), CommonsExceptionUtil.convertMessage("message.municipality.null"));
        return true;
    }


    /**
     * @param addressDTO
     * @param zipCodeUpdated
     * @param municipalityUpdated
     * @param addressId
     * @return ContactAddress
     * @Author Yatharth Govil
     * @Last ModifiedBy Yatharth Govil
     * @Description This method is used for creating address object for saving in DB
     */
    public ContactAddress getAddress(ContactAddressDTO addressDTO, boolean zipCodeUpdated, boolean municipalityUpdated, Long addressId, Long oldZipCodeId,
                                     Long oldMunicipalityId) {

        ContactAddress contactAddress = new ContactAddress(addressDTO.getHouseNumber(),
                addressDTO.getProvince(), addressDTO.getStreet(), addressDTO.getCity(), addressDTO.getRegionName());

        contactAddress.setId(addressId);
        if (Optional.ofNullable(addressDTO.getZipCodeId()).isPresent()) {
            ZipCode zipCode = zipCodeGraphRepository.findByIdDeletedFalse(addressDTO.getZipCodeId());
            if (!Optional.ofNullable(zipCode).isPresent()) {
                exceptionService.dataNotFoundByIdException(MESSAGE_ZIPCODE_NOTFOUND);
            }

            if (zipCodeUpdated) {
                zipCodeGraphRepository.deleteAddressZipcodeRelation(addressId, oldZipCodeId);
            }
            contactAddress.setZipCode(zipCode);
        }
        if (Optional.ofNullable(addressDTO.getMunicipalityId()).isPresent()) {
            Municipality municipality = municipalityGraphRepository.findByZipCodeIdandIdDeletedFalse(addressDTO.getMunicipalityId(), addressDTO.getZipCodeId());
            if (!Optional.ofNullable(municipality).isPresent()) {
                exceptionService.dataNotFoundByIdException(MESSAGE_MUNICIPALITY_NOTFOUND);
            }
            contactAddress.setMunicipality(municipality);
            if (municipalityUpdated) {
                municipalityGraphRepository.deleteAddressMunicipalityRelation(addressId, oldMunicipalityId);
            }
        }
        contactAddressGraphRepository.save(contactAddress);
        return contactAddress;
    }

    public UnionGlobalDataDTO getUnionData(Long countryId) {
        List<UnionDataQueryResult> unionDataObjects = unitGraphRepository.getUnionData(countryId);
        List<Long> locationIds = unionDataObjects.stream().flatMap(unionDataQueryResult -> unionDataQueryResult.getLocations().stream().map(Location::getId)).collect(
                Collectors.toList());
        Set<Long> municipalityIds = unionDataObjects.stream().flatMap(unionDataQueryResult -> unionDataQueryResult.getMunicipalities().stream().map(
                Municipality::getId)).collect(Collectors.toSet());
        List<LocationDataQueryResult> locationDataObjects = locationGraphRepository.getLocationData(locationIds);
        municipalityIds.addAll(locationDataObjects.stream().flatMap(locationDataQueryResult -> locationDataQueryResult.getMunicipalities().stream().map(
                Municipality::getId)).collect(Collectors.toSet()));
        List<MunicipalityQueryResult> municipalityQueryResults = municipalityGraphRepository.findMunicipalityRegionAndProvince(municipalityIds);
        Map<Long, MunicipalityQueryResult> municipalityMap = municipalityQueryResults.stream().collect(Collectors.toMap(municipalityQueryResult -> municipalityQueryResult.getMunicipality().getId(), v -> v));
        ZipCodeSectorQueryResult zipCodesSectors = zipCodeGraphRepository.getZipCodesAndSectors(countryId);

        List<ZipCodeDTO> zipCodes = null;
        List<SectorDTO> sectors = null;
        if (CollectionUtils.isNotEmpty(zipCodesSectors.getZipCodes())) {
            zipCodes = ObjectMapperUtils.copyCollectionPropertiesByMapper(zipCodesSectors.getZipCodes(), ZipCodeDTO.class);
        }
        if (CollectionUtils.isNotEmpty(zipCodesSectors.getSectors())) {
            sectors = ObjectMapperUtils.copyCollectionPropertiesByMapper(zipCodesSectors.getSectors(), SectorDTO.class);
        }
        UnionGlobalDataDTO globalDataDTO = new UnionGlobalDataDTO(zipCodes, sectors);

        Map<Long, LocationDataQueryResult> locationDataMap = locationDataObjects.stream().collect(Collectors.toMap(LocationDataQueryResult::getLocationId,
                locationDataQueryResult -> locationDataQueryResult, (first, second) -> second));
        List<UnionDataDTO> unionDataDTOS = new ArrayList<>();
        updateUnionData(unionDataObjects, municipalityMap, locationDataMap, unionDataDTOS,countryId);
        globalDataDTO.setUnions(unionDataDTOS);
        return globalDataDTO;
    }

    private void updateUnionData(List<UnionDataQueryResult> unionDataObjects, Map<Long, MunicipalityQueryResult> municipalityMap, Map<Long, LocationDataQueryResult> locationDataMap, List<UnionDataDTO> unionDataDTOS,Long countryId) {
        for (UnionDataQueryResult unionDataQueryResult : unionDataObjects) {

            UnionDataDTO unionDataDTO = new UnionDataDTO();
            unionDataDTO.setId(unionDataQueryResult.getUnion().getId());
            unionDataDTO.setName(unionDataQueryResult.getUnion().getName());
            unionDataDTO.setSectors(ObjectMapperUtils.copyCollectionPropertiesByMapper(unionDataQueryResult.getSectors(), SectorDTO.class));
            List<LocationDTO> locationDTOS = new ArrayList<>();
            List<MunicipalityDTO> municipalitiesUnion;
            if (Optional.ofNullable(unionDataQueryResult.getAddress()).isPresent()) {
                ContactAddressDTO contactAddressDTOUnion = ObjectMapperUtils.copyPropertiesByMapper(unionDataQueryResult.getAddress(), ContactAddressDTO.class);
                if (Optional.ofNullable(unionDataQueryResult.getZipCode()).isPresent()) {
                    contactAddressDTOUnion.setZipCodeId(unionDataQueryResult.getZipCode().getId());
                    contactAddressDTOUnion.setZipCodeValue(unionDataQueryResult.getZipCode().getZipCode());
                    municipalitiesUnion = ObjectMapperUtils.copyCollectionPropertiesByMapper(unionDataQueryResult.getMunicipalities(), MunicipalityDTO.class);
                    updateMunicipalities(municipalitiesUnion, municipalityMap);
                    unionDataDTO.setMunicipalities(municipalitiesUnion);
                }
                if (Optional.ofNullable(unionDataQueryResult.getMunicipality()).isPresent()) {
                    contactAddressDTOUnion.setMunicipalityId(unionDataQueryResult.getMunicipality().getId());
                    contactAddressDTOUnion.setMunicipalityName(unionDataQueryResult.getMunicipality().getName());
                }
                unionDataDTO.setMainAddress(contactAddressDTOUnion);

            }
            updateLocations(locationDataMap, unionDataQueryResult, municipalityMap, locationDTOS);
            unionDataDTO.setLocations(locationDTOS);
            unionDataDTO.setState(unionDataQueryResult.getUnion().isBoardingCompleted() ? UnionState.PUBLISHED : UnionState.DRAFT);


            unionDataDTOS.add(unionDataDTO);
        }
    }

    public void updateMunicipalities(List<MunicipalityDTO> municipalities, Map<Long, MunicipalityQueryResult> municipalityMap) {
        for (MunicipalityDTO municipalityDTO : municipalities) {
            MunicipalityQueryResult currentMunicipality = municipalityMap.get(municipalityDTO.getId());
            RegionDTO regionDTO = new RegionDTO( currentMunicipality.getRegion().getName(),currentMunicipality.getRegion().getId());
            ProvinceDTO province = new ProvinceDTO(currentMunicipality.getProvince().getId(), currentMunicipality.getProvince().getName(), regionDTO);
            municipalityDTO.setProvince(province);
        }
    }

    public void updateLocations(Map<Long, LocationDataQueryResult> locationDataMap, UnionDataQueryResult unionDataQueryResult, Map<Long, MunicipalityQueryResult>
            municipalityMap, List<LocationDTO> locationDTOS) {


        for (Location location : unionDataQueryResult.getLocations()) {
            LocationDataQueryResult locationDataQueryResult = locationDataMap.get(location.getId());
            ContactAddressDTO contactAddressDTO = null;
            List<MunicipalityDTO> municipalitiesLocation = null;
            if (Optional.ofNullable(locationDataQueryResult.getAddress()).isPresent()) {
                contactAddressDTO = ObjectMapperUtils.copyPropertiesByMapper(locationDataQueryResult.getAddress(), ContactAddressDTO.class);
                if (Optional.ofNullable(locationDataQueryResult.getZipCode()).isPresent()) {
                    contactAddressDTO.setZipCodeId(locationDataQueryResult.getZipCode().getId());
                    contactAddressDTO.setZipCodeValue(locationDataQueryResult.getZipCode().getZipCode());
                    municipalitiesLocation = ObjectMapperUtils.copyCollectionPropertiesByMapper(locationDataQueryResult.getMunicipalities(), MunicipalityDTO.class);
                    updateMunicipalities(municipalitiesLocation, municipalityMap);
                }
                if (Optional.ofNullable(locationDataQueryResult.getMunicipality()).isPresent()) {
                    contactAddressDTO.setMunicipalityId(locationDataQueryResult.getMunicipality().getId());
                    contactAddressDTO.setMunicipalityName(locationDataQueryResult.getMunicipality().getName());
                }
            }

            locationDTOS.add(new LocationDTO(location.getId(), location.getName(), contactAddressDTO, municipalitiesLocation));
        }
    }

    public StaffUnionWrapper getEmploymentDefaultData(Long unitId, Long staffId) {
        Optional<Staff> staff = staffGraphRepository.findById(staffId);
        if (!staff.isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_STAFF_UNITID_NOTFOUND);
        }
        boolean unit = unitGraphRepository.existsById(unitId);
        Organization organization = organizationGraphRepository.findOrganizationOfStaff(staffId);
        List<StaffExperienceInExpertiseDTO> staffSelectedExpertise = staffRetrievalService.getExpertiseWithExperienceByStaffIdAndUnitId(staffId, organization.getId());
        OrganizationBaseEntity organizationBaseEntity = !unit ? organization : organizationBaseRepository.findOne(unitId);
        if (!Optional.ofNullable(organizationBaseEntity).isPresent() || !Optional.ofNullable(organizationBaseEntity.getOrganizationSubTypes()).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_ORGANIZATION_NOTFOUND);

        }
        List<Long> organizationSubTypeIds = organizationBaseEntity.getOrganizationSubTypes().parallelStream().map(UserBaseEntity::getId).collect(Collectors.toList());
        List<UnionResponseDTO> unions = unitGraphRepository.getAllUnionsByOrganizationSubType(organizationSubTypeIds);
        List<OrganizationBasicResponse> organizationHierarchy = unitGraphRepository.getOrganizationHierarchy(organization.getId());

        List<ReasonCodeDTO> reasonCodeType = activityIntegrationService.getReasonCodeByType(unitId,ORDER);
        return new StaffUnionWrapper(unions, organizationHierarchy, reasonCodeType, staffSelectedExpertise);
    }
}
