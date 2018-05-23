package com.kairos.service.organization;

import com.kairos.activity.enums.IntegrationOperation;
import com.kairos.activity.util.ObjectMapperUtils;
import com.kairos.client.PeriodRestClient;
import com.kairos.client.PhaseRestClient;
import com.kairos.client.WorkingTimeAgreementRestClient;
import com.kairos.client.dto.OrganizationSkillAndOrganizationTypesDTO;
import com.kairos.client.dto.organization.CompanyType;
import com.kairos.client.dto.organization.CompanyUnitType;
import com.kairos.custom_exception.ActionNotPermittedException;
import com.kairos.custom_exception.DataNotFoundByIdException;
import com.kairos.custom_exception.DataNotMatchedException;
import com.kairos.persistence.model.enums.TimeSlotType;
import com.kairos.persistence.model.organization.*;
import com.kairos.persistence.model.organization.enums.OrganizationLevel;
import com.kairos.persistence.model.organization.group.Group;
import com.kairos.persistence.model.organization.team.Team;
import com.kairos.persistence.model.query_wrapper.OrganizationCreationData;
import com.kairos.persistence.model.query_wrapper.OrganizationStaffWrapper;
import com.kairos.persistence.model.query_wrapper.StaffUnitPositionWrapper;
import com.kairos.persistence.model.user.client.ContactAddress;
import com.kairos.persistence.model.user.client.ContactAddressDTO;
import com.kairos.persistence.model.user.country.*;
import com.kairos.persistence.model.user.country.DayType;
import com.kairos.persistence.model.user.country.dto.OrganizationMappingDTO;
import com.kairos.persistence.model.user.expertise.Expertise;
import com.kairos.persistence.model.user.region.Municipality;
import com.kairos.persistence.model.user.region.ZipCode;
import com.kairos.persistence.model.user.resources.VehicleQueryResult;
import com.kairos.persistence.model.user.staff.Staff;
import com.kairos.persistence.model.user.unit_position.UnitPositionEmploymentTypeRelationShip;
import com.kairos.persistence.repository.organization.*;
import com.kairos.persistence.repository.user.access_permission.AccessGroupRepository;
import com.kairos.persistence.repository.user.access_permission.AccessPageRepository;
import com.kairos.persistence.repository.user.agreement.cta.CollectiveTimeAgreementGraphRepository;
import com.kairos.persistence.repository.user.agreement.wta.WorkingTimeAgreementGraphRepository;
import com.kairos.persistence.repository.user.auth.UserGraphRepository;
import com.kairos.persistence.repository.user.client.ClientGraphRepository;
import com.kairos.persistence.repository.user.client.ContactAddressGraphRepository;
import com.kairos.persistence.repository.user.country.*;
import com.kairos.persistence.repository.user.expertise.ExpertiseGraphRepository;
import com.kairos.persistence.repository.user.payment_type.PaymentTypeGraphRepository;
import com.kairos.persistence.repository.user.region.MunicipalityGraphRepository;
import com.kairos.persistence.repository.user.region.RegionGraphRepository;
import com.kairos.persistence.repository.user.region.ZipCodeGraphRepository;
import com.kairos.persistence.repository.user.staff.StaffGraphRepository;
import com.kairos.persistence.repository.user.unit_position.UnitPositionGraphRepository;
import com.kairos.response.dto.web.CountryDTO;
import com.kairos.response.dto.web.OrganizationExternalIdsDTO;
import com.kairos.response.dto.web.OrganizationTypeDTO;
import com.kairos.response.dto.web.TimeSlotsDeductionDTO;
import com.kairos.response.dto.web.experties.ExpertiseResponseDTO;
import com.kairos.response.dto.web.wta.WTABasicDetailsDTO;
import com.kairos.service.UserBaseService;
import com.kairos.service.access_permisson.AccessGroupService;
import com.kairos.service.access_permisson.AccessPageService;
import com.kairos.service.agreement.wta.WTAService;
import com.kairos.service.client.AddressVerificationService;
import com.kairos.service.client.ClientOrganizationRelationService;
import com.kairos.service.client.ClientService;
import com.kairos.service.country.CitizenStatusService;
import com.kairos.service.country.CurrencyService;
import com.kairos.service.country.DayTypeService;
import com.kairos.service.integration.PriorityGroupIntegrationService;
import com.kairos.service.integration.PlannerSyncService;
import com.kairos.service.payment_type.PaymentTypeService;
import com.kairos.service.region.RegionService;
import com.kairos.service.skill.SkillService;
import com.kairos.service.staff.StaffService;
import com.kairos.util.DateConverter;
import com.kairos.util.DateUtil;
import com.kairos.util.FormatUtil;
import com.kairos.util.timeCareShift.GetAllWorkPlacesResponse;
import com.kairos.util.timeCareShift.GetAllWorkPlacesResult;
import com.kairos.util.timeCareShift.GetWorkShiftsFromWorkPlaceByIdResult;
import com.kairos.util.userContext.UserContext;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.constants.AppConstants.*;


/**
 * Calls OrganizationGraphRepository to perform CRUD operation on  Organization.
 */
