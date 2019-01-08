package com.kairos.service.master_data.asset_management;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kairos.commons.custom_exception.DuplicateDataException;
import com.kairos.commons.utils.ObjectMapperUtils;
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
import com.kairos.response.dto.common.RiskBasicResponseDTO;
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
    private List<AssetTypeMD> updateSubAssetTypes(Long countryId, List<AssetTypeDTO> subAssetTypesDto, Map<AssetTypeMD, List<BasicRiskDTO>> riskRelatedToSubAssetTypes, AssetTypeMD assetTypeMD) {
        List<BasicRiskDTO> newRiskOfSubAssetType = new ArrayList<>();
        Map<Long, AssetTypeDTO> subAssetTypeDtoCorrespondingToIds = new HashMap<>();
        Map<Long, BasicRiskDTO> subAssetTypeExistingRiskDtoCorrespondingToIds = new HashMap<>();
        Map<Long, List<BasicRiskDTO>> subAssetTypeNewRiskDto = new HashMap<>();
        List<AssetTypeMD> subAssetTypes = new ArrayList<>();
        List<RiskMD> subAssetRisks = new ArrayList<>();
        subAssetTypesDto.forEach(subAssetTypeDto -> {
            if (Optional.ofNullable(subAssetTypeDto.getId()).isPresent()) {
                subAssetTypeDtoCorrespondingToIds.put(subAssetTypeDto.getId(), subAssetTypeDto);
                subAssetTypeDto.getRisks().forEach( subAssetTypeRiskDto -> {
                    if (Optional.ofNullable(subAssetTypeRiskDto.getId()).isPresent()) {
                        subAssetTypeExistingRiskDtoCorrespondingToIds.put(subAssetTypeRiskDto.getId(), subAssetTypeRiskDto);
                    }else{
                        newRiskOfSubAssetType.add(subAssetTypeRiskDto);
                    }
                });
                subAssetTypeNewRiskDto.put(subAssetTypeDto.getId(), newRiskOfSubAssetType);
            } else {
                AssetTypeMD assetSubType = new AssetTypeMD(subAssetTypeDto.getName(), countryId, SuggestedDataStatus.APPROVED);
                assetSubType.setSubAssetType(true);
                assetSubType.setAssetType(assetTypeMD);
                for(BasicRiskDTO subAssetTypeRisk : subAssetTypeDto.getRisks())
                {
                    RiskMD risk = new RiskMD(subAssetTypeRisk.getName(), subAssetTypeRisk.getDescription(), subAssetTypeRisk.getRiskRecommendation(), subAssetTypeRisk.getRiskLevel() );
                    risk.setAssetType(assetSubType);
                    subAssetRisks.add(risk);
                }
                assetSubType.setRisks(subAssetRisks);
                subAssetTypes.add(assetSubType);
            }

        });

        assetTypeMD.getSubAssetTypes().forEach(subAssetType -> {
            AssetTypeDTO subAssetTypeDto = subAssetTypeDtoCorrespondingToIds.get(subAssetType.getId());
            subAssetType.setName(subAssetTypeDto.getName());
            if(!subAssetType.getRisks().isEmpty() && !subAssetTypeExistingRiskDtoCorrespondingToIds.isEmpty()){
            subAssetType.getRisks().forEach(subAssetTypeRisk -> {
                BasicRiskDTO basicRiskDTO = subAssetTypeExistingRiskDtoCorrespondingToIds.get(subAssetTypeRisk.getId());
                subAssetTypeRisk.setName(basicRiskDTO.getName());
                subAssetTypeRisk.setDescription(basicRiskDTO.getDescription());
                subAssetTypeRisk.setRiskRecommendation(basicRiskDTO.getRiskRecommendation());
                subAssetTypeRisk.setRiskLevel(basicRiskDTO.getRiskLevel());
                subAssetTypeRisk.setAssetType(subAssetType);
            });
            }
            subAssetTypeNewRiskDto.get(subAssetType.getId()).forEach( newRisk -> {
                RiskMD risk = new RiskMD(newRisk.getName(), newRisk.getDescription(), newRisk.getRiskRecommendation(), newRisk.getRiskLevel() );
                risk.setAssetType(subAssetType);
                subAssetType.getRisks().add(risk);
            });
            subAssetTypes.add(subAssetType);
        });


        return subAssetTypes;
    }


    /**
     * @param countryId
     * @param
     * @return return list of Asset types with sub Asset types if exist and if sub asset not exist then return empty array
     */
    public List<AssetTypeRiskResponseDTO> getAllAssetTypeWithSubAssetTypeAndRisk(Long countryId) {
        List<AssetTypeMD> assetTypes = assetTypeRepository.getAllAssetTypes(countryId);
        List<AssetTypeRiskResponseDTO> assetTypesWithAllData = new ArrayList<>();
        assetTypesWithAllData = buildAssetTypeOrSubTypeResponseData(assetTypes);
        return assetTypesWithAllData;
    }

    /**
     *  THis method is used to build response of asset type and asset sub type. This method used recursion
     *  to prepare the data of asset sub type.
     *
     * @param assetTypes - It may be asset type or asset sub type.
     * @return List<AssetTypeRiskResponseDTO> - List of asset-type or Sub asset-type response DTO.
     */
    private List<AssetTypeRiskResponseDTO> buildAssetTypeOrSubTypeResponseData(List<AssetTypeMD> assetTypes){
        List<AssetTypeRiskResponseDTO> assetTypeRiskResponseDTOS = new ArrayList<>();
        for(AssetTypeMD assetType : assetTypes){
            AssetTypeRiskResponseDTO assetTypeRiskResponseDTO = new AssetTypeRiskResponseDTO();
            assetTypeRiskResponseDTO.setId(assetType.getId());
            assetTypeRiskResponseDTO.setName(assetType.getName());
            assetTypeRiskResponseDTO.setHasSubAsset(assetType.isHasSubAsset());
            if(!assetType.getRisks().isEmpty()){
                assetTypeRiskResponseDTO.setRisks(buildAssetTypeRisksResponse(assetType.getRisks()));
            }
            if(assetType.isHasSubAsset()){
                assetTypeRiskResponseDTO.setSubAssetTypes(buildAssetTypeOrSubTypeResponseData(assetType.getSubAssetTypes()));
            }
            assetTypeRiskResponseDTOS.add(assetTypeRiskResponseDTO);
        }
        return assetTypeRiskResponseDTOS;
    }

    /**
     * Description : This method is used to convert Risks of asset-type or Sub asset-type to Risk Response DTO
     *  Convert RiskMD into RiskBasicResponseDTO
     * @param risks - Risks of asset-type or Sub asset-type
     * @return List<RiskBasicResponseDTO> - List of RiskResponse DTO.
     */
    private List<RiskBasicResponseDTO> buildAssetTypeRisksResponse(List<RiskMD> risks){
        List<RiskBasicResponseDTO> riskBasicResponseDTOS = new ArrayList<>();
        for(RiskMD assetTypeRisk : risks){
            RiskBasicResponseDTO riskBasicResponseDTO = new RiskBasicResponseDTO();
            riskBasicResponseDTO.setId(assetTypeRisk.getId());
            riskBasicResponseDTO.setName(assetTypeRisk.getName());
            riskBasicResponseDTO.setDescription(assetTypeRisk.getDescription());
            riskBasicResponseDTO.setRiskRecommendation(assetTypeRisk.getRiskRecommendation());
            riskBasicResponseDTO.setRiskLevel(assetTypeRisk.getRiskLevel());
            riskBasicResponseDTO.setDaysToReminderBefore(assetTypeRisk.getDaysToReminderBefore());
            riskBasicResponseDTO.setReminderActive(assetTypeRisk.isReminderActive());
            riskBasicResponseDTOS.add(riskBasicResponseDTO);
        }
        return riskBasicResponseDTOS;
    }

    /**
     * @param countryId
     * @param
     * @return return Asset types with sub Asset types if exist and if sub asset not exist then return empty array
     */
    public AssetTypeMD getAssetTypeById(Long countryId, Long id) {
        AssetTypeMD assetType = assetTypeRepository.findByIdAndCountryIdAndDeleted(id, countryId, false);
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
    public AssetTypeDTO updateAssetTypeUpdateAndCreateNewSubAssetsAndAddToAssetType(Long countryId, Long assetTypeId, AssetTypeDTO assetTypeDto) {
        AssetTypeMD assetType = assetTypeRepository.findByNameAndCountryIdAndSubAssetType(assetTypeDto.getName(), countryId, false);
        if (Optional.ofNullable(assetType).isPresent() && !assetTypeId.equals(assetType.getId())) {
            exceptionService.duplicateDataException("message.duplicate", "message.assetType", assetTypeDto.getName());
        }
        assetType = assetTypeRepository.findByCountryIdAndId(countryId, assetTypeId, false);
        if (!Optional.ofNullable(assetType).isPresent()) {
            exceptionService.duplicateDataException("message.dataNotFound", "message.assetType", assetTypeId);
        }
        assetType.setName(assetTypeDto.getName());
        Map<AssetTypeMD, List<BasicRiskDTO>> riskRelatedToAssetTypeAndSubAssetType = new HashMap<>();
        List<AssetTypeMD> subAssetTypeList = updateSubAssetTypes(countryId, assetTypeDto.getSubAssetTypes(), riskRelatedToAssetTypeAndSubAssetType, assetType);
        assetType.setSubAssetTypes(subAssetTypeList);
        assetType = updateOrAddAssetTypeRisk(assetType,assetTypeDto);
        assetTypeRepository.save(assetType);
        return assetTypeDto;

    }

    private AssetTypeMD updateOrAddAssetTypeRisk(AssetTypeMD assetType, AssetTypeDTO assetTypeDto){
        List<BasicRiskDTO> newRisks = new ArrayList<>();
        Map<Long, BasicRiskDTO> existingRiskDtoCorrespondingToIds = new HashMap<>();
        Map<Long, List<BasicRiskDTO>> assetTypeNewRiskDto = new HashMap<>();
        assetTypeDto.getRisks().forEach( assetTypeRiskDto -> {
            if (Optional.ofNullable(assetTypeRiskDto.getId()).isPresent()) {
                existingRiskDtoCorrespondingToIds.put(assetTypeRiskDto.getId(), assetTypeRiskDto);
            }else{
                newRisks.add(assetTypeRiskDto);
            }
        });
        assetTypeNewRiskDto.put(assetType.getId(), newRisks);
        if(!assetType.getRisks().isEmpty() && ! existingRiskDtoCorrespondingToIds.isEmpty()) {
            assetType.getRisks().forEach(assetTypeRisk -> {
                BasicRiskDTO basicRiskDTO = existingRiskDtoCorrespondingToIds.get(assetTypeRisk.getId());
                assetTypeRisk.setName(basicRiskDTO.getName());
                assetTypeRisk.setDescription(basicRiskDTO.getDescription());
                assetTypeRisk.setRiskRecommendation(basicRiskDTO.getRiskRecommendation());
                assetTypeRisk.setRiskLevel(basicRiskDTO.getRiskLevel());
                assetTypeRisk.setAssetType(assetType);
                assetTypeNewRiskDto.get(assetTypeRisk.getId()).forEach(newRisk -> {
                    RiskMD risk = new RiskMD(newRisk.getName(), newRisk.getDescription(), newRisk.getRiskRecommendation(), newRisk.getRiskLevel());
                    risk.setAssetType(assetType);
                    assetType.getRisks().add(risk);
                });
            });
        }
        return  assetType;
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

        //TODO
        //assetTypeDTO.setId(assetType.getId());
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
