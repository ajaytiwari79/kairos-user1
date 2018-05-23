package com.kairos.controller.agreement_template;


import com.kairos.persistance.model.agreement_template.AgreementSection;
import com.kairos.service.agreement_template.AgreementSectionService;
import com.kairos.utils.ResponseHandler;
import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static com.kairos.constant.ApiConstant.API_AGREEMENT_SECTION_URL;
/*
 *
 *  created by bobby 10/5/2018
 * */


@RestController
@RequestMapping(API_AGREEMENT_SECTION_URL)
@Api(API_AGREEMENT_SECTION_URL)
public class AgreementSectionController {



    @Inject
    private AgreementSectionService agreementSectionService;



    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public ResponseEntity<Object>  createAgreementSection(@RequestBody AgreementSection agreementSection)
    {
return ResponseHandler.generateResponse(HttpStatus.OK,true,agreementSectionService.createAgreementSection(agreementSection));

    }


    @RequestMapping(value = "/delete/{id}",method = RequestMethod.DELETE)
    public ResponseEntity<Object>  deleteAgreementSection(@PathVariable BigInteger id )
    { if (id!=null) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, agreementSectionService.deleteAgreementSection(id));
    }
        return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST,false,"request id Cannot be null");

    }



    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public ResponseEntity<Object>  getAgreementSectionWithDataById(@PathVariable BigInteger id )
    {
        if (id!=null) {
            return ResponseHandler.generateResponse(HttpStatus.OK, true, agreementSectionService.getAgreementSectionWithDataById(id));
        }
        return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST,false,"request id Cannot be null");
    }


    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public ResponseEntity<Object>  getAllAgreementSection()
    {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,agreementSectionService.getAllAgreementSection());

    }



    @RequestMapping(value = "/list",method = RequestMethod.POST)
    public ResponseEntity<Object>  getAllAgreementSectionList(@RequestBody @NotEmpty List<BigInteger> ids)
    {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,agreementSectionService.getAgreementSectionWithDataList(ids));

    }

}
