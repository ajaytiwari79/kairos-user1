package com.kairos.service.master_data.asset_management;



import com.kairos.commons.custom_exception.DataNotFoundByIdException;
import com.kairos.commons.custom_exception.DuplicateDataException;
import com.kairos.commons.custom_exception.InvalidRequestException;
import com.kairos.enums.gdpr.SuggestedDataStatus;
import com.kairos.dto.gdpr.metadata.OrganizationalSecurityMeasureDTO;
import com.kairos.persistence.model.master_data.default_asset_setting.OrganizationalSecurityMeasureMD;
import com.kairos.persistence.repository.master_data.asset_management.org_security_measure.OrganizationalSecurityMeasureRepository;
import com.kairos.response.dto.common.OrganizationalSecurityMeasureResponseDTO;
import com.kairos.service.exception.ExceptionService;
import com.kairos.utils.ComparisonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.constants.AppConstant.EXISTING_DATA_LIST;
import static com.kairos.constants.AppConstant.NEW_DATA_LIST;

@Service
public class OrganizationalSecurityMeasureService{

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationalSecurityMeasureService.class);

    @Inject
    private ExceptionService exceptionService;

    @Inject
    OrganizationalSecurityMeasureRepository organizationalSecurityMeasureRepository;


    /**
     * @param countryId
     * @param
     * @param securityMeasureDTOS
     * @return return map which contain list of new OrganizationalSecurityMeasure and list of existing OrganizationalSecurityMeasure if OrganizationalSecurityMeasure already exist
     * @description this method create new OrganizationalSecurityMeasure if OrganizationalSecurityMeasure not exist with same name ,
     * and if exist then simply add  OrganizationalSecurityMeasure to existing list and return list ;
     * findMetaDataByNamesAndCountryId()  return list of existing OrganizationalSecurityMeasure using collation ,used for case insensitive result
     */
    public Map<String, List<OrganizationalSecurityMeasureMD>> createOrganizationalSecurityMeasure(Long countryId, List<OrganizationalSecurityMeasureDTO> securityMeasureDTOS, boolean isSuggestion) {
        //TODO still need to optimize we can get name of list in string from here
        Map<String, List<OrganizationalSecurityMeasureMD>> result = new HashMap<>();
        Set<String> orgSecurityMeasureNames = new HashSet<>();
        if (!securityMeasureDTOS.isEmpty()) {
            for (OrganizationalSecurityMeasureDTO securityMeasure : securityMeasureDTOS) {
                orgSecurityMeasureNames.add(securityMeasure.getName());
            }
            List<String> nameInLowerCase = orgSecurityMeasureNames.stream().map(String::toLowerCase)
                    .collect(Collectors.toList());

            //TODO still need to update we can return name of list from here and can apply removeAll on list
            List<OrganizationalSecurityMeasureMD> existing = organizationalSecurityMeasureRepository.findByCountryIdAndDeletedAndNameIn(countryId, false, nameInLowerCase);
            orgSecurityMeasureNames = ComparisonUtils.getNameListForMetadata(existing, orgSecurityMeasureNames);
            List<OrganizationalSecurityMeasureMD> newOrgSecurityMeasures = new ArrayList<>();
            if (!orgSecurityMeasureNames.isEmpty()) {
                for (String name : orgSecurityMeasureNames) {
                    OrganizationalSecurityMeasureMD newOrganizationalSecurityMeasure = new OrganizationalSecurityMeasureMD(name,countryId);
                        if(isSuggestion){
                            newOrganizationalSecurityMeasure.setSuggestedDataStatus(SuggestedDataStatus.PENDING);
                            newOrganizationalSecurityMeasure.setSuggestedDate(LocalDate.now());
                        }else {
                            newOrganizationalSecurityMeasure.setSuggestedDataStatus(SuggestedDataStatus.APPROVED);
                        }
                    newOrgSecurityMeasures.add(newOrganizationalSecurityMeasure);

                }
                newOrgSecurityMeasures = organizationalSecurityMeasureRepository.saveAll(newOrgSecurityMeasures);
            }
            result.put(EXISTING_DATA_LIST, existing);
            result.put(NEW_DATA_LIST, newOrgSecurityMeasures);
            return result;
        } else
            throw new InvalidRequestException("list cannot be empty");


    }

    /**
     * @param countryId
     * @param
     * @return list of OrganizationalSecurityMeasure
     */
    public List<OrganizationalSecurityMeasureResponseDTO> getAllOrganizationalSecurityMeasure(Long countryId) {
        return organizationalSecurityMeasureRepository.findAllByCountryIdAndSortByCreatedDate(countryId);
    }


    /**
     * @param countryId
     * @param
     * @param id        id of OrganizationalSecurityMeasure
     * @return OrganizationalSecurityMeasure object fetch via id
     * @throws DataNotFoundByIdException throw exception if OrganizationalSecurityMeasure not exist for given id
     */
    public OrganizationalSecurityMeasureMD getOrganizationalSecurityMeasure(Long countryId, Long id) {

        OrganizationalSecurityMeasureMD exist = organizationalSecurityMeasureRepository.findByIdAndCountryIdAndDeleted(id, countryId, false);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("No data found");
        } else {
            return exist;

        }
    }


    public Boolean deleteOrganizationalSecurityMeasure(Long countryId, Long id) {
        Integer resultCount = organizationalSecurityMeasureRepository.deleteByIdAndCountryId(id, countryId);
        if (resultCount > 0) {
            LOGGER.info("Organizational Security Measure deleted successfully for id :: {}", id);
        }else{
            throw new DataNotFoundByIdException("No data found");
        }
        return true;

    }

    /**
     * @param countryId
     * @param
     * @param id                 id of OrganizationalSecurityMeasure
     * @param securityMeasureDTO
     * @return return updated OrganizationalSecurityMeasure object
     * @throws DuplicateDataException if OrganizationalSecurityMeasure not exist for given id
     */
    public OrganizationalSecurityMeasureDTO updateOrganizationalSecurityMeasure(Long countryId, Long id, OrganizationalSecurityMeasureDTO securityMeasureDTO) {

        //TODO What actually this code is doing?
        OrganizationalSecurityMeasureMD orgSecurityMeasure = organizationalSecurityMeasureRepository.findByCountryIdAndDeletedAndName(countryId, false, securityMeasureDTO.getName());
        if (Optional.ofNullable(orgSecurityMeasure).isPresent()) {
            if (id.equals(orgSecurityMeasure.getId())) {
                return securityMeasureDTO;
            }
            throw new DuplicateDataException("data exist of " + securityMeasureDTO.getName());
        }

        Integer resultCount =  organizationalSecurityMeasureRepository.updateMasterMetadataName(securityMeasureDTO.getName(), id, countryId);
        if(resultCount <=0){
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "Organizational Security Measure", id);
        }else{
            LOGGER.info("Data updated successfully for id : {} and name updated name is : {}", id, securityMeasureDTO.getName());
        }
        return securityMeasureDTO;

    }


    /**
     * @description method save Organizational security measure suggested by unit
     * @param countryId
     * @param organizationalSecurityMeasureDTOS
     * @return
     */
    public List<OrganizationalSecurityMeasureMD> saveSuggestedOrganizationalSecurityMeasuresFromUnit(Long countryId, List<OrganizationalSecurityMeasureDTO> organizationalSecurityMeasureDTOS) {
        Map<String, List<OrganizationalSecurityMeasureMD>> result = createOrganizationalSecurityMeasure(countryId, organizationalSecurityMeasureDTOS, true);
        return result.get(NEW_DATA_LIST);
    }


    /**
     *
     * @param countryId
     * @param orgSecurityMeasureIds
     * @param suggestedDataStatus
     * @return
     */
    public List<OrganizationalSecurityMeasureMD> updateSuggestedStatusOfOrganizationalSecurityMeasures(Long countryId, Set<Long> orgSecurityMeasureIds, SuggestedDataStatus suggestedDataStatus) {

        Integer updateCount = organizationalSecurityMeasureRepository.updateMetadataStatus(countryId, orgSecurityMeasureIds, suggestedDataStatus);
        if(updateCount > 0){
            LOGGER.info("Organizational Security Measures are updated successfully with ids :: {}", orgSecurityMeasureIds);
        }else{
            exceptionService.dataNotFoundByIdException("message.dataNotFound", "Organizational Security Measure", orgSecurityMeasureIds);
        }
        return organizationalSecurityMeasureRepository.findAllByIds(orgSecurityMeasureIds);
    }



}
