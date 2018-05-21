package com.kairos.controller.asset_management;


import com.kairos.persistance.model.asset_management.StorageType;
import com.kairos.service.asset_management.StorageTypeService;
import com.kairos.utils.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;
import static com.kairos.constant.ApiConstant.API_STORAGE_TYPE_URL;



@RestController
@RequestMapping(API_STORAGE_TYPE_URL)
@Api(API_STORAGE_TYPE_URL)
public class StorageTypeController {
    @Inject
    private StorageTypeService storageTypeService;


    @ApiOperation("add StorageType")
    @PostMapping("/add")
    public ResponseEntity<Object> createStorageType(@RequestBody List<StorageType> storageTypes) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.createStorageType(storageTypes));

    }


    @ApiOperation("get StorageFormat by id")
    @GetMapping("/id/{id}")
    public ResponseEntity<Object> getStorageType(@PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id is null");
        }

        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.getStorageType(id));

    }


    @ApiOperation("get all StorageFormat ")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllStorageType() {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.getAllStorageType());

    }


    @ApiOperation("delete StorageFormat  by id")
    @DeleteMapping("/delete/id/{id}")
    public ResponseEntity<Object> deleteStorageType(@PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id is null");
        }

        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.deleteStorageType(id));

    }

    @ApiOperation("update StorageFormat by id")
    @PutMapping("/update/id/{id}")
    public ResponseEntity<Object> updateStorageType(@PathVariable BigInteger id, @RequestBody StorageType storageType) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id is null");
        }

        return ResponseHandler.generateResponse(HttpStatus.OK, true, storageTypeService.updateStorageType(id, storageType));

    }


}
