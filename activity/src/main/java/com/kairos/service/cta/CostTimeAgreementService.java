package com.kairos.service.cta;

import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.commons.utils.ObjectUtils;
import com.kairos.dto.TranslationInfo;
import com.kairos.dto.activity.activity.TableConfiguration;
import com.kairos.dto.activity.activity.activity_tabs.ActivityWithCTAWTASettingsDTO;
import com.kairos.dto.activity.cta.*;
import com.kairos.dto.activity.phase.PhaseDTO;
import com.kairos.dto.activity.presence_type.PresenceTypeDTO;
import com.kairos.dto.activity.shift.StaffEmploymentDetails;
import com.kairos.dto.activity.tags.TagDTO;
import com.kairos.dto.activity.time_type.TimeTypeDTO;
import com.kairos.dto.activity.wta.rule_template_category.RuleTemplateCategoryDTO;
import com.kairos.dto.user.country.agreement.cta.CalculationFor;
import com.kairos.dto.user.country.agreement.cta.cta_response.DayTypeDTO;
import com.kairos.dto.user.country.basic_details.CountryDTO;
import com.kairos.dto.user.country.experties.ExpertiseResponseDTO;
import com.kairos.dto.user.employment.EmploymentLinesDTO;
import com.kairos.dto.user.organization.OrganizationDTO;
import com.kairos.dto.user.organization.OrganizationTypeDTO;
import com.kairos.dto.user.staff.StaffFilterDTO;
import com.kairos.dto.user.user.staff.StaffAdditionalInfoDTO;
import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.FilterType;
import com.kairos.enums.RuleTemplateCategoryType;
import com.kairos.enums.cta.ActivityTypeForCostCalculation;
import com.kairos.enums.phase.PhaseDefaultName;
import com.kairos.persistence.model.activity.tabs.ActivityCategory;
import com.kairos.persistence.model.common.MongoBaseEntity;
import com.kairos.persistence.model.cta.CTARuleTemplate;
import com.kairos.persistence.model.cta.CostTimeAgreement;
import com.kairos.persistence.model.phase.Phase;
import com.kairos.persistence.model.wta.WTAOrganization;
import com.kairos.persistence.model.wta.templates.RuleTemplateCategory;
import com.kairos.persistence.repository.cta.CTARuleTemplateRepository;
import com.kairos.persistence.repository.cta.CostTimeAgreementRepository;
import com.kairos.persistence.repository.phase.PhaseMongoRepository;
import com.kairos.persistence.repository.wta.rule_template.RuleTemplateCategoryRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.activity.ActivityService;
import com.kairos.service.activity.PlannedTimeTypeService;
import com.kairos.service.activity.TimeTypeService;
import com.kairos.service.cta_compensation_settings.CTACompensationSettingService;
import com.kairos.service.day_type.DayTypeService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.phase.PhaseService;
import com.kairos.service.table_settings.TableSettingService;
import com.kairos.service.time_bank.TimeBankService;
import com.kairos.service.unit_settings.ProtectedDaysOffService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.ActivityMessagesConstants.*;
import static com.kairos.constants.AppConstants.COPY_OF;
import static com.kairos.constants.AppConstants.ORGANIZATION;
import static com.kairos.enums.FilterType.CTA_ACCOUNT_TYPE;
import static com.kairos.persistence.model.constants.TableSettingConstants.ORGANIZATION_CTA_AGREEMENT_VERSION_TABLE_ID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


/**
 * @author pradeep
 * @date - 07/08/18
 */

@Transactional
@Service
public class CostTimeAgreementService {
    private static final Logger logger = LoggerFactory.getLogger(CostTimeAgreementService.class);


    @Inject
    private RuleTemplateCategoryRepository ruleTemplateCategoryRepository;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Inject
    private CTARuleTemplateRepository ctaRuleTemplateRepository;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private CountryCTAService countryCTAService;
    @Inject
    private CostTimeAgreementRepository costTimeAgreementRepository;
    @Inject
    private TableSettingService tableSettingService;
    @Inject
    private ActivityService activityService;
    @Inject
    private PhaseMongoRepository phaseMongoRepository;
    @Inject
    private TimeBankService timeBankService;
    @Inject private CTACompensationSettingService ctaCompensationSettingService;
    @Inject
    private DayTypeService dayTypeService;
    @Inject
    private ProtectedDaysOffService protectedDaysOffService;
    @Inject private PhaseService phaseService;
    @Inject private TimeTypeService timeTypeService;
    @Inject private PlannedTimeTypeService plannedTimeTypeService;


    /**
     * @param countryId
     * @return boolean
     */
    public boolean createDefaultCtaRuleTemplate(Long countryId) {
        RuleTemplateCategory category = ruleTemplateCategoryRepository.findByName(countryId, "NONE", RuleTemplateCategoryType.CTA);
        if (category == null) {
            category = new RuleTemplateCategory("NONE", "None", RuleTemplateCategoryType.CTA);
            category.setCountryId(countryId);
            ruleTemplateCategoryRepository.save(category);
            /*CountryDTO country = countryRestClient.getCountryById(countryId);
            if (country != null) {
                List<CTARuleTemplate> ctaRuleTemplates = createDefaultRuleTemplate(countryId, country.getCurrencyId(), category.getId());
                save(ctaRuleTemplates);
            }*/
        } else {
            logger.info("default CTARuleTemplateCategory is not exist");
        }
        return true;
    }


    /**
     * @param countryId
     * @param ctaRuleTemplateDTO
     * @return CTARuleTemplateDTO
     */
    public CTARuleTemplateDTO createCTARuleTemplate(Long countryId, CTARuleTemplateDTO ctaRuleTemplateDTO) {
        if (ctaRuleTemplateRepository.isCTARuleTemplateExistWithSameName(countryId, ctaRuleTemplateDTO.getName())) {
            exceptionService.dataNotFoundByIdException(MESSAGE_CTA_RULETEMPLATE_ALREADYEXIST, ctaRuleTemplateDTO.getName());
        }
        CountryDTO countryDTO = userIntegrationService.getCountryById(countryId);
        ctaRuleTemplateDTO.setId(null);
        ctaRuleTemplateDTO.setRuleTemplateType(ctaRuleTemplateDTO.getName());
        CTARuleTemplate ctaRuleTemplate = ObjectMapperUtils.copyPropertiesByMapper(ctaRuleTemplateDTO, CTARuleTemplate.class);
        this.buildCTARuleTemplate(ctaRuleTemplate, ctaRuleTemplateDTO, false, countryDTO);
        ctaRuleTemplate.setCountryId(countryId);
        ctaRuleTemplate.setStaffFunctions(null);
        if(CalculationFor.CONDITIONAL_BONUS.equals(ctaRuleTemplate.getCalculationFor())){
            ctaCompensationSettingService.validateInterval(ctaRuleTemplate.getCalculateValueAgainst().getCtaCompensationConfigurations());
        }
        ctaRuleTemplateRepository.save(ctaRuleTemplate);
        ctaRuleTemplateDTO.setId(ctaRuleTemplate.getId());
        return ctaRuleTemplateDTO;
    }


