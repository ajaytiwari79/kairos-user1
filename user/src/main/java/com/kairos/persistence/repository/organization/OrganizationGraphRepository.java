package com.kairos.persistence.repository.organization;
/*
 *Created By Pavan on 27/5/19
 *
 */

import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.organization.union.UnionDataQueryResult;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.kairos.persistence.model.constants.RelationshipConstants.*;

@Repository
public interface OrganizationGraphRepository extends Neo4jBaseRepository<Organization,Long> {

    Optional<Organization> findByExternalId(String externalId);

    @Query("MATCH(o:Organization{isEnable:true,boardingCompleted: true,isKairoHub:true}) RETURN o limit 1")
    Organization findHub();


    @Query("MATCH(o{isEnable:true,boardingCompleted: true}) where id(o) = {0} "+
            "OPTIONAL MATCH(o)-[orgRel:"+HAS_SUB_ORGANIZATION+"*]->(org:Organization{isEnable:true,boardingCompleted: true}) " +
            "OPTIONAL MATCH(o)-[unitRel:"+HAS_UNIT+"]->(u:Unit{isEnable:true,boardingCompleted: true}) " +
            "OPTIONAL MATCH(org)-[orgUnitRel:"+HAS_UNIT+"]->(un:Unit{isEnable:true,boardingCompleted: true}) " +
            "RETURN o,org,orgRel,unitRel,u,orgUnitRel,un")
    List<Organization> generateHierarchy(Long id);

    @Query("MATCH (union:Organization{union:true,isEnable:true}) WHERE id (union)={0}  RETURN union")
    Organization findByIdAndUnionTrueAndIsEnableTrue(Long unionId);

    @Query("MATCH(union:Organization{deleted:false,union:true}) WHERE id(union)<>{1} AND union.name=~{0} RETURN count(union)>0")
    boolean existsByName(String name,Long unionId);

    @Query("MATCH(union:Organization{deleted:false,union:true}) WHERE id(union)={0} or union.name={1} WITH union MATCH(union)-[:" + BELONGS_TO + "]-(country:Country) WITH union,country OPTIONAL " +
            "MATCH(union)-[:" + HAS_SECTOR + "]-(sector:Sector) WITH union,collect(sector) as sectors,country OPTIONAL MATCH(union)-[:" + CONTACT_ADDRESS + "]-" +
            "(address:ContactAddress) OPTIONAL MATCH(address)-[:" + ZIP_CODE + "]-(zipCode:ZipCode) WITH union,sectors,address,zipCode,country OPTIONAL MATCH(address)-[:" + MUNICIPALITY + "]-" +
            "(municipality:Municipality) WITH union,sectors,address,zipCode,country,municipality " +
            "OPTIONAL MATCH(union)-[:" + HAS_LOCATION + "]-(location:Location{deleted:false})" +
            "RETURN union,country,address,zipCode,sectors,municipality,collect(location) as locations ")
    List<UnionDataQueryResult> getUnionCompleteById(Long unionId, String name);

    @Query("MATCH (n:Organization) WHERE id(n)={0}\n" +
            "MATCH (n)-[:" + SUB_TYPE_OF + "]->(subType:OrganizationType) WITH subType,n\n" +
            "MATCH (subType)-[:" + ORGANIZATION_TYPE_HAS_SERVICES + "]->(organizationService:OrganizationService) WITH organizationService,n\n" +
            "create unique (n)-[:" + PROVIDE_SERVICE + "{isEnabled:true,creationDate:{1},lastModificationDate:{2}}]->(organizationService) ")
    void assignDefaultServicesToOrg(long orgId, long creationDate, long lastModificationDate);


    @Query("MATCH (organization:Organization) WHERE id(organization)={0} \n" +
            "MATCH (organization)-[:" + SUB_TYPE_OF + "]->(subType:OrganizationType) \n" +
            "MATCH (subType)-[:" + ORG_TYPE_HAS_SKILL + "]->(skill:Skill) WITH skill,organization\n" +
            "create unique (organization)-[r:" + ORGANISATION_HAS_SKILL + "{creationDate:{1},lastModificationDate:{2},isEnabled:true,customName:skill.name}]->(skill)")
    void assignDefaultSkillsToOrg(long orgId, long creationDate, long lastModificationDate);

