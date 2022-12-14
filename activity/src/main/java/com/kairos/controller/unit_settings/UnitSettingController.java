package com.kairos.controller.unit_settings;

import com.kairos.dto.activity.unit_settings.FlexibleTimeSettingDTO;
import com.kairos.dto.activity.unit_settings.UnitAgeSettingDTO;
import com.kairos.dto.activity.unit_settings.UnitSettingDTO;
import com.kairos.service.unit_settings.UnitSettingService;
import com.kairos.service.user_service_data.UnitDataService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.math.BigInteger;
import java.time.ZoneId;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_UNIT_URL;

@RestController
@RequestMapping(API_UNIT_URL)
@Api(API_UNIT_URL)
public class UnitSettingController {

    @Inject
    private UnitSettingService unitSettingService;
    @Inject
    private UnitDataService unitDataService;

    @ApiOperation(value = "get unit age settings")
    @GetMapping(value = "/age_setting")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getUnitAgeSettings(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitSettingService.getUnitAgeSettings(unitId));
    }

    @ApiOperation(value = "update unit age settings")
    @PutMapping(value = "/age_setting")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateUnitAgeSettings(@PathVariable Long unitId,
                                                                             @RequestBody @Valid UnitAgeSettingDTO unitAgeSettingDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitSettingService.updateUnitAgeSettings(unitId,  unitAgeSettingDTO));
    }

    @ApiOperation(value = "get unit open shift phase settings")
    @GetMapping(value = "unit_setting/open_shift_phase_setting")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getOpenShiftPhaseSettings(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitSettingService.getOpenShiftPhaseSettings(unitId));
    }

    @ApiOperation(value = "update unit open shift phase settings")
    @PutMapping(value = "/unit_setting/{unitSettingId}/open_shift_phase_setting")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateOpenShiftPhaseSettings(@PathVariable Long unitId,@PathVariable BigInteger unitSettingId,
                                                                     @RequestBody @Valid UnitSettingDTO unitSettingDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitSettingService.updateOpenShiftPhaseSettings(unitId,  unitSettingId,unitSettingDTO));
    }

    @ApiOperation(value = "add default open shift phase settings")
    @PostMapping(value = "/unit_setting/open_shift_phase_setting")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> addDefaultOpenShiftPhaseSettings(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitSettingService.createDefaultOpenShiftPhaseSettings(unitId,null));
    }

    @ApiOperation(value = "add parent organization id and country id for units")
    @PostMapping(value = "/parent_org_and_country")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> addParentOrganizationAndCountryIdForAllUnits() {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitDataService.addParentOrganizationAndCountryIdForAllUnits());
    }

    //Flexible Time Settings

    @ApiOperation("Get Flexible time settings")
    @GetMapping("/flexible_time")
    public ResponseEntity<Map<String,Object>> getFlexibleTime(@PathVariable Long unitId){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,unitSettingService.getFlexibleTime(unitId));
    }

    @ApiOperation("Update Flexible time setting")
    @PutMapping("/flexible_time")
    public ResponseEntity<Map<String,Object>> updateFlexibleTime(@PathVariable Long unitId, @RequestBody FlexibleTimeSettingDTO flexibleTimeSettingDTO){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,unitSettingService.updateFlexibleTime(unitId,flexibleTimeSettingDTO));
    }

    @ApiOperation("get All time type")
    @GetMapping("/time_type")
    public ResponseEntity<Map<String,Object>> getAllTimeTypes(@PathVariable Long unitId){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,unitSettingService.getAllTimeTypes(unitId));
    }

    @PutMapping("/update_time_zone")
    public ResponseEntity<Map<String,Object>> updateTimeZone(@PathVariable Long unitId,@RequestBody ZoneId timeZone){
        unitSettingService.updateTimeZone(unitId,timeZone);
        return ResponseHandler.generateResponse(HttpStatus.OK,true,null);
    }

}
