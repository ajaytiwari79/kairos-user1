package com.kairos.controller.agreement.cta;

import com.kairos.persistence.model.user.agreement.cta.CTARuleTemplateDTO;
import com.kairos.response.dto.web.cta.CollectiveTimeAgreementDTO;
import com.kairos.service.agreement.cta.CostTimeAgreementService;
import com.kairos.util.response.ResponseHandler;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.kairos.constants.ApiConstants.API_ORGANIZATION_URL;

@RequestMapping(API_ORGANIZATION_URL)
@RestController
public class CostTimeAgreementController {
    @Autowired
  private  CostTimeAgreementService costTimeAgreementService;

    /**
     * @auther anil maurya
     * @param countryId
     * @return
     */
    @RequestMapping(value = "/country/{countryId}/cta", method = RequestMethod.POST)
    @ApiOperation("Create CTA")
    public ResponseEntity<Map<String, Object>> createCTA(@PathVariable Long countryId
            , @RequestBody @Valid CollectiveTimeAgreementDTO collectiveTimeAgreementDTO ) throws ExecutionException, InterruptedException {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
                costTimeAgreementService.createCostTimeAgreement(countryId,collectiveTimeAgreementDTO));
    }

    @RequestMapping(value = "/country/{countryId}/cta/{ctaId}", method = RequestMethod.PUT)
    @ApiOperation("Update CTA")
    public ResponseEntity<Map<String, Object>> updateCTA(@PathVariable Long countryId, @PathVariable Long ctaId
            , @RequestBody @Valid CollectiveTimeAgreementDTO collectiveTimeAgreementDTO ) throws ExecutionException, InterruptedException {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.updateCostTimeAgreement(countryId, null, ctaId, collectiveTimeAgreementDTO));
    }

    @RequestMapping(value = "/unit/{unitId}/cta/{ctaId}", method = RequestMethod.PUT)
    @ApiOperation("Update CTA Of Unit")
    public ResponseEntity<Map<String, Object>> updateUnitCTA(@PathVariable Long unitId, @PathVariable Long ctaId
            , @RequestBody @Valid CollectiveTimeAgreementDTO collectiveTimeAgreementDTO ) throws ExecutionException, InterruptedException {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.updateCostTimeAgreement(null, unitId, ctaId, collectiveTimeAgreementDTO));
    }

    @RequestMapping(value = "/country/{countryId}/cta/{ctaId}", method = RequestMethod.DELETE)
    @ApiOperation("Delete CTA")
    public ResponseEntity<Map<String, Object>> deleteCTA(@PathVariable Long countryId, @PathVariable Long ctaId) throws ExecutionException, InterruptedException {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.deleteCostTimeAgreement(countryId, ctaId));
    }

    @RequestMapping(value = "/country/{countryId}/cta", method = RequestMethod.GET)
    @ApiOperation("GET CTA")
    public ResponseEntity<Map<String, Object>> getCTA(@PathVariable Long countryId) throws ExecutionException, InterruptedException {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.loadAllCTAByCountry(countryId));
    }

    @RequestMapping(value = "/unit/{unitId}/cta", method = RequestMethod.GET)
    @ApiOperation("GET CTA Of Unit")
    public ResponseEntity<Map<String, Object>> getCTAOfUnit(@PathVariable Long unitId) throws ExecutionException, InterruptedException {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.loadAllCTAByUnit(unitId));
    }

    @RequestMapping(value = "/country/{countryId}/cta_rule_template", method = RequestMethod.POST)
    @ApiOperation("Create CTA Rule Template")
    public ResponseEntity<Map<String, Object>> createCTARuleTemplate(@PathVariable Long countryId
            , @RequestBody @Valid CTARuleTemplateDTO ctaRuleTemplateDTO ) throws ExecutionException, InterruptedException {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.createCTARuleTemplate(countryId,ctaRuleTemplateDTO));
    }

    @RequestMapping(value = "/country/{countryId}/cta_rule_template/{templateId}", method = RequestMethod.PUT)
    @ApiOperation("Update CTA Rule Template")
    public ResponseEntity<Map<String, Object>> updateCTARuleTemplate(@PathVariable Long countryId,@PathVariable Long templateId
            , @RequestBody @Valid CTARuleTemplateDTO ctaRuleTemplateDTO ) throws ExecutionException, InterruptedException {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.updateCTARuleTemplate(countryId,templateId, ctaRuleTemplateDTO));
    }

    @RequestMapping(value = "/unit/{unitId}/cta/expertise", method = RequestMethod.GET)
    @ApiOperation("get expertise for cta rule template")
    public ResponseEntity<Map<String, Object>> getExpertiseForCTA(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.getExpertiseForOrgCTA(unitId));
    }
}
