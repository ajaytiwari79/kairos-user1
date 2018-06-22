package com.kairos.controller.master_data_management.asset_management;


import com.kairos.persistance.model.master_data_management.asset_management.HostingProvider;
import com.kairos.service.master_data_management.asset_management.HostingProviderService;
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

import static com.kairos.constants.ApiConstant.API_HOSTING_PROVIDER_URL;
/*
 *
 *  created by bobby 17/5/2018
 * */


@RestController
@RequestMapping(API_HOSTING_PROVIDER_URL)
@Api(API_HOSTING_PROVIDER_URL)
public class HostingProviderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostingProviderController.class);

    @Inject
    private HostingProviderService hostingProviderService;


    @ApiOperation("add HostingProvider")
    @PostMapping("/add")
    public ResponseEntity<Object> createHostingProvider(@PathVariable Long countryId, @Valid @RequestBody ValidateListOfRequestBody<HostingProvider> hostingProviders) {
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id is null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, hostingProviderService.createHostingProviders(countryId, hostingProviders.getRequestBody()));

    }


    @ApiOperation("get HostingProvider by id")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getHostingProvider(@PathVariable Long countryId, @PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id is null");
        } else if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id is null");

        } else
            return ResponseHandler.generateResponse(HttpStatus.OK, true, hostingProviderService.getHostingProviderById(countryId, id));

    }


    @ApiOperation("get all HostingProvider ")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllHostingProvider() {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, hostingProviderService.getAllHostingProvider());

    }

    @ApiOperation("get hosting provider by name ")
    @GetMapping("/name")
    public ResponseEntity<Object> getHostingProviderByName(@PathVariable Long countryId, @RequestParam String name) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, hostingProviderService.getHostingProviderByName(countryId, name));

    }

    @ApiOperation("delete HostingProvider  by id")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteHostingProvider(@PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id is null");
        }

        return ResponseHandler.generateResponse(HttpStatus.OK, true, hostingProviderService.deleteHostingProvider(id));

    }

    @ApiOperation("update HostingProvider by id")
    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateHostingProvider(@PathVariable BigInteger id, @Valid @RequestBody HostingProvider hostingProvider) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id is null");
        }

        return ResponseHandler.generateResponse(HttpStatus.OK, true, hostingProviderService.updateHostingProvider(id, hostingProvider));

    }


}