@Transactional
@Service
public class OrganizationService extends UserBaseService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private AccessGroupService accessGroupService;
    @Inject
    private AccessPageRepository accessPageRepository;
    @Inject
    private CountryGraphRepository countryGraphRepository;

    @Inject
    private OrganizationTypeGraphRepository typeGraphRepository;
    @Inject
    private OrganizationTypeGraphRepository organizationTypeGraphRepository;
    @Inject
    private ClientOrganizationRelationService relationService;
    @Inject
    private UserGraphRepository userGraphRepository;
    @Inject
    private AccessGroupRepository accessGroupRepository;
    @Inject
    private MunicipalityGraphRepository municipalityGraphRepository;
    @Inject
    private BusinessTypeGraphRepository businessTypeGraphRepository;
    @Inject
    private IndustryTypeGraphRepository industryTypeGraphRepository;
    @Inject
    private OwnershipTypeGraphRepository ownershipTypeGraphRepository;
    @Inject
    private ContractTypeGraphRepository contractTypeGraphRepository;
    @Inject
    private EmployeeLimitGraphRepository employeeLimitGraphRepository;
    @Inject
    private VatTypeGraphRepository vatTypeGraphRepository;
    @Inject
    private ZipCodeGraphRepository zipCodeGraphRepository;
    @Inject
    private RegionService regionService;
    @Inject
    private PaymentTypeService paymentTypeService;
    @Inject
    private CurrencyService currencyService;
    @Inject
    private PaymentTypeGraphRepository paymentTypeGraphRepository;
    @Inject
    private CurrencyGraphRepository currencyGraphRepository;
    @Inject
    private KairosStatusGraphRepository kairosStatusGraphRepository;
    @Inject
    private TimeSlotService timeSlotService;
    @Inject
    private AddressVerificationService addressVerificationService;
    @Inject
    private ContactAddressGraphRepository addressGraphRepository;
    @Inject
    private ClientGraphRepository clientGraphRepository;
    @Inject
    private GroupGraphRepository groupGraphRepository;
    @Inject
    private TeamGraphRepository teamGraphRepository;
    @Inject
    private ContactAddressGraphRepository contactAddressGraphRepository;

    @Inject
    private OpenningHourService openningHourService;
    @Inject
    ExpertiseGraphRepository expertiseGraphRepository;
    @Inject
    RegionGraphRepository regionGraphRepository;
    @Inject
    OrganizationGraphRepository organizationGraphRepository;
    @Inject
    StaffGraphRepository staffGraphRepository;
    @Inject
    private GroupService groupService;
    @Autowired
    TeamService teamService;
    @Autowired
    OrganizationMetadataRepository organizationMetadataRepository;
    @Autowired
    private PhaseRestClient phaseRestClient;
    @Autowired
    ClientService clientService;
    @Autowired
    OrganizationServiceRepository organizationServiceRepository;

    @Autowired
    CitizenStatusService citizenStatusService;

    @Inject
    AbsenceTypesRepository absenceTypesRepository;
    @Inject
    private SkillService skillService;
    @Autowired
    DayTypeService dayTypeService;
    @Inject
    private AccessPageService accessPageService;

    @Inject
    private WTAService wtaService;
    @Inject
    private EmploymentTypeGraphRepository employmentTypeGraphRepository;
    @Inject
    CollectiveTimeAgreementGraphRepository collectiveTimeAgreementGraphRepository;
    @Inject
    WorkingTimeAgreementGraphRepository workingTimeAgreementGraphRepository;
    @Inject
    PeriodRestClient periodRestClient;
    @Inject private WorkingTimeAgreementRestClient workingTimeAgreementRestClient;
    @Inject
    StaffService staffService;
    @Inject

    PriorityGroupIntegrationService priorityGroupIntegrationService;
    @Autowired
    private PlannerSyncService plannerSyncService;
    @Inject
    CompanyCategoryGraphRepository companyCategoryGraphRepository;
    @Inject
    private UnitPositionGraphRepository unitPositionGraphRepository;


    public Organization getOrganizationById(long id) {
        return organizationGraphRepository.findOne(id);
    }

    public com.kairos.activity.client.dto.organization.OrganizationDTO getOrganizationWithCountryId(long id) {
        Organization organization = organizationGraphRepository.findOne(id);
        Country country = organizationGraphRepository.getCountry(organization.getId());
        com.kairos.activity.client.dto.organization.OrganizationDTO organizationDTO = ObjectMapperUtils.copyPropertiesByMapper(organization, com.kairos.activity.client.dto.organization.OrganizationDTO.class);
        organizationDTO.setCountryId(country.getId());
        return organizationDTO;
    }

    public boolean showCountryTagForOrganization(long id) {
        Organization organization = organizationGraphRepository.findOne(id);
        if (organization.isShowCountryTags()) {
            return true;
        } else {
            return false;
        }

    }

    public Long getCountryIdOfOrganization(long orgId) {
        Organization organization = fetchParentOrganization(orgId);
        Country country = organizationGraphRepository.getCountry(organization.getId());
        return country != null ? country.getId() : null;
    }

    /**
     * Calls OrganizationGraphRepository ,creates a new Organization
     * and return newly created Organization.
     *
     * @param organization
     * @return Organization
     */
    public Organization createOrganization(Organization organization, Long id) {

        Organization parent = (id == null) ? null : getOrganizationById(id);
        logger.info("Received Parent ID: " + id);
        if (parent != null) {
            organizationGraphRepository.save(organization);
            Organization o = organizationGraphRepository.createChildOrganization(parent.getId(), organization.getId());
            logger.info("Parent Organization: " + o.getName());
        } else {
            organization = save(organization);
            int count = organizationGraphRepository.linkWithRegionLevelOrganization(organization.getId());
            logger.info("Linked with region level " + count);
        }
        accessGroupService.createDefaultAccessGroups(organization);
        timeSlotService.createDefaultTimeSlots(organization, TimeSlotType.SHIFT_PLANNING);
        timeSlotService.createDefaultTimeSlots(organization, TimeSlotType.TASK_PLANNING);
        organizationGraphRepository.assignDefaultSkillsToOrg(organization.getId(), DateUtil.getCurrentDate().getTime(), DateUtil.getCurrentDate().getTime());
        return organization;
    }

    public Map<String,OrganizationResponseWrapper> createParentOrganization(OrganizationRequestWrapper organizationRequestWrapper, long countryId, Long organizationId) {

        Map<String,OrganizationResponseWrapper> organizationResponseMap = new HashMap<>();

        OrganizationDTO orgDetails = organizationRequestWrapper.getCompany();

        Country country = countryGraphRepository.findOne(countryId);
        if (country == null) {
            throw new InternalError("Country not found");
        }
        Organization organization = new Organization();
        organization.setParentOrganization(true);
        organization.setCountry(country);
        organization.setBoardingCompleted(orgDetails.isBoardingCompleted());
        organization = saveOrganizationDetails(organization, orgDetails, false, countryId);



        OrganizationSetting organizationSetting = openningHourService.getDefaultSettings();
        organization.setOrganizationSetting(organizationSetting);
        // @ modified by vipul for KSP-107
        /**
         * @Modified vipul
         * when creating an organization linking all existing wta with this subtype to organization
         */
        //List<WorkingTimeAgreement> allWtaCopy = new ArrayList<>();
        //List<WTAAndExpertiseQueryResult> allWtaExpertiseQueryResults = organizationTypeGraphRepository.getAllWTAByOrganiationSubType(orgDetails.getSubTypeId());
        //List<WorkingTimeAgreement> allWta = getWTAWithExpertise(allWtaExpertiseQueryResults);
        //linkWTAToOrganization(allWtaCopy, allWta);
        organization.setTimeZone(ZoneId.of(TIMEZONE_UTC));

        organization.setCostTimeAgreements(collectiveTimeAgreementGraphRepository.getCTAsByOrganiationSubTypeIdsIn(orgDetails.getSubTypeId(), countryId));
        save(organization);
        workingTimeAgreementRestClient.assignWTAToOrganization(orgDetails.getSubTypeId(),organization.getId(),countryId);

        organizationGraphRepository.linkWithRegionLevelOrganization(organization.getId());
        accessGroupService.createDefaultAccessGroups(organization);
        timeSlotService.createDefaultTimeSlots(organization,TimeSlotType.SHIFT_PLANNING);
        timeSlotService.createDefaultTimeSlots(organization,TimeSlotType.TASK_PLANNING);
        long creationDate = DateUtil.getCurrentDate().getTime();
        organizationGraphRepository.assignDefaultSkillsToOrg(organization.getId(), creationDate, creationDate);
        creationDate = DateUtil.getCurrentDate().getTime();
        organizationGraphRepository.assignDefaultServicesToOrg(organization.getId(), creationDate, creationDate);
        // DO NOT CREATE PHASE for UNION
        if (!orgDetails.getUnion()) {
            phaseRestClient.createDefaultPhases(organization.getId());
            periodRestClient.createDefaultPeriodSettings(organization.getId());
        }

        priorityGroupIntegrationService.createDefaultPriorityGroupsFromCountry(organization.getCountry().getId(),organization.getId());


        /*// TODO Verify code to set Unit Manager of new organization
        // Create Employment for Unit Manager
        // Check if user exists or Create User
        StaffCreationPOJOData staffCreationPOJOData = new StaffCreationPOJOData("Andreas","L. Jacobsen", "0108572361",
                "Sam", "andreas@gmail.com", Gender.MALE, "andreas@gmail.com",null, 0L );
        User user = staffService.createUnitManagerForNewOrganization(organization.getId(), staffCreationPOJOData);*/

        OrganizationResponseWrapper organizationResponseWrapper = new OrganizationResponseWrapper();
        organizationResponseWrapper.setOrgData(organizationResponse(organization, orgDetails.getTypeId(), orgDetails.getSubTypeId(), orgDetails.getCompanyCategoryId()));
        organizationResponseWrapper.setPermissions(accessPageService.getPermissionOfUserInUnit(organizationId, organization, UserContext.getUserDetails().getId()));

        organizationResponseMap.put("company",organizationResponseWrapper);

        if(organizationRequestWrapper.getWorkCenterUnit()!=null){
            Map<String, Object>  workCenterUnitMap = createNewUnit(organizationRequestWrapper.getWorkCenterUnit(), organization.getId(),true, false);
            Long workCenterUnitId = Long.parseLong(workCenterUnitMap.get("id")+"");

            Organization workCenterUnit = organizationGraphRepository.findOne(workCenterUnitId);
            workCenterUnit.setWorkCenterUnit(true);
            organizationGraphRepository.save(workCenterUnit);

            organizationResponseWrapper = new OrganizationResponseWrapper();

            organizationResponseWrapper.setOrgData(organizationResponse(workCenterUnit, organizationRequestWrapper.getWorkCenterUnit().getTypeId(), organizationRequestWrapper.getWorkCenterUnit().getSubTypeId(), organizationRequestWrapper.getWorkCenterUnit().getCompanyCategoryId()));
            organizationResponseWrapper.setPermissions(accessPageService.getPermissionOfUserInUnit(organizationId, workCenterUnit, UserContext.getUserDetails().getId()));
            organizationResponseMap.put("workCenterUnit",organizationResponseWrapper);
        }

        if(organizationRequestWrapper.getGdprUnit()!=null){
            Map<String, Object>  gdprUnitMap = createNewUnit(organizationRequestWrapper.getGdprUnit(), organization.getId(),false,true);
            Long gdprUnitId = Long.parseLong(gdprUnitMap.get("id")+"");

            Organization gdprUnit = organizationGraphRepository.findOne(gdprUnitId);
            gdprUnit.setGdprUnit(true);
            organizationGraphRepository.save(gdprUnit);

            organizationResponseWrapper = new OrganizationResponseWrapper();
            organizationResponseWrapper.setOrgData(organizationResponse(gdprUnit, organizationRequestWrapper.getGdprUnit().getTypeId(), organizationRequestWrapper.getGdprUnit().getSubTypeId(), organizationRequestWrapper.getGdprUnit().getCompanyCategoryId()));
            organizationResponseWrapper.setPermissions(accessPageService.getPermissionOfUserInUnit(organizationId, gdprUnit, UserContext.getUserDetails().getId()));
            organizationResponseMap.put("gdprUnit",organizationResponseWrapper);

        }


        return organizationResponseMap;
    }

    public OrganizationResponseWrapper createUnion(OrganizationDTO orgDetails, long countryId, Long organizationId) {

        Country country = countryGraphRepository.findOne(countryId);
        if (country == null) {
            throw new InternalError("Country not found");
        }
        Organization organization = new Organization();
        organization.setParentOrganization(true);
        organization.setCountry(country);
        organization.setBoardingCompleted(orgDetails.isBoardingCompleted());
        organization = saveOrganizationDetails(organization, orgDetails, false, countryId);

        OrganizationSetting organizationSetting = openningHourService.getDefaultSettings();
        organization.setOrganizationSetting(organizationSetting);


        organization.setCostTimeAgreements(collectiveTimeAgreementGraphRepository.getCTAsByOrganiationSubTypeIdsIn(orgDetails.getSubTypeId(), countryId));
        save(organization);
        workingTimeAgreementRestClient.assignWTAToOrganization(orgDetails.getSubTypeId(),organization.getId(),countryId);

        organizationGraphRepository.linkWithRegionLevelOrganization(organization.getId());
        accessGroupService.createDefaultAccessGroups(organization);
        timeSlotService.createDefaultTimeSlots(organization,TimeSlotType.SHIFT_PLANNING);
        timeSlotService.createDefaultTimeSlots(organization,TimeSlotType.TASK_PLANNING);
        long creationDate = DateUtil.getCurrentDate().getTime();
        organizationGraphRepository.assignDefaultSkillsToOrg(organization.getId(), creationDate, creationDate);
        creationDate = DateUtil.getCurrentDate().getTime();
        organizationGraphRepository.assignDefaultServicesToOrg(organization.getId(), creationDate, creationDate);
        // DO NOT CREATE PHASE for UNION
        if (!orgDetails.getUnion()) {
            phaseRestClient.createDefaultPhases(organization.getId());
            periodRestClient.createDefaultPeriodSettings(organization.getId());
        }
        OrganizationResponseWrapper organizationResponseWrapper = new OrganizationResponseWrapper();
        organizationResponseWrapper.setOrgData(organizationResponse(organization, orgDetails.getTypeId(), orgDetails.getSubTypeId(), orgDetails.getCompanyCategoryId()));
        organizationResponseWrapper.setPermissions(accessPageService.getPermissionOfUserInUnit(organizationId, organization, UserContext.getUserDetails().getId()));

        return organizationResponseWrapper;
    }

    /*List<WorkingTimeAgreement> getWTAWithExpertise(List<WTAAndExpertiseQueryResult> allWtaExpertiseQueryResults){
        List<WorkingTimeAgreement> workingTimeAgreements = new ArrayList<>();
        for (WTAAndExpertiseQueryResult allWtaExpertiseQueryResult : allWtaExpertiseQueryResults) {
            allWtaExpertiseQueryResult.getWorkingTimeAgreement().setExpertise(allWtaExpertiseQueryResult.getExpertise());
            workingTimeAgreements.add(allWtaExpertiseQueryResult.getWorkingTimeAgreement());
        }
        return workingTimeAgreements;
    }*/


    /*private void linkWTAToOrganization(List<WorkingTimeAgreement> WTAList, List<WorkingTimeAgreement> allWta) {
        allWta.forEach(workingTimeAgreement -> {
            WorkingTimeAgreement newWtaObject = new WorkingTimeAgreement();
            wtaService.copyWta(workingTimeAgreement, newWtaObject);
            newWtaObject.setDisabled(false);
            newWtaObject.setCountryParentWTA(workingTimeAgreement);
            List<WTABaseRuleTemplate> ruleTemplateWithCategory = wtaService.copyRuleTemplate(workingTimeAgreement.getRuleTemplates());
            newWtaObject.setRuleTemplates(ruleTemplateWithCategory);
            WTAList.add(newWtaObject);
        });

    }*/

    public Map<String, OrganizationResponseDTO> updateParentOrganization(OrganizationRequestWrapper organizationRequestWrapper, long organizationId, long countryId) {
        Map<String, OrganizationResponseDTO> organizationResponseDTOs = new HashMap<>();
        OrganizationDTO orgDetails = organizationRequestWrapper.getCompany();
        Organization organization = organizationGraphRepository.findOne(organizationId, 2);
        if (!Optional.ofNullable(organization).isPresent()) {
            throw new InternalError("Organization not found by Id " + organizationId);
        }
        organization = saveOrganizationDetails(organization, orgDetails, true, countryId);
        if (!Optional.ofNullable(organization).isPresent()) {
            return null;
        }
        save(organization);
        organizationResponseDTOs.put("company",organizationResponse(organization, orgDetails.getTypeId(), orgDetails.getSubTypeId(), orgDetails.getCompanyCategoryId()));

        if(organizationRequestWrapper.getWorkCenterUnit()!=null) {
            Organization workCenterUnit;
            Long workCenterUnitId = organizationRequestWrapper.getWorkCenterUnit().getId();
            if(workCenterUnitId ==null){
            Map<String, Object>  workCenterUnitMap = createNewUnit(organizationRequestWrapper.getWorkCenterUnit(), organizationId, true, false);
               workCenterUnitId = Long.parseLong(workCenterUnitMap.get("id")+"");;
                workCenterUnit = organizationGraphRepository.findOne(workCenterUnitId,2);
            } else{
                workCenterUnit = organizationGraphRepository.findOne(workCenterUnitId, 2);
                if (!Optional.ofNullable(workCenterUnit).isPresent()) {
                    throw new InternalError("WorkCenter Unit not found by Id " + organizationRequestWrapper.getWorkCenterUnit().getId());
                }
                workCenterUnit = saveOrganizationDetails(workCenterUnit, organizationRequestWrapper.getWorkCenterUnit(), true, countryId);
                if (!Optional.ofNullable(workCenterUnit).isPresent()) {
                    return null;
                }
                save(workCenterUnit);
            }
            organizationResponseDTOs.put("workCenterUnit",organizationResponse(workCenterUnit, organizationRequestWrapper.getWorkCenterUnit().getTypeId(), organizationRequestWrapper.getWorkCenterUnit().getSubTypeId(), organizationRequestWrapper.getWorkCenterUnit().getCompanyCategoryId()));
        }

        if(organizationRequestWrapper.getGdprUnit()!=null) {
            Long gdprUnitId = organizationRequestWrapper.getGdprUnit().getId();
            Organization gdprUnit;
            if(gdprUnitId ==null){
                Map<String, Object>  gdprUnitMap = createNewUnit(organizationRequestWrapper.getGdprUnit(), organizationId,false,true);
                gdprUnitId = Long.parseLong(gdprUnitMap.get("id")+"");;
                gdprUnit = organizationGraphRepository.findOne(gdprUnitId, 2);
            } else {
                gdprUnit = organizationGraphRepository.findOne(gdprUnitId, 2);
                if (!Optional.ofNullable(gdprUnit).isPresent()) {
                    throw new InternalError("Gdpr Unit not found by Id " + organizationRequestWrapper.getGdprUnit().getId());
                }
                gdprUnit = saveOrganizationDetails(gdprUnit, organizationRequestWrapper.getGdprUnit(), true, countryId);
                if (!Optional.ofNullable(gdprUnit).isPresent()) {
                    return null;
                }
                save(gdprUnit);
            }

            organizationResponseDTOs.put("gdprUnit",organizationResponse(gdprUnit, organizationRequestWrapper.getGdprUnit().getTypeId(), organizationRequestWrapper.getGdprUnit().getSubTypeId(), organizationRequestWrapper.getGdprUnit().getCompanyCategoryId()));
        }
        return organizationResponseDTOs;
    }


    public OrganizationResponseDTO updateUnion(OrganizationDTO orgDetails, long unionId, long countryId) {
        Organization union = organizationGraphRepository.findOne(unionId, 2);
        if (!Optional.ofNullable(union).isPresent()) {
            throw new InternalError("Union not found by Id " + unionId);
        }
        union = saveOrganizationDetails(union, orgDetails, true, countryId);
        if (!Optional.ofNullable(union).isPresent()) {
            return null;
        }
        save(union);
        return organizationResponse(union, orgDetails.getTypeId(), orgDetails.getSubTypeId(), orgDetails.getCompanyCategoryId());
    }

    private OrganizationResponseDTO organizationResponse(Organization organization, List<Long> organizationTypeId, List<Long> organizationSubTypeId, Long companyCategoryId) {

        OrganizationResponseDTO organizationResponseDTO = new OrganizationResponseDTO();
        organizationResponseDTO.setName(organization.getName());
        organizationResponseDTO.setId(organization.getId());
        organizationResponseDTO.setPrekairos(organization.isPrekairos());
        organizationResponseDTO.setKairosHub( organization.isKairosHub());
        organizationResponseDTO.setDescription(organization.getDescription());
        organizationResponseDTO.setBusinessTypeIds(organization.getBusinessTypes().stream().map(businessType-> businessType.getId()).collect(Collectors.toList()));
        organizationResponseDTO.setTypeId(organizationTypeId);
        organizationResponseDTO.setSubTypeId(organizationSubTypeId);
        organizationResponseDTO.setExternalId(organization.getExternalId());
        organizationResponseDTO.setContactAddress(filterContactAddressInfo(organization.getContactAddress()));
        organizationResponseDTO.setLevelId((organization.getLevel() == null) ? null : organization.getLevel().getId());

        organizationResponseDTO.setUnion(organization.isUnion());
        organizationResponseDTO.setDesiredUrl(organization.getDesiredUrl());
        organizationResponseDTO.setShortCompanyName(organization.getShortCompanyName());
        organizationResponseDTO.setKairosCompanyId(organization.getKairosCompanyId());
        organizationResponseDTO.setCompanyCategoryId(companyCategoryId);
        organizationResponseDTO.setCompanyType(organization.getCompanyType());
        organizationResponseDTO.setVatId(organization.getVatId());
        organizationResponseDTO.setCostCenter(organization.isCostCenter());
        organizationResponseDTO.setCostCenterId(organization.getCostCenterId());
        organizationResponseDTO.setCompanyUnitType(organization.getCompanyUnitType());
        organizationResponseDTO.setBoardingCompleted(organization.isBoardingCompleted());

        return organizationResponseDTO;
    }

    private ContactAddressDTO filterContactAddressInfo(ContactAddress contactAddress) {

        ContactAddressDTO contactAddressDTO = new ContactAddressDTO();
        contactAddressDTO.setHouseNumber(contactAddress.getHouseNumber());
        contactAddressDTO.setFloorNumber(contactAddress.getFloorNumber());
        contactAddressDTO.setCity(contactAddress.getCity());
        contactAddressDTO.setZipCodeId(contactAddress.getZipCode().getId());
        contactAddressDTO.setRegionName(contactAddress.getRegionName());
        contactAddressDTO.setProvince(contactAddress.getProvince());
        contactAddressDTO.setAddressProtected(contactAddress.isAddressProtected());
        contactAddressDTO.setStreet1(contactAddress.getStreet1());
        contactAddressDTO.setLatitude(contactAddress.getLatitude());
        contactAddressDTO.setLongitude(contactAddress.getLongitude());
        contactAddressDTO.setZipCodeValue(contactAddress.getZipCode().getZipCode());
        contactAddressDTO.setMunicipalityName(contactAddress.getMunicipality().getName());
        return contactAddressDTO;


    }

    private Organization saveOrganizationDetails(Organization organization, OrganizationDTO orgDetails, boolean isUpdateOperation, long countryId) {
        organization.setName(WordUtils.capitalize(orgDetails.getName()));
        if (!Optional.ofNullable(orgDetails.getUnion()).isPresent()) {
            throw new ActionNotPermittedException("Please specify this is union or organization");
        }
        organization.setUnion(orgDetails.getUnion());
        List<OrganizationType> organizationTypes = organizationTypeGraphRepository.findByIdIn(orgDetails.getTypeId());
        List<OrganizationType> organizationSubTypes = organizationTypeGraphRepository.findByIdIn(orgDetails.getSubTypeId());


        // BusinessType
        List<BusinessType> businessTypes = businessTypeGraphRepository.findByIdIn(orgDetails.getBusinessTypeIds());

        ContactAddress contactAddress;
        if (isUpdateOperation) {
            contactAddress = organization.getContactAddress();
        } else {
            contactAddress = new ContactAddress();
        }
        organization.setKairosHub(orgDetails.isKairosHub());
        organization.setPrekairos(orgDetails.isPreKairos());
        organization.setDescription(orgDetails.getDescription());
        organization.setExternalId(orgDetails.getExternalId());

        // Verify Address here
        AddressDTO addressDTO = orgDetails.getContactAddress();
        ZipCode zipCode;
        addressDTO.setVerifiedByGoogleMap(true);
        if (addressDTO.isVerifiedByGoogleMap()) {
            contactAddress.setLongitude(addressDTO.getLongitude());
            contactAddress.setLatitude(addressDTO.getLatitude());
            zipCode = zipCodeGraphRepository.findOne(addressDTO.getZipCodeId());
        } else {
            Map<String, Object> tomtomResponse = addressVerificationService.verifyAddress(addressDTO, organization.getId());
            if (tomtomResponse == null) {
                return null;
            }
            contactAddress.setVerifiedByVisitour(true);
            contactAddress.setCountry("Denmark");
            contactAddress.setLongitude(Float.valueOf(String.valueOf(tomtomResponse.get("xCoordinates"))));
            contactAddress.setLatitude(Float.valueOf(String.valueOf(tomtomResponse.get("yCoordinates"))));
            zipCode = zipCodeGraphRepository.findOne(addressDTO.getZipCodeId());
        }

        if (zipCode == null) {
            throw new InternalError("Zip code not found");
        }
        Municipality municipality = municipalityGraphRepository.findOne(addressDTO.getMunicipalityId());
        if (municipality == null) {
            throw new InternalError("Municpality not found");
        }


        Map<String, Object> geographyData = regionGraphRepository.getGeographicData(municipality.getId());
        if (geographyData == null) {
            throw new InternalError("Geography data not found with provided municipality");
        }

        if (Optional.ofNullable(orgDetails.getTypeId()).isPresent() && orgDetails.getTypeId().size() > 0 && Optional.ofNullable(orgDetails.getLevelId()).isPresent()) {
            Level level = organizationTypeGraphRepository.getLevel(orgDetails.getTypeId().get(0), orgDetails.getLevelId());
            organization.setLevel(level);
        }
        // Geography Data
        contactAddress.setMunicipality(municipality);
        contactAddress.setProvince(String.valueOf(geographyData.get("provinceName")));
        contactAddress.setCountry(String.valueOf(geographyData.get("countryName")));
        contactAddress.setRegionName(String.valueOf(geographyData.get("regionName")));
        contactAddress.setCountry(String.valueOf(geographyData.get("countryName")));
        contactAddress.setStreet1(addressDTO.getStreet1());
        contactAddress.setHouseNumber(addressDTO.getHouseNumber());
        contactAddress.setFloorNumber(addressDTO.getFloorNumber());
        contactAddress.setCity(zipCode.getName());
        contactAddress.setZipCode(zipCode);
        contactAddress.setCity(zipCode.getName());
        organization.setContactAddress(contactAddress);
        organization.setOrganizationTypes(organizationTypes);
        organization.setOrganizationSubTypes(organizationSubTypes);
        organization.setBusinessTypes(businessTypes);

        CompanyCategory companyCategory =  companyCategoryGraphRepository.findOne(orgDetails.getCompanyCategoryId());
        if (!Optional.ofNullable(companyCategory).isPresent()) {
            throw new DataNotFoundByIdException("Invalid Company category " + orgDetails.getCompanyCategoryId());
        }

        organization.setDesiredUrl(orgDetails.getDesiredUrl());
        organization.setShortCompanyName(orgDetails.getShortCompanyName());
        organization.setKairosCompanyId(orgDetails.getKairosCompanyId());
        organization.setCompanyCategory(companyCategory);
        organization.setCompanyType(orgDetails.getCompanyType());
        organization.setVatId(orgDetails.getVatId());

        return organization;
    }

    public Map<String, Object> createNewUnit(OrganizationDTO organizationDTO, long unitId, boolean workCenterUnit, boolean gdprUnit) {

        Organization parent = organizationGraphRepository.findOne(unitId);

        Organization unit = new Organization();
        ContactAddress contactAddress = new ContactAddress();

        if (!Optional.ofNullable(parent).isPresent()) {
            throw new DataNotFoundByIdException("Can't find Organization with provided Id" + unitId);
        }
        unit.setTimeZone(ZoneId.of(TIMEZONE_UTC));
        unit.setName(WordUtils.capitalize(organizationDTO.getName()));
        unit.setDescription(organizationDTO.getDescription());
        unit.setPrekairos(organizationDTO.isPreKairos());
        unit.setWorkCenterUnit(workCenterUnit);
        unit.setGdprUnit(gdprUnit);

        AddressDTO addressDTO = organizationDTO.getContactAddress();


        // Verify Address here
        addressDTO.setVerifiedByGoogleMap(true);
        if (addressDTO.isVerifiedByGoogleMap()) {
            ZipCode zipCode =  zipCodeGraphRepository.findOne(addressDTO.getZipCodeId());
            if (zipCode == null) {
                logger.info("ZipCode Not Found returning null");
                return null;
            }
            Municipality municipality = municipalityGraphRepository.findOne(addressDTO.getMunicipalityId());
            if (municipality == null) {
                throw new InternalError("Municpality not found");
            }


            Map<String, Object> geographyData = regionGraphRepository.getGeographicData(municipality.getId());
            if (geographyData == null) {
                throw new InternalError("Geography data not found with provided municipality");
            }

            // Geography Data
            contactAddress.setMunicipality(municipality);
            contactAddress.setProvince(String.valueOf(geographyData.get("provinceName")));
            contactAddress.setCountry(String.valueOf(geographyData.get("countryName")));
            contactAddress.setRegionName(String.valueOf(geographyData.get("regionName")));
            // Coordinates
            contactAddress.setLongitude(addressDTO.getLongitude());
            contactAddress.setLatitude(addressDTO.getLatitude());
            contactAddress.setVerifiedByVisitour(false);
            // Native Details
            contactAddress.setStreet1(addressDTO.getStreet1());
            contactAddress.setHouseNumber(addressDTO.getHouseNumber());
            contactAddress.setFloorNumber(addressDTO.getFloorNumber());
            contactAddress.setCity(zipCode.getName());
            contactAddress.setZipCode(zipCode);
            contactAddress.setCity(zipCode.getName());
            unit.setContactAddress(contactAddress);
        } else {
            // Send Address to verify
            Map<String, Object> tomtomResponse = addressVerificationService.verifyAddress(addressDTO, unitId);
            if (tomtomResponse != null) {
                // -------Parse Address from DTO -------- //
                contactAddress.setVerifiedByVisitour(true);
                contactAddress.setCountry("Denmark");
                // Coordinates
                contactAddress.setLongitude(Float.valueOf(String.valueOf(tomtomResponse.get("xCoordinates"))));
                contactAddress.setLatitude(Float.valueOf(String.valueOf(tomtomResponse.get("yCoordinates"))));

                ZipCode zipCode = zipCodeGraphRepository.findOne(addressDTO.getZipCodeId());
                if (zipCode == null) {
                    return null;
                }
                Municipality municipality = municipalityGraphRepository.findOne(addressDTO.getMunicipalityId());
                if (municipality == null) {
                    throw new InternalError("Municpality not found");
                }
                Map<String, Object> geographyData = regionGraphRepository.getGeographicData(municipality.getId());
                if (geographyData == null) {
                    throw new InternalError("Geography data not found with provided municipality");
                }

                // Geography Data
                contactAddress.setMunicipality(municipality);
                contactAddress.setProvince(String.valueOf(geographyData.get("provinceName")));
                contactAddress.setCountry(String.valueOf(geographyData.get("countryName")));
                contactAddress.setRegionName(String.valueOf(geographyData.get("regionName")));
                contactAddress.setCity(zipCode.getName());
                // Native Details
                contactAddress.setStreet1(addressDTO.getStreet1());
                contactAddress.setHouseNumber(addressDTO.getHouseNumber());
                contactAddress.setFloorNumber(addressDTO.getFloorNumber());
                contactAddress.setCity(zipCode.getName());
                contactAddress.setZipCode(zipCode);
                contactAddress.setCity(zipCode.getName());
                unit.setContactAddress(contactAddress);
            } else {
                return null;
            }


        }

        List<BusinessType> businessTypes = businessTypeGraphRepository.findByIdIn(organizationDTO.getBusinessTypeId());
        List<OrganizationType> organizationTypes = organizationTypeGraphRepository.findByIdIn(organizationDTO.getTypeId());
        List<OrganizationType> organizationSubTypes = organizationTypeGraphRepository.findByIdIn(organizationDTO.getSubTypeId());
        unit.setBusinessTypes(businessTypes);
        unit.setOrganizationTypes(organizationTypes);
        unit.setOrganizationSubTypes(organizationSubTypes);
        unit.setCompanyUnitType(organizationDTO.getCompanyUnitType());
        unit.setCompanyType(organizationDTO.getCompanyType());

        CompanyCategory companyCategory =  companyCategoryGraphRepository.findOne(organizationDTO.getCompanyCategoryId());
        if (!Optional.ofNullable(companyCategory).isPresent()) {
            throw new DataNotFoundByIdException("Invalid Company category " + organizationDTO.getCompanyCategoryId());
        }

        unit.setDesiredUrl(organizationDTO.getDesiredUrl());
        unit.setShortCompanyName(organizationDTO.getShortCompanyName());
        unit.setKairosCompanyId(organizationDTO.getKairosCompanyId());
        unit.setCompanyCategory(companyCategory);
        unit.setCompanyType(organizationDTO.getCompanyType());
        unit.setVatId(organizationDTO.getVatId());

        logger.info("Now Setting Organization Setting from Parent Organization: " + parent.getName());
        OrganizationSetting organizationSetting = openningHourService.getDefaultSettings();
        unit.setOrganizationSetting(organizationSetting);
        /**
         * @Modified by vipul
         *  Added all wta of from parent organization
         */
       /* List<WorkingTimeAgreement> allWtaCopy = new ArrayList<>();
        List<WTAAndExpertiseQueryResult> allWtaExpertiseQueryResults=workingTimeAgreementGraphRepository.getAllWTAByOrganization(unitId);
        List<WorkingTimeAgreement> allWta = getWTAWithExpertise(allWtaExpertiseQueryResults);
        linkWTAToOrganization(allWtaCopy, allWta);
        unit.setWorkingTimeAgreements(allWtaCopy);
*/
        //Assign Parent Organization's level to unit
        unit.setLevel(parent.getLevel());

        organizationGraphRepository.save(unit);
        organizationGraphRepository.createChildOrganization(parent.getId(), unit.getId());
        accessGroupService.createDefaultAccessGroups(unit);
        timeSlotService.createDefaultTimeSlots(unit,TimeSlotType.SHIFT_PLANNING);
        timeSlotService.createDefaultTimeSlots(unit,TimeSlotType.TASK_PLANNING);
        phaseRestClient.createDefaultPhases(unit.getId());
        periodRestClient.createDefaultPeriodSettings(unit.getId());
        Organization organization = fetchParentOrganization(unit.getId());
        Country country = organizationGraphRepository.getCountry(organization.getId());
        workingTimeAgreementRestClient.assignWTAToOrganization(organizationDTO.getSubTypeId(),unit.getId(),country.getId());
        priorityGroupIntegrationService.createDefaultPriorityGroupsFromCountry(country.getId(),unit.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("id", unit.getId());
        response.put("name", unit.getName());
        response.put("type", ORGANIZATION_LABEL);
        response.put("contactAddress", unit.getContactAddress());
        response.put("children", Collections.emptyList());
        response.put("permissions", accessPageService.getPermissionOfUserInUnit(parent.getId(), unit, UserContext.getUserDetails().getId()));
        return response;

    }

    public List<Map<String, Object>> getAllOrganization() {
        return organizationGraphRepository.findAllOrganizations();
    }

    public boolean deleteOrganization(long organizationId) {
        Organization organization = organizationGraphRepository.findOne(organizationId);
        if (organization != null) {
            organization.setEnable(false);
            organizationGraphRepository.save(organization);
            return true;
        }
        return false;
    }

    public List<Map<String, Object>> getUnits(long organizationId) {
        return organizationGraphRepository.getUnits(organizationId);

    }

    public Map<String, Object> getGeneralDetails(long id, String type) {


        Map<String, Object> response = new HashMap<>();

        if (ORGANIZATION.equalsIgnoreCase(type)) {
            Organization unit = organizationGraphRepository.findOne(id, 0);
            if (unit == null) {
                throw new InternalError("Organization is null");
            }
            Map<String, Object> metaData = null;
            Long countryId = countryGraphRepository.getCountryIdByUnitId(id);
            List<Map<String, Object>> data = organizationGraphRepository.getGeneralTabMetaData(countryId);
            for (Map<String, Object> map : data) {
                metaData = (Map<String, Object>) map.get("data");
            }
            Map<String, Object> cloneMap = new HashMap<>(metaData);
            OrganizationContactAddress organizationContactAddress = organizationGraphRepository.getContactAddressOfOrg(id);
            ZipCode zipCode = organizationContactAddress.getZipCode();
            List<Municipality> municipalities = (zipCode == null) ? Collections.emptyList() : municipalityGraphRepository.getMunicipalitiesByZipCode(zipCode.getId());
            Map<String, Object> generalTabQueryResult = organizationGraphRepository.getGeneralTabInfo(unit.getId());
            HashMap<String, Object> generalTabInfo = new HashMap<>(generalTabQueryResult);
            generalTabInfo.put("clientSince", (generalTabInfo.get("clientSince") == null ? null : DateConverter.getDate((long) generalTabInfo.get("clientSince"))));
            cloneMap.put("municipalities", municipalities);
            response.put("generalTabInfo", generalTabInfo);
            response.put("otherData", cloneMap);

        } else if (GROUP.equalsIgnoreCase(type)) {
            Group group = groupGraphRepository.findOne(id, 0);
            if (group == null) {
                throw new InternalError("Group is null");
            }
            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("name", group.getName());
            groupInfo.put("id", group.getId());
            groupInfo.put("description", group.getDescription());
            response.put("generalTabInfo", groupInfo);
            response.put("otherData", Collections.emptyMap());
        } else if (TEAM.equalsIgnoreCase(type)) {
            Team team = teamGraphRepository.findOne(id);
            if (team == null) {
                throw new InternalError("Team is null");
            }
            Map<String, Object> teamInfo = new HashMap<>();
            teamInfo.put("name", team.getName());
            teamInfo.put("id", team.getId());
            teamInfo.put("description", team.getDescription());
            teamInfo.put("visitourId", team.getVisitourId());
            response.put("generalTabInfo", teamInfo);
            response.put("otherData", Collections.emptyMap());
        }

        return response;
    }


    public boolean updateOrganizationGeneralDetails(OrganizationGeneral organizationGeneral, long unitId) throws ParseException {
        Organization unit = organizationGraphRepository.findOne(unitId);
        if (unit == null) {
            throw new InternalError("unit is null");
        }

        OwnershipType ownershipType = null;
        ContractType contractType = null;
        IndustryType industryType = null;
        EmployeeLimit employeeLimit = null;
        KairosStatus kairosStatus = null;
        VatType vatType = null;
        if (organizationGeneral.getOwnershipTypeId() != null)
            ownershipType = ownershipTypeGraphRepository.findOne(organizationGeneral.getOwnershipTypeId());
        List<BusinessType> businessTypes = businessTypeGraphRepository.findByIdIn(organizationGeneral.getBusinessTypeId());
        if (organizationGeneral.getContractTypeId() != null)
            contractType = contractTypeGraphRepository.findOne(organizationGeneral.getContractTypeId());
        if (organizationGeneral.getIndustryTypeId() != null)
            industryType = industryTypeGraphRepository.findOne(organizationGeneral.getIndustryTypeId());
        if (organizationGeneral.getEmployeeLimitId() != null)
            employeeLimit = employeeLimitGraphRepository.findOne(organizationGeneral.getEmployeeLimitId());
        if (organizationGeneral.getVatTypeId() != null)
            vatType = vatTypeGraphRepository.findOne(organizationGeneral.getVatTypeId());
        if (organizationGeneral.getKairosStatusId() != null)
            kairosStatus = kairosStatusGraphRepository.findOne(organizationGeneral.getKairosStatusId());
        ContactAddress contactAddress = unit.getContactAddress();
        if (contactAddress != null) {
            Municipality municipality = municipalityGraphRepository.findOne(organizationGeneral.getMunicipalityId());
            if (municipality == null) {
                throw new InternalError("Municipality is null");
            }
            contactAddress.setMunicipality(municipality);
        }
        List<OrganizationType> organizationTypes = organizationTypeGraphRepository.findByIdIn(organizationGeneral.getOrganizationTypeId());
        List<OrganizationType> organizationSubTypes = organizationTypeGraphRepository.findByIdIn(organizationGeneral.getOrganizationSubTypeId());
        unit.setContactAddress(contactAddress);
        unit.setName(organizationGeneral.getName());
        unit.setShortName(organizationGeneral.getShortName());
        unit.setCvrNumber(organizationGeneral.getCvrNumber());
        unit.setpNumber(organizationGeneral.getpNumber());
        unit.setWebSiteUrl(organizationGeneral.getWebsiteUrl());
        unit.setEmployeeLimit(employeeLimit);
        unit.setDescription(organizationGeneral.getDescription());
        unit.setOrganizationTypes(organizationTypes);
        unit.setOrganizationSubTypes(organizationSubTypes);
        unit.setVatType(vatType);
        unit.setEanNumber(organizationGeneral.getEanNumber());
        unit.setCostCenterCode(organizationGeneral.getCostCenterCode());
        unit.setCostCenterName(organizationGeneral.getCostCenterName());
        unit.setOwnershipType(ownershipType);
        unit.setBusinessTypes(businessTypes);
        unit.setIndustryType(industryType);
        unit.setContractType(contractType);
        unit.setClientSince(DateConverter.parseDate(organizationGeneral.getClientSince()).getTime());
        unit.setKairosHub(organizationGeneral.isKairosHub());
        unit.setKairosStatus(kairosStatus);
        unit.setExternalId(organizationGeneral.getExternalId());
        unit.setEndTimeDeduction(organizationGeneral.getPercentageWorkDeduction());
        unit.setKmdExternalId(organizationGeneral.getKmdExternalId());
        unit.setDayShiftTimeDeduction(organizationGeneral.getDayShiftTimeDeduction());
        unit.setNightShiftTimeDeduction(organizationGeneral.getNightShiftTimeDeduction());
        organizationGraphRepository.save(unit);
        return true;

    }


    public Map<String, Object> getParentOrganization(Long countryId) {
        Map<String, Object> data = new HashMap<>();
        OrganizationQueryResult organizationQueryResult = organizationGraphRepository.getParentOrganizationOfRegion(countryId);
        OrganizationCreationData organizationCreationData = organizationGraphRepository.getOrganizationCreationData(countryId);
        List<Map<String, Object>> zipCodes = FormatUtil.formatNeoResponse(zipCodeGraphRepository.getAllZipCodeByCountryId(countryId));
        if(Optional.ofNullable(organizationCreationData).isPresent()){
            organizationCreationData.setZipCodes(zipCodes);
        }
        organizationCreationData.setCompanyTypes(CompanyType.getListOfCompanyType());
        organizationCreationData.setCompanyUnitTypes(CompanyUnitType.getListOfCompanyUnitType());
        List<Map<String, Object>> orgData = new ArrayList<>();
        for (Map<String, Object> organizationData : organizationQueryResult.getOrganizations()) {
            HashMap<String, Object> orgBasicData = new HashMap<>();
            orgBasicData.put("orgData", organizationData);
            Map<String, Object> address = (Map<String, Object>) organizationData.get("contactAddress");
            orgBasicData.put("municipalities", (address.get("zipCodeId") == null) ? Collections.emptyMap() : FormatUtil.formatNeoResponse(regionGraphRepository.getGeographicTreeData((long) address.get("zipCodeId"))));
            orgData.add(orgBasicData);
        }
        data.put("globalData", organizationCreationData);
        data.put("organization", orgData);
        return data;
    }


    public Map<String, Object> getOrganizationGdprAndWorkcenter(Long organizationId, Long countryId) {
        Map<String, Object> data = new HashMap<>();
        OrganizationQueryResult organizationQueryResult = organizationGraphRepository.getOrganizationGdprAndWorkCenter(organizationId);
        for (Map<String, Object> organizationData : organizationQueryResult.getOrganizations()) {
            HashMap<String, Object> orgBasicData = new HashMap<>();
            orgBasicData.put("orgData", organizationData);
            Map<String, Object> address = (Map<String, Object>) organizationData.get("contactAddress");
            orgBasicData.put("municipalities", (address.get("zipCodeId") == null) ? Collections.emptyMap() : FormatUtil.formatNeoResponse(regionGraphRepository.getGeographicTreeData((long) address.get("zipCodeId"))));
            if(organizationData.get("gdprUnit")!=null && (boolean)organizationData.get("gdprUnit")==true) {
                data.put("gdprUnit", orgBasicData);
            }else if(organizationData.get("workCenterUnit")!=null && (boolean)organizationData.get("workCenterUnit")){
                data.put("workCenterUnit", orgBasicData);
            }
        }
        return data;
    }

    public Organization getByPublicPhoneNumber(String phoneNumber) {
        logger.debug(":::::::::: " + phoneNumber + " ::::::::::::::::");
        return organizationGraphRepository.findOrganizationByPublicPhoneNumber(phoneNumber);
    }

    public Organization getOrganizationByExternalId(String externalId) {
        return organizationGraphRepository.findByExternalId(externalId);

    }

    public OrganizationStaffWrapper getOrganizationAndStaffByExternalId(String externalId, Long staffExternalId, Long staffTimeCareEmploymentId) {
        OrganizationStaffWrapper organizationStaffWrapper = new OrganizationStaffWrapper();
        organizationStaffWrapper.setOrganization(organizationGraphRepository.findByExternalId(externalId));
        StaffUnitPositionWrapper staffData = staffGraphRepository.getStaff(staffExternalId, staffTimeCareEmploymentId);
        organizationStaffWrapper.setStaff(staffData.getStaff());
        organizationStaffWrapper.setUnitPosition(staffData.getUnitPosition());
        return organizationStaffWrapper;
    }

    public boolean deleteOrganizationById(long parentOrganizationId, long childOrganizationId) {
        organizationGraphRepository.deleteChildRelationOrganizationById(parentOrganizationId, childOrganizationId);
        organizationGraphRepository.deleteOrganizationById(childOrganizationId);
        return organizationGraphRepository.findOne(childOrganizationId) == null;
    }

    public Map<String, Object> getManageHierarchyData(long unitId) {

        Organization organization = organizationGraphRepository.findOne(unitId);
        if (!Optional.ofNullable(organization).isPresent()) {
            throw new InternalError("organization is null");
        }

        Long countryId = countryGraphRepository.getCountryIdByUnitId(unitId);

        Map<String, Object> response = new HashMap<>(2);
        List<Map<String, Object>> units = organizationGraphRepository.getUnits(unitId);
        response.put("units", units.size() != 0 ? units.get(0).get("unitList") : Collections.emptyList());

        List<Map<String, Object>> groups = organizationGraphRepository.getGroups(unitId);
        response.put("groups", groups.size() != 0 ? groups.get(0).get("groups") : Collections.emptyList());

        if (Optional.ofNullable(countryId).isPresent()) {
            response.put("zipCodes", FormatUtil.formatNeoResponse(zipCodeGraphRepository.getAllZipCodeByCountryId(countryId)));
        }

        List<Map<String, Object>> organizationTypes = organizationTypeGraphRepository.getOrganizationTypesForUnit(unitId);
        List<Map<String, Object>> organizationTypesForUnit = new ArrayList<>();
        for (Map<String, Object> organizationType : organizationTypes) {
            organizationTypesForUnit.add((Map<String, Object>) organizationType.get("data"));
        }

        List<BusinessType> businessTypes=businessTypeGraphRepository.findBusinesTypesByCountry(countryId);
        response.put("organizationTypes", organizationTypesForUnit);
        response.put("businessTypes", businessTypes);
        response.put("level", organization.getLevel());
        response.put("companyTypes", CompanyType.getListOfCompanyType());
        response.put("companyUnitTypes", CompanyUnitType.getListOfCompanyUnitType());
        response.put("companyCategories",companyCategoryGraphRepository.findCompanyCategoriesByCountry(countryId));
        return response;
    }

    public Organization updateExternalId(long organizationId, long externalId) {
        Organization organization = organizationGraphRepository.findOne(organizationId);
        if (organization == null) {
            return null;
        }
        organization.setExternalId(String.valueOf(externalId));
        organizationGraphRepository.save(organization);
        return organization;

    }

    public Map setEstimoteCredentials(long organization, Map<String, String> payload) {
        Organization organizationObj = organizationGraphRepository.findOne(organization);
        if (
                organizationObj != null
                        && payload.containsKey("estimoteAppId")
                        && payload.containsKey("estimoteAppToken")
                        && payload.get("estimoteAppId") != null
                        && payload.get("estimoteAppToken") != null
                ) {
            organizationObj.setEstimoteAppId(payload.get("estimoteAppId"));
            organizationObj.setEstimoteAppToken(payload.get("estimoteAppToken"));
            return payload;
        }
        return null;
    }

    public Map getEstimoteCredentials(long organization) {
        Map returnData = new HashMap();
        Organization organizationObj = organizationGraphRepository.findOne(organization);
        if (organizationObj != null) {
            returnData.put("estimoteAppId", organizationObj.getEstimoteAppId());
            returnData.put("estimoteAppToken", organizationObj.getEstimoteAppToken());
        }
        return returnData;
    }

    public Boolean createLinkParentWithChildOrganization(Long parentId, Long childId) {
        Organization parent = organizationGraphRepository.findOne(parentId);
        Organization child = organizationGraphRepository.findOne(childId);
        organizationGraphRepository.createChildOrganization(parent.getId(), child.getId());
        accessGroupService.createDefaultAccessGroups(child);
        timeSlotService.createDefaultTimeSlots(child,TimeSlotType.SHIFT_PLANNING);
        timeSlotService.createDefaultTimeSlots(child,TimeSlotType.TASK_PLANNING);
        return true;
    }

    public Long getOrganizationIdByTeamIdOrGroupIdOrOrganizationId(String type, Long id) {
        if (ORGANIZATION.equalsIgnoreCase(type)) {
            Organization organization = organizationGraphRepository.findOne(id);
            if (organization == null) {
                return null;
            }
            return organization.getId();
        } else if (TEAM.equalsIgnoreCase(type)) {
            return organizationGraphRepository.getOrganizationByTeamId(id).getId();
        } else if (GROUP.equalsIgnoreCase(type)) {
            return organizationGraphRepository.getOrganizationByGroupId(id).getOrganization().getId();
        }
        return null;
    }

    public String getWorkPlaceFromTimeCare(GetAllWorkPlacesResponse workPlaces) {
        try {
            logger.info(" workPlaces---> " + workPlaces.getWorkPlaceList().size());
            for (GetAllWorkPlacesResult workPlace : workPlaces.getWorkPlaceList()) {
                logger.info("workPlace " + workPlace.getName());
                Organization organization = getOrganizationByExternalId(workPlace.getId().toString());
                logger.info("organization--exist-----> " + organization);
                if (organization == null) {
                    organization = new Organization(workPlace.getName());
                    organization.setExternalId(String.valueOf(workPlace.getId()));
                    if (workPlace.getIsParent()) {
                        logger.info("Creating parent organization " + workPlace.getName());
                        createOrganization(organization, null);
                    } else {
                        logger.info("Creating child organization " + workPlace.getName());
                        logger.info("Sending Parent ID: " + workPlace.getParentWorkPlaceID());
                        Organization parentOrganization = getOrganizationByExternalId(workPlace.getParentWorkPlaceID().toString());
                        logger.info("parentOrganization  ID: " + parentOrganization.getId());
                        createOrganization(organization, parentOrganization.getId());
                    }
                }
            }
            return "Received";
        } catch (Exception exception) {
            logger.warn("Exception while hitting rest for saving Organizations", exception);
        }
        return null;
    }

    public Integer checkDuplicationOrganizationRelation(Long organizationId, Long unitId) {
        return organizationGraphRepository.checkParentChildRelation(organizationId, unitId);
    }


    /**
     * @param unitId
     * @return
     * @auther anil maurya
     * this method is called from task micro service
     */
    public Map<String, Object> getCommonDataOfOrganization(Long unitId) {
        Map<String, Object> data = new HashMap<String, Object>();
        List<Map<String, Object>> organizationSkills = organizationGraphRepository.getSkillsOfParentOrganization(unitId);
        List<Map<String, Object>> orgSkillRel = new ArrayList<>(organizationSkills.size());
        for (Map<String, Object> map : organizationSkills) {
            orgSkillRel.add((Map<String, Object>) map.get("data"));
        }
        data.put("skillsOfOrganization", orgSkillRel);
        data.put("teamsOfOrganization", teamService.getTeamsInUnit(unitId));
        data.put("zipCodes", regionService.getAllZipCodes());

        return data;
    }

    /**
     * @param organizationId
     * @param unitId
     * @return
     * @auther anil maurya
     * this method is called from task micro service
     */
    public Map<String, Object> getUnitVisitationInfo(long organizationId, long unitId) {

        Map<String, Object> organizationResult = new HashMap();
        Map<String, Object> unitData = new HashMap();

        Map<String, Object> organizationTimeSlotList = timeSlotService.getTimeSlots(unitId);
        unitData.put("organizationTimeSlotList", organizationTimeSlotList.get("timeSlots"));

        Long countryId = countryGraphRepository.getCountryIdByUnitId(organizationId);
        List<Map<String, Object>> clientStatusList = citizenStatusService.getCitizenStatusByCountryId(countryId);
        unitData.put("clientStatusList", clientStatusList);
        List<Object> localAreaTagsList = new ArrayList<>();
        List<Map<String, Object>> tagList = organizationMetadataRepository.findAllByIsDeletedAndUnitId(unitId);
        for (Map<String, Object> map : tagList) {
            localAreaTagsList.add(map.get("tags"));
        }
        unitData.put("localAreaTags", localAreaTagsList);
        unitData.put("serviceTypes", organizationServiceRepository.getOrganizationServiceByOrgId(unitId));
        Map<String, Object> timeSlotData = timeSlotService.getTimeSlots(organizationId);

        if (timeSlotData != null) {
            unitData.put("timeSlotList", timeSlotData);
        }
        organizationResult.put("unitData", unitData);
        List<Map<String, Object>> citizenList = clientService.getOrganizationClientsExcludeDead(unitId);

        organizationResult.put("citizenList", citizenList);

        return organizationResult;

    }

    public boolean updateOneTimeSyncsettings(long unitId) {
        Organization organization = organizationGraphRepository.findOne(unitId);
        if (organization == null) {
            logger.debug("Searching organization by id " + unitId);
            throw new DataNotFoundByIdException("Incorrect id of an organization " + unitId);
        }
        organization.setOneTimeSyncPerformed(true);
        organizationGraphRepository.save(organization);
        return true;
    }

    public boolean updateAutoGenerateTaskSettings(long unitId) {
        Organization organization = organizationGraphRepository.findOne(unitId);
        if (organization == null) {
            logger.debug("Searching organization by id " + unitId);
            throw new DataNotFoundByIdException("Incorrect id of an organization " + unitId);
        }
        organization.setAutoGeneratedPerformed(true);
        organizationGraphRepository.save(organization);
        return true;
    }

    public Map<String, Object> getTaskDemandSupplierInfo(Long unitId) {
        Map<String, Object> supplierInfo = new HashMap();
        Organization weekdaySupplier = organizationGraphRepository.findOne(unitId, 0);
        supplierInfo.put("weekdaySupplier", weekdaySupplier.getName());
        supplierInfo.put("weekdaySupplierId", weekdaySupplier.getId());
        return supplierInfo;
    }

    public Organization getParentOrganizationOfCityLevel(Long unitId) {
        return organizationGraphRepository.getParentOrganizationOfCityLevel(unitId);
    }

    public Organization getParentOfOrganization(Long unitId) {
        return organizationGraphRepository.getParentOfOrganization(unitId);
    }

    public Organization getOrganizationByTeamId(Long teamId) {
        return organizationGraphRepository.getOrganizationByTeamId(teamId);
    }

    public Map<String, Object> getPrerequisitesForTimeCareTask(GetWorkShiftsFromWorkPlaceByIdResult workShift) {

        Organization organization = organizationGraphRepository.findByExternalId(workShift.getWorkPlace().getId().toString());
        if (organization == null) {
            throw new DataNotFoundByIdException("Incorrect id of an organization");
        }
        Map<String, Object> requiredDataForTimeCareTask = new HashMap<>();
        OrganizationContactAddress organizationContactData = organizationGraphRepository.getContactAddressOfOrg(organization.getId());
        Staff staff = staffGraphRepository.findByExternalId(workShift.getPerson().getId());
        AbsenceTypes absenceTypes = absenceTypesRepository.findByName(workShift.getActivity().getName());
        requiredDataForTimeCareTask.put("organizationContactAddress", organizationContactData);
        requiredDataForTimeCareTask.put("staff", staff);
        requiredDataForTimeCareTask.put("absenceTypes", absenceTypes);
        requiredDataForTimeCareTask.put("organization", organization);
        return requiredDataForTimeCareTask;
    }

    public Boolean verifyOrganizationExpertise(OrganizationMappingActivityTypeDTO organizationMappingActivityTypeDTO) {
        Long matchedExpertise = expertiseGraphRepository.findAllExpertiseCountMatchedByIds(organizationMappingActivityTypeDTO.getExpertises());
        if (matchedExpertise != organizationMappingActivityTypeDTO.getExpertises().size()) {
            throw new DataNotMatchedException("Mismatched Expertise while updating organizationMapping ");
        }
        Long matchedRegion = regionGraphRepository.findAllRegionCountMatchedByIds(organizationMappingActivityTypeDTO.getRegions());
        if (matchedRegion != organizationMappingActivityTypeDTO.getRegions().size()) {
            throw new DataNotMatchedException("Mismatched Region while updating organizationMapping ");
        }
        List<Long> organizationTypeAndSubTypeIds = new ArrayList<Long>();
        organizationTypeAndSubTypeIds.addAll(organizationMappingActivityTypeDTO.getOrganizationTypes());
        organizationTypeAndSubTypeIds.addAll(organizationMappingActivityTypeDTO.getOrganizationSubTypes());

        Long matchedOrganizationTypeAndSubTypeIdsCount = organizationGraphRepository.findAllOrgCountMatchedByIds(organizationTypeAndSubTypeIds);
        if (matchedOrganizationTypeAndSubTypeIdsCount != organizationTypeAndSubTypeIds.size()) {
            throw new DataNotMatchedException("Mismatched organizations while updating organizationMapping ");
        }
        return true;
    }

    public List<Long> getAllOrganizationIds() {
        return organizationGraphRepository.findAllOrganizationIds();
    }

    public OrganizationTypeAndSubTypeDTO getOrganizationTypeAndSubTypes(Long id, String type) {
        Organization organization = getOrganizationDetail(id, type);
        OrganizationTypeAndSubTypeDTO organizationTypeAndSubTypeDTO = new OrganizationTypeAndSubTypeDTO();

        if (!organization.isParentOrganization()) {
            Organization parentOrganization = organizationGraphRepository.getParentOfOrganization(organization.getId());
            organizationTypeAndSubTypeDTO.setParentOrganizationId(parentOrganization.getId());
            organizationTypeAndSubTypeDTO.setParent(false);
            //organizationTypeAndSubTypeDTO.setUnitId(organization.getId());
            //return organizationTypeAndSubTypeDTO;
        } else {

            organizationTypeAndSubTypeDTO.setParent(true);
        }

        List<Long> orgTypeIds = organizationTypeGraphRepository.getOrganizationTypeIdsByUnitId(organization.getId());
        List<Long> orgSubTypeIds = organizationTypeGraphRepository.getOrganizationSubTypeIdsByUnitId(organization.getId());
        organizationTypeAndSubTypeDTO.setOrganizationTypes(Optional.ofNullable(orgTypeIds).orElse(Collections.EMPTY_LIST));
        organizationTypeAndSubTypeDTO.setOrganizationSubTypes(Optional.ofNullable(orgSubTypeIds).orElse(Collections.EMPTY_LIST));

        organizationTypeAndSubTypeDTO.setUnitId(organization.getId());

        return organizationTypeAndSubTypeDTO;
    }

    public OrganizationExternalIdsDTO saveKMDExternalId(Long unitId, OrganizationExternalIdsDTO organizationExternalIdsDTO) {
        Organization organization = organizationGraphRepository.findOne(unitId);
        organization.setKmdExternalId(organizationExternalIdsDTO.getKmdExternalId());
        organization.setExternalId(organizationExternalIdsDTO.getTimeCareExternalId());
        organizationGraphRepository.save(organization);
        return organizationExternalIdsDTO;

    }

    public TimeSlotsDeductionDTO saveTimeSlotPercentageDeduction(Long unitId, TimeSlotsDeductionDTO timeSlotsDeductionDTO) {
        Organization organization = organizationGraphRepository.findOne(unitId);
        organization.setDayShiftTimeDeduction(timeSlotsDeductionDTO.getDayShiftTimeDeduction());
        organization.setNightShiftTimeDeduction(timeSlotsDeductionDTO.getNightShiftTimeDeduction());
        organizationGraphRepository.save(organization);
        return timeSlotsDeductionDTO;

    }

    public OrganizationExternalIdsDTO getKMDExternalId(Long unitId) {
        Organization organization = organizationGraphRepository.findOne(unitId);
        OrganizationExternalIdsDTO organizationExternalIdsDTO = new OrganizationExternalIdsDTO();
        organizationExternalIdsDTO.setKmdExternalId(organization.getKmdExternalId());
        organizationExternalIdsDTO.setTimeCareExternalId(organization.getExternalId());
        return organizationExternalIdsDTO;

    }

    public TimeSlotsDeductionDTO getTimeSlotPercentageDeduction(Long unitId) {
        Organization organization = organizationGraphRepository.findOne(unitId);
        TimeSlotsDeductionDTO timeSlotsDeductionDTO = new TimeSlotsDeductionDTO();
        timeSlotsDeductionDTO.setNightShiftTimeDeduction(organization.getNightShiftTimeDeduction());
        timeSlotsDeductionDTO.setDayShiftTimeDeduction(organization.getDayShiftTimeDeduction());
        return timeSlotsDeductionDTO;

    }

    public List<VehicleQueryResult> getVehicleList(long unitId) {
        Organization organization = organizationGraphRepository.findOne(unitId);
        if (!Optional.ofNullable(organization).isPresent()) {
            logger.debug("Searching organization by id " + unitId);
            throw new DataNotFoundByIdException("Incorrect id of an organization " + unitId);
        }
        Long countryId = organizationGraphRepository.getCountryId(unitId);
        return countryGraphRepository.getResourcesWithFeaturesByCountry(countryId);
    }

    public List<Long> getAllOrganizationWithoutPhases() {
        List<Long> organizationIds = organizationGraphRepository.getAllOrganizationWithoutPhases();
        return organizationIds;
    }

    public Long getOrganization(Long id, String type) {
        Organization organization = null;
        switch (type.toLowerCase()) {
            case ORGANIZATION:
                organization = organizationGraphRepository.findOne(id);
                break;
            case GROUP:
                organization = groupService.getUnitByGroupId(id);
                break;
            case TEAM:
                organization = teamService.getOrganizationByTeamId(id);
                break;
            default:
                throw new UnsupportedOperationException("Type is not valid");
        }
        if (!Optional.ofNullable(organization).isPresent()) {
            throw new DataNotFoundByIdException("Organization not found-" + id);
        }
        return organization.getId();
    }

    public Organization getOrganizationDetail(Long id, String type) {
        Organization organization = null;
        switch (type.toLowerCase()) {
            case ORGANIZATION:
                organization = organizationGraphRepository.findOne(id, 1);
                break;
            case GROUP:
                organization = groupService.getUnitByGroupId(id);
                break;
            case TEAM:
                organization = teamService.getOrganizationByTeamId(id);
                break;
            default:
                throw new UnsupportedOperationException("Type is not valid");
        }
        if (!Optional.ofNullable(organization).isPresent()) {
            throw new DataNotFoundByIdException("Organization not found-" + id);
        }
        return organization;
    }

    public void updateOrganizationWithoutPhases(List<Long> organizationIds) {

        organizationGraphRepository.updateOrganizationWithoutPhases(organizationIds);
    }


    /**
     * @param unitId
     * @return
     * @auther anil maurya
     */
    public OrganizationSkillAndOrganizationTypesDTO getOrganizationAvailableSkillsAndOrganizationTypesSubTypes(Long unitId) {
        OrganizationTypeAndSubTypeDTO organizationTypeAndSubTypeDTO = this.getOrganizationTypeAndSubTypes(unitId, "organization");
        return new OrganizationSkillAndOrganizationTypesDTO(organizationTypeAndSubTypeDTO, skillService.getSkillsOfOrganization(unitId));
    }


    public List<DayType> getDayType(Long organizationId, Date date) {
        Long countryId = organizationGraphRepository.getCountryId(organizationId);
        return dayTypeService.getDayTypeByDate(countryId, date);
    }

    public List<DayType> getAllDayTypeofOrganization(Long organizationId) {
        Long countryId = organizationGraphRepository.getCountryId(organizationId);
        return dayTypeService.getAllDayTypeByCountryId(countryId);

    }

    public List<Map<String, Object>> getUnitsByOrganizationIs(Long orgID) {
        return organizationGraphRepository.getOrganizationChildList(orgID);
    }


    public List<OrganizationType> getOrganizationTypeByCountryId(Long countryId) {
        return organizationTypeGraphRepository.findOrganizationTypeByCountry(countryId);
    }

    public OrganizationType getOrganizationTypeByCountryAndId(Long countryId, Long orgTypeId) {
        return organizationTypeGraphRepository.getOrganizationTypeById(countryId, orgTypeId);
    }

    public OrganizationType getOneDefaultOrganizationTypeByCountryId(Long countryId) {
        return organizationTypeGraphRepository.getOneDefaultOrganizationTypeById(countryId);
    }

    public List<OrganizationType> getOrganizationSubTypeById(Long orgTypeId) {
        return organizationTypeGraphRepository.getOrganizationSubTypesByTypeId(orgTypeId);
    }

    public Map<String, Object> getAvailableZoneIds(Long unitId) {
        Set<String> allZones = ZoneId.getAvailableZoneIds();
        List<String> zoneList = new ArrayList<>(allZones);
        Collections.sort(zoneList);

        Map<String, Object> timeZonesData = new HashMap<>();
        Organization unit = organizationGraphRepository.findOne(unitId);
        if (!Optional.ofNullable(unit).isPresent()) {
            throw new DataNotFoundByIdException("Incorrect id of an organization " + unitId);
        }

        timeZonesData.put("selectedTimeZone", unit.getTimeZone() != null ? unit.getTimeZone().getId() : null);
        timeZonesData.put("allTimeZones", zoneList);
        return timeZonesData;
    }

    public boolean assignUnitTimeZone(Long unitId, String zoneIdString) {
        ZoneId zoneId = ZoneId.of(zoneIdString);
        if (!Optional.ofNullable(zoneId).isPresent()) {
            throw new InternalError("Zone Id not found by " + zoneIdString);
        }
        Organization unit = organizationGraphRepository.findOne(unitId);
        unit.setTimeZone(zoneId);
        organizationGraphRepository.save(unit);
        return true;
    }


    public Organization fetchParentOrganization(Long unitId) {
        Organization parent = null;
        Organization unit = organizationGraphRepository.findOne(unitId, 0);
        if (!unit.isParentOrganization() && OrganizationLevel.CITY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOrganizationOfCityLevel(unit.getId());
        } else if (!unit.isParentOrganization() && OrganizationLevel.COUNTRY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOfOrganization(unit.getId());
        } else {
            parent = unit;
        }
        return parent;
    }

    public OrganizationMappingDTO getEmploymentTypeWithExpertise(Long unitId) {
        OrganizationMappingDTO organizationMappingDTO = new OrganizationMappingDTO();
        // Set employment type
        organizationMappingDTO.setEmploymentTypes(employmentTypeGraphRepository.getAllEmploymentTypeByOrganization(unitId, false));
        // set Expertise
        organizationMappingDTO.setExpertise(expertiseGraphRepository.getAllExpertiseByOrganizationId(unitId));
        return organizationMappingDTO;
    }


    // For Test Cases
    public Organization getOneParentUnitByCountry(Long countryId){
        return organizationGraphRepository.getOneParentUnitByCountry(countryId);
    }

    public WTABasicDetailsDTO getWTARelatedInfo(Long countryId,Long organizationId,Long organizationSubTypeId,Long organizationTypeId,Long expertiseId){
        WTABasicDetailsDTO wtaBasicDetailsDTO = new WTABasicDetailsDTO();
        if(expertiseId!=null) {
            Expertise expertise = expertiseGraphRepository.findOne(expertiseId, 0);
            if(expertise!=null){
                ExpertiseResponseDTO expertiseResponseDTO = new ExpertiseResponseDTO();
                BeanUtils.copyProperties(expertise,expertiseResponseDTO);
                wtaBasicDetailsDTO.setExpertiseResponse(expertiseResponseDTO);
            }
        }
        if(organizationId!=null) {
            Organization organization = organizationGraphRepository.findOne(organizationId, 0);
            if(organization!=null){
                OrganizationDTO organizationDTO = new OrganizationDTO();
                BeanUtils.copyProperties(organization,organizationDTO);
                wtaBasicDetailsDTO.setOrganization(organizationDTO);
            }
        }
        if(countryId!=null) {
            Country country = countryGraphRepository.findOne(countryId, 0);
            if(country!=null){
                CountryDTO countryDTO = new CountryDTO();
                BeanUtils.copyProperties(country,countryDTO);
                wtaBasicDetailsDTO.setCountryDTO(countryDTO);
            }
        }
        if(organizationTypeId!=null) {
            OrganizationType organizationType = organizationTypeGraphRepository.findOne(organizationTypeId, 0);
            if(organizationType!=null){
                OrganizationTypeDTO organizationTypeDTO = new OrganizationTypeDTO();
                BeanUtils.copyProperties(organizationType,organizationTypeDTO);
                wtaBasicDetailsDTO.setOrganizationType(organizationTypeDTO);
            }
        }
        if(organizationSubTypeId!=null) {
            OrganizationType organizationSubType = organizationTypeGraphRepository.findOne(organizationSubTypeId,0);
            List<Organization> organizations = organizationTypeGraphRepository.getOrganizationsByOrganizationType(organizationSubTypeId);
           if(organizationSubType!=null){
               OrganizationTypeDTO organizationSubTypeDTO = new OrganizationTypeDTO();
               BeanUtils.copyProperties(organizationSubType,organizationSubTypeDTO);
               wtaBasicDetailsDTO.setOrganizationSubType(organizationSubTypeDTO);
           }
           if(organizations!=null){
               List<OrganizationDTO> organizationDTOS = new ArrayList<>();
               organizations.forEach(organization->{
                   OrganizationDTO organizationDTO = new OrganizationDTO();
                   ObjectMapperUtils.copyProperties(organization,organizationDTO);
                    organizationDTOS.add(organizationDTO);
               });
               wtaBasicDetailsDTO.setOrganizations(organizationDTOS);
           }
        }
        return wtaBasicDetailsDTO;
    }

    public ZoneId getTimeZoneStringOfUnit(Long unitId) {
        Organization unit = organizationGraphRepository.findOne(unitId, 0);
        if (!Optional.ofNullable(unit).isPresent()) {
            throw new DataNotFoundByIdException("Incorrect id of an organization " + unitId);
        }
        return unit.getTimeZone(); //(Optional.ofNullable(unit.getTimeZone()).isPresent() ? unit.getTimeZone().toString() : "") ;
    }

    public Object initialOptaplannerSync(Long organizationId, Long unitId) {
        List<Staff> staff=staffGraphRepository.getAllStaffByUnitId(unitId);
        plannerSyncService.publishAllStaff(unitId,staff,IntegrationOperation.CREATE);
        List<UnitPositionEmploymentTypeRelationShip> unitPositionEmploymentTypeRelationShips=unitPositionGraphRepository.findUnitPositionEmploymentTypeRelationshipByParentOrganizationId(organizationId);
        plannerSyncService.publishAllUnitPositions(organizationId,unitPositionEmploymentTypeRelationShips,IntegrationOperation.CREATE);
        phaseRestClient.initialOptaplannerSync(unitId);
        return null;
    }
}



