package com.kairos.controller.master_data_management.processing_activity_masterdata;


import com.kairos.persistance.model.master_data_management.processing_activity_masterdata.AccessorParty;
import com.kairos.service.master_data_management.processing_activity_masterdata.AccessorPartyService;
import com.kairos.utils.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;
import static com.kairos.constant.ApiConstant.API_ACCESSOR_PARTY_URL;
/*
 *
 *  created by bobby 17/5/2018
 * */



@RestController
@RequestMapping(API_ACCESSOR_PARTY_URL)
@Api(API_ACCESSOR_PARTY_URL)
@CrossOrigin
public class AccessorPartyController {

    @Inject
    private AccessorPartyService accessorPartyService;


    @ApiOperation("add AccessorParty")
    @PostMapping("/add")
    public ResponseEntity<Object> createAccessorParty(@RequestBody List<AccessorParty> accessorParties) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessorPartyService.createAccessorParty(accessorParties));

    }


    @ApiOperation("get AccessorParty by id")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getAccessorParty(@PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id is null");
        }

        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessorPartyService.getAccessorParty(id));

    }


    @ApiOperation("get all AccessorParty ")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllAccessorParty() {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessorPartyService.getAllAccessorParty());

    }

    @ApiOperation("get AccessorParty by name")
    @GetMapping("/")
    public ResponseEntity<Object> getAccessorPartyByName(@RequestParam String name) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessorPartyService.getAccessorPartyByName(name));

    }


    @ApiOperation("delete AccessorParty  by id")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteAccessorParty(@PathVariable BigInteger id) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id is null");
        }

        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessorPartyService.deleteAccessorParty(id));

    }

    @ApiOperation("update AccessorParty by id")
    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateAccessorParty(@PathVariable BigInteger id, @RequestBody AccessorParty accessorParty) {
        if (id == null) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "id is null");
        }

        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessorPartyService.updateAccessorParty(id, accessorParty));

    }


}
