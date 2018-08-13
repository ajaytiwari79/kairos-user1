package com.kairos.persistence.repository.user.access_permission;

import com.kairos.persistence.model.access_permission.AccessGroup;
import com.kairos.persistence.model.access_permission.AccessGroupCountQueryResult;
import com.kairos.persistence.model.access_permission.AccessGroupQueryResult;
import com.kairos.persistence.model.access_permission.AccessPage;
import com.kairos.persistence.model.staff.personal_details.Staff;
import org.springframework.data.neo4j.annotation.Query;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.kairos.constants.AppConstants.AG_COUNTRY_ADMIN;
import static com.kairos.constants.AppConstants.HAS_ACCESS_OF_TABS;
import static com.kairos.persistence.model.constants.RelationshipConstants.*;

/**
 * Created by prabjot on 9/19/16.
 */
@Repository
public interface AccessGroupRepository extends Neo4jBaseRepository<AccessGroup,Long> {

    @Query("Match(staff:Staff),(accessGroup:AccessGroup) where id(staff)={0} AND id(accessGroup) IN {1} CREATE UNIQUE (staff)-[:"+STAFF_HAS_ACCESS_GROUP+"]->(accessGroup) return staff")
    Staff assignGroupToStaff(long staffId, List<Long> accessGroupIds);

    @Query("Match (staff:Staff) where id(staff)={0} MATCH (staff)-[:"+STAFF_HAS_ACCESS_GROUP+"]->(accessGroup:AccessGroup)\n" +
            "MATCH (accessGroup)-[r:"+ACCESS_GROUP_HAS_ACCESS_TO_PAGE+"]->(accessPage:AccessPage) return {id:id(accessPage),name:accessPage.name,read:r.read,write:r.write} as data")
    List<Map<String, Object>> getAccessPermissions(long staffId);


    @Query("Match (child:Organization) where id(child)={0}\n" +
            "optional match (child)<-[:"+HAS_SUB_ORGANIZATION+"*]-(n{organizationLevel:'CITY'})\n" +
            "where not (n)<-[:"+HAS_SUB_ORGANIZATION+"]-()\n" +
            "optional Match (n)-[:"+ORGANIZATION_HAS_ACCESS_GROUPS+"]->(p:AccessGroup {name:{1}})\n" +
            "optional Match (child)-[:"+ORGANIZATION_HAS_ACCESS_GROUPS+"]->(c:AccessGroup {name:{1}})\n" +
            "RETURN\n" +
            "CASE\n" +
            "WHEN p IS NOT NULL\n" +
            "THEN p\n" +
            "ELSE c END as n")
    AccessGroup findAccessGroupByName(long organizationId, String name);

    @Query("Match (organization:Organization) where id(organization)={0}\n" +
            "Match (organization)-[:"+ORGANIZATION_HAS_ACCESS_GROUPS+"]->(accessGroup:AccessGroup{deleted:false,enabled:true}) WHERE NOT (accessGroup.name='"+AG_COUNTRY_ADMIN+"') return accessGroup")
    List<AccessGroup> getAccessGroups(long unitId);

    @Query("Match (organization:Organization) where id(organization)={0}\n" +
            "Match (organization)-[:"+ORGANIZATION_HAS_ACCESS_GROUPS+"]->(accessGroup:AccessGroup{deleted:false}) WHERE NOT (accessGroup.name='"+AG_COUNTRY_ADMIN+"') return accessGroup")
    List<AccessGroup> getAccessGroupsForUnit(long unitId);


    @Query("Match (accessGroup:AccessGroup) where id(accessGroup)={1} WITH accessGroup\n" +
            "MATCH (c:Country)-[r:"+HAS_ACCESS_FOR_ORG_CATEGORY+"]-(accessPage:AccessPage) WHERE id(c)={0} AND r.accessibleForHub=true WITH accessGroup,accessPage \n" +
            "create unique (accessGroup)-[r:"+HAS_ACCESS_OF_TABS+"{isEnabled:true, read:true, write:true}]->(accessPage) return r")
    List<Map<String,Object>> setAccessPageForHubAccessGroup(Long countryId, Long accessGroupId);

