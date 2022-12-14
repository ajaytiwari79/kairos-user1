package com.kairos.service.organization;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.gdpr.filter.FilterAndFavouriteFilterDTO;
import com.kairos.dto.gdpr.filter.FilterAttributes;
import com.kairos.dto.gdpr.filter.FilterResponseDTO;
import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.gdpr.FilterType;
import com.kairos.persistence.model.access_permission.StaffAccessGroupQueryResult;
import com.kairos.persistence.model.common.QueryResult;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.organization.OrganizationBaseEntity;
import com.kairos.persistence.model.organization.Unit;
import com.kairos.persistence.model.query_wrapper.OrganizationWrapper;
import com.kairos.persistence.repository.organization.OrganizationGraphRepository;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import com.kairos.persistence.repository.user.auth.UserGraphRepository;
import com.kairos.service.access_permisson.AccessPageService;
import com.kairos.service.tree_structure.TreeStructureService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.constants.AppConstants.*;


/**
 * Created by prabjot on 19/5/17.
 */
@Transactional
@Service
public class OrganizationHierarchyService {

    @Inject
    private UnitGraphRepository unitGraphRepository;
    @Inject
    private OrganizationGraphRepository organizationGraphRepository;
    @Inject
    private TreeStructureService treeStructureService;
    @Inject
    private UserGraphRepository userGraphRepository;
    @Inject
    private AccessPageService accessPageService;
    @Inject
    private OrganizationService organizationService;

    //@Cacheable(value = "generateHierarchy", key = "#userId", cacheManager = "cacheManager")
    public List<QueryResult> generateHierarchy(Long userId) {
        List<OrganizationWrapper> organizationWrappers = userGraphRepository.getOrganizations(userId);
        boolean isHubMember = accessPageService.isHubMember(userId);
        List<QueryResult> queryResults = new ArrayList<>();
        for (OrganizationWrapper organizationWrapper : organizationWrappers) {
            Organization hierarchy = organizationGraphRepository.generateHierarchy(organizationWrapper.getId()).get(0);
            QueryResult orgHierarchy = setUnitPermission(hierarchy, isHubMember);
            queryResults.add(orgHierarchy);
        }
        return queryResults;
    }


    public QueryResult generateOrganizationHierarchyByFilter() {
        //TODO need to fix the complete query as per the current structure, currently filters won't work
       return generateHierarchy(UserContext.getUserDetails().getId()).get(0);
    }


    /**
     * @param unitId
     * @return
     */
    public FilterAndFavouriteFilterDTO getOrganizationHierarchyFilters(long unitId) {
        Organization parent = organizationService.fetchParentOrganization(unitId);
        FilterAndFavouriteFilterDTO filterAndFavouriteFilter = new FilterAndFavouriteFilterDTO();
        Map<String, Object> filterTypeDataMap = unitGraphRepository.getFiltersByParentOrganizationId(parent.getId());
        List<FilterResponseDTO> filterResponseDTOList = new ArrayList<>();
        for (String filterType : filterTypeDataMap.keySet()) {
            FilterResponseDTO filterResponseDTO = new FilterResponseDTO();
            setDetails(filterType, filterResponseDTO);
            List<FilterAttributes> filterAttributes = ObjectMapperUtils.copyCollectionPropertiesByMapper((List<Map>) filterTypeDataMap.get(filterType), FilterAttributes.class);
            filterResponseDTO.setFilterData(filterAttributes);
            filterResponseDTOList.add(filterResponseDTO);
        }
        filterAndFavouriteFilter.setAllFilters(filterResponseDTOList);
        return filterAndFavouriteFilter;
    }

