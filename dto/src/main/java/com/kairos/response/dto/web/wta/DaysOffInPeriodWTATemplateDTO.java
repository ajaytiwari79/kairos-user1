package com.kairos.response.dto.web.wta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.activity.enums.MinMaxSetting;
import com.kairos.activity.persistence.enums.PartOfDay;
import com.kairos.activity.persistence.enums.WTATemplateType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pawanmandhan on 5/8/17.
 * TEMPLATE10
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaysOffInPeriodWTATemplateDTO extends WTABaseRuleTemplateDTO {

    private long intervalLength;
    private String intervalUnit;
    private long validationStartDateMillis;
    private long daysLimit;
    private WTATemplateType wtaTemplateType = WTATemplateType.DAYS_OFF_IN_PERIOD;
    private List<PartOfDay> partOfDays = new ArrayList<>();
    private float recommendedValue;
    private MinMaxSetting minMaxSetting;


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

    public String getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(String intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public long getDaysLimit() {
        return daysLimit;
    }

    public void setDaysLimit(long daysLimit) {
        this.daysLimit = daysLimit;
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

    public DaysOffInPeriodWTATemplateDTO(String name, boolean disabled,
                                         String description, long intervalLength, long validationStartDateMillis, long minimumDaysOff, String intervalUnit) {
        this.intervalLength = intervalLength;
        this.daysLimit = minimumDaysOff;
        this.validationStartDateMillis = validationStartDateMillis;

        this.name = name;
        this.disabled = disabled;
        this.description = description;
        this.intervalUnit = intervalUnit;

    }

    public DaysOffInPeriodWTATemplateDTO() {
    }
}
