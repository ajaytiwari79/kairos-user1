package com.kairos.persistence.model.staff.personal_details;

import com.kairos.dto.user.access_group.UserAccessRoleDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.DayTypeDTO;
import com.kairos.dto.user.skill.SkillLevelDTO;
import com.kairos.dto.user.staff.staff.TeamRankingInfoDTO;
import com.kairos.dto.user.team.TeamDTO;
import com.kairos.enums.StaffStatusEnum;
import com.kairos.persistence.model.country.tag.Tag;
import com.kairos.persistence.model.organization.OrganizationType;
import com.kairos.persistence.model.user.employment.query_result.StaffEmploymentDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by prabjot on 17/5/17.
 */
@QueryResult
@Getter
@Setter
@NoArgsConstructor
public class StaffAdditionalInfoQueryResult {
    private String name;
    private Long id;
    private List<Long> teams;
    private List<Long> skills;
    private String profilePic;
    private Long unitId;
    private StaffEmploymentDetails employments;
    private Date organizationNightStartTimeFrom;
    private Date organizationNightEndTimeTo;
    private List<DayTypeDTO> dayTypes;
    private ZoneId unitTimeZone;
    private UserAccessRoleDTO user;
    private UserAccessRoleDTO userAccessRoleDTO;
    private Long staffUserId;
    private String cprNumber;
    private List<StaffChildDetail> staffChildDetails;
    private List<SkillLevelDTO> skillLevelDTOS;
    private boolean countryAdmin;
    private List<Tag> tags;
    private StaffStatusEnum currentStatus;
    private Map<String, String> unitWiseAccessRole=new HashMap<>();
    private OrganizationType organizationType;
    private OrganizationType organizationSubType;
    private Set<BigInteger> mainTeamActivities;
    private List<TeamDTO> teamsData;
    private List<TeamRankingInfoDTO> staffTeamRankingInfoData;

}
