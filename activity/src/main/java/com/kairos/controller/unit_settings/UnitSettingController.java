package com.kairos.controller.unit_settings;

import com.kairos.service.unit_settings.UnitSettingService;
import com.kairos.util.response.ResponseHandler;
import com.kairos.activity.unit_settings.UnitAgeSettingDTO;
import com.kairos.activity.unit_settings.UnitSettingDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_ORGANIZATION_UNIT_URL;

@RestController
@RequestMapping(API_ORGANIZATION_UNIT_URL)
@Api(API_ORGANIZATION_UNIT_URL)
public class UnitSettingController {

    @Inject
    private UnitSettingService unitSettingService;

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
}
