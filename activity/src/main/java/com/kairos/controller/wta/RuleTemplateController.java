package com.kairos.controller.wta;

import com.kairos.dto.activity.wta.basic_details.WTABaseRuleTemplateDTO;
import com.kairos.dto.activity.wta.rule_template_category.RuleTemplateCategoryRequestDTO;
import com.kairos.service.activity.ActivityService;
import com.kairos.service.wta.RuleTemplateCategoryService;
import com.kairos.service.wta.RuleTemplateService;
import com.kairos.service.wta.WTABuilderService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.Map;

import static com.kairos.constants.ApiConstants.*;


/**
 * Created by pawanmandhan on 5/8/17.
 */

@RequestMapping(API_V1)
@RestController
public class RuleTemplateController {


    @Inject
    @Lazy
    private RuleTemplateService ruleTemplateService;
    @Inject
    private RuleTemplateCategoryService ruleTemplateCategoryService;
    @Inject
    private WTABuilderService wtaBuilderService;
    @Inject
    private ActivityService activityService;

    @RequestMapping(value = COUNTRY_URL + "/rule_templates", method = RequestMethod.POST)
    ResponseEntity<Map<String, Object>> createRuleTemplate(@PathVariable Long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, ruleTemplateService.createRuleTemplate(countryId));
    }

    @RequestMapping(value = COUNTRY_URL + "/rule_templates", method = RequestMethod.GET)
    ResponseEntity<Map<String, Object>> getRuleTemplate(@PathVariable Long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, ruleTemplateService.getRuleTemplate(countryId));
    }

    @RequestMapping(value = COUNTRY_URL + "/rule_templates/{ruleTemplateId}", method = RequestMethod.PUT)
    ResponseEntity<Map<String, Object>> getRuleTemplate(@PathVariable Long countryId, @PathVariable BigInteger ruleTemplateId, @RequestBody @Valid WTABaseRuleTemplateDTO wtaBaseRuleTemplateDTO) {
        // WTABaseRuleTemplateDTO wtaBaseRuleTemplateDTO = WTABuilderService.copyRuleTemplateMapToDTO(template);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, ruleTemplateService.updateRuleTemplate(countryId, ruleTemplateId,wtaBaseRuleTemplateDTO));
    }


    @RequestMapping(value = COUNTRY_URL + "/rule_templates/category", method = RequestMethod.POST)
    ResponseEntity<Map<String, Object>> updateRuleTemplateCategory(@Valid @RequestBody RuleTemplateCategoryRequestDTO ruleTemplateDTO, @PathVariable long countryId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, ruleTemplateCategoryService.createRuleTemplateCategory(countryId, ruleTemplateDTO));
    }

    @RequestMapping(value = UNIT_URL + "/rule_templates", method = RequestMethod.GET)
    ResponseEntity<Map<String, Object>> getRulesTemplateCategoryByUnit(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, ruleTemplateService.getRulesTemplateCategoryByUnit(unitId));
    }

    @RequestMapping(value = COUNTRY_URL + "/copy_rule_template", method = RequestMethod.POST)
    ResponseEntity<Map<String, Object>> copyRuleTemplate(@PathVariable Long countryId, @RequestBody @Valid WTABaseRuleTemplateDTO template) {
        //WTABaseRuleTemplateDTO wtaBaseRuleTemplateDTO = WTABuilderService.copyRuleTemplateMapToDTO(template);
        return ResponseHandler.generateResponse(HttpStatus.OK, true, ruleTemplateService.copyRuleTemplate(countryId, template));
    }

    @ApiOperation("get cut off interval of Activity")
    @GetMapping(value = "/activity/{activityId}/cut_off_interval")
        //  @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    ResponseEntity<Map<String, Object>> getCutOffIntervalOfActivity(@PathVariable BigInteger activityId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, activityService.getCutOffInterValOfActivity(activityId));
    }


}
