package com.kairos.service.data_inventory.processing_activity;


import com.kairos.dto.data_inventory.ProcessingActivityDTO;
import com.kairos.persistance.model.data_inventory.processing_activity.ProcessingActivity;
import com.kairos.persistance.repository.data_inventory.processing_activity.ProcessingActivityMongoRepository;
import com.kairos.persistance.repository.master_data.processing_activity_masterdata.responsibility_type.ResponsibilityTypeMongoRepository;
import com.kairos.response.dto.data_inventory.ProcessingActivityResponseDTO;
import com.kairos.service.common.MongoBaseService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.master_data.processing_activity_masterdata.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;

@Service
public class ProcessingActivityService extends MongoBaseService {


    @Inject
    private ProcessingActivityMongoRepository processingActivityMongoRepository;


    @Inject
    private ExceptionService exceptionService;

    @Inject
    private AccessorPartyService accessorPartyService;

    @Inject
    private ResponsibilityTypeMongoRepository responsibilityTypeMongoRepository;

    @Inject
    private DataSourceService dataSourceService;

    @Inject
    private TransferMethodService transferMethodService;

    @Inject
    private ProcessingLegalBasisService processingLegalBasisService;

    @Inject
    private ProcessingPurposeService processingPurposeService;


    public ProcessingActivity createProcessingActivity(Long organizationId, ProcessingActivityDTO processingActivityDTO) {


        ProcessingActivity exist = processingActivityMongoRepository.findByName(organizationId, processingActivityDTO.getName());
        if (Optional.ofNullable(exist).isPresent()) {
            exceptionService.duplicateDataException("message.duplicate", " Processing Activity ", processingActivityDTO.getName());
        }
        ProcessingActivity processingActivity = buildProcessingActivity(organizationId, processingActivityDTO);
        if (!processingActivityDTO.getSubProcessingActivities().isEmpty()) {
            processingActivity.setSubProcessingActivities(createSubProcessingActivity(organizationId, processingActivityDTO.getSubProcessingActivities()));
        }
        return processingActivityMongoRepository.save(getNextSequence(processingActivity));

    }


    public Boolean deleteProcessingActivity(Long organizationId, BigInteger id) {
        ProcessingActivity exist = processingActivityMongoRepository.findByIdAndNonDeleted(organizationId, id);
        if (!Optional.ofNullable(exist).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", " Processing Activity ", id);
        }
        delete(exist);
        return true;

    }


    public ProcessingActivityResponseDTO getProcessingActivityWithMetaDataById(Long orgId, BigInteger id) {
        ProcessingActivityResponseDTO processingActivity = processingActivityMongoRepository.getProcessingActivityWithSubProcessingActivitiesAndMetaDataById(orgId, id);
        if (!Optional.ofNullable(processingActivity).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", " Processing Activity ", id);
        }
        return processingActivity;
    }


    public List<ProcessingActivityResponseDTO> getAllProcessingActivityWithMetaData(Long orgId) {
        return processingActivityMongoRepository.getAllProcessingActivityWithSubProcessingActivitiesAndMetaData(orgId);
    }



    public ProcessingActivity updateProcessingActivity(Long organizationId, BigInteger id, ProcessingActivityDTO processingActivityDTO) {


        ProcessingActivity exist = processingActivityMongoRepository.findByName(organizationId, processingActivityDTO.getName());
        if (Optional.ofNullable(exist).isPresent() && !id.equals(exist.getId())) {
            exceptionService.duplicateDataException("message.duplicate", " Processing Activity ", processingActivityDTO.getName());
        }
        exist = processingActivityMongoRepository.findByIdAndNonDeleted(organizationId, id);
        if (!Optional.ofNullable(exist).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", " Processing Activity ", id);
        }

        if (!processingActivityDTO.getSubProcessingActivities().isEmpty()) {
            exist.setSubProcessingActivities(updateExisitingSubProcessingActivitiesAndCreateNewSubProcess(organizationId, processingActivityDTO.getSubProcessingActivities()));

        }
        exist.setName(processingActivityDTO.getName());
        exist.setDescription(processingActivityDTO.getDescription());
        exist.setManagingDepartment(processingActivityDTO.getManagingDepartment());
        exist.setProcessOwner(processingActivityDTO.getProcessOwner());
        exist.setControllerContactInfo(processingActivityDTO.getControllerContactInfo());
        exist.setJointControllerContactInfo(processingActivityDTO.getJointControllerContactInfo());
        exist.setMaxDataSubjectVolume(processingActivityDTO.getMinDataSubjectVolume());
        exist.setMinDataSubjectVolume(processingActivityDTO.getMinDataSubjectVolume());
        return processingActivityMongoRepository.save(getNextSequence(exist));

    }

