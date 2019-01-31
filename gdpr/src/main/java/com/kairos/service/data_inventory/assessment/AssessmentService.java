package com.kairos.service.data_inventory.assessment;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.commons.client.RestTemplateResponseEnvelope;
import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.gdpr.ManagingOrganization;
import com.kairos.dto.gdpr.Staff;
import com.kairos.enums.DurationType;
import com.kairos.enums.IntegrationOperation;
import com.kairos.enums.gdpr.*;
import com.kairos.dto.gdpr.assessment.AssessmentDTO;
import com.kairos.persistence.model.data_inventory.assessment.*;
import com.kairos.persistence.model.data_inventory.asset.Asset;
import com.kairos.persistence.model.data_inventory.asset.AssetMD;
import com.kairos.persistence.model.data_inventory.processing_activity.ProcessingActivity;
import com.kairos.persistence.model.data_inventory.processing_activity.ProcessingActivityMD;
import com.kairos.persistence.model.questionnaire_template.QuestionnaireTemplate;
import com.kairos.persistence.model.questionnaire_template.QuestionnaireTemplateMD;
import com.kairos.persistence.model.risk_management.RiskMD;
import com.kairos.persistence.repository.data_inventory.Assessment.AssessmentMongoRepository;
import com.kairos.persistence.repository.data_inventory.Assessment.AssessmentRepository;
import com.kairos.persistence.repository.data_inventory.asset.AssetMongoRepository;
import com.kairos.persistence.repository.data_inventory.asset.AssetRepository;
import com.kairos.persistence.repository.data_inventory.processing_activity.ProcessingActivityMongoRepository;
import com.kairos.persistence.repository.data_inventory.processing_activity.ProcessingActivityRepository;
import com.kairos.persistence.repository.master_data.asset_management.AssetTypeMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.data_disposal.DataDisposalMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.hosting_provider.HostingProviderMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.hosting_type.HostingTypeMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.org_security_measure.OrganizationalSecurityMeasureMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.storage_format.StorageFormatMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.tech_security_measure.TechnicalSecurityMeasureMongoRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.accessor_party.AccessorPartyMongoRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.data_source.DataSourceMongoRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.legal_basis.ProcessingLegalBasisMongoRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.processing_purpose.ProcessingPurposeMongoRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.responsibility_type.ResponsibilityTypeMongoRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.transfer_method.TransferMethodMongoRepository;
import com.kairos.persistence.repository.questionnaire_template.QuestionnaireTemplateMongoRepository;
import com.kairos.persistence.repository.questionnaire_template.QuestionnaireTemplateRepository;
import com.kairos.response.dto.common.AssessmentBasicResponseDTO;
import com.kairos.response.dto.common.AssessmentResponseDTO;
import com.kairos.response.dto.data_inventory.AssetResponseDTO;
import com.kairos.response.dto.data_inventory.ProcessingActivityResponseDTO;
import com.kairos.response.dto.master_data.questionnaire_template.QuestionBasicResponseDTO;
import com.kairos.response.dto.master_data.questionnaire_template.QuestionnaireSectionResponseDTO;
import com.kairos.response.dto.master_data.questionnaire_template.QuestionnaireTemplateResponseDTO;
import com.kairos.rest_client.GenericRestClient;
import com.kairos.service.common.MongoBaseService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.utils.user_context.UserContext;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;

@Service
public class AssessmentService extends MongoBaseService {


    @Inject
    private AssessmentMongoRepository assessmentMongoRepository;

    @Inject
    private ProcessingActivityRepository processingActivityRepository;

    @Inject
    private QuestionnaireTemplateRepository questionnaireTemplateRepository;

    @Inject
    private AssetMongoRepository assetMongoRepository;

    @Inject
    private AssetRepository assetRepository;

    @Inject
    private ProcessingActivityMongoRepository processingActivityMongoRepository;

    @Inject
    private ExceptionService exceptionService;

    @Inject
    private QuestionnaireTemplateMongoRepository questionnaireTemplateMongoRepository;

    @Inject
    private AssetTypeMongoRepository assetTypeMongoRepository;

    @Inject
    private StorageFormatMongoRepository storageFormatMongoRepository;

    @Inject
    private OrganizationalSecurityMeasureMongoRepository organizationalSecurityMeasureRepository;

    @Inject
    private TechnicalSecurityMeasureMongoRepository technicalSecurityMeasureMongoRepository;

    @Inject
    private HostingProviderMongoRepository hostingProviderMongoRepository;

    @Inject
    private HostingTypeMongoRepository hostingTypeMongoRepository;

    @Inject
    private DataDisposalMongoRepository dataDisposalMongoRepository;

    @Inject
    private ProcessingPurposeMongoRepository processingPurposeMongoRepository;

    @Inject
    private DataSourceMongoRepository dataSourceMongoRepository;

    @Inject
    private TransferMethodMongoRepository transferMethodMongoRepository;

    @Inject
    private AccessorPartyMongoRepository accessorPartyMongoRepository;

    @Inject
    private ProcessingLegalBasisMongoRepository processingLegalBasisMongoRepository;

    @Inject
    private ResponsibilityTypeMongoRepository responsibilityTypeMongoRepository;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private GenericRestClient genericRestClient;

    @Inject
    private AssessmentRepository assessmentRepository;

    private static List<AssessmentStatus> assessmentStatusList = Arrays.asList(AssessmentStatus.NEW, AssessmentStatus.IN_PROGRESS);


