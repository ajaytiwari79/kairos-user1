package com.kairos.controller.payroll;
/*
 *Created By Pavan on 17/12/18
 *
 */

import com.kairos.dto.TranslationInfo;
import com.kairos.dto.activity.payroll.BankDTO;
import com.kairos.dto.activity.payroll.OrganizationBankDetailsDTO;
import com.kairos.dto.activity.payroll.StaffBankAndPensionProviderDetailsDTO;
import com.kairos.service.payroll.BankService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.Map;

import static com.kairos.constants.ApiConstants.*;
import static com.kairos.constants.payroll.PayRollAPIConstants.*;

@RestController
@RequestMapping(API_V1)
public class BankDetailsController {
    @Inject
    private BankService bankService;

    @ApiOperation("Create bank")
    @PostMapping(COUNTRY_URL+BANK)
    public ResponseEntity<Map<String,Object>> createBank(@PathVariable Long countryId,@Valid @RequestBody BankDTO bankDTO){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,bankService.createBank(countryId,bankDTO));
    }

    @ApiOperation("update bank")
    @PutMapping(COUNTRY_URL+UPDATE_BANK)
    public ResponseEntity<Map<String,Object>> updateBank(@PathVariable BigInteger bankId, @Valid @RequestBody BankDTO bankDTO){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,bankService.updateBank(bankId,bankDTO));
    }

    @ApiOperation("delete bank ")
    @DeleteMapping(COUNTRY_URL+DELETE_BANK)
    public ResponseEntity<Map<String,Object>> deleteBank(@PathVariable BigInteger bankId){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,bankService.deleteBank(bankId));
    }

    @ApiOperation("get bank by id")
    @GetMapping(COUNTRY_URL+GET_BANK_BY_ID)
    public ResponseEntity<Map<String,Object>> getBankById(@PathVariable BigInteger bankId){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,bankService.getBankById(bankId));
    }

    @ApiOperation("get All Bank ")
    @GetMapping(COUNTRY_URL+BANK)
    public ResponseEntity<Map<String,Object>> getAllBank(@PathVariable Long countryId){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,bankService.getAllBank(countryId));
    }


    @ApiOperation("get Bank details of Staff")
    @GetMapping(UNIT_URL + STAFF_BANK_DETAILS)
    public ResponseEntity<Map<String,Object>> getBankDetailsOfStaff(@PathVariable Long unitId,@PathVariable Long staffId){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,bankService.getBankDetailsOfStaff(staffId,unitId));
    }


    @ApiOperation("update Bank details of Staff")
    @PutMapping(UNIT_URL+STAFF_BANK_DETAILS)
    public ResponseEntity<Map<String,Object>> updateBankDetailsOfStaff(@PathVariable Long staffId,@RequestBody StaffBankAndPensionProviderDetailsDTO staffBankAndPensionProviderDetailsDTO){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,bankService.linkBankDetailsForStaff(staffId,staffBankAndPensionProviderDetailsDTO));
    }

    @ApiOperation("update Bank details of Organization")
    @PutMapping(UNIT_URL+ORGANIZATION_BANK_DETAILS)
    public ResponseEntity<Map<String,Object>> updateBankDetailsOfOrganization(@PathVariable Long unitId,@RequestBody OrganizationBankDetailsDTO organizationBankDetailsDTO){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,bankService.linkBankDetailsForOrganization(unitId,organizationBankDetailsDTO));
    }

    @ApiOperation("get Bank details of Organization")
    @GetMapping(UNIT_URL+ORGANIZATION_BANK_DETAILS)
    public ResponseEntity<Map<String,Object>> getBankDetailsOfOrganization(@PathVariable Long unitId){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,bankService.getBankDetailsOfOrganization(unitId));
    }

    @ApiOperation("update translation data ")
    @PutMapping(COUNTRY_URL+"/bank_details/{id}/language_settings")
    public ResponseEntity<Map<String,Object>> updatePensionProviderTranslation(@PathVariable BigInteger id, @RequestBody Map<String, TranslationInfo> translations){
        return ResponseHandler.generateResponse(HttpStatus.OK,true,bankService.updateTranslation(id,translations));
    }

}
