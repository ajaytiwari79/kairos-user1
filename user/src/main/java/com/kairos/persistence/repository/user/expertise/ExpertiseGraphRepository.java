package com.kairos.persistence.repository.user.expertise;

import com.kairos.dto.user.country.experties.ExpertiseDTO;
import com.kairos.persistence.model.organization.union.Location;
import com.kairos.persistence.model.user.expertise.Expertise;
import com.kairos.persistence.model.user.expertise.ExpertiseLine;
import com.kairos.persistence.model.user.expertise.response.ExpertiseBasicDetails;
import com.kairos.persistence.model.user.expertise.response.ExpertiseLineQueryResult;
import com.kairos.persistence.model.user.expertise.response.ExpertiseQueryResult;
import com.kairos.persistence.model.user.expertise.response.ExpertiseTagDTO;
import com.kairos.persistence.model.user.filter.FilterSelectionQueryResult;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static com.kairos.persistence.model.constants.RelationshipConstants.*;


/**
 * Created by prabjot on 28/10/16.
 */
@Repository
public interface ExpertiseGraphRepository extends Neo4jBaseRepository<Expertise, Long> {

    @Query("MATCH (country:Country) WHERE id(country)={0}  " +
            "MATCH (country)<-[:"+BELONGS_TO+"]-(expertise:Expertise{deleted:false,published:true}) WHERE  (expertise.endDate IS NULL OR DATE(expertise.endDate) >= DATE()) " +
            "RETURN expertise")
    List<Expertise> getAllExpertiseByCountry(long countryId);

    @Query("MATCH (country:Country) WHERE id(country)={0} MATCH (country)<-[:"+BELONGS_TO+"]-(expertise:Expertise{deleted:false,published:true}) RETURN expertise LIMIT 1")
    Expertise getOneDefaultExpertiseByCountry(long countryId);

    @Query("MATCH (country:Country) WHERE id(country)={0} " +
            "MATCH (country)<-[:"+BELONGS_TO+"]-(expertise:Expertise{deleted:false,published:true}) WHERE  (expertise.endDate IS NULL OR DATE(expertise.endDate) >= DATE())  " +
            "WITH expertise, country \n" +
            "RETURN id(expertise) as id, expertise.name as name")
    List<ExpertiseTagDTO> getAllExpertiseWithTagsByCountry(long countryId);



    @Override
    @Query("MATCH (expertise:Expertise{deleted:false,published:true}) RETURN expertise")
    List<Expertise> findAll();


    @Query("MATCH (e:Expertise{deleted:false,published:true})-[:" + BELONGS_TO + "]->(country:Country) WHERE id(country) = {0} AND id(e) = {1} RETURN e")
    Expertise getExpertiesOfCountry(Long countryId, Long expertiseId);


    @Query("MATCH (country:Country)<-[:" + BELONGS_TO + "]-(expertise:Expertise{deleted:false})-[:"+HAS_EXPERTISE_LINES+"]->(exl:ExpertiseLine) WHERE id(country) = {0} AND expertise.published IN {1} " +
            "OPTIONAL MATCH(expertise)-[:" + IN_ORGANIZATION_LEVEL + "]-(level:Level) \n" +
            "OPTIONAL MATCH(expertise)-[:" + SUPPORTED_BY_UNION + "]-(union:Organization)  " +
            "OPTIONAL MATCH(expertise)-[:"+BELONGS_TO_SECTOR+"]-(sector:Sector) "+
            "OPTIONAL MATCH(expertise)-[:" + HAS_SENIOR_DAYS + "]->(seniorDays:CareDays) \n " +
            "OPTIONAL MATCH(expertise)-[:" + HAS_CHILD_CARE_DAYS + "]->(childCareDays:CareDays) \n" +
            "with expertise,level,union,sector, " +
            "CASE when seniorDays IS NULL THEN [] ELSE collect(DISTINCT {id:id(seniorDays),from:seniorDays.from,to:seniorDays.to,leavesAllowed:seniorDays.leavesAllowed}) END as seniorDays, " +
            "CASE when childCareDays IS NULL THEN [] ELSE collect(DISTINCT {id:id(childCareDays),from:childCareDays.from,to:childCareDays.to,leavesAllowed:childCareDays.leavesAllowed}) END as childCareDays " +
            "RETURN expertise.translations as translations,\n" +
            "expertise.name as name ,id(expertise) as id,expertise.creationDate as creationDate, expertise.startDate as startDate , " +
            "expertise.endDate as endDate ,expertise.description as description ,expertise.breakPaymentSetting as breakPaymentSetting,expertise.published as published,level as organizationLevel,union,sector, " +
            "seniorDays,childCareDays ORDER BY expertise.name")
    List<ExpertiseQueryResult> getAllExpertise(long countryId, boolean[] published);

