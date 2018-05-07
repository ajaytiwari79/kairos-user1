package com.kairos.activity.persistence.model.wta.templates.template_types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kairos.activity.enums.MinMaxSetting;
import com.kairos.activity.persistence.enums.PartOfDay;
import com.kairos.activity.persistence.enums.WTATemplateType;
import com.kairos.persistence.model.enums.TimeBankTypeEnum;
import com.kairos.activity.persistence.model.wta.templates.WTABaseRuleTemplate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by pavan on 20/2/18.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class TimeBankWTATemplate extends WTABaseRuleTemplate {
    private TimeBankTypeEnum frequency;
    private Integer yellowZone;
    private boolean forbid;
    private boolean allowExtraActivity;

    private List<PartOfDay> partOfDays = new ArrayList<>();
    private float recommendedValue;
    private MinMaxSetting minMaxSetting = MinMaxSetting.MAXIMUM;


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
    public TimeBankWTATemplate() {
        wtaTemplateType = WTATemplateType.TIME_BANK;
        //Default Constructor
    }

    public TimeBankWTATemplate(String name, boolean disabled, String description, TimeBankTypeEnum frequency, Integer yellowZone, boolean forbid, boolean allowExtraActivity) {
        this.name=name;
        this.disabled=disabled;
        this.description=description;
        this.frequency = frequency;
        this.yellowZone = yellowZone;
        this.forbid = forbid;
        this.allowExtraActivity = allowExtraActivity;
    }

    public TimeBankTypeEnum getFrequency() {
        return frequency;
    }

    public void setFrequency(TimeBankTypeEnum frequency) {
        this.frequency = frequency;
    }

    public Integer getYellowZone() {
        return yellowZone;
    }

    public void setYellowZone(Integer yellowZone) {
        this.yellowZone = yellowZone;
    }

    public boolean isForbid() {
        return forbid;
    }

    public void setForbid(boolean forbid) {
        this.forbid = forbid;
    }

    public boolean isAllowExtraActivity() {
        return allowExtraActivity;
    }

    public void setAllowExtraActivity(boolean allowExtraActivity) {
        this.allowExtraActivity = allowExtraActivity;
    }
}
