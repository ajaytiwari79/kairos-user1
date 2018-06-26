package com.kairos.activity.response.dto.activity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.activity.persistence.model.activity.tabs.BalanceSettingsActivityTab;

import java.math.BigInteger;

/**
 * Created by pawanmandhan on 22/8/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceSettingActivityTabDTO {


    private Long activityId;
    private Integer addTimeTo;
    private BigInteger timeTypeId;
    private boolean onCallTimePresent;
    private Boolean negativeDayBalancePresent;


    public BalanceSettingsActivityTab buildBalanceSettingsActivityTab() {
        BalanceSettingsActivityTab balanceSettingsActivityTab = new BalanceSettingsActivityTab(addTimeTo, timeTypeId, onCallTimePresent, negativeDayBalancePresent);
        return balanceSettingsActivityTab;
    }


    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Boolean getOnCallTimePresent() {
        return onCallTimePresent;
    }

    public void setOnCallTimePresent(Boolean onCallTimePresent) {
        this.onCallTimePresent = onCallTimePresent;
    }

    public Boolean getNegativeDayBalancePresent() {
        return negativeDayBalancePresent;
    }

    public void setNegativeDayBalancePresent(Boolean negativeDayBalancePresent) {
        this.negativeDayBalancePresent = negativeDayBalancePresent;
    }

    public Integer getAddTimeTo() {
        return addTimeTo;
    }

    public void setAddTimeTo(Integer addTimeTo) {
        this.addTimeTo = addTimeTo;
    }

    public BigInteger getTimeTypeId() {
        return timeTypeId;
    }

    public void setTimeTypeId(BigInteger timeTypeId) {
        this.timeTypeId = timeTypeId;
    }

    public boolean isOnCallTimePresent() {
        return onCallTimePresent;
    }

    public void setOnCallTimePresent(boolean onCallTimePresent) {
        this.onCallTimePresent = onCallTimePresent;
    }
}
