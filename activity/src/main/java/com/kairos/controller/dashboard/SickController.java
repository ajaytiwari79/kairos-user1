package com.kairos.controller.dashboard;

import com.kairos.service.dashboard.SickService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_ORGANIZATION_URL;
import static com.kairos.constants.ApiConstants.UNIT_URL;

/**
 * CreatedBy vipulpandey on 30/8/18
 **/
@RestController
@Api(API_ORGANIZATION_URL)
@RequestMapping(API_ORGANIZATION_URL)
public class SickController {

    @Inject
    private SickService sickService;

    @ApiOperation("API is used to call the user as sick")
    @GetMapping("/sick")
    public ResponseEntity<Map<String, Object>> markUserAsSick(@RequestParam(value = "unitId", required = false) Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, sickService.markUserAsSick(unitId));
    }

    @ApiOperation("API is used to call the user as fine")
    @GetMapping("/fine")
    public ResponseEntity<Map<String, Object>> markUserAsFine(@RequestParam(value = "unitId", required = false) Long unitId, @RequestParam(value = "staffId") Long staffId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, sickService.markUserAsFine(staffId, unitId));
    }

    @ApiOperation("API is used to check sick users ")
    @PutMapping(UNIT_URL+"/check_user_health")
    public ResponseEntity<Map<String, Object>> checkStatusOfUserAndUpdateStatus(@RequestParam Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, sickService.checkStatusOfUserAndUpdateStatus(unitId));
    }


}
