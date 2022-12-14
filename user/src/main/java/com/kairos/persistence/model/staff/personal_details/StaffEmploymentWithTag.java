package com.kairos.persistence.model.staff.personal_details;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.dto.user.country.agreement.cta.cta_response.EmploymentTypeDTO;
import com.kairos.dto.user.country.experties.ExpertiseDTO;
import com.kairos.dto.user.country.skill.SkillDTO;
import com.kairos.dto.user.employment.PlanningEmploymentDTO;
import com.kairos.enums.Gender;
import com.kairos.persistence.model.country.tag.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@QueryResult
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StaffEmploymentWithTag {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private List<PlanningEmploymentDTO> employments;
    private List<Tag> tags;
    private List<EmploymentTypeDTO> employmentList;
    private String city;
    private String currentStatus;
    private String dateOfBirth;
    private Gender gender;
    private String profilePic;
    private String user_id;
    private String province;
    private List<ExpertiseDTO> expertiseList;
    private List<SkillDTO> skillList;
}
