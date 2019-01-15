package com.kairos.persistence.repository.master_data.asset_management.org_security_measure;


import com.kairos.enums.gdpr.SuggestedDataStatus;
import com.kairos.persistence.model.master_data.default_asset_setting.OrganizationalSecurityMeasureMD;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.CustomGenericRepository;
import com.kairos.response.dto.common.OrganizationalSecurityMeasureResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Repository
public interface OrganizationalSecurityMeasureRepository extends CustomGenericRepository<OrganizationalSecurityMeasureMD> {

    @Query(value = "SELECT new com.kairos.response.dto.common.OrganizationalSecurityMeasureResponseDTO(osm.id, osm.name, osm.organizationId, osm.suggestedDataStatus, osm.suggestedDate) FROM OrganizationalSecurityMeasureMD osm WHERE osm.countryId = ?1 and osm.deleted = false order by createdAt desc")
    List<OrganizationalSecurityMeasureResponseDTO> findAllByCountryIdAndSortByCreatedDate(Long countryId);

    @Query(value = "SELECT new com.kairos.response.dto.common.OrganizationalSecurityMeasureResponseDTO(osm.id, osm.name, osm.organizationId, osm.suggestedDataStatus, osm.suggestedDate) FROM OrganizationalSecurityMeasureMD osm WHERE osm.organizationId = ?1 and osm.deleted = false order by createdAt desc")
    List<OrganizationalSecurityMeasureResponseDTO> findAllByOrganizationIdAndSortByCreatedDate(Long orgId);

}
