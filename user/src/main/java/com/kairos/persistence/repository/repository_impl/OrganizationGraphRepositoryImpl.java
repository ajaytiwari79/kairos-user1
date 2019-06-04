package com.kairos.persistence.repository.repository_impl;

import com.kairos.dto.user.organization.hierarchy.OrganizationHierarchyFilterDTO;
import com.kairos.dto.user.staff.client.ClientFilterDTO;
import com.kairos.enums.Employment;
import com.kairos.enums.FilterType;
import com.kairos.enums.ModuleId;
import com.kairos.persistence.model.organization.OrganizationBaseEntity;
import com.kairos.persistence.repository.organization.CustomOrganizationGraphRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.session.Session;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.kairos.enums.CitizenHealthStatus.*;
import static com.kairos.persistence.model.constants.RelationshipConstants.*;

/**
 * Created by oodles on 26/10/17.
 */
@Repository
public class OrganizationGraphRepositoryImpl implements CustomOrganizationGraphRepository {

    @Inject
    private Session session;

    public String appendWhereOrAndPreFixOnQueryString(int countOfSubString) {
        String subString = (countOfSubString == 0 ? " WHERE" : ((countOfSubString > 0) ? " AND" : ""));
        return subString;
    }

    public String getMatchQueryForNameGenderStatusOfStaffByFilters(Map<FilterType, List<String>> filters, String searchText) {
        String matchQueryForStaff = "";
        int countOfSubString = 0;
        if (Optional.ofNullable(filters.get(FilterType.STAFF_STATUS)).isPresent()) {
            matchQueryForStaff += appendWhereOrAndPreFixOnQueryString(countOfSubString) + "  staff.currentStatus IN {staffStatusList} ";
            countOfSubString += 1;
        }
        if (Optional.ofNullable(filters.get(FilterType.GENDER)).isPresent()) {
            matchQueryForStaff += appendWhereOrAndPreFixOnQueryString(countOfSubString) + " user.gender IN {genderList} ";
            countOfSubString += 1;
        }
        if (StringUtils.isNotBlank(searchText)) {
            matchQueryForStaff += appendWhereOrAndPreFixOnQueryString(countOfSubString) +
                    " ( LOWER(staff.firstName) CONTAINS LOWER({searchText}) OR LOWER(staff.lastName) CONTAINS LOWER({searchText}) OR user.cprNumber STARTS WITH {searchText} )";
            countOfSubString += 1;
        }
        return matchQueryForStaff;
    }

