package com.kairos.controller.data_inventory.asset;


import com.kairos.dto.gdpr.data_inventory.AssetTypeOrganizationLevelDTO;
import com.kairos.dto.gdpr.metadata.AssetTypeBasicDTO;
import com.kairos.service.data_inventory.asset.OrganizationAssetTypeService;
import com.kairos.utils.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
class OrganizationAssetTypeController {


    @Inject
    private OrganizationAssetTypeService organizationAssetTypeService;


    @ApiOperation("add AssetType")
    @PostMapping("/asset_type")
    public ResponseEntity<Object> createAssetType(@PathVariable Long unitId, @Valid @RequestBody AssetTypeOrganizationLevelDTO assetTypeDTO) {

        return ResponseHandler.generateResponse(HttpStatus.OK, true, organizationAssetTypeService.createAssetTypeAndAddSubAssetTypes(unitId, assetTypeDTO));

    }


    @ApiOperation("get AssetType by id")
    @GetMapping("/asset_type/{assetTypeId}")
    public ResponseEntity<Object> getAssetType(@PathVariable Long unitId, @PathVariable Long assetTypeId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, organizationAssetTypeService.getAssetTypeById(unitId, assetTypeId));

    }


    @ApiOperation("get all AssetType with risk")
    @GetMapping("/asset_type")
    public ResponseEntity<Object> getAllAssetType(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, organizationAssetTypeService.getAllAssetType(unitId));

    }


    @ApiOperation("delete Asset Type by Id")
    @DeleteMapping("/asset_type/{assetTypeId}")
    public ResponseEntity<Object> deleteAssetTypeById(@PathVariable Long unitId, @PathVariable Long assetTypeId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, organizationAssetTypeService.deleteAssetTypeById(unitId, assetTypeId));

    }


    @ApiOperation("delete Asset Sub type by Id")
    @DeleteMapping("/asset_type/{assetTypeId}/sub_asset_type/{subAssetTypeId}")
    public ResponseEntity<Object> deleteAssetSubTypeById(@PathVariable Long unitId, @PathVariable Long assetTypeId, @PathVariable Long subAssetTypeId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, organizationAssetTypeService.deleteAssetSubTypeById(unitId, assetTypeId, subAssetTypeId));

    }

    @ApiOperation("unlink RISK From Asset Type and delete risk")
    @DeleteMapping("/asset_type/{assetTypeId}/risk/{riskId}")
    public ResponseEntity<Object> unlinkRiskFromAssetType(@PathVariable Long unitId, @PathVariable Long assetTypeId, @PathVariable Long riskId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, organizationAssetTypeService.unlinkRiskFromAssetTypeOrSubAssetTypeAndDeletedRisk(unitId, assetTypeId, riskId));

    }


    @ApiOperation("unlink RISK From Sub Asset Type and delete risk")
    @DeleteMapping("/asset_type/sub_asset_type/{subAssetTypeId}/risk/{riskId}")
    public ResponseEntity<Object> unlinkRiskFromSubAssetType(@PathVariable Long unitId, @PathVariable Long subAssetTypeId, @PathVariable Long riskId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, organizationAssetTypeService.unlinkRiskFromAssetTypeOrSubAssetTypeAndDeletedRisk(unitId, subAssetTypeId, riskId));

    }


    @ApiOperation("update Asset  type and Sub Asset type by id")
    @PutMapping("/asset_type/{assetTypeId}")
    public ResponseEntity<Object> updateAssetType(@PathVariable Long unitId, @PathVariable Long assetTypeId, @Valid @RequestBody AssetTypeOrganizationLevelDTO assetTypeDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, organizationAssetTypeService.updateAssetTypeAndSubAssetsAndAddRisks(unitId, assetTypeId, assetTypeDTO));

    }


    @ApiOperation("save and suggest AssetType to country admin ")
    @PostMapping(COUNTRY_URL+"/asset_type/suggest")
    public ResponseEntity<Object> saveAssetTypeAndSubAssetTypeAndSuggestToCountryAdmin(@PathVariable Long unitId, @PathVariable Long countryId,@Valid @RequestBody AssetTypeBasicDTO assetTypeDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, organizationAssetTypeService.saveAndSuggestAssetTypeAndSubAssetTypeToCountryAdmin(unitId,countryId,assetTypeDTO));

    }


}
