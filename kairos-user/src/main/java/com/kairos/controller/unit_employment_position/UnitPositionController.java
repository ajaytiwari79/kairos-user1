package com.kairos.controller.unit_employment_position;


import com.kairos.persistence.model.user.agreement.wta.WTADTO;
import com.kairos.response.dto.web.UnitPositionDTO;
import com.kairos.service.unit_position.UnitPositionService;
import com.kairos.util.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_ORGANIZATION_UNIT_URL;

/**
 * Created by pawanmandhan on 26/7/17.
 */

@RestController
@RequestMapping(API_ORGANIZATION_UNIT_URL)
@Api(API_ORGANIZATION_UNIT_URL)
public class UnitPositionController {

    @Inject
    private UnitPositionService unitPositionService;

    @ApiOperation(value = "Create a New Position")
    @PostMapping(value = "/unit_position")
    public ResponseEntity<Map<String, Object>> createUnitEmploymentPosition(@PathVariable Long unitId, @RequestParam("type") String type, @RequestBody @Valid UnitPositionDTO position) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitPositionService.createUnitPosition(unitId, type, position, false));
    }

    /*
   * @auth vipul
   * used to get all positions of organization n by organization and staff Id
   * */
    @ApiOperation(value = "Get all unit_position by organization and staff")
    @RequestMapping(value = "/unit_position/staff/{staffId}")
    ResponseEntity<Map<String, Object>> getAllUnitEmploymentPositionsOfStaff(@PathVariable Long unitId, @RequestParam("type") String type, @PathVariable Long staffId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitPositionService.getUnitPositionsOfStaff(unitId, staffId, type));
    }

    @ApiOperation(value = "Remove unit_position")
    @DeleteMapping(value = "/unit_position/{unitPositionId}")
    public ResponseEntity<Map<String, Object>> deleteUnitPosition(@PathVariable Long unitPositionId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitPositionService.removePosition(unitPositionId));
    }


    @ApiOperation(value = "Update unit_position")
    @PutMapping(value = "/unit_position/{unitPositionId}")
    public ResponseEntity<Map<String, Object>> updateUnitPosition(@PathVariable Long unitPositionId, @RequestBody @Valid UnitPositionDTO position) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitPositionService.updateUnitPosition(unitPositionId, position));
    }

    @ApiOperation(value = "Update unit_position's WTA")
    @PutMapping(value = "/unit_position/{unitPositionId}/wta/{wtaId}")
    public ResponseEntity<Map<String, Object>> updateUnitPositionWTA(@PathVariable Long unitPositionId, @PathVariable Long unitId, @PathVariable Long wtaId, @RequestBody @Valid WTADTO wtadto) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitPositionService.updateUnitPositionWTA(unitId, unitPositionId, wtaId, wtadto));
    }

    @ApiOperation(value = "get unit_position's WTA")
    @GetMapping(value = "/unit_position/{unitPositionId}/wta")
    public ResponseEntity<Map<String, Object>> getUnitEmploymentPositionWTA(@PathVariable Long unitPositionId, @PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitPositionService.getUnitPositionWTA(unitId, unitPositionId));
    }
/*
    @ApiOperation(value = "Get Position")
    @GetMapping(value = "/unit_employment_position/{unitPositionId}")
    public ResponseEntity<Map<String, Object>> getUnitEmploymentPosition(@PathVariable Long unitPositionId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitEmploymentPositionService.getUnitEmploymentPosition(unitPositionId));
    }

*/

//    @ApiOperation(value = "Get all positions by unit Employment")
//    @RequestMapping(value = "/position_code")
//    ResponseEntity<Map<String, Object>> getAllUnitEmploymentPositions(@PathVariable Long unitEmploymentId) {
//        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitEmploymentPositionService.getAllUnitEmploymentPositions(unitEmploymentId));
//    }

    @ApiOperation(value = "get unit_position's CTA")
    @GetMapping(value = "/unit_position/{unitPositionId}")
    public ResponseEntity<Map<String, Object>> getUnitEmploymentPositionCTA(@PathVariable Long unitPositionId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, unitPositionService.getUnitEmploymentPositionCTA( unitPositionId));
    }

}
