package com.kairos.controller.unit_settings;

import com.kairos.dto.TranslationInfo;
import com.kairos.dto.activity.unit_settings.PhaseSettingsDTO;
import com.kairos.service.unit_settings.PhaseSettingsService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_UNIT_URL;

@RestController
@RequestMapping(API_UNIT_URL)
@Api(API_UNIT_URL)
public class PhaseSettingsController {
    @Inject
    private PhaseSettingsService phaseSettingsService;

    @ApiOperation(value = "get unit phase settings")
    @GetMapping(value = "/phase_settings")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getPhaseSettings(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, phaseSettingsService.getPhaseSettings(unitId));
    }

    @ApiOperation(value = "update unit phase settings")
    @PutMapping(value = "/phase_settings")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateUnitAgeSettings(@PathVariable Long unitId,
                                                                     @RequestBody @Valid List<PhaseSettingsDTO> phaseSettingsDTOS) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, phaseSettingsService.updatePhaseSettings(unitId,  phaseSettingsDTOS));
    }

    @ApiOperation(value = "get unit phase settings")
    @PostMapping(value = "/default_phase_settings")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> createDefaultPhaseSettings(@PathVariable Long unitId) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, phaseSettingsService.createDefaultPhaseSettings(unitId,null));
    }

    @ApiOperation(value = "update translation data")
    @PutMapping(value = "/phase_settings/{phaseId}/language_settings")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateUnitAgeSettings(@PathVariable Long unitId,@PathVariable BigInteger phaseId,
                                                                     @RequestBody Map<String, TranslationInfo> translations) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, phaseSettingsService.updatePhaseSettingTranslations(unitId,phaseId,translations));
    }
}
