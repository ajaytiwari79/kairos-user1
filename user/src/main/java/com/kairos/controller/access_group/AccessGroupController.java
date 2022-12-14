package com.kairos.controller.access_group;

import com.kairos.dto.TranslationInfo;
import com.kairos.dto.user.access_group.CountryAccessGroupDTO;
import com.kairos.dto.user.access_permission.AccessGroupPermissionDTO;
import com.kairos.dto.user.access_permission.AccessGroupRole;
import com.kairos.dto.user.access_permission.AccessPermissionDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.AccessGroupDTO;
import com.kairos.enums.OrganizationCategory;
import com.kairos.persistence.model.access_permission.AccessGroup;
import com.kairos.service.access_permisson.AccessGroupService;
import com.kairos.service.translation.TranslationService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.kairos.constants.ApiConstants.*;


/**
 * Created by prabjot on 7/11/16.
 */
@RestController
@RequestMapping(API_V1)
@Api(value = API_V1)
public class AccessGroupController {

    @Inject
    private AccessGroupService accessGroupService;

    @Inject private TranslationService translationService;


    @PostMapping(value = UNIT_URL+"/access_group")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> createAccessGroup(@PathVariable long unitId,@Valid @RequestBody AccessGroupDTO accessGroupDTO) {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true, accessGroupService.createAccessGroup(unitId, accessGroupDTO));
    }

    @PutMapping(value = UNIT_URL+"/access_group/{accessGroupId}")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateAccessGroup(@PathVariable long unitId, @PathVariable long accessGroupId, @Valid @RequestBody AccessGroupDTO accessGroupDTO) {
        AccessGroupDTO updatedObject = accessGroupService.updateAccessGroup(accessGroupId, unitId, accessGroupDTO);
        if (updatedObject == null) {
            return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, false);
        }
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true, updatedObject);
    }

    @DeleteMapping(value = UNIT_URL+"/access_group/{accessGroupId}")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> deleteAccessGroup(@PathVariable Long unitId,@PathVariable long accessGroupId) {
        boolean isObjectDeleted = accessGroupService.deleteAccessGroup(accessGroupId);
        if (isObjectDeleted) {
            return ResponseHandler.generateResponse(HttpStatus.CREATED, true, true);
        }
        return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, false);
    }


    @GetMapping(value = UNIT_URL+"/access_group")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAccessGroups(@PathVariable long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.getAccessGroupsForUnit(unitId));
    }

    @RequestMapping(value = UNIT_URL+"/access_group_by_role", method = RequestMethod.GET)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getManagementAccessGroups(@PathVariable long unitId, @RequestParam  AccessGroupRole role) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.getOrganizationManagementAccessGroups(unitId,role));
    }

    @RequestMapping(value = UNIT_URL+"/staff/{staffId}/access_group", method = RequestMethod.POST)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> assignAccessGroupToStaff(@PathVariable Long unitId,@PathVariable Long staffId, @RequestBody Map<String, Object> reqData) {

        List<String> accessGroupIds = (List<String>) reqData.get("accessGroupIds");
        boolean isGroupAssigned = accessGroupService.assignAccessGroupToStaff(accessGroupIds, staffId,unitId);
        if (isGroupAssigned) {
            return ResponseHandler.generateResponse(HttpStatus.OK, true, isGroupAssigned);
        }
        return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, isGroupAssigned);
    }

    @RequestMapping(value = UNIT_URL+"/access_page", method = RequestMethod.POST)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> createAccessPage(@RequestBody Map<String, Object> reqData) {
        String name = (String) reqData.get("name");
        boolean isModule = (boolean) reqData.get("isModule");
        List<Map<String, Object>> childPages = (List<Map<String, Object>>) reqData.get("childPages");
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.createAccessPage(name, childPages, isModule));
    }

    @RequestMapping(value = UNIT_URL+"/user/{userId}/organization/{orgId}/access_modules", method = RequestMethod.GET)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAccessModulesForUnits(@PathVariable long userId, @PathVariable long orgId) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.getAccessModulesForUnits(orgId, userId));
    }

    @RequestMapping(value = UNIT_URL+"/access_group/{accessGroupId}/access_page", method = RequestMethod.GET)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAccessPageHierarchy(@PathVariable long accessGroupId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.getAccessPageHierarchy(accessGroupId, null));

    }

    @RequestMapping(value = UNIT_URL+"/access_group/{accessGroupId}/access_page", method = RequestMethod.PUT)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> setAccessPageInGroup(@PathVariable Long unitId,@PathVariable long accessGroupId, @RequestBody AccessGroupPermissionDTO accessGroupPermission) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.setAccessPagePermissions(accessGroupId, accessGroupPermission.getAccessPageIds(), accessGroupPermission.isSelected(), null));
    }

    @RequestMapping(value = UNIT_URL+"/access_group/{accessGroupId}/auth/access_page", method = RequestMethod.GET)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAccessPageByAccessGroup(@PathVariable Long unitId, @RequestParam("staffId") long staffId,
                                                                          @PathVariable Long accessGroupId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.getAccessPageByAccessGroup(accessGroupId, unitId,staffId));

    }

    @RequestMapping(value = UNIT_URL+"/access_group/{accessGroupId}/auth/access_page", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> assignPermission(@PathVariable Long unitId,@PathVariable long accessGroupId, @RequestBody AccessPermissionDTO accessPermissionDTO) {
        accessGroupService.assignPermission(accessGroupId,accessPermissionDTO);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, true);

    }


    @RequestMapping(value = COUNTRY_URL+"/access_group", method = RequestMethod.POST)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> createCountryAccessGroup(@PathVariable long countryId,@Valid @RequestBody CountryAccessGroupDTO accessGroupDTO) {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true, accessGroupService.createCountryAccessGroup(countryId, accessGroupDTO));
    }

    @RequestMapping(value = COUNTRY_URL+"/access_group/{accessGroupId}", method = RequestMethod.PUT)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateCountryAccessGroup(@PathVariable Long countryId, @PathVariable Long accessGroupId,@Valid @RequestBody CountryAccessGroupDTO accessGroupDTO) {
        AccessGroup accessGroup = accessGroupService.updateCountryAccessGroup(countryId, accessGroupId, accessGroupDTO);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroup);
    }


    @RequestMapping(value = COUNTRY_URL+"/access_group/{accessGroupId}", method = RequestMethod.DELETE)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> deleteCountryAccessGroup(@PathVariable long accessGroupId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.deleteCountryAccessGroup(accessGroupId));
    }

    @ApiOperation("Get organization category with count of Access Groups of country")
    @RequestMapping(value = COUNTRY_URL + "/organization_category" , method = RequestMethod.GET)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getListOfOrgCategoryWithCountryAccessGroupCount(@PathVariable Long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,accessGroupService.getListOfOrgCategoryWithCountryAccessGroupCount(countryId));
    }

    @ApiOperation("Get country Access Groups")
    @RequestMapping(value = COUNTRY_URL + "/access_group/organization_category/{organizationCategory}" , method = RequestMethod.GET)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getCountryAccessGroups(@PathVariable Long countryId, @PathVariable OrganizationCategory organizationCategory) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,accessGroupService.getCountryAccessGroups(countryId, organizationCategory));
    }

    @ApiOperation("Get country Access Groups with category")
    @RequestMapping(value = COUNTRY_URL + "/access_group" , method = RequestMethod.GET)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getCountryAccessGroupsOfAllcategories(@PathVariable Long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,accessGroupService.getCountryAccessGroupsOfAllCategories(countryId));
    }

    @RequestMapping(value = COUNTRY_URL+"/access_group/{accessGroupId}/access_page", method = RequestMethod.GET)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAccessPageHierarchyForCountry(@PathVariable long accessGroupId, @PathVariable Long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.getAccessPageHierarchy(accessGroupId, countryId));

    }

    @RequestMapping(value = COUNTRY_URL+"/access_group/{accessGroupId}/access_page", method = RequestMethod.PUT)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> setAccessPageInAccessGroupForCountry(@PathVariable Long accessGroupId, @PathVariable Long countryId, @RequestBody AccessGroupPermissionDTO accessGroupPermission) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.setAccessPagePermissions( accessGroupId, accessGroupPermission.getAccessPageIds(), accessGroupPermission.isSelected(), countryId));
    }

    @RequestMapping(value = COUNTRY_URL+"/access_group/{accessGroupId}/access_page/{accessPageId}/permission", method = RequestMethod.PUT)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updatePermissionsForAccessTabsOfAccessGroupOfCountry(@RequestParam(value = "updateChildren") Boolean updateChildren, @PathVariable long accessGroupId, @PathVariable Long accessPageId, @RequestBody AccessPermissionDTO accessPermissionDTO) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.updatePermissionsForAccessTabsOfAccessGroup(accessGroupId, accessPageId, accessPermissionDTO,updateChildren,null));
    }

    @RequestMapping(value = UNIT_URL+"/access_group/{accessGroupId}/access_page/{accessPageId}/permission", method = RequestMethod.PUT)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updatePermissionsForAccessTabsOfAccessGroupOfOrg(@PathVariable Long unitId,@RequestParam(value = "updateChildren") Boolean updateChildren, @PathVariable long accessGroupId, @PathVariable Long accessPageId, @RequestBody AccessPermissionDTO accessPermissionDTO) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.updatePermissionsForAccessTabsOfAccessGroup(accessGroupId, accessPageId, accessPermissionDTO, updateChildren,unitId));
    }

    @RequestMapping(value = UNIT_URL+"/copy_unit_access_group", method = RequestMethod.POST)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> copyUnitAccessGroup(@PathVariable long unitId, @Valid @RequestBody AccessGroupDTO accessGroupDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.copyUnitAccessGroup(unitId, accessGroupDTO));
    }

    @RequestMapping(value = COUNTRY_URL+"/copy_country_access_group", method = RequestMethod.POST)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> copyCountryAccessGroup(@PathVariable long countryId,@Valid @RequestBody CountryAccessGroupDTO countryAccessGroupDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.copyCountryAccessGroup(countryId, countryAccessGroupDTO));
    }

    @RequestMapping(value = UNIT_URL+"/current_user/access_role", method = RequestMethod.GET)
    @ApiOperation("To fetch Access Role (Staff/Management) of current logged in user")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> checkIfUserHasAccessByRoleInUnit(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.findUserAccessRole(unitId));

    }

    @RequestMapping(value = UNIT_URL+"/current_user/access_role_and_reason_codes", method = RequestMethod.GET)
    @ApiOperation("To fetch Access Role (Staff/Management) of current logged in user and fetch reasoncodes for absence")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAccessRoleAndReasonCodes(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.getAbsenceReasonCodesAndAccessRole(unitId));

    }

    @ApiOperation("Get country Access Groups for hub and organization")
    @RequestMapping(value = COUNTRY_URL + "/access_group/hub_and_organization" , method = RequestMethod.GET)
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getCountryAccessGroupsForOrganizationCreation(@PathVariable Long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,accessGroupService.getCountryAccessGroupsForOrganizationCreation(countryId));
    }


    @ApiOperation("Get country Access Groups by account type")
    @GetMapping(value = COUNTRY_URL + "/access_group/account_type/{accountTypeId}" )
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getCountryAccessGroupByAccountTypeId (@PathVariable Long countryId, @PathVariable Long accountTypeId, @RequestParam(value = "accessGroupRole",required=false) String accessGroupRole) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,accessGroupService.getCountryAccessGroupByAccountTypeId(countryId, accountTypeId,accessGroupRole));
    }

    @ApiOperation("get staff ids by unit id and accessgroup id")
    @PostMapping(value = UNIT_URL+"/access_group/staffs")
    public ResponseEntity<Map<String, Object>> getStaffIdsByUnitIdAndAccessGroupId(@PathVariable Long unitId,@RequestBody List<Long> accessGroupId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.getStaffIdsByUnitIdAndAccessGroupId(unitId,accessGroupId));
    }

    @ApiOperation("get staff ids by unit id and accessgroup id")
    @PostMapping(value = UNIT_URL+"/staffs/access_groups")
    public ResponseEntity<Map<String, Object>> getStaffIdAndAccessGroupsByUnitIdAndAccessGroupId(@PathVariable Long unitId,@RequestBody List<Long> accessGroupId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.getStaffAndAccessGroupsByUnitId(unitId,accessGroupId));
    }

    @ApiOperation("Get unit Access Groups by parent Access Group")
    @PostMapping(value = UNIT_URL + "/access_groups_by_parent" )
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAccessGroupUsingParentId(@PathVariable Long unitId,@RequestBody Set<Long> accessGroupIds) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,accessGroupService.getAccessGroupUsingParentId(unitId,accessGroupIds));
    }

    @ApiOperation("Get unit Access Groups by parent Access Group")
    @GetMapping(value = UNIT_URL + "/get_access_group_by_unitId" )
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAccessGroupDayTypesAndUserId(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,accessGroupService.getAccessGroupWithDayTypesByStaffIdAndUnitId(unitId));
    }

    @ApiOperation("Get unit Access Roles by accessGroupId")
    @PostMapping(value = UNIT_URL + "/get_access_roles" )
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAccessRolesByAccessGroupId(@PathVariable Long unitId,@RequestBody Set<Long> accessGroupIds) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,accessGroupService.getAccessRoles(accessGroupIds));
    }

    @ApiOperation("update translation data")
    @PutMapping(value = UNIT_URL + "/access_group/{id}/language_settings" )
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateTranslationOfAccessGroupOfOrganization(@PathVariable Long id, @RequestBody Map<String, TranslationInfo> translations) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,translationService.updateTranslation(id,translations));
    }

    @ApiOperation("update translation data")
    @PutMapping(value = UNIT_URL + "/access_page/{id}/language_settings" )
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateTranslationOfAccessPageOfOrganization(@PathVariable Long id, @RequestBody Map<String, TranslationInfo> translations) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, translationService.updateTranslation(id, translations));
    }

    @ApiOperation("update translation data")
    @PutMapping(value = COUNTRY_URL + "/access_group/{id}/language_settings" )
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateTranslationOfAccessGroup(@PathVariable Long id, @RequestBody Map<String, TranslationInfo> translations) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,translationService.updateTranslation(id,translations));
    }

    @ApiOperation("update translation data")
    @PutMapping(value = COUNTRY_URL + "/access_page/{id}/language_settings" )
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateTranslationOfAccessPage(@PathVariable Long id, @RequestBody Map<String, TranslationInfo> translations) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, translationService.updateTranslation(id, translations));
    }

    @ApiOperation("Get Country Access Group Linking Details")
    @GetMapping(value = COUNTRY_URL + "/get_country_access_group_details" )
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getCountryAccessGroupLinkingDetails(@RequestParam Long accessGroupId) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,accessGroupService.getCountryAccessGroupLinkingDetails(accessGroupId));
    }

    @ApiOperation("Get Unit Access Group Linking Details")
    @GetMapping(value = UNIT_URL + "/get_unit_access_group_details" )
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getOrganizationAccessGroupLinkingDetails(@RequestParam Long accessGroupId) {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,accessGroupService.getOrganizationAccessGroupLinkingDetails(accessGroupId));
    }

}
