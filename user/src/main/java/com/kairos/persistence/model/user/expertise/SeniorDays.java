package com.kairos.persistence.model.user.expertise;

import com.kairos.persistence.model.common.UserBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.time.LocalDate;
import java.util.List;

import static com.kairos.persistence.model.constants.RelationshipConstants.*;

@NodeEntity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeniorDays extends UserBaseEntity{
    private static final long serialVersionUID = -1427491727211230586L;
    @Relationship(type = BELONGS_TO_EXPERTISE)
    private Expertise expertise;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean published;
    @Relationship(type = HAS_CARE_DAYS)
    private List<CareDays> careDays;
    private boolean oneTimeUpdatedAfterPublish;
    @Relationship(type = VERSION_OF)
    private SeniorDays parentSeniorDays;
}
