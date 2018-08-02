package com.kairos.controller.data_inventory.asset;

import com.kairos.persistance.model.master_data.default_asset_setting.DataDisposal;
import com.kairos.service.data_inventory.asset.OrganizationDataDisposalService;
import com.kairos.utils.ResponseHandler;
import com.kairos.utils.validate_list.ValidateListOfRequestBody;
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

@RestController
@RequestMapping(API_ORGANIZATION_URL_UNIT_URL)
@Api(API_ORGANIZATION_URL_UNIT_URL)
public class OrganizationDataDisposalController {


    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationDataDisposalController.class);

    @Inject
    private OrganizationDataDisposalService dataDisposalService;


    @ApiOperation("add DataDisposal")
    @PostMapping("/data_disposal/add")
    public ResponseEntity<Object> createDataDisposal(@PathVariable Long unitId, @Valid @RequestBody ValidateListOfRequestBody<DataDisposal> dataDisposals) {
        if (unitId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.createDataDisposal(unitId, dataDisposals.getRequestBody()));

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

    @ApiOperation("get DataDisposal by name")
    @GetMapping("/data_disposal/name")
    public ResponseEntity<Object> getDataDisposalByName(@PathVariable Long unitId, @RequestParam String name) {
        if (unitId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.getDataDisposalByName(unitId, name));

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
    public ResponseEntity<Object> updateDataDisposal(@PathVariable Long unitId, @PathVariable BigInteger id, @Valid @RequestBody DataDisposal dataDisposal) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        } else if (unitId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, dataDisposalService.updateDataDisposal(unitId, id, dataDisposal));

    }


}
