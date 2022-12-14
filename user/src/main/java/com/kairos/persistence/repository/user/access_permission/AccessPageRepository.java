package com.kairos.persistence.repository.user.access_permission;

import com.kairos.persistence.model.access_permission.*;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.kairos.constants.AppConstants.ACCESS_PAGE_HAS_LANGUAGE;
import static com.kairos.constants.AppConstants.HAS_ACCESS_OF_TABS;
import static com.kairos.persistence.model.constants.RelationshipConstants.*;

/**
 * Created by arvind on 24/10/16.
 */

@Repository
public interface AccessPageRepository extends Neo4jBaseRepository<AccessPage, Long> {
    @Query("MATCH (org:Unit) WHERE id(org)={0}\n" +
            "MATCH (org)-[:" + HAS_SUB_ORGANIZATION + "*]->(n)\n" +
            "MATCH (n)-[:" + HAS_TEAMS + "]->(team:Team)-[:" + TEAM_HAS_MEMBER + "]->(staff:Staff)-[:" + BELONGS_TO + "]->(user:User) WHERE id(user)={1} WITH staff\n" +
            "MATCH (staff)-[:" + STAFF_HAS_ACCESS_GROUP + "]->(accessGroup:AccessGroup)-[r:" + ACCESS_GROUP_HAS_ACCESS_TO_PAGE + "{read:false}]->(accessPage:AccessPage{isModule:true}) RETURN DISTINCT accessPage")
    List<AccessPage> getAccessModulesForUnits(Long parentOrganizationId, Long userId);

    // Fetch access page hierarchy show only selected access page
    @Query("MATCH (ag:AccessGroup) WHERE id(ag)={0} WITH ag \n" +
            "MATCH path=(accessPage:AccessPage{active:true})-[:SUB_PAGE*]->(subPage:AccessPage{active:true})-[:"+HAS_ACCESS_OF_TABS+"]-(ag) \n" +
            "WITH NODES(path) AS np,ag WITH REDUCE(s=[], i IN RANGE(0, LENGTH(np)-3, 1) | s + {p:np[i], c:np[i+1]}) AS cpairs,ag UNWIND cpairs AS pairs WITH DISTINCT pairs AS ps,ag WITH ps,ag\n" +
            "OPTIONAL MATCH (parent:AccessPage)<-[r2:"+HAS_ACCESS_OF_TABS+"]-(ag)\n" +
            "WHERE id(parent)=id(ps.p) WITH r2,ps,ag\n" +
            "OPTIONAL MATCH (child:AccessPage)<-[r:"+HAS_ACCESS_OF_TABS+"]-(ag)\n" +
            "WHERE id(child)=id(ps.c) WITH r,r2,ps,ag\n" +
            "RETURN {" +
            "translations:ps.p.translations,\n" +
            "name:ps.p.name,id:id(ps.p),sequence:ps.p.sequence,selected:case when r2.isEnabled then true else false end, read:r2.read, write:r2.write,module:ps.p.isModule,children:collect({" +
            "translations:ps.c.translations,\n" +
            "name:ps.c.name,id:id(ps.c),sequence:ps.c.sequence,read:r.read, write:r.write,selected:case when r.isEnabled then true else false end})} as data\n" +
            "UNION\n" +
            // Fetch modules which does not have child
            "MATCH (ag:AccessGroup) WHERE id(ag)={0} WITH ag \n" +
            "MATCH (accessPage:AccessPage{isModule:true,active:true}) WHERE not (accessPage)-[:"+SUB_PAGE+"]->() WITH accessPage, ag\n" +
            "MATCH (accessPage)<-[r:"+HAS_ACCESS_OF_TABS+"]-(ag) WITH accessPage, ag,r\n" +
            "RETURN {" +
            "translations:accessPage.translations,\n" +
            "name:accessPage.name,id:id(accessPage),sequence:accessPage.sequence,read:r.read, write:r.write,selected:case when r.isEnabled then true else false end,module:accessPage.isModule,children:[]} as data")
    List<Map<String,Object>> getSelectedAccessPageHierarchy(Long accessGroupId);


    @Query("MATCH (accessGroup:AccessGroup),(accessPermission:AccessPermission) WHERE id(accessPermission)={0} AND id(accessGroup)={1}\n" +
            "MATCH (accessGroup)-[r:" + HAS_ACCESS_OF_TABS + "{isEnabled:true}]->(accessPage:AccessPage) WITH accessPage,r,accessPermission\n" +
            "Create unique (accessPermission)-[:" + HAS_ACCESS_PAGE_PERMISSION + "{isEnabled:r.isEnabled,isRead:true,isWrite:false}]->(accessPage) RETURN accessPermission")
    List<AccessPage> setDefaultPermission(Long accessPermissionId, Long accessGroupId);


    @Query("MATCH (accessGroup:AccessGroup{deleted:false})-[r:"+HAS_ACCESS_OF_TABS+"{isEnabled:true}]-(accessPage:AccessPage) WHERE id(accessPage)={1} AND id(accessGroup)={0} RETURN r.read as read, r.write as write")
    AccessPageQueryResult getAccessPermissionForAccessPage(Long accessGroupId, Long accessPageId);


