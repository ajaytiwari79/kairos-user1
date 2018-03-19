package com.kairos.persistence.repository.user.access_permission;

import com.kairos.persistence.model.user.access_permission.AccessPage;
import com.kairos.persistence.model.user.access_permission.AccessPageDTO;
import com.kairos.persistence.model.user.access_permission.AccessPageQueryResult;
import com.kairos.persistence.model.user.auth.StaffPermissionQueryResult;
import org.springframework.data.neo4j.annotation.Query;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.kairos.constants.AppConstants.HAS_ACCESS_OF_TABS;
import static com.kairos.constants.AppConstants.MANAGE_COUNTRY_TAB_MODULE_ID;
import static com.kairos.persistence.model.constants.RelationshipConstants.*;

/**
 * Created by arvind on 24/10/16.
 */

@Repository
public interface AccessPageRepository extends Neo4jBaseRepository<AccessPage, Long> {
    @Query("Match (org:Organization) where id(org)={0}\n" +
            "match (org)-[:" + HAS_SUB_ORGANIZATION + "*]->(n)\n" +
            "Match (n)-[:HAS_GROUP]->(group:Group)-[:" + HAS_TEAM + "]->(team:Team)-[:" + TEAM_HAS_MEMBER + "]->(staff:Staff)-[:" + BELONGS_TO + "]->(user:User) where id(user)={1} with staff\n" +
            "Match (staff)-[:" + STAFF_HAS_ACCESS_GROUP + "]->(accessGroup:AccessGroup)-[r:" + ACCESS_GROUP_HAS_ACCESS_TO_PAGE + "{read:false}]->(accessPage:AccessPage{isModule:true}) return distinct accessPage")
    List<AccessPage> getAccessModulesForUnits(long parentOrganizationId, long userId);

    @Query("Match (n:UnitPermission)-[:" + HAS_ACCESS_PERMISSION + "]->(accessPermission:AccessPermission)-[r:" + HAS_ACCESS_PAGE_PERMISSION + "]->(p:AccessPage) where id(n)={0} AND id(p)={1} SET r.isRead={2} return r")
    void modifyAccessPagePermission(long unitEmploymentId, long accessPageId, boolean value);

    @Query("MATCH path=(accessPage:AccessPage{active:true})-[:" + SUB_PAGE + "*]->(subPage:AccessPage{active:true}) WITH NODES(path) AS np WITH REDUCE(s=[], i IN RANGE(0, LENGTH(np)-2, 1) | s + {p:np[i], c:np[i+1]}) AS cpairs UNWIND cpairs AS pairs WITH DISTINCT pairs AS ps with ps\n" +
            "match (accessGroup:AccessGroup) where id(accessGroup)={0} with accessGroup,ps\n" +
            "optional match (parent:AccessPage)<-[r2:" + HAS_ACCESS_OF_TABS + "]-(accessGroup)\n" +
            "where id(parent)=id(ps.p) with r2,ps,accessGroup\n" +
            "optional match (child:AccessPage)<-[r:" + HAS_ACCESS_OF_TABS + "]-(accessGroup)\n" +
            "where id(child)=id(ps.c) with r,r2,ps,accessGroup\n" +
            "return {name:ps.p.name,id:id(ps.p),selected:case when r2.isEnabled then true else false end,module:ps.p.isModule,children:collect({name:ps.c.name,id:id(ps.c),selected:case when r.isEnabled then true else false end})} as data\n" +
            "UNION\n" +
            "Match (accessPage:AccessPage{isModule:true,active:true}) where not (accessPage)-[:" + SUB_PAGE + "]->() with accessPage\n" +
            "match (accessGroup:AccessGroup) where id(accessGroup)={0} with accessGroup,accessPage\n" +
            "optional match (accessPage:AccessPage)<-[r:" + HAS_ACCESS_OF_TABS + "]-(accessGroup)\n" +
            "return {name:accessPage.name,id:id(accessPage),selected:case when r.isEnabled then true else false end,module:accessPage.isModule,children:[]} as data")
    List<Map<String, Object>> getAccessPageHierarchy(long accessGroupId);


