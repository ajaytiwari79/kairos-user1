package com.kairos.persistence.repository.user.access_profile;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import com.kairos.persistence.model.user.staff.AccessPermission;

/**
 * Created by prabjot on 3/1/17.
 */
@Repository
public interface AccessPermissionGraphRepository extends GraphRepository<AccessPermission> {

    List<AccessPermission> findAll();

    @Query("Match (organization:Organization),(staff:Staff),(accessGroup:AccessGroup),(accessPage:AccessPage) where id(organization)={0} AND id(staff)={1} AND id(accessGroup)={2} AND id(accessPage)={3} with organization,staff,accessGroup,accessPage\n" +
            "Match (organization)-[:HAS_EMPLOYMENTS]->(employment:Employment)-[:BELONGS_TO]->(staff)-[:HAS_UNIT_EMPLOYMENTS]->(unitEmployment:UnitEmployment) with unitEmployment,accessGroup,accessPage\n" +
            "Match (employment)-[:HAS_UNIT_EMPLOYMENTS]->(unitEmployment:UnitEmployment)-[:HAS_ACCESS_PERMISSION]->(accessPermission:AccessPermission)-[:HAS_ACCESS_GROUP]->(accessGroup) with accessPermission,accessPage\n" +
            "Match (accessPermission)-[r:HAS_ACCESS_PAGE_PERMISSION]->(accessPage) SET r.isRead={4},r.isWrite={5} return r")
    Map<String,Object> setPagePermissionToUser(long unitId, long staffId, long groupId, long accessPageId, boolean read, boolean write);
}