    @Query("MATCH (accessPage:AccessPage) WHERE id(accessPage)={3} WITH accessPage\n" +
            "MATCH (n:Organization),(staff:Staff) WHERE id(n)={0} AND id(staff)={1} WITH n,staff,accessPage\n" +
            "MATCH (n)-[:"+ HAS_POSITIONS +"]->(position:Position)-[:"+BELONGS_TO+"]->(staff)-[:"+BELONGS_TO+"]->(user:User) WITH user,position,accessPage\n" +
            "MATCH (position)-[:"+HAS_UNIT_PERMISSIONS+"]->(unitPermission:UnitPermission)-[:"+APPLICABLE_IN_UNIT+"]->(unit) WHERE id(unit)={2} WITH unitPermission,accessPage\n" +
            "MATCH (unitPermission)-[r:"+HAS_CUSTOMIZED_PERMISSION+"]->(accessPage) WHERE r.accessGroupId={4}\n" +
            "RETURN r.read as read, r.write as write")
    AccessPageQueryResult getCustomPermissionOfTab(Long organizationId, Long staffId, Long unitId, Long accessPageId, Long accessGroupId);



    @Query("MATCH (n:Organization)-[:"+ HAS_POSITIONS +"]->(position:Position)-[:"+BELONGS_TO+"]->(staff:Staff)-[:"+BELONGS_TO+"]->(user:User) WHERE id(n)={0} AND  id(staff)={2}\n" +
            "MATCH (position)-[:"+HAS_UNIT_PERMISSIONS+"]->(unitPermission:UnitPermission)-[:"+APPLICABLE_IN_UNIT+"]->(unit) WHERE id(unit)={1}\n" +
            "MATCH (unitPermission)-[:"+HAS_ACCESS_GROUP+"]->(g:AccessGroup) WHERE id(g)={3} WITH g,unitPermission\n" +
            "MATCH (g)-[:"+HAS_ACCESS_OF_TABS+"]->(accessPage:AccessPage{isModule:true}) WITH accessPage,g,unitPermission\n" +
            "MATCH path=(accessPage)-[:SUB_PAGE*]->(accessPage1:AccessPage)<-[:HAS_ACCESS_OF_TABS{isEnabled:true}]-(g) \n" +
            "WITH NODES(path) AS np,g as g,unitPermission as unitPermission WITH REDUCE(s=[], i IN RANGE(0, LENGTH(np)-3, 1) | s + {p:np[i], c:np[i+1],g:g,unitPermission:unitPermission}) AS cpairs UNWIND cpairs AS pairs WITH DISTINCT pairs AS ps WITH DISTINCT ps.g as g,ps as ps,ps.unitPermission as unitPermission\n" +
            "OPTIONAL MATCH (parent:AccessPage)<-[r:HAS_ACCESS_OF_TABS{isEnabled:true}]-(g) WHERE id(parent)=id(ps.p) \n" +
            "OPTIONAL MATCH (unitPermission)-[parentCustomRel:HAS_CUSTOMIZED_PERMISSION]->(parent:AccessPage) WHERE parentCustomRel.accessGroupId={3}\n" +
            " AND id(parent)=id(ps.p) WITH r,parentCustomRel,ps,unitPermission,g\n" +
            "OPTIONAL MATCH (child:AccessPage)<-[r2:HAS_ACCESS_OF_TABS{isEnabled:true}]-(g) WHERE id(child)=id(ps.c)\n" +
            "OPTIONAL MATCH (unitPermission)-[childCustomRel:HAS_CUSTOMIZED_PERMISSION]->(child:AccessPage) WHERE childCustomRel.accessGroupId={3}\n" +
            "AND id(child)=id(ps.c) WITH r,r2,parentCustomRel,childCustomRel,ps\n" +
            "RETURN {name:ps.p.name,id:id(ps.p),\n" +
            "read:CASE WHEN parentCustomRel IS NULL THEN r.read ELSE parentCustomRel.read END ,\n" +
            "write:CASE WHEN parentCustomRel IS NULL THEN r.write ELSE parentCustomRel.write END,module:ps.p.isModule,sequence:ps.p.sequence,moduleId:ps.p.moduleId,\n" +
            "children:collect( DISTINCT {name:ps.c.name,id:id(ps.c),sequence:ps.c.sequence,moduleId:ps.c.moduleId,\n" +
            "read:CASE WHEN childCustomRel IS NULL THEN r2.read ELSE childCustomRel.read END,\n" +
            "write:CASE WHEN childCustomRel IS NULL THEN r2.write ELSE childCustomRel.write END})} as data\n"+
            " UNION \n" +
            "MATCH (n:Organization)-[:"+ HAS_POSITIONS +"]->(position:Position)-[:"+BELONGS_TO+"]->(staff:Staff)-[:"+BELONGS_TO+"]->(user:User) WHERE id(n)={0} AND  id(staff)={2}\n" +
            "MATCH (position)-[:"+HAS_UNIT_PERMISSIONS+"]->(unitPermission:UnitPermission)-[:"+APPLICABLE_IN_UNIT+"]->(unit) WHERE id(unit)={1}\n" +
            "MATCH (unitPermission)-[:"+HAS_ACCESS_GROUP+"]->(g:AccessGroup) WHERE id(g)={3} WITH g,unitPermission\n" +
            "MATCH (accessPage:AccessPage{isModule:true,active:true}) WHERE not (accessPage)-[:SUB_PAGE]->() WITH accessPage, g, unitPermission\n" +
            "MATCH (accessPage)<-[r:HAS_ACCESS_OF_TABS{isEnabled:true}]-(g) WITH accessPage, g,r, unitPermission\n" +
            "OPTIONAL MATCH (unitPermission)-[customRel:"+HAS_CUSTOMIZED_PERMISSION+"]->(accessPage) WHERE customRel.accessGroupId={3}\n" +
            "RETURN {name:accessPage.name,id:id(accessPage),\n" +
            "read:CASE WHEN customRel IS NULL THEN r.read ELSE customRel.read END, write:CASE WHEN customRel IS NULL THEN r.write ELSE customRel.write END,\n" +
            "selected:case when r.isEnabled then true else false end,module:accessPage.isModule,sequence:accessPage.sequence,children:[]} as data")
    List<Map<String, Object>> getAccessPagePermissionOfStaff(Long orgId, Long unitId, Long staffId, Long accessGroupId);