    // Fetch access page hierarchy show only selected access page
    @Query("match (ag:AccessGroup) where id(ag)={0} WITH ag \n" +
            "MATCH path=(accessPage:AccessPage{active:true})-[:SUB_PAGE*]->(subPage:AccessPage{active:true})-[:HAS_ACCESS_OF_TABS]-(ag) \n" +
            "WITH NODES(path) AS np,ag WITH REDUCE(s=[], i IN RANGE(0, LENGTH(np)-3, 1) | s + {p:np[i], c:np[i+1]}) AS cpairs,ag UNWIND cpairs AS pairs WITH DISTINCT pairs AS ps,ag with ps,ag\n" +
            "optional match (parent:AccessPage)<-[r2:HAS_ACCESS_OF_TABS]-(ag)\n" +
            "where id(parent)=id(ps.p) with r2,ps,ag\n" +
            "optional match (child:AccessPage)<-[r:HAS_ACCESS_OF_TABS]-(ag)\n" +
            "where id(child)=id(ps.c) with r,r2,ps,ag\n" +
            "return {name:ps.p.name,id:id(ps.p),selected:case when r2.isEnabled then true else false end,module:ps.p.isModule,children:collect({name:ps.c.name,id:id(ps.c),selected:case when r.isEnabled then true else false end})} as data\n" +
            "UNION\n" +
            "match (ag:AccessGroup) where id(ag)={0} WITH ag \n" +
            "Match (accessPage:AccessPage{isModule:true,active:true}) where not (accessPage)-[:SUB_PAGE]->() with accessPage, ag\n" +
            "Match (accessPage)<-[r:HAS_ACCESS_OF_TABS]-(ag) with accessPage, ag,r\n" +
            "return {name:accessPage.name,id:id(accessPage),selected:case when r.isEnabled then true else false end,module:accessPage.isModule,children:[]} as data")
    List<Map<String, Object>> getSelectedAccessPageHierarchy(Long accessGroupId);

    @Query("Match (accessGroup:AccessGroup),(accessPermission:AccessPermission) where id(accessPermission)={0} AND id(accessGroup)={1}\n" +
            "Match (accessGroup)-[r:" + HAS_ACCESS_OF_TABS + "{isEnabled:true}]->(accessPage:AccessPage) with accessPage,r,accessPermission\n" +
            "Create unique (accessPermission)-[:" + HAS_ACCESS_PAGE_PERMISSION + "{isEnabled:r.isEnabled,isRead:true,isWrite:false}]->(accessPage) return accessPermission")
    List<AccessPage> setDefaultPermission(long accessPermissionId, long accessGroupId);

    @Query("MATCH path=(accessPage:AccessPage)-[:" + SUB_PAGE + "*]->() WITH NODES(path) AS np WITH REDUCE(s=[], i IN RANGE(0, LENGTH(np)-2, 1) | s + {p:np[i], c:np[i+1]}) AS cpairs UNWIND cpairs AS pairs WITH DISTINCT pairs AS ps with ps\n" +
            "Match (accessPermission:AccessPermission) where id(accessPermission)={0} with accessPermission,ps\n" +
            "optional match (parent:AccessPage)<-[r2:" + HAS_ACCESS_PAGE_PERMISSION + "]-(accessPermission)\n" +
            "where id(parent)=id(ps.p) with r2,ps,accessPermission\n" +
            "optional match (child:AccessPage)<-[r:" + HAS_ACCESS_PAGE_PERMISSION + "]-(accessPermission)\n" +
            "where id(child)=id(ps.c) with r,r2,ps\n" +
            "return {name:ps.p.name,id:id(ps.p),selected:r2.isEnabled,module:ps.p.isModule,children:collect({name:ps.c.name,id:id(ps.c),selected:r.isEnabled})} as data")
    List<Map<String, Object>> getStaffPermission(long accessPermissionId);

