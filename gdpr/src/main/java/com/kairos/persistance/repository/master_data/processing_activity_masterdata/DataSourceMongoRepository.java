package com.kairos.persistance.repository.master_data.processing_activity_masterdata;


import com.kairos.persistance.model.master_data.default_proc_activity_setting.DataSource;
import com.kairos.response.dto.common.DataSourceResponseDTO;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@Repository
@JaversSpringDataAuditable
public interface DataSourceMongoRepository extends MongoRepository<DataSource,BigInteger> {

    @Query("{countryId:?0,_id:?1,deleted:false}")
    DataSource findByIdAndNonDeleted(Long countryId,BigInteger id);

    @Query("{countryId:?0,name:?1,deleted:false}")
    DataSource findByNameAndCountryId(Long countryId,String name);

    @Query("{_id:{$in:?0}}")
    List<DataSource> dataSourceList(List<BigInteger> ids);

    DataSource findByid(BigInteger id);

    @Query("{countryId:?0,deleted:false}")
    List<DataSource> findAllDataSources(Long countryId);

    @Query("{countryId:?0,name:{$in:?1},deleted:false}")
    List<DataSource>  findByCountryAndNameList(Long countryId,Set<String> name);

    @Query("{organizationId:?0,deleted:false}")
    List<DataSourceResponseDTO> findAllOrganizationDataSources(Long organizationId);

    @Query("{organizationId:?0,_id:?2,deleted:false}")
    DataSource findByOrganizationIdAndId(Long organizationId,BigInteger id);

    @Query("{organizationId:?0,name:?1,deleted:false}")
    DataSource findByNameAndOrganizationId(Long organizationId,String name);

}
