package com.kairos.controller.period;

import com.kairos.dto.activity.period.PlanningPeriodDTO;
import com.kairos.dto.planner.shift_planning.ShiftPlanningProblemSubmitDTO;
import com.kairos.enums.planning_period.PlanningPeriodAction;
import com.kairos.service.period.PlanningPeriodService;
import com.kairos.service.time_bank.TimeBankService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static com.kairos.constants.ApiConstants.API_UNIT_URL;

/**
 * Created by prerna on 6/4/18.
 */
@RestController()
@Api(API_UNIT_URL)
@RequestMapping(API_UNIT_URL)
public class PlanningPeriodController {

    @Inject
    PlanningPeriodService planningPeriodService;
    @Inject private TimeBankService timeBankService;

    @ApiOperation(value = "Create Planning Period")
    @PostMapping(value="/period")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> createPlanningPeriod(@PathVariable Long unitId,  @RequestBody @Valid PlanningPeriodDTO planningPeriodDTO) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.addPlanningPeriods(unitId, planningPeriodDTO));


    }

    @ApiOperation(value = "Get Planning Period")
    @GetMapping(value="/period")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getPlanningPeriod(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.getPlanningPeriods(unitId, null, null));
    }

    @ApiOperation(value = "Get Planning Period with phase for self roastering")
    @GetMapping(value="/period_of_interval")
    public ResponseEntity<Map<String, Object>> getPlanningPeriodOfInterval(@PathVariable Long unitId, @RequestParam(required = true, value = "startDate") @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate startDate, @RequestParam(required = true, value = "endDate") @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate endDate) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.getPeriodOfInterval(unitId, startDate,endDate));
    }

    @ApiOperation(value = "update period by unit Id and Period Id")
    @PutMapping(value = "/period/{periodId}")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updatePhase(@PathVariable BigInteger periodId, @PathVariable Long unitId, @RequestBody @Valid PlanningPeriodDTO planningPeriodDTO) {
         return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.updatePlanningPeriod(unitId, periodId, planningPeriodDTO));

    }

    @ApiOperation(value = "Remove Period")
    @DeleteMapping(value = "/period/{periodId}")
    public ResponseEntity<Map<String, Object>> deletePhase(@PathVariable Long unitId, @PathVariable BigInteger periodId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.deletePlanningPeriod(unitId, periodId));
    }


    @ApiOperation(value = "update period's phase to next phase")
    @PutMapping(value = "/period/{periodId}/next_phase")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updatePlanningPeriodPhaseToNext(@PathVariable BigInteger periodId, @PathVariable Long unitId , @RequestBody(required=false) Set<Long> employmentTypeIds, @RequestParam PlanningPeriodAction planningPeriodAction) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.setPlanningPeriodPhaseToPublishOrFlip(unitId, periodId ,employmentTypeIds,planningPeriodAction));
    }

    @ApiOperation(value = "restore shift base planning period and phase id")
    @PutMapping(value = "/period/{periodId}/reset_phase")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> resetPhaseData(@PathVariable BigInteger periodId, @PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.restoreShiftToPreviousPhase(periodId,unitId));
    }



    @ApiOperation(value = "Migrate Planning Period")
    @PostMapping(value="/migrate_planning_period")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> migratePlanningPeriod(@PathVariable Long unitId,  @RequestBody @Valid PlanningPeriodDTO planningPeriodDTO) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.migratePlanningPeriods(unitId, planningPeriodDTO));
    }


    @ApiOperation(value = "Register Job For Existing Planning Period")
    @PostMapping(value="/register_job_for_existing_planning_period")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> registerJobForExistingPlanningPeriod() {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.registerJobForExistingPlanningPeriod());
    }

    @ApiOperation(value = "/get details for auto planning")
    @PostMapping(value="/get_details_for_auto_planning")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> registerJobForExistingPlanningPeriod(@RequestBody ShiftPlanningProblemSubmitDTO shiftPlanningProblemSubmitDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.findDataForAutoPlanning(shiftPlanningProblemSubmitDTO));
    }

    @ApiOperation(value = "Get default data for solver config")
    @GetMapping(value="/get_default_data_for_solver_cofig")
    public ResponseEntity<Map<String, Object>> getDefaultDataForPlanning(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.getDefaultDataForPlanning(unitId));
    }

    @ApiOperation(value = "Get Planning Period Details")
    @GetMapping(value="/get_planning_period_details")
    public ResponseEntity<Map<String, Object>> getPlanningPeriodDetails(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, planningPeriodService.getPlanningPeriodDetails(unitId));
    }



}
