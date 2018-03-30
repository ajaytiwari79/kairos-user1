package com.kairos.persistence.model.user.staff;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.user.expertise.Expertise;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.typeconversion.DateLong;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.Date;

import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_EXPERTISE_IN;
import static com.kairos.persistence.model.constants.RelationshipConstants.STAFF_HAS_EXPERTISE;

/**
 * Created by pavan on 27/3/18.
 */

@RelationshipEntity(type = STAFF_HAS_EXPERTISE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@QueryResult
public class StaffExpertiseRelationShip extends UserBaseEntity{
    @StartNode
    private Staff staff;
    @EndNode
    private Expertise expertise;
    private Integer relevantExperienceInMonths;
    @DateLong
    private Date expertiseStartDate;

    public StaffExpertiseRelationShip() {
        //Default Constructor
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Expertise getExpertise() {
        return expertise;
    }

    public void setExpertise(Expertise expertise) {
        this.expertise = expertise;
    }

    public Integer getRelevantExperienceInMonths() {
        return relevantExperienceInMonths;
    }

    public void setRelevantExperienceInMonths(Integer relevantExperienceInMonths) {
        this.relevantExperienceInMonths = relevantExperienceInMonths;
    }

    public Date getExpertiseStartDate() {
        return expertiseStartDate;
    }

    public void setExpertiseStartDate(Date expertiseStartDate) {
        this.expertiseStartDate = expertiseStartDate;
    }

    public StaffExpertiseRelationShip(Long id,Staff staff, Expertise expertise, Integer relevantExperienceInMonths,Date expertiseStartDate) {
        this.id=id;
        this.staff = staff;
        this.expertise = expertise;
        this.relevantExperienceInMonths = relevantExperienceInMonths;
        this.expertiseStartDate=expertiseStartDate;
    }


    @Override
    public String toString() {
        return "StaffExpertiseRelationShip{" +
                "staff=" + staff +
                ", expertise=" + expertise +
                ", relevantExperienceInMonths=" + relevantExperienceInMonths +
                '}';
    }
}