    private List<BigInteger> createSubProcessingActivity(Long organizationId, List<ProcessingActivityDTO> subProcessingActivityDTOs) {

        List<ProcessingActivity> subProcessingActivities = new ArrayList<>();
        List<BigInteger> subProcessingActivityIdList = new ArrayList<>();

        for (ProcessingActivityDTO processingActivityDTO : subProcessingActivityDTOs) {

            ProcessingActivity processingActivity = buildProcessingActivity(organizationId, processingActivityDTO);
            processingActivity.setSubProcess(true);
            subProcessingActivities.add(processingActivity);
        }
        subProcessingActivities = processingActivityMongoRepository.saveAll(getNextSequence(subProcessingActivities));
        subProcessingActivities.forEach(processingActivity -> {

            subProcessingActivityIdList.add(processingActivity.getId());
        });
        return subProcessingActivityIdList;

    }


    private ProcessingActivity buildProcessingActivity(Long organizationId, ProcessingActivityDTO processingActivityDTO) {
        ProcessingActivity processingActivity = new ProcessingActivity(processingActivityDTO.getName(), processingActivityDTO.getDescription(),
                processingActivityDTO.getManagingDepartment(), processingActivityDTO.getProcessOwner());
        processingActivity.setOrganizationId(organizationId);
        processingActivity.setControllerContactInfo(processingActivityDTO.getControllerContactInfo());
        processingActivity.setJointControllerContactInfo(processingActivityDTO.getJointControllerContactInfo());
        processingActivity.setMaxDataSubjectVolume(processingActivityDTO.getMinDataSubjectVolume());
        processingActivity.setMinDataSubjectVolume(processingActivityDTO.getMinDataSubjectVolume());
        processingActivity.setResponsibilityType(processingActivityDTO.getResponsibilityType());
        processingActivity.setTransferMethods(processingActivityDTO.getTransferMethods());
        processingActivity.setProcessingPurposes(processingActivityDTO.getProcessingPurposes());
        processingActivity.setDataSources(processingActivityDTO.getDataSources());
        processingActivity.setAccessorParties(processingActivityDTO.getAccessorParties());
        processingActivity.setProcessingLegalBasis(processingActivityDTO.getProcessingLegalBasis());
        return processingActivity;

    }

    private List<BigInteger> updateExisitingSubProcessingActivitiesAndCreateNewSubProcess(Long organizationId, List<ProcessingActivityDTO> subProcessingActivityDTOs) {

        List<ProcessingActivityDTO> newSubProcessingActivityDTOList = new ArrayList<>();
        Map<BigInteger, ProcessingActivityDTO> exisingSubProcessingActivityMap = new HashMap<>();
        List<BigInteger> subProcessingActivitiesIdList = new ArrayList<>();
        subProcessingActivityDTOs.forEach(processingActivityDTO -> {
            if (Optional.ofNullable(processingActivityDTO.getId()).isPresent()) {
                exisingSubProcessingActivityMap.put(processingActivityDTO.getId(), processingActivityDTO);
                subProcessingActivitiesIdList.add(processingActivityDTO.getId());
            } else {
                newSubProcessingActivityDTOList.add(processingActivityDTO);
            }
        });
        if (!exisingSubProcessingActivityMap.isEmpty()) {
            updateSubProcessingActivities(organizationId, subProcessingActivitiesIdList, exisingSubProcessingActivityMap);
        } else if (!newSubProcessingActivityDTOList.isEmpty()) {
            subProcessingActivitiesIdList.addAll(createSubProcessingActivity(organizationId, newSubProcessingActivityDTOList));
        }
        return subProcessingActivitiesIdList;

    }