    AccessPage findByModuleId(String moduleId);


    @Query("MATCH (accessPage:AccessPage{isModule:true}) WITH accessPage\n" +
            "OPTIONAL MATCH (country:Country)-[r:" + HAS_ACCESS_FOR_ORG_CATEGORY + "]-(accessPage) " +
            "OPTIONAL MATCH(accessPage)-[subTabs:"+SUB_PAGE+"]-(sub:AccessPage) " +
            "WITH r.accessibleForHub as accessibleForHub, r.accessibleForUnion as accessibleForUnion, r.accessibleForOrganization as accessibleForOrganization,accessPage,subTabs \n" +
            "RETURN \n" +
            "id(accessPage) as id,accessPage.name as name,accessPage.moduleId as moduleId,accessPage.active as active,accessPage.editable as editable,accessPage.sequence as sequence,accessPage as accessPage, " +
            "CASE WHEN count(subTabs)>0 THEN true ELSE false END as hasSubTabs,\n" +
            "CASE WHEN accessibleForHub is NULL THEN false ELSE accessibleForHub END as accessibleForHub,\n" +
            "CASE WHEN accessibleForUnion is NULL THEN false ELSE accessibleForUnion END as accessibleForUnion,\n" +
            "CASE WHEN accessibleForOrganization is NULL THEN false ELSE accessibleForOrganization END as accessibleForOrganization ORDER BY id(accessPage)")
    List<AccessPageQueryResult> getMainTabs();

    @Query("MATCH (org:Unit) WHERE id(org)={0} WITH org\n" +
            "MATCH(org)-[:" + ORGANIZATION_HAS_ACCESS_GROUPS + "]-(accessgroup:AccessGroup{deleted: false}) WITH accessgroup\n" +
            "MATCH(accessgroup)-[r:" + HAS_ACCESS_OF_TABS + "]->(accessPage:AccessPage{isModule:true}) WITH  r.accessibleForHub as accessibleForHub, r.accessibleForUnion as accessibleForUnion, r.accessibleForOrganization as accessibleForOrganization,accessPage\n" +
            "RETURN DISTINCT id(accessPage) as id,accessPage.name as name,accessPage.moduleId as moduleId,accessPage.active as active,accessPage.sequence as sequence,accessPage.editable as editable,accessPage as accessPage," +
            "CASE WHEN accessibleForHub is NULL THEN false ELSE accessibleForHub END as accessibleForHub, \n" +
            "CASE WHEN accessibleForUnion is NULL THEN false ELSE accessibleForUnion END as accessibleForUnion, \n" +
            "CASE WHEN accessibleForOrganization is NULL THEN false ELSE accessibleForOrganization END as accessibleForOrganization  ORDER BY id(accessPage)")
    List<AccessPageQueryResult> getMainTabsForUnit(Long unitId);


    @Query("MATCH (accessPage:AccessPage)-[:"+SUB_PAGE+"]->(subPage:AccessPage) WHERE id(accessPage)={0} WITH subPage,accessPage \n" +
            "OPTIONAL MATCH (country:Country)-[r:"+HAS_ACCESS_FOR_ORG_CATEGORY+"]-(subPage) \n" +
            "OPTIONAL MATCH(subPage)-[subTabs:"+SUB_PAGE+"]->(sub:AccessPage)\n" +
            "WITH r,subPage,id(accessPage) as parentTabId,subTabs,\n" +
            "r.accessibleForHub as accessibleForHub, r.accessibleForUnion as accessibleForUnion, r.accessibleForOrganization as accessibleForOrganization\n" +
            "RETURN id(subPage) as id, subPage.name as name,subPage.moduleId as moduleId,subPage.active as active,subPage.sequence as sequence,subPage.editable as editable, parentTabId,subPage as accessPage, " +
            "CASE WHEN count(subTabs)>0 THEN true ELSE false END as hasSubTabs,\n" +
            "CASE WHEN accessibleForHub is NULL THEN false ELSE accessibleForHub END as accessibleForHub,\n" +
            "CASE WHEN accessibleForUnion is NULL THEN false ELSE accessibleForUnion END as accessibleForUnion,\n" +
            "CASE WHEN accessibleForOrganization is NULL THEN false ELSE accessibleForOrganization END as accessibleForOrganization "
    )
    List<AccessPageQueryResult> getChildTabs(Long tabId);