    @Query("Match (accessGroup:AccessGroup) where id(accessGroup)={1} WITH accessGroup\n" +
            "MATCH (c:Country)-[r:"+HAS_ACCESS_FOR_ORG_CATEGORY+"]-(accessPage:AccessPage) WHERE id(c)={0} AND r.accessibleForUnion=true WITH accessGroup,accessPage \n" +
            "create unique (accessGroup)-[r:"+HAS_ACCESS_OF_TABS+"{isEnabled:true, read:true, write:true}]->(accessPage) return r")
    List<Map<String,Object>> setAccessPageForUnionAccessGroup(Long countryId, Long accessGroupId);

    @Query("Match (accessGroup:AccessGroup) where id(accessGroup)={1} WITH accessGroup\n" +
            "MATCH (c:Country)-[r:"+HAS_ACCESS_FOR_ORG_CATEGORY+"]-(accessPage:AccessPage) WHERE id(c)={0} AND r.accessibleForOrganization=true WITH accessGroup,accessPage \n" +
            "create unique (accessGroup)-[r:"+HAS_ACCESS_OF_TABS+"{isEnabled:true, read:true, write:true}]->(accessPage) return r")
    List<Map<String,Object>> setAccessPageForOrganizationAccessGroup(Long countryId, Long accessGroupId);


    @Query("Match (accessGroup:AccessGroup) where id(accessGroup)={0} WITH accessGroup\n" +
            "Match (accessGroup)-[r:"+HAS_ACCESS_OF_TABS+"{isEnabled:true}]->(accessPage:AccessPage)  WITH accessPage, r \n" +
            "Match (orgAccessGroup:AccessGroup) where id(orgAccessGroup)={1} WITH orgAccessGroup,accessPage,r \n" +
            "create unique (orgAccessGroup)-[orgAccessPageRel:"+HAS_ACCESS_OF_TABS+"{isEnabled:true, read:r.read, write:r.write}]->(accessPage) return orgAccessPageRel")
    List<Map<String,Object>> setAccessPagePermissionForAccessGroup(Long countryAccessGroupId, Long orgAccessGroupId);

    @Query("Match (n:AccessPage) where id(n)={0} with n \n" +
            "OPTIONAL Match (n)-[:SUB_PAGE*]->(subPage:AccessPage)  with collect(subPage)+collect(n) as coll unwind coll as pages with distinct pages with collect(pages) as listOfPage \n" +
            "MATCH (c:Country) WHERE id(c)={1} WITH c, listOfPage\n" +
            "UNWIND listOfPage as page\n" +
            "Match (c)-[r:"+HAS_ACCESS_GROUP+"]-(accessGroup:AccessGroup) WHERE  r.organizationCategory = {2} WITH accessGroup, page\n" +
            "create unique (accessGroup)-[r:"+HAS_ACCESS_OF_TABS+"{isEnabled:true, read:true, write:true}]->(page)")
    void addAccessPageRelationshipForCountryAccessGroups(Long accessPageId, Long countryId, String organizationCategory);

    @Query("Match (n:AccessPage) where id(n)={0} with n \n" +
            "OPTIONAL Match (n)-[:SUB_PAGE*]->(subPage:AccessPage)  with collect(subPage)+collect(n) as coll unwind coll as pages with distinct pages with collect(pages) as listOfPage \n" +
            "MATCH (c:Country) WHERE id(c)={1} WITH c, listOfPage \n" +
            "UNWIND listOfPage as page \n" +
            "Match (c)-[r:"+HAS_ACCESS_GROUP+"]-(accessGroup:AccessGroup) WHERE  r.organizationCategory = {2} AND accessGroup.name<> '"+AG_COUNTRY_ADMIN+"' WITH accessGroup, page \n" +
            "MATCH (accessGroup)-[r:"+HAS_ACCESS_OF_TABS+"]->(page) DELETE r")
    void removeAccessPageRelationshipForCountryAccessGroup(Long accessPageId, Long countryId, String organizationCategory);