    @Query("MATCH (n:Organization)-[:HAS_EMPLOYMENTS]->(emp:Employment)-[:BELONGS_TO]->(staff:Staff)-[:BELONGS_TO]->(user:User) where id(n)={0} AND  id(staff)={2}\n" +
            "Match (emp)-[:HAS_UNIT_PERMISSIONS]->(unitPermission:UnitPermission)-[:APPLICABLE_IN_UNIT]->(unit:Organization) where id(unit)={1}\n" +
            "Match (unitPermission)-[:HAS_ACCESS_PERMISSION{isEnabled:true}]->(ap:AccessPermission)-[:HAS_ACCESS_GROUP]->(g:AccessGroup) where id(g)={3} with ap,g\n" +
            "Match (g)-[:HAS_ACCESS_OF_TABS{isEnabled:true}]->(accessPage:AccessPage{isModule:true}) with accessPage,ap,g\n" +
            "MATCH path=(accessPage)-[:SUB_PAGE*]->() WITH NODES(path) AS np,g as g,ap as ap with REDUCE(s=[], i IN RANGE(0, LENGTH(np)-2, 1) | s + {p:np[i], c:np[i+1],g:g,ap:ap}) AS cpairs UNWIND cpairs AS pairs WITH DISTINCT pairs AS ps with distinct ps.g as g,ps as ps,ps.ap as ap\n" +
            "match (child:AccessPage)<-[:HAS_ACCESS_OF_TABS{isEnabled:true}]-(g)\n" +
            "where id(child)=id(ps.c) with ps,child,g,ap\n" +
            "match (parent:AccessPage)<-[:HAS_ACCESS_OF_TABS{isEnabled:true}]-(g)\n" +
            "where id(parent)=id(ps.p) with ps,parent,child,ap\n" +
            "optional Match (ap)-[r2:HAS_ACCESS_PAGE_PERMISSION]->(child) with ps,parent,r2,ap\n" +
            "optional Match (ap)-[r:HAS_ACCESS_PAGE_PERMISSION]->(parent) with ps,parent,r2,r\n" +
            "return {name:ps.p.name,id:id(ps.p),read:r.isRead,write:r.isWrite,module:ps.p.isModule,moduleId:ps.p.moduleId,children:collect( distinct {name:ps.c.name,id:id(ps.c),moduleId:ps.c.moduleId,read:r2.isRead,write:r2.isWrite})} as data")
    List<Map<String, Object>> getAccessPageByAccessGroup(long orgId, long unitId, long staffId, long accessGroupId);

    //TODO CHECK ERROR
    @Query("MATCH (org:Organization) where id(org)={0} with org\n" +
            "MATCH (org)-[:" + HAS_EMPLOYMENTS + "]->(emp:Employment)-[:" + BELONGS_TO + "]->(staff:Staff)-[:" + BELONGS_TO + "]->(user:User) where id(user)={1}  with org,emp\n" +
            "Match (emp)-[:" + HAS_UNIT_PERMISSIONS + "]->(unitPermission:UnitPermission)-[:" + APPLICABLE_IN_UNIT + "]->(org) with org,unitPermission\n" +
            "Match (unitPermission)-[:" + HAS_ACCESS_PERMISSION + "{isEnabled:true}]->(ap:AccessPermission) with ap,org\n" +
            "MATCH (ap)-[:HAS_ACCESS_GROUP]->(accessGroup:AccessGroup) with accessGroup,ap,org\n" +
            "Match (ap)-[r:" + HAS_ACCESS_PAGE_PERMISSION + "]->(accessPage:AccessPage{isModule:true})<-[:HAS_ACCESS_OF_TABS{isEnabled:true}]-(accessGroup)\n" +
            "return id(accessPage) as id,accessPage.name as name,r.isRead as read,r.isWrite as write,accessPage.moduleId as moduleId,accessPage.active as active")
    List<AccessPageQueryResult> getPermissionOfMainModule(long orgId, long userId);

    @Query("MATCH (org:Organization),(pOrg:Organization) where id(org) IN {0} AND id(pOrg)={2} \n" +
            "MATCH (pOrg)-[:" + HAS_EMPLOYMENTS + "]->(emp:Employment)-[:" + BELONGS_TO + "]->(staff:Staff)-[:" + BELONGS_TO + "]->(user:User) where id(user)={1}  with org,emp\n" +
            "Match (emp)-[:" + HAS_UNIT_PERMISSIONS + "]->(unitPermission:UnitPermission)-[:" + APPLICABLE_IN_UNIT + "]->(org) with org,unitPermission\n" +
            "Match (unitPermission)-[:" + HAS_ACCESS_PERMISSION + "{isEnabled:true}]->(ap:AccessPermission) with ap,org\n" +
            "MATCH (ap)-[:HAS_ACCESS_GROUP]->(accessGroup:AccessGroup) with accessGroup,ap,org\n" +
            "Match (ap)-[r:" + HAS_ACCESS_PAGE_PERMISSION + "]->(accessPage:AccessPage)<-[:HAS_ACCESS_OF_TABS{isEnabled:true}]-(accessGroup)\n" +
            "return  id(accessPage) as id,accessPage.name as name,r.isRead as read,r.isWrite as write,accessPage.moduleId as moduleId,accessPage.active as active")
    List<AccessPageQueryResult> getPermissionOfMainModule(List<Long> orgIds, long userId, Long parentOrganizationId);


