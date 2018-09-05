package com.kairos.service.data_inventory.asset;

import com.kairos.custom_exception.DataNotFoundByIdException;
import com.kairos.custom_exception.DuplicateDataException;
import com.kairos.custom_exception.InvalidRequestException;
import com.kairos.gdpr.metadata.TechnicalSecurityMeasureDTO;
import com.kairos.persistance.model.master_data.default_asset_setting.TechnicalSecurityMeasure;
import com.kairos.persistance.repository.data_inventory.asset.AssetMongoRepository;
import com.kairos.persistance.repository.master_data.asset_management.tech_security_measure.TechnicalSecurityMeasureMongoRepository;
import com.kairos.response.dto.common.TechnicalSecurityMeasureResponseDTO;
import com.kairos.response.dto.data_inventory.AssetBasicResponseDTO;
import com.kairos.service.common.MongoBaseService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.master_data.asset_management.TechnicalSecurityMeasureService;
import com.kairos.utils.ComparisonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;

import static com.kairos.constants.AppConstant.EXISTING_DATA_LIST;
import static com.kairos.constants.AppConstant.NEW_DATA_LIST;

@Service
public class OrganizationTechnicalSecurityMeasureService extends MongoBaseService {


    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationTechnicalSecurityMeasureService.class);

    @Inject
    private TechnicalSecurityMeasureMongoRepository technicalSecurityMeasureMongoRepository;

    @Inject
    private ExceptionService exceptionService;

    @Inject
    private TechnicalSecurityMeasureService technicalSecurityMeasureService;


    @Inject
    private AssetMongoRepository assetMongoRepository;


    /**
     * @param
     * @param organizationId
     * @param technicalSecurityMeasureDTOS
     * @return return map which contain list of new TechnicalSecurityMeasure and list of existing TechnicalSecurityMeasure if TechnicalSecurityMeasure already exist
     * @description this method create new TechnicalSecurityMeasure if TechnicalSecurityMeasure not exist with same name ,
     * and if exist then simply add  TechnicalSecurityMeasure to existing list and return list ;
     * findMetaDataByNamesAndCountryId()  return list of existing TechnicalSecurityMeasure using collation ,used for case insensitive result
     */
    public Map<String, List<TechnicalSecurityMeasure>> createTechnicalSecurityMeasure(Long organizationId, List<TechnicalSecurityMeasureDTO> technicalSecurityMeasureDTOS) {

        Map<String, List<TechnicalSecurityMeasure>> result = new HashMap<>();
        Set<String> techSecurityMeasureNames = new HashSet<>();
        if (!technicalSecurityMeasureDTOS.isEmpty()) {
            for (TechnicalSecurityMeasureDTO technicalSecurityMeasure : technicalSecurityMeasureDTOS) {
                if (!StringUtils.isBlank(technicalSecurityMeasure.getName())) {
                    techSecurityMeasureNames.add(technicalSecurityMeasure.getName());
                } else
                    throw new InvalidRequestException("name could not be empty or null");
            }
            List<TechnicalSecurityMeasure> existing = findMetaDataByNameAndUnitId(organizationId, techSecurityMeasureNames, TechnicalSecurityMeasure.class);
            techSecurityMeasureNames = ComparisonUtils.getNameListForMetadata(existing, techSecurityMeasureNames);

            List<TechnicalSecurityMeasure> newTechnicalMeasures = new ArrayList<>();
            if (!techSecurityMeasureNames.isEmpty()) {
                for (String name : techSecurityMeasureNames) {
                    TechnicalSecurityMeasure newTechnicalSecurityMeasure = new TechnicalSecurityMeasure(name);
                    newTechnicalSecurityMeasure.setOrganizationId(organizationId);
                    newTechnicalMeasures.add(newTechnicalSecurityMeasure);

                }
                newTechnicalMeasures = technicalSecurityMeasureMongoRepository.saveAll(getNextSequence(newTechnicalMeasures));
            }
            result.put(EXISTING_DATA_LIST, existing);
            result.put(NEW_DATA_LIST, newTechnicalMeasures);
            return result;
        } else
            throw new InvalidRequestException("list cannot be empty");


    }


    /**
     * @param
     * @param organizationId
     * @return list of TechnicalSecurityMeasure
     */
    public List<TechnicalSecurityMeasureResponseDTO> getAllTechnicalSecurityMeasure(Long organizationId) {
        return technicalSecurityMeasureMongoRepository.findAllOrganizationTechnicalSecurityMeasures(organizationId);
    }


    /**
     * @param
     * @param organizationId
     * @param id             id of TechnicalSecurityMeasure
     * @return object of TechnicalSecurityMeasure
     * @throws DataNotFoundByIdException throw exception if TechnicalSecurityMeasure not exist for given id
     */
    public TechnicalSecurityMeasure getTechnicalSecurityMeasure(Long organizationId, BigInteger id) {

        TechnicalSecurityMeasure exist = technicalSecurityMeasureMongoRepository.findByOrganizationIdAndId(organizationId, id);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id " + id);
        } else {
            return exist;

        }
    }


    public Boolean deleteTechnicalSecurityMeasure(Long unitId, BigInteger techSecurityMeasureId) {

        List<AssetBasicResponseDTO> assetsLinkedWithTechnicalSecurityMeasure = assetMongoRepository.findAllAssetLinkedWithTechnicalSecurityMeasure(unitId, techSecurityMeasureId);
        if (!assetsLinkedWithTechnicalSecurityMeasure.isEmpty()) {
            StringBuilder assetNames=new StringBuilder();
            assetsLinkedWithTechnicalSecurityMeasure.forEach(asset->assetNames.append(asset.getName()+","));
            exceptionService.metaDataLinkedWithAssetException("message.metaData.linked.with.asset", "Technical Security Measure", assetNames);
        }
        TechnicalSecurityMeasure technicalSecurityMeasure = technicalSecurityMeasureMongoRepository.findByOrganizationIdAndId(unitId, techSecurityMeasureId);
        if (!Optional.ofNullable(technicalSecurityMeasure).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "Security Measure", techSecurityMeasureId);
        }
        delete(technicalSecurityMeasure);
        return true;
    }

    /**
     * @param
     * @param organizationId
     * @param id                          id of TechnicalSecurityMeasure
     * @param technicalSecurityMeasureDTO
     * @return TechnicalSecurityMeasure updated object
     * @throws DuplicateDataException throw exception if TechnicalSecurityMeasure data not exist for given id
     */
    public TechnicalSecurityMeasureDTO updateTechnicalSecurityMeasure(Long organizationId, BigInteger id, TechnicalSecurityMeasureDTO technicalSecurityMeasureDTO) {
        TechnicalSecurityMeasure technicalSecurityMeasure = technicalSecurityMeasureMongoRepository.findByOrganizationIdAndName(organizationId, technicalSecurityMeasureDTO.getName());
        if (Optional.ofNullable(technicalSecurityMeasure).isPresent()) {
            if (id.equals(technicalSecurityMeasure.getId())) {
                return technicalSecurityMeasureDTO;
            }
            exceptionService.duplicateDataException("message.duplicate", "Security Measure", technicalSecurityMeasure.getName());
        }
        technicalSecurityMeasure = technicalSecurityMeasureMongoRepository.findByid(id);
        if (!Optional.ofNullable(technicalSecurityMeasure).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "Security Measure", id);
        }
        technicalSecurityMeasure.setName(technicalSecurityMeasureDTO.getName());
        technicalSecurityMeasureMongoRepository.save(technicalSecurityMeasure);
        return technicalSecurityMeasureDTO;


    }


    public Map<String, List<TechnicalSecurityMeasure>> saveAndSuggestTechnicalSecurityMeasures(Long countryId, Long organizationId, List<TechnicalSecurityMeasureDTO> techSecurityMeasureDTOS) {

        Map<String, List<TechnicalSecurityMeasure>> result;
        result = createTechnicalSecurityMeasure(organizationId, techSecurityMeasureDTOS);
        List<TechnicalSecurityMeasure> masterTechnicalSecurityMeasureSuggestedByUnit = technicalSecurityMeasureService.saveSuggestedTechnicalSecurityMeasuresFromUnit(countryId, techSecurityMeasureDTOS);
        if (!masterTechnicalSecurityMeasureSuggestedByUnit.isEmpty()) {
            result.put("SuggestedData", masterTechnicalSecurityMeasureSuggestedByUnit);
        }
        return result;
    }


}
