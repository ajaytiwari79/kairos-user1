package com.kairos.controller.position;

import com.kairos.persistence.model.user.position.PositionName;
import com.kairos.service.position.PositionNameService;
import com.kairos.util.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_ORGANIZATION_UNIT_URL;

/**
 * Created by pawanmandhan on 27/7/17.
 */

@RestController
@RequestMapping(API_ORGANIZATION_UNIT_URL)
@Api(API_ORGANIZATION_UNIT_URL)
public class PositionNameController {

    @Inject
    private PositionNameService positionNameService;


    @ApiOperation("Create PositionName")
    @PostMapping(value = "/position_name")
    ResponseEntity<Map<String, Object>> createPositionName( @RequestParam("type") String type,@PathVariable Long unitId, @RequestBody PositionName positionName) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, positionNameService.createPositionName( unitId, positionName,type));
    }

    @ApiOperation("Update PositionName")
    @PutMapping(value = "/position_name/{positionNameId}")
    ResponseEntity<Map<String, Object>> updatePositionName(@RequestParam("type") String type, @PathVariable Long unitId, @PathVariable long positionNameId, @RequestBody PositionName positionName) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, positionNameService.updatePositionName(unitId, positionNameId, positionName,type));
    }

    @ApiOperation("Delete PositionName")
    @DeleteMapping(value = "/position_name/{positionNameId}")
    ResponseEntity<Map<String, Object>> deletePositionName(@RequestParam("type") String type,@PathVariable Long unitId,  @PathVariable Long positionNameId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, positionNameService.deletePositionName(unitId,positionNameId,type));
    }

    @ApiOperation("Get PositionName")
    @GetMapping(value = "/position_name/{positionNameId}")
    ResponseEntity<Map<String, Object>> getPositionName(@RequestParam("type") String type,@PathVariable Long positionNameId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, positionNameService.getPositionName(positionNameId));
    }

    //TODO  fixture org rest call

    @ApiOperation("Get All PositionName")
    @GetMapping(value = "/position_name")
    ResponseEntity<Map<String, Object>> getAllPositionName(@RequestParam("type") String type,@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, positionNameService.getAllPositionName(unitId,type));
    }




}
