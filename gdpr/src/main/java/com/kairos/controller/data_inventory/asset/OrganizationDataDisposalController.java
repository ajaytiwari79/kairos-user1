package com.kairos.controller.data_inventory.asset;

import com.kairos.gdpr.metadata.DataDisposalDTO;
import com.kairos.service.data_inventory.asset.OrganizationDataDisposalService;
import com.kairos.utils.ResponseHandler;
import com.kairos.utils.ValidateRequestBodyList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.math.BigInteger;

import static com.kairos.constants.ApiConstant.API_ORGANIZATION_URL_UNIT_URL;
import static com.kairos.constants.ApiConstant.COUNTRY_URL;

@RestController
@RequestMapping(API_ORGANIZATION_URL_UNIT_URL)
@Api(API_ORGANIZATION_URL_UNIT_URL)
public class OrganizationDataDisposalController {


    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationDataDisposalController.class);

    @Inject
    private OrganizationDataDisposalService dataDisposalService;


    @ApiOperation("add DataDisposal")
    @PostMapping("/data_disposal/add")
    public ResponseEntity<Object> createDataDisposal(@PathVariable Long unitId, @Valid @RequestBody ValidateRequestBodyList<DataDisposalDTO> dataDisposalDTOs) {
        if (unitId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.createDataDisposal(unitId, dataDisposalDTOs.getRequestBody()));

    }


    @ApiOperation("get DataDisposal by id")
    @GetMapping("/data_disposal/{id}")
    public ResponseEntity<Object> getDataDisposal(@PathVariable Long unitId, @PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        } else if (unitId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.getDataDisposalById(unitId, id));

    }


    @ApiOperation("get all DataDisposal ")
    @GetMapping("/data_disposal/all")
    public ResponseEntity<Object> getAllDataDisposal(@PathVariable Long unitId) {
        if (unitId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");

        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.getAllDataDisposal(unitId));

    }

    @ApiOperation("delete data disposal by id")
    @DeleteMapping("/data_disposal/delete/{id}")
    public ResponseEntity<Object> deleteDataDisposal(@PathVariable Long unitId, @PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        } else if (unitId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");

        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.deleteDataDisposalById(unitId, id));

    }

    @ApiOperation("update DataDisposal by id")
    @PutMapping("/data_disposal/update/{id}")
    public ResponseEntity<Object> updateDataDisposal(@PathVariable Long unitId, @PathVariable BigInteger id, @Valid @RequestBody DataDisposalDTO dataDisposalDTO) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        } else if (unitId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.updateDataDisposal(unitId, id, dataDisposalDTO));

    }


    @ApiOperation("save Data Disposal And Suggest To Country admin")
    @PostMapping(COUNTRY_URL + "/data_disposal")
    public ResponseEntity<Object> saveDataDisposalAndSuggestToCountryAdmin(@PathVariable Long countryId, @PathVariable Long unitId, @Valid @RequestBody ValidateRequestBodyList<DataDisposalDTO> dataDisposalDTOs) {
        if (unitId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.saveAndSuggestDataDisposal(countryId, unitId, dataDisposalDTOs.getRequestBody()));

    }

}
