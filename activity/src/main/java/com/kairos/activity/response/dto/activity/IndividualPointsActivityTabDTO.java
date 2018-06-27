package com.kairos.activity.response.dto.activity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.activity.persistence.model.activity.tabs.IndividualPointsActivityTab;

/**
 * Created by pawanmandhan on 23/8/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndividualPointsActivityTabDTO {

    private Long activityId;
    private String individualPointsCalculationMethod;
    private Double numberOfFixedPoints;

    public IndividualPointsActivityTab buildIndividualPointsActivityTab(){

        IndividualPointsActivityTab individualPointsActivityTab = new IndividualPointsActivityTab(individualPointsCalculationMethod,numberOfFixedPoints);

        return individualPointsActivityTab;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getIndividualPointsCalculationMethod() {
        return individualPointsCalculationMethod;
    }

    public void setIndividualPointsCalculationMethod(String individualPointsCalculationMethod) {
        this.individualPointsCalculationMethod = individualPointsCalculationMethod;
    }

    public Double getNumberOfFixedPoints() {
        return numberOfFixedPoints;
    }

    public void setNumberOfFixedPoints(Double numberOfFixedPoints) {
        this.numberOfFixedPoints = numberOfFixedPoints;
    }
}
