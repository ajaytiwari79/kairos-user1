package com.kairos.service.organization;

import com.kairos.persistence.model.auth.User;
import com.kairos.persistence.model.client.ContactAddress;
import com.kairos.persistence.model.country.Country;
import com.kairos.persistence.model.country.default_data.BusinessType;
import com.kairos.persistence.model.country.default_data.CompanyCategory;
import com.kairos.persistence.model.country.default_data.account_type.AccountType;
import com.kairos.persistence.model.organization.*;
import com.kairos.persistence.model.organization.OrganizationContactAddress;
import com.kairos.persistence.model.organization.company.CompanyValidationQueryResult;
import com.kairos.persistence.model.organization.time_slot.TimeSlot;
import com.kairos.persistence.model.staff.personal_details.StaffPersonalDetailDTO;
import com.kairos.persistence.model.user.open_shift.OrganizationTypeAndSubType;
import com.kairos.persistence.model.user.region.Municipality;
import com.kairos.persistence.model.user.region.ZipCode;
import com.kairos.persistence.repository.organization.OrganizationGraphRepository;
import com.kairos.persistence.repository.organization.OrganizationTypeGraphRepository;
import com.kairos.persistence.repository.organization.time_slot.TimeSlotGraphRepository;
import com.kairos.persistence.repository.user.auth.UserGraphRepository;
import com.kairos.persistence.repository.user.client.ContactAddressGraphRepository;
import com.kairos.persistence.repository.user.country.BusinessTypeGraphRepository;
import com.kairos.persistence.repository.user.country.CompanyCategoryGraphRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.country.default_data.AccountTypeGraphRepository;
import com.kairos.persistence.repository.user.region.LevelGraphRepository;
import com.kairos.persistence.repository.user.region.MunicipalityGraphRepository;
import com.kairos.persistence.repository.user.region.RegionGraphRepository;
import com.kairos.persistence.repository.user.region.ZipCodeGraphRepository;
import com.kairos.service.access_permisson.AccessGroupService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.integration.ActivityIntegrationService;
import com.kairos.service.staff.StaffService;
import com.kairos.user.organization.*;
import com.kairos.user.organization.UnitManagerDTO;
import com.kairos.user.staff.staff.StaffCreationDTO;
import com.kairos.util.FormatUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.kairos.constants.AppConstants.*;
import static com.kairos.util.validator.company.OrganizationDetailsValidator.*;

/**
 * CreatedBy vipulpandey on 17/8/18
 **/
