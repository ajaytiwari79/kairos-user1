package com.kairos.controller.shift;

import com.kairos.dto.activity.shift.CoverShiftDTO;
import com.kairos.dto.activity.shift.CoverShiftSettingDTO;
import com.kairos.dto.activity.shift.StaffInterest;
import com.kairos.persistence.model.shift.CoverShiftSetting;
import com.kairos.service.shift.CoverShiftService;
import com.kairos.service.shift.ShiftService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_UNIT_URL;

@RestController
@RequestMapping(API_UNIT_URL)
@Api(API_UNIT_URL)
public class CoverShiftController {

    @Inject private CoverShiftService coverShiftService;
    @Inject
    private ShiftService shiftService;

    @ApiOperation("get eligible staffs")
    @PostMapping(value = "/get_eligible_staffs")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getEligibleStaffs(@RequestParam BigInteger shiftId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, coverShiftService.getEligibleStaffs(shiftId));
    }

    @ApiOperation("create cover shift setting by unit")
    @PostMapping(value = "/cover_shift_setting")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> createCoverShiftSettingByUnit(@PathVariable Long unitId,@RequestBody CoverShiftSettingDTO coverShiftSettingDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, coverShiftService.createCoverShiftSettingByUnit(unitId,coverShiftSettingDTO));
    }

    @ApiOperation("update cover shift setting by unit")
    @PutMapping(value = "/cover_shift_setting")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateCoverShiftSettingByUnit(@PathVariable Long unitId,@RequestBody CoverShiftSettingDTO coverShiftSettingDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, coverShiftService.updateCoverShiftSettingByUnit(unitId,coverShiftSettingDTO));
    }

    @ApiOperation("get cover shift setting by unit")
    @GetMapping(value = "/cover_shift_setting")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getCoverShiftSettingByUnit(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, coverShiftService.getCoverShiftSettingByUnit(unitId));
    }

    @ApiOperation("get cover shift setting by unit")
    @GetMapping(value = "/cover_shift_details/{shiftId}")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getCoverShiftByShiftId(@PathVariable BigInteger shiftId,@RequestParam("staffId") Long staffId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, coverShiftService.getCoverShiftDetails(shiftId,staffId));
    }

    @ApiOperation("update cover shift setting by unit")
    @PutMapping(value = "/update_cover_shift_details/{shiftId}")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateCoverShiftByShiftId(@PathVariable BigInteger shiftId, @RequestBody CoverShiftDTO coverShiftDTO) {
        coverShiftDTO.setShiftId(shiftId);
        coverShiftService.updateCoverShiftDetails(coverShiftDTO);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, null);
    }

    @ApiOperation("cancel cover shift setting by unit")
    @PutMapping(value = "/cancel_cover_shift/{id}")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> cancelCoverShiftByShiftId(@PathVariable BigInteger id) {
        coverShiftService.cancelCoverShiftDetails(id);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, null);
    }

    @ApiOperation("update cover shift setting by unit")
    @PutMapping(value = "/show_interest/{id}")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateCoverShiftByShiftId(@PathVariable BigInteger id, @RequestParam("staffId") Long staffId, @RequestParam(value = "employmentId",required = false) Long employmentId, @RequestBody StaffInterest staffInterest) {
        coverShiftService.showInterestInCoverShift(id,staffId,employmentId,staffInterest);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, null);
    }

    @ApiOperation("update cover shift setting by unit")
    @PutMapping(value = "/remove_interest/{id}")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> removeInterestInCoverShiftByShiftId(@PathVariable BigInteger id,@RequestParam("staffId") Long staffId,@RequestParam(value = "selectedDate",required = false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate selectedDate,@RequestParam(value = "doNotAddDeclined",required = false) boolean doNotAddDeclined) {
        coverShiftService.notInterestInCoverShift(id,staffId,selectedDate,doNotAddDeclined);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, null);
    }

    @ApiOperation("move  shift to another staff")
    @PutMapping(value = "/cover_shift/{id}/approve_request")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateRemarkInShiftActivity(@PathVariable BigInteger id, @RequestParam Long staffId,@RequestParam Long employmentId) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, coverShiftService.assignCoverShiftToStaff(id, staffId,employmentId));
    }

    @ApiOperation("get details for cover shift")
    @GetMapping(value = "/cover_shift/staff_details/{staffId}")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getcoverShiftStaffDetails(@PathVariable Long staffId, @PathVariable Long unitId, @RequestParam("employmentId") Long employmentId, @RequestParam("startDate") @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate startDate, @RequestParam("endDate") @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate endDate) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, coverShiftService.getCoverShiftStaffDetails(startDate,endDate,unitId,staffId,employmentId));
    }

    @ApiOperation("get details for cover shift")
    @PostMapping(value = "/cover_shift/fetch_shifts")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> findAllShiftsByIds(@RequestBody List<BigInteger> shiftIds) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, shiftService.findAllShiftsByIds(shiftIds));
    }

    @ApiOperation("get wta details for cover shift")
    @GetMapping(value = "/cover_shift/wta_details")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getWTADetails(@RequestParam BigInteger shiftId,@RequestParam Long employmentId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, coverShiftService.getWTADetails(shiftId,employmentId));
    }
}
