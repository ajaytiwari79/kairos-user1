package com.kairos.service.master_data.asset_management;


import com.kairos.custom_exception.DataNotFoundByIdException;
import com.kairos.custom_exception.DuplicateDataException;
import com.kairos.custom_exception.InvalidRequestException;
import com.kairos.enums.SuggestedDataStatus;
import com.kairos.dto.gdpr.metadata.StorageFormatDTO;
import com.kairos.persistence.model.master_data.default_asset_setting.StorageFormat;
import com.kairos.persistence.repository.master_data.asset_management.storage_format.StorageFormatMongoRepository;
import com.kairos.response.dto.common.StorageFormatResponseDTO;
import com.kairos.service.common.MongoBaseService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.utils.ComparisonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;

import static com.kairos.constants.AppConstant.EXISTING_DATA_LIST;
import static com.kairos.constants.AppConstant.NEW_DATA_LIST;

@Service
public class StorageFormatService extends MongoBaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFormatService.class);

    @Inject
    private StorageFormatMongoRepository storageFormatMongoRepository;

    @Inject
    private ExceptionService exceptionService;

    /**
     * @param countryId
     * @param
     * @param storageFormatDTOS
     * @return return map which contain list of new StorageFormat and list of existing StorageFormat if StorageFormat already exist
     * @description this method create new StorageFormat if StorageFormat not exist with same name ,
     * and if exist then simply add  StorageFormat to existing list and return list ;
     * findMetaDataByNamesAndCountryId()  return list of existing StorageFormat using collation ,used for case insensitive result
     */
    public Map<String, List<StorageFormat>> createStorageFormat(Long countryId, List<StorageFormatDTO> storageFormatDTOS) {

        Map<String, List<StorageFormat>> result = new HashMap<>();
        Set<String> storageFormatNames = new HashSet<>();
        if (!storageFormatDTOS.isEmpty()) {
            for (StorageFormatDTO storageFormat : storageFormatDTOS) {
                storageFormatNames.add(storageFormat.getName());
            }
            List<StorageFormat> existing = findMetaDataByNamesAndCountryId(countryId, storageFormatNames, StorageFormat.class);
            storageFormatNames = ComparisonUtils.getNameListForMetadata(existing, storageFormatNames);

            List<StorageFormat> newStorageFormats = new ArrayList<>();
            if (!storageFormatNames.isEmpty()) {
                for (String name : storageFormatNames) {

                    StorageFormat newStorageFormat = new StorageFormat(name,countryId,SuggestedDataStatus.APPROVED);
                    newStorageFormats.add(newStorageFormat);

                }


                newStorageFormats = storageFormatMongoRepository.saveAll(getNextSequence(newStorageFormats));
            }
            result.put(EXISTING_DATA_LIST, existing);
            result.put(NEW_DATA_LIST, newStorageFormats);
            return result;
        } else
            throw new InvalidRequestException("list cannot be empty");


    }


    /**
     * @param countryId
     * @param
     * @return list of StorageFormat
     */
    public List<StorageFormatResponseDTO> getAllStorageFormat(Long countryId) {
        return storageFormatMongoRepository.findAllStorageFormats(countryId,new Sort(Sort.Direction.DESC, "_id"));
    }

    /**
     * @param countryId
     * @param
     * @param id        id of StorageFormat
     * @return StorageFormat object fetch via given id
     * @throws DataNotFoundByIdException throw exception if StorageFormat not exist for given id
     */
    public StorageFormat getStorageFormat(Long countryId, BigInteger id) {

        StorageFormat exist = storageFormatMongoRepository.findByIdAndNonDeleted(countryId, id);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id " + id);
        } else {
            return exist;

        }
    }


    public Boolean deleteStorageFormat(Long countryId, BigInteger id) {

        StorageFormat storageFormat = storageFormatMongoRepository.findByIdAndNonDeleted(countryId, id);
        if (!Optional.ofNullable(storageFormat).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id " + id);
        }
            delete(storageFormat);
            return true;


    }


    /**
     * @param countryId
     * @param
     * @param id               id of StorageFormat
     * @param storageFormatDTO
     * @return StorageFormat updated object
     * @throws DuplicateDataException throw exception if data not exist for given id
     */
    public StorageFormatDTO updateStorageFormat(Long countryId, BigInteger id, StorageFormatDTO storageFormatDTO) {

        StorageFormat storageFormat = storageFormatMongoRepository.findByNameAndCountryId(countryId, storageFormatDTO.getName());
        if (Optional.ofNullable(storageFormat).isPresent()) {
            if (id.equals(storageFormat.getId())) {
                return storageFormatDTO;
            }
            throw new DuplicateDataException("data  exist for  " + storageFormatDTO.getName());
        }
        storageFormat = storageFormatMongoRepository.findByid(id);
        if (!Optional.ofNullable(storageFormat).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "Storage Format", id);
        }
        storageFormat.setName(storageFormatDTO.getName());
        storageFormatMongoRepository.save(storageFormat);
        return storageFormatDTO;


    }

    /**
     * @description method save Storage format suggested by unit
     * @param countryId
     * @param storageFormatDTOS
     * @return
     */
    public List<StorageFormat> saveSuggestedStorageFormatsFromUnit(Long countryId, List<StorageFormatDTO> storageFormatDTOS) {

        Set<String> storageFormatNameList = new HashSet<>();
        for (StorageFormatDTO StorageFormat : storageFormatDTOS) {
            storageFormatNameList.add(StorageFormat.getName());
        }
        List<StorageFormat> existingStorageFormats = findMetaDataByNamesAndCountryId(countryId, storageFormatNameList, StorageFormat.class);
        storageFormatNameList = ComparisonUtils.getNameListForMetadata(existingStorageFormats, storageFormatNameList);
        List<StorageFormat> storageFormatList = new ArrayList<>();
        if (!storageFormatNameList.isEmpty()) {
            for (String name : storageFormatNameList) {

                StorageFormat storageFormat = new StorageFormat(name);
                storageFormat.setCountryId(countryId);
                storageFormat.setSuggestedDataStatus(SuggestedDataStatus.PENDING);
                storageFormat.setSuggestedDate(LocalDate.now());
                storageFormatList.add(storageFormat);
            }

             storageFormatMongoRepository.saveAll(getNextSequence(storageFormatList));
        }
        return storageFormatList;
    }


    /**
     *
     * @param countryId
     * @param storageFormatIds
     * @param suggestedDataStatus
     * @return
     */
    public List<StorageFormat> updateSuggestedStatusOfStorageFormatList(Long countryId, Set<BigInteger> storageFormatIds, SuggestedDataStatus suggestedDataStatus) {

        List<StorageFormat> storageFormatList = storageFormatMongoRepository.getStorageFormatListByIds(countryId, storageFormatIds);
        storageFormatList.forEach(storageFormat-> storageFormat.setSuggestedDataStatus(suggestedDataStatus));
        storageFormatMongoRepository.saveAll(getNextSequence(storageFormatList));
        return storageFormatList;
    }


}