@Service
@Transactional
public class CompanyCreationService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private OrganizationGraphRepository organizationGraphRepository;
    @Inject
    private AccountTypeGraphRepository accountTypeGraphRepository;
    @Inject
    private CompanyCategoryGraphRepository companyCategoryGraphRepository;
    @Inject
    private BusinessTypeGraphRepository businessTypeGraphRepository;
    @Inject
    private ZipCodeGraphRepository zipCodeGraphRepository;
    @Inject
    private MunicipalityGraphRepository municipalityGraphRepository;
    @Inject
    private RegionGraphRepository regionGraphRepository;
    @Inject
    private ContactAddressGraphRepository contactAddressGraphRepository;
    @Inject
    private StaffService staffService;
    @Inject
    private UserGraphRepository userGraphRepository;
    @Inject
    private OrganizationTypeGraphRepository organizationTypeGraphRepository;
    @Inject
    private LevelGraphRepository levelGraphRepository;
    @Inject
    private CompanyDefaultDataService companyDefaultDataService;
    @Inject
    private AccessGroupService accessGroupService;
    @Inject
    private TimeSlotService timeSlotService;
    @Inject
    private ActivityIntegrationService activityIntegrationService;
    @Inject
    private TimeSlotGraphRepository timeSlotGraphRepository;


    public OrganizationBasicDTO createCompany(OrganizationBasicDTO orgDetails, long countryId, Long organizationId) {
        Country country = countryGraphRepository.findOne(countryId);
        if (!Optional.ofNullable(country).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.country.id.notFound", countryId);
        }
        String kairosId = validateNameAndDesiredUrlOfOrganization(orgDetails);
        Organization organization = new OrganizationBuilder()
                .setIsParentOrganization(true)
                .setCountry(country)
                .setName(orgDetails.getName())
                .setCompanyType(orgDetails.getCompanyType())
                .setKairosId(kairosId)
                .setVatId(orgDetails.getVatId())
                .setTimeZone(ZoneId.of(TIMEZONE_UTC))
                .setShortCompanyName(orgDetails.getShortCompanyName())
                .setDesiredUrl(orgDetails.getDesiredUrl())
                .setDescription(orgDetails.getDescription())
                .createOrganization();


        if (CompanyType.COMPANY.equals(orgDetails.getCompanyType())) {
            AccountType accountType = accountTypeGraphRepository.findOne(orgDetails.getAccountTypeId(), 0);
            if (!Optional.ofNullable(accountType).isPresent()) {
                exceptionService.dataNotFoundByIdException("message.accountType.notFound");
            }
            organization.setAccountType(accountType);
        }
        organization.setCompanyCategory(getCompanyCategory(orgDetails.getCompanyCategoryId()));
        organization.setBusinessTypes(getBusinessTypes(orgDetails.getBusinessTypeIds()));
        organizationGraphRepository.save(organization);

        orgDetails.setId(organization.getId());
        return orgDetails;
    }

    public OrganizationBasicDTO updateParentOrganization(OrganizationBasicDTO orgDetails, long organizationId) {
        Organization organization = organizationGraphRepository.findOne(organizationId, 1);
        if (!Optional.ofNullable(organization).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.organization.id.notFound", organizationId);

        }
        updateOrganizationDetails(organization, orgDetails);
        organizationGraphRepository.save(organization);
        orgDetails.setId(organization.getId());
        return orgDetails;
    }

    private void updateOrganizationDetails(Organization organization, OrganizationBasicDTO orgDetails) {
        if (orgDetails.getDesiredUrl() != null && !orgDetails.getDesiredUrl().trim().equalsIgnoreCase(organization.getDesiredUrl())) {
            Boolean orgExistWithUrl = organizationGraphRepository.checkOrgExistWithUrl(orgDetails.getDesiredUrl());
            if (orgExistWithUrl) {
                exceptionService.dataNotFoundByIdException("error.Organization.desiredUrl.duplicate", orgDetails.getDesiredUrl());
            }
        }
        if (!orgDetails.getName().equalsIgnoreCase(organization.getName())) {
            Boolean orgExistWithName = organizationGraphRepository.checkOrgExistWithName(orgDetails.getName());
            if (orgExistWithName) {
                exceptionService.dataNotFoundByIdException("error.Organization.name.duplicate", orgDetails.getName());
            }
        }
        organization.setName(orgDetails.getName());
        organization.setCompanyType(orgDetails.getCompanyType());
        organization.setVatId(orgDetails.getVatId());
        organization.setShortCompanyName(orgDetails.getShortCompanyName());
        organization.setDesiredUrl(orgDetails.getDesiredUrl());
        organization.setDescription(orgDetails.getDescription());

        if (CompanyType.COMPANY.equals(orgDetails.getCompanyType())) {
            if (!Optional.ofNullable(orgDetails.getAccountTypeId()).isPresent()) {
                exceptionService.dataNotFoundByIdException("message.accountType.select");
            }
            AccountType accountType = accountTypeGraphRepository.findOne(orgDetails.getAccountTypeId(), 0);
            if (!Optional.ofNullable(accountType).isPresent()) {
                exceptionService.dataNotFoundByIdException("message.accountType.notFound");
            }
            organization.setAccountType(accountType);
        }
        organization.setCompanyCategory(getCompanyCategory(orgDetails.getCompanyCategoryId()));
        organization.setBusinessTypes(getBusinessTypes(orgDetails.getBusinessTypeIds()));
    }

    private String validateNameAndDesiredUrlOfOrganization(OrganizationBasicDTO orgDetails) {
        CompanyValidationQueryResult orgExistWithUrl = organizationGraphRepository.checkOrgExistWithUrlOrName("(?i)" + orgDetails.getDesiredUrl(), "(?i)" + orgDetails.getName(), orgDetails.getName().substring(0, 3));
        if (orgExistWithUrl.getName()) {
            exceptionService.invalidRequestException("error.Organization.name.duplicate", orgDetails.getName());
        }
        if (orgDetails.getDesiredUrl() != null && orgExistWithUrl.getDesiredUrl()) {
            exceptionService.invalidRequestException("error.Organization.desiredUrl.duplicate", orgDetails.getDesiredUrl());
        }
        String kairosId;
        if (orgExistWithUrl.getKairosId() == null) {
            kairosId = StringUtils.upperCase(orgDetails.getName().substring(0, 3)) + HYPHEN + KAI + ONE;
        } else {
            int lastSuffix = new Integer(orgExistWithUrl.getKairosId().substring(4, orgExistWithUrl.getKairosId().length()));
            kairosId = StringUtils.upperCase(orgDetails.getName().substring(0, 3)) + HYPHEN + KAI + (++lastSuffix);
        }

        return kairosId;
    }

    // tab 1 in FE
    public OrganizationBasicResponse getOrganizationDetailsById(Long unitId) {
        return organizationGraphRepository.getOrganizationDetailsById(unitId);

    }

    public AddressDTO setAddressInCompany(Long unitId, AddressDTO addressDTO) {
        ContactAddress contactAddress;
        if (addressDTO.getId() != null) {
            contactAddress = contactAddressGraphRepository.findOne(addressDTO.getId());
            prepareAddress(contactAddress, addressDTO);
            contactAddressGraphRepository.save(contactAddress);
        } else {
            Organization organization = organizationGraphRepository.findOne(unitId);
            if (!Optional.ofNullable(organization).isPresent()) {
                exceptionService.dataNotFoundByIdException("message.organization.id.notFound", unitId);
            }
            contactAddress = new ContactAddress();
            prepareAddress(contactAddress, addressDTO);
            organization.setContactAddress(contactAddress);
            organizationGraphRepository.save(organization);
            addressDTO.setId(contactAddress.getId());
        }
        return addressDTO;
    }

    public HashMap<String, Object> getAddressOfCompany(Long unitId) {
        HashMap<String, Object> orgBasicData = new HashMap<>();
        Map<String, Object> organizationContactAddress = organizationGraphRepository.getContactAddressOfParentOrganization(unitId);
        orgBasicData.put("address", organizationContactAddress);
        orgBasicData.put("municipalities", (organizationContactAddress.get("zipCodeId") == null) ? null : FormatUtil.formatNeoResponse(regionGraphRepository.getGeographicTreeData((long) organizationContactAddress.get("zipCodeId"))));
        return orgBasicData;
    }

    public UnitManagerDTO setUserInfoInOrganization(Long unitId, Organization organization, UnitManagerDTO unitManagerDTO) {
        if (organization == null)
            organization = organizationGraphRepository.findOne(unitId);
        if (!Optional.ofNullable(organization).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.organization.id.notFound", unitId);
        }
        User user = userGraphRepository.findUserByCprNumber(unitManagerDTO.getCprNumber());
        if (user != null) {
            user.setFirstName(unitManagerDTO.getFirstName());
            user.setLastName(unitManagerDTO.getLastName());
            userGraphRepository.save(user);
        } else {
            StaffCreationDTO unitManagerData = new StaffCreationDTO(unitManagerDTO.getFirstName(), unitManagerDTO.getLastName(),
                    unitManagerDTO.getCprNumber(),
                    null, unitManagerDTO.getEmail(), null, unitManagerDTO.getEmail(), null, unitManagerDTO.getAccessGroupId());
            staffService.createUnitManagerForNewOrganization(organization, unitManagerData);
        }
        return unitManagerDTO;
    }

    public StaffPersonalDetailDTO getUnitManagerOfOrganization(Long unitId) {
        return userGraphRepository.getUnitManagerOfOrganization(unitId);
    }

    public OrganizationBasicDTO setOrganizationTypeAndSubTypeInOrganization(OrganizationBasicDTO organizationBasicDTO, Long unitId) {
        Organization organization = organizationGraphRepository.findOne(unitId);
        if (!Optional.ofNullable(organization).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.organization.id.notFound", unitId);
        }
        setOrganizationTypeAndSubTypeInOrganization(organization, organizationBasicDTO);
        organizationGraphRepository.save(organization);
        return organizationBasicDTO;
    }

    private void setOrganizationTypeAndSubTypeInOrganization(Organization organization, OrganizationBasicDTO organizationBasicDTO) {
        Optional<OrganizationType> organizationType = organizationTypeGraphRepository.findById(organizationBasicDTO.getTypeId());
        List<OrganizationType> organizationSubTypes = organizationTypeGraphRepository.findByIdIn(organizationBasicDTO.getSubTypeId());
        if (organizationBasicDTO.getLevelId() != null) {
            Level level = levelGraphRepository.findOne(organizationBasicDTO.getLevelId(), 0);
            organization.setLevel(level);
        }
        organization.setOrganizationType(organizationType.get());
        organization.setOrganizationSubTypes(organizationSubTypes);

    }

    public OrganizationTypeAndSubType getOrganizationTypeAndSubTypeByUnitId(Long unitId) {
        return organizationTypeGraphRepository.getOrganizationTypesForUnit(unitId);
    }


    public OrganizationBasicDTO addNewUnit(OrganizationBasicDTO organizationBasicDTO, Long parentOrganizationId) {

        Organization parentOrganization = organizationGraphRepository.findOne(parentOrganizationId);
        if (!Optional.ofNullable(parentOrganization).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.organization.id.notFound", parentOrganizationId);
        }
        String kairosId = validateNameAndDesiredUrlOfOrganization(organizationBasicDTO);
        Organization unit = new OrganizationBuilder()
                .setName(WordUtils.capitalize(organizationBasicDTO.getName()))
                .setDescription(organizationBasicDTO.getDescription())
                .setDesiredUrl(organizationBasicDTO.getDesiredUrl())
                .setShortCompanyName(organizationBasicDTO.getShortCompanyName())
                .setCompanyType(organizationBasicDTO.getCompanyType())
                .setVatId(organizationBasicDTO.getVatId())
                .setTimeZone(ZoneId.of(TIMEZONE_UTC))
                .setKairosId(kairosId)
                .createOrganization();

        setOrganizationTypeAndSubTypeInOrganization(unit, organizationBasicDTO);
        ContactAddress contactAddress = new ContactAddress();
        prepareAddress(contactAddress, organizationBasicDTO.getAddress());
        unit.setContactAddress(contactAddress);
        setUserInfoInOrganization(null, unit, organizationBasicDTO.getUnitManager());
        //Assign Parent Organization's level to unit
        unit.setLevel(parentOrganization.getLevel());
        organizationGraphRepository.save(unit);
        organizationBasicDTO.setId(unit.getId());
        if (organizationBasicDTO.getAddress() != null) {
            organizationBasicDTO.getAddress().setId(unit.getContactAddress().getId());
        }
        organizationGraphRepository.createChildOrganization(parentOrganizationId, unit.getId());
        return organizationBasicDTO;

    }

    public OrganizationBasicDTO updateUnit(OrganizationBasicDTO organizationBasicDTO, Long unitId) {
        Organization organization = organizationGraphRepository.findOne(unitId);
        if (!Optional.ofNullable(organization).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.organization.id.notFound", unitId);
        }
        updateOrganizationDetails(organization, organizationBasicDTO);
        setAddressInCompany(unitId, organizationBasicDTO.getAddress());
        setOrganizationTypeAndSubTypeInOrganization(organization, organizationBasicDTO);
        setUserInfoInOrganization(unitId, organization, organizationBasicDTO.getUnitManager());
        return organizationBasicDTO;

    }

    private void prepareAddress(ContactAddress contactAddress, AddressDTO addressDTO) {
        if (addressDTO.getZipCodeId() != null) {
            ZipCode zipCode = zipCodeGraphRepository.findOne(addressDTO.getZipCodeId(), 0);
            if (zipCode == null) {
                exceptionService.dataNotFoundByIdException("message.zipcode.notFound");
            }
            contactAddress.setCity(zipCode.getName());
            contactAddress.setZipCode(zipCode);
            contactAddress.setCity(zipCode.getName());
        }
        if (addressDTO.getMunicipalityId() != null) {
            Municipality municipality = municipalityGraphRepository.findOne(addressDTO.getMunicipalityId(), 0);
            if (municipality == null) {
                exceptionService.dataNotFoundByIdException("message.municipality.notFound");
            }
            contactAddress.setMunicipality(municipality);
            Map<String, Object> geographyData = regionGraphRepository.getGeographicData(municipality.getId());
            if (geographyData == null) {
                exceptionService.dataNotFoundByIdException("message.geographyData.notFound", municipality.getId());
            }
            contactAddress.setProvince(String.valueOf(geographyData.get("provinceName")));
            contactAddress.setCountry(String.valueOf(geographyData.get("countryName")));
            contactAddress.setRegionName(String.valueOf(geographyData.get("regionName")));

        }
        contactAddress.setCity(addressDTO.getCity());
        contactAddress.setFloorNumber(addressDTO.getFloorNumber());
        contactAddress.setHouseNumber(addressDTO.getHouseNumber());
        contactAddress.setStreet(addressDTO.getStreet());
        contactAddress.setVerifiedByVisitour(false);

    }

    private List<BusinessType> getBusinessTypes(List<Long> businessTypeIds) {
        List<BusinessType> businessTypes = new ArrayList<>();
        if (!businessTypeIds.isEmpty()) {
            businessTypes = businessTypeGraphRepository.findByIdIn(businessTypeIds);
        }
        return businessTypes;
    }

    private CompanyCategory getCompanyCategory(Long companyCategoryId) {
        CompanyCategory companyCategory = null;
        if (companyCategoryId != null) {
            companyCategory = companyCategoryGraphRepository.findOne(companyCategoryId, 0);
            if (!Optional.ofNullable(companyCategory).isPresent()) {
                exceptionService.dataNotFoundByIdException("message.companyCategory.id.notFound", companyCategoryId);

            }
        }
        return companyCategory;
    }

    public boolean publishOrganization(Long countryId, Long organizationId) throws InterruptedException, ExecutionException {
        Organization organization = organizationGraphRepository.findOne(organizationId, 2);
        if (!Optional.ofNullable(organization).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.organization.id.notFound", organizationId);
        }
        // If it has any error then it will throw exception
        // Here a list is created and organization with all its childrens are sent to function to validate weather any of organization
        //or parent has any missing required details

        List<Organization> organizations = new ArrayList<>();
        organizations.addAll(organization.getChildren());
        organizations.add(organization);
        validateBasicDetails(organizations, exceptionService);

        List<Long> unitIds = organization.getChildren().stream().map(Organization::getId).collect(Collectors.toList());
        unitIds.add(organizationId);

        List<StaffPersonalDetailDTO> staffPersonalDetailDTOS = userGraphRepository.getUnitManagerOfOrganization(unitIds);
        validateUserDetails(staffPersonalDetailDTOS, exceptionService);

        List<OrganizationContactAddress> organizationContactAddresses = organizationGraphRepository.getContactAddressOfOrganizations(unitIds);
        validateAddressDetails(organizationContactAddresses, exceptionService);

        organization.getChildren().forEach(currentOrg -> currentOrg.setBoardingCompleted(true));
        organization.setBoardingCompleted(true);
        organizationGraphRepository.save(organization);

        // if more than 2 default things needed make a  async service Please

        Map<Long, Long> countryAndOrgAccessGroupIdsMap = accessGroupService.createDefaultAccessGroups(organization);
        List<TimeSlot> timeSlots = timeSlotGraphRepository.findBySystemGeneratedTimeSlotsIsTrue();

        List<Long> orgSubTypeIds = organization.getOrganizationSubTypes().stream().map(orgSubType -> orgSubType.getId()).collect(Collectors.toList());

        CompletableFuture<Boolean> hasUpdated = companyDefaultDataService
                .createDefaultDataForParentOrganization(organization,countryAndOrgAccessGroupIdsMap,timeSlots,organization.getOrganizationType().getId(),orgSubTypeIds);
        CompletableFuture.allOf(hasUpdated).join();

        CompletableFuture<Boolean> createdInUnit = companyDefaultDataService
                .createDefaultDataInUnit(organization.getId(), organization.getChildren(), countryId,timeSlots);
        CompletableFuture.allOf(createdInUnit).join();

        return true;
    }

}
