package com.kairos.persistence.repository.organization;
/*
 *Created By Pavan on 30/5/19
 *
 */

import com.kairos.persistence.model.organization.OrganizationBaseEntity;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

import static com.kairos.persistence.model.constants.RelationshipConstants.BELONGS_TO;

@Repository
public interface OrganizationBaseRepository extends Neo4jBaseRepository<OrganizationBaseEntity,Long> {
    @Override
    OrganizationBaseEntity findOne(Long id);

    @Query("MATCH(n{deleted:false}) where id(n)={0} " +
            "OPTIONAL MATCH(n)-[r:"+BELONGS_TO+"]-(country:Country) n,r,country")
    OrganizationBaseEntity findOneById(Long id);
}