    // TODO For HUB permission for module is not AccessGroup wise, we are giving access of all modules to every user of hub

    @Query("Match (accessPage:AccessPage{isModule:true})\n" +
            "return id(accessPage) as id,accessPage.name as name,true as read,true as write,accessPage.moduleId as moduleId,accessPage.active as active")
    List<AccessPageQueryResult> getPermissionOfMainModuleForHubMembers();

    @Query("MATCH (org:Organization),(parentOrganization:Organization) where id(org)={0} AND id(parentOrganization)={2} with org,parentOrganization\n" +
            "MATCH (emp:Employment)-[:BELONGS_TO]->(staff:Staff)-[:BELONGS_TO]->(user:User) where id(user)={1} with org,emp\n" +
            "MATCH (emp)-[:HAS_UNIT_PERMISSIONS]->(unitPermission:UnitPermission)-[:APPLICABLE_IN_UNIT]->(org) with unitPermission,org\n" +
            "Match (unitPermission)-[:HAS_ACCESS_PERMISSION]->(accessPermission:AccessPermission) with accessPermission,org\n" +
            "MATCH (accessPermission)-[:HAS_ACCESS_GROUP]->(accessGroup:AccessGroup) with accessGroup,accessPermission,org\n" +
            "Match (accessPermission)-[modulePermission:HAS_ACCESS_PAGE_PERMISSION]->(module:AccessPage{isModule:true})<-[:HAS_ACCESS_OF_TABS{isEnabled:true}]-(accessGroup) with module,accessPermission,modulePermission\n" +
            "Match (module)-[:SUB_PAGE*]->(subPage:AccessPage)<-[:HAS_ACCESS_OF_TABS{isEnabled:true}]-(accessGroup) with accessPermission,modulePermission,subPage,module\n" +
            "Match (subPage:AccessPage)<-[subPagePermission:HAS_ACCESS_PAGE_PERMISSION]-(accessPermission) with module,subPage,modulePermission,subPagePermission\n" +
            "return module.name as name,id(module) as id,module.moduleId as moduleId,modulePermission.isRead as read,modulePermission.isWrite as write,module.isModule as isModule,module.active as active,collect( distinct {name:subPage.name,id:id(subPage),moduleId:subPage.moduleId,read:subPagePermission.isRead,write:subPagePermission.isWrite,isModule:subPage.isModule,active:subPage.active}) as children")
    List<AccessPageQueryResult> getTabPermissionForUnit(long unitId, long userId, Long parentOrganizationId);

    @Query("Match (accessPage:AccessPage{isModule:true}) with accessPage as module\n" +
            "Match (module)-[:SUB_PAGE*]->(subPage:AccessPage) with module,subPage\n" +
            "return module.name as name,id(module) as id,module.moduleId as moduleId,true as read,true as write,module.isModule as isModule," +
            "module.active as active,collect({name:subPage.name,id:id(subPage),moduleId:subPage.moduleId,read:true,write:true," +
            "isModule:subPage.isModule,active:subPage.active}) as children")
    List<AccessPageQueryResult> getTabsPermissionForHubMember();

    AccessPage findByModuleId(String moduleId);

    @Query("Match (accessPage:AccessPage{isModule:true}) WITH accessPage\n" +
            "OPTIONAL MATCH (country:Country)-[r:" + HAS_ACCESS_FOR_ORG_CATEGORY + "]-(accessPage) WHERE id(country)={0} WITH r.accessibleForHub as accessibleForHub, r.accessibleForUnion as accessibleForUnion, r.accessibleForOrganization as accessibleForOrganization,accessPage\n" +
            "RETURN \n" +
            "id(accessPage) as id,accessPage.name as name,accessPage.moduleId as moduleId,accessPage.active as active, \n" +
            " CASE WHEN accessibleForHub is NULL THEN false ELSE accessibleForHub END as accessibleForHub,\n" +
            " CASE WHEN accessibleForUnion is NULL THEN false ELSE accessibleForUnion END as accessibleForUnion,\n" +
            " CASE WHEN accessibleForOrganization is NULL THEN false ELSE accessibleForOrganization END as accessibleForOrganization")
    List<AccessPageDTO> getMainTabs(Long countryId);


