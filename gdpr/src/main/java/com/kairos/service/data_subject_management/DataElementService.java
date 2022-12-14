package com.kairos.service.data_subject_management;

import com.kairos.dto.gdpr.master_data.DataElementDTO;
import com.kairos.persistence.model.master_data.data_category_element.DataElement;
import com.kairos.persistence.repository.master_data.data_category_element.DataElementRepository;
import com.kairos.service.exception.ExceptionService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

import static com.kairos.constants.GdprMessagesConstants.MESSAGE_DATAELEMENT;
import static com.kairos.constants.GdprMessagesConstants.MESSAGE_DUPLICATE;


@Service
public class DataElementService{

    @Inject
    private ExceptionService exceptionService;

    @Inject
    private DataElementRepository dataElementRepository;


    /**
     * @param referenceId     reference id may be country id or unitId
     * @param dataElementsDto request body for creating New Data Elements
     * @return map of Data Elements  List  and new Data Elements ids
     * @decription method create new Data Elements throw exception if data element already exist
     */
    public List<DataElement> createDataElements(Long referenceId, boolean isOrganization, List<DataElementDTO> dataElementsDto) {

        Set<String> dataElementNames = checkForDuplicacyInName(dataElementsDto);
        List<DataElement> existingDataElement = isOrganization ? dataElementRepository.findByUnitIdAndNames(referenceId, dataElementNames) : dataElementRepository.findByCountryIdAndNames(referenceId, dataElementNames);
        if (CollectionUtils.isNotEmpty(existingDataElement)) {
            exceptionService.duplicateDataException(MESSAGE_DUPLICATE, MESSAGE_DATAELEMENT, existingDataElement.iterator().next().getName());
        }
        List<DataElement> dataElementList = new ArrayList<>();
        for (String name : dataElementNames) {
            DataElement dataElement = new DataElement(name);
            if (isOrganization)
                dataElement.setOrganizationId(referenceId);
            else
                dataElement.setCountryId(referenceId);
            dataElementList.add(dataElement);

        }
        return dataElementList;
    }


    /**
     * @param referenceId     = unitId or countryId
     * @param dataElementsDto request body contain list Of Existing Data Elements which needs to be Update and List of New Data Elements
     * @return map of Data Element ids and ,List of  updated and new Data Elements
     * @desciption method create new data Data elements and update data Element if data element already exist.
     */

    public List<DataElement> updateDataElementAndCreateNewDataElement(Long referenceId, boolean isOrganization, List<DataElementDTO> dataElementsDto) {

        Set<String> dataElementNames = checkForDuplicacyInName(dataElementsDto);
        Map<Long, DataElementDTO> dataElementDTOMap = new HashMap<>();
        List<DataElement> dataElements = new ArrayList<>();
        dataElementsDto.forEach(dataElementDto -> {
            if (Optional.ofNullable(dataElementDto.getId()).isPresent()) {
                dataElementDTOMap.put(dataElementDto.getId(), dataElementDto);
            } else {
                DataElement dataElement = new DataElement(dataElementDto.getName());
                if (isOrganization)
                    dataElement.setOrganizationId(referenceId);
                else
                    dataElement.setCountryId(referenceId);
                dataElements.add(dataElement);
            }
        });
        List<DataElement> previousDataElementList = isOrganization ? dataElementRepository.findByUnitIdAndNames(referenceId, dataElementNames) : dataElementRepository.findByCountryIdAndNames(referenceId, dataElementNames);
        previousDataElementList.forEach(dataElement -> {

            if (!dataElementDTOMap.containsKey(dataElement.getId())) {
                exceptionService.duplicateDataException(MESSAGE_DUPLICATE, MESSAGE_DATAELEMENT, dataElement.getName());
            }
        });
        previousDataElementList = isOrganization ? dataElementRepository.findByUnitIdAndIds(referenceId, dataElementDTOMap.keySet()) : dataElementRepository.findByCountryIdAndIds(referenceId, dataElementDTOMap.keySet());
        previousDataElementList.forEach(dataElement -> dataElement.setName(dataElementDTOMap.get(dataElement.getId()).getName()));
        dataElements.addAll(previousDataElementList);
        dataElementRepository.saveAll(dataElements);
        return dataElements;
    }



    private Set<String> checkForDuplicacyInName(List<DataElementDTO> dataElementDTOs) {

        Set<String> dataElementNames = new HashSet<>();
        List<String> dataElementNamesLowerCase = new ArrayList<>();
        dataElementDTOs.forEach(dataElementDTO -> {
            if (dataElementNamesLowerCase.contains(dataElementDTO.getName().toLowerCase())) {
                exceptionService.duplicateDataException(MESSAGE_DUPLICATE, MESSAGE_DATAELEMENT,dataElementDTO.getName());
            }
            dataElementNames.add(dataElementDTO.getName());
            dataElementNamesLowerCase.add(dataElementDTO.getName().toLowerCase());
        });
        return dataElementNames;
    }

}
