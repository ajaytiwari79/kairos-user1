package com.kairos.persistance.repository.data_inventory.asset;

import com.kairos.persistance.model.data_inventory.asset.Asset;
import com.kairos.response.dto.data_inventory.AssetResponseDTO;

import java.math.BigInteger;
import java.util.List;

public interface CustomAssetRepository {

    Asset findByName(Long countryid, Long organizationId, String name);

    AssetResponseDTO findAssetWithMetaDataById(Long countryId, Long organizationId, BigInteger id);

    List<AssetResponseDTO> findAllAssetWithMetaData(Long countryId, Long organizationId);


}