    @Query("Match (accessPage:AccessPage)-[:" + SUB_PAGE + "]->(subPage:AccessPage) where id(accessPage)={0} WITH subPage,accessPage\n" +
            "OPTIONAL MATCH (country:Country)-[r:" + HAS_ACCESS_FOR_ORG_CATEGORY + "]-(subPage) WHERE id(country)={1} WITH r,subPage,id(accessPage) as parentTabId,\n" +
            "r.accessibleForHub as accessibleForHub, r.accessibleForUnion as accessibleForUnion, r.accessibleForOrganization as accessibleForOrganization\n" +
            "return id(subPage) as id, subPage.name as name,subPage.moduleId as moduleId,subPage.active as active, parentTabId,\n" +
            "CASE WHEN accessibleForHub is NULL THEN false ELSE accessibleForHub END as accessibleForHub,\n" +
            " CASE WHEN accessibleForUnion is NULL THEN false ELSE accessibleForUnion END as accessibleForUnion,\n" +
            " CASE WHEN accessibleForOrganization is NULL THEN false ELSE accessibleForOrganization END as accessibleForOrganization")
    List<AccessPageDTO> getChildTabs(Long tabId, Long countryId);

    @Query("Match (accessPage:AccessPage) where id(accessPage)={0} set accessPage.name={1} return accessPage")
    AccessPage updateAccessTab(Long id, String name);

    @Query("Match (n:AccessPage) where id(n)={0} with n\n" +
            "Optional Match (n)-[:" + SUB_PAGE + "*]->(subPage:AccessPage) with n+[subPage] as coll unwind coll as pages with distinct pages set pages.active={1} return distinct true")
    Boolean updateStatusOfAccessTabs(Long tabId, Boolean active);

    @Query("Match (n:AccessPage) where id(n)={0} with n \n" +
            "OPTIONAL Match (n)-[:SUB_PAGE*]->(subPage:AccessPage)  with collect(subPage)+collect(n) as coll unwind coll as pages with distinct pages with collect(pages) as listOfPage \n" +
            "MATCH (c:Country) WHERE id(c)={1} WITH c, listOfPage\n" +
            "UNWIND listOfPage as page\n" +
            "MERGE (c)-[r:HAS_ACCESS_FOR_ORG_CATEGORY]->(page)\n" +
            "ON CREATE SET r.accessibleForHub = (CASE WHEN {2}='HUB' THEN {3} ELSE false END), \n" +
            "r.accessibleForUnion = (CASE WHEN {2}='UNION' THEN {3} ELSE false END), \n" +
            "r.accessibleForOrganization= (CASE WHEN {2}='ORGANIZATION' THEN {3} ELSE false END)\n" +
            "ON MATCH SET r.accessibleForHub = (CASE WHEN {2}='HUB' THEN {3} ELSE r.accessibleForHub  END), \n" +
            "r.accessibleForUnion = (CASE WHEN {2}='UNION' THEN {3} ELSE r.accessibleForUnion  END),\n" +
            "r.accessibleForOrganization= (CASE WHEN {2}='ORGANIZATION' THEN {3} ELSE r.accessibleForOrganization END)\n" +
            " return distinct true")
    Boolean updateAccessStatusOfCountryByCategory(Long tabId, Long countryId, String organizationCategory, Boolean accessStatus);

    @Query("Match (n:AccessPage) where id(n)={0} with n \n" +
            "OPTIONAL Match (n)-[:" + SUB_PAGE + "*]->(subPage:AccessPage)  with collect(subPage)+collect(n) as coll unwind coll as pages with distinct pages with collect(pages) as listOfPage \n" +
            "MATCH (c:Country) WHERE id(c)={1} WITH c, listOfPage\n" +
            "UNWIND listOfPage as page\n" +
            "MERGE (c)-[r:" + HAS_ACCESS_FOR_ORG_CATEGORY + "]->(page)\n" +
            "ON CREATE SET r.accessibleForHub = {2},r.accessibleForUnion = false,r.accessibleForOrganization=false\n" +
            "ON MATCH  SET r.accessibleForHub = {2} return distinct true")
    Boolean updateAccessStatusForHubOfCountry(Long tabId, Long countryId, Boolean accessStatus);