    private void updateSubProcessingActivities(Long orgId, List<BigInteger> subProcessingActivityIds, Map<BigInteger, ProcessingActivityDTO> subProcessingActivityMap) {

        List<ProcessingActivity> subProcessingActivities = processingActivityMongoRepository.findSubProcessingActvitiesByIds(orgId, subProcessingActivityIds);
        subProcessingActivities.forEach(processingActivity -> {
            ProcessingActivityDTO processingActivityDTO = subProcessingActivityMap.get(processingActivity.getId());
            processingActivity.setName(processingActivityDTO.getName());
            processingActivity.setDescription(processingActivityDTO.getDescription());
            processingActivity.setManagingDepartment(processingActivityDTO.getManagingDepartment());
            processingActivity.setProcessOwner(processingActivityDTO.getProcessOwner());
            processingActivity.setControllerContactInfo(processingActivityDTO.getControllerContactInfo());
            processingActivity.setJointControllerContactInfo(processingActivityDTO.getJointControllerContactInfo());
            processingActivity.setMaxDataSubjectVolume(processingActivityDTO.getMinDataSubjectVolume());
            processingActivity.setMinDataSubjectVolume(processingActivityDTO.getMinDataSubjectVolume());
            processingActivity.setProcessingLegalBasis(processingActivityDTO.getProcessingLegalBasis());
            processingActivity.setAccessorParties(processingActivityDTO.getAccessorParties());
            processingActivity.setDataSources(processingActivityDTO.getDataSources());
            processingActivity.setProcessingLegalBasis(processingActivityDTO.getProcessingLegalBasis());
            processingActivity.setTransferMethods(processingActivityDTO.getTransferMethods());
            processingActivity.setResponsibilityType(processingActivityDTO.getResponsibilityType());

        });
        processingActivityMongoRepository.saveAll(getNextSequence(subProcessingActivities));

    }












/*

    private void buildProcessingActivityWithMetaDataAndUpdate( Long organizationId, ProcessingActivityDTO processingActivityDTO, ProcessingActivity processingActivity) {

        if (Optional.ofNullable(processingActivityDTO.getAccessorParties()).isPresent() && !processingActivityDTO.getAccessorParties().isEmpty()) {
            List<BigInteger> accessorPartyIds = accessorPartyService.createAccessorPartyForOrganizationOnInheritingFromParentOrganization( organizationId, processingActivityDTO);
            processingActivity.setAccessorParties(accessorPartyIds);
        } else if (Optional.ofNullable(processingActivityDTO.getDataSources()).isPresent() && !processingActivityDTO.getDataSources().isEmpty()) {
            List<BigInteger> dataSourceIds = dataSourceService.createDataSourceForOrganizationOnInheritingFromParentOrganization( organizationId, processingActivityDTO);
            processingActivity.setDataSources(dataSourceIds);
        } else if (Optional.ofNullable(processingActivityDTO.getProcessingLegalBasis()).isPresent() && !processingActivityDTO.getProcessingLegalBasis().isEmpty()) {
            List<BigInteger> processingLegalBasisIds = processingLegalBasisService.createProcessingLegaBasisForOrganizationOnInheritingFromParentOrganization( organizationId, processingActivityDTO);
            processingActivity.setProcessingLegalBasis(processingLegalBasisIds);
        } else if (Optional.ofNullable(processingActivityDTO.getProcessingPurposes()).isPresent() && !processingActivityDTO.getProcessingPurposes().isEmpty()) {
            List<BigInteger> processingPurposeIds = processingPurposeService.createProcessingPurposeForOrganizationOnInheritingFromParentOrganization( organizationId, processingActivityDTO);
            processingActivity.setProcessingPurposes(processingPurposeIds);

        } else if (Optional.ofNullable(processingActivityDTO.getTransferMethods()).isPresent() && !processingActivityDTO.getTransferMethods().isEmpty()) {
            List<BigInteger> transferMethodIds = transferMethodService.createTransferMethodForOrganizationOnInheritingFromParentOrganization( organizationId, processingActivityDTO.getTransferMethods());
            processingActivity.setTransferMethods(transferMethodIds);
        } else if (Optional.ofNullable(processingActivityDTO.getResponsibilityType()).isPresent()) {

            ResponsibilityTypeDTO responsibilityTypeDTO = processingActivityDTO.getResponsibilityType();
            if (!responsibilityTypeDTO.getOrganizationId().equals(organizationId)) {
                ResponsibilityType responsibilityType = new ResponsibilityType(responsibilityTypeDTO.getName());
                responsibilityType.setOrganizationId(organizationId);
                responsibilityType = responsibilityTypeMongoRepository.save(getNextSequence(responsibilityType));
                processingActivity.setResponsibilityType(responsibilityType.getId());
            } else {
                processingActivity.setResponsibilityType(responsibilityTypeDTO.getId());
            }
        }
    }
*/


}