    @Query("Match (n:AccessPage) where id(n)={0} with n \n" +
            "OPTIONAL Match (n)-[:SUB_PAGE*]->(subPage:AccessPage)  with collect(subPage)+collect(n) as coll unwind coll as pages with distinct pages with collect(pages) as listOfPage \n" +
            "Match (org:Organization)-[:"+BELONGS_TO+"]-(c:Country) where id(c)={1} AND org.isKairosHub ={2} AND org.union={3} with org,listOfPage \n" +
            "OPTIONAL Match (org)-[:HAS_SUB_ORGANIZATION*]->(childOrg:Organization)  where childOrg.isKairosHub ={2} AND childOrg.union={3} with org+[childOrg] as allOrg,listOfPage \n" +
            "UNWIND listOfPage as page\n" +
            "UNWIND allOrg as org \n" +
            "Match (org)-[r:"+ORGANIZATION_HAS_ACCESS_GROUPS+"]-(accessGroup:AccessGroup) WITH accessGroup, page \n"+
            "create unique (accessGroup)-[r:"+HAS_ACCESS_OF_TABS+"{isEnabled:true, read:true, write:true}]->(page)")
    void addAccessPageRelationshipForOrganizationAccessGroups(Long accessPageId, Long countryId, Boolean isKairosHub, Boolean isUnion);

    @Query("Match (n:AccessPage) where id(n)={0} with n \n" +
            "OPTIONAL Match (n)-[:SUB_PAGE*]->(subPage:AccessPage)  with collect(subPage)+collect(n) as coll unwind coll as pages with distinct pages with collect(pages) as listOfPage \n" +
            "Match (org:Organization)-[:"+BELONGS_TO+"]-(c:Country) where id(c)={1} AND org.isKairosHub ={2} AND org.union={3} with org,listOfPage \n" +
            "OPTIONAL Match (org)-[:HAS_SUB_ORGANIZATION*]->(childOrg:Organization)  where childOrg.isKairosHub ={2} AND childOrg.union={3} with org+[childOrg] as allOrg,listOfPage \n" +
            "UNWIND listOfPage as page\n" +
            "UNWIND allOrg as org \n" +
            "Match (org)-[r:"+ORGANIZATION_HAS_ACCESS_GROUPS+"]-(accessGroup:AccessGroup) WHERE accessGroup.name <> '"+ AG_COUNTRY_ADMIN +"' WITH accessGroup, page \n"+
            "MATCH (accessGroup)-[r:"+HAS_ACCESS_OF_TABS+"]->(page) DELETE r")
    void removeAccessPageRelationshipForOrganizationAccessGroup(Long accessPageId, Long countryId, Boolean isKairosHub, Boolean isUnion);

    @Query("Match (n:AccessPage) where id(n)={0} with n\n" +
            "Optional Match (n)-[:" + SUB_PAGE + "*]->(subPage:AccessPage) with n+[subPage] as coll unwind coll as pages with distinct pages \n"+
            " Optional Match (pages)-[r:"+HAS_ACCESS_OF_TABS+"]-(ag:AccessGroup) WHERE id(ag) = {1} WITH r\n"+
            "set r.read={2}, r.write={3} return distinct true")
    Boolean updatePermissionsForAccessTabsAndChildrenOfAccessGroup(Long tabId, Long accessGroupId, Boolean read, Boolean write);

    @Query("Match (n:AccessPage) where id(n)={0} with n\n" +
           " Optional Match (n)-[r:"+HAS_ACCESS_OF_TABS+"]-(ag:AccessGroup) WHERE id(ag) = {1} WITH r\n"+
            "set r.read={2}, r.write={3} return distinct true")
    Boolean updatePermissionsForAccessTabOfAccessGroup(Long tabId, Long accessGroupId, Boolean read, Boolean write);

    @Query("Match (accessGroup:AccessGroup),(accessPage:AccessPage) where id(accessGroup)={0} and id(accessPage) IN {1}\n" +
            "MERGE (accessGroup)-[r:"+HAS_ACCESS_OF_TABS+"]->(accessPage)\n" +
            "ON CREATE SET r.isEnabled={2},r.creationDate={3},r.lastModificationDate={4},r.read={5},r.write={6}\n" +
            "ON MATCH SET r.isEnabled={2},r.lastModificationDate={4},r.read={5},r.write={6} return true")
    List<Map<String,Object>> updateAccessPagePermission(long accessGroupId, List<Long> pageIds, boolean isSelected, long creationDate, long lastModificationDate, Boolean read, Boolean write);




