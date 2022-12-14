package com.kairos.persistence.model.organization.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.commons.utils.TranslationUtil;
import com.kairos.dto.TranslationInfo;
import com.kairos.enums.team.LeaderType;
import com.kairos.enums.team.TeamType;
import com.kairos.persistence.model.common.TranslationConverter;
import com.kairos.persistence.model.staff.StaffTeamDTO;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.springframework.data.neo4j.annotation.QueryResult;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.kairos.commons.utils.ObjectUtils.isCollectionEmpty;
import static com.kairos.constants.UserMessagesConstants.ERROR_NAME_NOTNULL;

/**
 * Created by prabjot on 20/1/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@QueryResult
@Getter
@Setter
public class TeamDTO {

    private Long id;
    @NotBlank(message = ERROR_NAME_NOTNULL)
    private String name;
    private String description;
    private boolean hasAddressOfUnit;
    private BigInteger activityId;
    private List<Long> skillIds;
    private Set<Long> mainTeamLeaderIds;
    private Set<Long> actingTeamLeaderIds;
    private List<StaffTeamDTO> staffDetails;
    private TeamType teamType;
    private LeaderType leaderType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int sequence;
    private boolean teamMembership;
    private Long unitId;
    private Map<String,String> translatedNames;
    private Map<String,String> translatedDescriptions;
    @Convert(TranslationConverter.class)
    private Map<String, TranslationInfo> translations;
    @AssertTrue(message = "message.same_staff.belongs_to.both_lead")
    public boolean isValid() {
        if(isCollectionEmpty(mainTeamLeaderIds) || isCollectionEmpty(actingTeamLeaderIds)){
            return true;
        }
        return !CollectionUtils.containsAny(mainTeamLeaderIds,actingTeamLeaderIds);
    }

    public String getName() {
        return TranslationUtil.getName(TranslationUtil.convertUnmodifiableMapToModifiableMap(translations),name);
    }

    public String getDescription() {
        return  TranslationUtil.getDescription(TranslationUtil.convertUnmodifiableMapToModifiableMap(translations),description);
    }
}
