package com.kairos.dto.activity.kpi;



import com.kairos.dto.activity.time_bank.EmploymentWithCtaDetailsDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.DayTypeDTO;
import com.kairos.dto.user.country.tag.TagDTO;
import com.kairos.dto.user.skill.SkillLevelDTO;
import com.kairos.dto.user.staff.staff.StaffChildDetailDTO;
import com.kairos.dto.user.team.TeamDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.isNull;
import static com.kairos.utils.CPRUtil.getAgeByCPRNumberAndStartDate;

@Getter
@Setter
public class StaffKpiFilterDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private List<Long> unitIds;
    private Long unitId;
    private String unitName;
    private String cprNumber;
    private int staffAge;
    private List<EmploymentWithCtaDetailsDTO> employment;
    private List<DayTypeDTO> dayTypeDTOS;
    private List<TeamDTO> teams;
    private List<TagDTO> tags;
    private List<SkillLevelDTO> skills = new ArrayList<>();
    private List<StaffChildDetailDTO> staffChildDetails;
    private long payTableAmount;


    public String getFullName(){
        return this.firstName+" "+this.getLastName();
    }

    public int getStaffAge(LocalDate localDate) {
        return isNull(this.cprNumber) ? 0 : getAgeByCPRNumberAndStartDate(this.cprNumber,localDate);
    }
    public boolean isTagValid(Set<Long> tagIds){
        return tags.stream().anyMatch(tag->tagIds.contains(tag.getId()));
    }

    public List<SkillLevelDTO> getSkillsByLocalDate(LocalDate localDate){
        return this.skills.stream().filter(skillLevelDTO -> skillLevelDTO.isValidSkillsByLocalDate(localDate)).collect(Collectors.toList());
    }

}