    @Query("Match (accessPage:AccessPage),(accessGroup:AccessGroup) where id(accessPage)={4} AND id(accessGroup)={3} with accessPage,accessGroup\n" +
            "optional match (accessPage)<-[:HAS_ACCESS_OF_TABS{isEnabled:true}]-(accessGroup) with accessPage, accessGroup\n" +
            "Match (n:Organization),(staff:Staff) where id(n)={0} AND id(staff)={1} with n,staff,accessPage,accessGroup\n" +
            "MATCH (n)-[:HAS_EMPLOYMENTS]->(emp:Employment)-[:BELONGS_TO]->(staff)-[:BELONGS_TO]->(user:User) with user,emp,accessPage,accessGroup\n" +
            "Match (emp)-[:HAS_UNIT_PERMISSIONS]->(unitPermission:UnitPermission)-[:APPLICABLE_IN_UNIT]->(unit:Organization) where id(unit)={2} with unitPermission,accessPage,accessGroup\n" +
            "MERGE (unitPermission)-[r:HAS_CUSTOMIZED_PERMISSION{accessGroupId:{3}}]->(accessPage) \n" +
            "ON CREATE SET r.read={5},r.write={6}\n" +
            "ON MATCH SET r.read={5},r.write={6} return distinct true")
    void setCustomPermissionForTab(long organizationId, long staffId, long unitId, long accessGroupId, long accessPageId, boolean isRead, boolean isWrite);

    @Query("Match (accessPage:AccessPage),(accessGroup:AccessGroup) where id(accessPage)={4} AND id(accessGroup)={3} with accessPage,accessGroup\n" +
            "optional match (accessPage)-[:SUB_PAGE*]->(subPage:AccessPage)<-[:HAS_ACCESS_OF_TABS{isEnabled:true}]-(accessGroup) with [subPage]+accessPage as coll,accessGroup as accessGroup\n" +
            "unwind coll as accessPage with distinct accessPage,accessGroup\n" +
            "Match (n:Organization),(staff:Staff) where id(n)={0} AND id(staff)={1} with n,staff,accessPage,accessGroup\n" +
            "MATCH (n)-[:HAS_EMPLOYMENTS]->(emp:Employment)-[:BELONGS_TO]->(staff)-[:BELONGS_TO]->(user:User) with user,emp,accessPage,accessGroup\n" +
            "Match (emp)-[:HAS_UNIT_PERMISSIONS]->(unitPermission:UnitPermission)-[:APPLICABLE_IN_UNIT]->(unit:Organization) where id(unit)={2} with unitPermission,accessPage,accessGroup\n" +
            "MATCH (accessGroup)-[:"+HAS_ACCESS_OF_TABS+"]->(accessPage) with unitPermission,accessPage,accessGroup\n" +
            "MERGE (unitPermission)-[r:HAS_CUSTOMIZED_PERMISSION{accessGroupId:{3}}]->(accessPage)\n" +
            "ON CREATE SET r.read={5},r.write={6}\n" +
            "ON MATCH SET r.read={5},r.write={6} return distinct true")
    void setCustomPermissionForChildren(long organizationId, long staffId, long unitId, long accessGroupId, long accessPageId, boolean isRead, boolean isWrite);


    @Query("Match (accessPage:AccessPage),(accessGroup:AccessGroup) where id(accessPage)={4} AND id(accessGroup)={3} with accessPage,accessGroup\n" +
//            "optional match (accessPage)-[:SUB_PAGE*]->(subPage:AccessPage)<-[:HAS_ACCESS_OF_TABS{isEnabled:true}]-(accessGroup) with accessPage+[subPage] as coll,accessGroup as accessGroup\n" +
//            "unwind coll as accessPage with distinct accessPage,accessGroup\n" +
            "Match (n:Organization),(staff:Staff) where id(n)={0} AND id(staff)={1} with n,staff,accessPage,accessGroup\n" +
            "MATCH (n)-[:HAS_EMPLOYMENTS]->(emp:Employment)-[:BELONGS_TO]->(staff)-[:BELONGS_TO]->(user:User) with user,emp,accessPage,accessGroup\n" +
            "Match (emp)-[:HAS_UNIT_PERMISSIONS]->(unitPermission:UnitPermission)-[:APPLICABLE_IN_UNIT]->(unit:Organization) where id(unit)={2} with unitPermission,accessPage,accessGroup\n" +
            "MATCH (unitPermission)-[r:HAS_CUSTOMIZED_PERMISSION]->(accessPage) WHERE r.accessGroupId ={3}\n" +
            "DELETE r")
    void deleteCustomPermissionForTab(long organizationId, long staffId, long unitId, long accessGroupId, long accessPageId);




