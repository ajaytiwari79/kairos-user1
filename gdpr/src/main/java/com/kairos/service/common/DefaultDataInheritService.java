package com.kairos.service.common;


import com.kairos.dto.gdpr.*;
import com.kairos.enums.gdpr.QuestionnaireTemplateStatus;
import com.kairos.persistence.model.clause.Clause;
import com.kairos.persistence.model.clause_tag.ClauseTag;
import com.kairos.persistence.model.data_inventory.asset.Asset;
import com.kairos.persistence.model.data_inventory.processing_activity.ProcessingActivity;
import com.kairos.persistence.model.master_data.data_category_element.DataCategory;
import com.kairos.persistence.model.master_data.data_category_element.DataElement;
import com.kairos.persistence.model.master_data.data_category_element.DataSubjectMapping;
import com.kairos.persistence.model.master_data.default_asset_setting.*;
import com.kairos.persistence.model.master_data.default_proc_activity_setting.*;
import com.kairos.persistence.model.questionnaire_template.Question;
import com.kairos.persistence.model.questionnaire_template.QuestionnaireSection;
import com.kairos.persistence.model.questionnaire_template.QuestionnaireTemplate;
import com.kairos.persistence.model.risk_management.Risk;
import com.kairos.persistence.repository.clause.ClauseMongoRepository;
import com.kairos.persistence.repository.clause_tag.ClauseTagMongoRepository;
import com.kairos.persistence.repository.data_inventory.asset.AssetMongoRepository;
import com.kairos.persistence.repository.data_inventory.processing_activity.ProcessingActivityMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.AssetTypeMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.MasterAssetMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.data_disposal.DataDisposalMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.hosting_provider.HostingProviderMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.hosting_type.HostingTypeMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.org_security_measure.OrganizationalSecurityMeasureMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.storage_format.StorageFormatMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.tech_security_measure.TechnicalSecurityMeasureMongoRepository;
import com.kairos.persistence.repository.master_data.data_category_element.DataCategoryMongoRepository;
import com.kairos.persistence.repository.master_data.data_category_element.DataElementMongoRepository;
import com.kairos.persistence.repository.master_data.data_category_element.DataSubjectMappingRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.MasterProcessingActivityRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.accessor_party.AccessorPartyMongoRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.data_source.DataSourceMongoRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.legal_basis.ProcessingLegalBasisMongoRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.processing_purpose.ProcessingPurposeMongoRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.responsibility_type.ResponsibilityTypeMongoRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.transfer_method.TransferMethodMongoRepository;
import com.kairos.persistence.repository.questionnaire_template.QuestionMongoRepository;
import com.kairos.persistence.repository.questionnaire_template.QuestionnaireSectionRepository;
import com.kairos.persistence.repository.questionnaire_template.QuestionnaireTemplateMongoRepository;
import com.kairos.persistence.repository.risk_management.RiskMongoRepository;
import com.kairos.response.dto.common.*;
import com.kairos.response.dto.master_data.AssetTypeRiskResponseDTO;
import com.kairos.response.dto.master_data.MasterAssetResponseDTO;
import com.kairos.response.dto.master_data.MasterProcessingActivityResponseDTO;
import com.kairos.response.dto.master_data.data_mapping.DataCategoryResponseDTO;
import com.kairos.response.dto.master_data.data_mapping.DataSubjectMappingResponseDTO;
import com.kairos.response.dto.master_data.questionnaire_template.QuestionBasicResponseDTO;
import com.kairos.response.dto.master_data.questionnaire_template.QuestionnaireSectionResponseDTO;
import com.kairos.response.dto.master_data.questionnaire_template.QuestionnaireTemplateResponseDTO;
import com.kairos.service.AsynchronousService;
import com.kairos.service.data_subject_management.DataSubjectMappingService;
import com.kairos.service.questionnaire_template.QuestionnaireTemplateService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefaultDataInheritService extends MongoBaseService {


    @Inject
    private AsynchronousService asynchronousService;
    @Inject
    private MasterAssetMongoRepository masterAssetMongoRepository;
    @Inject
    private MasterProcessingActivityRepository masterProcessingActivityRepository;
    @Inject
    private AssetMongoRepository assetMongoRepository;
    @Inject
    private ProcessingActivityMongoRepository processingActivityMongoRepository;
    @Inject
    private DataDisposalMongoRepository dataDisposalMongoRepository;
    @Inject
    private HostingProviderMongoRepository hostingProviderMongoRepository;
    @Inject
    private HostingTypeMongoRepository hostingTypeMongoRepository;
    @Inject
    private OrganizationalSecurityMeasureMongoRepository organizationalSecurityMeasureMongoRepository;
    @Inject
    private StorageFormatMongoRepository storageFormatMongoRepository;
    @Inject
    private TechnicalSecurityMeasureMongoRepository technicalSecurityMeasureMongoRepository;
    @Inject
    private AccessorPartyMongoRepository accessorPartyMongoRepository;
    @Inject
    private DataSourceMongoRepository dataSourceMongoRepository;
    @Inject
    private ProcessingLegalBasisMongoRepository processingLegalBasisMongoRepository;
    @Inject
    private ProcessingPurposeMongoRepository processingPurposeMongoRepository;
    @Inject
    private ResponsibilityTypeMongoRepository responsibilityTypeMongoRepository;
    @Inject
    private TransferMethodMongoRepository transferMethodMongoRepository;
    @Inject
    private DataSubjectMappingService dataSubjectMappingService;
    @Inject
    private DataCategoryMongoRepository dataCategoryMongoRepository;
    @Inject
    private RiskMongoRepository riskMongoRepository;
    @Inject
    private AssetTypeMongoRepository assetTypeMongoRepository;
    @Inject
    private QuestionnaireTemplateService questionnaireTemplateService;
    @Inject
    private QuestionMongoRepository questionMongoRepository;
    @Inject
    private QuestionnaireSectionRepository questionnaireSectionRepository;
    @Inject
    private QuestionnaireTemplateMongoRepository questionnaireTemplateMongoRepository;
    @Inject
    private DataElementMongoRepository dataElementMongoRepository;
    @Inject
    private DataSubjectMappingRepository dataSubjectMappingRepository;
    @Inject
    private ClauseTagMongoRepository clauseTagMongoRepository;
    @Inject
    private ClauseMongoRepository clauseMongoRepository;


    private Map<String, BigInteger> globalAssetTypeAndSubAssetTypeMap = new HashMap<>();
    private Map<String, BigInteger> globalCategoryNameAndIdMap = new HashMap<>();


    public boolean copyMasterDataFromCountry(Long unitId, OrgTypeSubTypeServiceCategoryVO orgTypeSubTypeServiceCategoryVO) throws Exception {

        //Long countryId = orgTypeSubTypeServiceCategoryVO.getCountryId();
        //OrganizationTypeAndSubTypeIdDTO organizationMetaDataDTO = new OrganizationTypeAndSubTypeIdDTO(Collections.singletonList(orgTypeSubTypeServiceCategoryVO.getId());
               // orgTypeSubTypeServiceCategoryVO.getOrganizationSubTypes().stream().map(OrganizationSubType::getId).collect(Collectors.toList()),
                //orgTypeSubTypeServiceCategoryVO.getOrganizationServices().stream().map(ServiceCategory::getId).collect(Collectors.toList()),
               // orgTypeSubTypeServiceCategoryVO.getOrganizationSubServices().stream().map(SubServiceCategory::getId).collect(Collectors.toList()));
        //List<AssetTypeRiskResponseDTO> assetTypeDTOS = assetTypeMongoRepository.getAllAssetTypeWithSubAssetTypeAndRiskByCountryId(countryId);
        //List<DataCategoryResponseDTO> dataCategoryDTOS = dataCategoryMongoRepository.getAllDataCategoryWithDataElement(countryId);
        //saveAssetTypeAndAssetSubType(unitId, assetTypeDTOS);
        //copyDataCategoryAndDataElements(unitId, dataCategoryDTOS);


        //List<Callable<Boolean>> callables = new ArrayList<>();
       /* Callable<Boolean> dataDispoaslTask = () -> {
            List<DataDisposalResponseDTO> dataDisposalResponseDTOS = dataDisposalMongoRepository.findAllByCountryId(countryId);
            saveDataDisposal(unitId, dataDisposalResponseDTOS);
            return true;
        };Callable<Boolean> hostingProviderTask = () -> {
            List<HostingProviderResponseDTO> hostingProviderDTOS = hostingProviderMongoRepository.findAllByCountryId(countryId);
            saveHostingProvider(unitId, hostingProviderDTOS);
            return true;
        };
        Callable<Boolean> hostingTypeTask = () -> {
            List<HostingTypeResponseDTO> hostingTypeDTOS = hostingTypeMongoRepository.findAllByCountryId(countryId);
            saveHostingType(unitId, hostingTypeDTOS);
            return true;

        };
        Callable<Boolean> storageFormatTask = () -> {
            List<StorageFormatResponseDTO> storageFormatDTOS = storageFormatMongoRepository.findAllByCountryId(countryId);
            saveStorageFormat(unitId, storageFormatDTOS);
            return true;

        };
        Callable<Boolean> technicalSecurityMeasureTask = () -> {

            List<TechnicalSecurityMeasureResponseDTO> techSecurityMeasureDTOS = technicalSecurityMeasureMongoRepository.findAllByCountryId(countryId);
            saveTechnicalSecurityMeasure(unitId, techSecurityMeasureDTOS);
            return true;

        };
        Callable<Boolean> orgSecurityMeasureTask = () -> {
            List<OrganizationalSecurityMeasureResponseDTO> orgSecurityMeasureDTOS = organizationalSecurityMeasureMongoRepository.findAllByCountryId(countryId);
            saveOrgSecurityMeasure(unitId, orgSecurityMeasureDTOS);
            return true;
        };

        Callable<Boolean> accessorPartyTask = () -> {
            List<AccessorPartyResponseDTO> accessorPartyDTOS = accessorPartyMongoRepository.findAllByCountryId(countryId);
            saveAccessorParties(unitId, accessorPartyDTOS);
            return true;
        };

        Callable<Boolean> dataSourceTask = () -> {
            List<DataSourceResponseDTO> dataSourceDTOS = dataSourceMongoRepository.findAllByCountryId(countryId);
            saveDataSources(unitId, dataSourceDTOS);
            return true;
        };
        Callable<Boolean> legalBasisTask = () -> {
            List<ProcessingLegalBasisResponseDTO> legalBasisDTOS = processingLegalBasisMongoRepository.findAllByCountryId(countryId);
            saveProcessingLegalBasis(unitId, legalBasisDTOS);
            return true;
        };
        Callable<Boolean> processingPurposeTask = () -> {
            List<ProcessingPurposeResponseDTO> processingPurposeDTOS = processingPurposeMongoRepository.findAllByCountryId(countryId);
            saveProcessingPurposes(unitId, processingPurposeDTOS);
            return true;
        };
        Callable<Boolean> responsibilityTypeTask = () -> {
            List<ResponsibilityTypeResponseDTO> responsibilityTypeDTOS = responsibilityTypeMongoRepository.findAllByCountryId(countryId);
            saveResponsibilityTypes(unitId, responsibilityTypeDTOS);
            return true;
        };
        Callable<Boolean> transferMethodTask = () -> {
            List<TransferMethodResponseDTO> transferMethodDTOS = transferMethodMongoRepository.findAllByCountryId(countryId);
            saveTransferMethods(unitId, transferMethodDTOS);
            return true;
        };
        Callable<Boolean> processingActivityTask = () -> {
            List<MasterProcessingActivityResponseDTO> masterProcessingActivityDTOS = masterProcessingActivityRepository.getMasterProcessingActivityByOrgTypeSubTypeCategoryAndSubCategory(countryId, organizationMetaDataDTO);
            copyProcessingActivityAndSubProcessingActivitiesFromCountryToUnit(unitId, masterProcessingActivityDTOS);
            return true;
        };
        Callable<Boolean> questionniareTemplateTask = () -> {
            List<QuestionnaireTemplateResponseDTO> questionnaireTemplateDTOS = questionnaireTemplateService.getAllMasterQuestionnaireTemplateWithSection(countryId);
            copyQuestionnaireTemplateFromCountry(unitId, questionnaireTemplateDTOS);
            return true;
        };
        Callable<Boolean> assetTask = () -> {
            List<MasterAssetResponseDTO> masterAssetDTOS = masterAssetMongoRepository.getMasterAssetByOrgTypeSubTypeCategoryAndSubCategory(countryId, organizationMetaDataDTO);
            copyMasterAssetAndAssetTypeFromCountryToUnit(unitId, masterAssetDTOS);
            return true;
        };
        Callable<Boolean> dataSubjectTask = () -> {
            List<DataSubjectMappingResponseDTO> dataSubjectMappingDTOS = dataSubjectMappingService.getAllDataSubjectWithDataCategoryByCountryId(countryId);
            copyDataSubjectAndDataCategoryFromCountry(unitId, dataSubjectMappingDTOS);
            return true;
        };
        Callable<Boolean> clauseTask = () -> {
            List<Clause> clauses = clauseMongoRepository.getClauseByCountryIdAndOrgTypeSubTypeCategoryAndSubCategory(countryId, organizationMetaDataDTO);
            copyClauseFromCountry(unitId, clauses);
            return true;
        };

        /*callables.add(hostingProviderTask);
        callables.add(dataDispoaslTask);
        callables.add(hostingTypeTask);
        callables.add(technicalSecurityMeasureTask);
        callables.add(storageFormatTask);
        callables.add(orgSecurityMeasureTask);
        callables.add(accessorPartyTask);
        callables.add(dataSourceTask);
        callables.add(legalBasisTask);
        callables.add(processingPurposeTask);
        callables.add(responsibilityTypeTask);
        callables.add(transferMethodTask);
        callables.add(processingActivityTask);
        callables.add(questionniareTemplateTask);
        callables.add(assetTask);
        callables.add(dataSubjectTask);
        callables.add(clauseTask);
        asynchronousService.executeAsynchronously(callables);*/
        return true;
    }


    private void copyMasterAssetAndAssetTypeFromCountryToUnit(Long unitId, List<MasterAssetResponseDTO> masterAssetDTOS) {
        if (CollectionUtils.isNotEmpty(masterAssetDTOS)) {
            List<Asset> assets = new ArrayList<>();
            for (MasterAssetResponseDTO masterAssetDTO : masterAssetDTOS) {
                Asset asset = new Asset(masterAssetDTO.getName(), masterAssetDTO.getDescription(), false);
                asset.setOrganizationId(unitId);
                AssetTypeBasicResponseDTO assetTypeBasicDTO = masterAssetDTO.getAssetType();
                asset.setAssetTypeId(globalAssetTypeAndSubAssetTypeMap.get(assetTypeBasicDTO.getName().trim().toLowerCase()));
                if (Optional.of(masterAssetDTO.getAssetSubType()).isPresent()) {
                    asset.setAssetSubTypeId(globalAssetTypeAndSubAssetTypeMap.get(masterAssetDTO.getAssetSubType().getName().toLowerCase().trim()));
                }
                assets.add(asset);
            }
            assetMongoRepository.saveAll(getNextSequence(assets));
        }
    }

    private void copyClauseFromCountry(Long unitId, List<Clause> clauses) {
        if (CollectionUtils.isNotEmpty(clauses)) {

            Set<BigInteger> clauseTagIds = new HashSet<>();
            List<ClauseTag> clauseTags = new ArrayList<>();
            List<Clause> clauseList = new ArrayList<>();
            clauses.forEach(clauseResponse -> {
                Clause clause = new Clause(clauseResponse.getTitle(), clauseResponse.getDescription());
                clause.setOrganizationId(unitId);
                List<ClauseTag> tags = new ArrayList<>();
                clauseResponse.getTags().forEach(clauseTag -> {
                    if (!clauseTagIds.contains(clauseTag.getId())) {
                        ClauseTag tag = new ClauseTag(clauseTag.getName());
                        tag.setOrganizationId(unitId);
                        tag.setDefaultTag(clauseTag.isDefaultTag());
                        clauseTagIds.add(clauseTag.getId());
                        tags.add(clauseTag);
                    }
                });
                clause.setTags(tags);
                clauseTags.addAll(tags);
                clauseList.add(clause);
            });
            clauseTagMongoRepository.saveAll(clauseTags);
            clauseMongoRepository.saveAll(clauseList);
        }

    }


    private void copyProcessingActivityAndSubProcessingActivitiesFromCountryToUnit(Long unitId, List<MasterProcessingActivityResponseDTO> masterProcessingActivityDTOS) {

        if (CollectionUtils.isNotEmpty(masterProcessingActivityDTOS)) {
            List<ProcessingActivity> processingActivities = new ArrayList<>();
            Map<ProcessingActivity, List<ProcessingActivity>> processingActivitySubProcessingActivityListMap = new HashMap<>();
            for (MasterProcessingActivityResponseDTO masterProcessingActivityDTO : masterProcessingActivityDTOS) {
                ProcessingActivity processingActivity = new ProcessingActivity(masterProcessingActivityDTO.getName(), masterProcessingActivityDTO.getDescription(), false);
                processingActivity.setSubProcess(false);
                processingActivity.setOrganizationId(unitId);
                List<ProcessingActivity> subProcessingActivities = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(masterProcessingActivityDTO.getSubProcessingActivities())) {
                    for (MasterProcessingActivityResponseDTO subProcessingActivityDTO : masterProcessingActivityDTO.getSubProcessingActivities()) {
                        ProcessingActivity subProcessingActivity = new ProcessingActivity(subProcessingActivityDTO.getName(), subProcessingActivityDTO.getDescription(), false);
                        subProcessingActivity.setOrganizationId(unitId);
                        subProcessingActivity.setSubProcess(true);
                        subProcessingActivities.add(subProcessingActivity);
                    }
                    processingActivities.addAll(subProcessingActivities);
                }
                processingActivitySubProcessingActivityListMap.put(processingActivity, subProcessingActivities);
            }

            if (CollectionUtils.isNotEmpty(processingActivities)) {
                processingActivityMongoRepository.saveAll(getNextSequence(processingActivities));
            }
            processingActivitySubProcessingActivityListMap.forEach((processingActivity, subProcessingActivities) -> {
                if (CollectionUtils.isNotEmpty(subProcessingActivities)) {
                    processingActivity.setSubProcessingActivities(subProcessingActivities.stream().map(ProcessingActivity::getId).collect(Collectors.toList()));
                }
            });
            processingActivities = new ArrayList<>(processingActivitySubProcessingActivityListMap.keySet());
            processingActivityMongoRepository.saveAll(getNextSequence(processingActivities));
        }
    }


    private void copyDataCategoryAndDataElements(Long unitId, List<DataCategoryResponseDTO> dataCategoryDTOS) {
        if (CollectionUtils.isNotEmpty(dataCategoryDTOS)) {

            Map<DataCategory, List<DataElement>> dataCategoryAndDataElementListMap = new HashMap<>();
            for (DataCategoryResponseDTO dataCategoryDTO : dataCategoryDTOS) {
                DataCategory dataCategory = new DataCategory(dataCategoryDTO.getName());
                dataCategory.setOrganizationId(unitId);
                List<DataElement> dataElementList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(dataCategoryDTO.getDataElements())) {
                   /* for (DataElementMD dataElementBasicResponseDTO : dataCategoryDTO.getDataElements()) {
                        DataElement dataElement = new DataElement(dataElementBasicResponseDTO.getName());
                        dataElement.setOrganizationId(unitId);
                        dataElementList.add(dataElement);
                    }*/
                }
                dataCategoryAndDataElementListMap.put(dataCategory, dataElementList);
            }

            List<DataElement> dataElements = new ArrayList<>();
            dataCategoryAndDataElementListMap.forEach((dataCategory, dataElementList) -> dataElements.addAll(dataElementList));
            if (CollectionUtils.isNotEmpty(dataElements)) {
                dataElementMongoRepository.saveAll(getNextSequence(dataElements));
            }
            List<DataCategory> dataCategories = new ArrayList<>(dataCategoryAndDataElementListMap.keySet());
            dataCategories.forEach(dataCategory -> dataCategory.setDataElements(dataCategoryAndDataElementListMap.get(dataCategory).stream().map(DataElement::getId).collect(Collectors.toList())));
            dataCategoryMongoRepository.saveAll(getNextSequence(dataCategories));
            dataCategories.parallelStream().forEach(dataCategory -> globalCategoryNameAndIdMap.put(dataCategory.getName().trim().toLowerCase(), dataCategory.getId()));

        }
    }


    private void copyDataSubjectAndDataCategoryFromCountry(Long unitId, List<DataSubjectMappingResponseDTO> dataSubjectMappingResponseDTOS) {
        if (CollectionUtils.isNotEmpty(dataSubjectMappingResponseDTOS)) {
            List<DataSubjectMapping> dataSubjects = new ArrayList<>();
            for (DataSubjectMappingResponseDTO dataSubjectDTO : dataSubjectMappingResponseDTOS) {
                DataSubjectMapping dataSubjectMapping = new DataSubjectMapping(dataSubjectDTO.getName(), dataSubjectDTO.getDescription());
                dataSubjectMapping.setOrganizationId(unitId);
                if (CollectionUtils.isNotEmpty(dataSubjectDTO.getDataCategories())) {
                    Set<BigInteger> dataCategoryIds = new HashSet<>();
                    dataSubjectDTO.getDataCategories().parallelStream().forEach(dataCategoryDTO -> dataCategoryIds.add(globalCategoryNameAndIdMap.get(dataCategoryDTO.getName().toLowerCase().trim())));
                    dataSubjectMapping.setDataCategories(dataCategoryIds);
                }
                dataSubjects.add(dataSubjectMapping);
            }
            dataSubjectMappingRepository.saveAll(getNextSequence(dataSubjects));
        }

    }


    private void copyQuestionnaireTemplateFromCountry(Long unitId, List<QuestionnaireTemplateResponseDTO> questionnaireTemplateDTOS) {


        Map<QuestionnaireTemplate, List<QuestionnaireSection>> questionnaireTemplateAndSectionListMap = new HashMap<>();
        Map<QuestionnaireSection, List<Question>> questionnaireSectionAndQuestionListMap = new HashMap<>();


        for (QuestionnaireTemplateResponseDTO questionnaireTemplateDTO : questionnaireTemplateDTOS) {

            QuestionnaireTemplate questionnaireTemplate = buildQuestionnaireTemplate(unitId, questionnaireTemplateDTO);
            List<QuestionnaireSection> questionnaireSections = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(questionnaireTemplateDTO.getSections())) {
                for (QuestionnaireSectionResponseDTO questionnaireSectionDTO : questionnaireTemplateDTO.getSections()) {
                    QuestionnaireSection questionnaireSection = new QuestionnaireSection(questionnaireSectionDTO.getTitle());
                    questionnaireSection.setOrganizationId(unitId);
                    questionnaireSections.add(questionnaireSection);
                    if (CollectionUtils.isNotEmpty(questionnaireSectionDTO.getQuestions())) {
                        List<Question> questions = new ArrayList<>();
                        for (QuestionBasicResponseDTO questionBasicDTO : questionnaireSectionDTO.getQuestions()) {
                            Question question = new Question(questionBasicDTO.getQuestion(), questionBasicDTO.getDescription(), questionBasicDTO.isRequired(), questionBasicDTO.getQuestionType(), questionBasicDTO.isNotSureAllowed());
                            question.setOrganizationId(unitId);
                            questions.add(question);
                        }
                        questionnaireSectionAndQuestionListMap.put(questionnaireSection, questions);
                    }
                }
                questionnaireTemplateAndSectionListMap.put(questionnaireTemplate, questionnaireSections);
            }
        }

        saveQuestionAndAddToQuestionnaireSection(questionnaireSectionAndQuestionListMap);
        saveQuestionnaireSectionAndAddToQuestionnaireTemplate(questionnaireTemplateAndSectionListMap);
        List<QuestionnaireTemplate> questionnaireTemplates = new ArrayList<>(questionnaireTemplateAndSectionListMap.keySet());
        questionnaireTemplateMongoRepository.saveAll(getNextSequence(questionnaireTemplates));
    }


    private void saveQuestionAndAddToQuestionnaireSection(Map<QuestionnaireSection, List<Question>> questionnaireSectionListMap) {

        if (CollectionUtils.isNotEmpty(questionnaireSectionListMap.keySet())) {
            List<Question> questionList = new ArrayList<>();
            questionnaireSectionListMap.forEach((questionnaireSection, questions) -> questionList.addAll(questions));
            questionMongoRepository.saveAll(getNextSequence(questionList));
            questionnaireSectionListMap.forEach((questionnaireSection, questions) -> questionnaireSection.setQuestions(questions.stream().map(Question::getId).collect(Collectors.toList())));
        }
    }


    private void saveQuestionnaireSectionAndAddToQuestionnaireTemplate(Map<QuestionnaireTemplate, List<QuestionnaireSection>> questionnaireTemplateAndSectionListMap) {
        if (CollectionUtils.isNotEmpty(questionnaireTemplateAndSectionListMap.keySet())) {
            List<QuestionnaireSection> questionnaireSectionList = new ArrayList<>();
            questionnaireTemplateAndSectionListMap.forEach((questionnaireTemplate, questionnaireSections) -> questionnaireSectionList.addAll(questionnaireSections));
            questionnaireSectionRepository.saveAll(getNextSequence(questionnaireSectionList));
            questionnaireTemplateAndSectionListMap.forEach((questionnaireTemplate, questionnaireSections) -> questionnaireTemplate.setSections(questionnaireSections.stream().map(QuestionnaireSection::getId).collect(Collectors.toList())));
        }
    }


    private QuestionnaireTemplate buildQuestionnaireTemplate(Long unitId, QuestionnaireTemplateResponseDTO questionnaireTemplateDTO) {

        QuestionnaireTemplate questionnaireTemplate = new QuestionnaireTemplate(questionnaireTemplateDTO.getName(), questionnaireTemplateDTO.getDescription(), QuestionnaireTemplateStatus.DRAFT);
        questionnaireTemplate.setOrganizationId(unitId);
        switch (questionnaireTemplateDTO.getTemplateType()) {
            case ASSET_TYPE:
                if (questionnaireTemplateDTO.isDefaultAssetTemplate()) {
                    questionnaireTemplate.setDefaultAssetTemplate(true);
                } else {
                    questionnaireTemplate.setAssetTypeId(globalAssetTypeAndSubAssetTypeMap.get(questionnaireTemplateDTO.getAssetType().getName().trim().toLowerCase()));
                    if (Optional.ofNullable(questionnaireTemplateDTO.getAssetSubType()).isPresent()) {
                        questionnaireTemplate.setAssetSubTypeId(globalAssetTypeAndSubAssetTypeMap.get(questionnaireTemplateDTO.getAssetSubType().getName().toLowerCase().trim()));
                    }
                }
                break;
            default:
                questionnaireTemplate.setTemplateType(questionnaireTemplateDTO.getTemplateType());
                break;
        }


        return questionnaireTemplate;

    }


    private void saveDataDisposal(Long unitId, List<DataDisposalResponseDTO> dataDisposalDTOS) {
        if (CollectionUtils.isNotEmpty(dataDisposalDTOS)) {
            List<DataDisposal> dataDisposalsList = new ArrayList<>();
            for (DataDisposalResponseDTO dataDisposalDTO : dataDisposalDTOS) {
                DataDisposal dataDisposal = new DataDisposal(dataDisposalDTO.getName());
                dataDisposal.setOrganizationId(unitId);
                dataDisposalsList.add(dataDisposal);
            }
            dataDisposalMongoRepository.saveAll(getNextSequence(dataDisposalsList));
        }
    }

    private void saveHostingProvider(Long unitId, List<HostingProviderResponseDTO> hostingProviderDTOS) {
        if (CollectionUtils.isNotEmpty(hostingProviderDTOS)) {
            List<HostingProvider> hostingProviderList = new ArrayList<>();
            for (HostingProviderResponseDTO hostingProviderDTO : hostingProviderDTOS) {
                HostingProvider hostingProvider = new HostingProvider(hostingProviderDTO.getName());
                hostingProvider.setOrganizationId(unitId);
                hostingProviderList.add(hostingProvider);
            }
            hostingProviderMongoRepository.saveAll(getNextSequence(hostingProviderList));

        }

    }

    private void saveHostingType(Long unitId, List<HostingTypeResponseDTO> hostingTypeDTOS) {
        if (CollectionUtils.isNotEmpty(hostingTypeDTOS)) {
            List<HostingType> hostingTypeList = new ArrayList<>();
            for (HostingTypeResponseDTO hostingTypeDTO : hostingTypeDTOS) {
                HostingType hostingType = new HostingType(hostingTypeDTO.getName());
                hostingType.setOrganizationId(unitId);
                hostingTypeList.add(hostingType);
            }
            hostingTypeMongoRepository.saveAll(getNextSequence(hostingTypeList));

        }
    }


    private void saveStorageFormat(Long unitId, List<StorageFormatResponseDTO> storageFormatDTOS) {
        if (CollectionUtils.isNotEmpty(storageFormatDTOS)) {
            List<StorageFormat> storageFormatList = new ArrayList<>();
            for (StorageFormatResponseDTO storageFormatDTO : storageFormatDTOS) {
                StorageFormat storageFormat = new StorageFormat(storageFormatDTO.getName());
                storageFormat.setOrganizationId(unitId);
                storageFormatList.add(storageFormat);
            }
            storageFormatMongoRepository.saveAll(getNextSequence(storageFormatList));
        }
    }

    private void saveTechnicalSecurityMeasure(Long unitId, List<TechnicalSecurityMeasureResponseDTO> techSecurityMeasureDTOS) {

        if (CollectionUtils.isNotEmpty(techSecurityMeasureDTOS)) {
            List<TechnicalSecurityMeasure> technicalSecurityMeasures = new ArrayList<>();
            for (TechnicalSecurityMeasureResponseDTO technicalSecurityMeasureDTO : techSecurityMeasureDTOS) {
                TechnicalSecurityMeasure technicalSecurityMeasure = new TechnicalSecurityMeasure(technicalSecurityMeasureDTO.getName());
                technicalSecurityMeasure.setOrganizationId(unitId);
                technicalSecurityMeasures.add(technicalSecurityMeasure);
            }
            technicalSecurityMeasureMongoRepository.saveAll(getNextSequence(technicalSecurityMeasures));
        }
    }

    private void saveOrgSecurityMeasure(Long unitId, List<OrganizationalSecurityMeasureResponseDTO> orgSecurityMeasureDTOS) {
        if (CollectionUtils.isNotEmpty(orgSecurityMeasureDTOS)) {
            List<OrganizationalSecurityMeasure> organizationalSecurityMeasureList = new ArrayList<>();
            for (OrganizationalSecurityMeasureResponseDTO orgSecurityMeasureDTO : orgSecurityMeasureDTOS) {
                OrganizationalSecurityMeasure organizationalSecurityMeasure = new OrganizationalSecurityMeasure(orgSecurityMeasureDTO.getName());
                organizationalSecurityMeasure.setOrganizationId(unitId);
                organizationalSecurityMeasureList.add(organizationalSecurityMeasure);
            }
            organizationalSecurityMeasureMongoRepository.saveAll(getNextSequence(organizationalSecurityMeasureList));
        }


    }

    private void saveAccessorParties(Long unitId, List<AccessorPartyResponseDTO> accessorPartyDTOS) {
        if (CollectionUtils.isNotEmpty(accessorPartyDTOS)) {
            List<AccessorParty> accessorParties = new ArrayList<>();
            for (AccessorPartyResponseDTO accessorPartyDTO : accessorPartyDTOS) {
                AccessorParty accessorParty = new AccessorParty(accessorPartyDTO.getName());
                accessorParty.setOrganizationId(unitId);
                accessorParties.add(accessorParty);
            }
            accessorPartyMongoRepository.saveAll(getNextSequence(accessorParties));
        }

    }

    private void saveDataSources(Long unitId, List<DataSourceResponseDTO> dataSourceDTOS) {
        if (CollectionUtils.isNotEmpty(dataSourceDTOS)) {
            List<DataSource> dataSourceList = new ArrayList<>();
            for (DataSourceResponseDTO dataSourceDTO : dataSourceDTOS) {
                DataSource dataSource = new DataSource(dataSourceDTO.getName());
                dataSource.setOrganizationId(unitId);
                dataSourceList.add(dataSource);
            }
            dataSourceMongoRepository.saveAll(getNextSequence(dataSourceList));

        }
    }

    private void saveProcessingLegalBasis(Long unitId, List<ProcessingLegalBasisResponseDTO> legalBasisDTOS) {
        if (CollectionUtils.isNotEmpty(legalBasisDTOS)) {
            List<ProcessingLegalBasis> processingLegalBasisList = new ArrayList<>();
            for (ProcessingLegalBasisResponseDTO legalBasisDTO : legalBasisDTOS) {
                ProcessingLegalBasis processingLegalBasis = new ProcessingLegalBasis(legalBasisDTO.getName());
                processingLegalBasis.setOrganizationId(unitId);
                processingLegalBasisList.add(processingLegalBasis);
            }
            processingLegalBasisMongoRepository.saveAll(getNextSequence(processingLegalBasisList));

        }
    }

    private void saveProcessingPurposes(Long unitId, List<ProcessingPurposeResponseDTO> processingPurposeDTOS) {
        if (CollectionUtils.isNotEmpty(processingPurposeDTOS)) {
            List<ProcessingPurpose> processingPurposes = new ArrayList<>();
            for (ProcessingPurposeResponseDTO processingPurposeDTO : processingPurposeDTOS) {
                ProcessingPurpose processingPurpose = new ProcessingPurpose(processingPurposeDTO.getName());
                processingPurpose.setOrganizationId(unitId);
                processingPurposes.add(processingPurpose);
            }
            processingPurposeMongoRepository.saveAll(getNextSequence(processingPurposes));
        }
    }

    private void saveResponsibilityTypes(Long unitId, List<ResponsibilityTypeResponseDTO> responsibilityTypeDTOS) {
        if (CollectionUtils.isNotEmpty(responsibilityTypeDTOS)) {
            List<ResponsibilityType> responsibilityTypes = new ArrayList<>();
            for (ResponsibilityTypeResponseDTO responsibilityTypeDTO : responsibilityTypeDTOS) {
                ResponsibilityType responsibilityType = new ResponsibilityType(responsibilityTypeDTO.getName());
                responsibilityType.setOrganizationId(unitId);
                responsibilityTypes.add(responsibilityType);
            }
            responsibilityTypeMongoRepository.saveAll(getNextSequence(responsibilityTypes));
        }
    }

    private void saveTransferMethods(Long unitId, List<TransferMethodResponseDTO> transferMethodDTOS) {
        if (CollectionUtils.isNotEmpty(transferMethodDTOS)) {
            List<TransferMethod> transferMethods = new ArrayList<>();
            for (TransferMethodResponseDTO transferMethodResponseDTO : transferMethodDTOS) {
                TransferMethod transferMethod = new TransferMethod(transferMethodResponseDTO.getName());
                transferMethod.setOrganizationId(unitId);
                transferMethods.add(transferMethod);
            }
            transferMethodMongoRepository.saveAll(getNextSequence(transferMethods));
        }
    }


    private void saveAssetTypeAndAssetSubType(Long unitId, List<AssetTypeRiskResponseDTO> assetTypeDTOS) {

        if (CollectionUtils.isNotEmpty(assetTypeDTOS)) {


            List<Risk> risks = new ArrayList<>();
            Map<AssetType, List<Risk>> assetTypeRiskMap = new HashMap<>();
            Map<AssetType, List<AssetType>> assetTypeAndSubAssetTypeMap = new HashMap<>();
            for (AssetTypeRiskResponseDTO assetTypeDTO : assetTypeDTOS) {
                AssetType assetType = new AssetType(assetTypeDTO.getName());
                assetType.setOrganizationId(unitId);
                assetTypeRiskMap.put(assetType, buildRisks(unitId, assetTypeDTO.getRisks()));
                List<AssetType> subAssetTypes = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(assetTypeDTO.getSubAssetTypes())) {
                    for (AssetTypeRiskResponseDTO subAssetTypeDTO : assetTypeDTO.getSubAssetTypes()) {
                        AssetType subAssetType = new AssetType(subAssetTypeDTO.getName());
                        subAssetType.setOrganizationId(unitId);
                        subAssetType.setSubAssetType(true);
                        assetType.setHasSubAsset(true);
                        assetTypeRiskMap.put(subAssetType, buildRisks(unitId, subAssetTypeDTO.getRisks()));
                        subAssetTypes.add(subAssetType);
                    }
                }
                assetTypeAndSubAssetTypeMap.put(assetType, subAssetTypes);
            }
            assetTypeRiskMap.forEach((assetType, riskList) -> risks.addAll(riskList));
            if (CollectionUtils.isNotEmpty(risks)) {
                riskMongoRepository.saveAll(getNextSequence(risks));
            }
            List<AssetType> assetSubTypes = new ArrayList<>();
            assetTypeAndSubAssetTypeMap.forEach((assetType, subAssetTypes) -> {
                assetType.setRisks(assetTypeRiskMap.get(assetType).stream().map(Risk::getId).collect(Collectors.toSet()));
                if (CollectionUtils.isNotEmpty(subAssetTypes)) {
                    assetSubTypes.addAll(subAssetTypes);
                    subAssetTypes.forEach(subAssetType -> subAssetType.setRisks(assetTypeRiskMap.get(subAssetType).stream().map(Risk::getId).collect(Collectors.toSet())));
                }
            });
            if (CollectionUtils.isNotEmpty(assetSubTypes)) {
                assetTypeMongoRepository.saveAll(getNextSequence(assetSubTypes));
                assetSubTypes.forEach(subAssetType -> globalAssetTypeAndSubAssetTypeMap.put(subAssetType.getName().toLowerCase(), subAssetType.getId()));
            }
            List<AssetType> assetTypes = new ArrayList<>(assetTypeAndSubAssetTypeMap.keySet());
            assetTypes.forEach(assetType -> assetType.setSubAssetTypes(assetTypeAndSubAssetTypeMap.get(assetType).stream().map(AssetType::getId).collect(Collectors.toSet())));
            assetTypeMongoRepository.saveAll(getNextSequence(assetTypes)).forEach(assetType -> globalAssetTypeAndSubAssetTypeMap.put(assetType.getName().toLowerCase(), assetType.getId()));
        }
    }


    private List<Risk> buildRisks(Long unitId, List<RiskBasicResponseDTO> riskDTOS) {

        List<Risk> risks = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(riskDTOS)) {
            riskDTOS.forEach(riskDTO -> {
                Risk risk = new Risk(riskDTO.getName(), riskDTO.getDescription(), riskDTO.getRiskRecommendation(), riskDTO.getRiskLevel());
                risk.setOrganizationId(unitId);
                risks.add(risk);
            });
        }
        return risks;

    }


}


