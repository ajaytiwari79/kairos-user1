package com.kairos.service.wta;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.TranslationInfo;
import com.kairos.dto.activity.tags.TagDTO;
import com.kairos.dto.activity.wta.basic_details.WTABaseRuleTemplateDTO;
import com.kairos.dto.activity.wta.rule_template_category.RuleTemplateAndCategoryResponseDTO;
import com.kairos.dto.activity.wta.rule_template_category.RuleTemplateCategoryDTO;
import com.kairos.dto.activity.wta.rule_template_category.RuleTemplateCategoryRequestDTO;
import com.kairos.dto.user.country.basic_details.CountryDTO;
import com.kairos.enums.RuleTemplateCategoryType;
import com.kairos.persistence.model.cta.CTARuleTemplate;
import com.kairos.persistence.model.wta.templates.RuleTemplateCategory;
import com.kairos.persistence.model.wta.templates.WTABaseRuleTemplate;
import com.kairos.persistence.repository.cta.CTARuleTemplateRepository;
import com.kairos.persistence.repository.tag.TagMongoRepository;
import com.kairos.persistence.repository.wta.WorkingTimeAgreementMongoRepository;
import com.kairos.persistence.repository.wta.rule_template.RuleTemplateCategoryRepository;
import com.kairos.persistence.repository.wta.rule_template.WTABaseRuleTemplateMongoRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.exception.ExceptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.kairos.constants.ActivityMessagesConstants.*;
import static com.kairos.enums.RuleTemplateCategoryType.CTA;
import static com.kairos.enums.RuleTemplateCategoryType.WTA;


/**
 * Created by vipul on 2/8/17.
 */
@Transactional
@Service
public class RuleTemplateCategoryService {
    @Inject
    private RuleTemplateCategoryRepository ruleTemplateCategoryMongoRepository;
    @Inject
    private WorkingTimeAgreementMongoRepository workingTimeAgreementMongoRepository;
    @Inject
    private WTABaseRuleTemplateMongoRepository wtaBaseRuleTemplateMongoRepository;
    @Inject
    private CTARuleTemplateRepository ctaRuleTemplateRepository;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Autowired
    private ExceptionService excpExceptionService;
    @Inject
    private TagMongoRepository tagMongoRepository;
    @Inject @Lazy private RuleTemplateService ruleTemplateService;

    private final static Logger LOGGER = LoggerFactory.getLogger(RuleTemplateCategoryService.class);

    /**
     * used to save a new Rule template in a country
     * Created by vipul on 2/8/17.
     * params countryId and rule template category via name and desc
     */
    //TODO need to modified this method
    public RuleTemplateAndCategoryResponseDTO createRuleTemplateCategory(long countryId, RuleTemplateCategoryRequestDTO ruleTemplateCategoryDTO) {
        CountryDTO country = userIntegrationService.getCountryById(countryId);
        if (!Optional.ofNullable(country).isPresent()) {
            excpExceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID, countryId);
        }

