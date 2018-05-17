package com.kairos.controller.clause;


import com.kairos.persistance.model.clause.AccountType;
import com.kairos.service.clause.AccountTypeService;
import com.kairos.utils.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static com.kairos.constant.ApiConstant.API_ACCOUNT_TYPE_URL;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@RestController
@Api(API_ACCOUNT_TYPE_URL)
@RequestMapping(API_ACCOUNT_TYPE_URL)
@CrossOrigin
public class AccountTypeController {


    @Inject
    private AccountTypeService accountTypeService;


    @ApiOperation(value ="create new account type" )
    @PostMapping("/add_account")
    public ResponseEntity<Object> createAccountType(@RequestBody AccountType accountType) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accountTypeService.createAccountType(accountType));

    }

    @ApiOperation(value ="account type by name" )
    @GetMapping("/typeOfAccount/{typeOfAccount}")
    public ResponseEntity<Object> getAccount(@PathVariable String typeOfAccount) {
        if (StringUtils.isBlank(typeOfAccount)) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "typeOfAccount parameter is null or empty");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accountTypeService.getAccount(typeOfAccount));

    }

    @ApiOperation(value ="accounts  by ids list" )
    @RequestMapping(value = "/account_list", method = RequestMethod.POST)
    public ResponseEntity<Object> getAccountList(@RequestBody List<BigInteger> accountids) {
        if (!Optional.ofNullable(accountids).isPresent()) {
            return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST, false, "Account List in Null");
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accountTypeService.getAccountList(accountids));

    }



    @ApiOperation(value ="all account type " )
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity<Object> getAllAccounts() {
          return ResponseHandler.generateResponse(HttpStatus.OK, true, accountTypeService.getAllAccounts());

    }


}
