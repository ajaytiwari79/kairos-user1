package com.kairos.persistence.model.user.employment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.enums.EmploymentSubType;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.organization.Unit;
import com.kairos.persistence.model.staff.personal_details.Staff;
import com.kairos.persistence.model.user.expertise.Expertise;
import lombok.Getter;
import lombok.Setter;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.kairos.persistence.model.constants.RelationshipConstants.*;


/**
 * Created by pawanmandhan on 24/7/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@NodeEntity
@Getter
@Setter
public class Employment extends UserBaseEntity {

    private static final long serialVersionUID = 1387806326762328484L;
    @Relationship(type = HAS_EXPERTISE_IN)
    private Expertise expertise;

    @Relationship(type = BELONGS_TO_STAFF, direction = "INCOMING")
    private Staff staff;

    @Relationship(type = SUPPORTED_BY_UNION)
    private Organization union;

    @Relationship(type = IN_UNIT)
    private Unit unit;

    private BigInteger reasonCodeId;


    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate lastWorkingDate;
    private Long timeCareExternalId;
    private boolean published;
    @Relationship(type = HAS_EMPLOYMENT_LINES)
    private List<EmploymentLine> employmentLines;
    private EmploymentSubType employmentSubType;
    private float taxDeductionPercentage;
    //This is the Intial value of accumulatedTimebank
    private long accumulatedTimebankMinutes;
    private LocalDate accumulatedTimebankDate;
    private boolean nightWorker;

    public Employment() {

    }

    public Employment(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Employment(Unit unit, LocalDate startDate, Long timeCareExternalId, boolean published, float taxDeductionPercentage, long accumulatedTimebankMinutes, LocalDate accumulatedTimebankDate) {
        this.unit = unit;
        this.startDate = startDate;
        this.timeCareExternalId = timeCareExternalId;
        this.published=published;
        this.taxDeductionPercentage=taxDeductionPercentage;
        this.accumulatedTimebankMinutes = accumulatedTimebankMinutes;
        this.accumulatedTimebankDate = accumulatedTimebankDate;
    }

    public List<EmploymentLine> getEmploymentLines() {
        return Optional.ofNullable(employmentLines).orElse(new ArrayList<>());
    }


    @Override
    public String toString() {
        return "Employment{" +
                "expertise=" + expertise +
                ", staff=" + staff +
                ", union=" + union +
                ", unit=" + unit +
                ", reasonCodeId=" + reasonCodeId +
                ", timeCareExternalId=" + timeCareExternalId +
                ", published=" + published +
                '}';
    }
}