    @Query("MATCH (accessPage:AccessPage) WHERE id(accessPage)={0} set accessPage.name={1} RETURN accessPage")
    AccessPage updateAccessTab(Long id, String name);

    @Query("MATCH (n:AccessPage) WHERE id(n)={0} WITH n\n" +
            "OPTIONAL MATCH (n)-[:" + SUB_PAGE + "*]->(subPage:AccessPage) WITH n+[subPage] as coll unwind coll as pages WITH DISTINCT pages set pages.active={1} RETURN DISTINCT true")
    Boolean updateStatusOfAccessTabs(Long tabId, Boolean active);

    @Query("MATCH (n:AccessPage) WHERE id(n)={0} WITH n \n" +
            "OPTIONAL MATCH (n)-[:SUB_PAGE*]->(subPage:AccessPage)  WITH collect(subPage)+collect(n) as coll unwind coll as pages WITH DISTINCT pages WITH collect(pages) as listOfPage \n" +
            "MATCH (c:Country)  WITH c, listOfPage\n" +
            "UNWIND listOfPage as page\n" +
            "MERGE (c)-[r:"+HAS_ACCESS_FOR_ORG_CATEGORY+"]->(page)\n" +
            "ON CREATE SET r.accessibleForHub = (CASE WHEN {1}='HUB' THEN {2} ELSE false END), \n" +
            "r.accessibleForUnion = (CASE WHEN {1}='UNION' THEN {2} ELSE false END), \n" +
            "r.accessibleForOrganization= (CASE WHEN {1}='ORGANIZATION' THEN {2} ELSE false END)\n" +
            "ON MATCH SET r.accessibleForHub = (CASE WHEN {1}='HUB' THEN {2} ELSE r.accessibleForHub  END), \n" +
            "r.accessibleForUnion = (CASE WHEN {1}='UNION' THEN {2} ELSE r.accessibleForUnion  END),\n" +
            "r.accessibleForOrganization= (CASE WHEN {1}='ORGANIZATION' THEN {2} ELSE r.accessibleForOrganization END)\n" +
            " RETURN DISTINCT true")
    Boolean updateAccessStatusOfCountryByCategory(Long tabId, String organizationCategory, Boolean accessStatus);

    @Query("MATCH (position:Position)-[:" + BELONGS_TO + "]->(staff:Staff)-[:" + BELONGS_TO + "]->(user:User) WHERE id(user)={0} WITH position\n" +
            "MATCH (position:Position)-[:" + HAS_UNIT_PERMISSIONS + "]->(unitPermission:UnitPermission)-[:" + APPLICABLE_IN_UNIT + "]->(org:Organization) WITH collect(org.isKairosHub) as hubList\n" +
            "RETURN true in hubList")
    Boolean isHubMember(Long userId);


    @Query("MATCH (accessPage)-[:"+SUB_PAGE+"]->(childAP:AccessPage) WHERE id(childAP)={0}  RETURN id(accessPage)")
    Long getParentTab(Long accessPageId);

    @Query("MATCH (n:Organization)-[:"+HAS_POSITIONS+"]->(position:Position)-[:"+BELONGS_TO+"]->(staff:Staff)-[:"+BELONGS_TO+"]->(user:User) WHERE id(n)={0} AND  id(staff)={2}\n" +
            "MATCH (position)-[:"+HAS_UNIT_PERMISSIONS+"]->(unitPermission:UnitPermission)-[:"+APPLICABLE_IN_UNIT+"]->(unit) WHERE id(unit)={1} \n" +
            "MATCH (unitPermission)-[:"+HAS_ACCESS_GROUP+"]->(g:AccessGroup) WITH g,unitPermission\n" +
            "OPTIONAL MATCH (g)-[:"+HAS_ACCESS_OF_TABS+"]->(accessPage:AccessPage) WITH g,unitPermission, accessPage\n" +
            "MATCH (accessPage)-[:"+SUB_PAGE+"]->(childrenAccessPages:AccessPage)<-[r:"+HAS_ACCESS_OF_TABS+"]-(g) WHERE id(accessPage)={3}\n" +
            "WITH childrenAccessPages,unitPermission,r,g\n" +
            "OPTIONAL MATCH (unitPermission)-[customRel:"+HAS_CUSTOMIZED_PERMISSION+"]->(childrenAccessPages:AccessPage) WHERE customRel.accessGroupId={4}\n" +
            "WITH r,customRel,unitPermission,childrenAccessPages,g\n" +
            "RETURN childrenAccessPages.name as name,id(childrenAccessPages) as id, CASE WHEN customRel IS NULL THEN r.read ELSE customRel.read END as read ,\n" +
            "CASE WHEN customRel IS NULL THEN r.write ELSE customRel.write END as write ORDER BY id DESC")
    List<AccessPageQueryResult> getChildTabsAccessPermissionsByStaffAndOrg(Long orgId, Long unitId, Long staffId, Long tabId, Long accessGroupId);

