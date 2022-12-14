package com.planner.controller;

import com.kairos.dto.planner.shift_planning.ShiftPlanningProblemSubmitDTO;
import com.planner.commonUtil.ResponseHandler;
import com.planner.service.shift_planning.ShiftPlanningInitializationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Map;

@RestController
@RequestMapping("/opta")
public class ShiftPlanningInitializationController {
    @Inject
    private ShiftPlanningInitializationService shiftPlanningInitializationService;


    @PostMapping(value = "/shiftPlanningInitialization")
    ResponseEntity<Map<String, Object>> initializeShiftPlanning(@RequestBody ShiftPlanningProblemSubmitDTO shiftPlanningProblemSubmitDTO) {
        shiftPlanningInitializationService.initializeShiftPlanning(shiftPlanningProblemSubmitDTO);

        return ResponseHandler.generateResponse(" Data fetched sucessFully", HttpStatus.OK);
    }
}
