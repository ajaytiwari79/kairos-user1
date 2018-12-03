package com.kairos.service.agreement_template;


import com.kairos.dto.gdpr.*;
import com.kairos.dto.gdpr.agreement_template.AgreementTemplateDTO;
import com.kairos.dto.gdpr.data_inventory.OrganizationTypeAndSubTypeIdDTO;
import com.kairos.dto.gdpr.agreement_template.AgreementTemplateClauseUpdateDTO;
import com.kairos.dto.gdpr.agreement_template.MasterAgreementTemplateDTO;
import com.kairos.persistence.model.agreement_template.AgreementSection;
import com.kairos.dto.gdpr.agreement_template.CoverPageVO;
import com.kairos.persistence.model.agreement_template.PolicyAgreementTemplate;
import com.kairos.persistence.model.clause.Clause;
import com.kairos.persistence.model.clause.ClauseCkEditorVO;
import com.kairos.persistence.repository.agreement_template.AgreementSectionMongoRepository;
import com.kairos.persistence.repository.agreement_template.PolicyAgreementTemplateRepository;
import com.kairos.persistence.repository.clause.ClauseMongoRepository;
import com.kairos.response.dto.clause.ClauseBasicResponseDTO;
import com.kairos.response.dto.policy_agreement.AgreementSectionResponseDTO;
import com.kairos.response.dto.policy_agreement.AgreementTemplateBasicResponseDTO;
import com.kairos.response.dto.policy_agreement.AgreementTemplateSectionResponseDTO;
import com.kairos.response.dto.policy_agreement.PolicyAgreementTemplateResponseDTO;
import com.kairos.service.common.MongoBaseService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.s3bucket.AWSBucketService;
import com.kairos.service.template_type.TemplateTypeService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class PolicyAgreementTemplateService extends MongoBaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyAgreementTemplateService.class);

    @Inject
    private PolicyAgreementTemplateRepository policyAgreementTemplateRepository;


    @Inject
    private AgreementSectionService agreementSectionService;

    @Inject
    private ExceptionService exceptionService;

    @Inject
    private TemplateTypeService templateTypeService;

    @Inject
    private ClauseMongoRepository clauseMongoRepository;

    @Inject
    private AgreementSectionMongoRepository agreementSectionMongoRepository;

    @Inject
    private AWSBucketService awsBucketService;


    /**
     * @param referenceId                - countryId or unitId
     * @param -                          isUnitId boolean to check whether referenceId id country id or unit id
     * @param policyAgreementTemplateDto
     * @return return object of basic policy agreement template.
     * @description this method creates a basic policy Agreement template with basic detail about organization type,
     * organizationSubTypes ,service Category and sub service Category.
     */
    public <E extends AgreementTemplateDTO> E saveAgreementTemplate(Long referenceId, boolean isUnitId, E policyAgreementTemplateDto) {

        PolicyAgreementTemplate previousTemplate = isUnitId ? policyAgreementTemplateRepository.findByUnitIdAndName(referenceId, policyAgreementTemplateDto.getName())
                : policyAgreementTemplateRepository.findByCountryIdAndName(referenceId, policyAgreementTemplateDto.getName());
        if (Optional.ofNullable(previousTemplate).isPresent()) {
            exceptionService.duplicateDataException("message.duplicate", "message.policy.agreementTemplate", policyAgreementTemplateDto.getName());
        }
        PolicyAgreementTemplate policyAgreementTemplate = buildAgreementTemplate(referenceId, isUnitId, policyAgreementTemplateDto);
        policyAgreementTemplateRepository.save(policyAgreementTemplate);
        policyAgreementTemplateDto.setId(policyAgreementTemplate.getId());
        return policyAgreementTemplateDto;

    }

    private <E extends AgreementTemplateDTO> PolicyAgreementTemplate buildAgreementTemplate(Long referenceId, boolean isUnitId, E policyAgreementTemplateDto) {

        PolicyAgreementTemplate policyAgreementTemplate = new PolicyAgreementTemplate(policyAgreementTemplateDto.getName(), policyAgreementTemplateDto.getDescription(), policyAgreementTemplateDto.getTemplateTypeId());
        if (isUnitId) {
            policyAgreementTemplate.setOrganizationId(referenceId);
        } else {
            MasterAgreementTemplateDTO agreementTemplateDTO = (MasterAgreementTemplateDTO) policyAgreementTemplateDto;
            policyAgreementTemplate.setOrganizationTypes(agreementTemplateDTO.getOrganizationTypes());
            policyAgreementTemplate.setOrganizationSubTypes(agreementTemplateDTO.getOrganizationSubTypes());
            policyAgreementTemplate.setOrganizationServices(agreementTemplateDTO.getOrganizationServices());
            policyAgreementTemplate.setOrganizationSubServices(agreementTemplateDTO.getOrganizationSubServices());
            policyAgreementTemplate.setAccountTypes(agreementTemplateDTO.getAccountTypes());
            policyAgreementTemplate.setCountryId(referenceId);
        }
        return policyAgreementTemplate;
    }


    /**
     * @param referenceId         - country Id or unitId
     * @param -                   isUnitId boolean to check whether referenceId id country id or unit id
     * @param agreementTemplateId - Agreement Template id
     * @param coverPageLogo       - Agreement Cover page
     * @return -Url of image uploaded at S3 bucket
     */
    public String uploadCoverPageLogo(Long referenceId, boolean isUnitId, BigInteger agreementTemplateId, MultipartFile coverPageLogo) {

        PolicyAgreementTemplate policyAgreementTemplate = isUnitId ? policyAgreementTemplateRepository.findByUnitIdAndId(referenceId, agreementTemplateId) : policyAgreementTemplateRepository.findByCountryIdAndId(referenceId, agreementTemplateId);
        if (!Optional.ofNullable(policyAgreementTemplate).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.policy.agreementTemplate", agreementTemplateId);
        }
        String coverPageLogoUrl = awsBucketService.uploadImage(coverPageLogo);
        if (policyAgreementTemplate.isCoverPageAdded()) {
            policyAgreementTemplate.getCoverPageData().setCoverPageLogoUrl(coverPageLogoUrl);
        } else {
            policyAgreementTemplate.setCoverPageData(new CoverPageVO(coverPageLogoUrl));
            policyAgreementTemplate.setCoverPageAdded(true);
        }

        policyAgreementTemplateRepository.save(policyAgreementTemplate);
        return coverPageLogoUrl;
    }

    /**
     * @param countryId
     * @return return agreement section list with empty section array as per front end requirement
     */
    public List<PolicyAgreementTemplateResponseDTO> getAllAgreementTemplateByCountryId(Long countryId) {
        return policyAgreementTemplateRepository.findAllTemplateByCountryIdOrUnitId(countryId, false);
    }

    /**
     * @param unitId -
     * @return return agreement section list with empty section array as per front end requirement
     */
    public List<PolicyAgreementTemplateResponseDTO> getAllAgreementTemplateByUnitId(Long unitId) {
        return policyAgreementTemplateRepository.findAllTemplateByCountryIdOrUnitId(unitId, true);
    }


    /**
     * @param referenceId                - countryId or unitId
     * @param isUnitId                   isUnitId boolean to check whether referenceId id coutry id or unit id
     * @param agreementTemplateId        - Agreement Template id
     * @param policyAgreementTemplateDto
     * @return
     */
    public <E extends AgreementTemplateDTO> E updatePolicyAgreementTemplateBasicDetails(Long referenceId, boolean isUnitId, BigInteger agreementTemplateId, E policyAgreementTemplateDto) {

        PolicyAgreementTemplate template = isUnitId ? policyAgreementTemplateRepository.findByUnitIdAndName(referenceId, policyAgreementTemplateDto.getName()) : policyAgreementTemplateRepository.findByCountryIdAndName(referenceId, policyAgreementTemplateDto.getName());
        if (Optional.ofNullable(template).isPresent() && !agreementTemplateId.equals(template.getId())) {
            exceptionService.duplicateDataException("message.duplicate", "message.policy.agreementTemplate", policyAgreementTemplateDto.getName());
        }
        template = policyAgreementTemplateRepository.findOne(agreementTemplateId);
        template.setName(policyAgreementTemplateDto.getName());
        template.setDescription(policyAgreementTemplateDto.getDescription());
        template.setTemplateTypeId(policyAgreementTemplateDto.getTemplateTypeId());
        if (!isUnitId) {
            MasterAgreementTemplateDTO agreementTemplateDTO = (MasterAgreementTemplateDTO) policyAgreementTemplateDto;
            template.setOrganizationTypes(agreementTemplateDTO.getOrganizationTypes());
            template.setOrganizationSubTypes(agreementTemplateDTO.getOrganizationSubTypes());
            template.setOrganizationServices(agreementTemplateDTO.getOrganizationServices());
            template.setOrganizationSubServices(agreementTemplateDTO.getOrganizationSubServices());
            template.setAccountTypes(agreementTemplateDTO.getAccountTypes());
        }
        policyAgreementTemplateRepository.save(template);
        return policyAgreementTemplateDto;

    }


    /**
     * @param referenceId         - countryId or unitId
     * @param isUnitId            - to check whether referenceId country id or unit id
     * @param agreementTemplateId
     * @return
     * @description method return list of Agreement sections with sub sections of policy agreement template
     */
    public AgreementTemplateSectionResponseDTO getAllSectionsAndSubSectionOfAgreementTemplateByAgreementTemplateIdAndReferenceId(Long referenceId, boolean isUnitId, BigInteger agreementTemplateId) {
        PolicyAgreementTemplate template = isUnitId ? policyAgreementTemplateRepository.findByUnitIdAndId(referenceId, agreementTemplateId) : policyAgreementTemplateRepository.findByCountryIdAndId(referenceId, agreementTemplateId);
        if (!Optional.ofNullable(template).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.policy.agreementTemplate", agreementTemplateId);
        }
        AgreementTemplateSectionResponseDTO agreementTemplateResponse = new AgreementTemplateSectionResponseDTO();
        OrganizationTypeAndSubTypeIdDTO organizationMetaDataDTO = new OrganizationTypeAndSubTypeIdDTO(template.getOrganizationTypes().stream().map(OrganizationType::getId).collect(Collectors.toList()),
                template.getOrganizationSubTypes().stream().map(OrganizationSubType::getId).collect(Collectors.toList()),
                template.getOrganizationServices().stream().map(ServiceCategory::getId).collect(Collectors.toList()),
                template.getOrganizationSubServices().stream().map(SubServiceCategory::getId).collect(Collectors.toList()));
        List<ClauseBasicResponseDTO> clauseListForTemplate = isUnitId ? clauseMongoRepository.findAllClauseByUnitId(referenceId) : clauseMongoRepository.findAllClauseByAgreementTemplateMetadataAndCountryId(referenceId, organizationMetaDataDTO);
        List<AgreementSectionResponseDTO> agreementSectionResponseDTOS = policyAgreementTemplateRepository.getAllAgreementSectionsAndSubSectionByReferenceIdAndAgreementTemplateId(referenceId, isUnitId, agreementTemplateId);
        agreementSectionResponseDTOS.forEach(agreementSectionResponseDTO ->
                {
                    Map<BigInteger, ClauseBasicResponseDTO> clauseBasicResponseDTOS = agreementSectionResponseDTO.getClauses().stream().collect(Collectors.toMap(ClauseBasicResponseDTO::getId, clauseBasicDTO -> clauseBasicDTO));
                    sortClauseOfAgreementSectionAndSubSectionInResponseDTO(clauseBasicResponseDTOS, agreementSectionResponseDTO);
                    if (!Optional.ofNullable(agreementSectionResponseDTO.getSubSections().get(0).getId()).isPresent()) {
                        agreementSectionResponseDTO.getSubSections().clear();
                    } else {
                        agreementSectionResponseDTO.getSubSections().forEach(agreementSubSectionResponseDTO -> {
                            Map<BigInteger, ClauseBasicResponseDTO> subSectionClauseBasicResponseDTOS = agreementSubSectionResponseDTO.getClauses().stream().collect(Collectors.toMap(ClauseBasicResponseDTO::getId, clauseBasicDTO -> clauseBasicDTO));
                            sortClauseOfAgreementSectionAndSubSectionInResponseDTO(subSectionClauseBasicResponseDTOS, agreementSubSectionResponseDTO);
                        });
                    }
                }
        );
        List<ClauseBasicResponseDTO> clauseBasicResponseDTOS=policyAgreementTemplateRepository.getAllClausesByAgreementTemplateId(referenceId,isUnitId,agreementTemplateId);
       Map<BigInteger,ClauseBasicResponseDTO> clauseBasicResponseDTOMap=new HashMap<>();
       clauseBasicResponseDTOS.forEach(clauseBasicResponseDTO -> {
           clauseBasicResponseDTOMap.put(clauseBasicResponseDTO.getId(),clauseBasicResponseDTO);
       });
        clauseListForTemplate.stream().forEach(clauseBasicResponseDTO -> {
            if(clauseBasicResponseDTOMap.get(clauseBasicResponseDTO.getId())!=null){
                clauseBasicResponseDTO.setLinkedWithOtherTemplate(true);
            }
        });
        agreementTemplateResponse.setClauseListForTemplate(clauseListForTemplate);
        agreementTemplateResponse.setSections(agreementSectionResponseDTOS);
        agreementTemplateResponse.setCoverPageAdded(template.isCoverPageAdded());
        agreementTemplateResponse.setCoverPageData(template.getCoverPageData());
        agreementTemplateResponse.setSignatureComponentAdded(template.isSignatureComponentAdded());
        agreementTemplateResponse.setSignatureComponentLeftAlign(template.isSignatureComponentLeftAlign());
        agreementTemplateResponse.setSignatureComponentRightAlign(template.isSignatureComponentRightAlign());
        agreementTemplateResponse.setSignatureHtml(template.getSignatureHtml());
        return agreementTemplateResponse;
    }

    private void sortClauseOfAgreementSectionAndSubSectionInResponseDTO(Map<BigInteger, ClauseBasicResponseDTO> clauseBasicResponseDTOS, AgreementSectionResponseDTO agreementSectionResponseDTO) {
        List<ClauseBasicResponseDTO> clauses = new ArrayList<>();
        Map<BigInteger, ClauseCkEditorVO> clauseCkEditorVOMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(agreementSectionResponseDTO.getClauseCkEditorVOS())) {
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
        agreementSectionResponseDTO.getClauseIdOrderedIndex().clear();
    }


    /**
     * @param referenceId
     * @param clauseId
     * @description - return list of Agreement Template Conatining clause in Section and Sub Sections
     */
    public List<AgreementTemplateBasicResponseDTO> getAllAgreementTemplateByReferenceIdAndClauseId(Long referenceId, boolean isUnitId, BigInteger clauseId) {
        return policyAgreementTemplateRepository.findAllByReferenceIdAndClauseId(referenceId, isUnitId, clauseId);
    }


    /**
     * @param referenceId
     * @param agreementTemplateClauseUpdateDTO - agreement template ids , clause previous id and new clause id
     * @Description method update agreement template section containing previous clause with new clause
     */
    public boolean updateAgreementTemplateClauseWithNewVersionByReferenceIdAndTemplateIds(Long referenceId, boolean isUnitId, AgreementTemplateClauseUpdateDTO agreementTemplateClauseUpdateDTO) {

        List<AgreementSection> agreementSectionsAndSubSectionsContainingClause = policyAgreementTemplateRepository.getAllAgreementSectionAndSubSectionByReferenceIdAndClauseId(referenceId, isUnitId, agreementTemplateClauseUpdateDTO.getAgreementTemplateIds(), agreementTemplateClauseUpdateDTO.getPreviousClauseId());
        Clause clause = clauseMongoRepository.findOne(agreementTemplateClauseUpdateDTO.getNewClauseId());

        if (CollectionUtils.isNotEmpty(agreementSectionsAndSubSectionsContainingClause)) {
            agreementSectionsAndSubSectionsContainingClause.forEach(agreementSection -> {
                ClauseCkEditorVO clauseCkEditorVO = new ClauseCkEditorVO(agreementTemplateClauseUpdateDTO.getNewClauseId(), "<p>" + clause.getTitle() + "</p>", "<p>" + clause.getDescription() + "</p>");
                List<ClauseCkEditorVO> clauseCkEditorVOS = new ArrayList<>(agreementSection.getClauseCkEditorVOS());
                ListIterator<ClauseCkEditorVO> clauseCkEditorVOIterator = clauseCkEditorVOS.listIterator();
                while (clauseCkEditorVOIterator.hasNext()) {
                    ClauseCkEditorVO clauseCkEditorVO1 = clauseCkEditorVOIterator.next();
                    if (agreementTemplateClauseUpdateDTO.getPreviousClauseId().equals(clauseCkEditorVO1.getId())) {
                        clauseCkEditorVOIterator.set(clauseCkEditorVO);
                    }
                }
                agreementSection.setClauseCkEditorVOS(new HashSet<>(clauseCkEditorVOS));
                int clauseIndex = agreementSection.getClauseIdOrderedIndex().indexOf(agreementTemplateClauseUpdateDTO.getPreviousClauseId());
                agreementSection.getClauseIdOrderedIndex().set(clauseIndex, agreementTemplateClauseUpdateDTO.getNewClauseId());
            });
            agreementSectionMongoRepository.saveAll(getNextSequence(agreementSectionsAndSubSectionsContainingClause));
        }
        return true;
    }


    /**
     * @param referenceId - countryId or unitId
     * @param isUnitId    - isUnitId boolean to check whether referenceId id country id or unit id
     * @param templateId  - Agreement Template id
     * @return
     */
    public boolean deletePolicyAgreementTemplate(Long referenceId, boolean isUnitId, BigInteger templateId) {

        PolicyAgreementTemplate policyAgreementTemplate = isUnitId ? policyAgreementTemplateRepository.findByUnitIdAndId(referenceId, templateId) : policyAgreementTemplateRepository.findByCountryIdAndId(referenceId, templateId);
        if (!Optional.ofNullable(policyAgreementTemplate).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.policy.agreementTemplate", templateId);
        }
        delete(policyAgreementTemplate);
        return true;

    }


}