    /**
     * @param unitId        organization id
     * @param assetId       asset id for which assessment is related
     * @param assessmentDTO Assessment Dto contain detail about who assign assessment and to whom assessment is assigned
     * @return
     */
    public AssessmentDTO launchAssessmentForAsset(Long unitId, Long assetId, AssessmentDTO assessmentDTO) {
        if(!Optional.ofNullable(assessmentDTO.getRelativeDeadlineDuration()).isPresent() || !Optional.ofNullable(assessmentDTO.getRelativeDeadlineType()).isPresent()){
            exceptionService.illegalArgumentException("message.assessment.relativedeadline.require");
        }
        AssessmentMD previousAssessment = assessmentDTO.isRiskAssessment() ? assessmentRepository.findPreviousLaunchedAssessmentByUnitIdAndAssetId(unitId, assetId, assessmentStatusList, true) : assessmentRepository.findPreviousLaunchedAssessmentByUnitIdAndAssetId(unitId, assetId, assessmentStatusList, false);
        if (Optional.ofNullable(previousAssessment).isPresent()) {
            exceptionService.duplicateDataException("message.assessment.cannotbe.launched.asset", previousAssessment.getName(), previousAssessment.getAssessmentStatus());
        }
        AssetMD asset = assetRepository.findByIdAndOrganizationIdAndDeleted(assetId,unitId, false);
        if (!Optional.ofNullable(asset).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.asset", assetId);
        }
        validateLaunchAssessmentValue(assessmentDTO);
        assessmentDTO.setRiskAssociatedEntity(QuestionnaireTemplateType.ASSET_TYPE);
        AssessmentMD assessment = assessmentDTO.isRiskAssessment() ? buildAssessmentWithBasicDetail(unitId, assessmentDTO, QuestionnaireTemplateType.RISK, asset) : buildAssessmentWithBasicDetail(unitId, assessmentDTO, QuestionnaireTemplateType.ASSET_TYPE, asset);
        assessment.setAsset(asset);
        if (!assessmentDTO.isRiskAssessment()) {
            //saveAssetValueToAssessment(unitId, assessment, assetResponseDTO);
        } else {
            //saveRiskTemplateAnswerToAssessment(unitId, assessment);
        }
        assessmentRepository.save(assessment);
        assessmentDTO.setId(assessment.getId());
        return assessmentDTO;
    }

    private boolean validateLaunchAssessmentValue(AssessmentDTO assessmentDTO){
            boolean result=true;
            if(assessmentDTO.getRelativeDeadlineType().equals(DurationType.DAYS)&&!(assessmentDTO.getRelativeDeadlineDuration()<=30)){
             result=false;
            }else if(assessmentDTO.getRelativeDeadlineType().equals(DurationType.HOURS)&&!(assessmentDTO.getRelativeDeadlineDuration()<=24)){
                result=false;
            }else if(assessmentDTO.getRelativeDeadlineType().equals(DurationType.MONTHS)&&!(assessmentDTO.getRelativeDeadlineDuration()<=12)){
                result=false;
            }else {
                LocalDate endDate = DateUtils.addDurationInLocalDate(assessmentDTO.getStartDate(), assessmentDTO.getRelativeDeadlineDuration(), assessmentDTO.getRelativeDeadlineType(), 1);
                if(endDate.isAfter(assessmentDTO.getEndDate())){
                    result=false;
                }
            }
            if(!result){
                exceptionService.illegalArgumentException("message.assessment.relativedeadline.value.invalid");
            }
            return result;
    }

    /**
     * @param unitId
     * @param processingActivityId Processing activity id for which assessment is related
     * @param assessmentDTO        Assessment Dto contain detail about who assign assessment and to whom assessment is assigned
     * @return
     */
    public AssessmentDTO launchAssessmentForProcessingActivity(Long unitId, Long processingActivityId, AssessmentDTO assessmentDTO,boolean subProcessingActivity) {

        AssessmentMD previousAssessment = assessmentDTO.isRiskAssessment() ? assessmentRepository.findPreviousLaunchedRiskAssessmentByUnitIdAndProcessingActivityId(unitId, processingActivityId,assessmentStatusList, true) : assessmentRepository.findPreviousLaunchedRiskAssessmentByUnitIdAndProcessingActivityId(unitId, processingActivityId,assessmentStatusList, false);
        if (Optional.ofNullable(previousAssessment).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.assessment.cannotbe.launched.processing.activity", previousAssessment.getName(), previousAssessment.getAssessmentStatus());
        }
        assessmentDTO.setRiskAssociatedEntity(QuestionnaireTemplateType.PROCESSING_ACTIVITY);
        ProcessingActivityMD processingActivity = processingActivityRepository.findByIdAndOrganizationIdAndDeletedAndIsSubProcessingActivity(processingActivityId,unitId, false);
        try {
            AssessmentMD assessment = assessmentDTO.isRiskAssessment() ? buildAssessmentWithBasicDetail(unitId, assessmentDTO, QuestionnaireTemplateType.RISK, processingActivity) : buildAssessmentWithBasicDetail(unitId, assessmentDTO, QuestionnaireTemplateType.PROCESSING_ACTIVITY, processingActivity);
            assessment.setProcessingActivity(processingActivity);
            if (!assessmentDTO.isRiskAssessment()) {
               // saveProcessingActivityValueToAssessment(unitId, assessment, processingActivityDTO);
            } else {
               // saveRiskTemplateAnswerToAssessment(unitId, assessment);
            }
            assessmentRepository.save(assessment);
            assessmentDTO.setId(assessment.getId());
        }catch (EntityNotFoundException ene){
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.processingActivity", processingActivityId);
        }
        return assessmentDTO;
    }


