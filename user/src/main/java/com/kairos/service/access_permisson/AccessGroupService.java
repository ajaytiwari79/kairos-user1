package com.kairos.service.access_permisson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.commons.custom_exception.ActionNotPermittedException;
import com.kairos.commons.custom_exception.DataNotFoundByIdException;
import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.commons.utils.TranslationUtil;
import com.kairos.dto.user.access_group.CountryAccessGroupDTO;
import com.kairos.dto.user.access_group.UserAccessRoleDTO;
import com.kairos.dto.user.access_permission.AccessGroupRole;
import com.kairos.dto.user.access_permission.AccessPermissionDTO;
import com.kairos.dto.user.access_permission.StaffAccessGroupDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.AccessGroupDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.CountryHolidayCalenderDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.DayTypeDTO;
import com.kairos.dto.user.organization.OrganizationCategoryDTO;
import com.kairos.dto.user.reason_code.ReasonCodeWrapper;
import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.Day;
import com.kairos.enums.OrganizationCategory;
import com.kairos.enums.user.UserType;
import com.kairos.persistence.model.access_permission.*;
import com.kairos.persistence.model.access_permission.query_result.AccessGroupDayTypesQueryResult;
import com.kairos.persistence.model.access_permission.query_result.AccessGroupStaffQueryResult;
import com.kairos.persistence.model.auth.User;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.country.Country;
import com.kairos.persistence.model.country.CountryAccessGroupRelationship;
import com.kairos.persistence.model.country.default_data.account_type.AccountType;
import com.kairos.persistence.model.country.default_data.account_type.AccountTypeAccessGroupCountQueryResult;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.organization.OrganizationBaseEntity;
import com.kairos.persistence.model.organization.Unit;
import com.kairos.persistence.model.staff.personal_details.Staff;
import com.kairos.persistence.model.user.access_permission.AccessGroupsByCategoryDTO;
import com.kairos.persistence.model.user.counter.StaffIdsQueryResult;
import com.kairos.persistence.repository.organization.OrganizationGraphRepository;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import com.kairos.persistence.repository.user.access_permission.AccessGroupRepository;
import com.kairos.persistence.repository.user.access_permission.AccessPageRepository;
import com.kairos.persistence.repository.user.access_permission.AccessPermissionGraphRepository;
import com.kairos.persistence.repository.user.auth.UserGraphRepository;
import com.kairos.persistence.repository.user.country.CountryAccessGroupRelationshipRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.country.default_data.AccountTypeGraphRepository;
import com.kairos.persistence.repository.user.staff.StaffGraphRepository;
import com.kairos.service.country.CountryService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.integration.ActivityIntegrationService;
import com.kairos.service.kpermissions.PermissionService;
import com.kairos.service.organization.OrganizationService;
import com.kairos.service.staff.StaffRetrievalService;
import com.kairos.service.tree_structure.TreeStructureService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.asDate;
import static com.kairos.commons.utils.DateUtils.asLocalDate;
import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.AppConstants.SUPER_ADMIN;
import static com.kairos.constants.UserMessagesConstants.*;
import static com.kairos.enums.OrganizationCategory.HUB;


/**
 * Created by prabjot on 9/19/16.
 */
@Transactional
@Service
public class AccessGroupService {
    public static final String TRANSLATED_NAMES = "translatedNames";
    public static final String TRANSLATED_DESCRIPTIONS = "translatedDescriptions";
    @Inject
    private AccessGroupRepository accessGroupRepository;
    @Inject
    private UnitGraphRepository unitGraphRepository;
    @Inject
    private OrganizationGraphRepository organizationGraphRepository;
    @Inject
    private CountryService countryService;
    @Inject
    private AccessPageRepository accessPageRepository;
    @Inject
    private AccessPermissionGraphRepository accessPermissionGraphRepository;
    @Inject
    private TreeStructureService treeStructureService;
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private CountryAccessGroupRelationshipRepository countryAccessGroupRelationshipRepository;
    @Inject
    private OrganizationService organizationService;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private AccountTypeGraphRepository accountTypeGraphRepository;
    @Inject
    private StaffGraphRepository staffGraphRepository;
    @Inject
    private StaffRetrievalService staffRetrievalService;
    @Inject private UserGraphRepository userGraphRepository;
    @Inject private ActivityIntegrationService activityIntegrationService;
    @Inject private PermissionService permissionService;

    public AccessGroupDTO createAccessGroup(Long organizationId, AccessGroupDTO accessGroupDTO) {
        validateDayTypes(accessGroupDTO.isAllowedDayTypes(), accessGroupDTO.getDayTypeIds());
        if (accessGroupDTO.getEndDate() != null && accessGroupDTO.getEndDate().isBefore(accessGroupDTO.getStartDate())) {
            exceptionService.actionNotPermittedException(START_DATE_LESS_FROM_END_DATE);
        }
        Boolean isAccessGroupExistWithSameName = accessGroupRepository.isOrganizationAccessGroupExistWithName(organizationId, accessGroupDTO.getName().trim());
        if (isAccessGroupExistWithSameName) {
            exceptionService.duplicateDataException(MESSAGE_DUPLICATE, ACCESS_GROUP, accessGroupDTO.getName());
        }
        Organization organization = organizationGraphRepository.findById(organizationId).orElseThrow(() -> new ActionNotPermittedException(exceptionService.convertMessage(MESSAGE_PERMITTED, ACCESS_GROUP)));
        if (organization.isKairosHub() && AccessGroupRole.STAFF.equals(accessGroupDTO.getRole())) {
            exceptionService.duplicateDataException("error.org.access.management.notnull");
        }
        AccessGroup accessGroup = ObjectMapperUtils.copyPropertiesByMapper(accessGroupDTO, AccessGroup.class);
        accessGroup.setDayTypeIds(accessGroupDTO.getDayTypeIds());

        organization.getAccessGroups().add(accessGroup);
        organization.getUnits().forEach(unit->unit.getAccessGroups().add(accessGroup));
        organizationGraphRepository.save(organization, 2);

        //set default permission of access page while creating access group
        Long countryId = organization.getCountry().getId();
        setAccessPageRelationshipWithAccessGroupByOrgCategory(countryId, accessGroup.getId(), organization.getOrganizationCategory());
        accessGroupDTO.setId(accessGroup.getId());
        return accessGroupDTO;
    }

