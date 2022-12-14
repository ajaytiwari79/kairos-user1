package com.kairos.controller.action;

import com.kairos.enums.TimeTypeEnum;
import com.kairos.service.action.ActionService;
import com.kairos.utils.response.ResponseHandler;
import com.kairos.wrapper.action.ActionDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_UNIT_URL;

/**
 * Created By G.P.Ranjan on 2/4/20
 **/
@RestController
@RequestMapping(API_UNIT_URL)
@Api(API_UNIT_URL)
public class ActionController {
    @Inject
    private ActionService actionService;

    @ApiOperation("Save Action")
    @PostMapping("/action")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> saveAction(@PathVariable Long unitId, @Validated @RequestBody ActionDTO actionDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, actionService.saveAction(unitId, actionDTO));
    }

    @ApiOperation("Create Default Action")
    @PostMapping("/action/default")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> createDefaultAction(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, actionService.createDefaultAction(unitId));
    }

    @ApiOperation("Update Action")
    @PutMapping("/action/{actionId}")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateAction(@PathVariable BigInteger actionId, @Validated @RequestBody ActionDTO actionDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, actionService.updateAction(actionId, actionDTO));
    }

    @ApiOperation("Get Action")
    @GetMapping("/action/{actionId}")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAction(@PathVariable BigInteger actionId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, actionService.getAction(actionId));
    }

    @ApiOperation("Get All Action By UnitId")
    @GetMapping("/action")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAllActionByUnitId(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, actionService.getAllActionByUnitId(unitId));
    }

    @ApiOperation("Get count before after shift")
    @GetMapping("/staff/{staffId}/count_before_after_shift")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getCountBeforeAfterDate(@PathVariable Long unitId, @PathVariable Long staffId,@RequestParam("shift_date") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date ShiftDate) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, actionService.getCountBeforeAfterDate(unitId, staffId, ShiftDate));
    }

    @ApiOperation("Remove before after shift by time type")
    @DeleteMapping("/staff/{staffId}/before_after_shift")
    ResponseEntity<Map<String, Object>> removeBeforeAfterShiftByTimeType(@PathVariable Long staffId, @RequestParam("remove_time_type") TimeTypeEnum timeTypeEnum, @RequestParam boolean before, @RequestParam("remove_nearest_one") boolean removeNearestOne, @RequestParam("shift_date") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date ShiftDate){
        return ResponseHandler.generateResponse(HttpStatus.OK, true, actionService.removeBeforeAfterShiftByTimeType(staffId, timeTypeEnum, before, removeNearestOne, ShiftDate));
    }

    @ApiOperation("Update action count of staff")
    @PutMapping("/staff/{staffId}/action_info")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateActionInfoOfStaff(@PathVariable Long unitId, @PathVariable Long staffId, @RequestParam("action_name") String actionName) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, actionService.updateActionInfoOfStaff(unitId, staffId, actionName));
    }

    @ApiOperation("Get action info of staff")
    @GetMapping("/staff/{staffId}/action_info")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getActionInfoOfStaff(@PathVariable Long unitId, @PathVariable Long staffId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, actionService.getActionInfoOfStaff(unitId, staffId));
    }

}