    @Query("Match (accessPage:AccessPage),(accessGroup:AccessGroup) where id(accessPage)={4} AND id(accessGroup)={3} with accessPage,accessGroup\n" +
            "optional match (accessPage)-[:SUB_PAGE*]->(subPage:AccessPage)<-[:HAS_ACCESS_OF_TABS{isEnabled:true}]-(accessGroup) with accessPage+[subPage] as coll,accessGroup as accessGroup\n" +
            "unwind coll as accessPage with distinct accessPage,accessGroup\n" +
            "Match (n:Organization),(staff:Staff) where id(n)={0} AND id(staff)={1} with n,staff,accessPage,accessGroup\n" +
            "MATCH (n)-[:HAS_EMPLOYMENTS]->(emp:Employment)-[:BELONGS_TO]->(staff)-[:BELONGS_TO]->(user:User) with user,emp,accessPage,accessGroup\n" +
            "Match (emp)-[:HAS_UNIT_PERMISSIONS]->(unitPermission:UnitPermission)-[:APPLICABLE_IN_UNIT]->(unit:Organization) where id(unit)={2} with unitPermission,accessPage,accessGroup\n" +
            "MATCH (unitPermission)-[r:HAS_CUSTOMIZED_PERMISSION]->(accessPage) WHERE r.accessGroupId={3} \n" +
            "DELETE r")
    void deleteCustomPermissionForChildren(long organizationId, long staffId, long unitId, long accessGroupId, long accessPageId);

    @Query("Match (accessGroup:AccessGroup)-[:"+HAS_ACCESS_OF_TABS+"{isEnabled:true}]->(accessPage:AccessPage) with accessPage where id(accessGroup)={0} return accessPage")
    List<AccessPage> getAccessPageByGroup(long accessGroupId);

    @Query("Match (n:Organization)-[:ORGANIZATION_HAS_ACCESS_GROUPS]->(ag:AccessGroup{typeOfTaskGiver:true}) where id(n)={0} return ag")
    AccessGroup findTaskGiverAccessGroup(Long organizationId);

    List<AccessGroup> findAll();

    @Query("MATCH (c:Country)-[r:"+HAS_ACCESS_GROUP+"]-(a:AccessGroup{deleted:false}) WHERE id(c)={0} AND id(a)={1} AND r.organizationCategory={2} return a ")
    AccessGroup findCountryAccessGroupByIdAndCategory(Long countryId, Long accessGroupId, String orgCategory);

    @Query("MATCH (c:Country)-[r:"+HAS_ACCESS_GROUP+"]-(a:AccessGroup{deleted:false}) WHERE id(c)={0} AND id(a)={1} return a ")
    AccessGroup findCountryAccessGroupById(Long countryId, Long accessGroupId);

    @Query("MATCH (c:Country)-[r:"+HAS_ACCESS_GROUP+"]-(a:AccessGroup{deleted:false}) WHERE id(c)={0} AND LOWER(a.name) = LOWER({1}) AND r.organizationCategory={2} return a ")
    AccessGroup findCountryAccessGroupByNameAndCategory(Long countryId, String accessGroupName, String orgCategory);

    @Query("MATCH (c:Country)-[r:"+HAS_ACCESS_GROUP+"]-(a:AccessGroup{deleted:false}) WHERE id(c)={0} AND LOWER(a.name) = LOWER({1}) AND r.organizationCategory={2} return COUNT(a)>0 ")
    Boolean isCountryAccessGroupExistWithName(Long countryId, String name, String orgCategory);

    @Query("MATCH (o:Organization)-[r:"+ORGANIZATION_HAS_ACCESS_GROUPS+"]-(a:AccessGroup{deleted:false}) WHERE id(o)={0} AND LOWER(a.name) = LOWER({1}) return COUNT(a)>0 ")
    Boolean isOrganizationAccessGroupExistWithName(Long orgId, String name);

