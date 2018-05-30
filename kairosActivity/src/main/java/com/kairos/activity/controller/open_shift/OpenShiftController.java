package com.kairos.activity.controller.open_shift;

import com.kairos.activity.service.open_shift.OpenShiftService;
import com.kairos.activity.util.response.ResponseHandler;
import com.kairos.response.dto.web.open_shift.OpenShiftResponseDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.Map;
import java.util.List;
import static com.kairos.activity.constants.ApiConstants.OPEN_SHIFT_URL;


@RestController
@Api(OPEN_SHIFT_URL)
@RequestMapping(OPEN_SHIFT_URL)
public class OpenShiftController {

    @Inject
    OpenShiftService openShiftService;


    @RequestMapping(value = "", method = RequestMethod.POST)
    @ApiOperation("create openshifts")
    public ResponseEntity<Map<String, Object>> createOpenShift(@RequestBody OpenShiftResponseDTO openShiftResponseDTO)  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,openShiftService.createOpenShift(openShiftResponseDTO));
    }

   /* @RequestMapping(value = "/{openShiftId}", method = RequestMethod.PUT)
    @ApiOperation("update openShift")
    public ResponseEntity<Map<String, Object>> updateOpenShift(@PathVariable BigInteger openShiftId, @RequestBody OpenShiftResponseDTO openShiftResponseDTO)  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,openShiftService.updateOpenShift(openShiftResponseDTO,openShiftId));
    }*/
    @RequestMapping(value = "", method = RequestMethod.PUT)
    @ApiOperation("update openShift")
    public ResponseEntity<Map<String, Object>> updateOpenShift(@PathVariable BigInteger openShiftId, @PathVariable BigInteger orderId, @RequestBody List<OpenShiftResponseDTO> openShiftResponseDTOs)  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,openShiftService.updateOpenShift(openShiftResponseDTOs,orderId));
    }

    @ApiOperation("delete an openshift")
    @DeleteMapping(value = "/{openshiftId}")
    public ResponseEntity<Map<String, Object>> deleteOpenShift(@PathVariable BigInteger openShiftId) {
        openShiftService.deleteOpenShift(openShiftId);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, true);
    }

    @ApiOperation(value = "Get All openshifts by order and unitId")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getAllOpenShiftsByUnitId(@PathVariable Long unitId,@PathVariable BigInteger orderId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, openShiftService.getOpenshiftsByUnitIdAndOrderId(unitId,orderId));
    }

    @ApiOperation(value = "Pick open Shift by staff")
    @RequestMapping(value = "/{openShiftId}/staff/{staffId}", method = RequestMethod.GET)
    // @PreAuthorize("@customPermissionEvaluator.isAuthorized()")

    public ResponseEntity<Map<String, Object>> pickOpenShiftByStaff(@PathVariable Long unitId, @PathVariable BigInteger openShiftId, @PathVariable Long staffId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, openShiftService.pickOpenShiftByStaff(unitId,openShiftId,staffId));
        }
}