    @Query("MATCH(ac:AccessGroup) WHERE id(ac)={1} WITH ac " +
            "MATCH(accessGroup:AccessGroup)-[rel:" + HAS_ACCESS_OF_TABS + "]->(accessPage:AccessPage) WHERE id(accessGroup)={0} WITH ac,accessGroup as accessGroup," +
            "accessPage as accessPage, rel.read as read, rel.write as write ,rel.isEnabled as isEnabled  " +
            "create unique (ac)-[r:"+HAS_ACCESS_OF_TABS+"{isEnabled:isEnabled, read:read, write:write}]->(accessPage)")
    void copyAccessGroupPageRelationShips(Long oldAccessGroupId,Long newAccessGroupId);

    @Query("MATCH (a:AccessPage) WHERE a.isModule={0} WITH id(a) as id,a.name as name,a.moduleId as moduleId, "+
            "toInt(split(a.moduleId,\"_\")[1]) as tabIdNumber RETURN tabIdNumber ORDER BY tabIdNumber DESC LIMIT 1")
    Integer getLastTabOrModuleIdOfAccessPage(boolean isModule);


    @Query(" MATCH (accessPage:AccessPage) WHERE id(accessPage)={0} \n" +
            "MATCH (parentAccessPage: AccessPage)-[:"+SUB_PAGE+"]->(accessPage) RETURN id(parentAccessPage)")
    Long getParentAccessPageIdForAccessGroup(Long accessPageId);


    @Query( "MATCH(accessGroup:AccessGroup) WHERE id(accessGroup)={0} \n" +
            "MATCH (accessPage:AccessPage) WHERE id(accessPage)={1} \n" +
            "MATCH (accessPage)-[:SUB_PAGE]->(childrenAccessPages:AccessPage)<-[r:"+HAS_ACCESS_OF_TABS+"]-(g) \n" +
            "RETURN childrenAccessPages.name as name,id(childrenAccessPages) as id, r.read as read ,\n" +
            "r.write as write ORDER BY id DESC")
    List<AccessPageQueryResult> getChildAccessPagePermissionsForAccessGroup(Long accessGroupId, Long accessPageId);

    @Query( "MATCH(accessGroup:AccessGroup) WHERE id(accessGroup)={0} \n" +
            "MATCH (accessPage:AccessPage) WHERE id(accessPage)={1} \n" +
            "MATCH (accessPage)<-[r:"+HAS_ACCESS_OF_TABS+"]-(g) \n" +
            "SET r.read={2}, r.write={3} ")
    List<AccessPageQueryResult> updateAccessPagePermissionsForAccessGroup(Long accessGroupId, Long accessPageId, Boolean readPermission, Boolean writePermission);

    @Query("MATCH (unitPermission:UnitPermission)-[customRel:"+HAS_CUSTOMIZED_PERMISSION+"]->(accessPages:AccessPage) " +
            "WHERE customRel.accessGroupId={0} AND id(accessPages) IN {1} DELETE customRel")
    void removeCustomPermissionsForAccessGroup(Long accessGroupId, List<Long> accessPageIds);


    @Query("MATCH (position:Position)-[:" + BELONGS_TO + "]->(staff:Staff)-[:" + BELONGS_TO + "]->(user:User) WHERE id(user)={0} WITH position\n" +
            "MATCH (position:Position)-[:" + HAS_UNIT_PERMISSIONS + "]->(unitPermission:UnitPermission)-[:" + APPLICABLE_IN_UNIT + "]->(org:Organization{isKairosHub:true})-[r:"+ORGANIZATION_HAS_ACCESS_GROUPS+"]-(ag:AccessGroup) RETURN org, COLLECT(r),COLLECT(ag) ORDER BY id(org) LIMIT 1 \n" )
    Organization fetchParentHub(Long userId);



    // fetch Permission of Hub Users

    @Query("MATCH (position:Position)-[:"+BELONGS_TO+"]->(staff:Staff)-[:"+BELONGS_TO+"]->(user:User) WHERE id(user)={0} WITH position \n" +
            "MATCH (position)-[:"+HAS_UNIT_PERMISSIONS+"]->(unitPermission:UnitPermission)-[:"+APPLICABLE_IN_UNIT+"]->(org{isEnable:true}) WHERE id(org)={1}  WITH unitPermission,org \n" +
            "MATCH (unitPermission)-[:"+HAS_ACCESS_GROUP+"]->(accessGroup:AccessGroup) WHERE id(accessGroup) IN {2} AND (accessGroup.endDate IS NULL OR date(accessGroup.endDate) >= date()) WITH accessGroup,org,unitPermission \n" +
            "MATCH (module:AccessPage)<-[modulePermission:"+HAS_ACCESS_OF_TABS+"{isEnabled:true}]-(accessGroup) WITH module,modulePermission,unitPermission,accessGroup\n" +
            "OPTIONAL MATCH (unitPermission)-[moduleCustomRel:"+HAS_CUSTOMIZED_PERMISSION+"]->(module) WHERE moduleCustomRel.accessGroupId=id(accessGroup) \n" +
            "WITH module,modulePermission,unitPermission,moduleCustomRel,accessGroup\n" +
            "RETURN module.name as name,id(module) as id,module.moduleId as moduleId,CASE WHEN moduleCustomRel IS NULL THEN modulePermission.read ELSE moduleCustomRel.read END as read,CASE WHEN moduleCustomRel IS NULL THEN modulePermission.write ELSE moduleCustomRel.write END as write,module.isModule as module,module.sequence as sequence  " +
            " UNION " +
            " MATCH (module:AccessPage)<-[modulePermission:"+HAS_ACCESS_OF_TABS+"{isEnabled:true}]-(ag:AccessGroup) WHERE id (ag) IN {3} " +
            " RETURN module.name as name,id(module) as id,module.moduleId as moduleId,modulePermission.read  as read,modulePermission.write  as write,module.isModule as module,module.sequence as sequence  ")
    List<AccessPageQueryResult> fetchHubUserPermissions(Long userId, Long organizationId, Set<Long> accessGroupIds, Set<Long> allAccessGroupIds);

