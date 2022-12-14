package com.kairos.persistence.model.pay_table;

import com.kairos.persistence.model.organization.Level;
import org.neo4j.ogm.annotation.typeconversion.DateLong;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.Date;
import java.util.List;

/**
 * Created by vipul on 15/3/18.
 */
@QueryResult
public class PayTableQueryResult {
    private Long id;
    private String name;
    private String shortName;
    private Long startDateMillis;
    @DateLong
    private Date endDateMillis;
    private String paymentUnit;
    private Level level;
    private List<PayGrade> payGrades;
    private String description;
    private Boolean active;

    public PayTableQueryResult() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Long getStartDateMillis() {
        return startDateMillis;
    }

    public void setStartDateMillis(Long startDateMillis) {
        this.startDateMillis = startDateMillis;
    }

    public Date getEndDateMillis() {
        return endDateMillis;
    }

    public void setEndDateMillis(Date endDateMillis) {
        this.endDateMillis = endDateMillis;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public List<PayGrade> getPayGrades() {
        return payGrades;
    }

    public void setPayGrades(List<PayGrade> payGrades) {
        this.payGrades = payGrades;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public PayTableQueryResult(String name, String shortName, String description, Long startDateMillis, Date endDateMillis) {
        this.name = name;
        this.description = description;
        this.shortName = shortName;
        this.startDateMillis = startDateMillis;
        this.endDateMillis = endDateMillis;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PayTableQueryResult{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", shortName='").append(shortName).append('\'');
        sb.append(", startDateMillis=").append(startDateMillis);
        sb.append(", endDateMillis=").append(endDateMillis);
        sb.append(", paymentUnit=").append(paymentUnit);
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getPaymentUnit() {
        return paymentUnit;
    }

    public void setPaymentUnit(String paymentUnit) {
        this.paymentUnit = paymentUnit;
    }
}
