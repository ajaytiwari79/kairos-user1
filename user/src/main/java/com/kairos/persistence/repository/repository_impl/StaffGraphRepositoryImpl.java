package com.kairos.persistence.repository.repository_impl;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.persistence.model.staff.StaffKpiFilterQueryResult;
import com.kairos.persistence.model.staff.StaffUnitPositionQueryResult;
import com.kairos.persistence.repository.user.staff.CustomStaffGraphRepository;
import com.kairos.dto.activity.open_shift.priority_group.StaffIncludeFilterDTO;
import org.apache.commons.collections.CollectionUtils;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.kairos.persistence.model.constants.RelationshipConstants.*;

@Repository
public class StaffGraphRepositoryImpl implements CustomStaffGraphRepository {

    @Inject
    private Session session;

    public List<StaffUnitPositionQueryResult> getStaffByPriorityGroupStaffIncludeFilter(StaffIncludeFilterDTO staffIncludeFilterDTO, Long unitId) {


        String staffFilterQuery = "Match (up:UnitPosition)-[:" + IN_UNIT + "]-(org:Organization) where id(org)={unitId} and (up.startDateMillis<{maxDate} and (up.endDateMillis is null or up.endateMillis>{maxDate}))" +
                " Match(up)-[:" + HAS_EXPERTISE_IN + "]-(expertise:Expertise) where id(expertise) in {expertiseIds}";

        StringBuilder stringBuilder = new StringBuilder();

        if (Optional.ofNullable(staffIncludeFilterDTO.getEmploymentTypeIds()).isPresent() && !staffIncludeFilterDTO.getEmploymentTypeIds().isEmpty() || staffIncludeFilterDTO.isAllowForFlexPool()) {
            stringBuilder = new StringBuilder(" Match(up)-[:" + HAS_EMPLOYMENT_TYPE + "]-(employmentType:EmploymentType) where ");
            if (Optional.ofNullable(staffIncludeFilterDTO.getEmploymentTypeIds()).isPresent() && !staffIncludeFilterDTO.getEmploymentTypeIds().isEmpty()) {
                stringBuilder.append("id(employmentType) in {employmentTypeIds} or ");
            }
            if (staffIncludeFilterDTO.isAllowForFlexPool()) {
                stringBuilder.append("employmentType.allowedForFlexPool = true or ");
            }

            int index = stringBuilder.lastIndexOf("or");
            stringBuilder.replace(index, index + "or".length(), "");

        }
        stringBuilder.append(" Match(staff)-[:" + BELONGS_TO_STAFF + "]-(up) ");
        stringBuilder.append("return {staffId:id(staff),staffEmail:staff.email,unitPositionId:id(up),workingDaysPerWeek:up.workingDaysInWeek,contractedMinByWeek:up.totalWeeklyMinutes," +
                " startDate:up.startDateMillis, endDate:up.endDateMillis } as data ");
        staffFilterQuery += stringBuilder.toString();
        Map<String, Object> queryParameters = new HashMap();
        queryParameters.put("unitId", unitId);
        queryParameters.put("expertiseIds", staffIncludeFilterDTO.getExpertiseIds());
        queryParameters.put("maxDate", staffIncludeFilterDTO.getMaxOpenShiftDate());
        queryParameters.put("employmentTypeIds", staffIncludeFilterDTO.getEmploymentTypeIds());
        List<Map> result=StreamSupport.stream(Spliterators.spliteratorUnknownSize(session.query(Map.class , staffFilterQuery, queryParameters).iterator(), Spliterator.ORDERED), false).collect(Collectors.<Map> toList());
        List<StaffUnitPositionQueryResult> staffUnitPositionList = ObjectMapperUtils.copyPropertiesOfListByMapper(result,StaffUnitPositionQueryResult.class);


        return staffUnitPositionList;

    }

    @Override
    public List<StaffKpiFilterQueryResult> getStaffsByFilter(Long organizationId, List<Long> unitIds, List<Long> employmentType, String startDate, String endDate, List<Long> staffIds) {
        Map<String, Object> queryParameters = new HashMap();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MATCH (org:Organization)");
        if(CollectionUtils.isNotEmpty(unitIds)){
            stringBuilder.append(" WHERE id(org) IN {unitIds}");
            queryParameters.put("unitIds", unitIds);
        }else{
            stringBuilder.append(" WHERE id(org) = {organizationId}");
            queryParameters.put("organizationId", organizationId);
        }
        if(CollectionUtils.isNotEmpty(employmentType)) {
            stringBuilder.append(" MATCH(empType:EmploymentType) WHERE id(empType) IN {employmentType}");
            queryParameters.put("employmentType", employmentType);
        }
        if(CollectionUtils.isEmpty(unitIds)){
            stringBuilder.append(" MATCH (org)-[:"+HAS_EMPLOYMENTS+"]-(emp:Employment)-[:"+BELONGS_TO+"]-(staff:Staff)-[:" + BELONGS_TO_STAFF + "]-(up:UnitPosition{deleted:false}) ");
        }else {
            stringBuilder.append(" MATCH (org)-[:" + IN_UNIT + "]-(up:UnitPosition{deleted:false})-[:" + BELONGS_TO_STAFF + "]-(staff:Staff)");
        }
        if(CollectionUtils.isNotEmpty(staffIds)) {
            stringBuilder.append(" WHERE id(staff) IN {staffIds}");
            queryParameters.put("staffIds",staffIds);
        }
        stringBuilder.append(" MATCH (up)-[:"+HAS_POSITION_LINES+"]-(positionLine:UnitPositionLine)"+
                "WHERE  date(positionLine.startDate) <= date({endDate}) AND (NOT exists(positionLine.endDate) OR date(positionLine.endDate) >= date({startDate}))"+
                "MATCH (positionLine)-[:"+HAS_EMPLOYMENT_TYPE+"]-(empType) RETURN   id(staff) as id,staff.firstName as firstName ,staff.lastName as lastName,collect(positionLine) as positionLines");
        queryParameters.put("endDate", endDate);
        queryParameters.put("startDate", startDate);

        Result results=session.query( stringBuilder.toString(), queryParameters);
        List<StaffKpiFilterQueryResult> staffKpiFilterQueryResults= new ArrayList<>();
        results.forEach(result->{
            staffKpiFilterQueryResults.add(ObjectMapperUtils.copyPropertiesByMapper(result,StaffKpiFilterQueryResult.class));
        });

        return staffKpiFilterQueryResults;
//
    }


}
