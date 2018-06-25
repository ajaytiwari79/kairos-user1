package com.kairos.controller.master_data_management.asset_management;


import com.kairos.dto.master_data.AssetTypeDTO;
import com.kairos.persistance.model.master_data_management.asset_management.AssetType;
import com.kairos.service.master_data_management.asset_management.AssetTypeService;
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

import static com.kairos.constants.ApiConstant.API_STORAGE_TYPE_URL;
/*
 *
 *  created by bobby 17/5/2018
 * */


@RestController
@RequestMapping(API_STORAGE_TYPE_URL)
@Api(API_STORAGE_TYPE_URL)
public class AssetTypeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetTypeController.class);

    @Inject
    private AssetTypeService storageTypeService;


    @ApiOperation("add AssetType")
    @PostMapping("/add")
    public ResponseEntity<Object> createStorageType(@PathVariable Long countryId, @PathVariable Long organizationId, @Valid @RequestBody ValidateListOfRequestBody<AssetType> assetTypes) {
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        } else if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.createAssetType(countryId, organizationId, assetTypes.getRequestBody()));

    }


    @ApiOperation("get AssetType by id")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getStorageType(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        }
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        }
        if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.getAssetType(countryId, organizationId, id));

    }


    @ApiOperation("get all AssetType ")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllStorageType(@PathVariable Long countryId, @PathVariable Long organizationId) {
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        }
        if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.getAllAssetType(countryId, organizationId));

    }


    @ApiOperation("get AssetType by name")
    @GetMapping("/name")
    public ResponseEntity<Object> getStorageTypeByName(@PathVariable Long countryId, @PathVariable Long organizationId, @RequestParam String name) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.getAssetTypeByName(countryId, organizationId, name));

    }


    @ApiOperation("delete AssetType  by id")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteStorageType(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        }
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        }
        if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");

        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.deleteAssetType(countryId, organizationId, id));

    }

    @ApiOperation("update subAsset by id")
    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateStorageType(@PathVariable Long countryId, @PathVariable Long organizationId, @PathVariable BigInteger id, @Valid @RequestBody AssetType assetType) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id cannot be null");
        }
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        }
        if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");

        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.updateAssetType(countryId, organizationId, id, assetType));

    }


    @ApiOperation("update subAsset by id")
    @PostMapping("/subAsset/add")
    public ResponseEntity<Object> addSubAssetTypes(@PathVariable Long countryId, @PathVariable Long organizationId, @Valid @RequestBody AssetTypeDTO assetTypeDto) {
        if (countryId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "country id can't be null");
        } else if (organizationId == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "organization id can't be null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.createAssetTypeAndAddSubAssetTypes(countryId, organizationId, assetTypeDto));
    }


}