    @Query(" MATCH (module:AccessPage) " +
            " RETURN module.name as name,id(module) as id,module.moduleId as moduleId,true  as read,true  as write,module.isModule as module,module.sequence as sequence  ")
    List<AccessPageQueryResult> fetchHubSystemAdminPermissions();

    @Query("MATCH (u:User) WHERE id(u)={0} \n" +
            "MATCH (org:Organization{isEnable:true})-[:"+ HAS_POSITIONS +"]-(position:Position)-[:"+BELONGS_TO+"]-(s:Staff)-[:"+BELONGS_TO+"]-(u) \n" +
            "OPTIONAL MATCH (org)-[:"+HAS_UNIT+"]->(unit:Unit{isEnable:true,boardingCompleted:true}) WITH position,org+[unit] as coll\n" +
            "unwind coll as units WITH  DISTINCT units,position \n" +
            "OPTIONAL MATCH  (o:Organization{isEnable:true,isParentOrganization:true,organizationLevel:'CITY'})-[r:HAS_SUB_ORGANIZATION*1..]-(units) \n" +
            "WITH o,position, [o]+units as units  unwind units as org  WITH DISTINCT org,o,position\n" +
            "MATCH (position)-[:"+HAS_UNIT_PERMISSIONS+"]->(unitPermission:UnitPermission)-[:"+APPLICABLE_IN_UNIT+"]->(org) WITH org,unitPermission \n" +
            "MATCH (unitPermission)-[agr:"+HAS_ACCESS_GROUP+"]->(accessGroup:AccessGroup{deleted:false,enabled:true}) WHERE agr.endDate IS NULL OR DATE(agr.endDate) >= DATE() " +
            "WITH accessGroup,org,unitPermission " +
            "WHERE ANY(dt IN accessGroup.dayTypeIds WHERE dt IN   {1})   AND (accessGroup.endDate IS NULL OR DATE(accessGroup.endDate) >= DATE())  WITH org,accessGroup,unitPermission\n" +
            "MATCH (accessPage:AccessPage)<-[r:"+HAS_ACCESS_OF_TABS+"{isEnabled:true}]-(accessGroup) \n" +
            "MATCH (org) WHERE id(org)={2} \n"+
            "OPTIONAL MATCH (unitPermission)-[customRel:"+HAS_CUSTOMIZED_PERMISSION+"]->(accessPage) WHERE customRel.accessGroupId=id(accessGroup)\n" +
            "WITH org,collect( DISTINCT {name:accessPage.name,id:id(accessPage),moduleId:accessPage.moduleId,read:CASE WHEN customRel IS NULL THEN r.read ELSE customRel.read END,write:CASE WHEN customRel IS NULL THEN r.write ELSE customRel.write END,module:accessPage.isModule,sequence:accessPage.sequence}) as permissions\n" +
            "RETURN id(org) as unitId,'Organization' IN labels(org) as parentOrganization, permissions as permission")
    List<UserPermissionQueryResult> fetchStaffPermissionsWithDayTypes(Long userId, Set<String> dayTypeIds, Long organizationId);



