package com.kairos.service.expertise;

import com.kairos.commons.utils.ObjectUtils;
import com.kairos.config.env.EnvConfig;
import com.kairos.dto.activity.cta_compensation_setting.CTACompensationSettingDTO;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.organization.services.OrganizationServicesAndLevelQueryResult;
import com.kairos.persistence.model.organization.union.Location;
import com.kairos.persistence.model.staff.personal_details.StaffPersonalDetailQueryResult;
import com.kairos.persistence.model.user.expertise.response.ExpertiseLineQueryResult;
import com.kairos.persistence.model.user.expertise.response.ExpertiseLocationStaffQueryResult;
import com.kairos.persistence.model.user.expertise.response.ExpertiseQueryResult;
import com.kairos.persistence.repository.organization.OrganizationBaseRepository;
import com.kairos.persistence.repository.organization.OrganizationServiceRepository;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.expertise.ExpertiseGraphRepository;
import com.kairos.persistence.repository.user.expertise.OrganizationPersonalizeLocationRelationShipGraphRepository;
import com.kairos.persistence.repository.user.staff.StaffGraphRepository;
import com.kairos.service.country.CountryService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.integration.ActivityIntegrationService;
import com.kairos.service.organization.OrganizationService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kairos.constants.AppConstants.FORWARD_SLASH;


/**
 * CreatedBy vipulpandey on 19/11/18
 **/
@Service
@Transactional
public class ExpertiseUnitService {
    @Inject
    private ExpertiseGraphRepository expertiseGraphRepository;
    @Inject
    private UnitGraphRepository unitGraphRepository;
    @Inject
    private OrganizationServiceRepository organizationServiceRepository;
    @Inject
    private StaffGraphRepository staffGraphRepository;
    @Inject
    private OrganizationService organizationService;
    @Inject
    private EnvConfig envConfig;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private CountryService countryService;
    @Inject
    private OrganizationPersonalizeLocationRelationShipGraphRepository organizationLocationRelationShipGraphRepository;
    @Inject
    private OrganizationBaseRepository organizationBaseRepository;
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private ActivityIntegrationService activityIntegrationService;



    public List<ExpertiseQueryResult> findAllExpertise(Long unitId) {
        Long countryId = countryService.getCountryIdByUnitId(unitId);
        List<Long> allUnitIds = organizationBaseRepository.fetchAllUnitIds(unitId);
        OrganizationServicesAndLevelQueryResult servicesAndLevel = organizationServiceRepository.getOrganizationServiceIdsByOrganizationId(allUnitIds);
        List<ExpertiseQueryResult> expertises=new ArrayList<>();
        if(ObjectUtils.isNotNull(servicesAndLevel)){
            expertises  = expertiseGraphRepository.findExpertiseByOrganizationServicesForUnit(countryId, servicesAndLevel.getServicesId());
            expertises.forEach(expertiseQueryResult -> {
                expertiseQueryResult.setUnitId(unitId);
            });
            List<Long> allExpertiseIds=expertises.stream().map(ExpertiseQueryResult::getId).collect(Collectors.toList());
            List<ExpertiseLineQueryResult> expertiseLineQueryResults=expertiseGraphRepository.findAllExpertiseLines(allExpertiseIds);
            Map<Long,List<ExpertiseLineQueryResult>> expertiseLineQueryResultMap=expertiseLineQueryResults.stream().collect(Collectors.groupingBy(ExpertiseLineQueryResult::getExpertiseId));
            expertises.forEach(expertiseQueryResult -> expertiseQueryResult.setExpertiseLines(expertiseLineQueryResultMap.get(expertiseQueryResult.getId())));
        }
        if (CollectionUtils.isNotEmpty(expertises)) {
            List<Long> expertiseIds = expertises.stream().map(ExpertiseQueryResult::getId).collect(Collectors.toList());
            List<ExpertiseLocationStaffQueryResult> locations = organizationLocationRelationShipGraphRepository.getExpertisesLocationInOrganization(expertiseIds, unitId);
            List<ExpertiseLocationStaffQueryResult> staffs = staffGraphRepository.findAllUnionRepresentativeOfExpertiseInUnit(expertiseIds, organizationService.fetchParentOrganization(unitId).getId());
            Map<Long, Map<String, Object>> staffMap = staffs.stream().collect(Collectors.toMap(ExpertiseLocationStaffQueryResult::getExpertiseId, ExpertiseLocationStaffQueryResult::getStaff));
            Map<Long, Location> locationMap = locations.stream().collect(Collectors.toMap(ExpertiseLocationStaffQueryResult::getExpertiseId, ExpertiseLocationStaffQueryResult::getLocation));
            expertises.forEach(current -> {
                current.setUnionRepresentative(staffMap.get(current.getId()));
                current.setUnionLocation(locationMap.get(current.getId()));
            });
        }
        return expertises;


    }

    public Map<String, Object> getStaffListOfExpertise(Long expertiseId, Long unitId) {
        Map<String, Object> response = new HashMap<>();
        Organization organization = organizationService.fetchParentOrganization(unitId);
        List<StaffPersonalDetailQueryResult> staffs = staffGraphRepository.getAllStaffByUnitIdAndExpertiseId(organization.getId(), envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath(), expertiseId);
        List<Location> locations = expertiseGraphRepository.findAllLocationsOfUnionInExpertise(expertiseId);

        response.put("staffs", staffs);
        response.put("locations", locations);
        return response;
    }

    public boolean updateExpertiseAtUnit(Long unitId, Long staffId, Long expertiseId, Long locationId) {
        organizationLocationRelationShipGraphRepository.setLocationInOrganizationForExpertise(expertiseId, unitId, locationId);
        Organization organization=organizationService.fetchParentOrganization(unitId);
        staffGraphRepository.removePreviousUnionRepresentativeOfExpertiseInUnit(organization.getId(), expertiseId);
        staffGraphRepository.assignStaffAsUnionRepresentativeOfExpertise(staffId, expertiseId);
        return true;
    }

    public List<ExpertiseQueryResult> findAllExpertiseWithUnits(Long unitId) {
        Long countryId = countryGraphRepository.getCountryIdByUnitId(unitId);
        List<CTACompensationSettingDTO> ctaCompensationSettingDTOS = activityIntegrationService.getCTACompensationSettingByCountryId(countryId);
        Map<Long,CTACompensationSettingDTO> ctaCompensationSettingDTOMap = ctaCompensationSettingDTOS.stream().collect(Collectors.toMap(ctaCompensationSettingDTO -> ctaCompensationSettingDTO.getExpertiseId(),v->v));
        List<ExpertiseQueryResult> expertiseQueryResults = expertiseGraphRepository.findAllExpertiseWithUnitIds();
        expertiseQueryResults.forEach(expertiseQueryResult -> expertiseQueryResult.setCtaCompensationSetting(ctaCompensationSettingDTOMap.get(expertiseQueryResult.getId())));
        return expertiseQueryResults;
    }

    public List<Long> getExpertiseIdsByUnit(Long unitId){
        List<Long> allUnitIds = organizationBaseRepository.fetchAllUnitIds(unitId);
        OrganizationServicesAndLevelQueryResult servicesAndLevel = organizationServiceRepository.getOrganizationServiceIdsByOrganizationId(allUnitIds);
        return expertiseGraphRepository.getExpertiseIdsByServices(servicesAndLevel.getServicesId());

    }


}

