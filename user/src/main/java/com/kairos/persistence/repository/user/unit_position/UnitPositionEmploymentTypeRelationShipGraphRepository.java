package com.kairos.persistence.repository.user.unit_position;

import com.kairos.enums.EmploymentCategory;
import com.kairos.persistence.model.user.unit_position.UnitPositionEmploymentTypeRelationShip;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_EMPLOYMENT_TYPE;

/**
 * Created by vipul on 6/4/18.
 */
@Repository
public interface UnitPositionEmploymentTypeRelationShipGraphRepository extends Neo4jBaseRepository<UnitPositionEmploymentTypeRelationShip, Long> {
    @Query("Match(unitPosition:UnitPosition) where id(unitPosition)={0} " +
            "MATCH(unitPosition)-[relation:" + HAS_EMPLOYMENT_TYPE + "]-(emp:EmploymentType) return emp as employmentType ,relation.employmentTypeCategory as employmentTypeCategory")
    UnitPositionEmploymentTypeRelationShip findEmploymentTypeWithCategoryByUnitPositionId(Long unitPositionId);

    @Query("match(positionLine:UnitPositionLine),(newEmployment:EmploymentType) where id(newEmployment)={2} AND id(positionLine)={0} "+
    "match(positionLine)-[oldRelation:HAS_EMPLOYMENT_TYPE]-(emp:EmploymentType) "+
    "detach delete oldRelation "+
    "MERGE(positionLine)-[newRelation:HAS_EMPLOYMENT_TYPE]->(newEmployment) "+
    "set newRelation.employmentTypeCategory={2} ")
    void updateEmploymentTypeInCurrentUnitPositionLine(Long positionLineId,Long newEmploymentType,EmploymentCategory newCategory);
}