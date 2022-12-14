package com.kairos.controller.cta;

import com.kairos.dto.TranslationInfo;
import com.kairos.dto.activity.cta.CTARuleTemplateDTO;
import com.kairos.dto.activity.cta.CollectiveTimeAgreementDTO;
import com.kairos.dto.activity.cta_compensation_setting.CTACompensationConfiguration;
import com.kairos.dto.activity.cta_compensation_setting.CTACompensationSettingDTO;
import com.kairos.service.cta.CostTimeAgreementService;
import com.kairos.service.cta.CountryCTAService;
import com.kairos.service.cta_compensation_settings.CTACompensationSettingService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static com.kairos.constants.ApiConstants.*;

/**
 * @author pradeep
 * @date - 30/7/18
 */

@RequestMapping(API_V1)
@RestController
public class CostTimeAgreementController {
    @Inject
    private CostTimeAgreementService costTimeAgreementService;
    @Inject
    private CountryCTAService countryCTAService;
    @Inject
    private CTACompensationSettingService ctaCompensationSettingService;

    /**
     *
     * @param countryId
     * @param collectiveTimeAgreementDTO
     * @return
     */
    @PostMapping(value = "/country/{countryId}/cta")
    @ApiOperation("Create CTA")
    public ResponseEntity<Map<String, Object>> createCTA(@PathVariable Long countryId
            , @RequestBody @Valid CollectiveTimeAgreementDTO collectiveTimeAgreementDTO )  {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
                countryCTAService.createCostTimeAgreementInCountry(countryId,collectiveTimeAgreementDTO,false));
    }

    /**
     *
     * @param countryId
     * @param ctaId
     * @param collectiveTimeAgreementDTO
     * @return
     */
    @PutMapping(value = "/country/{countryId}/cta/{ctaId}")
    @ApiOperation("Update CTA")
    public ResponseEntity<Map<String, Object>> updateCTA(@PathVariable Long countryId, @PathVariable BigInteger ctaId
            , @RequestBody @Valid CollectiveTimeAgreementDTO collectiveTimeAgreementDTO )  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                countryCTAService.updateCostTimeAgreementInCountry(countryId,  ctaId, collectiveTimeAgreementDTO));
    }

    /**
     *
     * @param unitId
     * @param ctaId
     * @param collectiveTimeAgreementDTO
     * @return
     */
    @PutMapping(value = "/unit/{unitId}/cta/{ctaId}")
    @ApiOperation("Update CTA Of Unit")
    public ResponseEntity<Map<String, Object>> updateUnitCTA(@PathVariable Long unitId, @PathVariable BigInteger ctaId
            , @RequestBody @Valid CollectiveTimeAgreementDTO collectiveTimeAgreementDTO )  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                countryCTAService.updateCostTimeAgreementInUnit( unitId, ctaId, collectiveTimeAgreementDTO));
    }

    /**
     *
     * @param countryId
     * @param ctaId
     * @return
     */
    @DeleteMapping(value = "/country/{countryId}/cta/{ctaId}")
    @ApiOperation("Delete CTA")
    public ResponseEntity<Map<String, Object>> deleteCTA(@PathVariable Long countryId, @PathVariable BigInteger ctaId)  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.deleteCostTimeAgreement(countryId, ctaId));
    }

    /**
     *
     * @param countryId
     * @return
     */
    @GetMapping(value = "/country/{countryId}/cta")
    @ApiOperation("GET CTA")
    public ResponseEntity<Map<String, Object>> getCTA(@PathVariable Long countryId){
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.loadAllCTAByCountry(countryId));
    }

    /**
     *
     * @param unitId
     * @return
     */
    @GetMapping(value = "/unit/{unitId}/cta")
    @ApiOperation("GET CTA Of Unit")
    public ResponseEntity<Map<String, Object>> getCTAOfUnit(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.loadAllCTAByUnit(unitId));
    }

    /**
     *
     * @param unitId
     * @param ctaId
     * @return
     */
    @GetMapping(value = "/unit/{unitId}/cta/{ctaId}/rule-templates")
    @ApiOperation("GET CTA RuleTemplate By ctaId")
    public ResponseEntity<Map<String, Object>> getCTARuleTemplateOfUnit(@PathVariable Long unitId,@PathVariable BigInteger ctaId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.getCTARuleTemplateOfUnit(unitId,ctaId));
    }




    /**
     *
     * @param employmentId
     * @param unitId
     * @param ctaId
     * @param ctaDTO
     * @return
     */
    @ApiOperation(value = "Update employment's CTA")
    @PutMapping(value = UNIT_URL+"/employment/{employmentId}/cta/{ctaId}")
    public ResponseEntity<Map<String, Object>> updateCostTimeAgreementForEmployment(@PathVariable Long employmentId, @PathVariable Long unitId, @PathVariable BigInteger ctaId, @RequestBody @Valid CollectiveTimeAgreementDTO ctaDTO,@RequestParam("save") Boolean save){
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.updateCostTimeAgreementForEmployment(unitId, employmentId, ctaId, ctaDTO,save));
    }


    /**
     *
     * @param employmentId
     * @param unitId
     * @return
     */
    @ApiOperation(value = "get employment's CTA")
    @GetMapping(value = UNIT_URL+"/employment/{employmentId}/cta")
    public ResponseEntity<Map<String, Object>> getUnitEmploymentPositionCTA(@PathVariable Long employmentId, @PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.getEmploymentCTA(unitId, employmentId));
    }


    /**
     *
     * @param countryId
     * @param ctaId
     * @return
     */
    @GetMapping(value = "/country/{countryId}/cta/{ctaId}/rule-templates")
    @ApiOperation("GET CTA Ruletemplate By ctaId in Country")
    public ResponseEntity<Map<String, Object>> getCTARuleTemplateOfCountry(@PathVariable Long countryId,@PathVariable BigInteger ctaId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.getCTARuleTemplateOfCountry(countryId,ctaId));
    }

    /**
     *
     * @param countryId
     * @param ctaRuleTemplateDTO
     * @return
     */
    @PostMapping(value = "/country/{countryId}/cta_rule_template")
    @ApiOperation("Create CTA Rule Template")
    public ResponseEntity<Map<String, Object>> createCTARuleTemplate(@PathVariable Long countryId
            , @RequestBody @Valid CTARuleTemplateDTO ctaRuleTemplateDTO )  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.createCTARuleTemplate(countryId,ctaRuleTemplateDTO));
    }

    /**
     *
     * @param countryId
     * @param templateId
     * @param ctaRuleTemplateDTO
     * @return
     */
    @PutMapping(value = "/country/{countryId}/cta_rule_template/{templateId}")
    @ApiOperation("Update CTA Rule Template")
    public ResponseEntity<Map<String, Object>> updateCTARuleTemplate(@PathVariable Long countryId,@PathVariable BigInteger templateId
            , @RequestBody @Valid CTARuleTemplateDTO ctaRuleTemplateDTO )  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,
                costTimeAgreementService.updateCTARuleTemplate(countryId,templateId, ctaRuleTemplateDTO));
    }

    /**
     *
     * @param unitId
     * @param collectiveTimeAgreementDTO
     * @return
     */
    @PostMapping(value = "unit/{unitId}/copy_unit_cta")
    @ApiOperation("Create copy of CTA at unit")
    public ResponseEntity<Map<String, Object>> createCopyOfUnitCTA(@PathVariable Long unitId
            , @RequestBody @Valid CollectiveTimeAgreementDTO collectiveTimeAgreementDTO )  {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
                costTimeAgreementService.createCopyOfUnitCTA(unitId,collectiveTimeAgreementDTO));
    }

    /**
     *
     * @param organizationSubTypeId
     * @return
     */
    @ApiOperation(value = "Get CTA by Organization sub type  by using sub type Id")
    @GetMapping(value = COUNTRY_URL + "/cta/organization_sub_type/{organizationSubTypeId}")
    public ResponseEntity<Map<String, Object>> getAllCTAByOrganizationSubType(@PathVariable Long countryId,@PathVariable Long organizationSubTypeId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.getAllCTAByOrganizationSubType(countryId,organizationSubTypeId));
    }

    /**
     *
     * @param countryId
     * @param ctaId
     * @param collectiveTimeAgreementDTO
     * @param organizationSubTypeId
     * @param checked
     * @return
     */
    @ApiOperation(value = "link and unlink cta with org sub-type")
    @PutMapping(value = COUNTRY_URL + "/organization_sub_type/{organizationSubTypeId}/cta/{ctaId}")
    public ResponseEntity<Map<String, Object>> setCTAWithOrganizationType(@PathVariable Long countryId, @PathVariable BigInteger ctaId, @RequestBody CollectiveTimeAgreementDTO collectiveTimeAgreementDTO, @PathVariable Long organizationSubTypeId, @RequestParam(value = "checked") boolean checked)  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.setCTAWithOrganizationType(countryId, ctaId,collectiveTimeAgreementDTO, organizationSubTypeId, checked));
    }

    /**
     *
     * @param countryId
     * @return
     */
    @ApiOperation(value = "create default cta ruletemplate ")
    @PostMapping(value = COUNTRY_URL + "/default_cta")
    public ResponseEntity<Map<String, Object>> createDefaultCtaRuleTemplate(@PathVariable Long countryId)  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.createDefaultCtaRuleTemplate(countryId));
    }


    @ApiOperation(value = "get versions cta")
    @GetMapping(value = UNIT_URL + "/get_versions_cta")
    public ResponseEntity<Map<String, Object>> getVersionsCTA(@PathVariable Long unitId,@RequestParam List<Long> employmentIds)  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.getVersionsCTA(unitId,employmentIds));
    }

    @ApiOperation(value = "get default cta")
    @GetMapping(value = UNIT_URL + "/get_default_cta/expertise/{expertiseId}")
    public ResponseEntity<Map<String, Object>> getDefaultCTA(@PathVariable Long unitId,@PathVariable Long expertiseId)  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.getDefaultCTA(unitId,expertiseId));
    }
    /**
     *
     * @param unitId
     * @param collectiveTimeAgreementDTO
     * @return
     */
    @PostMapping(value =UNIT_URL + "/cta")
    @ApiOperation("Create CTA in Organization")
    public ResponseEntity<Map<String, Object>> createCtaInOrganization(@PathVariable Long unitId
            , @RequestBody @Valid CollectiveTimeAgreementDTO collectiveTimeAgreementDTO )  {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
                countryCTAService.createCostTimeAgreementInOrganization(unitId,collectiveTimeAgreementDTO));
    }

    @PutMapping(value =COUNTRY_URL+API_EXPERTISE_URL + "/cta_compensation")
    @ApiOperation("Cta Compensation of expertise in Country")
    public ResponseEntity<Map<String, Object>> updateCTACompensationSetting(@PathVariable Long countryId,@PathVariable Long expertiseId
            , @RequestBody @Valid CTACompensationSettingDTO ctaCompensationSettingDTO )  {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
                ctaCompensationSettingService.updateCTACompensationSetting(countryId,expertiseId,ctaCompensationSettingDTO));
    }

    @PutMapping(value =UNIT_URL+API_EXPERTISE_URL + "/cta_compensation")
    @ApiOperation("Cta Compensation of expertise in Country")
    public ResponseEntity<Map<String, Object>> updateCTACompensationSettingByUnit(@PathVariable Long unitId,@PathVariable Long expertiseId
            , @RequestBody @Valid CTACompensationSettingDTO ctaCompensationSettingDTO )  {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
                ctaCompensationSettingService.updateCTACompensationSettingByUnit(unitId,expertiseId,ctaCompensationSettingDTO));
    }

    @GetMapping(value =COUNTRY_URL+API_EXPERTISE_URL + "/cta_compensation")
    @ApiOperation("Cta Compensation of expertise in Country")
    public ResponseEntity<Map<String, Object>> getCTACompensationSetting(@PathVariable Long countryId,@PathVariable Long expertiseId)  {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
                ctaCompensationSettingService.getCTACompensationSetting(countryId,expertiseId));
    }

    @GetMapping(value =COUNTRY_URL + "/cta_compensations")
    @ApiOperation("Cta Compensation of expertise in Country")
    public ResponseEntity<Map<String, Object>> getCTACompensationSettingByCountryId(@PathVariable Long countryId)  {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
                ctaCompensationSettingService.getCTACompensationSettingByCountryId(countryId));
    }

    @PutMapping(value = "/validate_cta_compensations")
    @ApiOperation("Cta Compensation of expertise in Country")
    public ResponseEntity<Map<String, Object>> validateCTACompensationSetting(@RequestBody List<CTACompensationConfiguration> ctaCompensationConfigurations)  {
        ctaCompensationSettingService.validateInterval(ctaCompensationConfigurations);
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true,null);
    }

    @GetMapping(value =UNIT_URL+API_EXPERTISE_URL + "/cta_compensation")
    @ApiOperation("Cta Compensation of expertise in Country")
    public ResponseEntity<Map<String, Object>> getCTACompensationSettingByUnit(@PathVariable Long unitId,@PathVariable Long expertiseId)  {
        return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
                ctaCompensationSettingService.getCTACompensationSettingByUnit(unitId,expertiseId));
    }

    @ApiOperation(value = "update translation data")
    @PutMapping(value = COUNTRY_URL+"/cta/rule_template/{id}/language_settings")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateTranslationDataOfCtaTemplates(@PathVariable BigInteger id, @RequestBody Map<String, TranslationInfo> translations) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,costTimeAgreementService.updateCtaRuleTranslations(id,translations));
    }

    @PutMapping(value = COUNTRY_URL + "/cta/{id}/language_settings")
    @ApiOperation("Add translated data")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateTranslationsOfCTA(@PathVariable BigInteger id, @RequestBody Map<String, TranslationInfo> translations) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.updateTranslation(id,translations));
    }

    @PutMapping(value = UNIT_URL + "/cta/{id}/language_settings")
    @ApiOperation("Add translated data")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> updateTranslationsOfCTAOfOrganization(@PathVariable BigInteger id, @RequestBody Map<String, TranslationInfo> translations) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.updateTranslation(id,translations));
    }

    @ApiOperation(value = "update translation data")
    @PutMapping(value = UNIT_URL+"/cta/rule_template/{id}/language_settings")
    //@PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> updateTranslationDataOfCTATemplatesOfOrganization(@PathVariable BigInteger id, @RequestBody Map<String, TranslationInfo> translations) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true,costTimeAgreementService.updateCtaRuleTranslations(id,translations));
    }

    @GetMapping(value = COUNTRY_URL+"/cta/default-data")
    @ApiOperation("get default data for cta rule template")
    public ResponseEntity<Map<String, Object>> getDefaultDataForCTAByCountryId(@PathVariable Long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.getDefaultDataForCTATemplate(countryId,null));
    }

    @GetMapping(value = UNIT_URL+"/cta/default-data")
    @ApiOperation("get default data for cta rule template")
    public ResponseEntity<Map<String, Object>> getDefaultDataForCTAByUnitId(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, costTimeAgreementService.getDefaultDataForCTATemplate(null,unitId));
    }


}

