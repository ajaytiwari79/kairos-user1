package com.kairos.persistence.repository.user.position;


import com.kairos.persistence.model.user.position.PositionCode;
import org.springframework.data.neo4j.annotation.Query;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;

import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_POSITION_NAME;

/**
 * Created by pawanmandhan on 27/7/17.
 */


public interface PositionNameGraphRepository extends Neo4jBaseRepository<PositionCode,Long> {


    @Query("MATCH (o:Organization)-[:"+HAS_POSITION_NAME+"]->(pn:PositionCode{ isEnabled:true }) WHERE id(o)={0} AND pn.name=~ {1} return pn ")
    PositionCode checkDuplicatePositionName(long orgId, String positionName);

    @Query("MATCH (o:Organization)-[:"+HAS_POSITION_NAME+"]->(pn:PositionCode{ isEnabled:true }) WHERE id(o)={0} AND id(pn)= {1} return pn ")
    PositionCode getPositionNameByUnitIdAndId(long orgId, long positionNameId);


}
