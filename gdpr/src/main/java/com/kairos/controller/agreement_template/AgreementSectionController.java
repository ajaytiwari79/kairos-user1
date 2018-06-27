package com.kairos.controller.agreement_template;


import com.kairos.service.agreement_template.AgreementSectionService;
import com.kairos.utils.ResponseHandler;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import java.math.BigInteger;
import java.util.Set;

import static com.kairos.constants.ApiConstant.API_AGREEMENT_SECTION_URL;
/*
 *
 *  created by bobby 10/5/2018
 * */


@RestController
@RequestMapping(API_AGREEMENT_SECTION_URL)
@Api(API_AGREEMENT_SECTION_URL)
public class AgreementSectionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementSectionController.class);

    @Inject
    private AgreementSectionService agreementSectionService;



   /* @RequestMapping(value = "/add",method = RequestMethod.POST)
    public ResponseEntity<Object>  createAgreementSection(@PathVariable Long countryId,@RequestBody AgreementSection agreementSection)
    {
return ResponseHandler.generateResponse(HttpStatus.OK,true,agreementSectionService.createAgreementSections(countryId,agreementSection));

    }*/


    @RequestMapping(value = "/delete/{id}",method = RequestMethod.DELETE)
    public ResponseEntity<Object>  deleteAgreementSection(@PathVariable BigInteger id )
    { if (id!=null) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, agreementSectionService.deleteAgreementSection(id));
    }
        return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST,false,"request id Cannot be null");

    }



    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public ResponseEntity<Object>  getAgreementSectionWithDataById(@PathVariable Long countryId,@PathVariable BigInteger id )
    {
        if (id!=null) {
            return ResponseHandler.generateResponse(HttpStatus.OK, true, agreementSectionService.getAgreementSectionWithDataById(countryId,id));
        }
        return ResponseHandler.invalidResponse(HttpStatus.BAD_REQUEST,false,"request id Cannot be null");
    }


    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public ResponseEntity<Object>  getAllAgreementSection(@PathVariable Long countryId)
    {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,agreementSectionService.getAllAgreementSection(countryId));

    }



    @RequestMapping(value = "/list",method = RequestMethod.POST)
    public ResponseEntity<Object>  getAllAgreementSectionList(@PathVariable Long countryId,@RequestBody Set<BigInteger> ids)
    {
        return ResponseHandler.generateResponse(HttpStatus.OK,true,agreementSectionService.getAgreementSectionWithDataList(countryId,ids));

    }

}
