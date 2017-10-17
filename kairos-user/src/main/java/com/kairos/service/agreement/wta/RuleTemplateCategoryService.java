package com.kairos.service.agreement.wta;

import com.kairos.custom_exception.ActionNotPermittedException;
import com.kairos.custom_exception.DataNotFoundByIdException;
import com.kairos.custom_exception.DuplicateDataException;
import com.kairos.persistence.model.user.agreement.wta.templates.RuleTemplateCategory;
import com.kairos.persistence.model.user.country.Country;
import com.kairos.persistence.repository.user.agreement.wta.RuleTemplateCategoryGraphRepository;
import com.kairos.persistence.repository.user.agreement.wta.WTABaseRuleTemplateGraphRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.service.UserBaseService;
import com.kairos.service.country.CountryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    @Inject private CountryGraphRepository countryGraphRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * used to save a new Rue template in a country
     * Created by vipul on 2/8/17.
     * params countryId and rulecategory via name and desc
     */

    public RuleTemplateCategory createRuleTemplate(long countryId, RuleTemplateCategory ruleTemplateCategory) {

        String name = "(?i)" + ruleTemplateCategory.getName();
        int ruleFound = countryGraphRepository.checkDuplicateRuleTemplate(countryId, name);

        if (ruleFound != 0) {
            throw new DuplicateDataException("Can't create duplicate rule template in same country");
        }


        Country country = countryService.getCountryById(countryId);
        List<RuleTemplateCategory> list = country.getRuleTemplateCategories();

        if (list == null) {
            list = new ArrayList<RuleTemplateCategory>();
        }

        list.add(ruleTemplateCategory);
        country.setRuleTemplateCategories(list);

        save(country);
        return ruleTemplateCategory;



    }

    public List<RuleTemplateCategory> getRulesTemplate(long countryId) {
        Country country = countryService.getCountryById(countryId);
        if (country == null) {
            throw new DataNotFoundByIdException("Country does not exist");
        }
        return ruleTemplateCategoryGraphRepository.getAllRulesOfCountry(countryId);

    }

    public boolean exists(long templateCategoryId) {
        return ruleTemplateCategoryGraphRepository.exists(templateCategoryId);
    }


    public boolean deleteRuleTemplateCategory(long countryId, long templateCategoryId) {
        RuleTemplateCategory ruleTemplateCategory=ruleTemplateCategoryGraphRepository.findOne(templateCategoryId);
        if (ruleTemplateCategory==null) {
            throw new DataNotFoundByIdException("RULE template ruleTemplateCategory does not exist"+templateCategoryId);
        }
        if(ruleTemplateCategory.getName()!=null && ruleTemplateCategory.getName().equals("NONE"))
        {
            throw new ActionNotPermittedException("Can't delete none template category "+templateCategoryId);
        }

        List<Long> wtaBaseRuleTemplateList =wtaBaseRuleTemplateGraphRepository.findAllWTABelongsByTemplateCategoryId(templateCategoryId);
        RuleTemplateCategory noneRuleTemplateCategory=ruleTemplateCategoryGraphRepository.findByName(countryId,"NONE");

        wtaBaseRuleTemplateGraphRepository.deleteRelationOfRuleTemplateCategoryAndWTA(templateCategoryId,wtaBaseRuleTemplateList);

        wtaBaseRuleTemplateGraphRepository.setAllWTAWithCategoryNone(noneRuleTemplateCategory.getId(),wtaBaseRuleTemplateList);

        ruleTemplateCategoryGraphRepository.softDelete(templateCategoryId);
        return true;

    }


    public Map<String, Object> updateRuleTemplateCategory(Long countryId, Long templateCategoryId, RuleTemplateCategory ruleTemplateCategory) {
        if (countryService.getCountryById(countryId) == null) {
            throw new DataNotFoundByIdException("Country does not exist");
        }


        RuleTemplateCategory ruleTemplateCategoryObj = (RuleTemplateCategory) ruleTemplateCategoryGraphRepository.findOne(templateCategoryId);
        if (ruleTemplateCategoryObj.getName() == ruleTemplateCategory.getName()) {
            throw new DuplicateDataException("Can't update, rule template ruleTemplateCategory name already in country");
        }
        if(ruleTemplateCategoryObj.getName().equals("NONE"))
        {
            throw new ActionNotPermittedException("Can't rename NONE template category "+templateCategoryId);
        }
        ruleTemplateCategoryObj.setName(ruleTemplateCategory.getName());
        ruleTemplateCategoryObj.setDescription(ruleTemplateCategory.getDescription());
        save(ruleTemplateCategoryObj);
        return ruleTemplateCategoryObj.printRuleTemp();

    }

    public void setRuleTemplatecategoryWithRuleTemplate( Long templateCategoryId,Long ruleTemplateId ){
        ruleTemplateCategoryGraphRepository.setRuleTemplateCategoryWithRuleTemplate(templateCategoryId,ruleTemplateId);

    }

}
