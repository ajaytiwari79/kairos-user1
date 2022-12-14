package com.kairos.persistence.model.user.expertise;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.enums.shift.BreakPaymentSetting;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.country.Country;
import com.kairos.persistence.model.organization.Level;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.organization.union.Sector;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kairos.commons.utils.DateUtils.getCurrentLocalDate;
import static com.kairos.commons.utils.DateUtils.startDateIsEqualsOrBeforeEndDate;
import static com.kairos.constants.UserMessagesConstants.ERROR_EXPERTISE_NAME_NOTNULL;
import static com.kairos.persistence.model.constants.RelationshipConstants.*;


/**
 * Created by prabjot on 28/10/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NodeEntity
@Getter
@Setter
@NoArgsConstructor
public class Expertise extends UserBaseEntity {
    private static final long serialVersionUID = 8079884125098345162L;
    @NotBlank(message = ERROR_EXPERTISE_NAME_NOTNULL)
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    @Relationship(type = BELONGS_TO)
    private Country country;

    private boolean published;

    @Relationship(type = HAS_EXPERTISE_LINES)
    private List<ExpertiseLine> expertiseLines = new ArrayList<>();

    @Relationship(type = BELONGS_TO_SECTOR)
    private Sector sector;

    @Relationship(type = IN_ORGANIZATION_LEVEL)
    private Level organizationLevel;

    @Relationship(type = SUPPORTED_BY_UNION)
    private Organization union;
    private BreakPaymentSetting breakPaymentSetting;

    public Expertise(String name, Country country) {
        this.name = name;
        this.country = country;
    }

    public Expertise(@NotBlank(message = ERROR_EXPERTISE_NAME_NOTNULL) String name, String description, LocalDate startDate, LocalDate endDate, Country country,
                     boolean published, List<ExpertiseLine> expertiseLines,BreakPaymentSetting breakPaymentSetting) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.country = country;
        this.published = published;
        this.expertiseLines = expertiseLines;
        this.breakPaymentSetting=breakPaymentSetting;
    }

    public Expertise(Long id, @NotBlank(message = ERROR_EXPERTISE_NAME_NOTNULL) String name, String description, LocalDate startDate, LocalDate endDate, boolean published, List<ExpertiseLine> expertiseLines) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.published = published;
        this.expertiseLines = expertiseLines;
    }

    public ExpertiseLine getCurrentlyActiveLine(LocalDate selectedDate) {
        selectedDate = selectedDate == null ? getCurrentLocalDate() : selectedDate;
        ExpertiseLine currentExpertiseLine = null;
        for (ExpertiseLine expertiseLine : this.getExpertiseLines()) {
            if (startDateIsEqualsOrBeforeEndDate(expertiseLine.getStartDate(),selectedDate ) &&
                    (expertiseLine.getEndDate() == null || startDateIsEqualsOrBeforeEndDate(selectedDate, expertiseLine.getEndDate()))) {
                currentExpertiseLine = expertiseLine;
                break;
            }
        }
        return currentExpertiseLine;
    }


    public Map<String, Object> retrieveDetails() {
        Map<String, Object> map = new HashMap<>(6);
        map.put("id", this.id);
        map.put("name", this.name);
        map.put("description", this.description);
        map.put("country", this.country.getName());
        map.put("lastModificationDate", this.getLastModificationDate());
        map.put("creationDate", this.getCreationDate());
        return map;
    }

}
