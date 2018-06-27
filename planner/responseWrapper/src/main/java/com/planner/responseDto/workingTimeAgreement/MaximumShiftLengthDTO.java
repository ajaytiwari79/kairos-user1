package com.planner.responseDto.workingTimeAgreement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by Pradeep singh on 4/8/17.
 * TEMPLATE1
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaximumShiftLengthDTO {

    private long timeLimit;
    private List<String> balanceType;//multiple check boxes
    private boolean checkAgainstTimeRules;
    private int weight;
    private String templateType;

    public MaximumShiftLengthDTO(long timeLimit, int weight, String level) {
        this.timeLimit = timeLimit;
        this.weight = weight;

    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }


    public MaximumShiftLengthDTO(long timeLimit, List<String> balanceType, boolean checkAgainstTimeRules) {
        this.timeLimit = timeLimit;
        this.balanceType = balanceType;
        this.checkAgainstTimeRules = checkAgainstTimeRules;
    }

    public MaximumShiftLengthDTO() {

    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public List<String> getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(List<String> balanceType) {
        this.balanceType = balanceType;
    }

    public boolean isCheckAgainstTimeRules() {
        return checkAgainstTimeRules;
    }

    public void setCheckAgainstTimeRules(boolean checkAgainstTimeRules) {
        this.checkAgainstTimeRules = checkAgainstTimeRules;
    }


}