    @Query("MATCH(expertise:Expertise)-["+HAS_EXPERTISE_LINES+"]-(exl:ExpertiseLine) WHERE id(expertise) IN {0} " +
            "OPTIONAL MATCH(exl)-[:" + SUPPORTS_SERVICES + "]-(orgService:OrganizationService)\n" +
            "with expertise,exl, Collect(orgService) as services \n" +
            "OPTIONAL MATCH(exl)-[:" + FOR_SENIORITY_LEVEL + "]->(seniorityLevel:SeniorityLevel)-[:"+HAS_BASE_PAY_GRADE+"]->(payGradeData:PayGrade)<-[:" + HAS_PAY_GRADE + "]-(payTable:PayTable)" +
            " with expertise,exl,services,Collect(payTable) as payTables, CASE when seniorityLevel IS NULL THEN [] ELSE collect({id:id(seniorityLevel),from:seniorityLevel.from,pensionPercentage:seniorityLevel.pensionPercentage," +
            "   freeChoicePercentage:seniorityLevel.freeChoicePercentage,freeChoiceToPension:seniorityLevel.freeChoiceToPension, " +
            "   to:seniorityLevel.to,payGrade:{id:id(payGradeData), payGradeLevel :payGradeData.payGradeLevel}})  END  as seniorityLevels "+
            "RETURN id(exl) as id ,id(expertise) as expertiseId, exl.startDate as startDate , " +
            "exl.endDate as endDate ,exl.fullTimeWeeklyMinutes as fullTimeWeeklyMinutes,exl.numberOfWorkingDaysInWeek as numberOfWorkingDaysInWeek, " +
            "services as organizationServices,payTables[0] as payTable,seniorityLevels ORDER BY exl.startDate")
    List<ExpertiseLineQueryResult> findAllExpertiseLines(List<Long> expertiseIds);

    @Query("MATCH (expertise:Expertise{deleted:false}) WHERE id(expertise) IN {0} \n" +
            "RETURN id(expertise) as id,expertise.name as name,expertise.description as description")
    List<Expertise> getExpertiseByIdsIn(List<Long> ids);

    @Query("MATCH(expertise:Expertise{deleted:false})  WHERE id(expertise) = {0} " +
            "RETURN expertise.name as name ,id(expertise) as id,expertise.creationDate as creationDate, expertise.startDate as startDate , " +
            "expertise.endDate as endDate ,expertise.description as description ,expertise.published as published,expertise.breakPaymentSetting as breakPaymentSetting ORDER BY expertise.name")
    ExpertiseQueryResult getExpertiseById(Long expertiseId);

    @Query("MATCH(expertise:Expertise{deleted:false}) where expertise.name=~{0} with count(expertise) as expertiseCount " +
            "RETURN case when expertiseCount>0 THEN  true ELSE false END as response")
    boolean findExpertiseByUniqueName(String expertiseName);


    @Query("MATCH (country:Country)<-[:" + BELONGS_TO + "]-(expertise:Expertise{deleted:false,published:true})-[:"+HAS_EXPERTISE_LINES+"]-(exl:ExpertiseLine) WHERE id(country) = {0} AND (expertise.endDate IS NULL OR DATE(expertise.endDate) >= DATE())\n" +
            "MATCH(exl)-[:" + SUPPORTS_SERVICES + "]-(orgService:OrganizationService) WHERE id(orgService) IN {1}\n" +
            "RETURN expertise order by expertise.creationDate")
    List<Expertise> getExpertiseByCountryAndOrganizationServices(Long countryId, Set<Long> organizationServicesIds);


