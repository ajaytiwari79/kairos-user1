package com.kairos.persistence.model.user.expertise;

import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.user.pay_group_area.PayGroupArea;
import com.kairos.user.pay_table.PayGrade;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.math.BigDecimal;
import java.util.List;

import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_BASE_PAY_GRADE;
import static com.kairos.persistence.model.constants.RelationshipConstants.HAS_PAY_GROUP_AREA;

/**
 * Created by vipul on 27/3/18.
 */
@NodeEntity
public class SeniorityLevel extends UserBaseEntity implements Comparable<SeniorityLevel> {
    private Integer from;
    private Integer to;

    @Relationship(type = HAS_BASE_PAY_GRADE)
    private PayGrade payGrade;  // this is payGrade which is coming from payTable

    // TODO We are unclear about this just adding and make sure this will utilize in future.
    private BigDecimal pensionPercentage;
    private BigDecimal freeChoicePercentage;
    private BigDecimal freeChoiceToPension;
    private boolean published;

    public SeniorityLevel() {
        // de
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public PayGrade getPayGrade() {
        return payGrade;
    }

    public void setPayGrade(PayGrade payGrade) {
        this.payGrade = payGrade;
    }

    public BigDecimal getPensionPercentage() {
        return pensionPercentage;
    }

    public void setPensionPercentage(BigDecimal pensionPercentage) {
        this.pensionPercentage = pensionPercentage;
    }

    public BigDecimal getFreeChoicePercentage() {
        return freeChoicePercentage;
    }

    public void setFreeChoicePercentage(BigDecimal freeChoicePercentage) {
        this.freeChoicePercentage = freeChoicePercentage;
    }

    public BigDecimal getFreeChoiceToPension() {
        return freeChoiceToPension;
    }

    public void setFreeChoiceToPension(BigDecimal freeChoiceToPension) {
        this.freeChoiceToPension = freeChoiceToPension;
    }

    @Override
    public int compareTo(SeniorityLevel seniorityLevel) {
        return seniorityLevel.from - this.from;
    }
}