    public String getMatchQueryForRelationshipOfStaffByFilters(Map<FilterType, List<String>> filters) {
        String matchRelationshipQueryForStaff = "";
        if (Optional.ofNullable(filters.get(FilterType.EMPLOYMENT_TYPE)).isPresent()) {
            matchRelationshipQueryForStaff += "MATCH(employment)-[:" + HAS_EMPLOYMENT_LINES + "]-(employmentLine:EmploymentLine)  " +
                    "MATCH (employmentLine)-[empRelation:" + HAS_EMPLOYMENT_TYPE + "]-(employmentType:EmploymentType) " +
                    "WHERE id(employmentType) IN {employmentTypeIds}  " +
                    "OPTIONAL MATCH(employment)-[:" + HAS_EXPERTISE_IN + "]-(exp:Expertise) WITH staff,organization,employment,user,exp,employmentType,employmentLine \n" +
                    "OPTIONAL MATCH(employmentLine)-[:" + APPLICABLE_FUNCTION + "]-(function:Function) " +
                    "WITH staff,organization,employment,user, CASE WHEN function IS NULL THEN [] ELSE COLLECT(distinct {id:id(function),name:function.name}) END as functions,employmentLine,exp,employmentType\n" +
                    "WITH staff,organization,employment,user, COLLECT(distinct {id:id(employmentLine),startDate:employmentLine.startDate,endDate:employmentLine.endDate,functions:functions}) as employmentLines,exp,employmentType\n" +
                    "with staff,user,CASE WHEN employmentType IS NULL THEN [] ELSE collect({id:id(employmentType),name:employmentType.name}) END as employmentList, \n" +
                    "COLLECT(distinct {id:id(employment),startDate:employment.startDate,endDate:employment.endDate,expertise:{id:id(exp),name:exp.name},employmentLines:employmentLines,employmentType:{id:id(employmentType),name:employmentType.name}}) as employments ";
        } else {
            matchRelationshipQueryForStaff += "OPTIONAL MATCH(employment)-[:" + HAS_EMPLOYMENT_LINES + "]-(employmentLine:EmploymentLine)  " +
                    "OPTIONAL MATCH(employment)-[:" + HAS_EXPERTISE_IN + "]-(exp:Expertise)\n" +
                    "OPTIONAL MATCH (employmentLine)-[empRelation:" + HAS_EMPLOYMENT_TYPE + "]-(employmentType:EmploymentType)  " +
                    "OPTIONAL MATCH(employmentLine)-[:" + APPLICABLE_FUNCTION + "]-(function:Function) " +
                    "WITH staff,organization,employment,user, CASE WHEN function IS NULL THEN [] ELSE COLLECT(distinct {id:id(function),name:function.name}) END as functions,employmentLine,exp,employmentType\n" +
                    "WITH staff,organization,employment,user, COLLECT(distinct {id:id(employmentLine),startDate:employmentLine.startDate,endDate:employmentLine.endDate,functions:functions}) as employmentLines,exp,employmentType\n" +
                    "with staff,user,CASE WHEN employmentType IS NULL THEN [] ELSE collect({id:id(employmentType),name:employmentType.name}) END as employmentList, \n" +
                    "COLLECT(distinct {id:id(employment),startDate:employment.startDate,endDate:employment.endDate,expertise:{id:id(exp),name:exp.name},employmentLines:employmentLines,employmentType:{id:id(employmentType),name:employmentType.name}}) as employments ";
        }

        if (Optional.ofNullable(filters.get(FilterType.EXPERTISE)).isPresent()) {
            matchRelationshipQueryForStaff += " with staff,employments,user,employmentList  MATCH (staff)-[" + HAS_EXPERTISE_IN + "]-(expertise:Expertise) " +
                    "WHERE id(expertise) IN {expertiseIds} ";
        } else {
            matchRelationshipQueryForStaff += " with staff,employments,user,employmentList  OPTIONAL MATCH (staff)-[" + HAS_EXPERTISE_IN + "]-(expertise:Expertise)  ";
        }

        matchRelationshipQueryForStaff += " with staff,employments, user, employmentList, " +
                "CASE WHEN expertise IS NULL THEN [] ELSE collect({id:id(expertise),name:expertise.name})  END as expertiseList " +
                " with staff, employments,user, employmentList,expertiseList  OPTIONAL Match (staff)-[:" + ENGINEER_TYPE + "]->(engineerType:EngineerType) " +
                " with engineerType,employments, staff, user, employmentList, expertiseList";
        return matchRelationshipQueryForStaff;
    }

    public List<Long> convertListOfStringIntoLong(List<String> listOfString) {
        return listOfString.stream().map(Long::parseLong).collect(Collectors.toList());
    }

