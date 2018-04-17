package com.kairos.activity.persistence.model.wta.templates.template_types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.activity.persistence.enums.WTATemplateType;
import com.kairos.activity.persistence.model.wta.templates.WTABaseRuleTemplate;
import org.springframework.data.mongodb.core.mapping.Document;


import java.math.BigInteger;
import java.util.List;

/**
 * Created by pawanmandhan on 5/8/17.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftsInIntervalWTATemplate extends WTABaseRuleTemplate {
    private List<String> balanceType;//multiple check boxes
    private long intervalLength;//
    private String intervalUnit;
    private long validationStartDateMillis;
    private long shiftsLimit;
    private boolean onlyCompositeShifts;//(checkbox)
    private WTATemplateType wtaTemplateType = WTATemplateType.NUMBER_OF_SHIFTS_IN_INTERVAL;

    private List<BigInteger> timeTypeIds;
    private List<BigInteger> activityIds;
    private List<Long> plannedTimeIds;

    public List<BigInteger> getTimeTypeIds() {
        return timeTypeIds;
    }

    public void setTimeTypeIds(List<BigInteger> timeTypeIds) {
        this.timeTypeIds = timeTypeIds;
    }

    public List<BigInteger> getActivityIds() {
        return activityIds;
    }

    public void setActivityIds(List<BigInteger> activityIds) {
        this.activityIds = activityIds;
    }

    public List<Long> getPlannedTimeIds() {
        return plannedTimeIds;
    }

    public void setPlannedTimeIds(List<Long> plannedTimeIds) {
        this.plannedTimeIds = plannedTimeIds;
    }

    public WTATemplateType getWtaTemplateType() {
        return wtaTemplateType;
    }

    public void setWtaTemplateType(WTATemplateType wtaTemplateType) {
        this.wtaTemplateType = wtaTemplateType;
    }
    public List<String> getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(List<String> balanceType) {
        this.balanceType = balanceType;
    }

    public long getIntervalLength() {
        return intervalLength;
    }

    public void setIntervalLength(long intervalLength) {
        this.intervalLength = intervalLength;
    }

    public String getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(String intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public long getValidationStartDateMillis() {
        return validationStartDateMillis;
    }

    public void setValidationStartDateMillis(long validationStartDateMillis) {
        this.validationStartDateMillis = validationStartDateMillis;
    }

    public long getShiftsLimit() {
        return shiftsLimit;
    }

    public void setShiftsLimit(long shiftsLimit) {
        this.shiftsLimit = shiftsLimit;
    }

    public boolean isOnlyCompositeShifts() {
        return onlyCompositeShifts;
    }

    public void setOnlyCompositeShifts(boolean onlyCompositeShifts) {
        this.onlyCompositeShifts = onlyCompositeShifts;
    }

    public ShiftsInIntervalWTATemplate(String name,  boolean disabled,
                                       String description, long intervalLength, String intervalUnit,
                                       long validationStartDateMillis, long shiftsLimit, boolean onlyCompositeShifts) {
        this.name = name;
        this.disabled = disabled;
        this.description = description;
        this.balanceType = balanceType;
        this.intervalLength =intervalLength;
        this.intervalUnit=intervalUnit;
        this.validationStartDateMillis =validationStartDateMillis;
        this.shiftsLimit =shiftsLimit;
        this.onlyCompositeShifts=onlyCompositeShifts;

    }
    public ShiftsInIntervalWTATemplate() {
    }
}
