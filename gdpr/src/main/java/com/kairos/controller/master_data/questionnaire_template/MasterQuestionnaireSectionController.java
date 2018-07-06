package com.kairos.controller.master_data.questionnaire_template;

import com.kairos.dto.master_data.MasterQuestionnaireSectionDTO;
import com.kairos.service.master_data.questionnaire_template.MasterQuestionnaireSectionService;
import com.kairos.utils.ResponseHandler;
import com.kairos.utils.validate_list.ValidateListOfRequestBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.math.BigInteger;

import static com.kairos.constants.ApiConstant.API_ORGANIZATION_URL;


@RestController
@RequestMapping(API_ORGANIZATION_URL)
@Api(API_ORGANIZATION_URL)
public class MasterQuestionnaireSectionController {


    @Inject
    private MasterQuestionnaireSectionService masterQuestionnaireSectionService;


    /**
     * @param countryId
     * @param templateId id of MAsterQuestionnaireTemplate
     * @param questionnaireSectionsDto
     * @return  master questionnaire template with questionniare sections
     */
    @ApiOperation(value = "create and add questionnaire section to questionnaire template ")
    @PostMapping("/questionnaire_template/{templateId}/section")
    public ResponseEntity<Object> addMasterQuestionnaireSectionToQuestionnaireTemplate(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable BigInteger templateId, @Valid @RequestBody ValidateListOfRequestBody<MasterQuestionnaireSectionDTO> questionnaireSectionsDto) {
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        } else if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, masterQuestionnaireSectionService.addMasterQuestionnaireSectionToQuestionnaireTemplate(countryId, organizationId, templateId, questionnaireSectionsDto.getRequestBody()));


    }


    /**@param countryId
     * @param organizationId
     * @param templateId id of MasterQUestionnaireTemplate
     * @param questionnaireSectionDTOs contains list of existing MasterQusetionniareSection list and new  MasterQusetionniareSection
     * @return return MasterQUestionnaireTemplate object with QuestionnaireSection
     */
    @ApiOperation(value = "update list of questionnaire section and deleted section if deleted property is true")
    @PutMapping("/questionnaire_template/{templateId}/section/update")
    public ResponseEntity updateQuestionnaireSectionAndQuestions(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable BigInteger templateId, @Valid @RequestBody ValidateListOfRequestBody<MasterQuestionnaireSectionDTO> questionnaireSectionDTOs) {
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        } else if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, masterQuestionnaireSectionService.updateExistingQuestionnaireSectionsAndCreateNewSectionsWithQuestions(countryId, organizationId, templateId, questionnaireSectionDTOs.getRequestBody()));


    }

    /**
     *
     * @param countryId
     * @param organizationId
     * @param id id of MasterQuestionnaireSection
     * @return true with responseEntity on deletion
     */
    @ApiOperation("delete questionnaire section by id ")
    @DeleteMapping("/questionnaire_template/section/{id}")
    public ResponseEntity<Object> deleteMasterQuestionnaireSection(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        }
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        }
        if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, masterQuestionnaireSectionService.deletedQuestionnaireSection(countryId, organizationId, id));
    }


}