    @Query("MATCH (u:User) WHERE id(u)={0} \n" +
            "MATCH (org:Organization{isEnable:true})-[:"+ HAS_POSITIONS +"]-(position:Position)-[:"+BELONGS_TO+"]-(s:Staff)-[:"+BELONGS_TO+"]-(u) \n" +
            "OPTIONAL MATCH (org)-[:"+HAS_UNIT+"*]->(unit:Unit{isEnable:true,boardingCompleted:true}) WITH position,org+[unit] as coll\n" +
            "unwind coll as units WITH  DISTINCT units,position \n" +
            "OPTIONAL MATCH  (o:Unit{isEnable:true,isParentOrganization:true,organizationLevel:'CITY'})-[r:HAS_SUB_ORGANIZATION*1..]-(units) \n" +
            "WITH o,position, [o]+units as units  unwind units as org  WITH DISTINCT org,o,position\n" +
            "MATCH (position)-[:"+HAS_UNIT_PERMISSIONS+"]->(unitPermission:UnitPermission)-[:"+APPLICABLE_IN_UNIT+"]->(org) WITH org,unitPermission \n" +
            "MATCH (unitPermission)-[agr:"+HAS_ACCESS_GROUP+"]->(accessGroup:AccessGroup{deleted:false,enabled:true}) WHERE (agr.endDate IS NULL OR date(agr.endDate) >= date())  WITH org,accessGroup,unitPermission\n" +
            "MATCH (accessPage:AccessPage)<-[r:HAS_ACCESS_OF_TABS{isEnabled:true}]-(accessGroup) \n" +
            "MATCH (org) WHERE id(org)={1} \n"+
            "OPTIONAL MATCH (unitPermission)-[customRel:HAS_CUSTOMIZED_PERMISSION]->(accessPage) WHERE customRel.accessGroupId=id(accessGroup)\n" +
            "WITH org,collect( DISTINCT {name:accessPage.name,id:id(accessPage),moduleId:accessPage.moduleId,read:CASE WHEN customRel IS NULL THEN r.read ELSE customRel.read END,write:CASE WHEN customRel IS NULL THEN r.write ELSE customRel.write END,module:accessPage.isModule,sequence:accessPage.sequence}) as permissions\n" +
            "RETURN id(org) as unitId,'Organization' IN labels(org) as parentOrganization, permissions as permission")
    List<UserPermissionQueryResult> fetchStaffPermissions(Long userId,Long organizationId);



    @Query("MATCH (accessPage:AccessPage{isModule:true}) WITH accessPage\n" +
            "MATCH (country:Country)-[:" + HAS_ACCESS_FOR_ORG_CATEGORY + "]-(accessPage) WHERE id(country)={0} WITH accessPage\n" +
            "OPTIONAL MATCH (accessPage) -[: " +SUB_PAGE + "]->(subPages:AccessPage{active:true,kpiEnabled:true})\n" +
            " RETURN accessPage.name as name,accessPage.moduleId as moduleId, collect(DISTINCT subPages) as child ORDER BY accessPage.moduleId")
    List<KPIAccessPageQueryResult> getKPITabsListForCountry(Long countryId);

    @Query("MATCH (user:User) ,(org)  where id(org)={0}\n" +
            "AND id(user)={1}\n" +
            "MATCH (user)-[:"+BELONGS_TO+"]-(staff:Staff)-[:"+BELONGS_TO+"]-(position:Position)-[:"+HAS_UNIT_PERMISSIONS+"]-(up:UnitPermission)-[:"+APPLICABLE_IN_UNIT+"]-(org)\n" +
            "MATCH(up)-[:"+HAS_ACCESS_GROUP+"]-(accessgroup:AccessGroup{deleted: false,enabled:true})-[r:"+HAS_ACCESS_OF_TABS+"{read:true}]->(accessPage:AccessPage{isModule:true,active:true}) where id(accessgroup)=id(accessgroup)\n" +
            "OPTIONAL MATCH (up)-[customRel:"+HAS_CUSTOMIZED_PERMISSION+"]->(accessPage) WHERE customRel.accessGroupId=id(accessgroup) \n" +
            "OPTIONAL MATCH (accessPage) -[:"+SUB_PAGE+"]->(subPages:AccessPage{active:true,kpiEnabled:true})\n" +
            "OPTIONAL MATCH (up)-[childCustomRel:"+HAS_CUSTOMIZED_PERMISSION+"]->(subPages) WHERE childCustomRel.accessGroupId=id(accessgroup) \n" +
            "WITH accessPage,subPages,customRel,r,childCustomRel\n" +
            "RETURN accessPage.translations as translations,\n" +
            "accessPage.name as name,accessPage.moduleId as moduleId,\n" +
            "CASE WHEN customRel IS NULL THEN r.read ELSE customRel.read END as read,\n" +
            "CASE WHEN customRel IS NULL THEN r.write ELSE customRel.write END as write ,\n" +
            "collect( DISTINCT {name:subPages.name,moduleId:subPages.moduleId,read:CASE WHEN childCustomRel IS NULL THEN r.read ELSE childCustomRel.read END,write:CASE WHEN childCustomRel IS NULL THEN r.write ELSE childCustomRel.write END}) as child ORDER BY moduleId")
    List<KPIAccessPageQueryResult> getKPITabsListForUnit(Long unitId,Long userId);


    @Query("MATCH (n:AccessPage) -[:SUB_PAGE *]->(subPages:AccessPage{active:true,kpiEnabled:true}) WHERE n.moduleId={0} RETURN subPages")
    List<AccessPage> getKPITabsList(String moduleId);

    @Query("MATCH(accessPage:AccessPage)-[rel:"+ ACCESS_PAGE_HAS_LANGUAGE +"]->(language:SystemLanguage{deleted:false}) WHERE accessPage.moduleId={0} AND id(language)={1} AND exists(rel.creationDate) " +
            "RETURN rel.description as description, id(rel) as id, rel.languageId as languageId, rel.moduleId as moduleId order by rel.creationDate DESC limit 1")
    AccessPageLanguageDTO findLanguageSpecificDataByModuleIdAndLanguageId(String moduleId, Long languageId);

    @Query("MATCH (accessPage:AccessPage{isModule:true}) WHERE id(accessPage) IN {0} RETURN accessPage")
    List<AccessPage> findAllModulesByIds(Set<Long> moduleIds);