    /**
     * @param countryId
     * @param organizationSubTypeIdList
     * @param organizationId
     */
    public void assignCountryCTAtoOrganisation(Long countryId, List<Long> organizationSubTypeIdList, Long organizationId) {
        List<CostTimeAgreement> costTimeAgreements = new ArrayList<>();
        for (Long organizationSubTypeId : organizationSubTypeIdList) {
            List<CTAResponseDTO> ctaResponseDTOS = costTimeAgreementRepository.getAllCTAByOrganizationSubType(countryId, organizationSubTypeId);
            List<BigInteger> activityIds = ctaResponseDTOS.stream().flatMap(ctaResponseDTO -> ctaResponseDTO.getRuleTemplates().stream()).filter(ruleTemp -> Optional.ofNullable(ruleTemp.getActivityIds()).isPresent()).flatMap(ctaRuleTemplateDTO -> ctaRuleTemplateDTO.getActivityIds().stream()).collect(Collectors.toList());
            List<Long> unitIds = Arrays.asList(organizationId);
            Map<Long, Map<Long, BigInteger>> unitActivities = activityService.getListOfActivityIdsOfUnitByParentIds(activityIds, unitIds);
            for (CTAResponseDTO ctaResponseDTO : ctaResponseDTOS) {
                //TODO Refactor Tag assignment in WTA
                CostTimeAgreement organisationCTA = createCloneCostTimeAgreementForOrganization(countryId, organizationId, unitActivities, ctaResponseDTO);
                costTimeAgreements.add(organisationCTA);
            }
        }
        if (!costTimeAgreements.isEmpty()) {
            costTimeAgreementRepository.saveEntities(costTimeAgreements);
        }

    }

