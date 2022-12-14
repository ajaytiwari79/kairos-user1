package com.kairos.service.wta;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.activity.cta.CTAResponseDTO;
import com.kairos.dto.activity.cta.CTAWTAAndAccumulatedTimebankWrapper;
import com.kairos.dto.activity.tags.TagDTO;
import com.kairos.dto.activity.wta.basic_details.WTABaseRuleTemplateDTO;
import com.kairos.dto.activity.wta.basic_details.WTADTO;
import com.kairos.dto.activity.wta.basic_details.WTAResponseDTO;
import com.kairos.dto.user.organization.OrganizationDTO;
import com.kairos.persistence.model.wta.WTAQueryResultDTO;
import com.kairos.persistence.model.wta.WorkingTimeAgreement;
import com.kairos.persistence.model.wta.templates.WTABaseRuleTemplate;
import com.kairos.persistence.repository.cta.CostTimeAgreementRepository;
import com.kairos.persistence.repository.tag.TagMongoRepository;
import com.kairos.persistence.repository.wta.WorkingTimeAgreementMongoRepository;
import com.kairos.persistence.repository.wta.rule_template.RuleTemplateCategoryRepository;
import com.kairos.persistence.repository.wta.rule_template.WTABaseRuleTemplateMongoRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.exception.ExceptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.asDate;
import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.ActivityMessagesConstants.*;


/**
 * Created by vipul on 19/12/17.
 */

@Transactional
@Service
public class WTAOrganizationService  {

    @Inject
    private WorkingTimeAgreementMongoRepository workingTimeAgreementMongoRepository;
    @Inject
    private RuleTemplateCategoryRepository ruleTemplateCategoryMongoRepository;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Inject
    @Lazy
    private RuleTemplateService ruleTemplateService;
    @Inject
    private WTABuilderService wtaBuilderService;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private CostTimeAgreementRepository costTimeAgreementRepository;
    @Inject
    private WorkTimeAgreementService workTimeAgreementService;
    @Inject
    private TagMongoRepository tagMongoRepository;
    @Inject private WTABaseRuleTemplateMongoRepository wtaBaseRuleTemplateMongoRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(WTAOrganizationService.class);

