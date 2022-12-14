package com.kairos.controller.template_type;

import com.kairos.dto.gdpr.master_data.TemplateTypeDTO;
import com.kairos.service.template_type.TemplateTypeService;
import com.kairos.utils.ResponseHandler;
import com.kairos.utils.ValidateRequestBodyList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;

import static com.kairos.constants.ApiConstant.API_V1;
import static com.kairos.constants.ApiConstant.COUNTRY_URL;

@RestController
@Api(API_V1)
@RequestMapping(API_V1)
class TemplateTypeController {


    @Inject
    private TemplateTypeService templateTypeService;


    /**
     * @param countryId
     * @param templateData
     * @return list
     * @description Create template type. Create form will have only name field. We can create multiple template type in one go.
     * @author
     */

    @ApiOperation(value = "create new Template type")
    @PostMapping(COUNTRY_URL+"/template")
    public ResponseEntity<Object> createTemplateType(@PathVariable Long countryId, @Valid @RequestBody ValidateRequestBodyList<TemplateTypeDTO> templateData) {
        if (CollectionUtils.isEmpty(templateData.getRequestBody())) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, null);
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, templateTypeService.createTemplateType(countryId, templateData.getRequestBody()));
    }


    /**
     * @param id
     * @param countryId
     * @param templateType
     * @return TemplateType
     * @description this template is used for update template type by id.
     * @author
     */
    @ApiOperation(value = "update template")
    @PutMapping(value = COUNTRY_URL+"/template/{id}")
    public ResponseEntity<Object> updateTemplate(@PathVariable Long id, @PathVariable Long countryId, @Valid @RequestBody TemplateTypeDTO templateType) {

        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id parameter is null or empty");

        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, templateTypeService.updateTemplateName(id, countryId, templateType));
    }

    /**
     * @param countryId
     * @param id
     * @description this template is used for delete template by id.
     * @author
     * @returne Boolean
     */
    @ApiOperation(value = "delete template by id")
    @DeleteMapping(value =COUNTRY_URL+"/template/{id}")
    public ResponseEntity<Object> deleteTemplateType(@PathVariable Long countryId, @PathVariable Long id) {
        if (id == null) {
            return ResponseHandler.generateResponse(HttpStatus.BAD_GATEWAY, false, "id cannot be null");
        } else
            return ResponseHandler.generateResponse(HttpStatus.OK, true, templateTypeService.deleteTemplateType(id, countryId));

    }

    /**
     * @param countryId
     * @return List<TemplateType>
     * @description this template is used for get all template type
     * @author
     */
    @ApiOperation(value = "All Template Type type ")
    @GetMapping(value = COUNTRY_URL+"/template")
    public ResponseEntity<Object> getAllTemplateType(@PathVariable Long countryId) {
        if (countryId != null) {
            return ResponseHandler.generateResponse(HttpStatus.OK, true, templateTypeService.getAllTemplateType(countryId));
        } else {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can not be empty");

        }


    }
}
