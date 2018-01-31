package com.kairos.persistence.model.user.agreement.wta.templates.template_types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.persistence.model.user.agreement.wta.templates.WTABaseRuleTemplate;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.List;

/**
 * Created by pawanmandhan on 5/8/17.
 * TEMPLATE9
 */
@NodeEntity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaximumNumberOfNightsWTATemplate extends WTABaseRuleTemplate {

    private List<String> balanceType;//multiple check boxes
    private long nightsWorked;
    private long intervalLength;
    private String intervalUnit;
    private long validationStartDateMillis;


    public String getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(String intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public List<String> getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(List<String> balanceType) {
        this.balanceType = balanceType;
    }

    public long getNightsWorked() {
        return nightsWorked;
    }

    public void setNightsWorked(long nightsWorked) {
        this.nightsWorked = nightsWorked;
    }

    public long getIntervalLength() {
        return intervalLength;
    }

    public void setIntervalLength(long intervalLength) {
        this.intervalLength = intervalLength;
    }

    public long getValidationStartDateMillis() {
        return validationStartDateMillis;
    }

    public void setValidationStartDateMillis(long validationStartDateMillis) {
        this.validationStartDateMillis = validationStartDateMillis;
    }

    public MaximumNumberOfNightsWTATemplate(String name, String templateType, boolean disabled, String description, List<String> balanceType, long nightsWorked, long intervalLength, long validationStartDateMillis, String intervalUnit) {
        this.nightsWorked = nightsWorked;
        this.balanceType = balanceType;
        this.intervalLength =intervalLength;
        this.validationStartDateMillis =validationStartDateMillis;;
        this.name = name;
        this.templateType = templateType;
        this.disabled = disabled;
        this.description = description;
        this.intervalUnit = intervalUnit;
    }
    public MaximumNumberOfNightsWTATemplate() {
    }



}