    /**
     * @param unitId
     * @param assessmentDTO
     * @return
     */
    private AssessmentMD buildAssessmentWithBasicDetail(Long unitId, AssessmentDTO assessmentDTO, QuestionnaireTemplateType templateType, Object entity) {

        AssessmentMD previousAssessment = assessmentRepository.findByOrganizationIdAndDeletedAndName(unitId, false, assessmentDTO.getName());
        if (Optional.ofNullable(previousAssessment).isPresent()) {
            exceptionService.duplicateDataException("message.duplicate", "Assessment", assessmentDTO.getName());
        }
        if (assessmentDTO.getStartDate().isBefore(LocalDate.now())) {
            exceptionService.invalidRequestException("message.assessment.enter.valid.startdate");
        }else if(assessmentDTO.getEndDate().isBefore(LocalDate.now()) || assessmentDTO.getEndDate().isBefore(assessmentDTO.getStartDate())){
            exceptionService.invalidRequestException("message.assessment.enter.valid.enddate");
        }
        AssessmentMD assessment = new AssessmentMD(assessmentDTO.getName(), assessmentDTO.getEndDate(),  assessmentDTO.getComment(),assessmentDTO.getStartDate());
        assessment.setApprover(ObjectMapperUtils.copyPropertiesByMapper(assessmentDTO.getApprover(), com.kairos.persistence.model.embeddables.Staff.class));
        assessment.setAssigneeList(ObjectMapperUtils.copyPropertiesOfListByMapper(assessmentDTO.getAssigneeList(), com.kairos.persistence.model.embeddables.Staff.class));
        assessment.setOrganizationId(unitId);
        QuestionnaireTemplateMD questionnaireTemplate;
        switch (templateType) {
            case ASSET_TYPE:
                AssetMD asset = (AssetMD)entity;
                questionnaireTemplate = checkPreviousLaunchedAssetAssessment(unitId,  asset);
                break;
            case RISK:
                questionnaireTemplate = checkPreviousLaunchedRiskAssessment(unitId, assessmentDTO, assessment, entity);
                assessment.setRiskAssessment(true);
                break;
            case PROCESSING_ACTIVITY:
                questionnaireTemplate = questionnaireTemplateRepository.findPublishedQuestionnaireTemplateByProcessingActivityAndByUnitId(unitId, QuestionnaireTemplateType.PROCESSING_ACTIVITY,QuestionnaireTemplateType.PROCESSING_ACTIVITY,QuestionnaireTemplateStatus.PUBLISHED);
                break;
            default:
                questionnaireTemplate = questionnaireTemplateRepository.getQuestionnaireTemplateByTemplateTypeAndUnitId(templateType, unitId, QuestionnaireTemplateStatus.PUBLISHED);
                break;

        }
        if (!Optional.ofNullable(questionnaireTemplate).isPresent()) {
            exceptionService.invalidRequestException("message.questionnaire.template.Not.Found.For.Template.Type", templateType);
        } else if (QuestionnaireTemplateStatus.DRAFT.equals(questionnaireTemplate.getTemplateStatus())) {
            exceptionService.invalidRequestException("message.assessment.cannotbe.launched.questionnaireTemplate.notPublished");
        }
        /*if (AssessmentSchedulingFrequency.CUSTOM_DATE.equals(assessmentDTO.getAssessmentSchedulingFrequency())) {
            if (!Optional.ofNullable(assessmentDTO.getAssessmentLaunchedDate()).isPresent()) {
                exceptionService.invalidRequestException("message.assessment.scheduling.date.not.Selected");
            } else if (LocalDate.now().equals(assessmentDTO.getAssessmentLaunchedDate()) || assessmentDTO.getAssessmentLaunchedDate().isBefore(LocalDate.now())) {
                exceptionService.invalidRequestException("message.assessment.enter.valid.date");

            assessment.setAssessmentLaunchedDate(assessmentDTO.getAssessmentLaunchedDate());
        }}*/
        assessment.setAssessmentLaunchedDate(LocalDate.now());
        assessment.setAssessmentSchedulingFrequency(assessmentDTO.getAssessmentSchedulingFrequency());
        assessment.setQuestionnaireTemplate(questionnaireTemplate);
        return assessment;

    }


    private QuestionnaireTemplateMD checkPreviousLaunchedAssetAssessment(Long unitId, AssetMD asset) {
        //TODO commented due to id type change from Biginteger to Long
        QuestionnaireTemplateMD questionnaireTemplate = null;
        if (asset.getSubAssetType() != null) {
            questionnaireTemplate = questionnaireTemplateRepository.findPublishedQuestionnaireTemplateByUnitIdAndAssetTypeIdAndSubAssetTypeId(unitId, asset.getAssetType().getId(), asset.getSubAssetType().getId(),QuestionnaireTemplateType.ASSET_TYPE,QuestionnaireTemplateStatus.PUBLISHED);
        } else {
            questionnaireTemplate = questionnaireTemplateRepository.findPublishedQuestionnaireTemplateByAssetTypeAndByUnitId(unitId, asset.getAssetType().getId(),QuestionnaireTemplateType.ASSET_TYPE,QuestionnaireTemplateStatus.PUBLISHED);
        }
        if (!Optional.ofNullable(questionnaireTemplate).isPresent()) {
            questionnaireTemplate = questionnaireTemplateRepository.findDefaultAssetQuestionnaireTemplateByUnitId(unitId,QuestionnaireTemplateType.ASSET_TYPE,QuestionnaireTemplateStatus.PUBLISHED);
        }

        return questionnaireTemplate;
    }


    /**
     * @param unitId
     * @param assessmentDTO
     * @param entity
     * @return
     */
    private QuestionnaireTemplateMD checkPreviousLaunchedRiskAssessment(Long unitId, AssessmentDTO assessmentDTO, AssessmentMD assessment, Object entity) {

        List<RiskMD> risks = new ArrayList<>();
        QuestionnaireTemplateMD questionnaireTemplate = null;
        if (QuestionnaireTemplateType.ASSET_TYPE.equals(assessmentDTO.getRiskAssociatedEntity())) {
            AssetMD asset = (AssetMD)entity;
            risks = asset.getAssetType().getRisks();
            risks.addAll(asset.getSubAssetType().getRisks());
            if (CollectionUtils.isEmpty(risks)) {
                exceptionService.invalidRequestException("message.assessment.cannotbe.launched.risk.not.present");
            }
            if (asset.getSubAssetType() != null)
                questionnaireTemplate = questionnaireTemplateRepository.findPublishedRiskTemplateByUnitIdAndAssetTypeAndSubAssetTypeAndTemplateType(unitId, asset.getAssetType().getId(), asset.getSubAssetType().getId(), QuestionnaireTemplateType.RISK,QuestionnaireTemplateStatus.PUBLISHED,QuestionnaireTemplateType.ASSET_TYPE);
            else
                questionnaireTemplate = questionnaireTemplateRepository.findPublishedRiskTemplateByUnitIdAndAssetTypeAndTemplateType(unitId, asset.getAssetType().getId(),QuestionnaireTemplateType.RISK,QuestionnaireTemplateType.ASSET_TYPE,QuestionnaireTemplateStatus.PUBLISHED);
        } else if (QuestionnaireTemplateType.PROCESSING_ACTIVITY.equals(assessmentDTO.getRiskAssociatedEntity())) {
            ProcessingActivityMD processingActivity = (ProcessingActivityMD) entity;
            risks.addAll(processingActivity.getRisks());
            if (CollectionUtils.isEmpty(risks)) {
                exceptionService.invalidRequestException("message.assessment.cannotbe.launched.risk.not.present");
            }
            //TODO changed due to id changes from Biginteger to Integer
            //riskIds.addAll(((ProcessingActivityResponseDTO) entity).getRisks().stream().map(RiskBasicResponseDTO::getId).collect(Collectors.toSet()));
            questionnaireTemplate = questionnaireTemplateRepository.findPublishedRiskTemplateByAssociatedProcessingActivityAndUnitIdAndTemplateTypeStatus(unitId, QuestionnaireTemplateType.RISK, QuestionnaireTemplateType.PROCESSING_ACTIVITY, QuestionnaireTemplateStatus.PUBLISHED);
        }
        assessment.setRisks(risks);
        return questionnaireTemplate;
    }