    @Query("Match (n:AccessPage) where id(n)={0} with n \n" +
            "OPTIONAL Match (n)-[:" + SUB_PAGE + "*]->(subPage:AccessPage)  with collect(subPage)+collect(n) as coll unwind coll as pages with distinct pages with collect(pages) as listOfPage \n" +
            "MATCH (c:Country) WHERE id(c)={1} WITH c, listOfPage\n" +
            "UNWIND listOfPage as page\n" +
            "MERGE (c)-[r:" + HAS_ACCESS_FOR_ORG_CATEGORY + "]->(page)\n" +
            "ON CREATE SET r.accessibleForHub = false,r.accessibleForUnion = {2},r.accessibleForOrganization=false\n" +
            "ON MATCH  SET r.accessibleForUnion = {2} return distinct true")
    Boolean updateAccessStatusForUnionOfCountry(Long tabId, Long countryId, Boolean accessStatus);


    @Query("Match (n:AccessPage) where id(n)={0} with n \n" +
            "OPTIONAL Match (n)-[:" + SUB_PAGE + "*]->(subPage:AccessPage)  with collect(subPage)+collect(n) as coll unwind coll as pages with distinct pages with collect(pages) as listOfPage \n" +
            "MATCH (c:Country) WHERE id(c)={1} WITH c, listOfPage\n" +
            "UNWIND listOfPage as page\n" +
            "MERGE (c)-[r:" + HAS_ACCESS_FOR_ORG_CATEGORY + "]->(page)\n" +
            "ON CREATE SET r.accessibleForHub = false,r.accessibleForUnion = false,r.accessibleForOrganization={2}\n" +
            "ON MATCH  SET r.accessibleForOrganization = {2} return distinct true")
    Boolean updateAccessStatusForOrganizationOfCountry(Long tabId, Long countryId, Boolean accessStatus);


    @Query("Match (emp:Employment)-[:BELONGS_TO]->(staff:Staff)-[:BELONGS_TO]->(user:User) where id(user)={0}\n" +
            "Match (emp)-[:HAS_UNIT_PERMISSIONS]->(unitPermission:UnitPermission)-[:APPLICABLE_IN_UNIT]->(unit:Organization) where id(unit)={1}\n" +
            "Match (unitPermission)-[:HAS_ACCESS_PERMISSION]->(accessPermission:AccessPermission)-[:HAS_ACCESS_GROUP]->(accessGroup:AccessGroup)\n" +
            "Match (module:AccessPage{isModule:true})-[:SUB_PAGE]->(subPage:AccessPage)\n" +
            "optional Match (accessPermission)-[r:HAS_ACCESS_PAGE_PERMISSION]->(module)\n" +
            "optional Match (subPage)<-[r2:HAS_ACCESS_PAGE_PERMISSION]-(accessPermission)with {name:subPage.name,active:subPage.active,moduleId:subPage.moduleId,read:case when r2.isRead then r2.isRead else false end,write:case when r2.isWrite then r2.isWrite else false end,id:id(subPage)} as data,accessGroup,module,r,r2\n" +
            "return id(accessGroup) as accessGroupId,id(module) as id,module.name as name,module.isModule as module,module.active as active,case when r.isRead then r.isRead else false end as read,case when r.isWrite then r.isWrite else false end as write,module.moduleId as moduleId, collect(data) as tabPermissions")
    List<StaffPermissionQueryResult> getAccessPermissionOfUserForUnit(Long userId, Long unitId);

    @Query("Match (accessPage:AccessPage{isModule:true}) with accessPage as module\n" +
            "Match (module)-[:SUB_PAGE*]->(subPage:AccessPage) with module,subPage\n" +
            "return module.name as name,id(module) as id,module.moduleId as moduleId,true as read,true as write,module.isModule as module," +
            "module.active as active,collect({name:subPage.name,id:id(subPage),moduleId:subPage.moduleId,read:true,write:true," +
            "active:subPage.active}) as tabPermissions")
    List<StaffPermissionQueryResult> getTabsPermissionForHubUserForUnit();

    @Query("Match (emp:Employment)-[:" + BELONGS_TO + "]->(staff:Staff)-[:" + BELONGS_TO + "]->(user:User) where id(user)={0} with emp\n" +
            "Match (emp:Employment)-[:" + HAS_UNIT_PERMISSIONS + "]->(unitPermission:UnitPermission)-[:" + APPLICABLE_IN_UNIT + "]->(org:Organization) with collect(org.isKairosHub) as hubList\n" +
            "return true in hubList")
    Boolean isHubMember(Long userId);

    // For Test Cases
    @Query("Match (accessPage:AccessPage{isModule:true}) WITH accessPage return accessPage LIMIT 1")
    AccessPage getOneMainModule();

}