    @Query("MATCH (country:Country)-[" + HAS_ACCESS_FOR_ORG_CATEGORY + "]-(accessPage:AccessPage{isModule:true,active:true}) WHERE id(country)={0} " +
            "RETURN id(accessPage) as id,accessPage.name as name,accessPage.moduleId as moduleId,accessPage.active as active")
    List<AccessPageDTO> getMainActiveTabs(Long countryId);

    @Query("MATCH (u:User) WHERE id(u)={0} \n" +
            "MATCH (org:Organization{isEnable:true})-[:"+ HAS_POSITIONS +"]-(position:Position)-[:"+BELONGS_TO+"]-(s:Staff)-[:"+BELONGS_TO+"]-(u) \n" +
            "OPTIONAL MATCH (org)-[:"+HAS_UNIT+"]->(subOrg:Unit{isEnable:true}) WITH position,org+[subOrg] as coll\n" +
            "unwind coll as orgList WITH  DISTINCT orgList,position \n" +
            "OPTIONAL MATCH  (o:Organization{isEnable:true,isParentOrganization:true,organizationLevel:'CITY'})-[r:"+HAS_SUB_ORGANIZATION+"*1..]->(orgList) \n" +
            "WITH o,position, [o]+orgList as units  unwind units as org  WITH DISTINCT org,o,position\n" +
            "MATCH (position)-[:"+HAS_UNIT_PERMISSIONS+"]->(unitPermission:UnitPermission)-[:"+APPLICABLE_IN_UNIT+"]->(org) WITH org,unitPermission \n" +
            "MATCH (unitPermission)-[:"+HAS_ACCESS_GROUP+"]->(accessGroup:AccessGroup{deleted:false,enabled:true})  RETURN accessGroup"
            )
    List<AccessGroup> fetchAccessGroupsOfStaffPermission(Long userId);

    @Query("MATCH(user:User)<-[:"+BELONGS_TO+"]-(staff:Staff)<-[:"+BELONGS_TO+"]-(position:Position)<-[:"+ HAS_POSITIONS +"]-(parentOrg:Organization)-[:"+ORGANIZATION_HAS_ACCESS_GROUPS+"]->(accessGroup:AccessGroup),(unit) " +
            "WHERE  id(user) = {0} AND id(unit) IN {1} \n" +
            "OPTIONAL MATCH(position)-[:"+HAS_UNIT_PERMISSIONS+"]->(up:UnitPermission)-[:"+APPLICABLE_IN_UNIT+"]->(unit)\n" +
            "OPTIONAL MATCH(up)-[r:"+HAS_ACCESS_GROUP+"]->(accessGroup) " +
            "RETURN id(unit) as unitId, CASE WHEN COUNT(r)>0 THEN TRUE ELSE FALSE END AS hasPermission")
    List<StaffAccessGroupQueryResult> getAccessPermission(Long userId, Set<Long> organizationIds);

    @Query("MATCH (accessPage:AccessPage{isModule:true}) WITH accessPage\n" +
            "OPTIONAL MATCH (accessPage)-[alr:" + ACCESS_PAGE_HAS_LANGUAGE + "]-(systemLanguage:SystemLanguage) " +
            "WHERE id(systemLanguage)={0} " +
            "WITH accessPage,alr " +
            "OPTIONAL MATCH(accessPage)-[subTabs:"+SUB_PAGE+"]-(sub:AccessPage) " +
            "WITH accessPage,subTabs,alr.description as helperText \n" +
            "RETURN \n" +
            "id(accessPage) as id,accessPage.name as name,accessPage.url as url,accessPage.moduleId as moduleId,accessPage.active as active,accessPage.editable as editable,accessPage.sequence as sequence,accessPage as accessPage,helperText, " +
            "CASE WHEN count(subTabs)>0 THEN true ELSE false END as hasSubTabs \n" +
            " ORDER BY id(accessPage) ")
    List<AccessPageQueryResult> getMainTabsWithHelperText(Long languageId);

    @Query("MATCH (accessPage:AccessPage)-[:"+SUB_PAGE+"]->(subPage:AccessPage) WHERE id(accessPage)={0} WITH subPage,accessPage \n" +
            "OPTIONAL MATCH(subPage)-[alr:" + ACCESS_PAGE_HAS_LANGUAGE + "]-(systemLanguage:SystemLanguage) \n" +
            "WHERE id(systemLanguage)={1} " +
            "WITH DISTINCT accessPage,subPage,alr " +
            "OPTIONAL MATCH(subPage)-[subTabs:"+SUB_PAGE+"]->(sub:AccessPage)\n" +
            "WITH DISTINCT subPage,id(accessPage) as parentTabId,subTabs,\n" +
            " alr.description as helperText\n" +
            "RETURN DISTINCT id(subPage) as id, subPage.name as name,subPage.url as url,subPage.moduleId as moduleId,subPage.active as active,subPage.sequence as sequence,subPage.editable as editable, parentTabId,subPage as accessPage,helperText, " +
            "CASE WHEN count(subTabs)>0 THEN true ELSE false END as hasSubTabs \n"
    )
    List<AccessPageQueryResult> getChildTabsWithHelperText(Long tabId, Long languageId);

}