    private CostTimeAgreement createCloneCostTimeAgreementForOrganization(Long countryId, Long organizationId, Map<Long, Map<Long, BigInteger>> unitActivities, CTAResponseDTO ctaResponseDTO) {
        ctaResponseDTO.setTags(null);
        CostTimeAgreement organisationCTA = ObjectMapperUtils.copyPropertiesByMapper(ctaResponseDTO, CostTimeAgreement.class);
        // Set activity Ids according to unit activity Ids
        organisationCTA.setId(null);
        assignOrganisationActivitiesToRuleTemplate(ctaResponseDTO.getRuleTemplates(), unitActivities.get(organisationCTA.getId()));
        organisationCTA.setOrganization(new WTAOrganization(organizationId, "", ""));
        organisationCTA.setParentCountryCTAId(ctaResponseDTO.getId());
        updateExistingPhaseIdOfCTA(ctaResponseDTO.getRuleTemplates(), organizationId, countryId);
        List<CTARuleTemplate> ruleTemplates = ObjectMapperUtils.copyCollectionPropertiesByMapper(ctaResponseDTO.getRuleTemplates(), CTARuleTemplate.class);
        List<BigInteger> ruleTemplateIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ruleTemplates)) {
            ruleTemplates.forEach(ctaRuleTemplate -> {
                ctaRuleTemplate.setId(null);
            });
            ctaRuleTemplateRepository.saveEntities(ruleTemplates);
            ruleTemplateIds = ruleTemplates.stream().map(rt -> rt.getId()).collect(Collectors.toList());
        }
        organisationCTA.setRuleTemplateIds(ruleTemplateIds);
        return organisationCTA;
    }

    public Map<Long, Map<PhaseDefaultName, BigInteger>> getMapOfPhaseIdsAndUnitByParentIds(List<Long> unitIds) {
        List<Phase> unitPhases = phaseMongoRepository.findAllByUnitIdsAndDeletedFalse(unitIds);
        Map<Long, List<Phase>> phasesOrganizationMap = unitPhases.stream().collect(Collectors.groupingBy(k -> k.getOrganizationId(), Collectors.toList()));
        Map<Long, Map<PhaseDefaultName, BigInteger>> organizationPhasesMapWithParentCountryPhaseId = new HashMap<>();
        phasesOrganizationMap.forEach((organisationId, phaseDTOS) -> {
            Map<PhaseDefaultName, BigInteger> parentPhasesAndUnitPhaseIdMap = phaseDTOS.stream().collect(Collectors.toMap(k -> k.getPhaseEnum(), v -> v.getId()));
            organizationPhasesMapWithParentCountryPhaseId.put(organisationId, parentPhasesAndUnitPhaseIdMap);
        });
        return organizationPhasesMapWithParentCountryPhaseId;
    }

    public void assignOrganisationActivitiesToRuleTemplate(List<CTARuleTemplateDTO> ruleTemplateDTOS, Map<Long, BigInteger> parentUnitActivityMap) {
        ruleTemplateDTOS.forEach(ctaRuleTemplateDTO -> {
            Set<BigInteger> parentActivityIds = ctaRuleTemplateDTO.getActivityIds();
            if (parentActivityIds != null) {
                Set<BigInteger> unitActivityIds = new HashSet<>();
                parentActivityIds.forEach(parentActivityId -> {
                    if (Optional.ofNullable(parentUnitActivityMap).isPresent() && Optional.ofNullable(parentUnitActivityMap.get(parentActivityId)).isPresent()) {
                        unitActivityIds.add(parentUnitActivityMap.get(parentActivityId));
                    }
                });
                ctaRuleTemplateDTO.setActivityIds(unitActivityIds);
            }
        });
    }

    /**
     * @param unitId
     * @param ctaId
     * @return List<CTARuleTemplateDTO>
     */
    public List<CTARuleTemplateDTO> getCTARuleTemplateOfUnit(Long unitId, BigInteger ctaId) {
        CTAResponseDTO ctaResponseDTO = costTimeAgreementRepository.getOneCtaById(ctaId);
        List<CTARuleTemplateDTO> ctaRuleTemplateDTOS = new ArrayList<>();
        if (Optional.ofNullable(ctaResponseDTO).isPresent()) {
            ctaRuleTemplateDTOS = ctaResponseDTO.getRuleTemplates();
        }
        ctaRuleTemplateDTOS.forEach(ctaRuleTemplateDTO -> {
            ctaRuleTemplateDTO.setUnitId(unitId);
        });
        return ctaRuleTemplateDTOS;
    }


    /**
     * @param countryId
     * @param ctaId
     * @return List<CTARuleTemplateDTO>
     */
    public List<CTARuleTemplateDTO> getCTARuleTemplateOfCountry(Long countryId, BigInteger ctaId) {
        CTAResponseDTO ctaResponseDTO = costTimeAgreementRepository.getOneCtaById(ctaId);
        List<CTARuleTemplateDTO> ctaRuleTemplateDTOS = new ArrayList<>();
        if (ctaResponseDTO != null) {
            ctaRuleTemplateDTOS = ctaResponseDTO.getRuleTemplates();
        }
        return ctaRuleTemplateDTOS;
    }


    public CTAResponseDTO getEmploymentCTA(Long unitId, Long employmentId) {
        EmploymentDTO employment = userIntegrationService.getEmploymentDTO(unitId, employmentId);
        if (!Optional.ofNullable(employment).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.InvalidEmploymentId", employmentId);
        }
        return costTimeAgreementRepository.getOneCtaById(employment.getCostTimeAgreementId());
    }



    @CacheEvict(value = "getAccumulatedTimebankAndDelta", key = "{#employmentId, true}")
    public StaffEmploymentDetails updateCostTimeAgreementForEmployment(Long unitId, Long employmentId, BigInteger ctaId, CollectiveTimeAgreementDTO ctaDTO,Boolean save) {
        StaffAdditionalInfoDTO staffAdditionalInfoDTO = userIntegrationService.verifyUnitEmploymentOfStaffByEmploymentId(unitId, null, ORGANIZATION, employmentId, new HashSet<>(),null);
        staffAdditionalInfoDTO.setDayTypes(dayTypeService.getDayTypeWithCountryHolidayCalender(UserContext.getUserDetails().getCountryId()));
        staffAdditionalInfoDTO.getEmployment().getExpertise().setProtectedDaysOffSettings(protectedDaysOffService.getProtectedDaysOffByExpertiseId(UserContext.getUserDetails().getCountryId()));
        CostTimeAgreement oldCTA = costTimeAgreementRepository.findOne(ctaId);
        validateEmploymentCTAWhileUpdate(ctaDTO,staffAdditionalInfoDTO,oldCTA);
        CTAResponseDTO responseCTA = null;
        boolean calculatedValueChanged = isCalculatedValueChanged(oldCTA.getRuleTemplateIds(), ctaDTO.getRuleTemplates());
        if(calculatedValueChanged && isNull(ctaDTO.getPublishDate())){
            exceptionService.actionNotPermittedException(ERROR_VALUE_CHANGED_PUBLISH_DATE_NULL,"CTA");
        }
        if (!staffAdditionalInfoDTO.getEmployment().isPublished() || isNull(ctaDTO.getPublishDate())) {
            responseCTA = updateEmploymentCTA(oldCTA, ctaDTO);
        }else {
            if (!calculatedValueChanged) {
                exceptionService.actionNotPermittedException(MESSAGE_CTA_VALUE,"CTA");
            } else {
                responseCTA = updateEmploymentCTAWhenCalculatedValueChanged(oldCTA, ctaDTO);
            }
        }
        staffAdditionalInfoDTO.getEmployment().setCostTimeAgreement(responseCTA);
        timeBankService.updateDailyTimeBankOnCTAChangeOfEmployment(staffAdditionalInfoDTO, responseCTA);
        return staffAdditionalInfoDTO.getEmployment();
    }

    private CTAResponseDTO updateEmploymentCTA(CostTimeAgreement costTimeAgreement, CollectiveTimeAgreementDTO ctaDTO) {
        List<CTARuleTemplate> ctaRuleTemplates = ObjectMapperUtils.copyCollectionPropertiesByMapper(ctaDTO.getRuleTemplates(), CTARuleTemplate.class);
        ctaRuleTemplates.forEach(ctaRuleTemplate -> {
            if(CalculationFor.CONDITIONAL_BONUS.equals(ctaRuleTemplate.getCalculationFor())){
                ctaCompensationSettingService.validateInterval(ctaRuleTemplate.getCalculateValueAgainst().getCtaCompensationConfigurations());
            }
            ctaRuleTemplate.setId(null);
        });
        if (CollectionUtils.isNotEmpty(ctaRuleTemplates)) {
            ctaRuleTemplateRepository.saveEntities(ctaRuleTemplates);
        }
        List<BigInteger> ruleTemplateIds = ctaRuleTemplates.stream().map(MongoBaseEntity::getId).collect(Collectors.toList());
        costTimeAgreement.setRuleTemplateIds(ruleTemplateIds);
        costTimeAgreement.setStartDate(ctaDTO.getStartDate());
        costTimeAgreement.setEndDate(ctaDTO.getEndDate());
        costTimeAgreement.setDescription(ctaDTO.getDescription());
        costTimeAgreement.setName(ctaDTO.getName());
        costTimeAgreementRepository.save(costTimeAgreement);
        List<CTARuleTemplateDTO> ctaRuleTemplateDTOS = ObjectMapperUtils.copyCollectionPropertiesByMapper(ctaRuleTemplates, CTARuleTemplateDTO.class);
        ExpertiseResponseDTO expertiseResponseDTO = ObjectMapperUtils.copyPropertiesByMapper(costTimeAgreement.getExpertise(), ExpertiseResponseDTO.class);
        return new CTAResponseDTO(costTimeAgreement.getId(), costTimeAgreement.getName(), expertiseResponseDTO, ctaRuleTemplateDTOS, costTimeAgreement.getStartDate(), costTimeAgreement.getEndDate(), false, costTimeAgreement.getEmploymentId(), costTimeAgreement.getDescription());
    }

    private CTAResponseDTO updateEmploymentCTAWhenCalculatedValueChanged(CostTimeAgreement oldCTA, CollectiveTimeAgreementDTO ctaDTO) {
        ctaDTO.setId(null);
        LocalDate publishDate = ctaDTO.getPublishDate();
        ctaDTO.setPublishDate(null);
        CostTimeAgreement costTimeAgreement = ObjectMapperUtils.copyPropertiesByMapper(ctaDTO, CostTimeAgreement.class);
        List<CTARuleTemplate> ctaRuleTemplates = getCtaRuleTemplates(ctaDTO, costTimeAgreement);
        costTimeAgreement.setId(oldCTA.getId());
        oldCTA.setId(null);
        oldCTA.setEndDate(publishDate.equals(oldCTA.getStartDate()) ? oldCTA.getStartDate() : publishDate.minusDays(1));
        costTimeAgreementRepository.save(oldCTA);
        setDataInCostTimeAgreement(oldCTA, ctaDTO, costTimeAgreement);
        costTimeAgreementRepository.save(costTimeAgreement);
        List<CTARuleTemplateDTO> ctaRuleTemplateDTOS = ObjectMapperUtils.copyCollectionPropertiesByMapper(ctaRuleTemplates, CTARuleTemplateDTO.class);
        ExpertiseResponseDTO expertiseResponseDTO = ObjectMapperUtils.copyPropertiesByMapper(oldCTA.getExpertise(), ExpertiseResponseDTO.class);
        CTAResponseDTO responseCTA = new CTAResponseDTO(costTimeAgreement.getId(), costTimeAgreement.getName(), expertiseResponseDTO, ctaRuleTemplateDTOS, costTimeAgreement.getStartDate(), costTimeAgreement.getEndDate(), false, oldCTA.getEmploymentId(), costTimeAgreement.getDescription());
        responseCTA.setEndDate((isNotNull(responseCTA.getEndDate())&&responseCTA.getStartDate().isBefore(responseCTA.getEndDate()))?responseCTA.getEndDate():null);
        responseCTA.setParentId(oldCTA.getId());
        responseCTA.setOrganizationParentId(oldCTA.getOrganizationParentId());
        responseCTA.setTranslations(costTimeAgreement.getTranslations());
        CTAResponseDTO versionCTA = ObjectMapperUtils.copyPropertiesByMapper(oldCTA, CTAResponseDTO.class);
        List<CTARuleTemplate> existingCtaRuleTemplates = ctaRuleTemplateRepository.findAllByIdAndDeletedFalse(oldCTA.getRuleTemplateIds());
        List<CTARuleTemplateDTO> existingCtaRuleTemplatesDTOS = ObjectMapperUtils.copyCollectionPropertiesByMapper(existingCtaRuleTemplates, CTARuleTemplateDTO.class);
        versionCTA.setRuleTemplates(existingCtaRuleTemplatesDTOS);
        responseCTA.setVersions(Arrays.asList(versionCTA));
        return responseCTA;
    }

    private void setDataInCostTimeAgreement(CostTimeAgreement oldCTA, CollectiveTimeAgreementDTO ctaDTO, CostTimeAgreement costTimeAgreement) {
        costTimeAgreement.setStartDate(oldCTA.getEndDate().plusDays(1));
        costTimeAgreement.setParentId(oldCTA.getId());
        costTimeAgreement.setOrganizationParentId(oldCTA.getOrganizationParentId());
        costTimeAgreement.setExpertise(oldCTA.getExpertise());
        costTimeAgreement.setOrganizationType(oldCTA.getOrganizationType());
        costTimeAgreement.setOrganizationSubType(oldCTA.getOrganizationSubType());
        costTimeAgreement.setOrganization(oldCTA.getOrganization());
        costTimeAgreement.setEmploymentId(oldCTA.getEmploymentId());
        costTimeAgreement.setDescription(ctaDTO.getDescription());
    }

    private void validateEmploymentCTAWhileUpdate(CollectiveTimeAgreementDTO collectiveTimeAgreementDTO,StaffAdditionalInfoDTO staffAdditionalInfoDTO,CostTimeAgreement oldCTA){
        if (!Optional.ofNullable(staffAdditionalInfoDTO.getEmployment()).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.InvalidEmploymentId", staffAdditionalInfoDTO.getEmployment().getId());
        }
        if ((staffAdditionalInfoDTO.getEmployment().getEndDate() != null && collectiveTimeAgreementDTO.getEndDate() != null && collectiveTimeAgreementDTO.getEndDate().isBefore(staffAdditionalInfoDTO.getEmployment().getEndDate())) || (isNull(oldCTA.getEndDate()) && isNull(staffAdditionalInfoDTO.getEmployment().getEndDate()) && isNotNull(collectiveTimeAgreementDTO.getEndDate()))) {
            exceptionService.actionNotPermittedException(END_DATE_FROM_END_DATE, "CTA");
        }
        if (staffAdditionalInfoDTO.getEmployment().getEndDate() != null && collectiveTimeAgreementDTO.getStartDate().isAfter(staffAdditionalInfoDTO.getEmployment().getEndDate())) {
            exceptionService.actionNotPermittedException(START_DATE_FROM_END_DATE, "CTA");
        }
        if(staffAdditionalInfoDTO.getEmployment().isPublished()){
            if(isNotNull(collectiveTimeAgreementDTO.getPublishDate()) && collectiveTimeAgreementDTO.getPublishDate().isBefore(LocalDate.now())){
                exceptionService.actionNotPermittedException(PUBLISH_DATE_SHOULD_BE_IN_FUTURE);
            }
            else if(isNotNull(collectiveTimeAgreementDTO.getPublishDate())){
                validateCtaOnUpdateEmploymentCta(oldCTA.getEmploymentId(),collectiveTimeAgreementDTO.getPublishDate(),collectiveTimeAgreementDTO.getId());
            }
            else if (!oldCTA.getStartDate().equals(collectiveTimeAgreementDTO.getStartDate())){
                exceptionService.actionNotPermittedException(STARTDATE_CANNOT_CHANGE,"CTA");
            }
            else if(isNotNull(oldCTA.getEndDate()) && !oldCTA.getEndDate().equals(collectiveTimeAgreementDTO.getEndDate())){
                validateCtaOnUpdateEmploymentCta(oldCTA.getEmploymentId(),collectiveTimeAgreementDTO.getEndDate(),collectiveTimeAgreementDTO.getId());
                validateGapBetweenCTA(collectiveTimeAgreementDTO, oldCTA);
            }
        }
    }

    private void validateGapBetweenCTA(CollectiveTimeAgreementDTO collectiveTimeAgreementDTO, CostTimeAgreement oldCTA) {
        boolean gapExists = costTimeAgreementRepository.isGapExistsInEmploymentCTA(oldCTA.getEmploymentId(),collectiveTimeAgreementDTO.getEndDate(),collectiveTimeAgreementDTO.getId());
        if (gapExists){
            exceptionService.actionNotPermittedException(ERROR_NO_GAP, "CTA");
        }
    }

    private void validateCtaOnUpdateEmploymentCta(Long employementId,LocalDate date,BigInteger ctaId) {
        boolean notValid = costTimeAgreementRepository.isEmploymentCTAExistsOnDate(employementId,date,ctaId);
        if (notValid) {
            exceptionService.duplicateDataException("error.cta.invalid", date, "");
        }
    }

    private List<CTARuleTemplate> getCtaRuleTemplates(CollectiveTimeAgreementDTO ctaDTO, CostTimeAgreement costTimeAgreement) {
        List<CTARuleTemplate> ctaRuleTemplates = ObjectMapperUtils.copyCollectionPropertiesByMapper(ctaDTO.getRuleTemplates(), CTARuleTemplate.class);
        if (!ctaRuleTemplates.isEmpty()) {
            ctaRuleTemplates.forEach(ctaRuleTemplate -> {
                if(CalculationFor.CONDITIONAL_BONUS.equals(ctaRuleTemplate.getCalculationFor())){
                    ctaCompensationSettingService.validateInterval(ctaRuleTemplate.getCalculateValueAgainst().getCtaCompensationConfigurations());
                }
                ctaRuleTemplate.setId(null);
                ctaRuleTemplate.setCountryId(null);
            });
            ctaRuleTemplateRepository.saveEntities(ctaRuleTemplates);
            List<BigInteger> ruleTemplateIds = ctaRuleTemplates.stream().map(MongoBaseEntity::getId).collect(Collectors.toList());
            costTimeAgreement.setRuleTemplateIds(ruleTemplateIds);
        }
        return ctaRuleTemplates;
    }


    /**
     * @param countryId
     * @return CTARuleTemplateCategoryWrapper
     */
    public CTARuleTemplateCategoryWrapper loadAllCTARuleTemplateByCountry(Long countryId, Long organizationId) {
        List<RuleTemplateCategoryDTO> ruleTemplateCategories = ruleTemplateCategoryRepository.findAllUsingCountryIdAndType(countryId, RuleTemplateCategoryType.CTA);
        Map<BigInteger, RuleTemplateCategoryDTO> ruleTemplateCategoryDTOMap = ruleTemplateCategories.stream().collect(Collectors.toMap(k -> k.getId(), v -> v));
        List<CTARuleTemplateDTO> ctaRuleTemplateDTOS = ctaRuleTemplateRepository.findByCountryIdAndDeletedFalse(countryId);
        if (isNotNull(organizationId)) {
            updateExistingPhaseIdOfCTA(ctaRuleTemplateDTOS, organizationId, countryId);
        }
        ctaRuleTemplateDTOS.forEach(c -> {
            c.setRuleTemplateCategoryName(ruleTemplateCategoryDTOMap.get(c.getRuleTemplateCategoryId()).getName());
        });
        return new CTARuleTemplateCategoryWrapper(ruleTemplateCategories, ctaRuleTemplateDTOS);
    }

    public Map<Long,CostTimeAgreement> updateWTACTA(List<Long> oldEmploymentIds, Map<Long, Long> newOldemploymentIdMap){
        List<CTAResponseDTO> ctaResponseDTOs = costTimeAgreementRepository.getCTAByEmploymentIds(oldEmploymentIds, DateUtils.getDate());
        List<CostTimeAgreement> newCTAs = new ArrayList<>();
        for (CTAResponseDTO cta : ctaResponseDTOs) {
            CostTimeAgreement newCTA = ObjectMapperUtils.copyPropertiesByMapper(cta, CostTimeAgreement.class);
            List<CTARuleTemplate> ctaRuleTemplates = ObjectMapperUtils.copyCollectionPropertiesByMapper(cta.getRuleTemplates(), CTARuleTemplate.class);
            for (CTARuleTemplate ctaRuleTemplate : ctaRuleTemplates) {
                ctaRuleTemplate.setId(null);
            }
            if (!ctaRuleTemplates.isEmpty()) {
                ctaRuleTemplateRepository.saveEntities(ctaRuleTemplates);
            }

            List<BigInteger> ctaRuleTemplateIds = ctaRuleTemplates.stream().map(ctaRuleTemplate -> ctaRuleTemplate.getId()).collect(Collectors.toList());
            newCTA.setRuleTemplateIds(ctaRuleTemplateIds);
            newCTA.setEmploymentId(newOldemploymentIdMap.get(cta.getEmploymentId()));
            newCTA.setId(null);
            newCTAs.add(newCTA);
        }
        if (!newCTAs.isEmpty()) {
            costTimeAgreementRepository.saveEntities(newCTAs);
        }
        return newCTAs.stream().collect(toMap(k -> k.getEmploymentId(), v -> v));
    }

    /**
     * @param unitId
     * @return CTARuleTemplateCategoryWrapper
     */
    public CTARuleTemplateCategoryWrapper loadAllCTARuleTemplateByUnit(Long unitId) {
        Long countryId = userIntegrationService.getCountryIdOfOrganization(unitId);
        return loadAllCTARuleTemplateByCountry(countryId, unitId);
    }

    /**
     * @param ctaRuleTemplate
     * @param ctaRuleTemplateDTO
     * @param doUpdate
     * @param countryDTO
     */
    private void buildCTARuleTemplate(CTARuleTemplate ctaRuleTemplate, CTARuleTemplateDTO ctaRuleTemplateDTO, Boolean doUpdate, CountryDTO countryDTO) {
        ctaRuleTemplate.setRuleTemplateCategoryId(ctaRuleTemplateDTO.getRuleTemplateCategoryId());
        setActivityBasesCostCalculationSettings(ctaRuleTemplate);
        if (ctaRuleTemplate.getCalculateValueAgainst() != null && ctaRuleTemplate.getCalculateValueAgainst().getCalculateValue() != null) {
            switch (ctaRuleTemplate.getCalculateValueAgainst().getCalculateValue()) {
                case FIXED_VALUE: {
                    if (doUpdate && ctaRuleTemplate.getCalculateValueAgainst().getFixedValue().getCurrencyId() != null) {
                        ctaRuleTemplate.getCalculateValueAgainst().getFixedValue().setCurrency(countryDTO.getCurrencyId());
                    }
                    break;
                }
                case WEEKLY_HOURS:
                case WEEKLY_SALARY:
                    ctaRuleTemplate.getCalculateValueAgainst().setScale(ctaRuleTemplate.getCalculateValueAgainst().getScale());
                    break;
                default:
                    break;
            }
        }
        ctaRuleTemplate.getCalculateValueAgainst().setCalculateValue(ctaRuleTemplateDTO.getCalculateValueAgainst().getCalculateValue());
        logger.info("ctaRuleTemplate.getCalculateValueAgainst().getScale : {}", ctaRuleTemplate.getCalculateValueAgainst().getScale());
    }


    /**
     * @param countryId
     * @param ctaId
     * @return Boolean
     */
    public Boolean deleteCostTimeAgreement(Long countryId, BigInteger ctaId) {
        CostTimeAgreement costTimeAgreement = costTimeAgreementRepository.findCTAByCountryAndIdAndDeleted(countryId, ctaId, false);
        if (costTimeAgreement == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_CTA_ID_NOTFOUND, ctaId);
        }
        costTimeAgreement.setDeleted(true);
        costTimeAgreementRepository.save(costTimeAgreement);
        return true;
    }

    /**
     * @param countryId
     * @return List<CTAResponseDTO>
     */
    public List<CTAResponseDTO> loadAllCTAByCountry(Long countryId) {
       return costTimeAgreementRepository.findCTAByCountryId(countryId);
    }

    /**
     * @param unitId
     * @return List<CTAResponseDTO>
     */
    public List<CTAResponseDTO> loadAllCTAByUnit(Long unitId) {
        List<CTAResponseDTO> ctaResponseDTOS = costTimeAgreementRepository.findCTAByUnitId(unitId);
        ctaResponseDTOS.forEach(ctaResponseDTO -> {
            ctaResponseDTO.setUnitId(unitId);
        });
        return ctaResponseDTOS;
    }


    /**
     * @param collectiveTimeAgreementDTO
     */
    private CostTimeAgreement buildCTA(CollectiveTimeAgreementDTO collectiveTimeAgreementDTO) {
        List<TagDTO> tagDTOS = collectiveTimeAgreementDTO.getTags();
        collectiveTimeAgreementDTO.setTags(null);
        CostTimeAgreement costTimeAgreement = ObjectMapperUtils.copyPropertiesByMapper(collectiveTimeAgreementDTO, CostTimeAgreement.class);
        costTimeAgreement.setTags(tagDTOS.stream().map(TagDTO::getId).collect(Collectors.toList()));
        collectiveTimeAgreementDTO.setTags(tagDTOS);
        List<CTARuleTemplate> ctaRuleTemplates = new ArrayList<>(collectiveTimeAgreementDTO.getRuleTemplates().size());
        for (CTARuleTemplateDTO ctaRuleTemplateDTO : collectiveTimeAgreementDTO.getRuleTemplates()) {
            if(CalculationFor.CONDITIONAL_BONUS.equals(ctaRuleTemplateDTO.getCalculationFor())){
                ctaCompensationSettingService.validateInterval(ctaRuleTemplateDTO.getCalculateValueAgainst().getCtaCompensationConfigurations());
            }
            CTARuleTemplate ctaRuleTemplate = ObjectMapperUtils.copyPropertiesByMapper(ctaRuleTemplateDTO, CTARuleTemplate.class);
            ctaRuleTemplate.setId(null);
            setActivityBasesCostCalculationSettings(ctaRuleTemplate);
            ctaRuleTemplate.setEmploymentTypes(ctaRuleTemplateDTO.getEmploymentTypes());
            ctaRuleTemplate.setRuleTemplateCategoryId(ctaRuleTemplateDTO.getRuleTemplateCategoryId());
            ctaRuleTemplates.add(ctaRuleTemplate);
        }
        ctaRuleTemplateRepository.saveEntities(ctaRuleTemplates);
        List<BigInteger> ruleTemplateIds = ctaRuleTemplates.stream().map(MongoBaseEntity::getId).collect(Collectors.toList());
        costTimeAgreement.setRuleTemplateIds(ruleTemplateIds);
        costTimeAgreement.setStartDate(collectiveTimeAgreementDTO.getStartDate());
        costTimeAgreement.setEndDate(collectiveTimeAgreementDTO.getEndDate());
        return costTimeAgreement;
    }

    /**
     * @param ctaRuleTemplate
     */
    private void setActivityBasesCostCalculationSettings(CTARuleTemplate ctaRuleTemplate) {
        if (ctaRuleTemplate.getActivityTypeForCostCalculation() == ActivityTypeForCostCalculation.TIME_TYPE_ACTIVITY) {
            ctaRuleTemplate.setActivityIds(new HashSet<>());
        } else {
            ctaRuleTemplate.setPlannedTimeIds(null);
            ctaRuleTemplate.setTimeTypeIds(null);
        }
    }


    /**
     * @param countryId
     * @param id
     * @param ctaRuleTemplateDTO
     * @return CTARuleTemplateDTO
     */
    public CTARuleTemplateDTO updateCTARuleTemplate(Long countryId, BigInteger id, CTARuleTemplateDTO ctaRuleTemplateDTO) {
        CountryDTO countryDTO = userIntegrationService.getCountryById(countryId);
        CTARuleTemplate ctaRuleTemplate = ctaRuleTemplateRepository.findOne(id);
        ctaRuleTemplateDTO.setRuleTemplateType(ctaRuleTemplate.getRuleTemplateType());
        CTARuleTemplate udpdateCtaRuleTemplate = ObjectMapperUtils.copyPropertiesByMapper(ctaRuleTemplateDTO, CTARuleTemplate.class);
        this.buildCTARuleTemplate(udpdateCtaRuleTemplate, ctaRuleTemplateDTO, true, countryDTO);
        udpdateCtaRuleTemplate.setId(ctaRuleTemplate.getId());
        udpdateCtaRuleTemplate.setCountryId(countryId);
        if (!udpdateCtaRuleTemplate.getRuleTemplateCategoryId().equals(ctaRuleTemplate.getRuleTemplateCategoryId())) {
            updateAllCtaRuleTemplateCategoryIdByCtaRuleTemplateName(udpdateCtaRuleTemplate.getName(), udpdateCtaRuleTemplate.getRuleTemplateCategoryId());
        }
        ctaRuleTemplateRepository.save(udpdateCtaRuleTemplate);
        return ctaRuleTemplateDTO;
    }

    private void updateAllCtaRuleTemplateCategoryIdByCtaRuleTemplateName(String CtaRuleTemplateName, BigInteger ctaRuleTemplateCategoryId) {
        List<CTARuleTemplate> ctaRuleTemplates = ctaRuleTemplateRepository.findAllByNameAndDeletedFalse(CtaRuleTemplateName);
        if (isCollectionNotEmpty(ctaRuleTemplates)) {
            ctaRuleTemplates.forEach(ctaRuleTemplate -> ctaRuleTemplate.setRuleTemplateCategoryId(ctaRuleTemplateCategoryId));
            ctaRuleTemplateRepository.saveAll(ctaRuleTemplates);
        }
    }

    /**
     * @param unitId
     * @param collectiveTimeAgreementDTO
     * @return CollectiveTimeAgreementDTO
     */
    public CollectiveTimeAgreementDTO createCopyOfUnitCTA(Long unitId, CollectiveTimeAgreementDTO collectiveTimeAgreementDTO) {
        logger.info("saving CostTimeAgreement unit {}", unitId);
        if (costTimeAgreementRepository.isCTAExistWithSameNameInUnit(unitId, collectiveTimeAgreementDTO.getName().trim(), new BigInteger("1"))) {
            exceptionService.duplicateDataException(MESSAGE_CTA_NAME_ALREADYEXIST, collectiveTimeAgreementDTO.getName());
        }
        OrganizationDTO organization = userIntegrationService.getOrganization();
        collectiveTimeAgreementDTO.setId(null);
        CostTimeAgreement costTimeAgreement = buildCTA(collectiveTimeAgreementDTO);
        costTimeAgreement.setOrganization(new WTAOrganization(organization.getId(), organization.getName(), organization.getDescription()));
        costTimeAgreementRepository.save(costTimeAgreement);
        collectiveTimeAgreementDTO.setId(costTimeAgreement.getId());
        return collectiveTimeAgreementDTO;
    }

    /**
     * @param countryId
     * @param organizationSubTypeId
     * @return List<CTAResponseDTO>
     */
    public List<CTAResponseDTO> getAllCTAByOrganizationSubType(Long countryId, Long organizationSubTypeId) {
        return costTimeAgreementRepository.getAllCTAByOrganizationSubType(countryId, organizationSubTypeId);
    }

    /**
     * @param countryId
     * @param ctaId
     * @param collectiveTimeAgreementDTO
     * @param organizationSubTypeId
     * @param checked
     * @return CollectiveTimeAgreementDTO
     */
    public CollectiveTimeAgreementDTO setCTAWithOrganizationType(Long countryId, BigInteger ctaId, CollectiveTimeAgreementDTO collectiveTimeAgreementDTO, long organizationSubTypeId, boolean checked) {
        CollectiveTimeAgreementDTO collectiveTimeAgreementDTO1 = null;
        if (checked) {
            String name = COPY_OF + collectiveTimeAgreementDTO.getName();
            collectiveTimeAgreementDTO.setName(name);
            collectiveTimeAgreementDTO.setOrganizationSubType(new OrganizationTypeDTO(organizationSubTypeId));
            collectiveTimeAgreementDTO1 = countryCTAService.createCostTimeAgreementInCountry(countryId, collectiveTimeAgreementDTO, true);
        } else {
            CostTimeAgreement cta = costTimeAgreementRepository.getCTAByIdAndOrganizationSubTypeAndCountryId(organizationSubTypeId, countryId, ctaId);
            if (!Optional.ofNullable(cta).isPresent())
                exceptionService.dataNotFoundByIdException(MESSAGE_CTA_ID_NOTFOUND, ctaId);
            cta.setDeleted(true);
            costTimeAgreementRepository.save(cta);
        }
        return collectiveTimeAgreementDTO1;
    }

    public CTATableSettingWrapper getVersionsCTA(Long unitId, List<Long> upIds) {
        TableConfiguration tableConfiguration = tableSettingService.getTableConfigurationByTabId(unitId, ORGANIZATION_CTA_AGREEMENT_VERSION_TABLE_ID);
        List<CTAResponseDTO> ctaResponseDTOS = costTimeAgreementRepository.getParentCTAByUpIds(upIds);
        if(isCollectionNotEmpty(ctaResponseDTOS)){
            CostTimeAgreement costTimeAgreement = costTimeAgreementRepository.findOne(ctaResponseDTOS.get(0).getOrganizationParentId());
            ctaResponseDTOS.forEach(ctaResponseDTO -> ctaResponseDTO.setTranslations(costTimeAgreement.getTranslations()));
        }
        return new CTATableSettingWrapper(ctaResponseDTOS, tableConfiguration);
    }

    public CTAResponseDTO getDefaultCTA(Long unitId, Long expertiseId) {
        List<CTAResponseDTO> ctaResponseDTOS = costTimeAgreementRepository.getDefaultCTA(unitId, expertiseId);
        return ctaResponseDTOS.isEmpty() ? null : ctaResponseDTOS.get(0);
    }

    public List<CTAResponseDTO> getCTAByEmploymentIds(Set<Long> employmentIds) {
        return costTimeAgreementRepository.getCTAByUpIds(employmentIds);
    }


    public CTAResponseDTO assignCTAToEmployment(Long employmentId, BigInteger ctaId, LocalDate startLocalDate) {
        CTAResponseDTO ctaResponseDTO = costTimeAgreementRepository.getOneCtaById(ctaId);
        CostTimeAgreement costTimeAgreement = ObjectMapperUtils.copyPropertiesByMapper(ctaResponseDTO, CostTimeAgreement.class);
        costTimeAgreement.setId(null);
        costTimeAgreement.setParentId(ctaId);
        costTimeAgreement.setOrganizationParentId(ctaId);
        costTimeAgreement.setStartDate(startLocalDate);
        List<CTARuleTemplate> ctaRuleTemplates = ObjectMapperUtils.copyCollectionPropertiesByMapper(ctaResponseDTO.getRuleTemplates(), CTARuleTemplate.class);
        ctaRuleTemplates.forEach(ctaRuleTemplate -> ctaRuleTemplate.setId(null));
        if (!ctaRuleTemplates.isEmpty()) {
            ctaRuleTemplateRepository.saveEntities(ctaRuleTemplates);
        }
        costTimeAgreement.setEmploymentId(employmentId);
        List<BigInteger> ruleTemplateIds = ctaRuleTemplates.stream().map(MongoBaseEntity::getId).collect(Collectors.toList());
        costTimeAgreement.setRuleTemplateIds(ruleTemplateIds);
        costTimeAgreementRepository.save(costTimeAgreement);

        return costTimeAgreementRepository.getOneCtaById(costTimeAgreement.getId());
    }


    private boolean isCalculatedValueChanged(List<BigInteger> ruleTemplateIds, List<CTARuleTemplateDTO> ctaRuleTemplateDTOS) {

        boolean isCalculatedValueChanged = false;
        if (ctaRuleTemplateDTOS.size() == ruleTemplateIds.size()) {
            List<CTARuleTemplate> existingCtaRuleTemplates = ctaRuleTemplateRepository.findAllByIdAndDeletedFalse(ruleTemplateIds);
            Map<BigInteger, CTARuleTemplate> existingCtaRuleTemplateMap = existingCtaRuleTemplates.stream().collect(Collectors.toMap(k -> k.getId(), v -> v));
            List<CTARuleTemplate> ctaRuleTemplates = ObjectMapperUtils.copyCollectionPropertiesByMapper(ctaRuleTemplateDTOS, CTARuleTemplate.class);
            for (CTARuleTemplate ctaRuleTemplate : ctaRuleTemplates) {
                if (existingCtaRuleTemplateMap.containsKey(ctaRuleTemplate.getId())) {
                    CTARuleTemplate existingCTARuletemplate = existingCtaRuleTemplateMap.get(ctaRuleTemplate.getId());
                    isCalculatedValueChanged = ctaRuleTemplate.isCalculatedValueChanged(existingCTARuletemplate);
                } else {
                    isCalculatedValueChanged = true;
                }
                if (isCalculatedValueChanged) {
                    break;
                }
            }
        } else {
            isCalculatedValueChanged = true;
        }
        return isCalculatedValueChanged;
    }

    private void updateExistingPhaseIdOfCTA(List<CTARuleTemplateDTO> ctaRuleTemplates, Long unitId, Long countryId) {
        List<Phase> countryPhase = phaseMongoRepository.findAllBycountryIdAndDeletedFalse(countryId);
        Map<BigInteger, PhaseDefaultName> phaseDefaultNameMap = countryPhase.stream().collect(Collectors.toMap(Phase::getId, Phase::getPhaseEnum));
        List<Phase> unitPhases = phaseMongoRepository.findByOrganizationIdAndDeletedFalse(unitId);
        Map<PhaseDefaultName, BigInteger> parentPhasesAndUnitPhaseIdMap = unitPhases.stream().collect(Collectors.toMap(Phase::getPhaseEnum, Phase::getId));
        for (CTARuleTemplateDTO ctaRuleTemplate : ctaRuleTemplates) {
            for (CTARuleTemplatePhaseInfo ctaRuleTemplatePhaseInfo : ctaRuleTemplate.getPhaseInfo()) {
                BigInteger phaseId = parentPhasesAndUnitPhaseIdMap.getOrDefault(phaseDefaultNameMap.get(ctaRuleTemplatePhaseInfo.getPhaseId()), ctaRuleTemplatePhaseInfo.getPhaseId());
                ctaRuleTemplatePhaseInfo.setPhaseId(phaseId);
            }
            if(CalculationFor.CONDITIONAL_BONUS.equals(ctaRuleTemplate.getCalculationFor())){
                ctaCompensationSettingService.validateInterval(ctaRuleTemplate.getCalculateValueAgainst().getCtaCompensationConfigurations());
            }
        }
    }

    public List<CTARuleTemplateDTO> getCtaRuleTemplatesByEmploymentId(Long employmentId, Date startDate, Date endDate) {
        List<CTAResponseDTO> ctaResponseDTOS = costTimeAgreementRepository.getCTAByEmploymentIdBetweenDate(employmentId, startDate, endDate);
        List<CTARuleTemplateDTO> ruleTemplates = ctaResponseDTOS.stream().flatMap(ctaResponseDTO -> ctaResponseDTO.getRuleTemplates().stream()).filter(ObjectUtils.distinctByKey(CTARuleTemplateDTO::getName)).collect(toList());
        return ruleTemplates;
    }

    public List<CTAResponseDTO> getAllCTAByUnitId(long unitId){
        return costTimeAgreementRepository.findCTAByUnitId(unitId);
    }


    public Map<String, TranslationInfo> updateCtaRuleTranslations(BigInteger ctaId, Map<String,TranslationInfo> translations) {
        CTARuleTemplate ctaRuleTemplate =ctaRuleTemplateRepository.findOne(ctaId);
        ctaRuleTemplate.setTranslations(translations);
        ctaRuleTemplateRepository.save(ctaRuleTemplate);
        return ctaRuleTemplate.getTranslations();
    }

    public Map<String, TranslationInfo> updateTranslation(BigInteger ctaId, Map<String,TranslationInfo> translations) {
        CostTimeAgreement costTimeAgreement =costTimeAgreementRepository.findOne(ctaId);
        costTimeAgreement.setTranslations(translations);
        costTimeAgreementRepository.save(costTimeAgreement);
        return costTimeAgreement.getTranslations();
    }

    public void updateCTADates(List<CostTimeAgreement> costTimeAgreements1, EmploymentLinesDTO employmentLinesDTO) {
        for (CostTimeAgreement costTimeAgreement : costTimeAgreements1) {
            if(costTimeAgreement.getStartDate().isAfter(employmentLinesDTO.getStartDate())){
                costTimeAgreement.setStartDate(employmentLinesDTO.getStartDate());
            }
        }
        if(isNull(employmentLinesDTO.getEndDate()) && !costTimeAgreements1.isEmpty()){
            costTimeAgreements1.get(costTimeAgreements1.size()-1).setEndDate(null);
        }else {
            for (CostTimeAgreement costTimeAgreement : costTimeAgreements1) {
                if(isNull(costTimeAgreement.getEndDate())){
                    break;
                }else if(costTimeAgreement.getEndDate().isBefore(employmentLinesDTO.getEndDate())){
                    costTimeAgreement.setEndDate(employmentLinesDTO.getEndDate());
                    break;
                }
            }
        }
    }

    public void updateDatesOnCTA(List<CostTimeAgreement> costTimeAgreements1) {
        for (int i = 1; i < costTimeAgreements1.size(); i++) {
            CostTimeAgreement first = costTimeAgreements1.get(i-1);
            CostTimeAgreement second = costTimeAgreements1.get(i);
            first.setEndDate(second.getStartDate().minusDays(1));
            if(first.getStartDate().equals(second.getStartDate())){
                first.setEndDate(first.getStartDate());
                second.setStartDate(first.getEndDate().plusDays(1));
            }
        }
    }

    public Set<Long> filterStaffByCTATemplateAccountType(StaffFilterDTO staffFilterDTO, Set<Long> staffIds, Map<FilterType, Set<String>> filterTypeMap) {
        Set<Long> filteredStaffIds = staffIds;
        if(filterTypeMap.containsKey(CTA_ACCOUNT_TYPE)){
            List<CTAResponseDTO> allCTAs = costTimeAgreementRepository.getParentCTAByUpIds(staffFilterDTO.getMapOfStaffAndEmploymentIds().values().stream().flatMap(longs -> longs.stream()).filter(longs -> isNotNull(longs)).collect(Collectors.toList()));
            Map<Long,List<CTAResponseDTO>>  ctagroup = allCTAs.stream().collect(Collectors.groupingBy(ctaResponseDTO -> ctaResponseDTO.getEmploymentId(),Collectors.toList()));
            Set<Long> staffFilterDTOList = new HashSet<>();
            for(Long staffId:staffIds) {
                FilterStaff(staffFilterDTO, filterTypeMap, ctagroup, staffFilterDTOList, staffId);
            }
            filteredStaffIds = staffFilterDTOList;

        }
        return filteredStaffIds;
    }

    private void FilterStaff(StaffFilterDTO staffFilterDTO, Map<FilterType, Set<String>> filterTypeMap, Map<Long, List<CTAResponseDTO>> ctagroup, Set<Long> staffFilterDTOList, Long staffId) {
        List<Long> employmentIDs=staffFilterDTO.getMapOfStaffAndEmploymentIds().get(staffId);
        for(Long employmentID:employmentIDs) {
            List<CTAResponseDTO> CTAs=ctagroup.getOrDefault(employmentID,new ArrayList<>());
            for(CTAResponseDTO ctaResponseDTO:CTAs) {
                for(CTARuleTemplateDTO CTARule:ctaResponseDTO.getRuleTemplates()) {
                    if(filterTypeMap.get(CTA_ACCOUNT_TYPE).contains(CTARule.getPlannedTimeWithFactor().getAccountType().toString())){
                        staffFilterDTOList.add(staffId);
                    }

                }
            }
        }
    }

    public Map<String,Object> getDefaultDataForCTATemplate(Long countryId,Long unitId) {
        List<ActivityWithCTAWTASettingsDTO> activityTypeDTOS;
        List<PhaseDTO> phases;
        if (Optional.ofNullable(unitId).isPresent()) {
            countryId = UserContext.getUserDetails().getCountryId();
            activityTypeDTOS = activityService.findAllActivityWithCtaWtaSettingByUnit(unitId);
            phases = phaseService.getDefaultPhasesByUnit(unitId);
        } else {
            activityTypeDTOS = activityService.findAllActivityWithCtaWtaSettingByCountry(countryId);
            phases = phaseService.getPhasesByCountryId(countryId);
        }
        Set<BigInteger> activityCategoriesIds = activityTypeDTOS.stream().map(ActivityWithCTAWTASettingsDTO::getCategoryId).collect(Collectors.toSet());
        List<ActivityCategory> activityCategories = activityService.findAllActivityCategoriesByCountry(activityCategoriesIds);
        List<TimeTypeDTO> timeType = timeTypeService.getAllTimeType(null,countryId);
        List<PresenceTypeDTO> plannedTime = plannedTimeTypeService.getAllPresenceTypeByCountry(countryId);
        List<DayTypeDTO> dayTypes = dayTypeService.getDayTypeWithCountryHolidayCalender(countryId);
        Map<String,Object> response = new HashMap();
        response.put("phases",phases);
        response.put("dayTypes",dayTypes);
        response.put("plannedTime",plannedTime);
        response.put("timeTypes",timeType);
        response.put("activityTypes",activityTypeDTOS);
        response.put("activityCategories",activityCategories);
        return response;
    }
}

