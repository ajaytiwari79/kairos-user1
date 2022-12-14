package com.kairos.controller.staff;

import com.kairos.dto.user.staff.staff.StaffTeamRankingDTO;
import com.kairos.service.staff.StaffTeamRankingService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_ORGANIZATION_UNIT_URL;

@RestController
@RequestMapping(API_ORGANIZATION_UNIT_URL)
@Api(value = API_ORGANIZATION_UNIT_URL)
public class StaffTeamRankingController {
    @Inject private StaffTeamRankingService staffTeamRankingService;

    @ApiOperation(value = "update a staff_team_ranking")
    @PutMapping(value = "/staff_team_ranking")
    public ResponseEntity<Map<String, Object>> updateStaffTeamRanking(@RequestBody @Valid StaffTeamRankingDTO staffTeamRankingDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, staffTeamRankingService.updateStaffTeamRanking(staffTeamRankingDTO));
    }

    @ApiOperation(value = "published a staff_team_ranking")
    @PutMapping(value =  "/staff/{staffId}/staff_team_ranking/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishStaffTeamRanking(@PathVariable Long id, @RequestParam("publishedDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate publishedDate) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, staffTeamRankingService.publishStaffTeamRanking(id, publishedDate));
    }

    @ApiOperation(value = "delete staff_team_ranking")
    @DeleteMapping(value = "/staff_team_ranking/{id}")
    public ResponseEntity<Map<String, Object>> deleteStaffTeamRanking(@PathVariable Long id) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, staffTeamRankingService.deleteStaffTeamRanking(id));
    }

    @ApiOperation("Get Staff Personalized team ranking")
    @GetMapping("/staff/{staffId}/staff_team_ranking")
    //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getStaffTeamRankings(@PathVariable Long unitId, @PathVariable Long staffId, @RequestParam(value = "includeDraft", required = false) boolean includeDraft) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, staffTeamRankingService.getStaffTeamRankings(unitId, staffId, includeDraft));
    }

    //For create staff team ranging manuale of all staff by unit
    @ApiOperation("Create Staff Personalized team ranking")
    @PostMapping("/create_staff_team_ranking")
    public ResponseEntity<Map<String, Object>> createAllStaffTeamRankingIfNotCreated(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, staffTeamRankingService.createAllStaffTeamRankingIfNotCreated(unitId));
    }

}