    @Query("MATCH (country:Country)<-[:" + BELONGS_TO + "]-(expertise:Expertise{deleted:false,published:true})-[:"+HAS_EXPERTISE_LINES+"]->(exl:ExpertiseLine) WHERE id(country) = {0} AND (DATE(exl.startDate)<=DATE() AND (exl.endDate IS NULL OR DATE(exl.endDate)>=DATE())) \n" +
            "MATCH(exl)-[:" + SUPPORTS_SERVICES + "]-(orgService:OrganizationService) WHERE id(orgService) IN {1} \n " +
            "RETURN DISTINCT toString(id(expertise)) as id, expertise.name as value ," +
            "expertise.startDate as startDate ,expertise.endDate as endDate ORDER BY startDate")
    List<FilterSelectionQueryResult> getExpertiseByCountryIdForFilters(Long countryId, Set<Long> servicesIds);

    @Query("MATCH(organizationType:OrganizationType) WHERE id(organizationType)={1}\n" +
            "MATCH(organizationType)-[:"+ORGANIZATION_TYPE_HAS_SERVICES+"]-(os:OrganizationService)\n" +
            " MATCH(os)<-[:"+SUPPORTS_SERVICES+"]-(exl:ExpertiseLine)-["+HAS_EXPERTISE_LINES+"]-(expertise:Expertise{deleted:false}) WHERE expertise.published AND  (expertise.endDate IS NULL OR DATE(expertise.endDate) >= DATE())\n" +
            "RETURN distinct id(expertise) as id,expertise.name as name")
    List<ExpertiseBasicDetails> getExpertiseByOrganizationSubType(Long countryId, Long organizationSubTypeId);

    @Query("MATCH (country:Country) WHERE id(country)={0}  " +
            "MATCH (country)<-[:"+BELONGS_TO+"]-(expertise:Expertise{deleted:false,published:true}) WHERE  (expertise.endDate IS NULL OR DATE(expertise.endDate) >= DATE()) " +
            "RETURN id(expertise) as id , expertise.name as name")
    List<ExpertiseBasicDetails> getAllExpertiseByCountryAndDate(long countryId);

    @Query("MATCH (country:Country)<-[:" + BELONGS_TO + "]-(expertise:Expertise{deleted:false,published:true})-[:"+HAS_EXPERTISE_LINES+"]->(exl:ExpertiseLine) WHERE id(country) = {0} AND (DATE(exl.startDate)<=DATE() AND (exl.endDate IS NULL OR DATE(exl.endDate)>=DATE()))  " +
            "MATCH(expertise)-[:" + BELONGS_TO_SECTOR + "]-(sector:Sector)\n" +
            "OPTIONAL MATCH(expertise)-[:" + IN_ORGANIZATION_LEVEL + "]-(organizationLevel:Level) \n" +
            "OPTIONAL MATCH(expertise)-[:" + SUPPORTED_BY_UNION + "]-(union:Organization) \n" +
            "OPTIONAL MATCH(expertise)-[:" + HAS_SENIOR_DAYS + "]->(seniorDays:CareDays) \n " +
            "OPTIONAL MATCH(expertise)-[:" + HAS_CHILD_CARE_DAYS + "]->(childCareDays:CareDays) \n" +
            "WITH DISTINCT expertise,exl,seniorDays,childCareDays,sector,organizationLevel,union  " +
            "MATCH(exl)-[:" + SUPPORTS_SERVICES + "]-(orgService:OrganizationService) WHERE id(orgService) IN {1}\n" +
            "MATCH(exl)-[:" + FOR_SENIORITY_LEVEL + "]->(seniorityLevel:SeniorityLevel) " +
            "with expertise,exl,seniorityLevel,sector,organizationLevel,union , " +
            "CASE WHEN seniorDays IS NULL THEN [] ELSE COLLECT(DISTINCT {id:id(seniorDays),from:seniorDays.from,to:seniorDays.to,leavesAllowed:seniorDays.leavesAllowed}) END as seniorDays, " +
            "CASE WHEN childCareDays IS NULL THEN [] ELSE COLLECT(DISTINCT {id:id(childCareDays),from:childCareDays.from,to:childCareDays.to,leavesAllowed:childCareDays.leavesAllowed}) END as childCareDays ORDER BY  seniorityLevel.from \n" +
            "RETURN DISTINCT expertise.translations as translations,\n" +
            "expertise.name as name ,id(expertise) as id,expertise.creationDate as creationDate, expertise.startDate as startDate ," +
            "expertise.endDate as endDate ,exl.fullTimeWeeklyMinutes as fullTimeWeeklyMinutes,exl.numberOfWorkingDaysInWeek as numberOfWorkingDaysInWeek," +
             " seniorDays,childCareDays,sector,organizationLevel,union  order by expertise.name")
    List<ExpertiseQueryResult> findExpertiseByOrganizationServicesForUnit(Long countryId, Set<Long> organizationServicesIds);