    /**
     * @param unitId
     * @param assessmentId
     * @return
     */
    public List<QuestionnaireSectionResponseDTO> getAssessmentById(Long unitId, BigInteger assessmentId) {

        Assessment assessment = assessmentMongoRepository.findByUnitIdAndId(unitId, assessmentId);
        if (!Optional.ofNullable(assessment).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "Assessment", assessmentId);
        }
        QuestionnaireTemplateResponseDTO assessmentQuestionnaireTemplate = questionnaireTemplateMongoRepository.getQuestionnaireTemplateWithSectionsByUnitId(unitId, assessment.getQuestionnaireTemplateId());
        List<QuestionnaireSectionResponseDTO> assessmentQuestionnaireSections = assessmentQuestionnaireTemplate.getSections();
        if (assessment.isRiskAssessment()) {
            getRiskAssessmentAnswer(assessment, assessmentQuestionnaireSections);
        } else {
            if (Optional.ofNullable(assessment.getAssetId()).isPresent())
                getAssetAssessmentQuestionAndAnswer(unitId, assessment, assessmentQuestionnaireSections);
            else
                getProcessingActivityAssessmentQuestionAndAnswer(unitId, assessment, assessmentQuestionnaireSections);
        }
        return assessmentQuestionnaireSections;
    }


    private void getRiskAssessmentAnswer(Assessment assessment, List<QuestionnaireSectionResponseDTO> assessmentQuestionnaireSections) {

        List<AssessmentAnswerValueObject> assessmentAnswers = assessment.getAssessmentAnswers();
        Map<BigInteger, Object> riskAssessmentAnswer = new HashMap<>();
        assessmentAnswers.forEach(assessmentAnswer -> riskAssessmentAnswer.put(assessmentAnswer.getQuestionId(), assessmentAnswer.getValue()));
        for (QuestionnaireSectionResponseDTO questionnaireSectionResponseDTO : assessmentQuestionnaireSections) {
            for (QuestionBasicResponseDTO question : questionnaireSectionResponseDTO.getQuestions()) {
                question.setValue(riskAssessmentAnswer.get(question.getId()));
            }
        }

    }


    private void getAssetAssessmentQuestionAndAnswer(Long unitId, Assessment assessment, List<QuestionnaireSectionResponseDTO> assessmentQuestionnaireSections) {

        List<AssessmentAnswerValueObject> assetAssessmentAnswers = assessment.getAssessmentAnswers();
        Map<AssetAttributeName, Object> assetAttributeNameObjectMap = new HashMap<>();
        assetAssessmentAnswers.forEach(assetAssessmentAnswer -> assetAttributeNameObjectMap.put(AssetAttributeName.valueOf(assetAssessmentAnswer.getAttributeName()), assetAssessmentAnswer.getValue()));
        for (QuestionnaireSectionResponseDTO questionnaireSectionResponseDTO : assessmentQuestionnaireSections) {
            for (QuestionBasicResponseDTO question : questionnaireSectionResponseDTO.getQuestions()) {
                if (assetAttributeNameObjectMap.containsKey(AssetAttributeName.valueOf(question.getAttributeName()))) {
                    if (QuestionType.MULTIPLE_CHOICE.equals(question.getQuestionType()) && !Optional.ofNullable(assetAttributeNameObjectMap.get(AssetAttributeName.valueOf(question.getAttributeName()))).isPresent()) {
                        question.setValue(new ArrayList<>());
                    } else {
                        question.setValue(assetAttributeNameObjectMap.get(AssetAttributeName.valueOf(question.getAttributeName())));
                    }
                    question.setAssessmentAnswerChoices(addAssessmentAnswerOptionsForAsset(unitId, AssetAttributeName.valueOf(question.getAttributeName())));
                }
            }
        }


    }


    private void getProcessingActivityAssessmentQuestionAndAnswer(Long unitId, Assessment assessment, List<QuestionnaireSectionResponseDTO> assessmentQuestionnaireSections) {

        List<AssessmentAnswerValueObject> processingActivityAssessmentAnswers = assessment.getAssessmentAnswers();
        Map<ProcessingActivityAttributeName, Object> processingActivityAttributeNameObjectMap = new HashMap<>();
        processingActivityAssessmentAnswers.forEach(processingActivityAssessmentAnswer -> processingActivityAttributeNameObjectMap.put(ProcessingActivityAttributeName.valueOf(processingActivityAssessmentAnswer.getAttributeName()), processingActivityAssessmentAnswer.getValue()));
        for (QuestionnaireSectionResponseDTO questionnaireSectionResponseDTO : assessmentQuestionnaireSections) {
            for (QuestionBasicResponseDTO question : questionnaireSectionResponseDTO.getQuestions()) {
                if (processingActivityAttributeNameObjectMap.containsKey(ProcessingActivityAttributeName.valueOf(question.getAttributeName()))) {
                    if (QuestionType.MULTIPLE_CHOICE.equals(question.getQuestionType()) && !Optional.ofNullable(processingActivityAttributeNameObjectMap.get(ProcessingActivityAttributeName.valueOf(question.getAttributeName()))).isPresent()) {
                        question.setValue(new ArrayList<>());
                    } else {
                        question.setValue(processingActivityAttributeNameObjectMap.get(ProcessingActivityAttributeName.valueOf(question.getAttributeName())));
                    }
                    question.setAssessmentAnswerChoices(addAssessmentAnswerOptionsForProcessingActivity(unitId, ProcessingActivityAttributeName.valueOf(question.getAttributeName())));
                }
            }
        }

    }


    private Object addAssessmentAnswerOptionsForAsset(Long unitId, AssetAttributeName assetAttributeName) {


        switch (assetAttributeName) {
            case HOSTING_PROVIDER:
                return hostingProviderMongoRepository.findAllByUnitId(unitId);
            case HOSTING_TYPE:
                return hostingTypeMongoRepository.findAllByUnitId(unitId);
            case ASSET_TYPE:
                return assetTypeMongoRepository.getAllAssetTypeWithSubAssetTypeByUnitId(unitId);
            case STORAGE_FORMAT:
                return storageFormatMongoRepository.findAllByUnitId(unitId);
            case DATA_DISPOSAL:
                return dataDisposalMongoRepository.findAllByUnitId(unitId);
            case TECHNICAL_SECURITY_MEASURES:
                return technicalSecurityMeasureMongoRepository.findAllByUnitId(unitId);
            case ORGANIZATION_SECURITY_MEASURES:
                return organizationalSecurityMeasureRepository.findAllByUnitId(unitId);
            default:
                return null;
        }


    }


    private Object addAssessmentAnswerOptionsForProcessingActivity(Long unitId, ProcessingActivityAttributeName processingActivityAttributeName) {


        switch (processingActivityAttributeName) {
            case RESPONSIBILITY_TYPE:
                return responsibilityTypeMongoRepository.findAllByUnitId(unitId);
            case PROCESSING_PURPOSES:
                return processingPurposeMongoRepository.findAllByUnitId(unitId);
            case DATA_SOURCES:
                return dataSourceMongoRepository.findAllByUnitId(unitId);
            case TRANSFER_METHOD:
                return transferMethodMongoRepository.findAllByUnitId(unitId);
            case ACCESSOR_PARTY:
                return accessorPartyMongoRepository.findAllByUnitId(unitId);
            case PROCESSING_LEGAL_BASIS:
                return processingLegalBasisMongoRepository.findAllByUnitId(unitId);
            default:
                return null;
        }


    }


    /**
     * @param unitId
     * @param assessmentId
     * @param assessmentStatus
     * @return
     */
    public boolean updateAssessmentStatus(Long unitId, Long assessmentId, AssessmentStatus assessmentStatus) {
        AssessmentMD assessment = assessmentRepository.findByOrganizationIdAndDeletedAndId( assessmentId,false, unitId);
        UserVO currentUser = new UserVO();
        ObjectMapperUtils.copyProperties(UserContext.getUserDetails(), currentUser);
        switch (assessmentStatus) {
            case IN_PROGRESS:
                if (assessment.getAssessmentStatus().equals(AssessmentStatus.COMPLETED)) {
                    exceptionService.invalidRequestException("message.assessment.invalid.status", assessment.getAssessmentStatus(), assessmentStatus);
                }
                break;
            case COMPLETED:

                if (assessment.getAssessmentStatus().equals(AssessmentStatus.NEW)) {
                    exceptionService.invalidRequestException("message.assessment.invalid.status", assessment.getAssessmentStatus(), assessmentStatus);
                } else if (!currentUser.equals(assessment.getAssessmentLastAssistBy())) {
                    exceptionService.invalidRequestException("message.notAuthorized.toChange.assessment.status");
                }
                //saveAssessmentAnswerOnCompletionToAssetOrProcessingActivity(unitId, assessment);
                break;
            case NEW:
                if (assessment.getAssessmentStatus().equals(AssessmentStatus.IN_PROGRESS) || assessment.getAssessmentStatus().equals(AssessmentStatus.COMPLETED)) {
                    exceptionService.invalidRequestException("message.assessment.invalid.status", assessment.getAssessmentStatus(), assessmentStatus);
                }
                break;
        }
        assessment.setAssessmentLastAssistBy(currentUser);
        assessment.setAssessmentStatus(assessmentStatus);
        assessmentRepository.save(assessment);
        return true;
    }


    private void saveAssessmentAnswerOnCompletionToAssetOrProcessingActivity(Long unitId, AssessmentMD assessment) {

        if (!assessment.isRiskAssessment() && Optional.ofNullable(assessment.getAsset()).isPresent()) {
            AssetMD asset = assetRepository.findByIdAndOrganizationIdAndDeleted(unitId, assessment.getAsset().getId(),false);
            List<AssessmentAnswer> assessmentAnswersForAsset = assessment.getAssessmentAnswers();
            assessmentAnswersForAsset.forEach(assetAssessmentAnswer -> {
                if (Optional.ofNullable(assetAssessmentAnswer.getAttributeName()).isPresent()) {
                    saveAssessmentAnswerForAssetOnCompletionOfAssessment(AssetAttributeName.valueOf(assetAssessmentAnswer.getAttributeName()), assetAssessmentAnswer.getValue(), asset);
                } else {

                    exceptionService.invalidRequestException("message.assessment.answer.attribute.null");
                }

            });
            assetRepository.save(asset);

        } else if (!assessment.isRiskAssessment() && Optional.ofNullable(assessment.getProcessingActivity()).isPresent()) {
            ProcessingActivityMD processingActivity = processingActivityRepository.findByIdAndOrganizationIdAndDeleted( assessment.getProcessingActivity().getId(),unitId,false);
            List<AssessmentAnswer> assessmentAnswersForProcessingActivity = assessment.getAssessmentAnswers();
            assessmentAnswersForProcessingActivity.forEach(processingActivityAssessmentAnswer
                    -> {
                if (Optional.ofNullable(processingActivityAssessmentAnswer.getAttributeName()).isPresent()) {
                   // saveAssessmentAnswerForProcessingActivityOnCompletionOfAssessment(ProcessingActivityAttributeName.valueOf(processingActivityAssessmentAnswer.getAttributeName()), processingActivityAssessmentAnswer.getValue(), processingActivity);

                } else {
                    exceptionService.invalidRequestException("message.assessment.answer.attribute.null");

                }
            });
            processingActivityRepository.save(processingActivity);

        }
    }


    /**
     * @param unitId
     * @return
     */
    public List<AssessmentBasicResponseDTO> getAllLaunchedAssessmentOfCurrentLoginUser(Long unitId) {

        Long staffId = genericRestClient.publishRequest(null, unitId, true, IntegrationOperation.GET, "/user/staffId", null, new ParameterizedTypeReference<RestTemplateResponseEnvelope<Long>>() {
        });
        List<AssessmentMD> assessments = assessmentRepository.getAllAssessmentByUnitIdAndStaffId(unitId, staffId, assessmentStatusList);
        return ObjectMapperUtils.copyPropertiesOfListByMapper(assessments,AssessmentBasicResponseDTO.class );
    }


    public List<AssessmentResponseDTO> getAllAssessmentByUnitId(Long unitId) {
        List<AssessmentMD> assessments = assessmentRepository.getAllAssessmentByUnitId(unitId);
        return ObjectMapperUtils.copyPropertiesOfListByMapper(assessments, AssessmentResponseDTO.class);
    }

    public AssessmentSchedulingFrequency[] getSchedulingFrequency() {
        return AssessmentSchedulingFrequency.values();
    }

    public boolean deleteAssessmentById(Long unitId, Long assessmentId) {

        AssessmentMD assessment = assessmentRepository.findByUnitIdAndIdAndAssessmentStatus(unitId, assessmentId, AssessmentStatus.IN_PROGRESS);
        if (Optional.ofNullable(assessment).isPresent()) {
            exceptionService.invalidRequestException("message.assessment.inprogress.cannot.delete", assessment.getName());
        }
        assessment.delete();
        assessmentRepository.save(assessment);
        return true;
    }

    /**
     * @param unitId
     * @param assessmentId
     * @return
     */
    public List<AssessmentAnswerValueObject> addAssessmentAnswerForAssetOrProcessingActivity(Long unitId, BigInteger assessmentId, List<AssessmentAnswerValueObject> assessmentAnswerValueObjects, AssessmentStatus status) {

        Assessment assessment = assessmentMongoRepository.findByUnitIdAndId(unitId, assessmentId);
        UserVO currentUser = new UserVO();
        ObjectMapperUtils.copyProperties(UserContext.getUserDetails(), currentUser);
        if (!Optional.ofNullable(assessment).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "Assessment", assessmentId);
        } else if (assessment.getAssessmentStatus().equals(AssessmentStatus.NEW)) {
            exceptionService.invalidRequestException("message.assessment.change.status", AssessmentStatus.IN_PROGRESS);
        } else if (assessment.getAssessmentStatus().equals(AssessmentStatus.COMPLETED)) {
            exceptionService.invalidRequestException("message.assessment.completed.cannot.fill.answer");
        }
        assessment.setAssessmentAnswers(assessmentAnswerValueObjects);
        if (Optional.ofNullable(status).isPresent() && AssessmentStatus.COMPLETED.equals(status)) {
            if (!currentUser.equals(assessment.getAssessmentLastAssistBy())) {
                exceptionService.invalidRequestException("message.notAuthorized.toChange.assessment.status");
            }
            assessment.setAssessmentStatus(status);
            assessment.setCompletedDate(LocalDate.now());
            //saveAssessmentAnswerOnCompletionToAssetOrProcessingActivity(unitId, assessment);
        }
        assessmentMongoRepository.save(assessment);
        return assessmentAnswerValueObjects;

    }


    /**
     * @param assetAttributeName  asset field
     * @param assetAttributeValue asset value corresponding to field
     * @param asset               asset to which value Assessment answer were filed by assignee
     */
    public void saveAssessmentAnswerForAssetOnCompletionOfAssessment(AssetAttributeName assetAttributeName, Object assetAttributeValue, AssetMD asset) {
        switch (assetAttributeName) {
            case NAME:
                asset.setName((String) assetAttributeValue);
                break;
            case DESCRIPTION:
                asset.setDescription((String) assetAttributeValue);
                break;
            case HOSTING_LOCATION:
                asset.setHostingLocation((String) assetAttributeValue);
                break;
           /* case HOSTING_TYPE:
                asset.setHostingType(castObjectIntoLinkedHashMapAndReturnIdList(assetAttributeValue).get(0));
                break;
            case DATA_DISPOSAL:
                asset.setDataDisposal(castObjectIntoLinkedHashMapAndReturnIdList(assetAttributeValue).get(0));
                break;
            case HOSTING_PROVIDER:
                asset.setHostingProvider(castObjectIntoLinkedHashMapAndReturnIdList(assetAttributeValue).get(0));
                break;
            case ASSET_TYPE:
                asset.setAssetType(castObjectIntoLinkedHashMapAndReturnIdList(assetAttributeValue).get(0));
                break;
            case STORAGE_FORMAT:
                asset.setStorageFormats(castObjectIntoLinkedHashMapAndReturnIdList(assetAttributeValue));
                break;
            case ASSET_SUB_TYPE:
                asset.setSubAssetType(castObjectIntoLinkedHashMapAndReturnIdList(assetAttributeValue).get(0));
                break;
            case TECHNICAL_SECURITY_MEASURES:
                asset.setTechnicalSecurityMeasures(castObjectIntoLinkedHashMapAndReturnIdList(assetAttributeValue));
                break;
            case ORGANIZATION_SECURITY_MEASURES:
                asset.setOrgSecurityMeasures(castObjectIntoLinkedHashMapAndReturnIdList(assetAttributeValue));
                break;
            case MANAGING_DEPARTMENT:
                asset.setManagingDepartment(objectMapper.convertValue(assetAttributeValue, ManagingOrganization.class));
                break;
            case ASSET_OWNER:
                asset.setAssetOwner(objectMapper.convertValue(assetAttributeValue, Staff.class));
                asset.setManagingDepartment(objectMapper.convertValue(assetAttributeValue,ManagingOrganization.class));
                break;*/
            case DATA_RETENTION_PERIOD:
                asset.setDataRetentionPeriod((Integer) assetAttributeValue);
                break;

        }
    }


    /**
     * @param processingActivityAttributeName  processing activity field
     * @param processingActivityAttributeValue processing activity  value corresponding to field
     * @param processingActivity               processing activity to which value Assessment answer were filed by assignee
     */
    public void saveAssessmentAnswerForProcessingActivityOnCompletionOfAssessment(ProcessingActivityAttributeName processingActivityAttributeName, Object processingActivityAttributeValue, ProcessingActivity processingActivity) {
        switch (processingActivityAttributeName) {
            case NAME:
                processingActivity.setName((String) processingActivityAttributeValue);
                break;
            case DESCRIPTION:
                processingActivity.setDescription((String) processingActivityAttributeValue);
                break;
            case RESPONSIBILITY_TYPE:
                processingActivity.setResponsibilityType(castObjectIntoLinkedHashMapAndReturnIdList(processingActivityAttributeValue).get(0));
                break;
            case ACCESSOR_PARTY:
                processingActivity.setAccessorParties(castObjectIntoLinkedHashMapAndReturnIdList(processingActivityAttributeValue));
                break;
            case PROCESSING_PURPOSES:
                processingActivity.setProcessingPurposes(castObjectIntoLinkedHashMapAndReturnIdList(processingActivityAttributeValue));
                break;
            case PROCESSING_LEGAL_BASIS:
                processingActivity.setProcessingLegalBasis(castObjectIntoLinkedHashMapAndReturnIdList(processingActivityAttributeValue));
                break;
            case TRANSFER_METHOD:
                processingActivity.setTransferMethods(castObjectIntoLinkedHashMapAndReturnIdList(processingActivityAttributeValue));
                break;
            case DATA_SOURCES:
                processingActivity.setDataSources(castObjectIntoLinkedHashMapAndReturnIdList(processingActivityAttributeValue));
                break;
            case MANAGING_DEPARTMENT:

                processingActivity.setManagingDepartment(objectMapper.convertValue(processingActivityAttributeValue, ManagingOrganization.class));
                break;
            case PROCESS_OWNER:
                processingActivity.setProcessOwner(objectMapper.convertValue(processingActivityAttributeValue, Staff.class));
                processingActivity.setManagingDepartment(objectMapper.convertValue(processingActivityAttributeValue,ManagingOrganization.class));
                break;
            case DATA_RETENTION_PERIOD:
                processingActivity.setDataRetentionPeriod((Integer) processingActivityAttributeValue);
                break;
            case MAX_DATA_SUBJECT_VOLUME:
                processingActivity.setMaxDataSubjectVolume((Long) processingActivityAttributeValue);
                break;
            case MIN_DATA_SUBJECT_VOLUME:
                processingActivity.setMinDataSubjectVolume((Long) processingActivityAttributeValue);
                break;
            case JOINT_CONTROLLER_CONTACT_INFO:
                processingActivity.setJointControllerContactInfo((Integer) processingActivityAttributeValue);
                break;
            case CONTROLLER_CONTACT_INFO:
                processingActivity.setJointControllerContactInfo((Integer) processingActivityAttributeValue);
                break;
            case DPO_CONTACT_INFO:
                processingActivity.setDpoContactInfo((Integer) processingActivityAttributeValue);
                break;
        }
    }

    private List<BigInteger> castObjectIntoLinkedHashMapAndReturnIdList(Object objectToCast) {

        List<BigInteger> entityIdList = new ArrayList<>();
        if (Optional.ofNullable(objectToCast).isPresent()) {
            if (objectToCast instanceof ArrayList) {
                List<LinkedHashMap<String, Object>> entityList = (List<LinkedHashMap<String, Object>>) objectToCast;
                entityList.forEach(entityKeyValueMap -> entityIdList.add(new BigInteger(entityKeyValueMap.get("id").toString())));
            } else {
                LinkedHashMap<String, Object> entityKeyValueMap = (LinkedHashMap<String, Object>) objectToCast;
                entityIdList.add(new BigInteger(entityKeyValueMap.get("id").toString()));
            }
        }
        return entityIdList;
    }


    private void saveAssetValueToAssessment(Long unitId, Assessment assessment, AssetResponseDTO assetResponseDTO) {

        QuestionnaireTemplateResponseDTO questionnaireTemplateDTO = questionnaireTemplateMongoRepository.getQuestionnaireTemplateWithSectionsByUnitId(unitId, assessment.getQuestionnaireTemplateId());
        if (!Optional.ofNullable(questionnaireTemplateDTO).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "Questionnaire Template");
        }
        List<AssessmentAnswerValueObject> assetAssessmentAnswerVOS = new ArrayList<>();
        for (QuestionnaireSectionResponseDTO questionnaireSectionResponseDTO : questionnaireTemplateDTO.getSections()) {
            for (QuestionBasicResponseDTO questionBasicDTO : questionnaireSectionResponseDTO.getQuestions()) {
                assetAssessmentAnswerVOS.add(mapAssetValueAsAsessmentAnswerStatusUpdatingFromNewToInProgress(assetResponseDTO, questionBasicDTO));
            }
        }
        assessment.setAssessmentAnswers(assetAssessmentAnswerVOS);
    }


    private void saveProcessingActivityValueToAssessment(Long unitId, Assessment assessment, ProcessingActivityResponseDTO processingActivityDTO) {
        QuestionnaireTemplateResponseDTO questionnaireTemplateDTO = questionnaireTemplateMongoRepository.getQuestionnaireTemplateWithSectionsByUnitId(unitId, assessment.getQuestionnaireTemplateId());
        if (!Optional.ofNullable(questionnaireTemplateDTO).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "Questionnaire Template");
        }
        List<AssessmentAnswerValueObject> processingActivityAssessmentAnswerVOS = new ArrayList<>();
        for (QuestionnaireSectionResponseDTO questionnaireSectionResponseDTO : questionnaireTemplateDTO.getSections()) {
            for (QuestionBasicResponseDTO questionBasicDTO : questionnaireSectionResponseDTO.getQuestions()) {
                processingActivityAssessmentAnswerVOS.add(mapProcessingActivityValueAssessmentAnswerOnStatusUpdatingFromNewToInProgress(processingActivityDTO, questionBasicDTO));
            }
        }
        assessment.setAssessmentAnswers(processingActivityAssessmentAnswerVOS);

    }

    private void saveRiskTemplateAnswerToAssessment(Long unitId, AssessmentMD assessment) {
/*
        QuestionnaireTemplateResponseDTO questionnaireTemplateDTO = questionnaireTemplateMongoRepository.getQuestionnaireTemplateWithSectionsByUnitId(unitId, assessment.getQuestionnaireTemplate());
        if (!Optional.ofNullable(questionnaireTemplateDTO).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "Questionnaire Template");
        }
        List<AssessmentAnswer> riskAssessmentAnswer = new ArrayList<>();
        for (QuestionnaireSectionResponseDTO questionnaireSectionResponseDTO : questionnaireTemplateDTO.getSections()) {
            for (QuestionBasicResponseDTO questionBasicDTO : questionnaireSectionResponseDTO.getQuestions()) {
                riskAssessmentAnswer.add(new AssessmentAnswer(questionBasicDTO.getId(), null, null, questionBasicDTO.getQuestionType()));
            }
        }
        assessment.setAssessmentAnswers(riskAssessmentAnswer);*/


    }


    //  private void mapRisk

    private AssessmentAnswerValueObject mapAssetValueAsAsessmentAnswerStatusUpdatingFromNewToInProgress(AssetResponseDTO assetResponseDTO, QuestionBasicResponseDTO questionBasicDTO) {

        AssetAttributeName assetAttributeName = AssetAttributeName.valueOf(questionBasicDTO.getAttributeName());
        switch (assetAttributeName) {
       /*     case NAME:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getName(), questionBasicDTO.getQuestionType());
            case DESCRIPTION:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getDescription(), questionBasicDTO.getQuestionType());
            case HOSTING_LOCATION:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getHostingLocation(), questionBasicDTO.getQuestionType());
            case HOSTING_TYPE:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getHostingType(), questionBasicDTO.getQuestionType());
            case DATA_DISPOSAL:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getDataDisposal(), questionBasicDTO.getQuestionType());
            case HOSTING_PROVIDER:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getHostingProvider(), questionBasicDTO.getQuestionType());
            case ASSET_TYPE:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getAssetType(), questionBasicDTO.getQuestionType());
            case STORAGE_FORMAT:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getStorageFormats(), questionBasicDTO.getQuestionType());
            case ASSET_SUB_TYPE:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getAssetSubType(), questionBasicDTO.getQuestionType());
            case TECHNICAL_SECURITY_MEASURES:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getTechnicalSecurityMeasures(), questionBasicDTO.getQuestionType());
            case ORGANIZATION_SECURITY_MEASURES:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getOrgSecurityMeasures(), questionBasicDTO.getQuestionType());
            case MANAGING_DEPARTMENT:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getManagingDepartment(), questionBasicDTO.getQuestionType());
            case ASSET_OWNER:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getAssetOwner(), questionBasicDTO.getQuestionType());
            case DATA_RETENTION_PERIOD:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), assetResponseDTO.getDataRetentionPeriod(), questionBasicDTO.getQuestionType());*/
            default:
                return null;
        }

    }

    private AssessmentAnswerValueObject mapProcessingActivityValueAssessmentAnswerOnStatusUpdatingFromNewToInProgress(ProcessingActivityResponseDTO processingActivityDTO, QuestionBasicResponseDTO questionBasicDTO) {

        ProcessingActivityAttributeName processingActivityAttributeName = ProcessingActivityAttributeName.valueOf(questionBasicDTO.getAttributeName());
        switch (processingActivityAttributeName) {
           /* case NAME:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getName(), questionBasicDTO.getQuestionType());
            case DESCRIPTION:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getDescription(), questionBasicDTO.getQuestionType());
            case RESPONSIBILITY_TYPE:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getResponsibilityType(), questionBasicDTO.getQuestionType());
            case ACCESSOR_PARTY:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getAccessorParties(), questionBasicDTO.getQuestionType());
            case PROCESSING_PURPOSES:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getProcessingPurposes(), questionBasicDTO.getQuestionType());
            case PROCESSING_LEGAL_BASIS:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getProcessingLegalBasis(), questionBasicDTO.getQuestionType());
            case TRANSFER_METHOD:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getTransferMethods(), questionBasicDTO.getQuestionType());
            case DATA_SOURCES:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getDataSources(), questionBasicDTO.getQuestionType());
            case PROCESS_OWNER:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getProcessOwner(), questionBasicDTO.getQuestionType());
            case MANAGING_DEPARTMENT:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getManagingDepartment(), questionBasicDTO.getQuestionType());
            case DATA_RETENTION_PERIOD:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getDataRetentionPeriod(), questionBasicDTO.getQuestionType());
            case DPO_CONTACT_INFO:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getDpoContactInfo(), questionBasicDTO.getQuestionType());
            case CONTROLLER_CONTACT_INFO:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getControllerContactInfo(), questionBasicDTO.getQuestionType());
            case MAX_DATA_SUBJECT_VOLUME:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getMaxDataSubjectVolume(), questionBasicDTO.getQuestionType());
            case MIN_DATA_SUBJECT_VOLUME:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getMinDataSubjectVolume(), questionBasicDTO.getQuestionType());
            case JOINT_CONTROLLER_CONTACT_INFO:
                return new AssessmentAnswerValueObject(questionBasicDTO.getId(), questionBasicDTO.getAttributeName(), processingActivityDTO.getJointControllerContactInfo(), questionBasicDTO.getQuestionType());*/
            default:
                return null;
        }

    }


}
