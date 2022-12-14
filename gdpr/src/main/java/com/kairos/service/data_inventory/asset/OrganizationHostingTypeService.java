package com.kairos.service.data_inventory.asset;

import com.kairos.commons.custom_exception.DataNotFoundByIdException;
import com.kairos.commons.custom_exception.DuplicateDataException;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.gdpr.metadata.HostingTypeDTO;
import com.kairos.persistence.model.master_data.default_asset_setting.HostingType;
import com.kairos.persistence.repository.master_data.asset_management.hosting_type.HostingTypeRepository;
import com.kairos.response.dto.common.HostingTypeResponseDTO;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.master_data.asset_management.HostingTypeService;
import com.kairos.utils.ComparisonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
public class OrganizationHostingTypeService {


    private static final Logger LOGGER = LoggerFactory.getLogger(HostingTypeService.class);

    @Inject
    private HostingTypeRepository hostingTypeRepository;

    @Inject
    private ExceptionService exceptionService;

    @Inject
    private HostingTypeService hostingTypeService;


    /**
     * @param unitId
     * @param hostingTypeDTOS
     * @return return map which contain list of new HostingType and list of existing HostingType if HostingType already exist
     * @description this method create new HostingType if HostingType not exist with same name ,
     * and if exist then simply add  HostingType to existing list and return list ;
     * findByOrganizationIdAndNamesList()  return list of existing HostingType using collation ,used for case insensitive result
     */
    public List<HostingTypeDTO> createHostingType(Long unitId, List<HostingTypeDTO> hostingTypeDTOS) {
        Set<String> existingHostingTypeNames = hostingTypeRepository.findNameByOrganizationIdAndDeleted(unitId);
        Set<String> hostingTypeNames = ComparisonUtils.getNewMetaDataNames(hostingTypeDTOS,existingHostingTypeNames );
        List<HostingType> hostingTypes = new ArrayList<>();
        if (!hostingTypeNames.isEmpty()) {
            for (String name : hostingTypeNames) {
                HostingType hostingType = new HostingType(name);
                hostingType.setOrganizationId(unitId);
                hostingTypes.add(hostingType);
            }
            hostingTypeRepository.saveAll(hostingTypes);
        }
        return ObjectMapperUtils.copyCollectionPropertiesByMapper(hostingTypes, HostingTypeDTO.class);
    }


    /**
     * @param
     * @param unitId
     * @return list of HostingType
     */
    public List<HostingTypeResponseDTO> getAllHostingType(Long unitId) {
        return hostingTypeRepository.findAllByOrganizationIdAndSortByCreatedDate(unitId);
    }


    /**
     * @param
     * @param unitId
     * @param id             of HostingType
     * @return HostingType object fetch by given id
     * @throws DataNotFoundByIdException throw exception if HostingType not found for given id
     */
    public HostingType getHostingType(Long unitId, Long id) {

        HostingType exist = hostingTypeRepository.findByIdAndOrganizationIdAndDeletedFalse(id, unitId);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id ");
        } else {
            return exist;

        }
    }



    /**
     * @param
     * @param unitId
     * @param id             id of HostingType
     * @param hostingTypeDTO
     * @return HostingType updated object
     * @throws DuplicateDataException if HostingType already exist with same name
     */
    public HostingTypeDTO updateHostingType(Long unitId, Long id, HostingTypeDTO hostingTypeDTO) {


        HostingType hostingType = hostingTypeRepository.findByOrganizationIdAndDeletedAndName(unitId, hostingTypeDTO.getName());
        if (Optional.ofNullable(hostingType).isPresent()) {
            if (id.equals(hostingType.getId())) {
                return hostingTypeDTO;
            }
            exceptionService.duplicateDataException("message.duplicate", "message.hostingType", hostingType.getName());
        }
        Integer resultCount = hostingTypeRepository.updateMetadataName(hostingTypeDTO.getName(), id, unitId);
        if (resultCount <= 0) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.hostingType", id);
        } else {
            LOGGER.info("Data updated successfully for id : {} and name updated name is : {}", id, hostingTypeDTO.getName());
        }
        return hostingTypeDTO;


    }


    public List<HostingTypeDTO> saveAndSuggestHostingTypes(Long countryId, Long unitId, List<HostingTypeDTO> hostingTypeDTOS) {

        List<HostingTypeDTO> result = createHostingType(unitId, hostingTypeDTOS);
        hostingTypeService.saveSuggestedHostingTypesFromUnit(countryId, hostingTypeDTOS);
        return result;
    }

}
