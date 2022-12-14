package com.kairos.controller.data_inventory.asset;

import com.kairos.dto.gdpr.metadata.DataDisposalDTO;
import com.kairos.service.data_inventory.asset.OrganizationDataDisposalService;
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

import static com.kairos.constants.ApiConstant.API_ORGANIZATION_UNIT_URL;
import static com.kairos.constants.ApiConstant.COUNTRY_URL;

@RestController
@RequestMapping(API_ORGANIZATION_UNIT_URL)
@Api(API_ORGANIZATION_UNIT_URL)
class OrganizationDataDisposalController {


    @Inject
    private OrganizationDataDisposalService dataDisposalService;


    @ApiOperation("add DataDisposal")
    @PostMapping("/data_disposal")
    public ResponseEntity<Object> createDataDisposal(@PathVariable Long unitId, @Valid @RequestBody ValidateRequestBodyList<DataDisposalDTO> dataDisposalDTOs) {

        if (CollectionUtils.isEmpty(dataDisposalDTOs.getRequestBody())) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, null);
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.createDataDisposal(unitId, dataDisposalDTOs.getRequestBody()));
    }


    @ApiOperation("get DataDisposal by id")
    @GetMapping("/data_disposal/{dataDisposalId}")
    public ResponseEntity<Object> getDataDisposal(@PathVariable Long unitId, @PathVariable Long dataDisposalId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.getDataDisposalById(unitId, dataDisposalId));
    }


    @ApiOperation("get all DataDisposal ")
    @GetMapping("/data_disposal")
    public ResponseEntity<Object> getAllDataDisposal(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.getAllDataDisposal(unitId));
    }

    @ApiOperation("delete data disposal by id")
    @DeleteMapping("/data_disposal/{dataDisposalId}")
    public ResponseEntity<Object> deleteDataDisposal(@PathVariable Long unitId, @PathVariable Long dataDisposalId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.deleteDataDisposalById(unitId, dataDisposalId));
    }

    @ApiOperation("update DataDisposal by id")
    @PutMapping("/data_disposal/{dataDisposalId}")
    public ResponseEntity<Object> updateDataDisposal(@PathVariable Long unitId, @PathVariable Long dataDisposalId, @Valid @RequestBody DataDisposalDTO dataDisposalDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.updateDataDisposal(unitId, dataDisposalId, dataDisposalDTO));

    }


    @ApiOperation("save Data Disposal And Suggest To Country admin")
    @PostMapping(COUNTRY_URL + "/data_disposal/suggest")
    public ResponseEntity<Object> saveDataDisposalAndSuggestToCountryAdmin(@PathVariable Long countryId, @PathVariable Long unitId, @Valid @RequestBody ValidateRequestBodyList<DataDisposalDTO> dataDisposalDTOs) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.saveAndSuggestDataDisposal(countryId, unitId, dataDisposalDTOs.getRequestBody()));

    }

}
