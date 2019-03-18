package com.kairos.service.agreement_template;


import com.kairos.commons.client.RestTemplateResponseEnvelope;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.gdpr.OrganizationSubTypeDTO;
import com.kairos.dto.gdpr.OrganizationTypeDTO;
import com.kairos.dto.gdpr.ServiceCategoryDTO;
import com.kairos.dto.gdpr.SubServiceCategoryDTO;
import com.kairos.dto.gdpr.agreement_template.AgreementTemplateDTO;
import com.kairos.dto.gdpr.agreement_template.CoverPageVO;
import com.kairos.dto.gdpr.agreement_template.MasterAgreementTemplateDTO;
import com.kairos.dto.gdpr.master_data.AccountTypeVO;
import com.kairos.enums.IntegrationOperation;
import com.kairos.persistence.model.agreement_template.AgreementSection;
import com.kairos.persistence.model.agreement_template.PolicyAgreementTemplate;
import com.kairos.persistence.model.clause.AgreementSectionClause;
import com.kairos.persistence.model.embeddables.*;
import com.kairos.persistence.model.template_type.TemplateType;
import com.kairos.persistence.repository.agreement_template.PolicyAgreementRepository;
import com.kairos.persistence.repository.clause.ClauseRepository;
import com.kairos.persistence.repository.template_type.TemplateTypeRepository;
import com.kairos.response.dto.clause.ClauseBasicResponseDTO;
import com.kairos.response.dto.master_data.TemplateTypeResponseDTO;
import com.kairos.response.dto.policy_agreement.AgreementSectionResponseDTO;
import com.kairos.response.dto.policy_agreement.AgreementTemplateBasicResponseDTO;
import com.kairos.response.dto.policy_agreement.AgreementTemplateSectionResponseDTO;
import com.kairos.response.dto.policy_agreement.PolicyAgreementTemplateResponseDTO;
import com.kairos.rest_client.GenericRestClient;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.s3bucket.AWSBucketService;
import com.kairos.service.template_type.TemplateTypeService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class PolicyAgreementTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyAgreementTemplateService.class);

    @Inject
    private PolicyAgreementRepository policyAgreementRepository;

    @Inject
    private ClauseRepository clauseRepository;


    @Inject
    private AgreementSectionService agreementSectionService;

    @Inject
    private ExceptionService exceptionService;

    @Inject
    private TemplateTypeService templateTypeService;

    @Inject
    private AWSBucketService awsBucketService;

    @Inject
    private TemplateTypeRepository templateTypeRepository;

    @Inject
    private GenericRestClient genericRestClient;


    /**
     * @param referenceId                - countryId or unitId
     * @param -                          isOrganization boolean to check whether referenceId id country id or unit id
     * @param policyAgreementTemplateDto
     * @return return object of basic policy agreement template.
     * @description this method creates a basic policy Agreement template with basic detail about organization type,
     * organizationSubTypes ,service Category and sub service Category.
     */
    public <E extends AgreementTemplateDTO> E saveAgreementTemplate(Long referenceId, boolean isOrganization, E policyAgreementTemplateDto) {

        PolicyAgreementTemplate previousTemplate = isOrganization ? policyAgreementRepository.findByOrganizationIdAndDeletedAndName(referenceId, policyAgreementTemplateDto.getName())
                : policyAgreementRepository.findByCountryIdAndName(referenceId, policyAgreementTemplateDto.getName());
        if (Optional.ofNullable(previousTemplate).isPresent()) {
            exceptionService.duplicateDataException("message.duplicate", "message.policy.agreementTemplate", policyAgreementTemplateDto.getName());
        }
        PolicyAgreementTemplate policyAgreementTemplate = buildAgreementTemplate(referenceId, isOrganization, policyAgreementTemplateDto);
        policyAgreementRepository.save(policyAgreementTemplate);
        policyAgreementTemplateDto.setId(policyAgreementTemplate.getId());
        return policyAgreementTemplateDto;

    }

    private <E extends AgreementTemplateDTO> PolicyAgreementTemplate buildAgreementTemplate(Long referenceId, boolean isOrganization, E policyAgreementTemplateDto) {

        PolicyAgreementTemplate policyAgreementTemplate = new PolicyAgreementTemplate(policyAgreementTemplateDto.getName(), policyAgreementTemplateDto.getDescription(), templateTypeRepository.getOne(policyAgreementTemplateDto.getTemplateTypeId()));
        if (isOrganization) {
            policyAgreementTemplate.setOrganizationId(referenceId);
        } else {
            MasterAgreementTemplateDTO agreementTemplateDTO = (MasterAgreementTemplateDTO) policyAgreementTemplateDto;
            policyAgreementTemplate.setOrganizationTypes(ObjectMapperUtils.copyPropertiesOfListByMapper(agreementTemplateDTO.getOrganizationTypes(), OrganizationType.class));
            policyAgreementTemplate.setOrganizationSubTypes(ObjectMapperUtils.copyPropertiesOfListByMapper(agreementTemplateDTO.getOrganizationSubTypes(), OrganizationSubType.class));
            policyAgreementTemplate.setOrganizationServices(ObjectMapperUtils.copyPropertiesOfListByMapper(agreementTemplateDTO.getOrganizationServices(), ServiceCategory.class));
            policyAgreementTemplate.setOrganizationSubServices(ObjectMapperUtils.copyPropertiesOfListByMapper(agreementTemplateDTO.getOrganizationSubServices(), SubServiceCategory.class));
            policyAgreementTemplate.setAccountTypes(ObjectMapperUtils.copyPropertiesOfListByMapper(agreementTemplateDTO.getAccountTypes(), AccountType.class));
            policyAgreementTemplate.setCountryId(referenceId);
        }
        return policyAgreementTemplate;
    }


    /**
     * @param referenceId         - country Id or unitId
     * @param -                   isOrganization boolean to check whether referenceId id country id or unit id
     * @param agreementTemplateId - Agreement Template id
     * @param coverPageLogo       - Agreement Cover page
     * @return -Url of image uploaded at S3 bucket
     */
    public String uploadCoverPageLogo(Long referenceId, boolean isOrganization, Long agreementTemplateId, MultipartFile coverPageLogo) {

        PolicyAgreementTemplate policyAgreementTemplate = isOrganization ? policyAgreementRepository.findByIdAndOrganizationIdAndDeletedFalse(agreementTemplateId, referenceId) : policyAgreementRepository.findByIdAndCountryIdAndDeletedFalse(agreementTemplateId, referenceId);
        if (!Optional.ofNullable(policyAgreementTemplate).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.policy.agreementTemplate", agreementTemplateId);
        }
        String coverPageLogoUrl = awsBucketService.uploadImage(coverPageLogo);
        if (policyAgreementTemplate.isCoverPageAdded()) {
            policyAgreementTemplate.getCoverPageData().setCoverPageLogoUrl(coverPageLogoUrl);
        } else {
            // policyAgreementTemplate.setCoverPageData(new CoverPageVO(coverPageLogoUrl));
            policyAgreementTemplate.setCoverPageAdded(true);
        }

        policyAgreementRepository.save(policyAgreementTemplate);
        return coverPageLogoUrl;
    }

    /**
     * @param countryId
     * @return return agreement section list with empty section array as per front end requirement
     */
    public List<PolicyAgreementTemplateResponseDTO> getAllAgreementTemplateByCountryId(Long countryId) {
        List<PolicyAgreementTemplateResponseDTO> policyAgreementTemplateResponseDTOS = new ArrayList<>();
        List<PolicyAgreementTemplate> policyAgreementTemplates = policyAgreementRepository.findAllByCountryId(countryId);
        policyAgreementTemplates.forEach(policyAgreementTemplate -> {
            PolicyAgreementTemplateResponseDTO policyAgreementTemplateResponseDTO = new PolicyAgreementTemplateResponseDTO(policyAgreementTemplate.getId(), policyAgreementTemplate.getName(), policyAgreementTemplate.getDescription(), ObjectMapperUtils.copyPropertiesByMapper(policyAgreementTemplate.getTemplateType(), TemplateTypeResponseDTO.class));
            policyAgreementTemplateResponseDTO.setOrganizationTypes(ObjectMapperUtils.copyPropertiesOfListByMapper(policyAgreementTemplate.getOrganizationTypes(), OrganizationTypeDTO.class));
            policyAgreementTemplateResponseDTO.setOrganizationSubTypes(ObjectMapperUtils.copyPropertiesOfListByMapper(policyAgreementTemplate.getOrganizationSubTypes(), OrganizationSubTypeDTO.class));
            policyAgreementTemplateResponseDTO.setOrganizationServices(ObjectMapperUtils.copyPropertiesOfListByMapper(policyAgreementTemplate.getOrganizationServices(), ServiceCategoryDTO.class));
            policyAgreementTemplateResponseDTO.setOrganizationSubServices(ObjectMapperUtils.copyPropertiesOfListByMapper(policyAgreementTemplate.getOrganizationSubServices(), SubServiceCategoryDTO.class));
            policyAgreementTemplateResponseDTO.setAccountTypes(ObjectMapperUtils.copyPropertiesOfListByMapper(policyAgreementTemplate.getAccountTypes(), AccountTypeVO.class));
            policyAgreementTemplateResponseDTOS.add(policyAgreementTemplateResponseDTO);
        });
        return policyAgreementTemplateResponseDTOS;
    }

    /**
     * @param unitId -
     * @return return agreement section list with empty section array as per front end requirement
     */
    public List<PolicyAgreementTemplateResponseDTO> getAllAgreementTemplateByUnitId(Long unitId) {

        List<PolicyAgreementTemplate> templates = policyAgreementRepository.findAllByOrganizationId(unitId);
        return templates.stream().map(policyAgreementTemplate -> {
            return new PolicyAgreementTemplateResponseDTO(policyAgreementTemplate.getId(), policyAgreementTemplate.getName(), policyAgreementTemplate.getDescription(), ObjectMapperUtils.copyPropertiesByMapper(policyAgreementTemplate.getTemplateType(), TemplateTypeResponseDTO.class));
        }).collect(Collectors.toList());
    }


    /**
     * @param referenceId                - countryId or unitId
     * @param isOrganization             isOrganization boolean to check whether referenceId id coutry id or unit id
     * @param agreementTemplateId        - Agreement Template id
     * @param policyAgreementTemplateDto
     * @return
     */
    public <E extends AgreementTemplateDTO> E updatePolicyAgreementTemplateBasicDetails(Long referenceId, boolean isOrganization, Long agreementTemplateId, E policyAgreementTemplateDto) {

        PolicyAgreementTemplate template = isOrganization ? policyAgreementRepository.findByOrganizationIdAndDeletedAndName(referenceId, policyAgreementTemplateDto.getName()) : policyAgreementRepository.findByCountryIdAndName(referenceId, policyAgreementTemplateDto.getName());
        if (Optional.ofNullable(template).isPresent() && !agreementTemplateId.equals(template.getId())) {
            exceptionService.duplicateDataException("message.duplicate", "message.policy.agreementTemplate", policyAgreementTemplateDto.getName());
        }
        try {
            template = policyAgreementRepository.getOne(agreementTemplateId);
            template.setName(policyAgreementTemplateDto.getName());
            template.setDescription(policyAgreementTemplateDto.getDescription());
            template.setTemplateType(templateTypeRepository.getOne(policyAgreementTemplateDto.getTemplateTypeId()));
            if (!isOrganization) {
                MasterAgreementTemplateDTO agreementTemplateDTO = (MasterAgreementTemplateDTO) policyAgreementTemplateDto;
                template.setOrganizationTypes(ObjectMapperUtils.copyPropertiesOfListByMapper(agreementTemplateDTO.getOrganizationTypes(), OrganizationType.class));
                template.setOrganizationSubTypes(ObjectMapperUtils.copyPropertiesOfListByMapper(agreementTemplateDTO.getOrganizationSubTypes(), OrganizationSubType.class));
                template.setOrganizationServices(ObjectMapperUtils.copyPropertiesOfListByMapper(agreementTemplateDTO.getOrganizationServices(), ServiceCategory.class));
                template.setOrganizationSubServices(ObjectMapperUtils.copyPropertiesOfListByMapper(agreementTemplateDTO.getOrganizationSubServices(), SubServiceCategory.class));
                template.setAccountTypes(ObjectMapperUtils.copyPropertiesOfListByMapper(agreementTemplateDTO.getAccountTypes(), AccountType.class));
            }
            policyAgreementRepository.save(template);
        } catch (EntityNotFoundException nfe) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.policy.agreementTemplate", agreementTemplateId);
        }
        return policyAgreementTemplateDto;

    }


    /**
     * @param referenceId         - countryId or unitId
     * @param isOrganization      - to check whether referenceId country id or unit id
     * @param agreementTemplateId
     * @return
     * @description method return list of Agreement sections with sub sections of policy agreement template
     */
    public AgreementTemplateSectionResponseDTO getAllSectionsAndSubSectionOfAgreementTemplateByAgreementTemplateIdAndReferenceId(Long referenceId, boolean isOrganization, Long agreementTemplateId) {
        PolicyAgreementTemplate template = isOrganization ? policyAgreementRepository.findByIdAndOrganizationIdAndDeletedFalse(agreementTemplateId, referenceId) : policyAgreementRepository.findByIdAndCountryIdAndDeletedFalse(agreementTemplateId, referenceId);
        if (!Optional.ofNullable(template).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.policy.agreementTemplate", agreementTemplateId);
        }
        AgreementTemplateSectionResponseDTO agreementTemplateResponse = new AgreementTemplateSectionResponseDTO();
        List<ClauseBasicResponseDTO> clauseListForPolicyAgreementTemplate;
        List<AgreementSectionResponseDTO> agreementSectionResponseDTOS = prepareAgreementSectionResponseDTO(template.getAgreementSections());
        if (isOrganization) {
            clauseListForPolicyAgreementTemplate = ObjectMapperUtils.copyPropertiesOfListByMapper(clauseRepository.findAllClauseByUnitIdAndTemplateTypeId(referenceId, Arrays.asList(template.getTemplateType().getId())), ClauseBasicResponseDTO.class);
        } else {
            List<Long> organizationTypeIds = template.getOrganizationTypes().stream().map(OrganizationType::getId).collect(Collectors.toList());
            List<Long> organizationSubTypeIds = template.getOrganizationSubTypes().stream().map(OrganizationSubType::getId).collect(Collectors.toList());
            List<Long> serviceCategoryIds = template.getOrganizationServices().stream().map(ServiceCategory::getId).collect(Collectors.toList());
            List<Long> subServiceCategoryIds = template.getOrganizationSubServices().stream().map(SubServiceCategory::getId).collect(Collectors.toList());
            clauseListForPolicyAgreementTemplate = ObjectMapperUtils.copyPropertiesOfListByMapper(clauseRepository.findAllClauseByAgreementTemplateMetadataAndCountryId(referenceId, organizationTypeIds, organizationSubTypeIds, serviceCategoryIds, subServiceCategoryIds, template.getTemplateType().getId()), ClauseBasicResponseDTO.class);
        }
        agreementTemplateResponse.setClauseListForTemplate(clauseListForPolicyAgreementTemplate);
        agreementTemplateResponse.setIncludeContentPage(template.isIncludeContentPage());
        agreementTemplateResponse.setAgreementSections(agreementSectionResponseDTOS);
        agreementTemplateResponse.setCoverPageAdded(template.isCoverPageAdded());
        agreementTemplateResponse.setCoverPageData(ObjectMapperUtils.copyPropertiesByMapper(template.getCoverPageData(), CoverPageVO.class));
        agreementTemplateResponse.setSignatureComponentAdded(template.isSignatureComponentAdded());
        agreementTemplateResponse.setSignatureComponentLeftAlign(template.isSignatureComponentLeftAlign());
        agreementTemplateResponse.setSignatureComponentRightAlign(template.isSignatureComponentRightAlign());
        agreementTemplateResponse.setSignatureHtml(template.getSignatureHtml());
        return agreementTemplateResponse;
    }

    public List<AgreementSectionResponseDTO> prepareAgreementSectionResponseDTO(List<? extends AgreementSection> agreementSections) {
        List<AgreementSectionResponseDTO> agreementSectionResponseDTOS = new ArrayList<>();
        agreementSections.forEach(agreementSection -> {
            AgreementSectionResponseDTO agreementSectionResponseDTO = new AgreementSectionResponseDTO(agreementSection.getId(), agreementSection.getTitle(), agreementSection.getTitleHtml(), agreementSection.getOrderedIndex());
            agreementSectionResponseDTO.setClauses(agreementSection.getClauses());
            if (!agreementSection.getAgreementSubSections().isEmpty()) {
                agreementSectionResponseDTO.setAgreementSubSections(prepareAgreementSectionResponseDTO(agreementSection.getAgreementSubSections()));
            }
            agreementSectionResponseDTOS.add(agreementSectionResponseDTO);
        });
        return agreementSectionResponseDTOS;
    }

    private void sortClauseOfAgreementSectionAndSubSectionInResponseDTO(Map<BigInteger, ClauseBasicResponseDTO> clauseBasicResponseDTOS, AgreementSectionResponseDTO agreementSectionResponseDTO) {
        List<ClauseBasicResponseDTO> clauses = new ArrayList<>();
        Map<BigInteger, AgreementSectionClause> clauseCkEditorVOMap = new HashMap<>();
 /*       if (CollectionUtils.isNotEmpty(agreementSectionResponseDTO.getClauseCkEditorVOS())) {
            clauseCkEditorVOMap = agreementSectionResponseDTO.getClauseCkEditorVOS().stream().collect(Collectors.toMap(ClauseCkEditorVO::getId, clauseCkEditorVO -> clauseCkEditorVO));
        }
        List<BigInteger> clauseIdOrderIndex = agreementSectionResponseDTO.getClauseIdOrderedIndex();
        for (int i = 0; i < clauseIdOrderIndex.size(); i++) {
            ClauseBasicResponseDTO clause = clauseBasicResponseDTOS.get(clauseIdOrderIndex.get(i));
            if (clauseCkEditorVOMap.containsKey(clause.getId())) {
                ClauseCkEditorVO clauseCkEditorVO = clauseCkEditorVOMap.get(clause.getId());
                clause.setTitleHtml(clauseCkEditorVO.getTitleHtml());
                clause.setDescriptionHtml(clauseCkEditorVO.getDescriptionHtml());
            }
            clauses.add(clause);
        }
        agreementSectionResponseDTO.setClauses(clauses);
        agreementSectionResponseDTO.getClauseCkEditorVOS().clear();
        agreementSectionResponseDTO.getClauseIdOrderedIndex().clear();*/
    }


    /**
     * @param referenceId
     * @param clauseId
     * @description - return list of Agreement Template Conatining clause in Section and Sub Sections
     */
    public List<AgreementTemplateBasicResponseDTO> getAllAgreementTemplateByReferenceIdAndClauseId(Long referenceId, boolean isOrganization, BigInteger clauseId) {
        //TODO
        // return policyAgreementTemplateRepository.findAllByReferenceIdAndClauseId(referenceId, isOrganization, clauseId);
        return new ArrayList<>();
    }

    /**
     * @param referenceId    - countryId or unitId
     * @param isOrganization - isOrganization boolean to check whether referenceId id country id or unit id
     * @param templateId     - Agreement Template id
     * @return
     */
    public boolean deletePolicyAgreementTemplate(Long referenceId, boolean isOrganization, Long templateId) {

        PolicyAgreementTemplate policyAgreementTemplate = isOrganization ? policyAgreementRepository.findByIdAndOrganizationIdAndDeletedFalse(templateId, referenceId) : policyAgreementRepository.findByIdAndCountryIdAndDeletedFalse(templateId, referenceId);
        if (!Optional.ofNullable(policyAgreementTemplate).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.policy.agreementTemplate", templateId);
        }
        policyAgreementTemplate.delete();
        policyAgreementRepository.save(policyAgreementTemplate);
        return true;

    }

    //get country template by unitId
    public List<TemplateType> getAllTemplateType(Long unitId) {
        Long countryId = genericRestClient.publishRequest(null, unitId, true, IntegrationOperation.GET, "/country_id", null, new ParameterizedTypeReference<RestTemplateResponseEnvelope<Long>>() {
        });
        return templateTypeRepository.getAllTemplateType(countryId);
    }


}


