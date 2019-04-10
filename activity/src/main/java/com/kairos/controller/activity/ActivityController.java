package com.kairos.controller.activity;


import com.kairos.dto.activity.activity.ActivityDTO;
import com.kairos.dto.activity.activity.activity_tabs.*;
import com.kairos.dto.activity.activity.activity_tabs.communication_tab.CommunicationActivityDTO;
import com.kairos.persistence.model.activity.tabs.OptaPlannerSettingActivityTab;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.activity.ActivityService;
import com.kairos.service.activity.TimeTypeService;
import com.kairos.utils.response.ResponseHandler;
import com.kairos.dto.activity.activity.activity_tabs.RulesActivityTabDTO;
import com.kairos.dto.activity.activity.activity_tabs.SkillActivityDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.kairos.constants.ApiConstants.API_ORGANIZATION_COUNTRY_URL;


/**
 * Created by pawanmandhan on 17/8/17.
 */

@RestController
@RequestMapping(API_ORGANIZATION_COUNTRY_URL)
@Api(API_ORGANIZATION_COUNTRY_URL)
public class ActivityController {

    @Inject
    private ActivityService activityService;
    @Inject private TimeTypeService timeTypeService;
    @Inject
    private UserIntegrationService userIntegrationService;


    @ApiOperation("Create Activity")
    @PostMapping(value = "/activity")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> createActivity(@PathVariable Long countryId, @RequestBody @Valid ActivityDTO activity) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.createActivity(countryId, activity));
    }

    @ApiOperation("Get all activity based on countryId")
    @GetMapping(value = "/activity")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getActivity(@PathVariable long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.findAllActivityByCountry(countryId));
    }

    @ApiOperation("Get all activity based on countryId for CTA default data")
    @GetMapping(value = "/activity/cta_wta_setting")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getActivityForDefaultData(@PathVariable long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.findAllActivityWithCtaWtaSettingByCountry(countryId));
    }

    @ApiOperation("Get all activity categories based on countryId ")
    @GetMapping(value = "/activity/activity_categories")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getActivityCategoriesForDefaultData(@PathVariable long countryId,@RequestParam List<BigInteger> activityCategoriesIds) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.findAllActivityCategoriesByCountry(countryId, activityCategoriesIds));
    }

    @ApiOperation("Get all activity Ids of units by parentIds")
    @GetMapping(value = "/unit_activity")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getListOfActivityIdsOfUnitByParentIds(@RequestParam List<BigInteger> parentActivityIds, @RequestParam List<Long> unitIds) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getListOfActivityIdsOfUnitByParentIds(parentActivityIds, unitIds));
    }


    @ApiOperation("Update GeneralActivityTab Tab of Activity")
    @PutMapping(value = "/activity/general")
        //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateGeneralTab(@PathVariable Long countryId, @RequestBody GeneralActivityTabDTO generalDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateGeneralTab(countryId, generalDTO));
    }

    @ApiOperation("Update icon  in Activity")
    @PostMapping(value = "/activity/{activityId}/icon")
        //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> addIconInActivity(@PathVariable BigInteger activityId, @RequestParam("file") MultipartFile file) throws
            IOException {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.addIconInActivity(activityId, file));
    }

    /*
    * @vipul
    * used to get all tabs data
    * */

    @ApiOperation("get General Tab of Activity")
    @GetMapping(value = "/activity/{activityId}/general")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getGeneralTab(@PathVariable Long countryId, @PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getGeneralTabOfActivity(countryId, activityId));
    }

    /*@ApiOperation("get balanceSettings Tab of Activity")
    @GetMapping(value = "/activity/{activityId}/balanceSettings")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getBalanceSettingsTab(@PathVariable Long countryId, @PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getBalanceSettingsTabOfActivity(activityId,countryId));
    }*/


    @ApiOperation("get getTime Calculation Tab of Activity")
    @GetMapping(value = "/activity/{activityId}/timeCalculation")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getTimeCalculationTabOfActivity(@PathVariable Long countryId, @PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getTimeCalculationTabOfActivity(activityId,countryId));
    }


    //getTimeCalculationTabOfActivity


  /*  @ApiOperation("Update Balance Settings Tab of Activity")
    @PutMapping(value = "/activity/balanceSettings")
        //  @PreAuthorize("@S.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateBalanceSettingsTab(@RequestBody BalanceSettingActivityTabDTO balanceDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateBalanceTab(balanceDTO));
    }
*/

    @ApiOperation("Update IndividualPoints Tab of Activity")
    @PutMapping(value = "/activity/individualPoints")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateIndividualPointsTab(@RequestBody IndividualPointsActivityTabDTO individualPointsDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateIndividualPointsTab(individualPointsDTO));
    }

    @ApiOperation("get IndividualPoints Tab of Activity")
    @GetMapping(value = "/activity/{activityId}/individualPoints")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getIndividualPointsTab(@PathVariable BigInteger countryId, @PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getIndividualPointsTabOfActivity(activityId));
    }



    @ApiOperation("delete an activity based on countryId")
    @DeleteMapping(value = "/activity/{activityId}")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> deleteActivity(@PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.deleteCountryActivity(activityId));
    }

    @ApiOperation("get Rules Tab of Activity")
    @GetMapping(value = "/activity/{activityId}/rules")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getRulesTab(@PathVariable Long countryId, @PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getRulesTabOfActivity(activityId,countryId));
    }

    @ApiOperation("Update Rules Tab of Activity")
    @PutMapping(value = "/activity/rules")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateRulesTab(@RequestBody RulesActivityTabDTO rulesDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateRulesTab(rulesDTO));
    }

    //Phase Settings

    @ApiOperation("get Phase setting Tab of Activity")
    @GetMapping(value = "/activity/{activityId}/phase_settings")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getPhaseSettingTab(@PathVariable Long countryId, @PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getPhaseSettingTabOfActivity(activityId,countryId));
    }

    @ApiOperation("Update Phase setting Tab of Activity")
    @PutMapping(value = "/activity/phase_settings")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updatePhaseSettingTab(@RequestBody PhaseSettingsActivityTab phaseSettingsActivityTab) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updatePhaseSettingTab(phaseSettingsActivityTab));
    }


    @ApiOperation("Update Time calculation Tab of Activity")
    @PutMapping(value = "/activity/timeCalculation")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateTimeCalculationTabOfActivity(@RequestBody TimeCalculationActivityDTO timeCalculationActivityDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateTimeCalculationTabOfActivity(timeCalculationActivityDTO));
    }


    @ApiOperation("Update compositeShifts Tab of Activity")
    @PutMapping(value = "/activity/{activityId}/compositeShifts")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> assignCompositeActivitiesInActivity(@PathVariable BigInteger activityId,@RequestBody List<CompositeShiftActivityDTO> compositeShiftActivityDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.assignCompositeActivitiesInActivity(activityId,compositeShiftActivityDTO));
    }

    @ApiOperation("get compositeShifts Tab of Activity")
    @GetMapping(value = "/activity/{activityId}/compositeShifts")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getCompositeShiftTabOfActivity(@PathVariable BigInteger activityId,@PathVariable Long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getCompositeAndChildActivityOfCountryActivity(activityId,countryId));
    }


    @ApiOperation("Update child activity Tab of Activity")
    @PutMapping(value = "/activity/{activityId}/childActivities")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> assignChildActivitiesInActivity(@PathVariable BigInteger activityId,@RequestBody Set<BigInteger> childActivitiesIds) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.assignChildActivitiesInActivity(activityId,childActivitiesIds));
    }

   /* @ApiOperation("Update notes Tab of Activity")
    @PutMapping(value = "/activity/notes")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateNotesTabOfActivity(@RequestBody NotesActivityDTO notesActivityDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateNotesTabOfActivity(notesActivityDTO));
    }*/

    @ApiOperation("get notes Tab of Activity")
    @GetMapping(value = "/activity/{activityId}/notes")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getNotesTabOfActivity(@PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getNotesTabOfActivity(activityId));
    }
    //Api for uploading Documents in Notes Tab
    @ApiOperation("Upload Notes  in Activity")
    @PostMapping(value = "/activity/{activityId}/upload_note")
    ResponseEntity<Map<String, Object>> addDocumentInNotesTab(@PathVariable BigInteger activityId, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.addDocumentInNotesTab(activityId, file));
    }

    @ApiOperation("Update Communication Tab of Activity")
    @PutMapping(value = "/activity/communication")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateCommunicationTabOfActivity(@RequestBody CommunicationActivityDTO communicationActivityDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateCommunicationTabOfActivity(communicationActivityDTO));
    }

    @ApiOperation("get CommunicationTab of Activity")
    @GetMapping(value = "/activity/{activityId}/communication")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getCommunicationTabOfActivity(@PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getCommunicationTabOfActivity(activityId));
    }

    //updateBonusTabOfActivityType

    @ApiOperation("Update Bonus Tab of Activity")
    @PutMapping(value = "/activity/bonus")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateBonusTabOfActivity(@RequestBody BonusActivityDTO bonusActivityDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateBonusTabOfActivity(bonusActivityDTO));
    }

    @ApiOperation("get Bonus Tab of Activity")
    @GetMapping(value = "/activity/{activityId}/bonus")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getBonusTabOfActivity(@PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getBonusTabOfActivity(activityId));
    }

    // skills

    @ApiOperation("update Skill tab of activity Type")
    @PutMapping(value = "/activity/skill")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateSkillTabOfActivity(@RequestBody SkillActivityDTO skillActivityDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateSkillTabOfActivity(skillActivityDTO));
    }

    @ApiOperation("get Bonus Tab of Activity")
    @GetMapping(value = "/activity/{activityId}/skill")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getSkillTabOfActivity(@PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getSkillTabOfActivity(activityId));
    }


    //organization Mapping

    @ApiOperation("update organization Mapping details  of activity Type")
    @PutMapping(value = "/activity/{activityId}/organizationMapping")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateOrgMappingDetailOfActivity(@RequestBody OrganizationMappingActivityDTO organizationMappingActivityDTO, @PathVariable BigInteger activityId) {
        activityService.updateOrgMappingDetailOfActivity(organizationMappingActivityDTO, activityId);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, null);
    }

    @ApiOperation("get organization Mapping details  of Activity")
    @GetMapping(value = "/activity/{activityId}/organizationMapping")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getOrgMappingDetailOfActivity(@PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getOrgMappingDetailOfActivity(activityId));
    }

    // optaPlannerSetting
    @ApiOperation("get Opta PlannerSetting tab data of activity type")
    @GetMapping(value = "/activity/{activityId}/opta_planner_settings")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getOptaPlannerSettingsTabOfActivity(@PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getOptaPlannerSettingsTabOfActivity(activityId));
    }

    @ApiOperation("update Opta PlannerSetting  details  of activity Type")
    @PutMapping(value = "/activity/{activityId}/opta_planner_settings")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateOrgMappingDetailOfActivity(@PathVariable BigInteger activityId,@RequestBody OptaPlannerSettingActivityTab optaPlannerSettingActivityTab) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateOptaPlannerSettingsTabOfActivity(activityId,optaPlannerSettingActivityTab));
    }

    // cta_wta_settings
    @ApiOperation("get Cta And Wta settings tab data of activity type")
    @GetMapping(value = "/activity/{activityId}/cta_wta_settings")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getCtaAndWtaSettingsTabOfActivity(@PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getCtaAndWtaSettingsTabOfActivity(activityId));
    }

    @ApiOperation("update cta_response and wta settings  of activity Type")
    @PutMapping(value = "/activity/cta_wta_settings")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateCtaAndWtaSettingsTabOfActivity(@RequestBody CTAAndWTASettingsActivityTabDTO ctaAndWtaSettingsActivityTabDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateCtaAndWtaSettingsTabOfActivity(ctaAndWtaSettingsActivityTabDTO));
    }


   /* @ApiOperation("getAll TimeType")
    @GetMapping(value = "/timeTypes")
    public ResponseEntity<Map<String, Object>> getAllTimeType() {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, timeTypeService.getAllTimeType());
    }*/

    /*@ApiOperation("getAll TimeType")
    @GetMapping(value = "/timeType_setting")
    public ResponseEntity<Map<String, Object>> updatetimeType_setting(@RequestParam BigInteger activityId,@RequestParam BigInteger timeTypeId) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updatetimeType_setting(activityId,timeTypeId));
    }*/

     @ApiOperation("publish activity")
    @PutMapping(value = "/activity/{activityId}/publish")
    public ResponseEntity<Map<String, Object>> publishActivity(@PathVariable BigInteger activityId) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.publishActivity(activityId));
    }



    @ApiOperation("copy Activity")
    @PostMapping(value = "/activity/{activityId}/copy_activity")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> copyActivityDetails(@PathVariable Long countryId, @PathVariable BigInteger activityId, @RequestBody ActivityDTO activity) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.copyActivityDetails(countryId, activityId, activity));
    }



    @ApiOperation("get location settings data of activity")
    @GetMapping(value = "/activity/{activityId}/location_settings")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getLocationsTabOfActivity(@PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getLocationsTabOfActivity(activityId));
    }

    @ApiOperation("update location settings of activity ")
    @PutMapping(value = "/activity/location_settings")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateLocationsTabOfActivity(@RequestBody LocationActivityTabDTO locationActivityTabDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.updateLocationsTabOfActivity(locationActivityTabDTO));
    }

    @ApiOperation("Get all absence activity")
    @GetMapping(value = "/absence-activities")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAllAbsenceActivities(@PathVariable long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.findAllActivityByCountry(countryId));
    }

    @ApiOperation("Get all activity based on country")
    @GetMapping(value = "/activities_with_time_types")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getActivitiesWithTimeTypes(@PathVariable Long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getActivitiesWithTimeTypes(countryId));
    }

    @ApiOperation("Remove uploaded Attachments in Activity")
    @DeleteMapping(value = "/activity/{activityId}/remove_uploaded_attachments")
    ResponseEntity<Map<String, Object>> removeAttachementsFromActivity(@PathVariable BigInteger activityId, @RequestParam boolean removeNotes){
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.removeAttachementsFromActivity(activityId, removeNotes));
    }

}
