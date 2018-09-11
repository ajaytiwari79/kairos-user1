package com.kairos.persistance.repository.master_data.processing_activity_masterdata;

import com.kairos.dto.gdpr.FilterSelection;
import com.kairos.dto.gdpr.FilterSelectionDTO;
import com.kairos.dto.gdpr.data_inventory.OrganizationMetaDataDTO;
import com.kairos.enums.FilterType;
import com.kairos.persistance.model.master_data.default_proc_activity_setting.MasterProcessingActivity;
import com.kairos.response.dto.master_data.MasterProcessingActivityResponseDTO;
import com.kairos.response.dto.master_data.MasterProcessingActivityRiskResponseDTO;
import org.springframework.data.mongodb.core.query.Criteria;

import java.math.BigInteger;
import java.util.List;

public interface CustomMasterProcessingActivity {


   MasterProcessingActivity findByName(Long countryId,Long unitId,String name);

   MasterProcessingActivityResponseDTO getMasterProcessingActivityWithSubProcessingActivity(Long countryId,Long unitId, BigInteger id);

   List<MasterProcessingActivityResponseDTO> getMasterProcessingActivityListWithSubProcessingActivity(Long countryId,Long unitId);

   List<MasterProcessingActivityResponseDTO> getMasterProcessingActivityWithFilterSelection(Long countryId,Long unitId,FilterSelectionDTO filterSelectionDto);

   Criteria buildMatchCriteria(FilterSelection filterSelection, FilterType filterType);

   List<MasterProcessingActivity> getMasterProcessingActivityByOrgTypeSubTypeCategoryAndSubCategory(Long  countryId, Long unitId, OrganizationMetaDataDTO organizationMetaDataDTO);

   List<MasterProcessingActivityRiskResponseDTO>  getAllProcessingActivityWithLinkedRisks(Long countryId,Long unitId);

   List<MasterProcessingActivityRiskResponseDTO> getAllSubProcessingActivityWithLinkedRisksByProcessingActivityId(Long countryId,Long unitId,BigInteger processingActivityId);


}
