package com.kairos.persistence.repository.user.country.default_data;

import com.kairos.persistence.model.access_permission.UnitModuleAccess;
import com.kairos.persistence.model.country.default_data.UnitType;
import com.kairos.persistence.model.country.default_data.UnitTypeQueryResult;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static com.kairos.persistence.model.constants.RelationshipConstants.*;

@Repository
public interface UnitTypeGraphRepository extends Neo4jBaseRepository<UnitType, Long> {
    @Query("match(country:Country)<-[:" + IN_COUNTRY + "]-(unitType:UnitType{deleted:false}) where id(country)={0} AND unitType.name =~{1} AND id(unitType)<>{2} " +
            " with count(unitType) as totalCount " +
            " RETURN CASE WHEN totalCount>0 THEN TRUE ELSE FALSE END as result")
    Boolean checkUnitTypeExistInCountry(Long countryId, String name, Long currentUnitTypeId);

    @Query("match(country:Country)<-[:" + IN_COUNTRY + "]-(unitType:UnitType{deleted:false}) where id(country)={0} " +
            "OPTIONAL MATCH(unitType)-[:" + HAS_ACCESS_Of_MODULE + "]-(accessPage:AccessPage{isModule:true}) " +
            "RETURN id(unitType) as id,unitType.name as name,unitType.description as description,collect(accessPage) as modules")
    List<UnitTypeQueryResult> getAllUnitTypeOfCountry(Long countryId);

    @Query("MATCH(unitType:UnitType{deleted:false}) where id(unitType) IN {0} " +
            "RETURN unitType")
    List<UnitType> getUnitTypeByIds(Set<Long> unitTypeIds);

    @Query("match(unit:Organization)-[:" + HAS_UNIT_TYPE + "]->(unitType:UnitType{deleted:false}) where id(unit) IN {0} " +
            "MATCH(unitType)-[:" + HAS_ACCESS_Of_MODULE + "]-(accessPage:AccessPage{isModule:true}) " +
            "RETURN id(unit) as unitId,collect(id(accessPage)) as accessibleModules")
    List<UnitModuleAccess> getAccessibleModulesByUnits(List<Long> unitIds);
}