    @Query("MATCH(expertise:Expertise{deleted:false,published:true})-[:" + SUPPORTED_BY_UNION + "]-(union:Organization)-[:"+HAS_LOCATION+"]-(location:Location{deleted:false})  WHERE id(expertise)={0}" +
            " RETURN location as name ORDER BY location.name ASC" )
    List<Location> findAllLocationsOfUnionInExpertise(Long expertiseId);

    @Query("MATCH(expertise:Expertise{deleted:false,published:true})-[:"+HAS_EXPERTISE_LINES+"]->(exl:ExpertiseLine)-[:"+SUPPORTS_SERVICES+"]->(os)<-[:"+PROVIDE_SERVICE+"{isEnabled:true}]-(unit:Unit) WHERE expertise.endDate IS NULL OR DATE(expertise.endDate) >= DATE()\n" +
            "RETURN id(expertise) as id,expertise.name as name, collect(id(unit)) as supportedUnitIds")
    List<ExpertiseQueryResult> findAllExpertiseWithUnitIds();

    @Query("MATCH(expertise:Expertise{deleted:false,published:true})-[:"+HAS_EXPERTISE_LINES+"]-(exl:ExpertiseLine) WHERE id(expertise) = {0} AND (DATE(exl.startDate)<=DATE({1}) AND (exl.endDate IS NULL OR DATE(exl.endDate)>=DATE({1})))" +
            "RETURN exl LIMIT 1")
    ExpertiseLine getCurrentlyActiveExpertiseLineByDate(Long expertiseId, String startDate);

    @Query("MATCH (e:Expertise{deleted:false,published:true})-[:" + BELONGS_TO + "]->(country:Country) WHERE id(country) = {0} RETURN e")
    List<Expertise> getExpertiseOfCountry(Long countryId);


    @Query("MATCH (expertise:Expertise{deleted:false,published:true})-[:"+HAS_EXPERTISE_LINES+"]->(exl:ExpertiseLine) WHERE  (DATE(exl.startDate)<=DATE() AND (exl.endDate IS NULL OR DATE(exl.endDate)>=DATE()))  " +
            "MATCH(exl)-[:" + SUPPORTS_SERVICES + "]-(orgService:OrganizationService) WHERE id(orgService) IN {0}\n" +
            "RETURN id(expertise) ")
    List<Long> getExpertiseIdsByServices(Set<Long> organizationServicesIds);

    @Query("Match(expertise:Expertise) where id(expertise)={0}\n" +
            "Match(employment:Employment)-[r:HAS_EXPERTISE_IN]-(et:Expertise) where id(employment)={1} Detach delete r\n" +
            "CREATE UNIQUE(employment)-[r1:HAS_EXPERTISE_IN]->(expertise)")
    void updateExpertiseByExpertiseIdAndEmploymentId(Long expertiseId,Long employmentId);

    @Query("MATCH (e:Expertise{deleted:false,published:true})-[:" + BELONGS_TO + "]->(country:Country) WHERE id(country) = {0} RETURN id(e)")
    Set<Long> getExpertiseIdsByCountryId(Long countryId);
 }