    public List<WTAResponseDTO> getAllWTAByOrganization(Long unitId) {
        OrganizationDTO organization = userIntegrationService.getOrganizationWithCountryId(unitId);
        if (!Optional.ofNullable(organization).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_UNIT_ID, unitId);
        }
        List<WTAQueryResultDTO> workingTimeAgreements = workingTimeAgreementMongoRepository.getWtaByOrganization(unitId);
        List<WTAResponseDTO> wtaResponseDTOs = new ArrayList<>();
        workingTimeAgreements.forEach(wta -> {
            WTAResponseDTO wtaResponseDTO = ObjectMapperUtils.copyPropertiesByMapper(wta, WTAResponseDTO.class);
            ruleTemplateService.assignCategoryToRuleTemplate(organization.getCountryId(), wtaResponseDTO.getRuleTemplates());
            wtaResponseDTOs.add(wtaResponseDTO);
        });
        return wtaResponseDTOs;
    }


    public WTAResponseDTO updateWtaOfOrganization(Long unitId, BigInteger wtaId, WTADTO updateDTO) {
        WorkingTimeAgreement oldWta = workingTimeAgreementMongoRepository.findOne(wtaId);
        List<WTABaseRuleTemplate> wtaBaseRuleTemplates = new ArrayList<>();
        if (isCollectionNotEmpty(updateDTO.getRuleTemplates())) {
            wtaBaseRuleTemplates = wtaBuilderService.copyRuleTemplates(updateDTO.getRuleTemplates(), false);
        }
        boolean isValueChanged =workTimeAgreementService.isCalculatedValueChangedForWTA(oldWta,wtaBaseRuleTemplates);
        if ((isValueChanged && !oldWta.getStartDate().equals(updateDTO.getStartDate()) && updateDTO.getStartDate().isBefore(LocalDate.now()))) {
            exceptionService.actionNotPermittedException(MESSAGE_WTA_START_ENDDATE);
        }
        OrganizationDTO organization = validateAndGetOrganizationDetails(unitId, wtaId, updateDTO, oldWta);
        WorkingTimeAgreement newWta = new WorkingTimeAgreement();
        BeanUtils.copyProperties(oldWta, newWta);
        newWta.setId(null);
        newWta.setDeleted(true);
        newWta.setStartDate(oldWta.getStartDate());
        newWta.setEndDate(updateDTO.getStartDate());
        newWta.setCountryParentWTA(null);
        workingTimeAgreementMongoRepository.save(newWta);
        if (Optional.ofNullable(oldWta.getParentId()).isPresent()) {
            WorkingTimeAgreement workingTimeAgreement = workingTimeAgreementMongoRepository.findOne(oldWta.getParentId());
            workingTimeAgreement.setDeleted(true);
            workingTimeAgreementMongoRepository.save(workingTimeAgreement);
        }
        List<WTABaseRuleTemplate> ruleTemplates = updateOldWTA(updateDTO, oldWta, organization, newWta);
        //Preparing Response for frontend
        oldWta.setParentId(newWta.getParentId());
        List<TagDTO> tags = getTags(oldWta);
        WTAResponseDTO wtaResponseDTO = ObjectMapperUtils.copyPropertiesByMapper(oldWta, WTAResponseDTO.class);
        wtaResponseDTO.setStartDate(oldWta.getStartDate());
        wtaResponseDTO.setEndDate(oldWta.getEndDate());
        wtaResponseDTO.setTags(tags);
        List<WTABaseRuleTemplateDTO> wtaBaseRuleTemplateDTOS = WTABuilderService.copyRuleTemplatesToDTO(ruleTemplates);
        ruleTemplateService.assignCategoryToRuleTemplate(organization.getCountryId(), wtaBaseRuleTemplateDTOS);
        wtaResponseDTO.setRuleTemplates(wtaBaseRuleTemplateDTOS);
        return wtaResponseDTO;
    }

    private List<WTABaseRuleTemplate> updateOldWTA(WTADTO updateDTO, WorkingTimeAgreement oldWta, OrganizationDTO organization, WorkingTimeAgreement newWta) {
        oldWta.setName(updateDTO.getName());
        oldWta.setDescription(updateDTO.getDescription());
        oldWta.setTags(updateDTO.getTags());
        oldWta.setStartDate(updateDTO.getStartDate());
        oldWta.setEndDate(updateDTO.getEndDate());
        oldWta.setExpertise(oldWta.getExpertise());
        oldWta.setParentId(newWta.getId());
        oldWta.setDisabled(false);
        List<WTABaseRuleTemplate> ruleTemplates = createWtaBaseRuleTemplates(updateDTO, oldWta, organization);
        workingTimeAgreementMongoRepository.save(oldWta);
        return ruleTemplates;
    }

    private List<TagDTO> getTags(WorkingTimeAgreement oldWta) {
        List<TagDTO> tags = null;
        if (isCollectionNotEmpty(oldWta.getTags())) {
            tags = tagMongoRepository.findAllTagsByIdIn(oldWta.getTags());
            oldWta.setTags(null);
        }
        return tags;
    }

    private OrganizationDTO validateAndGetOrganizationDetails(Long unitId, BigInteger wtaId, WTADTO updateDTO, WorkingTimeAgreement oldWta) {
        WorkingTimeAgreement agreement = workingTimeAgreementMongoRepository.checkUniqueWTANameInOrganization(updateDTO.getName(), unitId, wtaId);
        if (Optional.ofNullable(agreement).isPresent()) {
            LOGGER.info("Duplicate WTA name in organization {}", wtaId);
            exceptionService.duplicateDataException(MESSAGE_WTA_NAME_ALREADYEXISTS, updateDTO.getName());
        }

        if (!Optional.ofNullable(oldWta).isPresent()) {
            LOGGER.info("wta not found while updating at unit {}", wtaId);
            exceptionService.dataNotFoundByIdException(MESSAGE_WTA_ID, wtaId);
        }
        if (oldWta.getExpertise().getId() != updateDTO.getExpertiseId()) {
            LOGGER.info("Expertise cant be changed at unit level {}", wtaId);
            exceptionService.actionNotPermittedException("message.expertise.unitlevel.update", wtaId);
        }
        OrganizationDTO organization = userIntegrationService.getOrganizationWithCountryId(unitId);
        if (!Optional.ofNullable(organization).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_UNIT_ID, unitId);
        }
        return organization;
    }

    private List<WTABaseRuleTemplate> createWtaBaseRuleTemplates(WTADTO updateDTO, WorkingTimeAgreement oldWta, OrganizationDTO organization) {
        List<WTABaseRuleTemplate> ruleTemplates = new ArrayList<>();
        if (isCollectionNotEmpty(updateDTO.getRuleTemplates())) {
            ruleTemplates = wtaBuilderService.copyRuleTemplates(updateDTO.getRuleTemplates(), true);
            for (WTABaseRuleTemplate ruleTemplate : ruleTemplates) {
                workTimeAgreementService.updateExistingPhaseIdOfWTA(ruleTemplate.getPhaseTemplateValues(), organization.getId(), organization.getCountryId(), true);
            }
            wtaBaseRuleTemplateMongoRepository.saveEntities(ruleTemplates);
            List<BigInteger> ruleTemplatesIds = ruleTemplates.stream().map(ruleTemplate -> ruleTemplate.getId()).collect(Collectors.toList());
            oldWta.setRuleTemplateIds(ruleTemplatesIds);
        }
        return ruleTemplates;
    }


    public CTAWTAAndAccumulatedTimebankWrapper getAllWtaOfOrganizationByExpertise(Long unitId, Long expertiseId, LocalDate selectedDate,Long employmentId) {
        List<WTAQueryResultDTO> wtaQueryResultDTOS=new ArrayList<>();
        wtaQueryResultDTOS.addAll(workingTimeAgreementMongoRepository.getAllWtaOfOrganizationByExpertise(unitId, expertiseId, selectedDate));
        List<WTAResponseDTO> wtaResponseDTOS = ObjectMapperUtils.copyCollectionPropertiesByMapper(wtaQueryResultDTOS, WTAResponseDTO.class);
        List<CTAResponseDTO> ctaResponseDTOS = costTimeAgreementRepository.getDefaultCTAOfExpertiseAndDate(unitId, expertiseId, selectedDate);
        if(isNotNull(employmentId)){
            List<WTAResponseDTO> wtaResponseDTOList;
            List<CTAResponseDTO> ctaResponseDTOList;
            if(isNotNull(selectedDate)){
                Date date = asDate(selectedDate);
                WTAQueryResultDTO wtaByEmploymentIdAndDate = workingTimeAgreementMongoRepository.getWTAByEmploymentIdAndDate(employmentId, date);
                CTAResponseDTO ctaResponseDTO = costTimeAgreementRepository.getCTAByEmploymentIdAndDate(employmentId,date);
                wtaResponseDTOList = newArrayList(new WTAResponseDTO(wtaByEmploymentIdAndDate.getName(), wtaByEmploymentIdAndDate.getId(), wtaByEmploymentIdAndDate.getParentId()));
                ctaResponseDTOList = newArrayList(new CTAResponseDTO(ctaResponseDTO.getName(), ctaResponseDTO.getId(), ctaResponseDTO.getParentId()));
            }else {
                wtaResponseDTOList =workingTimeAgreementMongoRepository.getWTAByEmployment(employmentId).stream().map(wta -> new WTAResponseDTO(wta.getName(), wta.getId(), wta.getParentId())).collect(Collectors.toList());
                ctaResponseDTOList = costTimeAgreementRepository.getCTAByEmployment(employmentId).stream().map(cta->new CTAResponseDTO(cta.getName(), cta.getId(), cta.getParentId())).collect(Collectors.toList());
            }
            Set<String> employmentWTANames = wtaResponseDTOList.stream().map(wtaResponseDTO -> wtaResponseDTO.getName()).collect(Collectors.toSet());
            Set<String> employmentCTANames = ctaResponseDTOList.stream().map(ctaResponseDTO -> ctaResponseDTO.getName()).collect(Collectors.toSet());
            wtaResponseDTOS.removeIf(wtaQueryResultDTO -> employmentWTANames.contains(wtaQueryResultDTO.getName()));
            ctaResponseDTOS.removeIf(ctaQueryResultDTO -> employmentCTANames.contains(ctaQueryResultDTO.getName()));
            wtaResponseDTOS.addAll(wtaResponseDTOList);
            ctaResponseDTOS.addAll(ctaResponseDTOList);
        }
        return new CTAWTAAndAccumulatedTimebankWrapper(ctaResponseDTOS, wtaResponseDTOS);
    }


}