package com.kairos.controller.data_inventory.assessment;


import com.kairos.dto.gdpr.assessment.AssessmentTypeRiskDTO;
import com.kairos.dto.response.ResponseDTO;
import com.kairos.enums.gdpr.AssessmentStatus;
import com.kairos.dto.gdpr.assessment.AssessmentDTO;
import com.kairos.persistence.model.data_inventory.assessment.AssessmentAnswerValueObject;
import com.kairos.response.dto.common.AssessmentResponseDTO;
import com.kairos.response.dto.master_data.questionnaire_template.QuestionnaireSectionResponseDTO;
import com.kairos.service.data_inventory.assessment.AssessmentService;
import com.kairos.utils.ResponseHandler;
import com.kairos.utils.ValidateRequestBodyList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static com.kairos.constants.ApiConstant.API_ORGANIZATION_UNIT_URL;


@RestController
@RequestMapping(API_ORGANIZATION_UNIT_URL)
@Api(API_ORGANIZATION_UNIT_URL)
public class AssessmentController {


    @Inject
    private AssessmentService assessmentService;


    @ApiOperation(value = "launch assessment for Asset")
    @PostMapping( "/assessment/asset/{assetId}")
    public ResponseEntity<ResponseDTO<AssessmentDTO>> launchAssessmentForAsset(@PathVariable Long unitId, @PathVariable BigInteger assetId, @RequestBody @Valid AssessmentDTO assessmentDTO) {
        return ResponseHandler.generateResponseDTO(HttpStatus.OK, true, assessmentService.saveAssessmentForAsset(unitId,  assetId, assessmentDTO));

    }


    @ApiOperation(value = "launch assessment for processing activity")
    @PostMapping( "/assessment/processing_activity/{processingActivityId}")
    public ResponseEntity<ResponseDTO<AssessmentDTO>> launchAssessmentForProcessingActivity(@PathVariable Long unitId, @PathVariable BigInteger processingActivityId, @RequestBody @Valid AssessmentDTO assessmentDTO) {
        return ResponseHandler.generateResponseDTO(HttpStatus.OK, true, assessmentService.saveAssessmentForProcessingActivity(unitId,  processingActivityId, assessmentDTO));

    }

    @ApiOperation(value = "launch risk  assessment for asset")
    @PostMapping( "/assessment/asset/{assetId}/risk")
    public ResponseEntity<ResponseDTO<AssessmentTypeRiskDTO>> launchRiskAssessmentForAsset(@PathVariable Long unitId, @PathVariable BigInteger assetId, @RequestBody @Valid AssessmentTypeRiskDTO assessmentDTO) {
        return ResponseHandler.generateResponseDTO(HttpStatus.OK, true, assessmentService.launchAssetRiskAssessment(unitId,  assetId, assessmentDTO));

    }

    @ApiOperation(value = "launch risk assessment for processing activity")
    @PostMapping( "/assessment/processing_activity/{processingActivityId}/risk")
    public ResponseEntity<ResponseDTO<AssessmentTypeRiskDTO>> launchRiskAssessmentForProcessingActivity(@PathVariable Long unitId, @PathVariable BigInteger processingActivityId, @RequestBody @Valid AssessmentTypeRiskDTO assessmentDTO) {
        return ResponseHandler.generateResponseDTO(HttpStatus.OK, true, assessmentService.launchProcessingActivityRiskAssessment(unitId,  processingActivityId, assessmentDTO));

    }

    @ApiOperation(value = "get Assessment  By Id")
    @GetMapping( "/assessment/{assessmentId}")
    public ResponseEntity<ResponseDTO<List<QuestionnaireSectionResponseDTO>>> getAssetAssessmentById(@PathVariable Long unitId, @PathVariable BigInteger assessmentId) {
        return ResponseHandler.generateResponseDTO(HttpStatus.OK, true, assessmentService.getAssessmentById( unitId, assessmentId));
    }


    @ApiOperation(value = "get All launched Assessment Assign to respondent and are in New and InProgress state")
    @GetMapping("/assessment/assignee")
    public ResponseEntity<Object> getAllLaunchedAssessment(@PathVariable Long unitId,@RequestParam Long loggedInUserId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, assessmentService.getAllLaunchedAssessmentOfAssignee(unitId,loggedInUserId));
    }

    @ApiOperation(value = "get All Assessment of unit")
    @GetMapping("/assessment")
    public ResponseEntity<ResponseDTO<List<AssessmentResponseDTO>>> getAllAssessmentByUnitId(@PathVariable Long unitId) {
        return ResponseHandler.generateResponseDTO(HttpStatus.OK, true, assessmentService.getAllAssessmentByUnitId(unitId));
    }

    @ApiOperation(value = "delete Assessment by id")
    @DeleteMapping("/assessment/{assessmentId}")
    public ResponseEntity<ResponseDTO<Boolean>> deleteAssessment(@PathVariable Long unitId,@PathVariable BigInteger assessmentId) {
        return ResponseHandler.generateResponseDTO(HttpStatus.OK, true, assessmentService.deleteAssessmentbyId(unitId,assessmentId));
    }


    @ApiOperation(value = "save answer of assessment question In progress state by  Assignee")
    @PutMapping("/assessment/{assessmentId}")
    public ResponseEntity<Object> saveAssessmentAnswerForAssetOrProcessingActivity(@PathVariable Long unitId, @PathVariable BigInteger assessmentId, @Valid @RequestBody ValidateRequestBodyList<AssessmentAnswerValueObject> assessmentAnswerValueObjects ,@RequestParam AssessmentStatus status) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, assessmentService.addAssessmentAnswerForAssetOrProcessingActivityToAssessment(unitId, assessmentId, assessmentAnswerValueObjects.getRequestBody(),status));
    }


    @ApiOperation(value = "Change Assessment status")
    @PutMapping("/assessment/{assessmentId}/status")
    public ResponseEntity<Object> changeAssessmentStatusKanbanView(@PathVariable Long unitId, @PathVariable BigInteger assessmentId, @RequestParam(value = "assessmentStatus",required = true) AssessmentStatus assessmentStatus) {
        if (!Optional.ofNullable(assessmentStatus).isPresent())
        {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "Assessment Status "+assessmentStatus+" is invalid");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, assessmentService.updateAssessmentStatus(unitId, assessmentId,assessmentStatus));
    }



}
