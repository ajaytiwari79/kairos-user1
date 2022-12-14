package com.kairos.persistence.model.staff;

import com.kairos.dto.user.country.agreement.cta.cta_response.DayTypeDTO;
import com.kairos.dto.user.skill.SkillLevelDTO;
import com.kairos.dto.user.staff.staff.StaffChildDetailDTO;
import com.kairos.persistence.model.country.tag.TagQueryResult;
import com.kairos.persistence.model.organization.team.TeamDTO;
import com.kairos.persistence.model.user.employment.query_result.EmploymentQueryResult;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.ArrayList;
import java.util.List;

import static com.kairos.commons.utils.ObjectUtils.isNullOrElse;

@QueryResult
@Getter
@Setter
public class StaffKpiFilterQueryResult {
    private Long id;
    private String firstName;
    private String lastName;
    private List<Long> unitIds;
    private Long unitId;
    private String unitName;
    private String cprNumber;
    private int staffAge;
    private List<EmploymentQueryResult> employment;
    private List<DayTypeDTO> dayTypeDTOS;
    private List<TeamDTO> teams;
    private List<StaffChildDetailDTO> staffChildDetails;
    private long payTableAmount;
    private List<TagQueryResult> tags;
    private List<SkillLevelDTO> skills;

    public StaffKpiFilterQueryResult() {
        //Default Constructor
    }

    public List<EmploymentQueryResult> getEmployment() {
        return isNullOrElse(employment,new ArrayList<>());
    }
}