    @Query("MATCH (c:Country)-[r:"+HAS_ACCESS_GROUP+"]-(a:AccessGroup{deleted:false}) WHERE id(c)={0} AND LOWER(a.name) = LOWER({1}) AND r.organizationCategory={2} AND NOT(id(a) = {3}) return COUNT(a)>0 ")
    Boolean isCountryAccessGroupExistWithNameExceptId(Long countryId, String name, String orgCategory, Long accessGroupId);

    @Query("MATCH (o:Organization)-[r:"+ORGANIZATION_HAS_ACCESS_GROUPS+"]-(a:AccessGroup{deleted:false}) WHERE id(o)={0} AND LOWER(a.name) = LOWER({1}) AND NOT(id(a) = {2}) return COUNT(a)>0 ")
    Boolean isOrganizationAccessGroupExistWithNameExceptId(Long orgId, String name, Long accessGroupId);

    @Query("MATCH (c:Country) WHERE id(c)={0}\n" +
            "OPTIONAL MATCH (c)-[r:"+HAS_ACCESS_GROUP+"{organizationCategory:'HUB'}]->(a:AccessGroup{deleted:false})  WITH COUNT(r) as hubCount\n" +
            "OPTIONAL MATCH (c)-[r:"+HAS_ACCESS_GROUP+"{organizationCategory:'UNION'}]->(a:AccessGroup{deleted:false})  WITH COUNT(r) as unionCount, hubCount\n" +
            "RETURN hubCount, unionCount")
    AccessGroupCountQueryResult getListOfOrgCategoryWithCountryAccessGroupCount(Long countryId);

    @Query("MATCH (c:Country)-[r:HAS_ACCESS_GROUP]->(ag:AccessGroup{deleted:false}) WHERE id(c)={0} AND r.organizationCategory={1} \n" +
            "RETURN id(ag) as id, ag.name as name, ag.description as description, ag.typeOfTaskGiver as typeOfTaskGiver, ag.deleted as deleted, ag.role as role, ag.enabled as enabled")
    List<AccessGroupQueryResult> getCountryAccessGroupByOrgCategory(Long countryId, String orgCategory);

    @Query("MATCH (c:Country)-[r:HAS_ACCESS_GROUP]->(ag:AccessGroup{deleted:false,enabled:true}) WHERE id(c)={0} AND r.organizationCategory={1} RETURN ag")
    List<AccessGroup> getCountryAccessGroupByCategory(Long countryId, String organizationCategory);

    // For Test cases
    @Query("Match (accessGroup:AccessGroup)-[:"+HAS_ACCESS_OF_TABS+"{isEnabled:true}]->(accessPage:AccessPage) with accessPage where id(accessGroup)={0} return accessPage")
    List<Long> getAccessPageIdsByAccessGroup(long accessGroupId);

    @Query("Match (accessGroup:AccessGroup)-[:"+HAS_ACCESS_OF_TABS+"{isEnabled:true}]->(accessPage:AccessPage) with accessPage where id(accessGroup)={0} return accessPage LIMIT 1")
    Long getAccessPageIdByAccessGroup(long accessGroupId);


    @Query("Match(employment:Employment)-[:" + HAS_UNIT_PERMISSIONS + "]-(unitPermission:UnitPermission) where id(employment) in {0} \n"+
            "Match(unitPermission)-[rel_has_access_group:" + HAS_ACCESS_GROUP + " ]-(ag:AccessGroup) optional Match(unitPermission)-[rel_has_customized_permission:" + HAS_CUSTOMIZED_PERMISSION + "]-" +
            "(accesspage:AccessPage) delete rel_has_access_group, rel_has_customized_permission")
    void deleteAccessGroupRelationAndCustomizedPermissionRelation(List<Long> empIds );


    @Query("MATCH (c:Country)-[r:HAS_ACCESS_GROUP]->(ag:AccessGroup{deleted:false,enabled:true}) WHERE id(c)={0} AND r.organizationCategory={1} AND id(ag)={2} \n" +
            "RETURN COUNT(ag)>0")
    boolean isCountryAccessGroupExistsByOrgCategory(Long countryId, String orgCategory, Long accessGroupId);


