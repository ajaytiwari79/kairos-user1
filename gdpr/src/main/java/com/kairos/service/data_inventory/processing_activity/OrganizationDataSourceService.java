package com.kairos.service.data_inventory.processing_activity;

import com.kairos.commons.custom_exception.DataNotFoundByIdException;
import com.kairos.commons.custom_exception.DuplicateDataException;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.gdpr.metadata.DataSourceDTO;
import com.kairos.persistence.model.master_data.default_proc_activity_setting.DataSource;
import com.kairos.persistence.repository.data_inventory.processing_activity.ProcessingActivityRepository;
import com.kairos.persistence.repository.master_data.processing_activity_masterdata.data_source.DataSourceRepository;
import com.kairos.response.dto.common.DataSourceResponseDTO;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.master_data.processing_activity_masterdata.DataSourceService;
import com.kairos.utils.ComparisonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.kairos.constants.GdprMessagesConstants.MESSAGE_DATASOURCE;


@Service
public class OrganizationDataSourceService{


    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationDataSourceService.class);

    @Inject
    private ExceptionService exceptionService;

    @Inject
    private DataSourceRepository dataSourceRepository;

    @Inject
    private DataSourceService dataSourceService;

    @Inject
    private ProcessingActivityRepository processingActivityRepository;


    /**
     * @param unitId
     * @param dataSourceDTOS
     * @return return map which contain list of new DataSource and list of existing DataSource if DataSource already exist
     * @description this method create new DataSource if DataSource not exist with same name ,
     * and if exist then simply add  DataSource to existing list and return list ;
     * findMetaDataByNamesAndCountryId()  return list of existing DataSource using collation ,used for case insensitive result
     */
    public List<DataSourceDTO> createDataSource(Long unitId, List<DataSourceDTO> dataSourceDTOS) {
        Set<String> existingDataSourceNames = dataSourceRepository.findNameByOrganizationIdAndDeleted(unitId);
        Set<String> dataSourceNames = ComparisonUtils.getNewMetaDataNames(dataSourceDTOS,existingDataSourceNames );
            List<DataSource> dataSources = new ArrayList<>();
            if (!dataSourceNames.isEmpty()) {
                for (String name : dataSourceNames) {
                    DataSource dataSource = new DataSource(name);
                    dataSource.setOrganizationId(unitId);
                    dataSources.add(dataSource);

                }

               dataSourceRepository.saveAll(dataSources);
            }
          return ObjectMapperUtils.copyCollectionPropertiesByMapper(dataSources,DataSourceDTO.class);
    }

    /**
     * @param unitId
     * @return list of DataSource
     */
    public List<DataSourceResponseDTO> getAllDataSource(Long unitId) {
        return dataSourceRepository.findAllByOrganizationIdAndSortByCreatedDate(unitId);
    }

    /**
     * @param unitId
     * @param id             id of DataSource
     * @return DataSource object fetch by given id
     * @throws DataNotFoundByIdException throw exception if DataSource not found for given id
     */
    public DataSource getDataSource(Long unitId, Long id) {

        DataSource exist = dataSourceRepository.findByIdAndOrganizationIdAndDeletedFalse( id, unitId);
        if (!Optional.ofNullable(exist).isPresent()) {
            throw new DataNotFoundByIdException("data not exist for id ");
        }
        return exist;
    }


    public Boolean deleteDataSource(Long unitId, Long dataSourceId) {

        List<String>  processingActivitiesLinkedWithDataSource = processingActivityRepository.findAllProcessingActivityLinkedWithDataSource(unitId, dataSourceId);
        if (!processingActivitiesLinkedWithDataSource.isEmpty()) {
            exceptionService.metaDataLinkedWithProcessingActivityException("message.metaData.linked.with.ProcessingActivity", MESSAGE_DATASOURCE, StringUtils.join(processingActivitiesLinkedWithDataSource, ','));
        }
       dataSourceRepository.deleteByIdAndOrganizationId(dataSourceId, unitId);
        return true;
    }

    /***
     * @throws DuplicateDataException throw exception if DataSource data not exist for given id
     * @param unitId
     * @param id id of DataSource
     * @param dataSourceDTO
     * @return DataSource updated object
     */
    public DataSourceDTO updateDataSource(Long unitId, Long id, DataSourceDTO dataSourceDTO) {

        DataSource dataSource = dataSourceRepository.findByOrganizationIdAndDeletedAndName(unitId,  dataSourceDTO.getName());
        if (Optional.ofNullable(dataSource).isPresent()) {
            if (id.equals(dataSource.getId())) {
                return dataSourceDTO;
            }
            exceptionService.duplicateDataException("message.duplicate",MESSAGE_DATASOURCE,dataSource.getName());
        }
        Integer resultCount =  dataSourceRepository.updateMetadataName(dataSourceDTO.getName(), id, unitId);
        if(resultCount <=0){
            exceptionService.dataNotFoundByIdException("message.dataNotFound", MESSAGE_DATASOURCE, id);
        }else{
            LOGGER.info("Data updated successfully for id : {} and name updated name is : {}", id, dataSourceDTO.getName());
        }
        return dataSourceDTO;


    }

    public List<DataSourceDTO> saveAndSuggestDataSources(Long countryId, Long unitId, List<DataSourceDTO> dataSourceDTOS) {

        List<DataSourceDTO> result = createDataSource(unitId, dataSourceDTOS);
        dataSourceService.saveSuggestedDataSourcesFromUnit(countryId, dataSourceDTOS);
        return result;
    }


}
