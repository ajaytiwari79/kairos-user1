package com.kairos.persistence.repository.user.country;

import com.kairos.persistence.model.country.default_data.ContractType;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.kairos.persistence.model.constants.RelationshipConstants.BELONGS_TO;

/**
 * Created by oodles on 9/1/17.
 */
@Repository
public interface ContractTypeGraphRepository extends Neo4jBaseRepository<ContractType,Long>{

    @Override
    @Query("MATCH (ct:ContractType {isEnabled:true}) return ct")
    List<ContractType> findAll();

    @Query("MATCH (country:Country)-[:" +BELONGS_TO+ "]-(contractType:ContractType {isEnabled:true}) where id(country)={0} " +
            " RETURN contractType")
    List<ContractType> findContractTypeByCountry(long countryId);

    @Query("MATCH(country:Country)<-[:" + BELONGS_TO + "]-(contractType:ContractType {isEnabled:true}) WHERE id(country)={0} AND id(contractType)<>{3} AND (contractType.name =~{1} OR contractType.code={2}) " +
            " WITH count(contractType) as totalCount " +
            " RETURN CASE WHEN totalCount>0 THEN TRUE ELSE FALSE END as result")
    Boolean contractTypeExistInCountryByNameOrCode(Long countryId, String name, int code, Long currentVatTypeId);
}
