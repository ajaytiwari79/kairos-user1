package com.kairos.persistence.model.wta.templates.template_types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.enums.wta.WTATemplateType;
import com.kairos.persistence.model.wta.templates.WTABaseRuleTemplate;
import com.kairos.wrapper.wta.RuleTemplateSpecificInfo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.Objects;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaysOffAfterASeriesWTATemplate extends WTABaseRuleTemplate {

    @Positive
    private long intervalLength;
    @NotEmpty(message = "message.ruleTemplate.interval.notNull")
    private String intervalUnit;
    private int nightShiftSequence;
    private boolean restingTimeAllowed;
    private int restingTime;

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

    public int getNightShiftSequence() {
        return nightShiftSequence;
    }

    public void setNightShiftSequence(int nightShiftSequence) {
        this.nightShiftSequence = nightShiftSequence;
    }

    public boolean isRestingTimeAllowed() {
        return restingTimeAllowed;
    }

    public void setRestingTimeAllowed(boolean restingTimeAllowed) {
        this.restingTimeAllowed = restingTimeAllowed;
    }

    public int getRestingTime() {
        return restingTime;
    }

    public void setRestingTime(int restingTime) {
        this.restingTime = restingTime;
    }

    public WTATemplateType getWtaTemplateType() {
        return wtaTemplateType;
    }

    public void setWtaTemplateType(WTATemplateType wtaTemplateType) {
        this.wtaTemplateType = wtaTemplateType;
    }
    public DaysOffAfterASeriesWTATemplate() {
        wtaTemplateType = WTATemplateType.DAYS_OFF_AFTER_A_SERIES;
    }

    @Override
    public void validateRules(RuleTemplateSpecificInfo infoWrapper) {

    }

    public DaysOffAfterASeriesWTATemplate(String name, boolean disabled, String description, long intervalLength, String intervalUnit, int nightShiftSequence) {
        this.name=name;
        this.disabled=disabled;
        this.description=description;
        this.intervalLength = intervalLength;
        this.intervalUnit = intervalUnit;
        this.nightShiftSequence = nightShiftSequence;
        wtaTemplateType=WTATemplateType.DAYS_OFF_AFTER_A_SERIES;
    }


    @Override
    public boolean isCalculatedValueChanged(WTABaseRuleTemplate wtaBaseRuleTemplate) {
        DaysOffAfterASeriesWTATemplate daysOffAfterASeriesWTATemplate = (DaysOffAfterASeriesWTATemplate)wtaBaseRuleTemplate;
        return (this != daysOffAfterASeriesWTATemplate) && !(intervalLength == daysOffAfterASeriesWTATemplate.intervalLength &&
                nightShiftSequence == daysOffAfterASeriesWTATemplate.nightShiftSequence &&
                restingTimeAllowed == daysOffAfterASeriesWTATemplate.restingTimeAllowed &&
                restingTime == daysOffAfterASeriesWTATemplate.restingTime &&
                Objects.equals(intervalUnit, daysOffAfterASeriesWTATemplate.intervalUnit) && Objects.equals(this.phaseTemplateValues,daysOffAfterASeriesWTATemplate.phaseTemplateValues));
    }


}
