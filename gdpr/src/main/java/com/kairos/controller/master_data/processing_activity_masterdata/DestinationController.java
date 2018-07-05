package com.kairos.controller.master_data.processing_activity_masterdata;

import com.kairos.persistance.model.master_data.processing_activity_masterdata.Destination;
import com.kairos.service.master_data.processing_activity_masterdata.DestinationService;
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

import static com.kairos.constants.ApiConstant.COUNTRY_URL;
import static com.kairos.constants.ApiConstant.API_ORGANIZATION_URL;

/*
 *
 *  created by bobby 20/5/2018
 * */


@RestController
@RequestMapping(API_ORGANIZATION_URL)
@Api(API_ORGANIZATION_URL)
public class DestinationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DestinationController.class);

    @Inject
    private DestinationService destinationService;


    @ApiOperation("add Destination")
    @PostMapping(COUNTRY_URL+"/destination/add")
    public ResponseEntity<Object> createDestination(@PathVariable Long countryId, @PathVariable Long organizationId, @Valid @RequestBody ValidateListOfRequestBody<Destination> destinations) {
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        } else if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");

        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, destinationService.createDestination(countryId, organizationId, destinations.getRequestBody()));

    }


    @ApiOperation("get Destination by id")
    @GetMapping(COUNTRY_URL+"/destination/{id}")
    public ResponseEntity<Object> getDestination(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        }
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        }
        if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, destinationService.getDestination(countryId, organizationId, id));

    }


    @ApiOperation("get all Destination")
    @GetMapping(COUNTRY_URL+"/destination/all")
    public ResponseEntity<Object> getAllDestination(@PathVariable Long countryId, @PathVariable Long organizationId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, destinationService.getAllDestination(countryId, organizationId));

    }

    @ApiOperation("get Destination by name")
    @GetMapping(COUNTRY_URL+"/destination/name")
    public ResponseEntity<Object> getDestinationByName(@PathVariable Long countryId, @PathVariable Long organizationId, @RequestParam String name) {
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        } else if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, destinationService.getDestinationByName(countryId, organizationId, name));

    }


    @ApiOperation("delete Destination by id")
    @DeleteMapping(COUNTRY_URL+"/destination/delete/{id}")
    public ResponseEntity<Object> deleteDestination(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        }
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        }
        if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, destinationService.deleteDestination(countryId, organizationId, id));

    }

    @ApiOperation("update Destination by id")
    @PutMapping(COUNTRY_URL+"/destination/update/{id}")
    public ResponseEntity<Object> updateDestination(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable BigInteger id, @Valid @RequestBody Destination destination) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        }
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        }
        if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, destinationService.updateDestination(countryId, organizationId, id, destination));

    }


}