    private void setDetails(String filterType, FilterResponseDTO filterResponseDTO) {
        switch (filterType) {
            case ORGANIZATION_TYPES:
                filterResponseDTO.setDisplayName(FilterType.ORGANIZATION_TYPES.value);
                filterResponseDTO.setName(FilterType.ORGANIZATION_TYPES);
                break;
            case ORGANIZATION_SUB_TYPES:
                filterResponseDTO.setDisplayName(FilterType.ORGANIZATION_SUB_TYPES.value);
                filterResponseDTO.setName(FilterType.ORGANIZATION_SUB_TYPES);
                break;
            case ORGANIZATION_SERVICES:
                filterResponseDTO.setDisplayName(FilterType.ORGANIZATION_SERVICES.value);
                filterResponseDTO.setName(FilterType.ORGANIZATION_SERVICES);
                break;
            case ORGANIZATION_SUB_SERVICES:
                filterResponseDTO.setDisplayName(FilterType.ORGANIZATION_SUB_SERVICES.value);
                filterResponseDTO.setName(FilterType.ORGANIZATION_SUB_SERVICES);
                break;
            case ACCOUNT_TYPES:
                filterResponseDTO.setDisplayName(FilterType.ACCOUNT_TYPES.value);
                filterResponseDTO.setName(FilterType.ACCOUNT_TYPES);
                break;
            default:
        }
    }

    private QueryResult setUnitPermission(OrganizationBaseEntity organizationHierarchy, boolean countryAdmin) {
        Set<Long> organizationIds = new HashSet<>();
        List<StaffAccessGroupQueryResult> staffAccessGroupQueryResults = accessPageService.getAccessPermission(UserContext.getUserDetails().getId(), getAllUnitIds(organizationHierarchy, organizationIds));
        Map<Long, Boolean> unitPermissionMap = staffAccessGroupQueryResults.stream().collect(Collectors.toMap(StaffAccessGroupQueryResult::getUnitId, StaffAccessGroupQueryResult::isHasPermission));
        QueryResult data = ObjectMapperUtils.copyPropertiesByMapper(organizationHierarchy, QueryResult.class);
        boolean hub = ((organizationHierarchy instanceof Organization) && ((Organization) organizationHierarchy).isKairosHub());
        Long hubId=unitGraphRepository.getHubIdByOrganizationId(organizationHierarchy.getId());
        setPermission(data, unitPermissionMap, countryAdmin, hub ? 0 : 1,hubId,(organizationHierarchy instanceof Unit));
        return data;
    }

    private Set<Long> getAllUnitIds(OrganizationBaseEntity organizationHierarchy, Set<Long> organizationIds) {
        if (organizationHierarchy instanceof Organization) {
            ((Organization) organizationHierarchy).getChildren().forEach(organization -> {
                organizationIds.add(organization.getId());
                getAllUnitIds(organization, organizationIds);
            });

            ((Organization) organizationHierarchy).getUnits().forEach(unit -> {
                organizationIds.add(unit.getId());
                getAllUnitIds(unit, organizationIds);
            });
        }
        organizationIds.add(organizationHierarchy.getId());
        return organizationIds;
    }

    private void setPermission(QueryResult queryResult, Map<Long, Boolean> unitPermissionMap, boolean countryAdmin, int hubCount,Long hubId,boolean isUnit) {
        queryResult.setAccessable(true);
        queryResult.setKairosHub((++hubCount == 1));
        queryResult.setUnion(queryResult.isUnion());
        queryResult.setHubId(hubId);
        queryResult.setUnit(isUnit);
        queryResult.setHasPermission(countryAdmin ? true : unitPermissionMap.get(queryResult.getId()));
        queryResult.setEnabled(true);
        Iterator iterator=queryResult.getUnits().iterator();
        while (iterator.hasNext()){
            QueryResult unit= (QueryResult) iterator.next();
            if(!countryAdmin &&!unitPermissionMap.get(unit.getId())){
                iterator.remove();
            } else {
                setPermission(unit, unitPermissionMap, countryAdmin, hubCount,hubId,true);
            }

        }
        for (QueryResult organization : queryResult.getChildren()) {
            setPermission(organization, unitPermissionMap, countryAdmin, hubCount,hubId,false);
        }


    }
}
