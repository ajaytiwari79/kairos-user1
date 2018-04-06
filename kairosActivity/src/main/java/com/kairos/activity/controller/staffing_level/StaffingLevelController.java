package com.kairos.activity.controller.staffing_level;
import com.kairos.activity.constants.ApiConstants;
import com.kairos.activity.persistence.model.staffing_level.StaffingLevel;
import com.kairos.activity.response.dto.staffing_level.StaffingLevelDto;
import com.kairos.activity.service.staffing_level.StaffingLevelService;
import com.kairos.activity.util.Message;
import com.kairos.activity.util.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import static com.kairos.activity.constants.ApiConstants.API_ORGANIZATION_UNIT_URL;

@RestController
@RequestMapping(API_ORGANIZATION_UNIT_URL + "/staffing_level")
@Api(value = API_ORGANIZATION_UNIT_URL + "/staffing_level")
public class StaffingLevelController {

    private Logger logger= LoggerFactory.getLogger(StaffingLevelController.class);
    @Autowired private StaffingLevelService staffingLevelService;


    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ApiOperation("Create staffing_level")
    public ResponseEntity<Map<String, Object>> addStaffingLevel(@RequestBody @Valid StaffingLevelDto staffingLevelDto,
                                                                @PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
                staffingLevelService.createStaffingLevel(staffingLevelDto,unitId));
    }


    /**
     * get staffing level between date and unit.
     * @param unitId
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ApiOperation("getting  staffing_level between date unit wise ")
    public ResponseEntity<Map<String, Object>> getStaffingLevels(@PathVariable Long unitId
    , @RequestParam("startDate")@DateTimeFormat(pattern="yyyy-MM-dd") Date startDate, @RequestParam("endDate")@DateTimeFormat(pattern="yyyy-MM-dd")Date endDate) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                staffingLevelService.getStaffingLevel(unitId,startDate,endDate));
    }


    /**
     * get staffing level by unit and day
     * @param unitId

     * @return
     */
    @RequestMapping(value = "/currentDay", method = RequestMethod.GET)
    @ApiOperation("getting  staffing_level for selected day ")
    public ResponseEntity<Map<String, Object>> getStaffingLevel(@PathVariable Long unitId
            ,@RequestParam("currentDate")Date currentDate) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                staffingLevelService.getStaffingLevel(unitId,currentDate));
    }

    /**
     * get staffing level by Id
     * @param id

     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ApiOperation("update staffing_level")
    public ResponseEntity<Map<String, Object>> getStaffingLevel(@PathVariable BigInteger id) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                staffingLevelService.getStaffingLevel(id));
    }

    @RequestMapping(value = "/{staffingLevelId}", method = RequestMethod.PUT)
    @ApiOperation("update staffing_level")
    public ResponseEntity<Map<String, Object>> updateStaffingLevel(@RequestBody @Valid StaffingLevelDto staffingLevelDto,
        @PathVariable Long unitId,@PathVariable BigInteger staffingLevelId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                staffingLevelService.updateStaffingLevel(staffingLevelId,unitId,staffingLevelDto));
    }


    @RequestMapping(value = "/activity_skills", method = RequestMethod.GET)
    @ApiOperation("Create staffing_level")
    public ResponseEntity<Map<String, Object>> getActivityTypesAndSkillsByUnitId(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                staffingLevelService.getActivityTypesAndSkillsByUnitId(unitId));
    }

    @RequestMapping(value = "/phase_daytype", method = RequestMethod.GET)
    @ApiOperation("getting phase and dayType of selected date")
    public ResponseEntity<Map<String, Object>> getPhaseAndDayTypes(@PathVariable Long unitId,@RequestParam("date")Date date ) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                staffingLevelService.getPhaseAndDayTypesForStaffingLevel(unitId,date));
    }


    @MessageMapping("/staffing_level/graph/{unitId}")
    @SendTo(ApiConstants.API_V1+"/ws/dynamic-push/staffing-level/graph/{unitId}")
    public StaffingLevel dynamicStaffingLevelGraphSyncResponse(@DestinationVariable Long unitId, Message message){

        return  staffingLevelService.getStaffingLevel(unitId,message.getCurrentDate());
    }

    @RequestMapping(value = "/submitShiftPlanningInfoToPlanner", method = RequestMethod.POST)
    @ApiOperation("getting phase and dayType of selected date")
    public ResponseEntity<Map<String, Object>> getShiftPlanningInfo(@PathVariable Long unitId ,@RequestParam(value = "startDate") @DateTimeFormat(pattern="yyyy-MM-dd") Date startDate, @RequestParam(value = "endDate") @DateTimeFormat(pattern="yyyy-MM-dd") Date endDate) {
        staffingLevelService.submitShiftPlanningInfoToPlanner(unitId,startDate,endDate);
        return ResponseHandler.generateResponse(HttpStatus.OK, true,null
                );
    }


    @RequestMapping(method = RequestMethod.PUT, value = "/import_csv")
    @ApiOperation("update staffing level from csv")
    // @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    private ResponseEntity<Map<String, Object>> updateStaffingLevelFromCSV(@RequestParam("file") MultipartFile multipartFile,@PathVariable Long unitId) throws Exception{
        staffingLevelService.processStaffingLevel(multipartFile,unitId);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, true);
    }
}