    @Query("Match(org:Organization) where id(org) in {0} Match(ag:AccessGroup) where id(ag)={1} \n" +
     "merge(org)-[:ORGANIZATION_HAS_ACCESS_GROUPS]-(ag)")
    void createAccessGroupUnitRelation(List<Long> orgIds, Long accessGroupId);

    @Query("MATCH (org:Organization) WHERE id(org)={0} WITH org\n" +
            "MATCH (org)-[:HAS_EMPLOYMENTS]-(employment:Employment)-[:BELONGS_TO]-(staff:Staff)-[:BELONGS_TO]->(user:User) WITH employment\n" +
            "MATCH (employment)-[:HAS_UNIT_PERMISSIONS]-(up:UnitPermission)-[:APPLICABLE_IN_UNIT]->(unit:Organization) WHERE id(unit)={1} \n" +
            "MATCH (up)-[:HAS_ACCESS_GROUP]-(ag:AccessGroup) WHERE ag.role={2} return count(ag) > 0")
    Boolean checkIfUserHasAccessByRoleInUnit(Long parentOrgId, Long unitId, String role);

    @Query("MATCH (org:Organization) WHERE id(org)={0} WITH org\n" +
            "MATCH (org)-[:HAS_EMPLOYMENTS]-(employment:Employment)-[:BELONGS_TO]-(staff:Staff) where id(staff)={3} WITH employment\n" +
            "MATCH (employment)-[:HAS_UNIT_PERMISSIONS]-(up:UnitPermission)-[:APPLICABLE_IN_UNIT]->(unit:Organization) WHERE id(unit)={1} \n" +
            "MATCH (up)-[:HAS_ACCESS_GROUP]-(ag:AccessGroup) WHERE ag.role={2} return count(ag) > 0")
    Boolean getStaffAccessRoles(Long parentOrgId, Long unitId, String role,Long staffId);

    @Query("MATCH (c:Country)-[r:HAS_ACCESS_GROUP]->(ag:AccessGroup{deleted:false, enabled:true}) WHERE id(c)={0} AND r.organizationCategory={1} AND ag.role={2}\n" +
            "RETURN id(ag) as id, ag.name as name, ag.description as description, ag.typeOfTaskGiver as typeOfTaskGiver, ag.deleted as deleted, ag.role as role")
    List<AccessGroupQueryResult> getCountryAccessGroupByOrgCategoryAndRole(Long countryId, String orgCategory, String role);

    @Query("MATCH (org:Organization)-[r:ORGANIZATION_HAS_ACCESS_GROUPS]->(ag:AccessGroup{deleted:false}) WHERE id(org)={0} AND ag.role={1}\n" +
            "RETURN id(ag) as id, ag.name as name, ag.description as description, ag.typeOfTaskGiver as typeOfTaskGiver, ag.deleted as deleted, ag.role as role")
    List<AccessGroupQueryResult> getOrganizationAccessGroupByRole(Long organizationId,  String role);

    @Query("MATCH (org:Organization)-[r:ORGANIZATION_HAS_ACCESS_GROUPS]->(ag:AccessGroup{deleted:false}) WHERE id(org)={0} AND ag.name={1} AND ag.role={2}\n" +
            "RETURN ag")
    AccessGroup getOrganizationAccessGroupByName(Long organizationId, String name, String role);




    //for test cases
    @Query("Match(emp:Employment)-[:"+HAS_UNIT_PERMISSIONS+"]-(up:UnitPermission)-[:"+HAS_ACCESS_GROUP+"]-(ag:AccessGroup) where id(emp)=8767 return id(ag)")
    Long findAccessGroupByEmploymentId(Long employmentId);


    @Query("MATCH (c:Country)-[r:"+HAS_ACCESS_GROUP+"]->(ag:AccessGroup{deleted:false})-[:"+HAS_ACCOUNT_TYPE+"]->(accountType:AccountType) WHERE id(c)={0} AND id(accountType)={1} \n" +
            "RETURN id(ag) as id, ag.name as name, ag.description as description, ag.typeOfTaskGiver as typeOfTaskGiver, ag.role as role, ag.enabled as enabled ")
    List<AccessGroupQueryResult> getCountryAccessGroupByAccountTypeId(Long countryId, Long accountTypeId);

}


