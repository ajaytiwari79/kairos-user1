package com.kairos.service.agreement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.persistence.model.user.agreement.cta.RuleTemplate;
import com.kairos.persistence.model.user.agreement.cta.RuleTemplateCategoryType;
import com.kairos.persistence.model.user.agreement.wta.RuleTemplateCategoryDTO;
import com.kairos.persistence.model.user.agreement.wta.templates.RuleTemplateCategory;
import com.kairos.persistence.model.user.country.Country;
import com.kairos.persistence.repository.organization.OrganizationGraphRepository;
import com.kairos.persistence.repository.user.agreement.wta.RuleTemplateCategoryGraphRepository;
import com.kairos.persistence.repository.user.agreement.wta.WTABaseRuleTemplateGraphRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.response.dto.web.RuleTemplateDTO;
import com.kairos.response.dto.web.UpdateRuleTemplateCategoryDTO;
import com.kairos.service.UserBaseService;
import com.kairos.service.country.CountryService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

import static com.kairos.persistence.model.user.agreement.cta.RuleTemplateCategoryType.CTA;

/**
 * Created by vipul on 2/8/17.
 */
@Service
public class RuleTemplateCategoryService extends UserBaseService {
    @Inject
    private RuleTemplateCategoryGraphRepository ruleTemplateCategoryGraphRepository;
    @Inject
    private CountryService countryService;

    @Inject
    WTABaseRuleTemplateGraphRepository wtaBaseRuleTemplateGraphRepository;
    @Inject
    private OrganizationGraphRepository organizationGraphRepository;
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private WTABaseRuleTemplateGraphRepository wtaRuleTemplateGraphRepository;
    @Inject
    private RuleTemplateCategoryGraphRepository ruleTemplateCategoryRepository;
    @Inject
    private ExceptionService exceptionService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * used to save a new Rule template in a country
     * Created by vipul on 2/8/17.
     * params countryId and rule template category via name and desc
     */
    //TODO need to modified this method
    public RuleTemplateCategory createRuleTemplateCategory(long countryId, RuleTemplateCategory ruleTemplateCategory) {

        String name = "(?i)" + ruleTemplateCategory.getName();
        int ruleFound = countryGraphRepository.checkDuplicateRuleTemplateCategory(countryId, ruleTemplateCategory.getRuleTemplateCategoryType(), name);

        if (ruleFound != 0) {
            exceptionService.duplicateDataException("message.ruleTemplate.category.duplicate",name);
        }

        Country country = countryService.getCountryById(countryId);
        country.addRuleTemplateCategory(ruleTemplateCategory);
        save(country);
        return ruleTemplateCategory;

    }

    public List<RuleTemplateCategory> getRulesTemplateCategory(long countryId, RuleTemplateCategoryType ruleTemplateCategoryType) {
        Country country = countryService.getCountryById(countryId);
        if (country == null) {
            exceptionService.dataNotFoundByIdException("message.country.id.notExist");

        }
        return ruleTemplateCategoryGraphRepository.getRuleTemplateCategoryByCountry(countryId, ruleTemplateCategoryType);

    }

    public boolean exists(long templateCategoryId) {
        return ruleTemplateCategoryGraphRepository.existsById(templateCategoryId);
    }


