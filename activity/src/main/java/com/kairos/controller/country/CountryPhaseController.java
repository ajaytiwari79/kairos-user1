package com.kairos.controller.country;

import com.kairos.dto.TranslationInfo;
import com.kairos.dto.activity.phase.PhaseDTO;
import com.kairos.service.phase.PhaseService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_V1;
import static com.kairos.constants.ApiConstants.COUNTRY_URL;

/**
 * Created by vipul on 15/12/17.
 */
@RestController
@RequestMapping(API_V1)
@Api(API_V1)
public class CountryPhaseController {
    @Inject
    private PhaseService phaseService;


    @ApiOperation(value = "Create Phases in country")
    @PostMapping(value = COUNTRY_URL+"/phase")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> createPhase(@PathVariable Long countryId, @RequestBody @Valid PhaseDTO phaseDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, phaseService.createPhaseInCountry(countryId, phaseDTO));
    }



    /**
     * @Author vipul
     * used to get all phases of country
     */
    @ApiOperation(value = "get All  Phases in country")
    @GetMapping(value = COUNTRY_URL+"/phase")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getPhasesByCountryId(@PathVariable Long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, phaseService.getPhasesWithCategoryByCountryId(countryId));
    }


    @ApiOperation(value = "get All  Phase status")
    @GetMapping(value = "/phase/status")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAllPhasesStatus() {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, phaseService.getAllApplicablePhaseStatus());
    }

    @ApiOperation(value = "get All  Phases in country")
    @GetMapping(value = COUNTRY_URL+"/phase/all")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getAllPhases(@PathVariable Long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, phaseService.getPhasesByCountryId(countryId));
    }

    @ApiOperation(value = "remove a  Phase in country")
    @DeleteMapping(value = COUNTRY_URL+"/phase/{phaseId}")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> deletePhase(@PathVariable Long countryId,@PathVariable BigInteger phaseId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, phaseService.deletePhase(countryId,phaseId));
    }

    @ApiOperation(value = "update a  Phase in country")
    @PutMapping(value = COUNTRY_URL+"/phase/{phaseId}")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updatePhases(@PathVariable Long countryId,@PathVariable BigInteger phaseId,@RequestBody @Valid PhaseDTO phaseDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, phaseService.updatePhases(countryId,phaseId,phaseDTO));
    }

    @ApiOperation(value = "update translation data")
    @PutMapping(value = COUNTRY_URL+"/phase/{phaseId}/language_settings")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updatePhasesTRanslations(@PathVariable BigInteger phaseId, @RequestBody Map<String, TranslationInfo> translations) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, phaseService.updateTranslations(phaseId,translations));
    }
}
