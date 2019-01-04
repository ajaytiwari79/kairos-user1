package com.kairos.service.master_data.asset_management;


import com.kairos.commons.custom_exception.DuplicateDataException;
import com.kairos.dto.gdpr.BasicRiskDTO;
import com.kairos.dto.gdpr.master_data.AssetTypeDTO;
import com.kairos.dto.gdpr.metadata.AssetTypeBasicDTO;
import com.kairos.enums.gdpr.SuggestedDataStatus;
import com.kairos.persistence.model.master_data.default_asset_setting.AssetType;
import com.kairos.persistence.model.master_data.default_asset_setting.AssetTypeMD;
import com.kairos.persistence.model.master_data.default_asset_setting.MasterAsset;
import com.kairos.persistence.model.risk_management.Risk;
import com.kairos.persistence.model.risk_management.RiskMD;
import com.kairos.persistence.repository.master_data.asset_management.AssetTypeMongoRepository;
import com.kairos.persistence.repository.master_data.asset_management.AssetTypeRepository;
import com.kairos.persistence.repository.master_data.asset_management.MasterAssetMongoRepository;
import com.kairos.persistence.repository.risk_management.RiskMongoRepository;
import com.kairos.response.dto.master_data.AssetTypeRiskResponseDTO;
import com.kairos.service.common.MongoBaseService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.risk_management.RiskService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AssetTypeService extends MongoBaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetTypeService.class);


    @Inject
    private ExceptionService exceptionService;

    @Inject
    private AssetTypeMongoRepository assetTypeMongoRepository;

    @Inject
    private MasterAssetMongoRepository masterAssetMongoRepository;

    @Inject
    private RiskService riskService;

    @Inject
    private RiskMongoRepository riskMongoRepository;

    @Inject
    private AssetTypeRepository assetTypeRepository;


    /**
     * @param countryId
     * @param
     * @param assetTypeDto contain asset data ,and list of sub asset types
     * @return asset type object
     * @throws DuplicateDataException if asset type is already present with same name
     * @description method create Asset type if sub Asset Types if present then create and add sub Asset Types to Asset type.
     */
    public AssetTypeDTO createAssetTypeAndAddSubAssetTypes(Long countryId, AssetTypeDTO assetTypeDto) {


        AssetTypeMD assetTypeExist = assetTypeRepository.findByNameAndCountryIdAndSubAssetType(assetTypeDto.getName(),countryId,  false);
        if (Optional.ofNullable(assetTypeExist).isPresent()) {
            exceptionService.duplicateDataException("message.duplicate", "message.assetType", assetTypeDto.getName());
        }
        AssetTypeMD assetType = new AssetTypeMD(assetTypeDto.getName(), countryId, SuggestedDataStatus.APPROVED);
        Map<AssetTypeMD, List<BasicRiskDTO>> riskRelatedToAssetTypeAndSubAssetType = new HashMap<>();
        List<AssetTypeMD> subAssetTypeList = new ArrayList<>();
        List<RiskMD> assetTypeRisks = new ArrayList<>();
        riskRelatedToAssetTypeAndSubAssetType.put(assetType, assetTypeDto.getRisks());
        if (!assetTypeDto.getSubAssetTypes().isEmpty()) {
            subAssetTypeList = buildSubAssetTypesListAndRiskAndLinkedToAssetType(countryId, assetTypeDto.getSubAssetTypes(), riskRelatedToAssetTypeAndSubAssetType,assetType);
            assetType.setHasSubAsset(true);
            assetType.setSubAssetTypes(subAssetTypeList);
        }
        for(BasicRiskDTO assetTypeRisk : assetTypeDto.getRisks())
        {
            RiskMD risk = new RiskMD(assetTypeRisk.getName(), assetTypeRisk.getDescription(), assetTypeRisk.getRiskRecommendation(), assetTypeRisk.getRiskLevel());
            risk.setAssetType(assetType);
            assetTypeRisks.add(risk);
        }
        /*Map<AssetTypeMD, List<RiskMD>> riskIdsCorrespondingToAssetAndSubAssetType;
        if (CollectionUtils.isNotEmpty(riskRelatedToAssetTypeAndSubAssetType.entrySet().stream().map(Map.Entry::getValue).flatMap(List::stream).collect(Collectors.toList()))) {
            riskIdsCorrespondingToAssetAndSubAssetType = riskService.saveRiskAtCountryLevelOrOrganizationLevel(countryId, false, riskRelatedToAssetTypeAndSubAssetType);
            for (AssetTypeMD subAssetType : subAssetTypeList) {
                subAssetType.setRisks(riskIdsCorrespondingToAssetAndSubAssetType.get(subAssetType).stream().map(Risk::getId).collect(Collectors.toSet()));
            }
            if (riskIdsCorrespondingToAssetAndSubAssetType.containsKey(assetType))
                assetType.setRisks(riskIdsCorrespondingToAssetAndSubAssetType.get(assetType).stream().map(Risk::getId).collect(Collectors.toSet()));
        }
        if (!subAssetTypeList.isEmpty()) {
            assetTypeMongoRepository.saveAll(getNextSequence(subAssetTypeList));
            assetType.setSubAssetTypes(subAssetTypeList.stream().map(AssetType::getId).collect(Collectors.toSet()));
        }*/
        assetType.setRisks(assetTypeRisks);
        assetTypeRepository.save(assetType);
        assetTypeDto.setId(assetType.getId());
        return assetTypeDto;
    }

    /**
     * @param countryId
     * @param subAssetTypesDto contain list of sub Asset DTOs
     * @return create new Sub Asset type ids
     */
    public List<AssetTypeMD> buildSubAssetTypesListAndRiskAndLinkedToAssetType(Long countryId, List<AssetTypeDTO> subAssetTypesDto, Map<AssetTypeMD, List<BasicRiskDTO>> riskRelatedToSubAssetTypes, AssetTypeMD assetTypeMD) {

        checkForDuplicacyInNameOfAssetType(subAssetTypesDto);
        List<AssetTypeMD> subAssetTypes = new ArrayList<>();
        List<RiskMD> subAssetRisks = new ArrayList<>();
        for (AssetTypeDTO subAssetTypeDto : subAssetTypesDto) {
            AssetTypeMD assetSubType = new AssetTypeMD(subAssetTypeDto.getName(), countryId, SuggestedDataStatus.APPROVED);
            assetSubType.setSubAssetType(true);
            assetSubType.setAssetType(assetTypeMD);
            for(BasicRiskDTO subAssetTypeRisk : subAssetTypeDto.getRisks())
            {
                RiskMD risk = new RiskMD(subAssetTypeRisk.getName(), subAssetTypeRisk.getDescription(), subAssetTypeRisk.getRiskRecommendation(), subAssetTypeRisk.getRiskLevel() );
                risk.setAssetType(assetSubType);
                subAssetRisks.add(risk);
            }
            riskRelatedToSubAssetTypes.put(assetSubType, subAssetTypeDto.getRisks());
            assetSubType.setRisks(subAssetRisks);
            subAssetTypes.add(assetSubType);
        }
        return subAssetTypes;
    }


    /**
     * @param countryId
     * @param subAssetTypesDto contain list of Existing Sub Asset type which need to we update
     * @return map of Sub asset Types List and Ids (List for rollback)
     * @description this method update existing Sub asset Types and return list of Sub Asset Types and  ids list
     */
    private List<AssetType> updateSubAssetTypes(Long countryId, List<AssetTypeDTO> subAssetTypesDto, Map<AssetType, List<BasicRiskDTO>> riskRelatedToSubAssetTypes) {

        List<BigInteger> subAssetTypesIds = new ArrayList<>();
        Map<BigInteger, AssetTypeDTO> subAssetTypeDtoCorrespondingToIds = new HashMap<>();
        subAssetTypesDto.forEach(subAssetTypeDto -> {
            //subAssetTypesIds.add(subAssetTypeDto.getId());
           // subAssetTypeDtoCorrespondingToIds.put(subAssetTypeDto.getId(), subAssetTypeDto);
        });
        List<AssetType> subAssetTypesList = assetTypeMongoRepository.findAllAssetTypeByCountryIdAndIds(countryId, subAssetTypesIds);
        subAssetTypesList.forEach(subAssetType -> {
            AssetTypeDTO subAssetTypeDto = subAssetTypeDtoCorrespondingToIds.get(subAssetType.getId());
            riskRelatedToSubAssetTypes.put(subAssetType, subAssetTypeDto.getRisks());
            subAssetType.setName(subAssetTypeDto.getName());
        });
        return subAssetTypesList;
    }


    /**
     * @param countryId
     * @param
     * @return return list of Asset types with sub Asset types if exist and if sub asset not exist then return empty array
     */
    public List<AssetTypeRiskResponseDTO> getAllAssetTypeWithSubAssetTypeAndRisk(Long countryId) {
        return assetTypeMongoRepository.getAllAssetTypeWithSubAssetTypeAndRiskByCountryId(countryId);
    }


    /**
     * @param countryId
     * @param
     * @return return Asset types with sub Asset types if exist and if sub asset not exist then return empty array
     */
    public AssetType getAssetTypeById(Long countryId, BigInteger id) {
        AssetType assetType = assetTypeMongoRepository.findByCountryIdAndId(countryId, id);
        if (!Optional.ofNullable(assetType).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.assetType", id);
        }
        return assetType;

    }


    public Boolean deleteAssetType(Long countryId, BigInteger assetTypeId) {

        List<MasterAsset> masterAssetsLinkedWithAssetType = masterAssetMongoRepository.findAllByCountryIdAndAssetTypeId(countryId, assetTypeId);
        if (CollectionUtils.isNotEmpty(masterAssetsLinkedWithAssetType)) {
            exceptionService.invalidRequestException("message.metaData.linked.with.asset", "message.assetType", new StringBuilder(masterAssetsLinkedWithAssetType.stream().map(MasterAsset::getName).map(String::toString).collect(Collectors.joining(","))));
        }
        assetTypeMongoRepository.safeDeleteById(assetTypeId);
        return true;

    }


    /**
     * @param countryId
     * @param
     * @param assetTypeId  id of Asset Type to which Sub Asset Types Link.
     * @param assetTypeDto asset type Dto contain list of Existing sub Asset types which need to be update and New SubAsset Types  which we need to create and add to asset afterward.
     * @return Asset Type with updated Sub Asset and new Sub Asset Types
     * @throws DuplicateDataException if Asset type is already present with same name .
     * @description method simply (update already exit Sub asset types if id is present)and (add create new sub asset types if id is not present in sub asset types)
     */
    public AssetTypeDTO updateAssetTypeUpdateAndCreateNewSubAssetsAndAddToAssetType(Long countryId, BigInteger assetTypeId, AssetTypeDTO assetTypeDto) {
        AssetType assetType = assetTypeMongoRepository.findByNameAndCountryId(countryId, assetTypeDto.getName());
        if (Optional.ofNullable(assetType).isPresent() && !assetTypeId.equals(assetType.getId())) {
            exceptionService.duplicateDataException("message.duplicate", "message.assetType", assetTypeDto.getName());
        }
        assetType = assetTypeMongoRepository.findByCountryIdAndId(countryId, assetTypeId);
        if (!Optional.ofNullable(assetType).isPresent()) {
            exceptionService.duplicateDataException("message.dataNotFound", "message.assetType", assetTypeId);
        }
        assetType.setName(assetTypeDto.getName());
        Map<AssetType, List<BasicRiskDTO>> riskRelatedToAssetTypeAndSubAssetType = new HashMap<>();
        List<AssetTypeDTO> updateExistingSubAssetTypeDTOs = new ArrayList<>();
        List<AssetType> subAssetTypeList = new ArrayList<>();
        assetTypeDto.getSubAssetTypes().forEach(subAssetTypeDto -> {
            if (Optional.ofNullable(subAssetTypeDto.getId()).isPresent()) {
                updateExistingSubAssetTypeDTOs.add(subAssetTypeDto);
            } else {
                AssetType assetSubType = new AssetType(subAssetTypeDto.getName(), countryId, SuggestedDataStatus.APPROVED);
                assetSubType.setSubAssetType(true);
                riskRelatedToAssetTypeAndSubAssetType.put(assetSubType, subAssetTypeDto.getRisks());
                subAssetTypeList.add(assetSubType);
            }
        });
        riskRelatedToAssetTypeAndSubAssetType.put(assetType, assetTypeDto.getRisks());
        if (!updateExistingSubAssetTypeDTOs.isEmpty()) {
            subAssetTypeList.addAll(updateSubAssetTypes(countryId, updateExistingSubAssetTypeDTOs, riskRelatedToAssetTypeAndSubAssetType));
        }
        Map<AssetType, List<Risk>> riskRelatedToSubAssetTypeOrAssetType;
        if (CollectionUtils.isNotEmpty(riskRelatedToAssetTypeAndSubAssetType.entrySet().stream().map(Map.Entry::getValue).flatMap(List::stream).collect(Collectors.toList()))) {
            riskRelatedToSubAssetTypeOrAssetType = riskService.saveRiskAtCountryLevelOrOrganizationLevel(countryId, false, riskRelatedToAssetTypeAndSubAssetType);
            for (AssetType subAssetType : subAssetTypeList) {
                subAssetType.setRisks(riskRelatedToSubAssetTypeOrAssetType.get(subAssetType).stream().map(Risk::getId).collect(Collectors.toSet()));
            }
            if (riskRelatedToSubAssetTypeOrAssetType.containsKey(assetType))
                assetType.setRisks(riskRelatedToSubAssetTypeOrAssetType.get(assetType).stream().map(Risk::getId).collect(Collectors.toSet()));
        }
        if (!subAssetTypeList.isEmpty()) {
            assetTypeMongoRepository.saveAll(getNextSequence(subAssetTypeList));
            assetType.setSubAssetTypes(subAssetTypeList.stream().map(AssetType::getId).collect(Collectors.toSet()));
        }
        assetTypeMongoRepository.save(assetType);
        return assetTypeDto;

    }


    /**
     * @param countryId
     * @param assetTypeId
     * @param riskId
     * @return
     */
    public boolean unlinkRiskFromAssetTypeOrSubAssetTypeAndDeletedRisk(Long countryId, BigInteger assetTypeId, BigInteger riskId) {

        AssetType assetType = assetTypeMongoRepository.findByCountryIdAndId(countryId, assetTypeId);
        if (!Optional.ofNullable(assetType).isPresent()) {
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "message.assetType", assetTypeId);
        }
        assetType.getRisks().remove(riskId);
        riskMongoRepository.safeDeleteById(riskId);
        assetTypeMongoRepository.save(assetType);
        return true;
    }


    /**
     * @return
     */
    public AssetTypeBasicDTO saveSuggestedAssetTypeAndSubAssetTypeFromUnit(Long countryId, AssetTypeBasicDTO assetTypeDTO) {

        AssetType previousAssetType = assetTypeMongoRepository.findByNameAndCountryId(countryId, assetTypeDTO.getName());
        if (Optional.ofNullable(previousAssetType).isPresent()) {
            return null;
        }
        AssetType assetType = new AssetType(assetTypeDTO.getName(), countryId, SuggestedDataStatus.PENDING);
        assetType.setSuggestedDate(LocalDate.now());
        List<AssetType> subAssetTypes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(assetTypeDTO.getSubAssetTypes())) {
            for (AssetTypeBasicDTO subAssetTypeDTO : assetTypeDTO.getSubAssetTypes()) {
                AssetType subAssetType = new AssetType(subAssetTypeDTO.getName(), countryId, SuggestedDataStatus.PENDING);
                subAssetType.setSuggestedDate(LocalDate.now());
                subAssetType.setSubAssetType(true);
                subAssetTypes.add(subAssetType);
            }
        }
        if (!subAssetTypes.isEmpty()) {
            assetTypeMongoRepository.saveAll(getNextSequence(subAssetTypes));
            assetType.setSubAssetTypes(subAssetTypes.stream().map(AssetType::getId).collect(Collectors.toSet()));
        }
        assetTypeMongoRepository.save(assetType);
        assetTypeDTO.setId(assetType.getId());
        return assetTypeDTO;
    }


    /**
     * @param assetTypeDTOs check for duplicates in name of Asset types
     */
    private void checkForDuplicacyInNameOfAssetType(List<AssetTypeDTO> assetTypeDTOs) {
        List<String> names = new ArrayList<>();
        for (AssetTypeDTO assetTypeDTO : assetTypeDTOs) {
            if (names.contains(assetTypeDTO.getName().toLowerCase())) {
                exceptionService.duplicateDataException("message.duplicate", "message.assetType", assetTypeDTO.getName());
            }
            names.add(assetTypeDTO.getName().toLowerCase());
        }
    }


}