    public boolean deleteRuleTemplateCategory(long countryId, long templateCategoryId) {
        RuleTemplateCategory ruleTemplateCategory = ruleTemplateCategoryGraphRepository.findOne(templateCategoryId);
        if (ruleTemplateCategory == null) {
            exceptionService.dataNotFoundByIdException("message.ruleTemplate.category.notExist",templateCategoryId);

        }
        if (ruleTemplateCategory.getName() != null && ruleTemplateCategory.getName().equals("NONE")) {
            exceptionService.actionNotPermittedException("message.ruleTemplate.category.delete",templateCategoryId);

        }
        if (ruleTemplateCategory.getRuleTemplateCategoryType().equals(CTA)) {
            List<Long> ctaRuleTemplates = ruleTemplateCategoryGraphRepository.findAllExistingCTARuleTemplateByCategory(ruleTemplateCategory.getName(), countryId);
            RuleTemplateCategory noneRuleTemplateCategory = ruleTemplateCategoryGraphRepository.findByName(countryId, "NONE", CTA);
            ruleTemplateCategoryGraphRepository.deleteRelationOfRuleTemplateCategoryAndCTA(templateCategoryId, ctaRuleTemplates);
            ruleTemplateCategoryGraphRepository.setAllCTAWithCategoryNone(noneRuleTemplateCategory.getId(), ctaRuleTemplates);
        }/* else {
            List<Long> wtaBaseRuleTemplateList = wtaBaseRuleTemplateGraphRepository.findAllWTABelongsByTemplateCategoryId(templateCategoryId);
            RuleTemplateCategory noneRuleTemplateCategory = ruleTemplateCategoryGraphRepository.findByName(countryId, "NONE", RuleTemplateCategoryType.WTA);
            wtaBaseRuleTemplateGraphRepository.deleteRelationOfRuleTemplateCategoryAndWTA(templateCategoryId, wtaBaseRuleTemplateList);
            wtaBaseRuleTemplateGraphRepository.setAllWTAWithCategoryNone(noneRuleTemplateCategory.getId(), wtaBaseRuleTemplateList);
        }*/
        return true;

    }


    public Map<String, Object> updateRuleTemplateCategory(Long countryId, Long templateCategoryId, UpdateRuleTemplateCategoryDTO ruleTemplateCategory) {
        if (countryService.getCountryById(countryId) == null) {
            exceptionService.dataNotFoundByIdException("message.country.id.notExist");

        }
        RuleTemplateCategory ruleTemplateCategoryObj = (RuleTemplateCategory) ruleTemplateCategoryGraphRepository.findOne(templateCategoryId);
        if(!Optional.ofNullable(ruleTemplateCategoryObj).isPresent()){
            exceptionService.dataNotFoundByIdException("message.ruleTemplate.category.notfound",templateCategoryId);

        }
        if (ruleTemplateCategoryObj.getName().equals("NONE") || ruleTemplateCategory.getName().equals("NONE")) {
           exceptionService.actionNotPermittedException("message.ruleTemplate.category.rename",templateCategoryId);

        }
        if(!ruleTemplateCategory.getName().trim().equalsIgnoreCase(ruleTemplateCategoryObj.getName())){
            boolean isAlreadyExists=ruleTemplateCategoryGraphRepository.findByNameExcludingCurrent(countryId,CTA,"(?i)" + ruleTemplateCategory.getName().trim(),templateCategoryId);
            if(isAlreadyExists){
                exceptionService.duplicateDataException("message.ruleTemplate.category.duplicate",ruleTemplateCategory.getName());

            }
        }
        ruleTemplateCategoryObj.setName(ruleTemplateCategory.getName());
        ruleTemplateCategoryObj.setDescription(ruleTemplateCategory.getDescription());
        save(ruleTemplateCategoryObj);
        return ruleTemplateCategoryObj.printRuleTemp();

    }


    /*
  *
  * This method will change the category of rule Template when we change the rule template all existing rule templates wil set to none
   * and new rule temp wll be setted to  this new rule template category
  * */
    public Map<String, Object> updateRuleTemplateCategory(RuleTemplateDTO ruleTemplateDTO, long countryId) {
        Map<String, Object> response = new HashMap();
        if (ruleTemplateDTO.getRuleTemplateCategoryType().equals(RuleTemplateCategoryType.CTA.name())) {
            response = changeCTARuleTemplateCategory(countryId, ruleTemplateDTO);
        } else {
            response = changeWTARuleTemplateCategory(countryId, ruleTemplateDTO);
        }
        return response;
    }