    public List<Map> getStaffWithFilters(Long unitId, Long parentOrganizationId, String moduleId,
                                         Map<FilterType, List<String>> filters, String searchText, String imagePath) {

        Map<String, Object> queryParameters = new HashMap();

        queryParameters.put("unitId", unitId);
        queryParameters.put("parentOrganizationId", parentOrganizationId);
        if (Optional.ofNullable(filters.get(FilterType.STAFF_STATUS)).isPresent()) {
            queryParameters.put("staffStatusList",
                    filters.get(FilterType.STAFF_STATUS));
        }
        if (Optional.ofNullable(filters.get(FilterType.GENDER)).isPresent()) {
            queryParameters.put("genderList",
                    filters.get(FilterType.GENDER));
        }
        if (Optional.ofNullable(filters.get(FilterType.EMPLOYMENT_TYPE)).isPresent()) {
            queryParameters.put("employmentTypeIds",
                    convertListOfStringIntoLong(filters.get(FilterType.EMPLOYMENT_TYPE)));
        }
        if (Optional.ofNullable(filters.get(FilterType.EXPERTISE)).isPresent()) {
            queryParameters.put("expertiseIds",
                    convertListOfStringIntoLong(filters.get(FilterType.EXPERTISE)));
        }

        if (StringUtils.isNotBlank(searchText)) {
            queryParameters.put("searchText", searchText);
        }
        queryParameters.put("imagePath", imagePath);

        String query = "";
        if (ModuleId.SELF_ROSTERING_MODULE_ID.value.equals(moduleId)) {
            query += " MATCH (staff:Staff)-[:" + BELONGS_TO_STAFF + "]-(employment:Employment{deleted:false,published:true})-[:" + IN_UNIT + "]-(organization:Unit) where id(organization)={unitId}" +
                    " MATCH (staff)-[:" + BELONGS_TO + "]->(user:User) " + getMatchQueryForNameGenderStatusOfStaffByFilters(filters, searchText) + " WITH user, staff, employment,organization ";
        } else if (Optional.ofNullable(filters.get(FilterType.EMPLOYMENT)).isPresent() && filters.get(FilterType.EMPLOYMENT).contains(Employment.STAFF_WITH_EMPLOYMENT.name()) && !ModuleId.SELF_ROSTERING_MODULE_ID.value.equals(moduleId)) {
            query += " MATCH (staff:Staff)-[:" + BELONGS_TO_STAFF + "]-(employment:Employment{deleted:false})-[:" + IN_UNIT + "]-(organization:Unit) where id(organization)={unitId}" +
                    " MATCH (staff)-[:" + BELONGS_TO + "]->(user:User) " + getMatchQueryForNameGenderStatusOfStaffByFilters(filters, searchText) + " WITH user, staff, employment,organization ";
        } else if (Optional.ofNullable(filters.get(FilterType.EMPLOYMENT)).isPresent() && filters.get(FilterType.EMPLOYMENT).contains(Employment.STAFF_WITHOUT_EMPLOYMENT.name()) && !ModuleId.SELF_ROSTERING_MODULE_ID.value.equals(moduleId)) {
            query += " MATCH (organization:Unit)-[:" + HAS_POSITIONS + "]-(position:Position)-[:" + BELONGS_TO + "]-(staff:Staff) where id(organization)={parentOrganizationId} " +
                    " MATCH(unit:Unit) WHERE id(unit)={unitId}" +
                    " MATCH (staff) WHERE NOT (staff)-[:" + BELONGS_TO_STAFF + "]->(:Employment)-[:" + IN_UNIT + "]-(unit)" +
                    " MATCH (staff)-[:" + BELONGS_TO + "]->(user:User)  " + getMatchQueryForNameGenderStatusOfStaffByFilters(filters, searchText) +
                    " OPTIONAL MATCH (staff)-[:" + BELONGS_TO_STAFF + "]-(employment:Employment)" +
                    " WITH user, staff, employment,organization ";
        } else {
            query += " MATCH (organization:Unit)-[:" + HAS_POSITIONS + "]-(position:Position)-[:" + BELONGS_TO + "]-(staff:Staff) where id(organization)={parentOrganizationId} " +
                    " MATCH (staff)-[:" + BELONGS_TO + "]->(user:User)  " + getMatchQueryForNameGenderStatusOfStaffByFilters(filters, searchText) +
                    " with user, staff OPTIONAL MATCH (staff)-[:" + BELONGS_TO_STAFF + "]-(employment:Employment{deleted:false})-[:" + IN_UNIT + "]-(organization:Unit) where id(organization)={unitId} with user, staff, employment,organization ";
        }

        query += getMatchQueryForRelationshipOfStaffByFilters(filters);

        query += " WITH engineerType, staff,employments, user,expertiseList,employmentList Optional MATCH (staff)-[:" + HAS_CONTACT_ADDRESS + "]-(contactAddress:ContactAddress) ";

        query += " RETURN distinct {id:id(staff), employments:employments,expertiseList:expertiseList,employmentList:collect(employmentList[0]),city:contactAddress.city,province:contactAddress.province, " +
                "firstName:user.firstName,lastName:user.lastName,employedSince :staff.employedSince," +
                "age:duration.between(date(user.dateOfBirth),date()).years," +
                "badgeNumber:staff.badgeNumber, userName:staff.userName,externalId:staff.externalId, access_token:staff.access_token," +
                "cprNumber:user.cprNumber, visitourTeamId:staff.visitourTeamId, familyName: staff.familyName, " +
                "gender:user.gender, pregnant:user.pregnant,  profilePic:{imagePath} + staff.profilePic, engineerType:id(engineerType),user_id:staff.user_id } as staff ORDER BY staff.id\n";

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(session.query(Map.class, query, queryParameters).iterator(), Spliterator.ORDERED), false).collect(Collectors.<Map>toList());
    }

    public List<Map> getClientsWithFilterParameters(ClientFilterDTO clientFilterDTO, List<Long> citizenIds,
                                                    Long organizationId, String imagePath, String skip, String moduleId) {
        Map<String, Object> queryParameters = new HashMap();
        String query = "";
        String dynamicWhereQuery = "";
        queryParameters.put("unitId", organizationId);

        if (clientFilterDTO.getName() != null && !StringUtils.isBlank(clientFilterDTO.getName())) {
            queryParameters.put("name", clientFilterDTO.getName());
            dynamicWhereQuery += "AND ( user.firstName=~{name} OR user.lastName=~{name})";
        }
        if (clientFilterDTO.getCprNumber() != null && !StringUtils.isBlank(clientFilterDTO.getCprNumber())) {
            queryParameters.put("cprNumber", clientFilterDTO.getCprNumber());
            dynamicWhereQuery += "AND user.cprNumber STARTS WITH {cprNumber}";
        }
        queryParameters.put("phoneNumber", clientFilterDTO.getPhoneNumber());
        queryParameters.put("civilianStatus", clientFilterDTO.getClientStatus());
        queryParameters.put("skip", Integer.valueOf(skip));
        queryParameters.put("latLngs", clientFilterDTO.getLocalAreaTags());
        queryParameters.put("citizenIds", citizenIds);

        queryParameters.put("imagePath", imagePath);


        if (Arrays.asList("module_2", "tab_1").contains(moduleId)) {
            queryParameters.put("healthStatus", Arrays.asList(ALIVE, DECEASED, TERMINATED));
        } else {
            queryParameters.put("healthStatus", Arrays.asList(ALIVE, DECEASED));
        }

        if (citizenIds.isEmpty() && clientFilterDTO.getServicesTypes().isEmpty() && clientFilterDTO.getTimeSlots().isEmpty() && clientFilterDTO.getTaskTypes().isEmpty() && !clientFilterDTO.isNewDemands()) {
            query = "MATCH (user:User)<-[:" + IS_A + "]-(c:Client)-[r:" + GET_SERVICE_FROM + "]->(o:Unit) WHERE id(o)= {unitId} AND c.healthStatus IN {healthStatus} " + dynamicWhereQuery + "  with c,r,user\n";
        } else {
            query = "MATCH (user:User)<-[:" + IS_A + "]-(c:Client{healthStatus:'ALIVE'})-[r:" + GET_SERVICE_FROM + "]->(o:Unit) WHERE id(o)= {unitId} AND id(c) in {citizenIds} AND c.healthStatus IN {healthStatus} " + dynamicWhereQuery + " with c,r,user\n";

        }
        query += "OPTIONAL MATCH (c)-[:HAS_HOME_ADDRESS]->(ca:ContactAddress)  with ca,c,r,user\n";
        query += "OPTIONAL MATCH (c)-[houseHoldRel:" + PEOPLE_IN_HOUSEHOLD_LIST + "]-(houseHold) with ca,c,r,houseHoldRel,houseHold,user\n";
        if (clientFilterDTO.getPhoneNumber() == null) {
            query += "OPTIONAL MATCH (c)-[:HAS_CONTACT_DETAIL]->(cd:ContactDetail) with cd,ca,c,r,houseHoldRel,houseHold,user\n";
        } else {
            query += "MATCH (c)-[:HAS_CONTACT_DETAIL]->(cd:ContactDetail) WHERE cd.privatePhone STARTS WITH {phoneNumber} with cd,ca,c,r,houseHoldRel,houseHold,user\n";
        }
        if (clientFilterDTO.getClientStatus() == null) {
            query += "OPTIONAL MATCH (c)-[:CIVILIAN_STATUS]->(cs:CitizenStatus) with cs,cd,ca,c,r,houseHoldRel,houseHold,user\n";
        } else {
            query += "MATCH (c)-[:CIVILIAN_STATUS]->(cs:CitizenStatus) WHERE id(cs) = {civilianStatus} with cs,cd,ca,c,r,houseHoldRel,houseHold,user\n";
        }
        if (clientFilterDTO.getLocalAreaTags().isEmpty()) {
            query += "OPTIONAL MATCH (c)-[:HAS_LOCAL_AREA_TAG]->(lat:LocalAreaTag) with lat,cs,cd,ca,c,r,houseHoldRel,houseHold,user\n";
        } else {
            query += "MATCH (c)-[:HAS_LOCAL_AREA_TAG]->(lat:LocalAreaTag) WHERE id(lat) in {latLngs} with lat,cs,cd,ca,c,r,houseHoldRel,houseHold,user\n";
        }
        query += "return {name:user.firstName+\" \" +user.lastName,id:id(c), healthStatus:c.healthStatus,age:round ((timestamp()-c.dateOfBirth) / (365*24*60*60*1000)), emailId:user.email, profilePic: {imagePath} + c.profilePic, gender:c.gender, cprNumber:c.cprNumber , citizenDead:c.citizenDead, joiningDate:r.joinDate,city:ca.city,";
        query += "address:ca.houseNumber+\" \" +ca.street1, phoneNumber:cd.privatePhone, workNumber:cd.workPhone, clientStatus:id(cs), lat:ca.latitude, lng:ca.longitude, ";
        query += "localAreaTag:CASE WHEN lat IS NOT NULL THEN {id:id(lat), name:lat.name} ELSE NULL END,houseHoldList:case when houseHoldRel is null then [] else collect({id:id(houseHold),firstName:houseHold.firstName,lastName:houseHold.lastName}) end}  as Client ORDER BY Client.name ASC SKIP {skip} LIMIT 20 ";
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(session.query(Map.class, query, queryParameters).iterator(), Spliterator.ORDERED), false).collect(Collectors.<Map>toList());

    }

    //@Override
    public OrganizationBaseEntity getOrganizationHierarchyByFilters(long parentOrganizationId, OrganizationHierarchyFilterDTO organizationHierarchyFilterDTO) {
        String filterQuery = "";
        final String SUB_ORGANIZATIONS = "subOrganizations";
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("parentOrganizationId", parentOrganizationId);

        if (organizationHierarchyFilterDTO != null) {
            //organizationType Filter
            if (CollectionUtils.isNotEmpty(organizationHierarchyFilterDTO.getOrganizationTypeIds())) {
                filterQuery = filterQuery + " Match(organizationType:OrganizationType)-[:" + TYPE_OF + "]-(" + SUB_ORGANIZATIONS + ") WHERE id(organizationType) IN {organizationTypeIds} ";
                queryParameters.put("organizationTypeIds", organizationHierarchyFilterDTO.getOrganizationTypeIds());
            }
            if (CollectionUtils.isNotEmpty(organizationHierarchyFilterDTO.getOrganizationSubTypeIds())) {
                filterQuery = filterQuery + " Match(organizationSubType:OrganizationType)-[:" + SUB_TYPE_OF + "]-(" + SUB_ORGANIZATIONS + ") WHERE id(organizationSubType) IN {organizationSubTypeIds} ";
                queryParameters.put("organizationSubTypeIds", organizationHierarchyFilterDTO.getOrganizationSubTypeIds());
            }
            //organizationService Filter
            if (CollectionUtils.isNotEmpty(organizationHierarchyFilterDTO.getOrganizationServiceIds())) {
                filterQuery = filterQuery + " Match(organizationService:OrganizationService)-[:" + HAS_CUSTOM_SERVICE_NAME_FOR + "]-(" + SUB_ORGANIZATIONS + ") WHERE id(organizationService) IN {organizationServiceIds} ";
                queryParameters.put("organizationServiceIds", organizationHierarchyFilterDTO.getOrganizationServiceIds());
            }
            if (CollectionUtils.isNotEmpty(organizationHierarchyFilterDTO.getOrganizationSubServiceIds())) {
                filterQuery = filterQuery + " Match(organizationSubService:OrganizationService)-[:" + PROVIDE_SERVICE + "]-(" + SUB_ORGANIZATIONS + ") WHERE id(organizationSubService) IN {organizationSubServiceIds} ";
                queryParameters.put("organizationSubServiceIds", organizationHierarchyFilterDTO.getOrganizationSubServiceIds());
            }

            //accountType Filter
            if (CollectionUtils.isNotEmpty(organizationHierarchyFilterDTO.getOrganizationAccountTypeIds())) {
                filterQuery = filterQuery + " Match(accountType:AccountType)-[:" + HAS_ACCOUNT_TYPE + "]-(" + SUB_ORGANIZATIONS + ") WHERE id(accountType) IN {accountTypeIds} ";
                queryParameters.put("accountTypeIds", organizationHierarchyFilterDTO.getOrganizationAccountTypeIds());
            }
        }

        String query = "MATCH(o{isEnable:true,boardingCompleted: true}) where id(o)={parentOrganizationId}\n" + filterQuery +
                "OPTIONAL MATCH(o)-[orgRel:HAS_SUB_ORGANIZATION*]->(org:Organization{isEnable:true,boardingCompleted: true})\n" + filterQuery +
                "OPTIONAL MATCH(o)-[unitRel:HAS_UNIT]->(u:Unit{isEnable:true,boardingCompleted: true})\n" + filterQuery +
                "OPTIONAL MATCH(org)-[orgUnitRel:HAS_UNIT]->(un:Unit{isEnable:true,boardingCompleted: true})\n" +
                "RETURN o,org,orgRel,unitRel,u,orgUnitRel,un";

        return session.queryForObject(OrganizationBaseEntity.class, query, queryParameters);
    }
}
