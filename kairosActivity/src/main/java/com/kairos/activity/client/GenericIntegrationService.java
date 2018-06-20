package com.kairos.activity.client;

import com.kairos.activity.enums.IntegrationOperation;
import com.kairos.activity.response.dto.shift.StaffUnitPositionDetails;
import com.kairos.activity.service.exception.ExceptionService;
import com.kairos.activity.util.ObjectMapperUtils;
import com.kairos.response.dto.web.access_group.UserAccessRoleDTO;
import com.kairos.response.dto.web.day_type.DayTypeEmploymentTypeWrapper;
import com.kairos.response.dto.web.open_shift.PriorityGroupDefaultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GenericIntegrationService {
    @Autowired
    GenericRestClient genericRestClient;
    @Autowired
    ExceptionService exceptionService;

    public Long getUnitPositionId(Long unitId, Long staffId, Long expertiseId) {
        Integer value = genericRestClient.publish(null, unitId, true, IntegrationOperation.GET, "/staff/{staffId}/expertise/{expertiseId}/unitPositionId", null, staffId, expertiseId);
        if (value == null) {
            exceptionService.dataNotFoundByIdException("message.unitPosition.notFound", expertiseId);
        }
        return value.longValue();
    }

    public PriorityGroupDefaultData getExpertiseAndEmployment(Long countryId) {
        return ObjectMapperUtils.copyPropertiesByMapper(genericRestClient.publish(null, countryId, false, IntegrationOperation.GET, "/country/" + countryId + "/employment_type_and_expertise", null), PriorityGroupDefaultData.class);
    }

    public PriorityGroupDefaultData getExpertiseAndEmploymentForUnit(Long unitId) {
        return ObjectMapperUtils.copyPropertiesByMapper(genericRestClient.publish(null, unitId, true, IntegrationOperation.GET, "/employment_type_and_expertise", null), PriorityGroupDefaultData.class);
    }

    public List<StaffUnitPositionDetails> getStaffsUnitPosition(Long unitId, List<Long> staffIds, Long expertiseId) {
        List<StaffUnitPositionDetails> staffData = ObjectMapperUtils.copyPropertiesOfListByMapper(genericRestClient.publish(staffIds, unitId, true, IntegrationOperation.CREATE, "/expertise/{expertiseId}/unitPositions", null, expertiseId),StaffUnitPositionDetails.class);
        return  staffData;
    }

    public List<String> getEmailsOfStaffByStaffIds(Long unitId,List<Long> staffIds) {
        return genericRestClient.publish(staffIds, unitId, true, IntegrationOperation.CREATE, "/staff/emails", null);
    }

    public List<StaffUnitPositionDetails> getStaffIdAndUnitPositionId(Long unitId, List<Long> staffIds, Long expertiseId) {
        List<StaffUnitPositionDetails> staffData = ObjectMapperUtils.copyPropertiesOfListByMapper(genericRestClient.publish(staffIds, unitId, true, IntegrationOperation.CREATE, "/expertise/{expertiseId}/staff_and_unit_positions", null, expertiseId),StaffUnitPositionDetails.class);
        return  staffData;
    }

    public UserAccessRoleDTO getAccessRolesOfStaff(Long unitId) {
        return ObjectMapperUtils.copyPropertiesByMapper(genericRestClient.publish(null, unitId, true, IntegrationOperation.GET, "/staff/access_roles", null),UserAccessRoleDTO.class);
    }

    public DayTypeEmploymentTypeWrapper getDayTypesAndEmploymentTypes(Long countryId) {
        return ObjectMapperUtils.copyPropertiesByMapper(genericRestClient.publish(null, countryId, false, IntegrationOperation.GET, "/country/" + countryId +"/day_types_and_employment_types", null),DayTypeEmploymentTypeWrapper.class);
    }

    public DayTypeEmploymentTypeWrapper getDayTypesAndEmploymentTypesAtUnit(Long unitId) {
        return ObjectMapperUtils.copyPropertiesByMapper(genericRestClient.publish(null, unitId, true, IntegrationOperation.GET, "/day_types_and_employment_types", null),DayTypeEmploymentTypeWrapper.class);
    }
}