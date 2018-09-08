package com.kairos.activity.wta.templates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.activity.wta.basic_details.WTABaseRuleTemplateDTO;
import com.kairos.enums.MinMaxSetting;
import com.kairos.enums.WTATemplateType;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;


/**
 * Created by pawanmandhan on 5/8/17.
 * TEMPLATE12
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VetoPerPeriodWTATemplateDTO extends WTABaseRuleTemplateDTO {

    private List<BigInteger> activityIds;
    private float recommendedValue;
    private MinMaxSetting minMaxSetting;
    private int numberOfWeeks;
    private LocalDate validationStartDate;
    private Long vetoPerPeriod;

    public int getNumberOfWeeks() {
        return numberOfWeeks;
    }

    public void setNumberOfWeeks(int numberOfWeeks) {
        this.numberOfWeeks = numberOfWeeks;
    }

    public LocalDate getValidationStartDate() {
        return validationStartDate;
    }

    public void setValidationStartDate(LocalDate validationStartDate) {
        this.validationStartDate = validationStartDate;
    }

    public MinMaxSetting getMinMaxSetting() {
        return minMaxSetting;
    }

    public void setMinMaxSetting(MinMaxSetting minMaxSetting) {
        this.minMaxSetting = minMaxSetting;
    }


    public float getRecommendedValue() {
        return recommendedValue;
    }

    public void setRecommendedValue(float recommendedValue) {
        this.recommendedValue = recommendedValue;
    }

    public List<BigInteger> getActivityIds() {
        return activityIds;
    }

    public void setActivityIds(List<BigInteger> activityIds) {
        this.activityIds = activityIds;
    }

    public WTATemplateType getWtaTemplateType() {
        return wtaTemplateType;
    }

    public void setWtaTemplateType(WTATemplateType wtaTemplateType) {
        this.wtaTemplateType = wtaTemplateType;
    }

    public Long getVetoPerPeriod() {
        return vetoPerPeriod;
    }

    public void setVetoPerPeriod(Long vetoPerPeriod) {
        this.vetoPerPeriod = vetoPerPeriod;
    }

    public VetoPerPeriodWTATemplateDTO(String name, boolean disabled,
                                       String description) {
        this.name = name;
        this.disabled = disabled;
        this.description = description;

    }
    public VetoPerPeriodWTATemplateDTO() {
        this.wtaTemplateType = WTATemplateType.VETO_PER_PERIOD;;
    }

}