        RuleTemplateCategory ruleTemplateCategory = ruleTemplateCategoryMongoRepository.findByName(countryId, ruleTemplateCategoryDTO.getName(), ruleTemplateCategoryDTO.getRuleTemplateCategoryType());
        RuleTemplateAndCategoryResponseDTO ruleTemplateAndCategoryDTO = null;
        if (ruleTemplateCategory == null) {
            ruleTemplateCategory = new RuleTemplateCategory();
            ObjectMapperUtils.copyProperties(ruleTemplateCategoryDTO, ruleTemplateCategory);
            ruleTemplateCategory.setCountryId(country.getId());
            ruleTemplateCategory.setTags(ruleTemplateCategoryDTO.getTags());
            ruleTemplateCategoryMongoRepository.save(ruleTemplateCategory);
            if (ruleTemplateCategory.getRuleTemplateCategoryType().equals(RuleTemplateCategoryType.WTA)) {
                ruleTemplateAndCategoryDTO = updateCategoryToWTATemplates(ruleTemplateCategory, ruleTemplateCategoryDTO);
            } else {
                ruleTemplateAndCategoryDTO = updateCategoryToCTATemplate(ruleTemplateCategory, ruleTemplateCategoryDTO);
            }

        } else {
            ruleTemplateAndCategoryDTO = updateRuleTemplateCategory(countryId, null, ruleTemplateCategoryDTO, ruleTemplateCategory);
        }
        return ruleTemplateAndCategoryDTO;

    }

    private RuleTemplateAndCategoryResponseDTO updateCategoryToCTATemplate(RuleTemplateCategory ruleTemplateCategory, RuleTemplateCategoryRequestDTO ruleTemplateCategoryDTO) {
        List<CTARuleTemplate> ctaRuleTemplates = (List<CTARuleTemplate>) ctaRuleTemplateRepository.findAllById(ruleTemplateCategoryDTO.getRuleTemplateIds());
        for (CTARuleTemplate ctaRuleTemplate : ctaRuleTemplates) {
            ctaRuleTemplate.setRuleTemplateCategoryId(ruleTemplateCategory.getId());
        }
        if (!ctaRuleTemplates.isEmpty()) {
            ctaRuleTemplateRepository.saveEntities(ctaRuleTemplates);
        }
        ruleTemplateCategoryDTO.setId(ruleTemplateCategory.getId());
        return new RuleTemplateAndCategoryResponseDTO(ruleTemplateCategoryDTO, null);
    }

    private RuleTemplateAndCategoryResponseDTO updateCategoryToWTATemplates(RuleTemplateCategory ruleTemplateCategory, RuleTemplateCategoryRequestDTO ruleTemplateCategoryDTO) {
        List<WTABaseRuleTemplate> wtaBaseRuleTemplates = (List<WTABaseRuleTemplate>) wtaBaseRuleTemplateMongoRepository.findAllById(ruleTemplateCategoryDTO.getRuleTemplateIds());
        wtaBaseRuleTemplates.forEach(wtr -> wtr.setRuleTemplateCategoryId(ruleTemplateCategory.getId()));
        if (!wtaBaseRuleTemplates.isEmpty()) {
            wtaBaseRuleTemplateMongoRepository.saveEntities(wtaBaseRuleTemplates);
        }
        ruleTemplateCategoryDTO.setId(ruleTemplateCategory.getId());
        List<WTABaseRuleTemplateDTO> wtaBaseRuleTemplateDTOS = WTABuilderService.copyRuleTemplatesToDTO(wtaBaseRuleTemplates);
        List<TagDTO> tagDTOS = tagMongoRepository.findAllTagsByIdIn(ruleTemplateCategoryDTO.getTags());
        ruleTemplateCategoryDTO.setTags(null);
        RuleTemplateCategoryDTO ruleTemplateCatg = ObjectMapperUtils.copyPropertiesByMapper(ruleTemplateCategoryDTO, RuleTemplateCategoryDTO.class);
        ruleTemplateCatg.setTags(tagDTOS);
        wtaBaseRuleTemplateDTOS.forEach(wtaBaseRuleTemplateDTO -> wtaBaseRuleTemplateDTO.setRuleTemplateCategory(ruleTemplateCatg));
        return new RuleTemplateAndCategoryResponseDTO(ruleTemplateCategoryDTO, wtaBaseRuleTemplateDTOS);
    }


    public List<RuleTemplateCategory> getRulesTemplateCategory(Long countryId, RuleTemplateCategoryType ruleTemplateCategoryType) {
        CountryDTO country = userIntegrationService.getCountryById(countryId);
        if (country == null) {
            excpExceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID, countryId);
        }
        return ruleTemplateCategoryMongoRepository.getRuleTemplateCategoryByCountry(countryId, ruleTemplateCategoryType);

    }

    public boolean exists(BigInteger templateCategoryId) {
        return ruleTemplateCategoryMongoRepository.existsById(templateCategoryId);
    }


    public boolean deleteRuleTemplateCategory(Long countryId, BigInteger templateCategoryId) {
        RuleTemplateCategory ruleTemplateCategory = ruleTemplateCategoryMongoRepository.findOne(templateCategoryId);
        if (ruleTemplateCategory == null) {
            excpExceptionService.dataNotFoundByIdException(MESSAGE_RULETEMPLATECATEGORY_ID, templateCategoryId);
        }

        if (ruleTemplateCategory.getName() != null && ruleTemplateCategory.getName().equals("NONE")) {
            excpExceptionService.actionNotPermittedException(MESSAGE_RULETEMPLATECATEGORY_DELETE, templateCategoryId);
        }
        ruleTemplateService.updateCategoryInTemplate(countryId, templateCategoryId, ruleTemplateCategory);
        ruleTemplateCategory.setDeleted(true);
        ruleTemplateCategoryMongoRepository.save(ruleTemplateCategory);
        return true;

    }

    //Create and Update method should be different
    public RuleTemplateAndCategoryResponseDTO updateRuleTemplateCategory(Long countryId, BigInteger templateCategoryId, RuleTemplateCategoryRequestDTO ruleTemplateCategoryDTO, RuleTemplateCategory ruleTemplateCategoryObj) {
        if (!Optional.ofNullable(ruleTemplateCategoryObj).isPresent()) {
            excpExceptionService.dataNotFoundByIdException(MESSAGE_RULETEMPLATECATEGORY_NAME_NOTFOUND, ruleTemplateCategoryDTO.getName());
        }

        if (!ruleTemplateCategoryDTO.getName().trim().equalsIgnoreCase(ruleTemplateCategoryObj.getName())) {
            RuleTemplateCategory templateCategory = ruleTemplateCategoryMongoRepository.findByName(countryId, ruleTemplateCategoryDTO.getName(), RuleTemplateCategoryType.WTA);
            if (Optional.ofNullable(templateCategory).isPresent()) {
                excpExceptionService.duplicateDataException(MESSAGE_RULETEMPLATECATEGORY_NAME_ALREADYEXIST, ruleTemplateCategoryDTO.getName());
            }
        }
        ruleTemplateCategoryObj.setName(ruleTemplateCategoryDTO.getName());
        ruleTemplateCategoryObj.setDescription(ruleTemplateCategoryDTO.getDescription());
        ruleTemplateCategoryObj.setTags(ruleTemplateCategoryDTO.getTags());
        ruleTemplateCategoryMongoRepository.save(ruleTemplateCategoryObj);
        RuleTemplateAndCategoryResponseDTO ruleTemplateAndCategoryResponseDTO;
        if (ruleTemplateCategoryObj.getRuleTemplateCategoryType().equals(RuleTemplateCategoryType.WTA)) {
            ruleTemplateAndCategoryResponseDTO = updateWTADefaultCategory(countryId, ruleTemplateCategoryObj, ruleTemplateCategoryDTO);
        } else {
            ruleTemplateAndCategoryResponseDTO = updateCTADefaultCategory(countryId, ruleTemplateCategoryObj, ruleTemplateCategoryDTO);
        }
        ruleTemplateAndCategoryResponseDTO.setCategory(null);
        return ruleTemplateAndCategoryResponseDTO;
    }

    private RuleTemplateAndCategoryResponseDTO updateWTADefaultCategory(Long countryId, RuleTemplateCategory ruleTemplateCategoryObj, RuleTemplateCategoryRequestDTO ruleTemplateCategoryDTO) {
        List<WTABaseRuleTemplate> wtaBaseRuleTemplates = wtaBaseRuleTemplateMongoRepository.findAllByCategoryId(ruleTemplateCategoryObj.getId());
        RuleTemplateCategory defaultCategory = ruleTemplateCategoryMongoRepository
                .findByName(countryId, "NONE", WTA);
        wtaBaseRuleTemplates.forEach(wtr -> wtr.setRuleTemplateCategoryId(defaultCategory.getId()));
        ruleTemplateCategoryDTO.setId(ruleTemplateCategoryObj.getId());
        return updateCategoryToWTATemplates(ruleTemplateCategoryObj, ruleTemplateCategoryDTO);
    }

    private RuleTemplateAndCategoryResponseDTO updateCTADefaultCategory(Long countryId, RuleTemplateCategory ruleTemplateCategoryObj, RuleTemplateCategoryRequestDTO ruleTemplateCategoryDTO) {
        List<CTARuleTemplate> ctaRuleTemplates = ctaRuleTemplateRepository.findAllByCategoryId(ruleTemplateCategoryObj.getId());
        RuleTemplateCategory defaultCategory = ruleTemplateCategoryMongoRepository
                .findByName(countryId, "NONE", CTA);
        ctaRuleTemplates.forEach(ctr -> ctr.setRuleTemplateCategoryId(defaultCategory.getId()));
        ruleTemplateCategoryDTO.setId(ruleTemplateCategoryObj.getId());
        return updateCategoryToCTATemplate(ruleTemplateCategoryObj, ruleTemplateCategoryDTO);
    }


    public RuleTemplateCategoryRequestDTO updateRuleTemplateCategory(Long countryId, BigInteger templateCategoryId, RuleTemplateCategoryRequestDTO ruleTemplateCategory) {
        CountryDTO country = userIntegrationService.getCountryById(countryId);
        if (!Optional.ofNullable(country).isPresent()) {
            excpExceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID, countryId);
        }
        RuleTemplateCategory ruleTemplateCategoryObj = ruleTemplateCategoryMongoRepository.findOne(templateCategoryId);
        ruleTemplateCategoryObj.setName(ruleTemplateCategory.getName());
        ruleTemplateCategoryObj.setDescription(ruleTemplateCategory.getDescription());
        ruleTemplateCategoryObj.setTags(ruleTemplateCategory.getTags());
        ruleTemplateCategoryMongoRepository.save(ruleTemplateCategoryObj);
        ruleTemplateCategory.setId(ruleTemplateCategoryObj.getId());
        return ruleTemplateCategory;
    }

    public Map<String, TranslationInfo> updateTranslation(BigInteger categoryId, Map<String,TranslationInfo> translations) {
        RuleTemplateCategory ruleTemplateCategory= ruleTemplateCategoryMongoRepository.findOne(categoryId);
        ruleTemplateCategory.setTranslations(translations);
        ruleTemplateCategoryMongoRepository.save(ruleTemplateCategory);
        return ruleTemplateCategory.getTranslations();
    }



}
