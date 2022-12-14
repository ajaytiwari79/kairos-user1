package com.kairos.persistence.repository.user.employment;

import com.kairos.enums.employment_type.EmploymentCategory;
import com.kairos.persistence.model.user.employment.EmploymentLineEmploymentTypeRelationShip;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_EMPLOYMENT_TYPE;

/**
 * Created by vipul on 6/4/18.
 */
@Repository
public interface EmploymentAndEmploymentTypeRelationShipGraphRepository extends Neo4jBaseRepository<EmploymentLineEmploymentTypeRelationShip, Long> {

    @Query("MATCH(employmentLine:EmploymentLine),(newEmployment:EmploymentType) WHERE id(newEmployment)={1} AND id(employmentLine)={0} "+
    "MATCH(employmentLine)-[oldRelation:"+HAS_EMPLOYMENT_TYPE+"]-(emp:EmploymentType) "+
    "DETACH DELETE oldRelation "+
    "MERGE(employmentLine)-[newRelation:"+HAS_EMPLOYMENT_TYPE+"]->(newEmployment) "+
    "set newRelation.employmentTypeCategory={2} ")
    void updateEmploymentTypeInCurrentEmploymentLine(Long employmentLineId, Long newEmploymentType, EmploymentCategory newCategory);

    @Query("MATCH(employmentLine:EmploymentLine)-[rel:"+HAS_EMPLOYMENT_TYPE+"]-(empType:EmploymentType) WHERE ID(employmentLine)={0} return employmentLine,rel,empType" )
    EmploymentLineEmploymentTypeRelationShip findByEmploymentLineId(Long employmentLineId);

}