    @Query("MATCH(org:Organization{union:false,deleted:false,isEnable:true}) WHERE id(org) IN {0} RETURN COUNT(org)=2")
    boolean verifyOrganization(List<Long> orgIds);

    @Query("MATCH(org:Organization{union:false,deleted:false,isEnable:true}),(newOrg:Organization{union:false,deleted:false,isEnable:true})<-[hubRelationShip:"+HAS_SUB_ORGANIZATION+"]-(hub:Organization{isKairosHub: true,isParentOrganization: true}) WHERE id(org)={0} AND id(newOrg)={1} " +
            "CREATE UNIQUE(org)-[:"+HAS_SUB_ORGANIZATION+"]->(newOrg)  DETACH DELETE  hubRelationShip ")
    void mergeTwoOrganizations(Long orgId,Long newOrgId);

    @Query("MATCH(organization:Organization),(hub:Organization) WHERE id(organization)={0} AND id(hub)={1} " +
            "CREATE UNIQUE(hub)-[r:"+HAS_SUB_ORGANIZATION+"]->(organization)  ")
    void linkOrganizationToHub(Long organizationId,Long hubId);

    @Query("MATCH(o:Organization) where id(o)={0} \n" +
            "OPTIONAL MATCH(o)-[:"+HAS_SUB_ORGANIZATION+"*]->(sub:Organization) \n" +
            "WITH collect(id(sub))+id(o) as orgIds \n" +
            "unwind orgIds as x \n" +
            "return x ")
    List<Long> findAllOrganizationIdsInHierarchy(Long parentOrgId);

    @Query("MATCH (org:Organization)-[:"+HAS_POSITIONS+"]->(position:Position)-[:"+BELONGS_TO+"]->(staff) WHERE id(staff)={0} return org")
    Organization findOrganizationOfStaff(Long staffId);

    @Query("MATCH(u:Unit)-[:HAS_UNIT]-(o:Organization)-[:BELONGS_TO]-(c:Country) where id(c)={0} \n" +
            "with collect(id(u)) as u, collect(id(o)) as o \n" +
            "WITH u+o as units \n" +
            "unwind units as x with distinct x \n" +
            "RETURN x")
    List<Long> getAllUnitsByCountryId(Long countryId);

    @Query("MATCH(ex:Expertise)  WHERE id(ex)={0} " +
            "OPTIONAL MATCH(ex)-[rel:"+BELONGS_TO_SECTOR+"]-(sector:Sector)\n" +
            "with ex,rel  " +
            "MATCH(newSector:Sector) where id(newSector) = {1} \n" +
            "DETACH delete rel \n" +
            "CREATE UNIQUE(ex)-[:"+BELONGS_TO_SECTOR+"]-(newSector) ")
    void addSector(Long expertiseId,Long sectorId);

    @Query("MATCH(ex:Expertise)  WHERE id(ex)={0} " +
            "OPTIONAL MATCH(ex)-[rel:"+SUPPORTED_BY_UNION+"]-(union:Organization)\n" +
            "with ex,rel  " +
            "MATCH(newUnion:Organization) where id(newUnion) = {1} \n" +
            "DETACH delete rel \n" +
            "CREATE UNIQUE(ex)-[:"+SUPPORTED_BY_UNION+"]-(newUnion) ")
    void addUnion(Long expertiseLineId,Long unionId);

    @Query("MATCH (org:Organization)-[:" + SUB_TYPE_OF + "]->(orgType:OrganizationType) WHERE id(orgType) IN {0} \n" +
            "RETURN org")
    List<Organization> getOrganizationsBySubOrgTypeIds(List<Long> organizationSubTypeIds);

    @Query("MATCH(o:Organization)-[:HAS_SUB_ORGANIZATION]-(union:Organization{union:true}) WHERE id(o)={0}\n" +
            "return union\n" +
            "Union \n" +
            "MATCH(c:Country)-[:BELONGS_TO]-(union:Organization{union:true}) WHERE id(c)={1}\n" +
            "RETURN union")
    List<Organization> getAllUnionsByOrganizationOrCountryId(Long organizationId,Long countryId);

    @Query("MATCH (n:Organization) where n.name='Kairos' return id(n)")
    Long findKairosOrganizationId();
}