    private Map<String, Object> changeWTARuleTemplateCategory(Long countryId, RuleTemplateDTO ruleTemplateDTO) {
        Map<String, Object> response = new HashMap();
        List<RuleTemplate> wtaBaseRuleTemplates = wtaRuleTemplateGraphRepository.getWtaBaseRuleTemplateByIds(ruleTemplateDTO.getRuleTemplateIds());
        RuleTemplateCategory previousRuleTemplateCategory = ruleTemplateCategoryRepository.findByName(countryId, "(?i)" + ruleTemplateDTO.getCategoryName(), RuleTemplateCategoryType.WTA);
        if (!Optional.ofNullable(previousRuleTemplateCategory).isPresent()) {  // Rule Template Category does not exist So creating  a new one and adding in country
            previousRuleTemplateCategory = new RuleTemplateCategory(ruleTemplateDTO.getCategoryName());
            previousRuleTemplateCategory.setRuleTemplateCategoryType(RuleTemplateCategoryType.WTA);
            previousRuleTemplateCategory.setDeleted(false);
            Country country = countryGraphRepository.findOne(countryId);
            List<RuleTemplateCategory> ruleTemplateCategories = country.getRuleTemplateCategories();
            ruleTemplateCategories.add(previousRuleTemplateCategory);
            country.setRuleTemplateCategories(ruleTemplateCategories);
            countryGraphRepository.save(country);
            // Break Previous Relation
            wtaRuleTemplateGraphRepository.deleteOldCategories(ruleTemplateDTO.getRuleTemplateIds());
            previousRuleTemplateCategory.setRuleTemplates(wtaBaseRuleTemplates);
            save(previousRuleTemplateCategory);
            response.put("category", previousRuleTemplateCategory);
            response.put("templateList", getJsonOfUpdatedTemplates(wtaBaseRuleTemplates, previousRuleTemplateCategory));

        } else {
            List<Long> previousBaseRuleTemplates = ruleTemplateCategoryRepository.findAllExistingRuleTemplateAddedToThiscategory(ruleTemplateDTO.getCategoryName(), countryId);
            List<Long> newRuleTemplates = ruleTemplateDTO.getRuleTemplateIds();
            List<Long> ruleTemplateIdsNeedToAddInCategory = ArrayUtil.getUniqueElementWhichIsNotInFirst(previousBaseRuleTemplates, newRuleTemplates);
            List<Long> ruleTemplateIdsNeedToRemoveFromCategory = ArrayUtil.getUniqueElementWhichIsNotInFirst(newRuleTemplates, previousBaseRuleTemplates);
            ruleTemplateCategoryRepository.updateCategoryOfRuleTemplate(ruleTemplateIdsNeedToAddInCategory, ruleTemplateDTO.getCategoryName());
            ruleTemplateCategoryRepository.updateCategoryOfRuleTemplate(ruleTemplateIdsNeedToRemoveFromCategory, "NONE");
            response.put("templateList", getJsonOfUpdatedTemplates(wtaBaseRuleTemplates, previousRuleTemplateCategory));
        }
        return response;
    }

    private List<RuleTemplateCategoryDTO> getJsonOfUpdatedTemplates(List<RuleTemplate> wtaBaseRuleTemplates, RuleTemplateCategory ruleTemplateCategory) {

        ObjectMapper objectMapper = new ObjectMapper();
        List<RuleTemplateCategoryDTO> wtaBaseRuleTemplateDTOS = new ArrayList<>(wtaBaseRuleTemplates.size());
        wtaBaseRuleTemplates.forEach(wtaBaseRuleTemplate -> {
            RuleTemplateCategoryDTO wtaBaseRuleTemplateDTO = objectMapper.convertValue(wtaBaseRuleTemplate, RuleTemplateCategoryDTO.class);
            wtaBaseRuleTemplateDTO.setRuleTemplateCategory(ruleTemplateCategory);
            wtaBaseRuleTemplateDTOS.add(wtaBaseRuleTemplateDTO);
        });

        return wtaBaseRuleTemplateDTOS;
    }

