package com.kairos.dto.activity.wta.templates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.dto.activity.wta.basic_details.WTABaseRuleTemplateDTO;
import com.kairos.enums.wta.MinMaxSetting;
import com.kairos.enums.wta.PartOfDay;
import com.kairos.enums.wta.WTATemplateType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by pawanmandhan on 5/8/17.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeeklyRestPeriodWTATemplateDTO extends WTABaseRuleTemplateDTO {

    private long continuousWeekRest;
    private List<BigInteger> plannedTimeIds = new ArrayList<>();
    private List<BigInteger> timeTypeIds = new ArrayList<>();

    protected List<PartOfDay> partOfDays = new ArrayList<>();
    protected float recommendedValue;
    private MinMaxSetting minMaxSetting;
    @Positive(message = "message.ruleTemplate.interval.notNull")
    private long intervalLength;//
    @NotEmpty(message = "message.ruleTemplate.interval.notNull")
    private String intervalUnit;


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

    public MinMaxSetting getMinMaxSetting() {
        return minMaxSetting;
    }

    public void setMinMaxSetting(MinMaxSetting minMaxSetting) {
        this.minMaxSetting = minMaxSetting;
    }

    public List<PartOfDay> getPartOfDays() {
        return partOfDays;
    }

    public void setPartOfDays(List<PartOfDay> partOfDays) {
        this.partOfDays = partOfDays;
    }

    public float getRecommendedValue() {
        return recommendedValue;
    }

    public void setRecommendedValue(float recommendedValue) {
        this.recommendedValue = recommendedValue;
    }


    public WTATemplateType getWtaTemplateType() {
        return wtaTemplateType;
    }

    public void setWtaTemplateType(WTATemplateType wtaTemplateType) {
        this.wtaTemplateType = wtaTemplateType;
    }
    public long getContinuousWeekRest() {
        return continuousWeekRest;
    }

    public void setContinuousWeekRest(long continuousWeekRest) {
        this.continuousWeekRest = continuousWeekRest;
    }

    public List<BigInteger> getPlannedTimeIds() {
        return plannedTimeIds;
    }

    public void setPlannedTimeIds(List<BigInteger> plannedTimeIds) {
        this.plannedTimeIds = plannedTimeIds;
    }

    public List<BigInteger> getTimeTypeIds() {
        return timeTypeIds;
    }

    public void setTimeTypeIds(List<BigInteger> timeTypeIds) {
        this.timeTypeIds = timeTypeIds;
    }

    public WeeklyRestPeriodWTATemplateDTO(String name, boolean disabled,
                                          String description, long continuousWeekRest) {
        this.name = name;
        this.disabled = disabled;
        this.description = description;

        this.continuousWeekRest=continuousWeekRest;

    }

    public WeeklyRestPeriodWTATemplateDTO() {
        this.wtaTemplateType = WTATemplateType.WEEKLY_REST_PERIOD;;
    }
}