    public AccessGroupDTO updateAccessGroup(Long accessGroupId, Long unitId, AccessGroupDTO accessGroupDTO) {
        validateDayTypes(accessGroupDTO.isAllowedDayTypes(), accessGroupDTO.getDayTypeIds());
        if (accessGroupDTO.getEndDate() != null && accessGroupDTO.getEndDate().isBefore(accessGroupDTO.getStartDate())) {
            exceptionService.actionNotPermittedException(START_DATE_LESS_FROM_END_DATE);
        }
        AccessGroup accessGrpToUpdate = accessGroupRepository.findOne(accessGroupId);
        if (!Optional.ofNullable(accessGrpToUpdate).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_ACESSGROUPID_INCORRECT, accessGroupId);
        }
        if (accessGroupRepository.isOrganizationAccessGroupExistWithNameExceptId(unitId, accessGroupDTO.getName(), accessGroupId)) {
            exceptionService.duplicateDataException(MESSAGE_DUPLICATE, ACCESS_GROUP, accessGroupDTO.getName());
        }
        accessGrpToUpdate.setName(accessGroupDTO.getName());
        accessGrpToUpdate.setRole(accessGroupDTO.getRole());
        accessGrpToUpdate.setDescription(accessGroupDTO.getDescription());
        accessGrpToUpdate.setEnabled(accessGroupDTO.isEnabled());
        accessGrpToUpdate.setStartDate(accessGroupDTO.getStartDate());
        accessGrpToUpdate.setEndDate(accessGroupDTO.getEndDate());
        accessGrpToUpdate.setDayTypeIds(accessGroupDTO.getDayTypeIds());
        accessGrpToUpdate.setAllowedDayTypes(accessGroupDTO.isAllowedDayTypes());
        accessGroupRepository.save(accessGrpToUpdate);
        accessGroupDTO.setId(accessGrpToUpdate.getId());
        resetPermissionByAccessGroupIds(unitId,newArrayList(accessGroupId));
        return accessGroupDTO;
    }


    public boolean deleteAccessGroup(Long accessGroupId) {
        AccessGroup objectToDelete = accessGroupRepository.findOne(accessGroupId);
        if (objectToDelete == null) {
            return false;
        }
        objectToDelete.setDeleted(true);
        accessGroupRepository.save(objectToDelete);
        return true;
    }

    /**
     * @param organization
     * @author prabjot
     * this method will find the root organization, if root node exist then will return access group of root node
     * otherwise new access group will be created for organization
     */
    public Map<Long, Long> createDefaultAccessGroups(Organization organization) {
        Organization parent = organizationService.fetchParentOrganization(organization.getId());
        Map<Long, Long> countryAndOrgAccessGroupIdsMap = new HashMap<>();
        Long countryId = countryService.getCountryIdByUnitId(organization.getId());
        List<AccessGroup> accessGroupList = null;
        if (parent == null) {
            List<AccessGroupQueryResult> countryAccessGroups = accessGroupRepository.getCountryAccessGroupByCategory(countryId, organization.getOrganizationCategory().toString());
            accessGroupList = new ArrayList<>(countryAccessGroups.size());
            for (AccessGroupQueryResult countryAccessGroup : countryAccessGroups) {
                AccessGroup accessGroup = new AccessGroup(countryAccessGroup.getName(), countryAccessGroup.getDescription(), countryAccessGroup.getRole(), countryAccessGroup.getDayTypeIds(), countryAccessGroup.getStartDate(), countryAccessGroup.getEndDate());
                accessGroupRepository.save(accessGroup);
                countryAndOrgAccessGroupIdsMap.put(countryAccessGroup.getId(), accessGroup.getId());
                accessGroupRepository.setAccessPagePermissionForAccessGroup(countryAccessGroup.getId(), accessGroup.getId());
                accessGroupList.add(accessGroup);
            }

            organization.setAccessGroups(accessGroupList);
        } else {
            // Remove AG_COUNTRY_ADMIN access group to be copied
            List<AccessGroup> accessGroups = new ArrayList<>(parent.getAccessGroups());
            for (AccessGroup accessGroup : accessGroups) {
                if (accessGroup.getName().equals(SUPER_ADMIN)) {
                    accessGroups.remove(accessGroup);
                }
            }
            organization.setAccessGroups(accessGroups);
        }
        organizationGraphRepository.save(organization);
        return countryAndOrgAccessGroupIdsMap;
    }

    public void removeDefaultCopiedAccessGroup(List<Long> organizationIds) {
        accessGroupRepository.removeDefaultCopiedAccessGroup(organizationIds);
    }

    public void createDefaultAccessGroups(Organization parentOrg, List<Unit> units) {
        List<AccessGroupQueryResult> accessGroupList = accountTypeGraphRepository.getAccessGroupsByAccountTypeId(parentOrg.getAccountType().getId());
        createDefaultAccessGroupsInOrganization(parentOrg, accessGroupList, true);
        units.forEach(org -> {
            createDefaultAccessGroupsInOrganization(org, accessGroupList, false);
        });
    }

    /**
     * @param organization,accountTypeId
     * @author vipul
     * this method will create accessgroup to the organization
     * @Extra Need to optimize
     */
    public <T extends OrganizationBaseEntity> Map<Long, Long> createDefaultAccessGroupsInOrganization(T organization, List<AccessGroupQueryResult> accessGroupList, boolean company) {
        Map<Long, Long> countryAndOrgAccessGroupIdsMap = new LinkedHashMap<>();
        List<AccessGroup> newAccessGroupList = new ArrayList<>(accessGroupList.size());
        for (AccessGroupQueryResult currentAccessGroup : accessGroupList) {
            AccessGroup parent = new AccessGroup(currentAccessGroup.getName(), currentAccessGroup.getDescription(), currentAccessGroup.getRole(), currentAccessGroup.getDayTypeIds(), company ? DateUtils.getCurrentLocalDate() : currentAccessGroup.getStartDate(), currentAccessGroup.getEndDate());
            parent.setId(currentAccessGroup.getId());
            AccessGroup accessGroup = new AccessGroup(currentAccessGroup.getName(), currentAccessGroup.getDescription(), currentAccessGroup.getRole(), currentAccessGroup.getDayTypeIds(), company ? DateUtils.getCurrentLocalDate() : currentAccessGroup.getStartDate(), currentAccessGroup.getEndDate());
            accessGroup.setParentAccessGroup(parent);
            accessGroup.setLastModificationDate(accessGroup.getCreationDate());
            countryAndOrgAccessGroupIdsMap.put(currentAccessGroup.getId(), null);
            newAccessGroupList.add(accessGroup);
        }
        accessGroupRepository.saveAll(newAccessGroupList);
        AtomicInteger counter = new AtomicInteger(0);
        countryAndOrgAccessGroupIdsMap.forEach((k, v) -> {
            countryAndOrgAccessGroupIdsMap.put(k, newAccessGroupList.get(counter.get()).getId());
            // TODO PAVAN vipul remove this looped and use below when parent id is set to acccess group
            accessGroupRepository.setAccessPagePermissionForAccessGroup(k, newAccessGroupList.get(counter.get()).getId());
            // increment counter
            counter.addAndGet(1);
        });
        if (company) {
            organization.setAccessGroups(newAccessGroupList);
            organizationGraphRepository.save(((Organization) organization));
        } else {
            organization.setAccessGroups(newAccessGroupList);
            unitGraphRepository.save((Unit) organization);
        }
        return countryAndOrgAccessGroupIdsMap;
    }

    public List<AccessGroupQueryResult> getAccessGroupsForUnit(Long organizationId) {
        Organization organization = organizationService.fetchParentOrganization(organizationId);
        return accessGroupRepository.getAccessGroupsForUnitWithLinkUnitAndStaffCount(organization.getId());
    }

    public List<AccessGroup> getAccessGroups(Long organizationId) {
        Organization organization=organizationService.fetchParentOrganization(organizationId);
        return accessGroupRepository.getAccessGroups(organization.getId());
    }


    public boolean assignAccessGroupToStaff(List<String> accessGroupIds, Long staffId,Long unitId) {
        List<Long> accessGroupLongValue = new ArrayList<Long>(accessGroupIds.size());
        for (String accessGroupId : accessGroupIds) {
            accessGroupLongValue.add(Long.valueOf(accessGroupId));
        }
        Staff staff = accessGroupRepository.assignGroupToStaff(staffId, accessGroupLongValue);
        resetPermissionByAccessGroupIds(unitId,accessGroupLongValue);
        return staff != null;
    }

    //@Async
    public boolean resetPermissionByAccessGroupIds(Long unitId, Collection<Long> accessGroupIds){
        List<Long> userIds = accessGroupRepository.getUserIdsByAccessGroupId(accessGroupIds);
        for (Long userId : userIds) {
            permissionService.resetPerMissionByUserId(unitId,userId);
        }
        return true;
    }

    public AccessPage createAccessPage(String name, List<Map<String, Object>> childPage, boolean isModule) {
        AccessPage parentAccessPage = new AccessPage(name);
        List<AccessPage> accessPageList = new ArrayList<>();
        for (Map<String, Object> childAccessPage : childPage) {
            AccessPage accessPage = new AccessPage((String) childAccessPage.get("name"));
            accessPageRepository.save(accessPage);
            accessPageList.add(accessPage);
        }
        parentAccessPage.setModule(isModule);
        parentAccessPage.setSubPages(accessPageList);
        accessPageRepository.save(parentAccessPage);
        return parentAccessPage;
    }

    public List<AccessPage> getAccessModulesForUnits(Long parentOrganizationId, Long userId) {
        return accessPageRepository.getAccessModulesForUnits(parentOrganizationId, userId);
    }

    public List<AccessPageQueryResult> getAccessPageHierarchy(Long accessGroupId, Long countryId) {
        // Check if access group is of country
        if (Optional.ofNullable(countryId).isPresent()) {
            AccessGroup accessGroup = accessGroupRepository.findCountryAccessGroupById(accessGroupId, countryId);
            if (Optional.ofNullable(accessGroup).isPresent()) {
                exceptionService.dataNotFoundByIdException(MESSAGE_ACESSGROUPID_INCORRECT, accessGroupId);

            }
        }

        List<Map<String, Object>> accessPages = accessPageRepository.getSelectedAccessPageHierarchy(accessGroupId);
        ObjectMapper objectMapper = new ObjectMapper();
        List<AccessPageQueryResult> queryResults = new ArrayList<>();
        for (Map<String, Object> accessPage : accessPages) {
            Map<String, Object> data = ObjectMapperUtils.copyPropertiesByMapper(accessPage.get("data"), HashedMap.class);
            TranslationUtil.convertTranslationFromStringToMap(data);
            AccessPageQueryResult accessPageQueryResult = objectMapper.convertValue(data, AccessPageQueryResult.class);
            queryResults.add(accessPageQueryResult);
        }
        List<AccessPageQueryResult> treeData = getAccessPageHierarchy(queryResults, queryResults);
        List<AccessPageQueryResult> modules = new ArrayList<>();
        for (AccessPageQueryResult accessPageQueryResult : treeData) {
            if (accessPageQueryResult.isModule()) {
                modules.add(accessPageQueryResult);
            }
        }
        return modules;
    }

    public List<AccessPageQueryResult> getAccessPageByAccessGroup(Long accessGroupId, Long unitId, Long staffId) {
        Organization organization = organizationService.fetchParentOrganization(unitId);
        List<Map<String, Object>> accessPages = accessPageRepository.getAccessPagePermissionOfStaff(organization.getId(), unitId, staffId, accessGroupId);

        ObjectMapper objectMapper = new ObjectMapper();
        List<AccessPageQueryResult> queryResults = new ArrayList<>();
        for (Map<String, Object> accessPage : accessPages) {
            AccessPageQueryResult accessPageQueryResult = objectMapper.convertValue((Map<String, Object>) accessPage.get("data"), AccessPageQueryResult.class);
            queryResults.add(accessPageQueryResult);
        }
        List<AccessPageQueryResult> treeData = getAccessPageHierarchy(queryResults, queryResults);

        List<AccessPageQueryResult> modules = new ArrayList<>();
        for (AccessPageQueryResult accessPageQueryResult : treeData) {
            if (accessPageQueryResult.isModule()) {
                modules.add(accessPageQueryResult);
            }
        }
        return modules;
    }

    public Boolean setAccessPagePermissions(Long accessGroupId, List<Long> accessPageIds, boolean isSelected, Long countryId) {
        // Check if access group is of country
        if (Optional.ofNullable(countryId).isPresent()) {
            AccessGroup accessGroup = accessGroupRepository.findCountryAccessGroupById(accessGroupId, countryId);
            if (Optional.ofNullable(accessGroup).isPresent()) {
                exceptionService.dataNotFoundByIdException(MESSAGE_ACESSGROUPID_INCORRECT, accessGroupId);
            }
        }
        long creationDate = DateUtils.getDate().getTime();
        long lastModificationDate = DateUtils.getDate().getTime();
        Boolean read = isSelected;
        Boolean write = isSelected;
        accessGroupRepository.updateAccessPagePermission(accessGroupId, accessPageIds, isSelected, creationDate, lastModificationDate, read, write);
        // Update read/write permission of successive parent tabs
        updateReadWritePermissionOfSuccessiveParentTabForAccessGroup(accessGroupId, accessPageIds.get(0));
        // Remove customized permission for accessPageIds of accessGroupId
        accessPageRepository.removeCustomPermissionsForAccessGroup(accessGroupId, accessPageIds);
        return true;
    }

    public Map<String, Object> setPagePermissionToUser(Long staffId, Long unitId, Long accessGroupId, Long tabId, boolean read, boolean write,Long userId) {
        resetPermissionByAccessGroupIds(unitId,newArrayList(accessGroupId));
        return accessPermissionGraphRepository.setPagePermissionToUser(unitId, staffId, accessGroupId, tabId, read, write);

    }

    public List<AccessPageQueryResult> getAccessPageHierarchy(List<AccessPageQueryResult> allResults, List<AccessPageQueryResult> accessPageQueryResults) {
        for (AccessPageQueryResult accessPageQueryResult : accessPageQueryResults) {
            accessPageQueryResult.setChildren(getChilds(allResults, accessPageQueryResult));
            getAccessPageHierarchy(allResults, accessPageQueryResult.getChildren());
        }
        return allResults;
    }

    private List<AccessPageQueryResult> getChilds(List<AccessPageQueryResult> accessPageQueryResults, AccessPageQueryResult accessPageQueryResult) {


        AccessPageQueryResult result = null;
        for (AccessPageQueryResult accessPageQueryResult1 : accessPageQueryResults) {
            if (accessPageQueryResult1.getId() == accessPageQueryResult.getId() && accessPageQueryResult1.isWrite()) {
                result = accessPageQueryResult1;
                break;
            } else if (accessPageQueryResult1.getId() == accessPageQueryResult.getId()) {
                result = accessPageQueryResult1;
            }

        }

        if (result == null) {
            return new ArrayList<>();
        }
        return result.getChildren();
    }

    public List<Map<String, Object>> getAccessPermissions(Long staffId) {

        return accessGroupRepository.getAccessPermissions(staffId);
    }

    public void updateReadWritePermissionOfSuccessiveParentTabForAccessGroup(Long tabId, Long accessGroupId) {
        // fetch parentTab Id
        Long parentTabId = accessPageRepository.getParentAccessPageIdForAccessGroup(tabId);

        if (!Optional.ofNullable(parentTabId).isPresent()) {
            return;
        }

        // Check read/write permission of child tabs and set parent tab permission accordingly
        List<AccessPageQueryResult> accesPageList = accessPageRepository.getChildAccessPagePermissionsForAccessGroup(accessGroupId, parentTabId);

        boolean parentTabRead = false, parentTabWrite = false;

        // Predicate to check if any of tab has read and write access
        parentTabRead = accesPageList.stream().anyMatch(accessPage -> accessPage.isRead());
        parentTabWrite = accesPageList.stream().anyMatch(accessPage -> accessPage.isWrite());

        accessPageRepository.updateAccessPagePermissionsForAccessGroup(accessGroupId, parentTabId, parentTabRead, parentTabWrite);
        updateReadWritePermissionOfSuccessiveParentTabForAccessGroup(parentTabId, accessGroupId);
    }


    public void updateReadWritePermissionOfParentTab(Long accessGroupId, Boolean read, Boolean write, Long tabId, Long orgId, Long unitId, Long staffId) {

        AccessPageQueryResult readAndWritePermissionForAccessGroup = accessPageRepository.getAccessPermissionForAccessPage(accessGroupId, tabId);

        List<AccessPageQueryResult> accesPageList = accessPageRepository.getChildTabsAccessPermissionsByStaffAndOrg(orgId, unitId, staffId, tabId, accessGroupId);

        boolean parentTabRead = false, parentTabWrite = false;

        // Predicate to check if any of tab has read and write access
        parentTabRead = accesPageList.stream().anyMatch(accessPage -> accessPage.isRead());
        parentTabWrite = accesPageList.stream().anyMatch(accessPage -> accessPage.isWrite());

        // Check if new permissions are different then of Access Group
        if (Optional.ofNullable(readAndWritePermissionForAccessGroup).isPresent() &&
                readAndWritePermissionForAccessGroup.isRead() == parentTabRead && readAndWritePermissionForAccessGroup.isWrite() == parentTabWrite) {
            // CHECK if custom permission exist and then delete
            accessGroupRepository.deleteCustomPermissionForTab(orgId, staffId, unitId, accessGroupId, tabId);
        } else {
            accessGroupRepository.setCustomPermissionForTab(orgId, staffId, unitId, accessGroupId, tabId, read, write);
        }

        Long parentTabId = accessPageRepository.getParentTab(tabId);
        if (!Optional.ofNullable(parentTabId).isPresent()) {
            return;
        }
        updateReadWritePermissionOfParentTab(accessGroupId, parentTabRead, parentTabWrite, parentTabId, orgId, unitId, staffId);
    }

    public void assignPermission(Long accessGroupId, AccessPermissionDTO accessPermissionDTO) {
        Organization organization = organizationService.fetchParentOrganization(accessPermissionDTO.getUnitId());
        AccessPageQueryResult readAndWritePermissionForAccessGroup = accessPageRepository.getAccessPermissionForAccessPage(accessGroupId, accessPermissionDTO.getPageId());
        AccessPageQueryResult customReadAndWritePermissionForAccessGroup = accessPageRepository.getCustomPermissionOfTab(organization.getId(), accessPermissionDTO.getStaffId(), accessPermissionDTO.getUnitId(), accessPermissionDTO.getPageId(), accessGroupId);

        Boolean savedReadCheck = readAndWritePermissionForAccessGroup.isRead();
        Boolean savedWriteCheck = readAndWritePermissionForAccessGroup.isWrite();
        if (Optional.ofNullable(customReadAndWritePermissionForAccessGroup).isPresent()) {
            savedReadCheck = customReadAndWritePermissionForAccessGroup.isRead();
            savedWriteCheck = customReadAndWritePermissionForAccessGroup.isWrite();
        }
        Boolean write = accessPermissionDTO.isWrite();
        Boolean read = accessPermissionDTO.isRead();
        // If change has been done in read and if it is false then set write as false too
        if (savedReadCheck != read && !read) {
            write = false;
        }
        // If change has been done in write and if it is true then set read as true too
        else if (savedWriteCheck != write && write) {
            read = true;
        }
        // Check if new permissions are different then of Access Group
        if (readAndWritePermissionForAccessGroup.isRead() == read && readAndWritePermissionForAccessGroup.isWrite() == write) {
            accessGroupRepository.deleteCustomPermissionForChildren(organization.getId(), accessPermissionDTO.getStaffId(), accessPermissionDTO.getUnitId(), accessGroupId, accessPermissionDTO.getPageId());
        } else {
            accessGroupRepository.setCustomPermissionForChildren(organization.getId(), accessPermissionDTO.getStaffId(), accessPermissionDTO.getUnitId(), accessGroupId, accessPermissionDTO.getPageId(), read, write);
        }
        Long parentTabId = accessPageRepository.getParentTab(accessPermissionDTO.getPageId());
        if (Optional.ofNullable(parentTabId).isPresent()) {
            updateReadWritePermissionOfParentTab(accessGroupId, read, write, accessPermissionDTO.getPageId(), organization.getId(), accessPermissionDTO.getUnitId(), accessPermissionDTO.getStaffId());
        }
        resetPermissionByAccessGroupIds(accessPermissionDTO.getUnitId(),newArrayList(accessGroupId));
    }

    public Boolean updatePermissionsForAccessTabsOfAccessGroup(Long accessGroupId, Long accessPageId, AccessPermissionDTO accessPermissionDTO, Boolean updateChildren,Long unitId) {
        AccessPageQueryResult readAndWritePermissionOfAccessPage = accessPageRepository.getAccessPermissionForAccessPage(accessGroupId, accessPageId);
        List<Long> organizationAccessGroupIds = accessGroupRepository.getOrganizationAccessGroupIdsList(accessGroupId);
        Boolean write = accessPermissionDTO.isWrite();
        Boolean read = accessPermissionDTO.isRead();
        // If change has been done in read and if it is false then set write as false too
        if (readAndWritePermissionOfAccessPage.isRead() != read && !read) {
            write = false;
        }
        // If change has been done in write and if it is true then set read as true too
        else if (readAndWritePermissionOfAccessPage.isWrite() != write && write) {
            read = true;
        }
        if(updateChildren && isCollectionNotEmpty(organizationAccessGroupIds)){
            for(Long organizationAccessGroupId :organizationAccessGroupIds){
                accessGroupRepository.updatePermissionsForAccessTabsAndChildrenOfAccessGroup(accessPageId, organizationAccessGroupId, read, write);
            }
        }
        resetPermissionByAccessGroupIds(unitId,newArrayList(accessGroupId));
        if (updateChildren) {
            // Update read/write permission of tab and its children
            return accessGroupRepository.updatePermissionsForAccessTabsAndChildrenOfAccessGroup(accessPageId, accessGroupId, read, write);
        } else {
            // Update read/write permission of tab itself
            return accessGroupRepository.updatePermissionsForAccessTabOfAccessGroup(accessPageId, accessGroupId, accessPermissionDTO.isRead(), accessPermissionDTO.isWrite());
        }
    }

    /***** Access group - COUNTRY LEVEL - STARTS HERE ******************/
    private void setAccessPageRelationshipWithAccessGroupByOrgCategory(Long countryId, Long accessGroupId, OrganizationCategory organizationCategory) {
        switch (organizationCategory) {
            case HUB:
                accessGroupRepository.setAccessPageForHubAccessGroup(countryId, accessGroupId);
                break;
            case ORGANIZATION:
                accessGroupRepository.setAccessPageForOrganizationAccessGroup(countryId, accessGroupId);
                break;
            case UNION:
                accessGroupRepository.setAccessPageForUnionAccessGroup(countryId, accessGroupId);
                break;
            default:
                break;
        }
    }


    public CountryAccessGroupDTO createCountryAccessGroup(Long countryId, CountryAccessGroupDTO accessGroupDTO) {
        validateDetails(countryId, accessGroupDTO);
        List<AccountType> accountType = accountTypeGraphRepository.getAllAccountTypeByIds(accessGroupDTO.getAccountTypeIds());
        if (accountType.size() != accessGroupDTO.getAccountTypeIds().size()) {
            exceptionService.dataNotMatchedException(MESSAGE_ACCOUNTTYPE_NOTFOUND);
        }
        Country country = countryGraphRepository.findOne(countryId);
        AccessGroup accessGroup = OrganizationCategory.ORGANIZATION.equals(accessGroupDTO.getOrganizationCategory()) ? new AccessGroup(accessGroupDTO.getName().trim(), accessGroupDTO.getDescription(), accessGroupDTO.getRole(), accountType, accessGroupDTO.getDayTypeIds(), accessGroupDTO.getStartDate(), accessGroupDTO.getEndDate()) : new AccessGroup(accessGroupDTO.getName().trim(), accessGroupDTO.getDescription(), accessGroupDTO.getRole(), accessGroupDTO.getDayTypeIds(), accessGroupDTO.getStartDate(), accessGroupDTO.getEndDate());
        CountryAccessGroupRelationship accessGroupRelationship = new CountryAccessGroupRelationship(country, accessGroup, accessGroupDTO.getOrganizationCategory());
        countryAccessGroupRelationshipRepository.save(accessGroupRelationship);
        countryGraphRepository.save(country);
        //set default permission of access page while creating access group
        setAccessPageRelationshipWithAccessGroupByOrgCategory(countryId, accessGroup.getId(), accessGroupDTO.getOrganizationCategory());
        accessGroupDTO.setId(accessGroup.getId());
        accessGroupDTO.setEnabled(true);
        return accessGroupDTO;
    }

    private void validateDetails(Long countryId, CountryAccessGroupDTO accessGroupDTO) {
        if (HUB.equals(accessGroupDTO.getOrganizationCategory()) && AccessGroupRole.STAFF.equals(accessGroupDTO.getRole())) {
            exceptionService.duplicateDataException("error.org.access.management.notnull");
        }
        if ((accessGroupDTO.isAllowedDayTypes() && CollectionUtils.isEmpty(accessGroupDTO.getDayTypeIds()))) {
            exceptionService.actionNotPermittedException(ERROR_DAY_TYPE_ABSENT);
        } else if ((!accessGroupDTO.isAllowedDayTypes() && CollectionUtils.isNotEmpty(accessGroupDTO.getDayTypeIds()))) {
            exceptionService.actionNotPermittedException(ERROR_ALLOWED_DAY_TYPE_ABSENT);
        }
        if (accessGroupDTO.getEndDate() != null && accessGroupDTO.getEndDate().isBefore(accessGroupDTO.getStartDate())) {
            exceptionService.actionNotPermittedException(START_DATE_LESS_FROM_END_DATE);
        }
        if (OrganizationCategory.ORGANIZATION.equals(accessGroupDTO.getOrganizationCategory()) && accessGroupDTO.getAccountTypeIds().isEmpty()) {
            exceptionService.actionNotPermittedException(MESSAGE_ACCOUNTTYPE_SELECT);
        }
        boolean isAccessGroupExistWithSameName;
        if ("Organization".equals(accessGroupDTO.getOrganizationCategory().value)) {
            isAccessGroupExistWithSameName = accessGroupRepository.isCountryAccessGroupExistWithName(countryId, accessGroupDTO.getName(), accessGroupDTO.getOrganizationCategory().toString(), accessGroupDTO.getAccountTypeIds(),accessGroupDTO.getId()==null?-1L:accessGroupDTO.getId());

        } else {
            isAccessGroupExistWithSameName = accessGroupRepository.isCountryAccessGroupExistWithName(countryId, accessGroupDTO.getName(), accessGroupDTO.getOrganizationCategory().toString(),accessGroupDTO.getId()==null?-1L:accessGroupDTO.getId());
        }

        if (isAccessGroupExistWithSameName) {
            exceptionService.duplicateDataException(MESSAGE_DUPLICATE, ACCESS_GROUP, accessGroupDTO.getName());
        }
    }

    public AccessGroup updateCountryAccessGroup(Long countryId, Long accessGroupId, CountryAccessGroupDTO accessGroupDTO) {
        validateDetails(countryId,accessGroupDTO);
        AccessGroup accessGrpToUpdate = accessGroupRepository.findById(accessGroupId).orElseThrow(() -> new DataNotFoundByIdException(exceptionService.convertMessage(MESSAGE_ACESSGROUPID_INCORRECT, accessGroupId)));
        accessGrpToUpdate.setName(accessGroupDTO.getName());
        accessGrpToUpdate.setDescription(accessGroupDTO.getDescription());
        accessGrpToUpdate.setRole(accessGroupDTO.getRole());
        accessGrpToUpdate.setEnabled(accessGroupDTO.isEnabled());
        accessGrpToUpdate.setStartDate(accessGroupDTO.getStartDate());
        accessGrpToUpdate.setEndDate(accessGroupDTO.getEndDate());
        accessGrpToUpdate.setDayTypeIds(accessGroupDTO.getDayTypeIds());
        accessGrpToUpdate.setAllowedDayTypes(accessGroupDTO.isAllowedDayTypes());
        accessGroupRepository.save(accessGrpToUpdate);
        return accessGrpToUpdate;
    }

    public boolean deleteCountryAccessGroup(Long accessGroupId) {
        AccessGroup accessGroupToDelete = accessGroupRepository.findOne(accessGroupId);
        if (!Optional.ofNullable(accessGroupToDelete).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_ACESSGROUPID_INCORRECT, accessGroupId);

        }
        accessGroupToDelete.setDeleted(true);
        accessGroupRepository.save(accessGroupToDelete);
        return true;
    }

    public Map<String, Object> getListOfOrgCategoryWithCountryAccessGroupCount(Long countryId) {
        List<OrganizationCategoryDTO> organizationCategoryDTOS = OrganizationCategory.getListOfOrganizationCategory();
        AccessGroupCountQueryResult accessGroupCountData = accessGroupRepository.getListOfOrgCategoryWithCountryAccessGroupCount(countryId);
        List<AccountTypeAccessGroupCountQueryResult> accountTypes
                = accountTypeGraphRepository.getAllAccountTypeWithAccessGroupCountByCountryId(countryId);
        organizationCategoryDTOS.forEach(orgCategoryDTO -> {
            switch (OrganizationCategory.valueOf(orgCategoryDTO.getValue())) {
                case HUB: {
                    orgCategoryDTO.setCount(accessGroupCountData.getHubCount());
                    break;
                }
                case ORGANIZATION: {
                    orgCategoryDTO.setCount(accountTypes.size());
                    break;
                }
                case UNION: {
                    orgCategoryDTO.setCount(accessGroupCountData.getUnionCount());
                    break;
                }
                default:
                    break;
            }
        });

        Map<String, Object> response = new HashMap<>(2);
        response.put("accountTypes", accountTypes);
        response.put("category", organizationCategoryDTOS);
        return response;
    }

    /**
     * @param accountTypeId
     * @return
     * @author vipul
     * @Desc This api is used to fetch all access group by account type id in country.
     */
    public List<AccessGroupQueryResult> getCountryAccessGroupByAccountTypeId(Long countryId, Long accountTypeId, String accessGroupRole) {
        List<String> accessGroupRoles = isNotNull(accessGroupRole) ? Arrays.asList(accessGroupRole) : Arrays.asList(AccessGroupRole.MANAGEMENT.toString(), AccessGroupRole.STAFF.toString());
        return accessGroupRepository.getCountryAccessGroupByAccountTypeId(countryId, accountTypeId, accessGroupRoles);
    }

    public List<AccessGroupQueryResult> getCountryAccessGroups(Long countryId, OrganizationCategory organizationCategory) {

        return accessGroupRepository.getCountryAccessGroupByOrgCategory(countryId, organizationCategory.toString());
    }

    public List<AccessGroupsByCategoryDTO> getCountryAccessGroupsOfAllCategories(Long countryId) {

        List<AccessGroupsByCategoryDTO> accessGroupsData = new ArrayList<>();
        accessGroupsData.add(new AccessGroupsByCategoryDTO(HUB,
                accessGroupRepository.getCountryAccessGroupByOrgCategory(countryId, HUB.toString())));

        accessGroupsData.add(new AccessGroupsByCategoryDTO(OrganizationCategory.ORGANIZATION,
                accessGroupRepository.getCountryAccessGroupByOrgCategory(countryId, OrganizationCategory.ORGANIZATION.toString())));

        accessGroupsData.add(new AccessGroupsByCategoryDTO(OrganizationCategory.UNION,
                accessGroupRepository.getCountryAccessGroupByOrgCategory(countryId, OrganizationCategory.UNION.toString())));
        return accessGroupsData;
    }

    /***** Access group - COUNTRY LEVEL - ENDS HERE ******************/

    public AccessGroupDTO copyUnitAccessGroup(Long organizationId, AccessGroupDTO accessGroupDTO) {
        validateDayTypes(accessGroupDTO.isAllowedDayTypes(), accessGroupDTO.getDayTypeIds());
        if (accessGroupDTO.getEndDate() != null && accessGroupDTO.getEndDate().isBefore(accessGroupDTO.getStartDate())) {
            exceptionService.actionNotPermittedException(START_DATE_LESS_FROM_END_DATE);
        }
        Organization organization = organizationGraphRepository.findById(organizationId).orElseThrow(() -> new ActionNotPermittedException(exceptionService.convertMessage(MESSAGE_ACCESSGROUP_COPIED)));
        Boolean isAccessGroupExistWithSameName = accessGroupRepository.isOrganizationAccessGroupExistWithName(organizationId, accessGroupDTO.getName().trim());
        if (isAccessGroupExistWithSameName) {
            exceptionService.duplicateDataException(MESSAGE_DUPLICATE, ACCESS_GROUP, accessGroupDTO.getName().trim());
        }
        AccessGroupQueryResult currentAccessGroup = accessGroupRepository.findByAccessGroupId(organizationId, accessGroupDTO.getId());
        if (currentAccessGroup == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_ACESSGROUPID_INCORRECT, accessGroupDTO.getId());

        }
        AccessGroup accessGroup = new AccessGroup(accessGroupDTO.getName().trim(), accessGroupDTO.getDescription(), accessGroupDTO.getRole(), currentAccessGroup.getDayTypeIds(), currentAccessGroup.getStartDate(), currentAccessGroup.getEndDate());
        accessGroupRepository.save(accessGroup);
        organization.getAccessGroups().add(accessGroup);
        organization.getUnits().forEach(unit->unit.getAccessGroups().add(accessGroup));
        organizationGraphRepository.save(organization);
        accessPageRepository.copyAccessGroupPageRelationShips(accessGroupDTO.getId(), accessGroup.getId());
        accessGroupDTO.setId(accessGroup.getId());
        return accessGroupDTO;

    }

    public CountryAccessGroupDTO copyCountryAccessGroup(Long countryId, CountryAccessGroupDTO countryAccessGroupDTO) {
        validateDayTypes(countryAccessGroupDTO.isAllowedDayTypes(), countryAccessGroupDTO.getDayTypeIds());
        if (countryAccessGroupDTO.getEndDate() != null && countryAccessGroupDTO.getEndDate().isBefore(countryAccessGroupDTO.getStartDate())) {
            exceptionService.actionNotPermittedException(START_DATE_LESS_FROM_END_DATE);
        }
        Optional<Country> country = countryGraphRepository.findById(countryId);
        if (!country.isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, countryId);
        }
        Boolean isAccessGroupExistWithSameName = accessGroupRepository.isCountryAccessGroupExistWithName(countryId, countryAccessGroupDTO.getName().trim(), countryAccessGroupDTO.getOrganizationCategory().toString(),-1L);
        if (isAccessGroupExistWithSameName) {
            exceptionService.duplicateDataException(MESSAGE_DUPLICATE, ACCESS_GROUP, countryAccessGroupDTO.getName().trim());
        }
        Optional<AccessGroup> currentAccessGroup = accessGroupRepository.findById(countryAccessGroupDTO.getId());
        if (!currentAccessGroup.isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_ACESSGROUPID_INCORRECT, countryAccessGroupDTO.getId());

        }
        AccessGroup accessGroup = new AccessGroup(countryAccessGroupDTO.getName().trim(), countryAccessGroupDTO.getDescription(), currentAccessGroup.get().getRole(), currentAccessGroup.get().getAccountType(), currentAccessGroup.get().getDayTypeIds(), currentAccessGroup.get().getStartDate(), currentAccessGroup.get().getEndDate());

        CountryAccessGroupRelationship accessGroupRelationship = new CountryAccessGroupRelationship(country.get(), accessGroup, countryAccessGroupDTO.getOrganizationCategory());
        countryAccessGroupRelationshipRepository.save(accessGroupRelationship);
        countryGraphRepository.save(country.get());
        accessPageRepository.copyAccessGroupPageRelationShips(countryAccessGroupDTO.getId(), accessGroup.getId());
        countryAccessGroupDTO.setId(accessGroup.getId());
        return countryAccessGroupDTO;
    }

    // Method to fetch list of access group by Organization category ( Hub, Organization and Union)
    //TODO all three db calls can be combined in one
    public Map<String, List<AccessGroupQueryResult>> getCountryAccessGroupsForOrganizationCreation(Long countryId) {
        Map<String, List<AccessGroupQueryResult>> accessGroupForParentOrganizationCreation = new HashMap<>();
        accessGroupForParentOrganizationCreation.put("hub",
                accessGroupRepository.getCountryAccessGroupByOrgCategoryAndRole(countryId, HUB.toString(), AccessGroupRole.MANAGEMENT.toString()));
        accessGroupForParentOrganizationCreation.put("organization",
                accessGroupRepository.getCountryAccessGroupByOrgCategoryAndRole(countryId, OrganizationCategory.ORGANIZATION.toString(), AccessGroupRole.MANAGEMENT.toString()));
        accessGroupForParentOrganizationCreation.put("union",
                accessGroupRepository.getCountryAccessGroupByOrgCategoryAndRole(countryId, OrganizationCategory.UNION.toString(), AccessGroupRole.MANAGEMENT.toString()));
        return accessGroupForParentOrganizationCreation;
    }

    // Method to fetch list of Management access group of Organization
    public List<AccessGroupQueryResult> getOrganizationManagementAccessGroups(Long organizationId, AccessGroupRole role) {
        Organization organization = organizationService.fetchParentOrganization(organizationId);
        return accessGroupRepository.getOrganizationAccessGroupByRole(organization.getId(), role.toString());
    }


    public UserAccessRoleDTO findUserAccessRole(Long unitId) {
        Long userId = UserContext.getUserDetails().getId();
        Optional<User> userOptional = userGraphRepository.findById(userId);
        UserAccessRoleDTO userAccessRoleDTO = null;
        if(userOptional.isPresent() && UserType.SYSTEM_ACCOUNT.equals(userOptional.get().getUserType())){
            userAccessRoleDTO = new UserAccessRoleDTO(userId, unitId, true, true,newHashSet());
        }else {
            Organization parent = organizationService.fetchParentOrganization(unitId);
            Staff staffAtHub = staffGraphRepository.getStaffByOrganizationHub(parent.getId(), userId);
            if (staffAtHub != null) {
                userAccessRoleDTO = new UserAccessRoleDTO(userId, unitId, staffAtHub.getId(), false, true);
                Organization parentHub = accessPageRepository.fetchParentHub(userId);
                userAccessRoleDTO.setAccessGroupIds(parentHub.getAccessGroups().stream().map(UserBaseEntity::getId).collect(Collectors.toSet()));
            } else {
                userAccessRoleDTO = getStaffAccessRole(unitId, userId, userAccessRoleDTO, parent);
            }
        }
        return userAccessRoleDTO;
    }

    private UserAccessRoleDTO getStaffAccessRole(Long unitId, Long userId, UserAccessRoleDTO userAccessRoleDTO, Organization parent) {
        Staff staffAtHub;
        Long hubIdByOrganizationId = unitGraphRepository.getHubIdByOrganizationId(parent.getId());
        staffAtHub = staffGraphRepository.getStaffOfHubByHubIdAndUserId(parent.isKairosHub() ? parent.getId() : hubIdByOrganizationId, userId);
        if (staffAtHub != null) {
            userAccessRoleDTO = new UserAccessRoleDTO(userId, unitId, staffAtHub.getId(), false, true);
        } else if (isNull(userAccessRoleDTO)) {
            AccessGroupStaffQueryResult accessGroupQueryResult = accessGroupRepository.getAccessGroupDayTypesAndUserId(unitId, userId);
            if (isNull(accessGroupQueryResult)) {
                exceptionService.actionNotPermittedException(MESSAGE_STAFF_INVALID_UNIT);
            }
            accessGroupQueryResult = ObjectMapperUtils.copyPropertiesByMapper(accessGroupQueryResult, AccessGroupStaffQueryResult.class);
            String staffRole = staffRetrievalService.getStaffAccessRole(accessGroupQueryResult);
            boolean staff = AccessGroupRole.STAFF.name().equals(staffRole);
            boolean management = AccessGroupRole.MANAGEMENT.name().equals(staffRole);
            Set<Long> accessGroupIds = accessGroupQueryResult.getDayTypesByAccessGroup().stream().map(dayTypesByAccessGroup -> dayTypesByAccessGroup.getAccessGroup().getId()).collect(Collectors.toSet());
            userAccessRoleDTO = new UserAccessRoleDTO(userId, unitId, staff, management, accessGroupIds);
            userAccessRoleDTO.setStaffId(accessGroupQueryResult.getStaffId());
        }
        return userAccessRoleDTO;
    }

    public UserAccessRoleDTO findStaffAccessRole(Long unitId, Long staffId) {
        AccessGroupStaffQueryResult accessGroupQueryResult = accessGroupRepository.getAccessGroupDayTypesAndStaffId(unitId, staffId);
        if (accessGroupQueryResult == null) {
            exceptionService.actionNotPermittedException(MESSAGE_STAFF_INVALID_UNIT);
        }
        String staffRole = staffRetrievalService.getStaffAccessRole(accessGroupQueryResult);
        boolean staff = AccessGroupRole.STAFF.name().equals(staffRole);
        boolean management = AccessGroupRole.MANAGEMENT.name().equals(staffRole);
        return new UserAccessRoleDTO(unitId, staff, management, staffId);
    }

    public ReasonCodeWrapper getAbsenceReasonCodesAndAccessRole(Long unitId) {
        UserAccessRoleDTO userAccessRoleDTO = findUserAccessRole(unitId);
        return new ReasonCodeWrapper(userAccessRoleDTO);
    }


    public List<StaffIdsQueryResult> getStaffIdsByUnitIdAndAccessGroupId(Long unitId, List<Long> accessGroupId) {
        return accessGroupRepository.getStaffIdsByUnitIdAndAccessGroupId(unitId, accessGroupId);
    }


    public List<StaffAccessGroupDTO> getStaffAndAccessGroupsByUnitId(Long unitId, List<Long> accessGroupId) {
        return ObjectMapperUtils.copyCollectionPropertiesByMapper(accessGroupRepository.getStaffIdsAndAccessGroupsByUnitId(unitId, accessGroupId), StaffAccessGroupDTO.class);
    }

    public StaffAccessGroupQueryResult getAccessGroupIdsByStaffIdAndUnitId(Long unitId) {
        Long staffId = staffRetrievalService.getStaffIdOfLoggedInUser(unitId);
        return accessGroupRepository.getAccessGroupIdsByStaffIdAndUnitId(staffId, unitId);

    }

    public Map<Long, Long> getAccessGroupUsingParentId(Long unitId, Set<Long> accessGroupIds) {
        List<AccessPageQueryResult> accessPageQueryResults = accessGroupRepository.findAllAccessGroupWithParentIds(unitId, accessGroupIds);
        return convertToMap(accessPageQueryResults);
    }

    public Map<Long, Long> findAllAccessGroupWithParentOfOrganization(Long organizationId) {
        List<AccessPageQueryResult> accessPageQueryResults = accessGroupRepository.findAllAccessGroupWithParentOfOrganization(organizationId);
        return convertToMap(accessPageQueryResults);
    }

    private Map<Long, Long> convertToMap(List<AccessPageQueryResult> accessPageQueryResults) {
        Map<Long, Long> response = new HashMap<>();
        accessPageQueryResults.forEach(accessPageQueryResult -> {
            response.put(accessPageQueryResult.getParentId(), accessPageQueryResult.getId());
        });
        return response;
    }


    public void linkParentOrganizationAccessGroup(Unit unit, Long parentOrganizationId) {
        List<AccessGroupQueryResult> accessGroupQueryResults = getOrganizationAccessGroups(parentOrganizationId);
        List<AccessGroup> accessGroupList = ObjectMapperUtils.copyCollectionPropertiesByMapper(accessGroupQueryResults, AccessGroup.class);
        unit.setAccessGroups(accessGroupList);
        accessGroupRepository.saveAll(accessGroupList);

    }

    private List<AccessGroupQueryResult> getOrganizationAccessGroups(Long parentOrganizationId) {
        return accessGroupRepository.getAccessGroupsForUnit(parentOrganizationId);
    }

    public StaffAccessGroupQueryResult getAccessGroupWithDayTypesByStaffIdAndUnitId(Long unitId){
        Organization parent = organizationService.fetchParentOrganization(unitId);
        Staff staffAtHub = staffGraphRepository.getStaffByOrganizationHub(parent.getId(), UserContext.getUserDetails().getId());
        StaffAccessGroupQueryResult accessGroupStaffQueryResult = new StaffAccessGroupQueryResult();
        if(staffAtHub!=null){
            accessGroupStaffQueryResult.setCountryAdmin(true);
            return accessGroupStaffQueryResult;
        }
        Long staffId = staffRetrievalService.getStaffIdOfLoggedInUser(unitId);
        List<AccessGroup> accessGroups=accessGroupRepository.getAccessGroupWithDayTypesByStaffIdAndUnitId(staffId,unitId);
        accessGroupStaffQueryResult.setAccessGroups(accessGroups);
        return  accessGroupStaffQueryResult;
    }

    public List<AccessGroup> validAccessGroupByDate(Long unitId,Date date){
        List<AccessGroup> accessGroups = new ArrayList<>();
        if(!UserContext.getUserDetails().isSystemAdmin()){
            AccessGroupStaffQueryResult accessGroupStaffQueryResult = accessGroupRepository.getAccessGroupDayTypesAndUserId(unitId,UserContext.getUserDetails().getId());
            if(isNull(accessGroupStaffQueryResult)){
                exceptionService.dataNotFoundByIdException(ERROR_POSITION_ACCESSGROUP_NOTFOUND);
            }
            List<AccessGroupDayTypesQueryResult> accessGroupDayTypesQueryResults = ObjectMapperUtils.copyCollectionPropertiesByMapper(accessGroupStaffQueryResult.getDayTypesByAccessGroup(),AccessGroupDayTypesQueryResult.class);
            Map<Long,Set<BigInteger>> accessGroupAndDayTypeMap= getMapOfAccessGroupAndDayType(accessGroupDayTypesQueryResults);
            Set<BigInteger> dayTypesIds=new HashSet<>();
            accessGroupAndDayTypeMap.forEach((k,v)->dayTypesIds.addAll(v));
            List<DayTypeDTO> dayTypeDTOS=activityIntegrationService.getDayTypeByIds(dayTypesIds);
            for (AccessGroupDayTypesQueryResult accessGroupDayTypesQueryResult : accessGroupDayTypesQueryResults) {
                if(isNotNull(accessGroupDayTypesQueryResult.getAccessGroup())){
                    List<DayTypeDTO> dayTypeDTOList=dayTypeDTOS.stream().filter(k->accessGroupAndDayTypeMap.get(accessGroupDayTypesQueryResult.getAccessGroup().getId()).contains(k.getId())).collect(Collectors.toList());
                    if(!accessGroupDayTypesQueryResult.getAccessGroup().isAllowedDayTypes() || isDayTypeValid(date,dayTypeDTOList)){
                        accessGroups.add(accessGroupDayTypesQueryResult.getAccessGroup());
                    }
                }
            }
        }

        return accessGroups;
    }

    public Map<Long, Set<BigInteger>> getMapOfAccessGroupAndDayType(List<AccessGroupDayTypesQueryResult> accessGroupDayTypesQueryResults) {
        Map<Long,Set<BigInteger>> accessGroupAndDayTypeMap=new HashMap<>();
        accessGroupDayTypesQueryResults.forEach(k->accessGroupAndDayTypeMap.put(k.getAccessGroup().getId(),k.getAccessGroup().getDayTypeIds()));
        return accessGroupAndDayTypeMap;
    }

    public boolean isDayTypeValid(Date date, List<DayTypeDTO> dayTypeDTOs) {
        boolean valid = false;
        for (DayTypeDTO dayTypeDTO : dayTypeDTOs) {
            if (dayTypeDTO.isHolidayType()) {
                for (CountryHolidayCalenderDTO countryHolidayCalendarQueryResult : dayTypeDTO.getCountryHolidayCalenderData()) {
                    DateTimeInterval dateTimeInterval = getDateTimeInterval(dayTypeDTO, countryHolidayCalendarQueryResult);
                    valid = dateTimeInterval.contains(date);
                    if (valid) {
                        break;
                    }
                }
            } else {
                valid = isCollectionNotEmpty(dayTypeDTO.getValidDays()) && dayTypeDTO.getValidDays().contains(Day.fromValue(asLocalDate(date).getDayOfWeek().toString()));
            }
            if (valid) {
                break;
            }
        }
        return valid;
    }

    private DateTimeInterval getDateTimeInterval(DayTypeDTO dayTypeCountryHolidayCalenderQueryResult, CountryHolidayCalenderDTO countryHolidayCalendarQueryResult) {
        DateTimeInterval dateTimeInterval;
        if (dayTypeCountryHolidayCalenderQueryResult.isAllowTimeSettings()) {
            LocalTime holidayEndTime = countryHolidayCalendarQueryResult.getEndTime().get(ChronoField.MINUTE_OF_DAY) == 0 ? LocalTime.MAX : countryHolidayCalendarQueryResult.getEndTime();
            dateTimeInterval = new DateTimeInterval(asDate(countryHolidayCalendarQueryResult.getHolidayDate(), countryHolidayCalendarQueryResult.getStartTime()), asDate(countryHolidayCalendarQueryResult.getHolidayDate(), holidayEndTime));
        } else {
            dateTimeInterval = new DateTimeInterval(asDate(countryHolidayCalendarQueryResult.getHolidayDate()), asDate(countryHolidayCalendarQueryResult.getHolidayDate().plusDays(1)));
        }
        return dateTimeInterval;
    }

    private void validateDayTypes(boolean allowedDayTypes, Set<BigInteger> dayTypeIds) {
        if ((allowedDayTypes && CollectionUtils.isEmpty(dayTypeIds))) {
            exceptionService.actionNotPermittedException(ERROR_DAY_TYPE_ABSENT);
        } else if ((!allowedDayTypes && CollectionUtils.isNotEmpty(dayTypeIds))) {
            exceptionService.actionNotPermittedException(ERROR_ALLOWED_DAY_TYPE_ABSENT);
        }


    }
    public List<AccessGroupQueryResult> getCountryAccessGroupByOrgCategory(Long countryId, String orgCategory){
        return accessGroupRepository.getCountryAccessGroupByOrgCategory(countryId, orgCategory);
    }

    public Set<String> getAccessRoles(Set<Long> accessGroupIds){
        return accessGroupRepository.getAccessRolesByAccessGroupId(accessGroupIds);
    }

    public Set<Long> getAccessGroupIdsOfUnit(final Long unitId){
        return organizationService.fetchParentOrganization(unitId).getAccessGroups().stream().map(k->k.getId()).collect(Collectors.toSet());
    }

    public Map<String,List<Map>> getCountryAccessGroupLinkingDetails(Long accessGroupId){
        Map<String,List<Map>> accessGroupDetails = new HashMap<>();
        accessGroupDetails.put("expertiseDetails",accessGroupRepository.getCountryAccessGroupLinkingDetailsByExpertise(accessGroupId));
        accessGroupDetails.put("organizationDetails",accessGroupRepository.getCountryAccessGroupLinkingDetailsByOrganization(accessGroupId));
        accessGroupDetails.put("employmentTypeDetails",accessGroupRepository.getCountryAccessGroupLinkingDetailsByEmploymentType(accessGroupId));
        return accessGroupDetails;
    }

    public Map<String,List<Map>> getOrganizationAccessGroupLinkingDetails(Long accessGroupId){
        Map<String,List<Map>> accessGroupDetails = new HashMap<>();
        accessGroupDetails.put("expertiseDetails",accessGroupRepository.getOrganizationAccessGroupLinkingDetailsByExpertise(accessGroupId));
        accessGroupDetails.put("organizationDetails",accessGroupRepository.getOrganizationAccessGroupLinkingDetailsByOrganization(accessGroupId));
        accessGroupDetails.put("employmentTypeDetails",accessGroupRepository.getOrganizationAccessGroupLinkingDetailsByEmploymentType(accessGroupId));
        return accessGroupDetails;
    }
}