    public Map<String, Object> changeCTARuleTemplateCategory(Long countryId, RuleTemplateDTO ruleTemplateDTO) {
        Map<String, Object> response = new HashMap();
        RuleTemplateCategory ruleTemplateCategory = new RuleTemplateCategory();
        Country country = countryGraphRepository.findOne(countryId);
        List<RuleTemplateCategory> ruleTemplateCategories = country.getRuleTemplateCategories();

        Optional<RuleTemplateCategory> countryRuleTemplateCategory = ruleTemplateCategories.parallelStream().filter(ruleTemplateCategory1 -> "CTA".equalsIgnoreCase(ruleTemplateCategory1.getRuleTemplateCategoryType() !=null ? ruleTemplateCategory1.getRuleTemplateCategoryType().toString() : "")
                && ruleTemplateCategory1.getName().equalsIgnoreCase(ruleTemplateDTO.getCategoryName())).findFirst();

        if (!countryRuleTemplateCategory.isPresent() || (countryRuleTemplateCategory.isPresent() && countryRuleTemplateCategory.get().isDeleted()==true)) {
            ruleTemplateCategory.setName(ruleTemplateDTO.getCategoryName());
            ruleTemplateCategory.setDeleted(false);
            ruleTemplateCategory.setRuleTemplateCategoryType(CTA);
            country.addRuleTemplateCategory(ruleTemplateCategory);
            save(country);
            ruleTemplateCategoryGraphRepository.updateCategoryOfCTARuleTemplate(ruleTemplateDTO.getRuleTemplateIds(), ruleTemplateCategory.getName());
            response.put("category", ruleTemplateCategory);
        } else {
            List<Long> ctaRuleTemplates = ruleTemplateCategoryGraphRepository.findAllExistingCTARuleTemplateByCategory(ruleTemplateDTO.getCategoryName(), countryId);
            List<Long> ruleTemplateIdsNeedToAddInCategory = ArrayUtil.getUniqueElementWhichIsNotInFirst(ctaRuleTemplates, ruleTemplateDTO.getRuleTemplateIds());
            List<Long> ruleTemplateIdsNeedToRemoveFromCategory = ArrayUtil.getUniqueElementWhichIsNotInFirst(ruleTemplateDTO.getRuleTemplateIds(), ctaRuleTemplates);
            ruleTemplateCategoryGraphRepository.updateCategoryOfCTARuleTemplate(ruleTemplateIdsNeedToAddInCategory, ruleTemplateDTO.getCategoryName());
            ruleTemplateCategoryGraphRepository.updateCategoryOfCTARuleTemplate(ruleTemplateIdsNeedToRemoveFromCategory, "NONE");
        }

        return response;
    }


    /*public RuleTemplateWrapper getRulesTemplateCategoryByUnit(Long unitId) {
        Organization organization = organizationGraphRepository.findOne(unitId);
        if (!Optional.ofNullable(organization).isPresent()) {
            throw new DataNotFoundByIdException("Organization does not exist");
        }
        List<RuleTemplateCategoryTagDTO> categoryList = ruleTemplateCategoryRepository.getRuleTemplateCategoryByUnitId(unitId);
        List<RuleTemplateResponseDTO> templateList = wtaBaseRuleTemplateGraphRepository.getWTABaseRuleTemplateByUnitId(unitId);
        RuleTemplateWrapper ruleTemplateWrapper = new RuleTemplateWrapper();
        ruleTemplateWrapper.setCategoryList(categoryList);
        ruleTemplateWrapper.setTemplateList(templateList);

        return ruleTemplateWrapper;

    }*/
    // creating default rule template category NONE
    public void createDefaultRuleTemplateCategory( RuleTemplateCategory ruleTemplateCategory) {
        save(ruleTemplateCategory);

    }

    public RuleTemplateCategory getCTARuleTemplateCategoryOfCountryByName(Long countryId, String name){
        RuleTemplateCategory category = ruleTemplateCategoryGraphRepository
                .findByName(countryId, "NONE", RuleTemplateCategoryType.CTA);
        return category;
    }

}
