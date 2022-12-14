package com.kairos.persistence.repository.organization.default_data;

import com.kairos.persistence.model.organization.default_data.SickConfiguration;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

import static com.kairos.persistence.model.constants.RelationshipConstants.IN_UNIT;

/**
 * CreatedBy vipulpandey on 29/8/18
 **/
@Repository
public interface SickConfigurationRepository extends Neo4jBaseRepository<SickConfiguration,Long> {

    @Query("MATCH(unit:Unit) where id(unit)={0}" +
            " MATCH(unit)<-["+IN_UNIT+"]-(sickConfiguration:SickConfiguration)" +
            " return sickConfiguration")
    SickConfiguration findSickConfigurationOfUnit(Long unitId);

}
